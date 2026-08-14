//! Backend-owned QElectroTech-evidenced electrical property plates.

use std::collections::BTreeMap;

use athena_domain::{FolioId, TitleBlockPlacement, resolve_template_text};
use athena_editor::{TitleBlockField, TitleBlockValue};
use serde::{Deserialize, Serialize};

use crate::{
    AthenaFrontendMessage, AthenaMessage, DocumentMessage, EditorSnapshot, LayoutMessage,
    ResolvedTitleBlockDisplay, Widget, WidgetCallback, WidgetId, WidgetKind, WidgetValue,
};

/// Context whose property plate is being rendered or updated.
#[derive(Clone, Copy, Debug, Deserialize, Eq, Ord, PartialEq, PartialOrd, Serialize)]
pub enum LayoutTarget {
    Project,
    Folio(FolioId),
}

/// Result of one layout message without direct document-handler calls.
pub(crate) struct PlateOutput {
    pub effects: Vec<AthenaFrontendMessage>,
    pub messages: Vec<AthenaMessage>,
}

/// Owns workspace state, rendered plate caches, and stable callback routing.
#[derive(Default)]
pub struct PlateHandler {
    rendered: BTreeMap<LayoutTarget, Vec<Widget>>,
    callbacks: BTreeMap<(LayoutTarget, WidgetId), WidgetCallback>,
}

impl PlateHandler {
    pub(crate) fn handle(
        &mut self,
        message: LayoutMessage,
        state: Option<&EditorSnapshot>,
    ) -> PlateOutput {
        let mut output = PlateOutput {
            effects: Vec::new(),
            messages: Vec::new(),
        };
        match message {
            LayoutMessage::RequestProjectPlate => {
                if let Some(state) = state {
                    self.emit_plate(
                        LayoutTarget::Project,
                        project_widgets(state),
                        &mut output.effects,
                    );
                }
            }
            LayoutMessage::RequestFolioPlate { folio_id } => {
                if let Some(state) = state
                    && let Some(widgets) = folio_widgets(state, folio_id)
                {
                    if let Some(display) = resolved_title_block(state, folio_id) {
                        output
                            .effects
                            .push(AthenaFrontendMessage::ResolvedTitleBlockUpdated {
                                folio_id,
                                display: Box::new(display),
                            });
                    }
                    self.emit_plate(LayoutTarget::Folio(folio_id), widgets, &mut output.effects);
                }
            }
            LayoutMessage::CommitWidget {
                target,
                widget_id,
                value,
            } => {
                let Some(callback) = self.callbacks.get(&(target, widget_id)).cloned() else {
                    output.effects.push(AthenaFrontendMessage::Diagnostic {
                        code: "unknown-widget".into(),
                        message: "Widget callback is no longer valid".into(),
                    });
                    return output;
                };
                match callback_message(callback, value) {
                    Ok(message) => output.messages.push(message.into()),
                    Err(message) => output.effects.push(AthenaFrontendMessage::Diagnostic {
                        code: "invalid-widget-value".into(),
                        message,
                    }),
                }
            }
        }
        output
    }

    fn emit_plate(
        &mut self,
        target: LayoutTarget,
        widgets: Vec<(Widget, WidgetCallback)>,
        effects: &mut Vec<AthenaFrontendMessage>,
    ) {
        for (widget, callback) in &widgets {
            self.callbacks
                .insert((target, widget.id.clone()), callback.clone());
        }
        let next = widgets
            .into_iter()
            .map(|(widget, _)| widget)
            .collect::<Vec<_>>();
        match self.rendered.get(&target) {
            None => effects.push(AthenaFrontendMessage::PanelLayoutUpdated {
                target,
                widgets: next.clone(),
            }),
            Some(previous) if !same_structure(previous, &next) => {
                effects.push(AthenaFrontendMessage::PanelLayoutUpdated {
                    target,
                    widgets: next.clone(),
                })
            }
            Some(previous) => {
                let values = previous
                    .iter()
                    .zip(&next)
                    .filter(|(a, b)| a.value != b.value)
                    .map(|(_, widget)| (widget.id.clone(), widget.value.clone()))
                    .collect::<Vec<_>>();
                if !values.is_empty() {
                    effects.push(AthenaFrontendMessage::WidgetValuesUpdated { target, values });
                }
            }
        }
        self.rendered.insert(target, next);
    }
}

