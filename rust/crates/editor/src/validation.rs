//! Preflight validation for persistent editor commands.
//!
//! Validation happens before cloning and mutating the document, so rejected
//! commands leave the state and command history unchanged.

use std::collections::BTreeSet;

use athena_domain::{FolioId, Junction, Point, Project, Wire, WireEndpoint, canonical_wire_route};

use crate::{
    ApplyError, EditorCommand, FieldTarget, ItemId, StoredItem, WireSide,
    command::sheet_contains_item,
};

/// Performs all identifier and endpoint checks before an editor-state mutation
/// is attempted.
pub(crate) fn validate_command(
    project: &Project,
    command: &EditorCommand,
) -> Result<(), ApplyError> {
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
            if folio.label.trim().is_empty() {
                return Err(ApplyError::InvalidFolioLabel);
            }
        }
        EditorCommand::RemoveFolio { folio_id } => {
            let _ = folio(project, *folio_id)?;
            if project.folios.len() <= 1 {
                return Err(ApplyError::CannotRemoveLastFolio);
            }
        }
        EditorCommand::MoveFolio { folio_id, from, to } => {
            let _ = folio(project, *folio_id)?;
            if *from >= project.folio_order.len() {
                return Err(ApplyError::InvalidFolioPosition { position: *from });
            }
            if *to >= project.folio_order.len() {
                return Err(ApplyError::InvalidFolioPosition { position: *to });
            }
        }
        EditorCommand::RenameProject { new_name, .. } => {
            if new_name.trim().is_empty() {
                return Err(ApplyError::InvalidProjectName);
            }
        }
        EditorCommand::UpdateFolioDefaults { .. } | EditorCommand::SetProjectVariable { .. } => {}
        EditorCommand::SetFolioVariable { folio_id, .. }
        | EditorCommand::SetTitleBlockValue { folio_id, .. } => {
            let _ = folio(project, *folio_id)?;
        }
        EditorCommand::PlaceSymbol { folio_id, symbol } => {
            let folio = folio(project, *folio_id)?;
            if project.symbol_definition(symbol.definition_id).is_none() {
                return Err(ApplyError::UnknownSymbolDefinition {
                    definition_id: symbol.definition_id,
                });
            }
            if folio.symbol_instances.contains_key(&symbol.id) {
                return Err(ApplyError::DuplicateSymbol {
                    folio_id: *folio_id,
                    symbol_id: symbol.id,
                });
            }
            for (terminal_id, terminal) in &symbol.terminals {
                if terminal.id != *terminal_id || terminal.owner != symbol.id {
                    return Err(ApplyError::InvalidProject(
                        athena_domain::DomainError::TerminalOwnerMismatch {
                            terminal_id: *terminal_id,
                            expected_owner: symbol.id,
                            actual_owner: terminal.owner,
                        },
                    ));
                }
            }
        }
        EditorCommand::MoveItems {
            folio_id, items, ..
        }
        | EditorCommand::RotateItems {
            folio_id, items, ..
        }
        | EditorCommand::MirrorItems { folio_id, items }
        | EditorCommand::DeleteItems { folio_id, items } => {
            let folio = folio(project, *folio_id)?;
            validate_item_selection(folio, items)?;
        }
        EditorCommand::CreateWire { folio_id, wire } => {
            let folio = folio(project, *folio_id)?;
            if folio.wires.contains_key(&wire.id) {
                return Err(ApplyError::DuplicateWire {
                    folio_id: *folio_id,
                    wire_id: wire.id,
                });
            }
            validate_endpoint(project, *folio_id, &wire.start)?;
            validate_endpoint(project, *folio_id, &wire.end)?;
            validate_wire_route(project, *folio_id, wire, None)?;
        }
        EditorCommand::SplitWire {
            folio_id,
            wire_id,
            junction,
            first_wire,
            second_wire,
        } => {
            let folio = folio(project, *folio_id)?;
            if !folio.wires.contains_key(wire_id) {
                return Err(ApplyError::UnknownItem {
                    folio_id: *folio_id,
                    item: ItemId::Wire(*wire_id),
                });
            }
            if folio.junctions.contains_key(&junction.id) {
                return Err(ApplyError::DuplicateJunction {
                    folio_id: *folio_id,
                    junction_id: junction.id,
                });
            }
            if first_wire.id == second_wire.id
                || first_wire.id == *wire_id
                || second_wire.id == *wire_id
                || folio.wires.contains_key(&first_wire.id)
                || folio.wires.contains_key(&second_wire.id)
                || !wire_uses_junction(first_wire, junction.id)
                || !wire_uses_junction(second_wire, junction.id)
            {
                return Err(ApplyError::InvalidWireSplit { wire_id: *wire_id });
            }
            validate_endpoint_with_new_junction(
                project,
                *folio_id,
                &first_wire.start,
                junction.id,
            )?;
            validate_wire_route(project, *folio_id, first_wire, Some(junction))?;
            validate_wire_route(project, *folio_id, second_wire, Some(junction))?;
            validate_endpoint_with_new_junction(project, *folio_id, &first_wire.end, junction.id)?;
            validate_endpoint_with_new_junction(
                project,
                *folio_id,
                &second_wire.start,
                junction.id,
            )?;
            validate_endpoint_with_new_junction(project, *folio_id, &second_wire.end, junction.id)?;
        }
        EditorCommand::DeleteWire { folio_id, wire_id } => {
            let folio = folio(project, *folio_id)?;
            if !folio.wires.contains_key(wire_id) {
                return Err(ApplyError::UnknownItem {
                    folio_id: *folio_id,
                    item: ItemId::Wire(*wire_id),
                });
            }
        }
        EditorCommand::ReconnectWireEndpoint {
            folio_id,
            wire_id,
            terminal_id,
            endpoint,
        } => {
            let wire = validate_wire(project, *folio_id, *wire_id)?;
            validate_endpoint(project, *folio_id, &WireEndpoint::Terminal(*terminal_id))?;
            // A wire represents a connection between distinct endpoint owners.
            // Check semantics before mutation because a multi-bend route could
            // otherwise remain geometrically valid after the reconnect.
            let opposite = match endpoint {
                WireSide::Start => &wire.end,
                WireSide::End => &wire.start,
            };
            if matches!(opposite, WireEndpoint::Terminal(opposite_id) if *opposite_id == *terminal_id)
            {
                return Err(ApplyError::WireEndpointsShareTerminal {
                    wire_id: *wire_id,
                    terminal_id: *terminal_id,
                });
            }
        }
        EditorCommand::InsertWireVertex {
            folio_id,
            wire_id,
            segment_index,
            position,
        } => {
            let wire = validate_wire(project, *folio_id, *wire_id)?;
            let Some(segment) = wire.route.windows(2).nth(*segment_index) else {
                return Err(ApplyError::InvalidWireSegmentIndex {
                    wire_id: *wire_id,
                    segment_index: *segment_index,
                });
            };
            if !point_is_strictly_on_segment(*position, segment[0], segment[1]) {
                return Err(ApplyError::WireVertexNotOnSegment {
                    wire_id: *wire_id,
                    segment_index: *segment_index,
                });
            }
        }
        EditorCommand::MoveWireVertex {
            folio_id,
            wire_id,
            vertex_index,
            ..
        }
        | EditorCommand::DeleteWireVertex {
            folio_id,
            wire_id,
            vertex_index,
        } => {
            let wire = validate_wire(project, *folio_id, *wire_id)?;
            if *vertex_index == 0 || *vertex_index >= wire.route.len().saturating_sub(1) {
                return Err(ApplyError::InvalidWireVertexIndex {
                    wire_id: *wire_id,
                    vertex_index: *vertex_index,
                });
            }
        }
        EditorCommand::SetFieldValue {
            folio_id, target, ..
        } => {
            let folio = folio(project, *folio_id)?;
            let item = match target {
                FieldTarget::Symbol(id) => ItemId::Symbol(*id),
                FieldTarget::Wire(id) => ItemId::Wire(*id),
                FieldTarget::Annotation(id) => ItemId::Annotation(*id),
            };
            if !sheet_contains_item(folio, item) {
                return Err(ApplyError::UnknownItem {
                    folio_id: *folio_id,
                    item,
                });
            }
        }
        EditorCommand::ApplySchematicSettings { folio_id, settings } => {
            let _ = folio(project, *folio_id)?;
            if settings.page_width <= 0 || settings.page_height <= 0 || settings.grid_spacing <= 0 {
                return Err(ApplyError::InvalidSchematicSettings);
            }
        }
        EditorCommand::RenameFolio {
            folio_id,
            new_label,
            ..
        } => {
            let _ = folio(project, *folio_id)?;
            if new_label.trim().is_empty() {
                return Err(ApplyError::InvalidFolioLabel);
            }
        }
        EditorCommand::RestoreItems {
            folio_id,
            remove,
            restore,
        } => {
            let folio = folio(project, *folio_id)?;
            validate_distinct_items(remove)?;
            let remove_ids = remove.iter().copied().collect::<BTreeSet<_>>();
            let mut restore_ids = BTreeSet::new();
            for stored in restore {
                let item = stored.item_id();
                if !restore_ids.insert(item) {
                    return Err(ApplyError::DuplicateItem { item });
                }
                if sheet_contains_item(folio, item) && !remove_ids.contains(&item) {
                    return Err(match stored {
                        StoredItem::Symbol(symbol) => ApplyError::DuplicateSymbol {
                            folio_id: *folio_id,
                            symbol_id: symbol.id,
                        },
                        StoredItem::Wire(wire) => ApplyError::DuplicateWire {
                            folio_id: *folio_id,
                            wire_id: wire.id,
                        },
                        StoredItem::Junction(junction) => ApplyError::DuplicateJunction {
                            folio_id: *folio_id,
                            junction_id: junction.id,
                        },
                        StoredItem::Annotation(annotation) => ApplyError::InvalidProject(
                            athena_domain::DomainError::DuplicateEntityId {
                                entity_id: annotation.id.as_uuid(),
                            },
                        ),
                    });
                }
            }
        }
    }

    Ok(())
}

