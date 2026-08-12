use std::collections::BTreeSet;

use athena_domain::{Project, SheetId, WireEndpoint};

use crate::{
    ApplyError, EditorCommand, FieldTarget, ItemId, StoredItem, command::sheet_contains_item,
};

/// Performs all identifier and endpoint checks before an editor-state mutation
/// is attempted.
pub(crate) fn validate_command(
    project: &Project,
    command: &EditorCommand,
) -> Result<(), ApplyError> {
    match command {
        EditorCommand::PlaceSymbol { sheet_id, symbol } => {
            let sheet = sheet(project, *sheet_id)?;
            if project.symbol_definition(symbol.definition_id).is_none() {
                return Err(ApplyError::UnknownSymbolDefinition {
                    definition_id: symbol.definition_id,
                });
            }
            if sheet.symbol_instances.contains_key(&symbol.id) {
                return Err(ApplyError::DuplicateSymbol {
                    sheet_id: *sheet_id,
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
            sheet_id, items, ..
        }
        | EditorCommand::RotateItems {
            sheet_id, items, ..
        }
        | EditorCommand::MirrorItems { sheet_id, items }
        | EditorCommand::DeleteItems { sheet_id, items } => {
            let sheet = sheet(project, *sheet_id)?;
            validate_item_selection(sheet, items)?;
        }
        EditorCommand::CreateWire { sheet_id, wire } => {
            let sheet = sheet(project, *sheet_id)?;
            if sheet.wires.contains_key(&wire.id) {
                return Err(ApplyError::DuplicateWire {
                    sheet_id: *sheet_id,
                    wire_id: wire.id,
                });
            }
            validate_endpoint(project, *sheet_id, &wire.start)?;
            validate_endpoint(project, *sheet_id, &wire.end)?;
        }
        EditorCommand::SplitWire {
            sheet_id,
            wire_id,
            junction,
            first_wire,
            second_wire,
        } => {
            let sheet = sheet(project, *sheet_id)?;
            if !sheet.wires.contains_key(wire_id) {
                return Err(ApplyError::UnknownItem {
                    sheet_id: *sheet_id,
                    item: ItemId::Wire(*wire_id),
                });
            }
            if sheet.junctions.contains_key(&junction.id) {
                return Err(ApplyError::DuplicateJunction {
                    sheet_id: *sheet_id,
                    junction_id: junction.id,
                });
            }
            if first_wire.id == second_wire.id
                || first_wire.id == *wire_id
                || second_wire.id == *wire_id
                || sheet.wires.contains_key(&first_wire.id)
                || sheet.wires.contains_key(&second_wire.id)
                || !wire_uses_junction(first_wire, junction.id)
                || !wire_uses_junction(second_wire, junction.id)
            {
                return Err(ApplyError::InvalidWireSplit { wire_id: *wire_id });
            }
            validate_endpoint_with_new_junction(
                project,
                *sheet_id,
                &first_wire.start,
                junction.id,
            )?;
            validate_endpoint_with_new_junction(project, *sheet_id, &first_wire.end, junction.id)?;
            validate_endpoint_with_new_junction(
                project,
                *sheet_id,
                &second_wire.start,
                junction.id,
            )?;
            validate_endpoint_with_new_junction(project, *sheet_id, &second_wire.end, junction.id)?;
        }
        EditorCommand::DeleteWire { sheet_id, wire_id } => {
            let sheet = sheet(project, *sheet_id)?;
            if !sheet.wires.contains_key(wire_id) {
                return Err(ApplyError::UnknownItem {
                    sheet_id: *sheet_id,
                    item: ItemId::Wire(*wire_id),
                });
            }
        }
        EditorCommand::SetFieldValue {
            sheet_id, target, ..
        } => {
            let sheet = sheet(project, *sheet_id)?;
            let item = match target {
                FieldTarget::Symbol(id) => ItemId::Symbol(*id),
                FieldTarget::Wire(id) => ItemId::Wire(*id),
                FieldTarget::Annotation(id) => ItemId::Annotation(*id),
            };
            if !sheet_contains_item(sheet, item) {
                return Err(ApplyError::UnknownItem {
                    sheet_id: *sheet_id,
                    item,
                });
            }
        }
        EditorCommand::ApplySheetSettings { sheet_id, settings } => {
            let _ = sheet(project, *sheet_id)?;
            if settings.page_width <= 0 || settings.page_height <= 0 || settings.grid_spacing <= 0 {
                return Err(ApplyError::InvalidSheetSettings);
            }
        }
        EditorCommand::RestoreItems {
            sheet_id,
            remove,
            restore,
        } => {
            let sheet = sheet(project, *sheet_id)?;
            validate_distinct_items(remove)?;
            let remove_ids = remove.iter().copied().collect::<BTreeSet<_>>();
            let mut restore_ids = BTreeSet::new();
            for stored in restore {
                let item = stored.item_id();
                if !restore_ids.insert(item) {
                    return Err(ApplyError::DuplicateItem { item });
                }
                if sheet_contains_item(sheet, item) && !remove_ids.contains(&item) {
                    return Err(match stored {
                        StoredItem::Symbol(symbol) => ApplyError::DuplicateSymbol {
                            sheet_id: *sheet_id,
                            symbol_id: symbol.id,
                        },
                        StoredItem::Wire(wire) => ApplyError::DuplicateWire {
                            sheet_id: *sheet_id,
                            wire_id: wire.id,
                        },
                        StoredItem::Junction(junction) => ApplyError::DuplicateJunction {
                            sheet_id: *sheet_id,
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

fn sheet(project: &Project, sheet_id: SheetId) -> Result<&athena_domain::Sheet, ApplyError> {
    project
        .sheet(sheet_id)
        .ok_or(ApplyError::UnknownSheet { sheet_id })
}

fn validate_item_selection(
    sheet: &athena_domain::Sheet,
    items: &[ItemId],
) -> Result<(), ApplyError> {
    if items.is_empty() {
        return Err(ApplyError::EmptyItemSelection);
    }
    validate_distinct_items(items)?;
    for item in items {
        if !sheet_contains_item(sheet, *item) {
            return Err(ApplyError::UnknownItem {
                sheet_id: sheet.id,
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
    sheet_id: SheetId,
    endpoint: &WireEndpoint,
) -> Result<(), ApplyError> {
    match endpoint {
        WireEndpoint::Terminal(terminal_id)
            if project.terminal(sheet_id, *terminal_id).is_none() =>
        {
            Err(ApplyError::UnknownTerminal {
                sheet_id,
                terminal_id: *terminal_id,
            })
        }
        WireEndpoint::Junction(junction_id)
            if project.junction(sheet_id, *junction_id).is_none() =>
        {
            Err(ApplyError::UnknownJunction {
                sheet_id,
                junction_id: *junction_id,
            })
        }
        _ => Ok(()),
    }
}

fn validate_endpoint_with_new_junction(
    project: &Project,
    sheet_id: SheetId,
    endpoint: &WireEndpoint,
    new_junction_id: athena_domain::JunctionId,
) -> Result<(), ApplyError> {
    if matches!(endpoint, WireEndpoint::Junction(id) if *id == new_junction_id) {
        Ok(())
    } else {
        validate_endpoint(project, sheet_id, endpoint)
    }
}

fn wire_uses_junction(wire: &athena_domain::Wire, junction_id: athena_domain::JunctionId) -> bool {
    matches!(wire.start, WireEndpoint::Junction(id) if id == junction_id)
        || matches!(wire.end, WireEndpoint::Junction(id) if id == junction_id)
}