fn resolved_title_block(
    state: &EditorSnapshot,
    folio_id: FolioId,
) -> Option<ResolvedTitleBlockDisplay> {
    let folio = state.project.folio(folio_id)?;
    let values = &folio.title_block;
    let resolve =
        |template| resolve_template_text(template, &state.project.variables, &folio.variables);
    Some(ResolvedTitleBlockDisplay {
        title: resolve(&values.title),
        author: resolve(&values.author),
        date_text: resolve(&values.date_text),
        file_label: resolve(&values.file_label),
        folio_label: resolve(&values.folio_label),
        plant: resolve(&values.plant),
        location: resolve(&values.location),
        revision: resolve(&values.revision),
        page_number: resolve(&values.page_number),
    })
}

fn same_structure(left: &[Widget], right: &[Widget]) -> bool {
    left.len() == right.len()
        && left
            .iter()
            .zip(right)
            .all(|(a, b)| a.id == b.id && a.label == b.label && a.kind == b.kind)
}

fn widget(
    id: &str,
    label: &str,
    kind: WidgetKind,
    value: WidgetValue,
    callback: WidgetCallback,
) -> (Widget, WidgetCallback) {
    (
        Widget {
            id: WidgetId::new(id),
            label: label.into(),
            kind,
            value,
        },
        callback,
    )
}

fn project_widgets(state: &EditorSnapshot) -> Vec<(Widget, WidgetCallback)> {
    let defaults = &state.project.folio_defaults;
    vec![
        widget(
            "project.name",
            "Project name",
            WidgetKind::TextInput,
            WidgetValue::Text(state.project.name.clone()),
            WidgetCallback::RenameProject,
        ),
        widget(
            "project.variables",
            "Project variables",
            WidgetKind::VariableTable,
            WidgetValue::Variables(state.project.variables.clone()),
            WidgetCallback::ProjectVariables,
        ),
        widget(
            "project.default_template",
            "Default template",
            WidgetKind::TextInput,
            WidgetValue::Text(defaults.title_block.template_id.clone()),
            WidgetCallback::DefaultTemplate,
        ),
        widget(
            "project.default_placement",
            "Default placement",
            WidgetKind::Select {
                options: vec!["Bottom".into(), "Right".into()],
            },
            WidgetValue::Choice(format!("{:?}", defaults.title_block.placement)),
            WidgetCallback::DefaultPlacement,
        ),
        widget(
            "project.default_page_number",
            "Default Page Num",
            WidgetKind::TextInput,
            WidgetValue::Template(defaults.title_block.page_number.clone()),
            WidgetCallback::DefaultPageNumber,
        ),
    ]
}

fn folio_widgets(
    state: &EditorSnapshot,
    folio_id: FolioId,
) -> Option<Vec<(Widget, WidgetCallback)>> {
    let folio = state.project.folio(folio_id)?;
    let text = |id, label, field, value| {
        widget(
            id,
            label,
            WidgetKind::TextInput,
            WidgetValue::Template(value),
            WidgetCallback::TitleBlock { folio_id, field },
        )
    };
    Some(vec![
        widget(
            "folio.label",
            "Folio label",
            WidgetKind::TextInput,
            WidgetValue::Text(folio.label.clone()),
            WidgetCallback::RenameFolio { folio_id },
        ),
        widget(
            "folio.template",
            "Template",
            WidgetKind::TextInput,
            WidgetValue::Text(folio.title_block.template_id.clone()),
            WidgetCallback::TitleBlock {
                folio_id,
                field: TitleBlockField::TemplateId,
            },
        ),
        widget(
            "folio.placement",
            "Placement",
            WidgetKind::Select {
                options: vec!["Bottom".into(), "Right".into()],
            },
            WidgetValue::Choice(format!("{:?}", folio.title_block.placement)),
            WidgetCallback::TitleBlock {
                folio_id,
                field: TitleBlockField::Placement,
            },
        ),
        text(
            "folio.title",
            "Title",
            TitleBlockField::Title,
            folio.title_block.title.clone(),
        ),
        text(
            "folio.author",
            "Author",
            TitleBlockField::Author,
            folio.title_block.author.clone(),
        ),
        text(
            "folio.date",
            "Date",
            TitleBlockField::DateText,
            folio.title_block.date_text.clone(),
        ),
        text(
            "folio.file",
            "File",
            TitleBlockField::FileLabel,
            folio.title_block.file_label.clone(),
        ),
        text(
            "folio.folio",
            "Folio",
            TitleBlockField::FolioLabel,
            folio.title_block.folio_label.clone(),
        ),
        text(
            "folio.plant",
            "Plant",
            TitleBlockField::Plant,
            folio.title_block.plant.clone(),
        ),
        text(
            "folio.location",
            "Location",
            TitleBlockField::Location,
            folio.title_block.location.clone(),
        ),
        text(
            "folio.revision",
            "Rev index",
            TitleBlockField::Revision,
            folio.title_block.revision.clone(),
        ),
        text(
            "folio.page_number",
            "Page Num",
            TitleBlockField::PageNumber,
            folio.title_block.page_number.clone(),
        ),
        widget(
            "folio.variables",
            "Folio variables",
            WidgetKind::VariableTable,
            WidgetValue::Variables(folio.variables.clone()),
            WidgetCallback::FolioVariables { folio_id },
        ),
    ])
}

