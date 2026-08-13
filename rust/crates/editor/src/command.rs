//! Persistent, deterministic mutations for schematic documents.
//!
//! Commands are deliberately UI-agnostic: shells create commands, this module
//! applies them atomically, and [`crate::History`] stores their exact inverses.

use std::collections::BTreeMap;

use athena_domain::{
    Annotation, AnnotationId, DomainError, FieldValue, Junction, JunctionId, Point, Project,
    ProjectId, SheetId, SheetSettings, SymbolInstance, SymbolInstanceId, TerminalId, Wire,
    WireEndpoint, WireId, canonical_wire_route,
};
use serde::{Deserialize, Serialize};
use thiserror::Error;
use uuid::Uuid;

use crate::validation;

/// A saved-sheet entity that can be selected and modified as one editor item.
#[derive(Clone, Copy, Debug, Deserialize, Eq, Ord, PartialEq, PartialOrd, Serialize)]
pub enum ItemId {
    Symbol(SymbolInstanceId),
    Wire(WireId),
    Junction(JunctionId),
    Annotation(AnnotationId),
}

/// A field-bearing item in the active sheet.
#[derive(Clone, Copy, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub enum FieldTarget {
    Symbol(SymbolInstanceId),
    Wire(WireId),
    Annotation(AnnotationId),
}

/// Identifies which endpoint of a wire a reconnect command changes.
///
/// The type is serialized as part of a command envelope, so desktop and WASM
/// sessions use identical endpoint semantics.
#[derive(Clone, Copy, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub enum WireSide {
    /// The first route point and `Wire::start` endpoint.
    Start,
    /// The last route point and `Wire::end` endpoint.
    End,
}

/// Deterministic mutations supported by the schematic editor.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub enum EditorCommand {
    PlaceSymbol {
        sheet_id: SheetId,
        symbol: SymbolInstance,
    },
    MoveItems {
        sheet_id: SheetId,
        items: Vec<ItemId>,
        delta: Point,
    },
    RotateItems {
        sheet_id: SheetId,
        items: Vec<ItemId>,
        quarter_turns: u8,
    },
    MirrorItems {
        sheet_id: SheetId,
        items: Vec<ItemId>,
    },
    DeleteItems {
        sheet_id: SheetId,
        items: Vec<ItemId>,
    },
    CreateWire {
        sheet_id: SheetId,
        wire: Wire,
    },
    SplitWire {
        sheet_id: SheetId,
        wire_id: WireId,
        junction: Junction,
        first_wire: Wire,
        second_wire: Wire,
    },
    DeleteWire {
        sheet_id: SheetId,
        wire_id: WireId,
    },
    /// Rebinds one wire endpoint to a terminal on the same sheet.
    ReconnectWireEndpoint {
        sheet_id: SheetId,
        wire_id: WireId,
        endpoint: WireSide,
        terminal_id: TerminalId,
    },
    /// Adds a route vertex on an existing wire segment.
    InsertWireVertex {
        sheet_id: SheetId,
        wire_id: WireId,
        segment_index: usize,
        position: Point,
    },
    /// Moves an interior route vertex while retaining an orthogonal route.
    MoveWireVertex {
        sheet_id: SheetId,
        wire_id: WireId,
        vertex_index: usize,
        position: Point,
    },
    /// Removes an interior route vertex and normalizes adjacent bends.
    DeleteWireVertex {
        sheet_id: SheetId,
        wire_id: WireId,
        vertex_index: usize,
    },
    SetFieldValue {
        sheet_id: SheetId,
        target: FieldTarget,
        field: String,
        value: Option<FieldValue>,
    },
    ApplySheetSettings {
        sheet_id: SheetId,
        settings: SheetSettings,
    },
    /// Internal, serializable inverse used by deterministic undo/redo.
    #[doc(hidden)]
    RestoreItems {
        sheet_id: SheetId,
        remove: Vec<ItemId>,
        restore: Vec<StoredItem>,
    },
}

