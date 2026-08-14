//! Persistent, deterministic mutations for schematic documents.
//!
//! Commands are deliberately UI-agnostic: shells create commands, this module
//! applies them atomically, and [`crate::History`] stores their exact inverses.

use std::collections::BTreeMap;

use athena_domain::{
    Annotation, AnnotationId, DomainError, FieldValue, Folio, FolioId, Junction, JunctionId, Point,
    Project, ProjectFolioDefaults, ProjectId, SchematicSettings, SymbolInstance, SymbolInstanceId,
    TemplateText, TerminalId, TitleBlockPlacement, Wire, WireEndpoint, WireId,
    canonical_wire_route,
};
use serde::{Deserialize, Serialize};
use thiserror::Error;
use uuid::Uuid;

use crate::validation;

/// A saved-folio entity that can be selected and modified as one editor item.
#[derive(Clone, Copy, Debug, Deserialize, Eq, Ord, PartialEq, PartialOrd, Serialize)]
pub enum ItemId {
    Symbol(SymbolInstanceId),
    Wire(WireId),
    Junction(JunctionId),
    Annotation(AnnotationId),
}

/// A field-bearing item in the active folio.
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

/// Stable title-block property identity used by typed editor commands.
#[derive(Clone, Copy, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub enum TitleBlockField {
    /// Opaque title-block template identity.
    TemplateId,
    /// Edge placement of the title block.
    Placement,
    /// Drawing title.
    Title,
    /// Drawing author.
    Author,
    /// User-controlled date text.
    DateText,
    /// File or document label.
    FileLabel,
    /// Folio label rendered inside the title block.
    FolioLabel,
    /// Plant designation.
    Plant,
    /// Installation location.
    Location,
    /// Revision label.
    Revision,
    /// User-configured page number independent of folio order.
    PageNumber,
}

/// Typed value accepted by a title-block property command.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub enum TitleBlockValue {
    /// Plain text used by template identity fields.
    Text(String),
    /// Typed title-block edge placement.
    Placement(TitleBlockPlacement),
    /// Structured literal and variable-reference text.
    Template(TemplateText),
}

/// Deterministic mutations supported by the schematic editor.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub enum EditorCommand {
    /// Inserts a preallocated folio at an explicit order position.
    AddFolio {
        folio: Folio,
        position: usize,
    },
    /// Removes a folio while preserving its identity in the inverse command.
    #[doc(hidden)]
    RemoveFolio {
        folio_id: FolioId,
    },
    /// Moves a folio between explicit order positions.
    MoveFolio {
        folio_id: FolioId,
        from: usize,
        to: usize,
    },
    /// Renames the project with an optimistic old-value check.
    RenameProject {
        old_name: String,
        new_name: String,
    },
    /// Replaces project defaults copied into subsequently created folios.
    UpdateFolioDefaults {
        old_defaults: ProjectFolioDefaults,
        new_defaults: ProjectFolioDefaults,
    },
    /// Sets or removes one project-scoped variable.
    SetProjectVariable {
        key: String,
        old_value: Option<String>,
        new_value: Option<String>,
    },
    /// Sets or removes one folio-scoped variable.
    SetFolioVariable {
        folio_id: FolioId,
        key: String,
        old_value: Option<String>,
        new_value: Option<String>,
    },
    /// Commits one typed title-block property edit.
    SetTitleBlockValue {
        folio_id: FolioId,
        field: TitleBlockField,
        old_value: TitleBlockValue,
        new_value: TitleBlockValue,
    },
    PlaceSymbol {
        folio_id: FolioId,
        symbol: SymbolInstance,
    },
    MoveItems {
        folio_id: FolioId,
        items: Vec<ItemId>,
        delta: Point,
    },
    RotateItems {
        folio_id: FolioId,
        items: Vec<ItemId>,
        quarter_turns: u8,
    },
    MirrorItems {
        folio_id: FolioId,
        items: Vec<ItemId>,
    },
    DeleteItems {
        folio_id: FolioId,
        items: Vec<ItemId>,
    },
    CreateWire {
        folio_id: FolioId,
        wire: Wire,
    },
    SplitWire {
        folio_id: FolioId,
        wire_id: WireId,
        junction: Junction,
        first_wire: Wire,
        second_wire: Wire,
    },
    DeleteWire {
        folio_id: FolioId,
        wire_id: WireId,
    },
    /// Rebinds one wire endpoint to a terminal on the same folio.
    ReconnectWireEndpoint {
        folio_id: FolioId,
        wire_id: WireId,
        endpoint: WireSide,
        terminal_id: TerminalId,
    },
    /// Adds a route vertex on an existing wire segment.
    InsertWireVertex {
        folio_id: FolioId,
        wire_id: WireId,
        segment_index: usize,
        position: Point,
    },
    /// Moves an interior route vertex while retaining an orthogonal route.
    MoveWireVertex {
        folio_id: FolioId,
        wire_id: WireId,
        vertex_index: usize,
        position: Point,
    },
    /// Removes an interior route vertex and normalizes adjacent bends.
    DeleteWireVertex {
        folio_id: FolioId,
        wire_id: WireId,
        vertex_index: usize,
    },
    SetFieldValue {
        folio_id: FolioId,
        target: FieldTarget,
        field: String,
        value: Option<FieldValue>,
    },
    ApplySchematicSettings {
        folio_id: FolioId,
        settings: SchematicSettings,
    },
    /// Renames one folio through deterministic command history.
    RenameFolio {
        folio_id: FolioId,
        old_label: String,
        new_label: String,
    },
    /// Internal, serializable inverse used by deterministic undo/redo.
    #[doc(hidden)]
    RestoreItems {
        folio_id: FolioId,
        remove: Vec<ItemId>,
        restore: Vec<StoredItem>,
    },
}