fn validate_wire(
    project: &Project,
    folio_id: FolioId,
    wire_id: athena_domain::WireId,
) -> Result<&athena_domain::Wire, ApplyError> {
    let wire = project
        .wire(folio_id, wire_id)
        .ok_or(ApplyError::UnknownItem {
            folio_id,
            item: ItemId::Wire(wire_id),
        })?;
    validate_wire_route(project, folio_id, wire, None)?;
    Ok(wire)
}

/// Checks the persisted route contract against positions owned by this folio.
fn validate_wire_route(
    project: &Project,
    folio_id: FolioId,
    wire: &Wire,
    pending_junction: Option<&Junction>,
) -> Result<(), ApplyError> {
    validate_canonical_wire_route(
        project,
        folio_id,
        wire.id,
        &wire.start,
        &wire.end,
        &wire.route,
        pending_junction,
    )?;

    // CreateWire and SplitWire persist the canonical route, so validate that
    // exact representation as well. A U-turn can be valid before folding but
    // collapse to an invalid endpoint pair after redundant bends are removed.
    let canonical = canonical_wire_route(&wire.route);
    validate_canonical_wire_route(
        project,
        folio_id,
        wire.id,
        &wire.start,
        &wire.end,
        &canonical,
        pending_junction,
    )
}