/// The metadata required to replay a command in a local-first or shared session.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct CommandEnvelope {
    pub operation_id: Uuid,
    pub project_id: ProjectId,
    pub sheet_id: SheetId,
    pub base_revision: u64,
    pub author_id: Uuid,
    pub session_id: Uuid,
    pub command_version: u32,
    pub payload: EditorCommand,
}

/// A command plus the exact command needed to undo it.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct AppliedCommand {
    pub command: EditorCommand,
    pub inverse: EditorCommand,
    pub before_revision: u64,
    pub after_revision: u64,
}

/// Platform-neutral editable project state.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct EditorState {
    project: Project,
    revision: u64,
}

impl EditorState {
    #[must_use]
    pub fn new(project: Project) -> Self {
        Self {
            project,
            revision: 0,
        }
    }

    #[must_use]
    pub fn project(&self) -> &Project {
        &self.project
    }

    #[must_use]
    pub const fn revision(&self) -> u64 {
        self.revision
    }

    /// Applies a validated command atomically.
    ///
    /// The real project is only replaced after command validation, candidate
    /// mutation, and full domain validation all succeed.
    pub fn apply(&mut self, command: EditorCommand) -> Result<AppliedCommand, ApplyError> {
        validation::validate_command(&self.project, &command)?;

        let before_revision = self.revision;
        let after_revision = before_revision
            .checked_add(1)
            .ok_or(ApplyError::RevisionOverflow)?;
        let mut candidate = self.project.clone();
        let inverse = apply_to_project(&mut candidate, &command)?;
        candidate.validate().map_err(ApplyError::InvalidProject)?;

        self.project = candidate;
        self.revision = after_revision;

        Ok(AppliedCommand {
            command,
            inverse,
            before_revision,
            after_revision,
        })
    }
}

/// A saved entity record used exclusively by a deterministic inverse command.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub enum StoredItem {
    Symbol(SymbolInstance),
    Wire(Wire),
    Junction(Junction),
    Annotation(Annotation),
}

impl StoredItem {
    #[must_use]
    pub fn item_id(&self) -> ItemId {
        match self {
            Self::Symbol(symbol) => ItemId::Symbol(symbol.id),
            Self::Wire(wire) => ItemId::Wire(wire.id),
            Self::Junction(junction) => ItemId::Junction(junction.id),
            Self::Annotation(annotation) => ItemId::Annotation(annotation.id),
        }
    }
}