/// The metadata required to replay a command in a local-first or shared session.
#[derive(Clone, Debug, Deserialize, Eq, PartialEq, Serialize)]
pub struct CommandEnvelope {
    /// Stable identity of this operation for replay and synchronization.
    pub operation_id: Uuid,
    /// Project aggregate targeted by the command.
    pub project_id: ProjectId,
    /// Folio context targeted by the command.
    pub folio_id: FolioId,
    /// Semantic revision against which the command was authored.
    pub base_revision: DocumentRevision,
    /// Stable author identity reserved for synchronization.
    pub author_id: Uuid,
    /// Stable editing-session identity reserved for synchronization.
    pub session_id: Uuid,
    /// Version of the serialized command payload contract.
    pub command_version: u32,
    /// Typed deterministic mutation payload.
    pub payload: EditorCommand,
}

/// Stable identity of one committed semantic document state.
#[derive(Clone, Copy, Debug, Default, Deserialize, Eq, Ord, PartialEq, PartialOrd, Serialize)]
pub struct DocumentRevision(pub u64);

impl DocumentRevision {
    /// Initial unsaved project state.
    pub const INITIAL: Self = Self(0);
}

/// A command plus the exact command needed to undo it.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct AppliedCommand {
    /// Forward mutation committed by the user.
    pub command: EditorCommand,
    /// Exact mutation that restores the preceding state.
    pub inverse: EditorCommand,
    /// Existing semantic identity restored by undo.
    pub before_revision: DocumentRevision,
    /// Existing semantic identity restored by redo.
    pub after_revision: DocumentRevision,
}

/// Platform-neutral editable project state.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct EditorState {
    project: Project,
    revision: DocumentRevision,
    next_revision: DocumentRevision,
}

impl EditorState {
    #[must_use]
    pub fn new(project: Project) -> Self {
        Self {
            project,
            revision: DocumentRevision::INITIAL,
            next_revision: DocumentRevision(1),
        }
    }

    #[must_use]
    pub fn project(&self) -> &Project {
        &self.project
    }

    #[must_use]
    pub const fn revision(&self) -> DocumentRevision {
        self.revision
    }

    /// Applies a validated command atomically.
    ///
    /// The real project is only replaced after command validation, candidate
    /// mutation, and full domain validation all succeed.
    pub fn apply(&mut self, command: EditorCommand) -> Result<AppliedCommand, ApplyError> {
        // Persisted projects can enter the session through external adapters;
        // reject an invalid aggregate before route-repair helpers inspect it.
        self.project
            .validate()
            .map_err(ApplyError::InvalidProject)?;
        validation::validate_command(&self.project, &command)?;

        let before_revision = self.revision;
        let after_revision = self.next_revision;
        let next_revision = DocumentRevision(
            after_revision
                .0
                .checked_add(1)
                .ok_or(ApplyError::RevisionOverflow)?,
        );
        let mut candidate = self.project.clone();
        let inverse = apply_to_project(&mut candidate, &command)?;
        candidate.validate().map_err(ApplyError::InvalidProject)?;

        self.project = candidate;
        self.revision = after_revision;
        self.next_revision = next_revision;

        Ok(AppliedCommand {
            command,
            inverse,
            before_revision,
            after_revision,
        })
    }