fn validate_canonical_wire_route(
    project: &Project,
    folio_id: FolioId,
    wire_id: athena_domain::WireId,
    start_endpoint: &WireEndpoint,
    end_endpoint: &WireEndpoint,
    route: &[Point],
    pending_junction: Option<&Junction>,
) -> Result<(), ApplyError> {
    if route.len() < 2 {
        return Err(ApplyError::WireRouteTooShort { wire_id });
    }
    let start = endpoint_position(project, folio_id, start_endpoint, pending_junction)?;
    let end = endpoint_position(project, folio_id, end_endpoint, pending_junction)?;
    if route[0] != start {
        return Err(ApplyError::WireRouteEndpointMismatch {
            wire_id,
            endpoint: WireSide::Start,
        });
    }
    if route.last().copied() != Some(end) {
        return Err(ApplyError::WireRouteEndpointMismatch {
            wire_id,
            endpoint: WireSide::End,
        });
    }
    for (segment_index, segment) in route.windows(2).enumerate() {
        if segment[0] == segment[1] {
            return Err(ApplyError::ZeroLengthWireSegment {
                wire_id,
                segment_index,
            });
        }
        if segment[0].x != segment[1].x && segment[0].y != segment[1].y {
            return Err(ApplyError::NonOrthogonalWireSegment {
                wire_id,
                segment_index,
            });
        }
    }
    Ok(())
}