/// Errors returned before any mutation becomes visible in the editor state.
#[derive(Clone, Debug, Error, Eq, PartialEq)]
pub enum ApplyError {
    #[error("sheet {sheet_id} does not exist")]
    UnknownSheet { sheet_id: SheetId },
    #[error("item {item:?} does not exist on sheet {sheet_id}")]
    UnknownItem { sheet_id: SheetId, item: ItemId },
    #[error("symbol definition {definition_id} does not exist")]
    UnknownSymbolDefinition {
        definition_id: athena_domain::SymbolDefinitionId,
    },
    #[error("wire endpoint refers to unknown terminal {terminal_id} on sheet {sheet_id}")]
    UnknownTerminal {
        sheet_id: SheetId,
        terminal_id: TerminalId,
    },
    #[error("wire endpoint refers to unknown junction {junction_id} on sheet {sheet_id}")]
    UnknownJunction {
        sheet_id: SheetId,
        junction_id: JunctionId,
    },
    /// A reconnect would bind both endpoints of one wire to the same terminal.
    #[error("wire {wire_id} cannot use terminal {terminal_id} for both endpoints")]
    WireEndpointsShareTerminal {
        wire_id: WireId,
        terminal_id: TerminalId,
    },
    #[error("command repeats item {item:?}")]
    DuplicateItem { item: ItemId },
    #[error("command must target at least one item")]
    EmptyItemSelection,
    #[error("symbol instance {symbol_id} already exists on sheet {sheet_id}")]
    DuplicateSymbol {
        sheet_id: SheetId,
        symbol_id: SymbolInstanceId,
    },
    #[error("wire {wire_id} already exists on sheet {sheet_id}")]
    DuplicateWire { sheet_id: SheetId, wire_id: WireId },
    #[error("junction {junction_id} already exists on sheet {sheet_id}")]
    DuplicateJunction {
        sheet_id: SheetId,
        junction_id: JunctionId,
    },
    #[error("split wire {wire_id} must produce two distinct wires joined at its new junction")]
    InvalidWireSplit { wire_id: WireId },
    #[error("wire {wire_id} does not contain segment {segment_index}")]
    InvalidWireSegmentIndex {
        wire_id: WireId,
        segment_index: usize,
    },
    #[error("wire {wire_id} does not contain an editable interior vertex at index {vertex_index}")]
    InvalidWireVertexIndex {
        wire_id: WireId,
        vertex_index: usize,
    },
    #[error("wire {wire_id} vertex position must lie strictly on segment {segment_index}")]
    WireVertexNotOnSegment {
        wire_id: WireId,
        segment_index: usize,
    },
    #[error("wire {wire_id} must contain at least a start and end route point")]
    WireRouteTooShort { wire_id: WireId },
    #[error("wire {wire_id} route endpoint {endpoint:?} does not match its attached entity")]
    WireRouteEndpointMismatch { wire_id: WireId, endpoint: WireSide },
    #[error("wire {wire_id} route contains a zero-length segment at index {segment_index}")]
    ZeroLengthWireSegment {
        wire_id: WireId,
        segment_index: usize,
    },
    #[error("wire {wire_id} route contains a non-orthogonal segment at index {segment_index}")]
    NonOrthogonalWireSegment {
        wire_id: WireId,
        segment_index: usize,
    },
    #[error("sheet settings must have positive page dimensions and grid spacing")]
    InvalidSheetSettings,
    #[error("coordinate arithmetic overflowed")]
    CoordinateOverflow,
    #[error("editor revision cannot advance beyond u64::MAX")]
    RevisionOverflow,
    #[error("command would violate project invariants: {0}")]
    InvalidProject(DomainError),
}