fn callback_message(
    callback: WidgetCallback,
    value: WidgetValue,
) -> Result<DocumentMessage, String> {
    match (callback, value) {
        (WidgetCallback::RenameProject, WidgetValue::Text(name)) if !name.trim().is_empty() => {
            Ok(DocumentMessage::RenameProject { name })
        }
        (WidgetCallback::ProjectVariables, WidgetValue::VariableEdit { key, value }) => {
            Ok(DocumentMessage::SetProjectVariable { key, value })
        }
        (WidgetCallback::DefaultTemplate, WidgetValue::Text(template_id))
            if !template_id.trim().is_empty() =>
        {
            Ok(DocumentMessage::SetDefaultTemplate { template_id })
        }
        (WidgetCallback::DefaultPlacement, WidgetValue::Choice(value)) => {
            Ok(DocumentMessage::SetDefaultPlacement {
                placement: parse_placement(&value)?,
            })
        }
        (WidgetCallback::DefaultPageNumber, WidgetValue::Template(value)) => {
            Ok(DocumentMessage::SetDefaultPageNumber { value })
        }
        (WidgetCallback::RenameFolio { folio_id }, WidgetValue::Text(label))
            if !label.trim().is_empty() =>
        {
            Ok(DocumentMessage::RenameFolio { folio_id, label })
        }
        (WidgetCallback::FolioVariables { folio_id }, WidgetValue::VariableEdit { key, value }) => {
            Ok(DocumentMessage::SetFolioVariable {
                folio_id,
                key,
                value,
            })
        }
        (
            WidgetCallback::TitleBlock {
                folio_id,
                field: TitleBlockField::TemplateId,
            },
            WidgetValue::Text(value),
        ) => Ok(DocumentMessage::SetTitleBlockValue {
            folio_id,
            field: TitleBlockField::TemplateId,
            value: TitleBlockValue::Text(value),
        }),
        (
            WidgetCallback::TitleBlock {
                folio_id,
                field: TitleBlockField::Placement,
            },
            WidgetValue::Choice(value),
        ) => Ok(DocumentMessage::SetTitleBlockValue {
            folio_id,
            field: TitleBlockField::Placement,
            value: TitleBlockValue::Placement(parse_placement(&value)?),
        }),
        (WidgetCallback::TitleBlock { folio_id, field }, WidgetValue::Template(value)) => {
            Ok(DocumentMessage::SetTitleBlockValue {
                folio_id,
                field,
                value: TitleBlockValue::Template(value),
            })
        }
        _ => Err("Widget value does not match its Rust callback".into()),
    }
}

fn parse_placement(value: &str) -> Result<TitleBlockPlacement, String> {
    match value {
        "Bottom" => Ok(TitleBlockPlacement::Bottom),
        "Right" => Ok(TitleBlockPlacement::Right),
        _ => Err("Unknown title-block placement".into()),
    }
}