fn endpoint_position(
    project: &Project,
    folio_id: FolioId,
    endpoint: &WireEndpoint,
    pending_junction: Option<&Junction>,
) -> Result<Point, ApplyError> {
    match endpoint {
        WireEndpoint::Terminal(terminal_id) => project
            .terminal(folio_id, *terminal_id)
            .map(|terminal| terminal.position)
            .ok_or(ApplyError::UnknownTerminal {
                folio_id,
                terminal_id: *terminal_id,
            }),
        WireEndpoint::Junction(junction_id)
            if pending_junction.map(|junction| junction.id) == Some(*junction_id) =>
        {
            Ok(pending_junction
                .expect("matching pending junction")
                .position)
        }
        WireEndpoint::Junction(junction_id) => project
            .junction(folio_id, *junction_id)
            .map(|junction| junction.position)
            .ok_or(ApplyError::UnknownJunction {
                folio_id,
                junction_id: *junction_id,
            }),
    }
}

fn point_is_strictly_on_segment(
    point: athena_domain::Point,
    start: athena_domain::Point,
    end: athena_domain::Point,
) -> bool {
    if point == start || point == end {
        return false;
    }
    if start.x == end.x {
        point.x == start.x && point.y > start.y.min(end.y) && point.y < start.y.max(end.y)
    } else if start.y == end.y {
        point.y == start.y && point.x > start.x.min(end.x) && point.x < start.x.max(end.x)
    } else {
        false
    }
}

fn folio(project: &Project, folio_id: FolioId) -> Result<&athena_domain::Folio, ApplyError> {
    project
        .folio(folio_id)
        .ok_or(ApplyError::UnknownFolio { folio_id })
}

fn validate_item_selection(
    folio: &athena_domain::Folio,
    items: &[ItemId],
) -> Result<(), ApplyError> {
    if items.is_empty() {
        return Err(ApplyError::EmptyItemSelection);
    }
    validate_distinct_items(items)?;
    for item in items {
        if !sheet_contains_item(folio, *item) {
            return Err(ApplyError::UnknownItem {
                folio_id: folio.id,
                item: *item,
            });
        }
    }
    Ok(())
}

fn validate_distinct_items(items: &[ItemId]) -> Result<(), ApplyError> {
    let mut seen = BTreeSet::new();
    for item in items {
        if !seen.insert(*item) {
            return Err(ApplyError::DuplicateItem { item: *item });
        }
    }
    Ok(())
}

fn validate_endpoint(
    project: &Project,
    folio_id: FolioId,
    endpoint: &WireEndpoint,
) -> Result<(), ApplyError> {
    match endpoint {
        WireEndpoint::Terminal(terminal_id)
            if project.terminal(folio_id, *terminal_id).is_none() =>
        {
            Err(ApplyError::UnknownTerminal {
                folio_id,
                terminal_id: *terminal_id,
            })
        }
        WireEndpoint::Junction(junction_id)
            if project.junction(folio_id, *junction_id).is_none() =>
        {
            Err(ApplyError::UnknownJunction {
                folio_id,
                junction_id: *junction_id,
            })
        }
        _ => Ok(()),
    }
}

fn validate_endpoint_with_new_junction(
    project: &Project,
    folio_id: FolioId,
    endpoint: &WireEndpoint,
    new_junction_id: athena_domain::JunctionId,
) -> Result<(), ApplyError> {
    if matches!(endpoint, WireEndpoint::Junction(id) if *id == new_junction_id) {
        Ok(())
    } else {
        validate_endpoint(project, folio_id, endpoint)
    }
}

fn wire_uses_junction(wire: &athena_domain::Wire, junction_id: athena_domain::JunctionId) -> bool {
    matches!(wire.start, WireEndpoint::Junction(id) if id == junction_id)
        || matches!(wire.end, WireEndpoint::Junction(id) if id == junction_id)
}