pub(crate) fn apply_to_project(
    project: &mut Project,
    command: &EditorCommand,
) -> Result<EditorCommand, ApplyError> {
    match command {
        EditorCommand::PlaceSymbol { sheet_id, symbol } => {
            project
                .sheet_mut(*sheet_id)
                .expect("validated sheet")
                .symbol_instances
                .insert(symbol.id, symbol.clone());
            Ok(EditorCommand::DeleteItems {
                sheet_id: *sheet_id,
                items: vec![ItemId::Symbol(symbol.id)],
            })
        }
        EditorCommand::MoveItems {
            sheet_id,
            items,
            delta,
        } => {
            let moved_terminal_ids =
                terminal_ids_owned_by_selected_symbols(project, *sheet_id, items)?;
            let affected_items =
                items_with_attached_wires(project, *sheet_id, items, &moved_terminal_ids)?;
            let originals = collect_items(project, *sheet_id, &affected_items)?;
            let sheet = project.sheet_mut(*sheet_id).expect("validated sheet");
            for item in items {
                move_item(sheet, *item, *delta)?;
            }
            move_attached_wire_endpoints(sheet, &moved_terminal_ids, *delta)?;
            Ok(restore_command(*sheet_id, affected_items, originals))
        }
        EditorCommand::RotateItems {
            sheet_id,
            items,
            quarter_turns,
        } => {
            let originals = collect_items(project, *sheet_id, items)?;
            let sheet = project.sheet_mut(*sheet_id).expect("validated sheet");
            for item in items {
                rotate_item(sheet, *item, *quarter_turns);
            }
            Ok(restore_command(*sheet_id, items.clone(), originals))
        }
        EditorCommand::MirrorItems { sheet_id, items } => {
            let originals = collect_items(project, *sheet_id, items)?;
            let sheet = project.sheet_mut(*sheet_id).expect("validated sheet");
            for item in items {
                mirror_item(sheet, *item);
            }
            Ok(restore_command(*sheet_id, items.clone(), originals))
        }
        EditorCommand::DeleteItems { sheet_id, items } => {
            let delete_ids = deletion_closure(project, *sheet_id, items)?;
            let originals = collect_items(project, *sheet_id, &delete_ids)?;
            let sheet = project.sheet_mut(*sheet_id).expect("validated sheet");
            for item in &delete_ids {
                remove_item_direct(sheet, *item);
            }
            Ok(restore_command(*sheet_id, delete_ids, originals))
        }
        EditorCommand::CreateWire { sheet_id, wire } => {
            let mut canonical_wire = wire.clone();
            canonical_wire.route = canonical_wire_route(&canonical_wire.route);
            project
                .sheet_mut(*sheet_id)
                .expect("validated sheet")
                .wires
                .insert(canonical_wire.id, canonical_wire);
            Ok(EditorCommand::DeleteWire {
                sheet_id: *sheet_id,
                wire_id: wire.id,
            })
        }
        EditorCommand::SplitWire {
            sheet_id,
            wire_id,
            junction,
            first_wire,
            second_wire,
        } => {
            let original = project
                .sheet(*sheet_id)
                .expect("validated sheet")
                .wire(*wire_id)
                .expect("validated wire")
                .clone();
            let mut canonical_first_wire = first_wire.clone();
            let mut canonical_second_wire = second_wire.clone();
            canonical_first_wire.route = canonical_wire_route(&canonical_first_wire.route);
            canonical_second_wire.route = canonical_wire_route(&canonical_second_wire.route);
            let sheet = project.sheet_mut(*sheet_id).expect("validated sheet");
            sheet.wires.remove(wire_id);
            sheet.junctions.insert(junction.id, junction.clone());
            sheet
                .wires
                .insert(canonical_first_wire.id, canonical_first_wire);
            sheet
                .wires
                .insert(canonical_second_wire.id, canonical_second_wire);

            Ok(EditorCommand::RestoreItems {
                sheet_id: *sheet_id,
                remove: vec![
                    ItemId::Junction(junction.id),
                    ItemId::Wire(first_wire.id),
                    ItemId::Wire(second_wire.id),
                ],
                restore: vec![StoredItem::Wire(original)],
            })
        }
        EditorCommand::DeleteWire { sheet_id, wire_id } => {
            let wire = project
                .sheet_mut(*sheet_id)
                .expect("validated sheet")
                .wires
                .remove(wire_id)
                .expect("validated wire");
            Ok(EditorCommand::CreateWire {
                sheet_id: *sheet_id,
                wire,
            })
        }
        EditorCommand::ReconnectWireEndpoint {
            sheet_id,
            wire_id,
            endpoint,
            terminal_id,
        } => {
            let original = project
                .wire(*sheet_id, *wire_id)
                .expect("validated wire")
                .clone();
            let position = project
                .terminal(*sheet_id, *terminal_id)
                .expect("validated terminal")
                .position;
            let wire = project
                .sheet_mut(*sheet_id)
                .expect("validated sheet")
                .wires
                .get_mut(wire_id)
                .expect("validated wire");
            match endpoint {
                WireSide::Start => {
                    wire.start = WireEndpoint::Terminal(*terminal_id);
                    replace_route_endpoint(&mut wire.route, true, position);
                }
                WireSide::End => {
                    wire.end = WireEndpoint::Terminal(*terminal_id);
                    replace_route_endpoint(&mut wire.route, false, position);
                }
            }
            normalize_wire_route(&mut wire.route);
            Ok(restore_wire_command(*sheet_id, original))
        }
        EditorCommand::InsertWireVertex {
            sheet_id,
            wire_id,
            segment_index,
            position,
        } => {
            let original = project
                .wire(*sheet_id, *wire_id)
                .expect("validated wire")
                .clone();
            let wire = project
                .sheet_mut(*sheet_id)
                .expect("validated sheet")
                .wires
                .get_mut(wire_id)
                .expect("validated wire");
            wire.route.insert(*segment_index + 1, *position);
            normalize_wire_route(&mut wire.route);
            Ok(restore_wire_command(*sheet_id, original))
        }
        EditorCommand::MoveWireVertex {
            sheet_id,
            wire_id,
            vertex_index,
            position,
        } => {
            let original = project
                .wire(*sheet_id, *wire_id)
                .expect("validated wire")
                .clone();
            let wire = project
                .sheet_mut(*sheet_id)
                .expect("validated sheet")
                .wires
                .get_mut(wire_id)
                .expect("validated wire");
            replace_route_vertex_with_elbows(&mut wire.route, *vertex_index, *position);
            normalize_wire_route(&mut wire.route);
            Ok(restore_wire_command(*sheet_id, original))
        }
        EditorCommand::DeleteWireVertex {
            sheet_id,
            wire_id,
            vertex_index,
        } => {
            let original = project
                .wire(*sheet_id, *wire_id)
                .expect("validated wire")
                .clone();
            let wire = project
                .sheet_mut(*sheet_id)
                .expect("validated sheet")
                .wires
                .get_mut(wire_id)
                .expect("validated wire");
            let previous = wire.route[*vertex_index - 1];
            let next = wire.route[*vertex_index + 1];
            wire.route.splice(
                *vertex_index - 1..=*vertex_index + 1,
                orthogonal_join(previous, next),
            );
            normalize_wire_route(&mut wire.route);
            Ok(restore_wire_command(*sheet_id, original))
        }
        EditorCommand::SetFieldValue {
            sheet_id,
            target,
            field,
            value,
        } => {
            let old_value = set_field_value(
                project.sheet_mut(*sheet_id).expect("validated sheet"),
                *target,
                field,
                value.clone(),
            );
            Ok(EditorCommand::SetFieldValue {
                sheet_id: *sheet_id,
                target: *target,
                field: field.clone(),
                value: old_value,
            })
        }
        EditorCommand::ApplySheetSettings { sheet_id, settings } => {
            let sheet = project.sheet_mut(*sheet_id).expect("validated sheet");
            let old_settings = std::mem::replace(&mut sheet.settings, settings.clone());
            Ok(EditorCommand::ApplySheetSettings {
                sheet_id: *sheet_id,
                settings: old_settings,
            })
        }
        EditorCommand::RestoreItems {
            sheet_id,
            remove,
            restore,
        } => {
            let sheet = project.sheet_mut(*sheet_id).expect("validated sheet");
            let previous = remove
                .iter()
                .filter_map(|item| stored_item_if_present(sheet, *item))
                .collect();
            for item in remove {
                remove_item_direct(sheet, *item);
            }
            for item in restore {
                insert_stored_item(sheet, item.clone());
            }
            Ok(EditorCommand::RestoreItems {
                sheet_id: *sheet_id,
                remove: restore.iter().map(StoredItem::item_id).collect(),
                restore: previous,
            })
        }
    }
}