    /// Applies an undo/redo command while restoring an existing revision ID.
    pub(crate) fn apply_existing(
        &mut self,
        command: EditorCommand,
        revision: DocumentRevision,
    ) -> Result<(), ApplyError> {
        self.project
            .validate()
            .map_err(ApplyError::InvalidProject)?;
        validation::validate_command(&self.project, &command)?;
        let mut candidate = self.project.clone();
        apply_to_project(&mut candidate, &command)?;
        candidate.validate().map_err(ApplyError::InvalidProject)?;
        self.project = candidate;
        self.revision = revision;
        Ok(())
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
    #[error("command old value does not match current document state")]
    StateMismatch,
    #[error("folio {folio_id} already exists")]
    DuplicateFolio { folio_id: FolioId },
    #[error("folio position {position} is outside the project order")]
    InvalidFolioPosition { position: usize },
    #[error("the final project folio cannot be removed")]
    CannotRemoveLastFolio,
    #[error("title-block value does not match the selected field")]
    InvalidTitleBlockValue,
    #[error("folio {folio_id} does not exist")]
    UnknownFolio { folio_id: FolioId },
    #[error("item {item:?} does not exist on folio {folio_id}")]
    UnknownItem { folio_id: FolioId, item: ItemId },
    #[error("symbol definition {definition_id} does not exist")]
    UnknownSymbolDefinition {
        definition_id: athena_domain::SymbolDefinitionId,
    },
    #[error("wire endpoint refers to unknown terminal {terminal_id} on folio {folio_id}")]
    UnknownTerminal {
        folio_id: FolioId,
        terminal_id: TerminalId,
    },
    #[error("wire endpoint refers to unknown junction {junction_id} on folio {folio_id}")]
    UnknownJunction {
        folio_id: FolioId,
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
    #[error("symbol instance {symbol_id} already exists on folio {folio_id}")]
    DuplicateSymbol {
        folio_id: FolioId,
        symbol_id: SymbolInstanceId,
    },
    #[error("wire {wire_id} already exists on folio {folio_id}")]
    DuplicateWire { folio_id: FolioId, wire_id: WireId },
    #[error("junction {junction_id} already exists on folio {folio_id}")]
    DuplicateJunction {
        folio_id: FolioId,
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
    #[error("folio settings must have positive page dimensions and grid spacing")]
    InvalidSchematicSettings,
    #[error("project name must not be empty")]
    InvalidProjectName,
    #[error("folio label must not be empty")]
    InvalidFolioLabel,
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
        EditorCommand::AddFolio { folio, position } => {
            if project.folios.contains_key(&folio.id) {
                return Err(ApplyError::DuplicateFolio { folio_id: folio.id });
            }
            if *position > project.folio_order.len() {
                return Err(ApplyError::InvalidFolioPosition {
                    position: *position,
                });
            }
            project.folios.insert(folio.id, folio.clone());
            project.folio_order.insert(*position, folio.id);
            Ok(EditorCommand::RemoveFolio { folio_id: folio.id })
        }
        EditorCommand::RemoveFolio { folio_id } => {
            if project.folios.len() <= 1 {
                return Err(ApplyError::CannotRemoveLastFolio);
            }
            let position = project
                .folio_order
                .iter()
                .position(|id| id == folio_id)
                .ok_or(ApplyError::UnknownFolio {
                    folio_id: *folio_id,
                })?;
            let folio = project
                .folios
                .remove(folio_id)
                .ok_or(ApplyError::UnknownFolio {
                    folio_id: *folio_id,
                })?;
            project.folio_order.remove(position);
            Ok(EditorCommand::AddFolio { folio, position })
        }
        EditorCommand::MoveFolio { folio_id, from, to } => {
            if *to >= project.folio_order.len() {
                return Err(ApplyError::InvalidFolioPosition { position: *to });
            }
            if project.folio_order.get(*from) != Some(folio_id) {
                return Err(ApplyError::StateMismatch);
            }
            let moved = project.folio_order.remove(*from);
            project.folio_order.insert(*to, moved);
            Ok(EditorCommand::MoveFolio {
                folio_id: *folio_id,
                from: *to,
                to: *from,
            })
        }
        EditorCommand::RenameProject { old_name, new_name } => {
            if &project.name != old_name {
                return Err(ApplyError::StateMismatch);
            }
            project.name.clone_from(new_name);
            Ok(EditorCommand::RenameProject {
                old_name: new_name.clone(),
                new_name: old_name.clone(),
            })
        }
        EditorCommand::UpdateFolioDefaults {
            old_defaults,
            new_defaults,
        } => {
            if &project.folio_defaults != old_defaults {
                return Err(ApplyError::StateMismatch);
            }
            project.folio_defaults = new_defaults.clone();
            Ok(EditorCommand::UpdateFolioDefaults {
                old_defaults: new_defaults.clone(),
                new_defaults: old_defaults.clone(),
            })
        }
        EditorCommand::SetProjectVariable {
            key,
            old_value,
            new_value,
        } => {
            if project.variables.get(key) != old_value.as_ref() {
                return Err(ApplyError::StateMismatch);
            }
            set_optional_value(&mut project.variables, key, new_value);
            Ok(EditorCommand::SetProjectVariable {
                key: key.clone(),
                old_value: new_value.clone(),
                new_value: old_value.clone(),
            })
        }
        EditorCommand::SetFolioVariable {
            folio_id,
            key,
            old_value,
            new_value,
        } => {
            let folio = project
                .folio_mut(*folio_id)
                .ok_or(ApplyError::UnknownFolio {
                    folio_id: *folio_id,
                })?;
            if folio.variables.get(key) != old_value.as_ref() {
                return Err(ApplyError::StateMismatch);
            }
            set_optional_value(&mut folio.variables, key, new_value);
            Ok(EditorCommand::SetFolioVariable {
                folio_id: *folio_id,
                key: key.clone(),
                old_value: new_value.clone(),
                new_value: old_value.clone(),
            })
        }
        EditorCommand::SetTitleBlockValue {
            folio_id,
            field,
            old_value,
            new_value,
        } => {
            let folio = project
                .folio_mut(*folio_id)
                .ok_or(ApplyError::UnknownFolio {
                    folio_id: *folio_id,
                })?;
            if title_block_value(&folio.title_block, *field) != *old_value {
                return Err(ApplyError::StateMismatch);
            }
            set_title_block_value(&mut folio.title_block, *field, new_value.clone())?;
            Ok(EditorCommand::SetTitleBlockValue {
                folio_id: *folio_id,
                field: *field,
                old_value: new_value.clone(),
                new_value: old_value.clone(),
            })
        }
        EditorCommand::PlaceSymbol { folio_id, symbol } => {
            project
                .folio_mut(*folio_id)
                .expect("validated folio")
                .symbol_instances
                .insert(symbol.id, symbol.clone());
            Ok(EditorCommand::DeleteItems {
                folio_id: *folio_id,
                items: vec![ItemId::Symbol(symbol.id)],
            })
        }
        EditorCommand::MoveItems {
            folio_id,
            items,
            delta,
        } => {
            let moved_terminal_ids =
                terminal_ids_owned_by_selected_symbols(project, *folio_id, items)?;
            let affected_items =
                items_with_attached_wires(project, *folio_id, items, &moved_terminal_ids)?;
            let originals = collect_items(project, *folio_id, &affected_items)?;
            let folio = project.folio_mut(*folio_id).expect("validated folio");
            for item in items {
                move_item(folio, *item, *delta)?;
            }
            move_attached_wire_endpoints(folio, &moved_terminal_ids, *delta)?;
            Ok(restore_command(*folio_id, affected_items, originals))
        }
        EditorCommand::RotateItems {
            folio_id,
            items,
            quarter_turns,
        } => {
            let originals = collect_items(project, *folio_id, items)?;
            let folio = project.folio_mut(*folio_id).expect("validated folio");
            for item in items {
                rotate_item(folio, *item, *quarter_turns);
            }
            Ok(restore_command(*folio_id, items.clone(), originals))
        }
        EditorCommand::MirrorItems { folio_id, items } => {
            let originals = collect_items(project, *folio_id, items)?;
            let folio = project.folio_mut(*folio_id).expect("validated folio");
            for item in items {
                mirror_item(folio, *item);
            }
            Ok(restore_command(*folio_id, items.clone(), originals))
        }
        EditorCommand::DeleteItems { folio_id, items } => {
            let delete_ids = deletion_closure(project, *folio_id, items)?;
            let originals = collect_items(project, *folio_id, &delete_ids)?;
            let folio = project.folio_mut(*folio_id).expect("validated folio");
            for item in &delete_ids {
                remove_item_direct(folio, *item);
            }
            Ok(restore_command(*folio_id, delete_ids, originals))
        }
        EditorCommand::CreateWire { folio_id, wire } => {
            let mut canonical_wire = wire.clone();
            canonical_wire.route = canonical_wire_route(&canonical_wire.route);
            project
                .folio_mut(*folio_id)
                .expect("validated folio")
                .wires
                .insert(canonical_wire.id, canonical_wire);
            Ok(EditorCommand::DeleteWire {
                folio_id: *folio_id,
                wire_id: wire.id,
            })
        }
        EditorCommand::SplitWire {
            folio_id,
            wire_id,
            junction,
            first_wire,
            second_wire,
        } => {
            let original = project
                .folio(*folio_id)
                .expect("validated folio")
                .wire(*wire_id)
                .expect("validated wire")
                .clone();
            let mut canonical_first_wire = first_wire.clone();
            let mut canonical_second_wire = second_wire.clone();
            canonical_first_wire.route = canonical_wire_route(&canonical_first_wire.route);
            canonical_second_wire.route = canonical_wire_route(&canonical_second_wire.route);
            let folio = project.folio_mut(*folio_id).expect("validated folio");
            folio.wires.remove(wire_id);
            folio.junctions.insert(junction.id, junction.clone());
            folio
                .wires
                .insert(canonical_first_wire.id, canonical_first_wire);
            folio
                .wires
                .insert(canonical_second_wire.id, canonical_second_wire);

            Ok(EditorCommand::RestoreItems {
                folio_id: *folio_id,
                remove: vec![
                    ItemId::Junction(junction.id),
                    ItemId::Wire(first_wire.id),
                    ItemId::Wire(second_wire.id),
                ],
                restore: vec![StoredItem::Wire(original)],
            })
        }
        EditorCommand::DeleteWire { folio_id, wire_id } => {
            let wire = project
                .folio_mut(*folio_id)
                .expect("validated folio")
                .wires
                .remove(wire_id)
                .expect("validated wire");
            Ok(EditorCommand::CreateWire {
                folio_id: *folio_id,
                wire,
            })
        }
        EditorCommand::ReconnectWireEndpoint {
            folio_id,
            wire_id,
            endpoint,
            terminal_id,
        } => {
            let original = project
                .wire(*folio_id, *wire_id)
                .expect("validated wire")
                .clone();
            let position = project
                .terminal(*folio_id, *terminal_id)
                .expect("validated terminal")
                .position;
            let wire = project
                .folio_mut(*folio_id)
                .expect("validated folio")
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
            Ok(restore_wire_command(*folio_id, original))
        }
        EditorCommand::InsertWireVertex {
            folio_id,
            wire_id,
            segment_index,
            position,
        } => {
            let original = project
                .wire(*folio_id, *wire_id)
                .expect("validated wire")
                .clone();
            let wire = project
                .folio_mut(*folio_id)
                .expect("validated folio")
                .wires
                .get_mut(wire_id)
                .expect("validated wire");
            wire.route.insert(*segment_index + 1, *position);
            normalize_wire_route(&mut wire.route);
            Ok(restore_wire_command(*folio_id, original))
        }
        EditorCommand::MoveWireVertex {
            folio_id,
            wire_id,
            vertex_index,
            position,
        } => {
            let original = project
                .wire(*folio_id, *wire_id)
                .expect("validated wire")
                .clone();
            let wire = project
                .folio_mut(*folio_id)
                .expect("validated folio")
                .wires
                .get_mut(wire_id)
                .expect("validated wire");
            replace_route_vertex_with_elbows(&mut wire.route, *vertex_index, *position);
            normalize_wire_route(&mut wire.route);
            Ok(restore_wire_command(*folio_id, original))
        }
        EditorCommand::DeleteWireVertex {
            folio_id,
            wire_id,
            vertex_index,
        } => {
            let original = project
                .wire(*folio_id, *wire_id)
                .expect("validated wire")
                .clone();
            let wire = project
                .folio_mut(*folio_id)
                .expect("validated folio")
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
            Ok(restore_wire_command(*folio_id, original))
        }
        EditorCommand::SetFieldValue {
            folio_id,
            target,
            field,
            value,
        } => {
            let old_value = set_field_value(
                project.folio_mut(*folio_id).expect("validated folio"),
                *target,
                field,
                value.clone(),
            );
            Ok(EditorCommand::SetFieldValue {
                folio_id: *folio_id,
                target: *target,
                field: field.clone(),
                value: old_value,
            })
        }
        EditorCommand::ApplySchematicSettings { folio_id, settings } => {
            let folio = project.folio_mut(*folio_id).expect("validated folio");
            let old_settings = std::mem::replace(&mut folio.settings, settings.clone());
            Ok(EditorCommand::ApplySchematicSettings {
                folio_id: *folio_id,
                settings: old_settings,
            })
        }
        EditorCommand::RenameFolio {
            folio_id,
            old_label,
            new_label,
        } => {
            let folio = project.folio_mut(*folio_id).expect("validated folio");
            if &folio.label != old_label {
                return Err(ApplyError::StateMismatch);
            }
            folio.label.clone_from(new_label);
            Ok(EditorCommand::RenameFolio {
                folio_id: *folio_id,
                old_label: new_label.clone(),
                new_label: old_label.clone(),
            })
        }
        EditorCommand::RestoreItems {
            folio_id,
            remove,
            restore,
        } => {
            let folio = project.folio_mut(*folio_id).expect("validated folio");
            let previous = remove
                .iter()
                .filter_map(|item| stored_item_if_present(folio, *item))
                .collect();
            for item in remove {
                remove_item_direct(folio, *item);
            }
            for item in restore {
                insert_stored_item(folio, item.clone());
            }
            Ok(EditorCommand::RestoreItems {
                folio_id: *folio_id,
                remove: restore.iter().map(StoredItem::item_id).collect(),
                restore: previous,
            })
        }
    }
}

fn set_optional_value(values: &mut BTreeMap<String, String>, key: &str, value: &Option<String>) {
    if let Some(value) = value {
        values.insert(key.to_owned(), value.clone());
    } else {
        values.remove(key);
    }
}

fn title_block_value(
    values: &athena_domain::TitleBlockValues,
    field: TitleBlockField,
) -> TitleBlockValue {
    match field {
        TitleBlockField::TemplateId => TitleBlockValue::Text(values.template_id.clone()),
        TitleBlockField::Placement => TitleBlockValue::Placement(values.placement),
        TitleBlockField::Title => TitleBlockValue::Template(values.title.clone()),
        TitleBlockField::Author => TitleBlockValue::Template(values.author.clone()),
        TitleBlockField::DateText => TitleBlockValue::Template(values.date_text.clone()),
        TitleBlockField::FileLabel => TitleBlockValue::Template(values.file_label.clone()),
        TitleBlockField::FolioLabel => TitleBlockValue::Template(values.folio_label.clone()),
        TitleBlockField::Plant => TitleBlockValue::Template(values.plant.clone()),
        TitleBlockField::Location => TitleBlockValue::Template(values.location.clone()),
        TitleBlockField::Revision => TitleBlockValue::Template(values.revision.clone()),
        TitleBlockField::PageNumber => TitleBlockValue::Template(values.page_number.clone()),
    }
}

fn set_title_block_value(
    values: &mut athena_domain::TitleBlockValues,
    field: TitleBlockField,
    value: TitleBlockValue,
) -> Result<(), ApplyError> {
    match (field, value) {
        (TitleBlockField::TemplateId, TitleBlockValue::Text(value)) => values.template_id = value,
        (TitleBlockField::Placement, TitleBlockValue::Placement(value)) => {
            values.placement = value;
        }
        (TitleBlockField::Title, TitleBlockValue::Template(value)) => values.title = value,
        (TitleBlockField::Author, TitleBlockValue::Template(value)) => values.author = value,
        (TitleBlockField::DateText, TitleBlockValue::Template(value)) => values.date_text = value,
        (TitleBlockField::FileLabel, TitleBlockValue::Template(value)) => {
            values.file_label = value;
        }
        (TitleBlockField::FolioLabel, TitleBlockValue::Template(value)) => {
            values.folio_label = value;
        }
        (TitleBlockField::Plant, TitleBlockValue::Template(value)) => values.plant = value,
        (TitleBlockField::Location, TitleBlockValue::Template(value)) => values.location = value,
        (TitleBlockField::Revision, TitleBlockValue::Template(value)) => values.revision = value,
        (TitleBlockField::PageNumber, TitleBlockValue::Template(value)) => {
            values.page_number = value;
        }
        _ => return Err(ApplyError::InvalidTitleBlockValue),
    }
    Ok(())
}

fn restore_wire_command(folio_id: FolioId, original: Wire) -> EditorCommand {
    restore_command(
        folio_id,
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
fn stored_item_if_present(folio: &athena_domain::Folio, item: ItemId) -> Option<StoredItem> {
    match item {
        ItemId::Symbol(id) => folio
            .symbol_instances
            .get(&id)
            .cloned()
            .map(StoredItem::Symbol),
        ItemId::Wire(id) => folio.wires.get(&id).cloned().map(StoredItem::Wire),
        ItemId::Junction(id) => folio.junctions.get(&id).cloned().map(StoredItem::Junction),
        ItemId::Annotation(id) => folio
            .annotations
            .get(&id)
            .cloned()
            .map(StoredItem::Annotation),
    }
}

fn restore_command(
    folio_id: FolioId,
    remove: Vec<ItemId>,
    restore: Vec<StoredItem>,
) -> EditorCommand {
    EditorCommand::RestoreItems {
        folio_id,
        remove,
        restore,
    }
}

fn collect_items(
    project: &Project,
    folio_id: FolioId,
    items: &[ItemId],
) -> Result<Vec<StoredItem>, ApplyError> {
    collect_items_from_sheet(
        project
            .folio(folio_id)
            .ok_or(ApplyError::UnknownFolio { folio_id })?,
        items,
    )
}

fn collect_items_from_sheet(
    folio: &athena_domain::Folio,
    items: &[ItemId],
) -> Result<Vec<StoredItem>, ApplyError> {
    items
        .iter()
        .map(|item| match item {
            ItemId::Symbol(id) => folio
                .symbol_instances
                .get(id)
                .cloned()
                .map(StoredItem::Symbol),
            ItemId::Wire(id) => folio.wires.get(id).cloned().map(StoredItem::Wire),
            ItemId::Junction(id) => folio.junctions.get(id).cloned().map(StoredItem::Junction),
            ItemId::Annotation(id) => folio
                .annotations
                .get(id)
                .cloned()
                .map(StoredItem::Annotation),
        })
        .collect::<Option<Vec<_>>>()
        .ok_or_else(|| ApplyError::UnknownItem {
            folio_id: folio.id,
            item: items
                .iter()
                .copied()
                .find(|item| !sheet_contains_item(folio, *item))
                .expect("a missing item exists when collection failed"),
        })
}

pub(crate) fn sheet_contains_item(folio: &athena_domain::Folio, item: ItemId) -> bool {
    match item {
        ItemId::Symbol(id) => folio.symbol_instances.contains_key(&id),
        ItemId::Wire(id) => folio.wires.contains_key(&id),
        ItemId::Junction(id) => folio.junctions.contains_key(&id),
        ItemId::Annotation(id) => folio.annotations.contains_key(&id),
    }
}

fn insertion_map_for_fields(
    folio: &mut athena_domain::Folio,
    target: FieldTarget,
) -> &mut BTreeMap<String, FieldValue> {
    match target {
        FieldTarget::Symbol(id) => {
            &mut folio
                .symbol_instances
                .get_mut(&id)
                .expect("validated symbol target")
                .fields
        }
        FieldTarget::Wire(id) => {
            &mut folio
                .wires
                .get_mut(&id)
                .expect("validated wire target")
                .fields
        }
        FieldTarget::Annotation(id) => {
            &mut folio
                .annotations
                .get_mut(&id)
                .expect("validated annotation target")
                .fields
        }
    }
}

fn set_field_value(
    folio: &mut athena_domain::Folio,
    target: FieldTarget,
    field: &str,
    value: Option<FieldValue>,
) -> Option<FieldValue> {
    let fields = insertion_map_for_fields(folio, target);
    match value {
        Some(value) => fields.insert(field.into(), value),
        None => fields.remove(field),
    }
}

fn move_item(
    folio: &mut athena_domain::Folio,
    item: ItemId,
    delta: Point,
) -> Result<(), ApplyError> {
    match item {
        ItemId::Symbol(id) => {
            let symbol = folio
                .symbol_instances
                .get_mut(&id)
                .expect("validated symbol");
            symbol.position = translate(symbol.position, delta)?;
            for terminal in symbol.terminals.values_mut() {
                terminal.position = translate(terminal.position, delta)?;
            }
        }
        ItemId::Wire(id) => {
            let wire = folio.wires.get_mut(&id).expect("validated wire");
            for point in &mut wire.route {
                *point = translate(*point, delta)?;
            }
        }
        ItemId::Junction(id) => {
            let junction = folio.junctions.get_mut(&id).expect("validated junction");
            junction.position = translate(junction.position, delta)?;
        }
        ItemId::Annotation(id) => {
            let annotation = folio
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
    folio_id: FolioId,
    items: &[ItemId],
) -> Result<BTreeMap<TerminalId, Point>, ApplyError> {
    let folio = project
        .folio(folio_id)
        .ok_or(ApplyError::UnknownFolio { folio_id })?;
    Ok(items
        .iter()
        .filter_map(|item| match item {
            ItemId::Symbol(symbol_id) => folio.symbol_instances.get(symbol_id),
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
    folio_id: FolioId,
    selected_items: &[ItemId],
    moved_terminal_ids: &BTreeMap<TerminalId, Point>,
) -> Result<Vec<ItemId>, ApplyError> {
    let folio = project
        .folio(folio_id)
        .ok_or(ApplyError::UnknownFolio { folio_id })?;
    let mut items = selected_items
        .iter()
        .copied()
        .collect::<std::collections::BTreeSet<_>>();
    for (wire_id, wire) in &folio.wires {
        if endpoint_references_moved_terminal(&wire.start, moved_terminal_ids)
            || endpoint_references_moved_terminal(&wire.end, moved_terminal_ids)
        {
            items.insert(ItemId::Wire(*wire_id));
        }
    }
    Ok(items.into_iter().collect())
}

fn move_attached_wire_endpoints(
    folio: &mut athena_domain::Folio,
    moved_terminal_ids: &BTreeMap<TerminalId, Point>,
    delta: Point,
) -> Result<(), ApplyError> {
    for wire in folio.wires.values_mut() {
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

fn rotate_item(folio: &mut athena_domain::Folio, item: ItemId, quarter_turns: u8) {
    if let ItemId::Symbol(id) = item {
        let symbol = folio
            .symbol_instances
            .get_mut(&id)
            .expect("validated symbol");
        symbol.rotation_quarter_turns =
            ((u16::from(symbol.rotation_quarter_turns) + u16::from(quarter_turns)) % 4) as u8;
    }
}

fn mirror_item(folio: &mut athena_domain::Folio, item: ItemId) {
    if let ItemId::Symbol(id) = item {
        let symbol = folio
            .symbol_instances
            .get_mut(&id)
            .expect("validated symbol");
        symbol.mirrored = !symbol.mirrored;
    }
}

fn remove_item_direct(folio: &mut athena_domain::Folio, item: ItemId) {
    match item {
        ItemId::Symbol(id) => {
            folio.symbol_instances.remove(&id);
        }
        ItemId::Wire(id) => {
            folio.wires.remove(&id);
        }
        ItemId::Junction(id) => {
            folio.junctions.remove(&id);
        }
        ItemId::Annotation(id) => {
            folio.annotations.remove(&id);
        }
    }
}

fn insert_stored_item(folio: &mut athena_domain::Folio, item: StoredItem) {
    match item {
        StoredItem::Symbol(symbol) => {
            folio.symbol_instances.insert(symbol.id, symbol);
        }
        StoredItem::Wire(wire) => {
            folio.wires.insert(wire.id, wire);
        }
        StoredItem::Junction(junction) => {
            folio.junctions.insert(junction.id, junction);
        }
        StoredItem::Annotation(annotation) => {
            folio.annotations.insert(annotation.id, annotation);
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
    folio_id: FolioId,
    selected: &[ItemId],
) -> Result<Vec<ItemId>, ApplyError> {
    let folio = project
        .folio(folio_id)
        .ok_or(ApplyError::UnknownFolio { folio_id })?;
    let mut delete = selected
        .iter()
        .copied()
        .collect::<std::collections::BTreeSet<_>>();
    let terminal_ids = delete
        .iter()
        .filter_map(|item| match item {
            ItemId::Symbol(symbol_id) => folio.symbol_instances.get(symbol_id),
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

    for (wire_id, wire) in &folio.wires {
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