fn restore_wire_command(sheet_id: SheetId, original: Wire) -> EditorCommand {
    restore_command(
        sheet_id,
        vec![ItemId::Wire(original.id)],
        vec![StoredItem::Wire(original)],
    )
}

/// Replaces one endpoint while preserving a valid route to its former neighbor.
fn replace_route_endpoint(route: &mut Vec<Point>, replace_start: bool, position: Point) {
    let neighbor = if replace_start {
        route[1]
    } else {
        route[route.len() - 2]
    };
    if replace_start {
        route.splice(0..=1, orthogonal_join(position, neighbor));
    } else {
        let last = route.len() - 1;
        route.splice(last - 1..=last, orthogonal_join(neighbor, position));
    }
}

/// Rebuilds only the two adjacent segments around a moved interior vertex.
fn replace_route_vertex_with_elbows(route: &mut Vec<Point>, index: usize, position: Point) {
    let previous = route[index - 1];
    let next = route[index + 1];
    let mut replacement = orthogonal_join(previous, position);
    let mut tail = orthogonal_join(position, next);
    tail.remove(0);
    replacement.extend(tail);
    route.splice(index - 1..=index + 1, replacement);
}

/// Creates the fewest points needed for a deterministic horizontal-first bend.
fn orthogonal_join(start: Point, end: Point) -> Vec<Point> {
    if start.x == end.x || start.y == end.y {
        vec![start, end]
    } else {
        vec![start, Point::new(end.x, start.y), end]
    }
}

/// Removes redundant bends while retaining the endpoint pair and axis alignment.
fn normalize_wire_route(route: &mut Vec<Point>) {
    *route = canonical_wire_route(route);
}

/// Returns the canonical form without assuming that an external command has
/// already supplied a valid endpoint pair. Validation checks the result before
/// the route can enter persistent project state.
fn stored_item_if_present(sheet: &athena_domain::Sheet, item: ItemId) -> Option<StoredItem> {
    match item {
        ItemId::Symbol(id) => sheet
            .symbol_instances
            .get(&id)
            .cloned()
            .map(StoredItem::Symbol),
        ItemId::Wire(id) => sheet.wires.get(&id).cloned().map(StoredItem::Wire),
        ItemId::Junction(id) => sheet.junctions.get(&id).cloned().map(StoredItem::Junction),
        ItemId::Annotation(id) => sheet
            .annotations
            .get(&id)
            .cloned()
            .map(StoredItem::Annotation),
    }
}

fn restore_command(
    sheet_id: SheetId,
    remove: Vec<ItemId>,
    restore: Vec<StoredItem>,
) -> EditorCommand {
    EditorCommand::RestoreItems {
        sheet_id,
        remove,
        restore,
    }
}

fn collect_items(
    project: &Project,
    sheet_id: SheetId,
    items: &[ItemId],
) -> Result<Vec<StoredItem>, ApplyError> {
    collect_items_from_sheet(
        project
            .sheet(sheet_id)
            .ok_or(ApplyError::UnknownSheet { sheet_id })?,
        items,
    )
}

fn collect_items_from_sheet(
    sheet: &athena_domain::Sheet,
    items: &[ItemId],
) -> Result<Vec<StoredItem>, ApplyError> {
    items
        .iter()
        .map(|item| match item {
            ItemId::Symbol(id) => sheet
                .symbol_instances
                .get(id)
                .cloned()
                .map(StoredItem::Symbol),
            ItemId::Wire(id) => sheet.wires.get(id).cloned().map(StoredItem::Wire),
            ItemId::Junction(id) => sheet.junctions.get(id).cloned().map(StoredItem::Junction),
            ItemId::Annotation(id) => sheet
                .annotations
                .get(id)
                .cloned()
                .map(StoredItem::Annotation),
        })
        .collect::<Option<Vec<_>>>()
        .ok_or_else(|| ApplyError::UnknownItem {
            sheet_id: sheet.id,
            item: items
                .iter()
                .copied()
                .find(|item| !sheet_contains_item(sheet, *item))
                .expect("a missing item exists when collection failed"),
        })
}

pub(crate) fn sheet_contains_item(sheet: &athena_domain::Sheet, item: ItemId) -> bool {
    match item {
        ItemId::Symbol(id) => sheet.symbol_instances.contains_key(&id),
        ItemId::Wire(id) => sheet.wires.contains_key(&id),
        ItemId::Junction(id) => sheet.junctions.contains_key(&id),
        ItemId::Annotation(id) => sheet.annotations.contains_key(&id),
    }
}

fn insertion_map_for_fields(
    sheet: &mut athena_domain::Sheet,
    target: FieldTarget,
) -> &mut BTreeMap<String, FieldValue> {
    match target {
        FieldTarget::Symbol(id) => {
            &mut sheet
                .symbol_instances
                .get_mut(&id)
                .expect("validated symbol target")
                .fields
        }
        FieldTarget::Wire(id) => {
            &mut sheet
                .wires
                .get_mut(&id)
                .expect("validated wire target")
                .fields
        }
        FieldTarget::Annotation(id) => {
            &mut sheet
                .annotations
                .get_mut(&id)
                .expect("validated annotation target")
                .fields
        }
    }
}

fn set_field_value(
    sheet: &mut athena_domain::Sheet,
    target: FieldTarget,
    field: &str,
    value: Option<FieldValue>,
) -> Option<FieldValue> {
    let fields = insertion_map_for_fields(sheet, target);
    match value {
        Some(value) => fields.insert(field.into(), value),
        None => fields.remove(field),
    }
}

fn move_item(
    sheet: &mut athena_domain::Sheet,
    item: ItemId,
    delta: Point,
) -> Result<(), ApplyError> {
    match item {
        ItemId::Symbol(id) => {
            let symbol = sheet
                .symbol_instances
                .get_mut(&id)
                .expect("validated symbol");
            symbol.position = translate(symbol.position, delta)?;
            for terminal in symbol.terminals.values_mut() {
                terminal.position = translate(terminal.position, delta)?;
            }
        }
        ItemId::Wire(id) => {
            let wire = sheet.wires.get_mut(&id).expect("validated wire");
            for point in &mut wire.route {
                *point = translate(*point, delta)?;
            }
        }
        ItemId::Junction(id) => {
            let junction = sheet.junctions.get_mut(&id).expect("validated junction");
            junction.position = translate(junction.position, delta)?;
        }
        ItemId::Annotation(id) => {
            let annotation = sheet
                .annotations
                .get_mut(&id)
                .expect("validated annotation");
            annotation.position = translate(annotation.position, delta)?;
        }
    }
    Ok(())
}

fn terminal_ids_owned_by_selected_symbols(
    project: &Project,
    sheet_id: SheetId,
    items: &[ItemId],
) -> Result<BTreeMap<TerminalId, Point>, ApplyError> {
    let sheet = project
        .sheet(sheet_id)
        .ok_or(ApplyError::UnknownSheet { sheet_id })?;
    Ok(items
        .iter()
        .filter_map(|item| match item {
            ItemId::Symbol(symbol_id) => sheet.symbol_instances.get(symbol_id),
            _ => None,
        })
        .flat_map(|symbol| {
            symbol
                .terminals
                .iter()
                .map(|(terminal_id, terminal)| (*terminal_id, terminal.position))
        })
        .collect())
}

fn items_with_attached_wires(
    project: &Project,
    sheet_id: SheetId,
    selected_items: &[ItemId],
    moved_terminal_ids: &BTreeMap<TerminalId, Point>,
) -> Result<Vec<ItemId>, ApplyError> {
    let sheet = project
        .sheet(sheet_id)
        .ok_or(ApplyError::UnknownSheet { sheet_id })?;
    let mut items = selected_items
        .iter()
        .copied()
        .collect::<std::collections::BTreeSet<_>>();
    for (wire_id, wire) in &sheet.wires {
        if endpoint_references_moved_terminal(&wire.start, moved_terminal_ids)
            || endpoint_references_moved_terminal(&wire.end, moved_terminal_ids)
        {
            items.insert(ItemId::Wire(*wire_id));
        }
    }
    Ok(items.into_iter().collect())
}

fn move_attached_wire_endpoints(
    sheet: &mut athena_domain::Sheet,
    moved_terminal_ids: &BTreeMap<TerminalId, Point>,
    delta: Point,
) -> Result<(), ApplyError> {
    for wire in sheet.wires.values_mut() {
        if let Some(position) = endpoint_terminal_position(&wire.start, moved_terminal_ids) {
            replace_route_endpoint(&mut wire.route, true, translate(position, delta)?);
        }
        if let Some(position) = endpoint_terminal_position(&wire.end, moved_terminal_ids) {
            replace_route_endpoint(&mut wire.route, false, translate(position, delta)?);
        }
        normalize_wire_route(&mut wire.route);
    }
    Ok(())
}

fn endpoint_references_moved_terminal(
    endpoint: &WireEndpoint,
    moved_terminal_ids: &BTreeMap<TerminalId, Point>,
) -> bool {
    matches!(endpoint, WireEndpoint::Terminal(terminal_id) if moved_terminal_ids.contains_key(terminal_id))
}

fn endpoint_terminal_position(
    endpoint: &WireEndpoint,
    moved_terminal_ids: &BTreeMap<TerminalId, Point>,
) -> Option<Point> {
    match endpoint {
        WireEndpoint::Terminal(terminal_id) => moved_terminal_ids.get(terminal_id).copied(),
        WireEndpoint::Junction(_) => None,
    }
}

fn rotate_item(sheet: &mut athena_domain::Sheet, item: ItemId, quarter_turns: u8) {
    if let ItemId::Symbol(id) = item {
        let symbol = sheet
            .symbol_instances
            .get_mut(&id)
            .expect("validated symbol");
        symbol.rotation_quarter_turns =
            ((u16::from(symbol.rotation_quarter_turns) + u16::from(quarter_turns)) % 4) as u8;
    }
}

fn mirror_item(sheet: &mut athena_domain::Sheet, item: ItemId) {
    if let ItemId::Symbol(id) = item {
        let symbol = sheet
            .symbol_instances
            .get_mut(&id)
            .expect("validated symbol");
        symbol.mirrored = !symbol.mirrored;
    }
}

fn remove_item_direct(sheet: &mut athena_domain::Sheet, item: ItemId) {
    match item {
        ItemId::Symbol(id) => {
            sheet.symbol_instances.remove(&id);
        }
        ItemId::Wire(id) => {
            sheet.wires.remove(&id);
        }
        ItemId::Junction(id) => {
            sheet.junctions.remove(&id);
        }
        ItemId::Annotation(id) => {
            sheet.annotations.remove(&id);
        }
    }
}

fn insert_stored_item(sheet: &mut athena_domain::Sheet, item: StoredItem) {
    match item {
        StoredItem::Symbol(symbol) => {
            sheet.symbol_instances.insert(symbol.id, symbol);
        }
        StoredItem::Wire(wire) => {
            sheet.wires.insert(wire.id, wire);
        }
        StoredItem::Junction(junction) => {
            sheet.junctions.insert(junction.id, junction);
        }
        StoredItem::Annotation(annotation) => {
            sheet.annotations.insert(annotation.id, annotation);
        }
    }
}

fn translate(point: Point, delta: Point) -> Result<Point, ApplyError> {
    Ok(Point::new(
        point
            .x
            .checked_add(delta.x)
            .ok_or(ApplyError::CoordinateOverflow)?,
        point
            .y
            .checked_add(delta.y)
            .ok_or(ApplyError::CoordinateOverflow)?,
    ))
}

fn deletion_closure(
    project: &Project,
    sheet_id: SheetId,
    selected: &[ItemId],
) -> Result<Vec<ItemId>, ApplyError> {
    let sheet = project
        .sheet(sheet_id)
        .ok_or(ApplyError::UnknownSheet { sheet_id })?;
    let mut delete = selected
        .iter()
        .copied()
        .collect::<std::collections::BTreeSet<_>>();
    let terminal_ids = delete
        .iter()
        .filter_map(|item| match item {
            ItemId::Symbol(symbol_id) => sheet.symbol_instances.get(symbol_id),
            _ => None,
        })
        .flat_map(|symbol| symbol.terminals.keys().copied())
        .collect::<std::collections::BTreeSet<_>>();
    let junction_ids = delete
        .iter()
        .filter_map(|item| match item {
            ItemId::Junction(junction_id) => Some(*junction_id),
            _ => None,
        })
        .collect::<std::collections::BTreeSet<_>>();

    for (wire_id, wire) in &sheet.wires {
        if endpoint_is_removed(&wire.start, &terminal_ids, &junction_ids)
            || endpoint_is_removed(&wire.end, &terminal_ids, &junction_ids)
        {
            delete.insert(ItemId::Wire(*wire_id));
        }
    }

    Ok(delete.into_iter().collect())
}

fn endpoint_is_removed(
    endpoint: &WireEndpoint,
    terminal_ids: &std::collections::BTreeSet<TerminalId>,
    junction_ids: &std::collections::BTreeSet<JunctionId>,
) -> bool {
    match endpoint {
        WireEndpoint::Terminal(id) => terminal_ids.contains(id),
        WireEndpoint::Junction(id) => junction_ids.contains(id),
    }
}
