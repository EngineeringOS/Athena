//! GPUI composition for Athena's Graphite-style electrical workbench.
//!
//! The shell renders the backend-owned workspace and widget plates. Native
//! controls retain focus and dialog handles, but every document action is sent
//! through [`athena_application::AthenaMessage`].

use std::collections::BTreeMap;

use athena_application::{
    DocumentMessage, LayoutMessage, LayoutTarget, PanelId, PortfolioMessage, Widget, WidgetId,
    WidgetKind, WidgetValue,
};
use athena_domain::{FolioId, ProjectId, TemplateSegment, TemplateText};
use gpui::{
    AnyElement, App, Application, Context, Entity, IntoElement, PathPromptOptions, Render,
    SharedString, Subscription, Window, WindowOptions, div, prelude::*, px, rgb,
};
use gpui_component::{
    IconName, Root, Selectable, Sizable, StyledExt,
    button::Button,
    input::{Input, InputEvent, InputState},
    scroll::ScrollableElement,
};

use crate::app::DesktopEditor;

#[derive(Clone, Copy)]
enum InputValueKind {
    Text,
    Template,
    VariableEdit,
}

struct WidgetInput {
    state: Entity<InputState>,
    _subscription: Subscription,
}

/// Native GPUI view over the effect-derived desktop adapter state.
pub struct NativeShell {
    editor: DesktopEditor,
    properties_target: LayoutTarget,
    inputs: BTreeMap<String, WidgetInput>,
}

impl NativeShell {
    fn new(window: &mut Window, cx: &mut Context<Self>) -> Self {
        let mut editor = DesktopEditor::default();
        editor.dispatch(PortfolioMessage::CreateProject {
            project_id: ProjectId::new(),
            name: "Untitled electrical project".into(),
        });
        editor.dispatch(LayoutMessage::RequestProjectPlate);
        let properties_target = LayoutTarget::Folio(
            editor
                .view_state()
                .active_folio_id
                .expect("new projects always contain a folio"),
        );
        let mut shell = Self {
            editor,
            properties_target,
            inputs: BTreeMap::new(),
        };
        shell.synchronize_inputs(window, cx);
        shell
    }

    fn dispatch(
        &mut self,
        message: impl Into<athena_application::AthenaMessage>,
        cx: &mut Context<Self>,
    ) {
        self.editor.dispatch(message);
        cx.notify();
    }

    fn control_key(target: LayoutTarget, widget_id: &WidgetId) -> String {
        match target {
            LayoutTarget::Project => format!("project:{}", widget_id.0),
            LayoutTarget::Folio(folio_id) => format!("folio:{folio_id:?}:{}", widget_id.0),
        }
    }

    fn synchronize_inputs(&mut self, window: &mut Window, cx: &mut Context<Self>) {
        if let Some(active) = self.editor.view_state().active_folio_id
            && matches!(self.properties_target, LayoutTarget::Folio(id) if id != active)
        {
            self.properties_target = LayoutTarget::Folio(active);
        }
        let mut plate_widgets = self
            .editor
            .view_state()
            .widgets(LayoutTarget::Project)
            .iter()
            .cloned()
            .map(|widget| (LayoutTarget::Project, widget))
            .collect::<Vec<_>>();
        if let Some(active) = self.editor.view_state().active_folio_id {
            plate_widgets.extend(
                self.editor
                    .view_state()
                    .widgets(LayoutTarget::Folio(active))
                    .iter()
                    .cloned()
                    .map(|widget| (LayoutTarget::Folio(active), widget)),
            );
        }

        for (target, widget) in plate_widgets {
            let kind = match (&widget.kind, &widget.value) {
                (WidgetKind::TextInput, WidgetValue::Text(_)) => Some(InputValueKind::Text),
                (WidgetKind::TextInput, WidgetValue::Template(_)) => Some(InputValueKind::Template),
                (WidgetKind::VariableTable, _) => Some(InputValueKind::VariableEdit),
                _ => None,
            };
            let Some(kind) = kind else { continue };
            let key = Self::control_key(target, &widget.id);
            if let std::collections::btree_map::Entry::Vacant(entry) = self.inputs.entry(key) {
                let initial = input_text(&widget.value);
                let placeholder = match kind {
                    InputValueKind::VariableEdit => "variable=value",
                    _ => "Enter value",
                };
                let state = cx.new(|cx| {
                    InputState::new(window, cx)
                        .default_value(initial)
                        .placeholder(placeholder)
                });
                let widget_id = widget.id.clone();
                let subscription = cx.subscribe(&state, move |this, input, event, cx| {
                    let commit = match kind {
                        InputValueKind::VariableEdit => {
                            matches!(event, InputEvent::PressEnter { .. })
                        }
                        _ => matches!(event, InputEvent::PressEnter { .. } | InputEvent::Blur),
                    };
                    if !commit {
                        return;
                    }
                    let raw = input.read(cx).value().to_string();
                    let Some(value) = committed_value(kind, &raw) else {
                        return;
                    };
                    this.dispatch(
                        LayoutMessage::CommitWidget {
                            target,
                            widget_id: widget_id.clone(),
                            value,
                        },
                        cx,
                    );
                });
                entry.insert(WidgetInput {
                    state,
                    _subscription: subscription,
                });
            } else if !matches!(kind, InputValueKind::VariableEdit) {
                let expected = input_text(&widget.value);
                let input = &self.inputs[&Self::control_key(target, &widget.id)].state;
                if input.read(cx).value().as_ref() != expected {
                    input.update(cx, |input, cx| input.set_value(expected, window, cx));
                }
            }
        }
    }

    fn request_save_dialog(&mut self, window: &mut Window, cx: &mut Context<Self>) {
        self.editor.dispatch(PortfolioMessage::RequestSave);
        if !self.editor.view_state().save_dialog_pending() {
            cx.notify();
            return;
        }
        let directory = std::env::current_dir().unwrap_or_default();
        let receiver = cx.prompt_for_new_path(&directory, Some("project.athena.json"));
        let view = cx.entity();
        cx.spawn_in(window, async move |_, window| {
            let selected = receiver.await.ok().and_then(Result::ok).flatten();
            window
                .update(|_, cx| {
                    view.update(cx, |this, cx| {
                        let _ = this.editor.complete_save_dialog(selected);
                        cx.notify();
                    })
                })
                .ok()
        })
        .detach();
    }

    fn request_open_dialog(&mut self, window: &mut Window, cx: &mut Context<Self>) {
        self.editor.dispatch(PortfolioMessage::RequestOpen);
        let receiver = cx.prompt_for_paths(PathPromptOptions {
            files: true,
            directories: false,
            multiple: false,
            prompt: Some("Open Athena electrical project".into()),
        });
        let view = cx.entity();
        cx.spawn_in(window, async move |_, window| {
            let selected = receiver
                .await
                .ok()
                .and_then(Result::ok)
                .flatten()
                .and_then(|paths| paths.into_iter().next());
            window
                .update(|_, cx| {
                    view.update(cx, |this, cx| {
                        let _ = this.editor.complete_open_dialog(selected);
                        cx.notify();
                    })
                })
                .ok()
        })
        .detach();
    }

    fn render_outline(&mut self, cx: &mut Context<Self>) -> AnyElement {
        let active = self.editor.view_state().active_folio_id;
        let outline = self.editor.view_state().outline.clone();
        let project_name = self
            .editor
            .view_state()
            .project_name
            .clone()
            .unwrap_or_else(|| "No project".into());
        div()
            .h_full()
            .flex()
            .flex_col()
            .bg(rgb(0x171a1f))
            .border_r_1()
            .border_color(rgb(0x30343b))
            .child(panel_heading("PROJECT"))
            .child(
                Button::new("project-properties")
                    .w_full()
                    .justify_start()
                    .label(project_name)
                    .selected(matches!(self.properties_target, LayoutTarget::Project))
                    .on_click(cx.listener(|this, _, _, cx| {
                        this.properties_target = LayoutTarget::Project;
                        this.dispatch(LayoutMessage::RequestProjectPlate, cx);
                    })),
            )
            .child(
                div()
                    .px_2()
                    .pt_3()
                    .pb_1()
                    .text_xs()
                    .text_color(rgb(0x8f98a6))
                    .child("FOLIOS"),
            )
            .child(
                div()
                    .flex_1()
                    .overflow_y_scrollbar()
                    .px_1()
                    .children(outline.into_iter().map(|(folio_id, label)| {
                        Button::new(("folio", folio_id.as_uuid().as_u128() as u64))
                            .w_full()
                            .justify_start()
                            .label(label)
                            .selected(active == Some(folio_id))
                            .on_click(cx.listener(move |this, _, _, cx| {
                                this.properties_target = LayoutTarget::Folio(folio_id);
                                this.dispatch(DocumentMessage::ActivateFolio { folio_id }, cx);
                            }))
                    })),
            )
            .child(
                div().p_2().child(
                    Button::new("add-folio")
                        .w_full()
                        .icon(IconName::Plus)
                        .label("Add folio")
                        .on_click(cx.listener(|this, _, _, cx| {
                            let number = this.editor.view_state().outline.len() + 1;
                            this.dispatch(
                                DocumentMessage::AddFolio {
                                    folio_id: FolioId::new(),
                                    label: format!("Folio {number}"),
                                },
                                cx,
                            );
                        })),
                ),
            )
            .into_any_element()
    }

    fn render_properties(&mut self, cx: &mut Context<Self>) -> AnyElement {
        let widgets = self
            .editor
            .view_state()
            .widgets(self.properties_target)
            .to_vec();
        let target_label = match self.properties_target {
            LayoutTarget::Project => "PROJECT DEFAULTS",
            LayoutTarget::Folio(_) => "FOLIO TITLE BLOCK",
        };
        div()
            .h_full()
            .flex()
            .flex_col()
            .bg(rgb(0x1d2026))
            .border_l_1()
            .border_color(rgb(0x30343b))
            .child(panel_heading("ELECTRICAL PROPERTIES"))
            .child(
                div()
                    .px_3()
                    .py_2()
                    .text_xs()
                    .text_color(rgb(0x8f98a6))
                    .child(target_label),
            )
            .child(
                div()
                    .flex_1()
                    .overflow_y_scrollbar()
                    .px_3()
                    .pb_4()
                    .children(
                        widgets
                            .into_iter()
                            .map(|widget| self.render_widget(self.properties_target, widget, cx)),
                    ),
            )
            .into_any_element()
    }

    fn render_widget(
        &mut self,
        target: LayoutTarget,
        widget: Widget,
        cx: &mut Context<Self>,
    ) -> AnyElement {
        let control = match &widget.kind {
            WidgetKind::Select { options } => {
                let selected = match &widget.value {
                    WidgetValue::Choice(value) => value.clone(),
                    _ => String::new(),
                };
                div()
                    .flex()
                    .gap_1()
                    .children(options.clone().into_iter().map(|option| {
                        let value = option.clone();
                        let widget_id = widget.id.clone();
                        Button::new(SharedString::from(format!("{}-{option}", widget.id.0)))
                            .small()
                            .label(option.clone())
                            .selected(option == selected)
                            .on_click(cx.listener(move |this, _, _, cx| {
                                this.dispatch(
                                    LayoutMessage::CommitWidget {
                                        target,
                                        widget_id: widget_id.clone(),
                                        value: WidgetValue::Choice(value.clone()),
                                    },
                                    cx,
                                );
                            }))
                    }))
                    .into_any_element()
            }
            WidgetKind::VariableTable => {
                let rows = match &widget.value {
                    WidgetValue::Variables(values) => values
                        .iter()
                        .map(|(key, value)| format!("{key} = {value}"))
                        .collect::<Vec<_>>(),
                    _ => Vec::new(),
                };
                let key = Self::control_key(target, &widget.id);
                let input = self.inputs.get(&key).expect("input synchronized");
                div()
                    .flex()
                    .flex_col()
                    .gap_1()
                    .children(
                        rows.into_iter()
                            .map(|row| div().px_2().py_1().bg(rgb(0x252930)).text_xs().child(row)),
                    )
                    .child(Input::new(&input.state).small())
                    .child(
                        div()
                            .text_xs()
                            .text_color(rgb(0x7f8997))
                            .child("Press Enter to set; empty value removes"),
                    )
                    .into_any_element()
            }
            WidgetKind::TextInput => {
                let key = Self::control_key(target, &widget.id);
                let input = self.inputs.get(&key).expect("input synchronized");
                Input::new(&input.state).w_full().small().into_any_element()
            }
        };
        div()
            .pt_2()
            .flex()
            .flex_col()
            .gap_1()
            .child(
                div()
                    .text_xs()
                    .text_color(rgb(0xb7bec8))
                    .child(widget.label),
            )
            .child(control)
            .into_any_element()
    }

    fn render_viewport(&self) -> AnyElement {
        let active = self.editor.view_state().active_folio_id;
        let label = active
            .and_then(|active| {
                self.editor
                    .view_state()
                    .outline
                    .iter()
                    .find(|(id, _)| *id == active)
                    .map(|(_, label)| label.clone())
            })
            .unwrap_or_else(|| "No folio".into());
        let title = active
            .and_then(|folio_id| {
                self.editor
                    .view_state()
                    .widgets(LayoutTarget::Folio(folio_id))
                    .iter()
                    .find(|widget| widget.id == WidgetId::new("folio.title"))
                    .map(|widget| input_text(&widget.value))
            })
            .filter(|title| !title.is_empty())
            .unwrap_or_else(|| "Untitled schematic".into());

        div()
            .h_full()
            .flex()
            .flex_col()
            .bg(rgb(0x111318))
            .child(
                div()
                    .h(px(34.0))
                    .flex()
                    .items_center()
                    .px_3()
                    .bg(rgb(0x1b1e24))
                    .border_b_1()
                    .border_color(rgb(0x30343b))
                    .text_sm()
                    .child(label.clone()),
            )
            .child(
                div()
                    .flex_1()
                    .min_h(px(0.0))
                    .p_5()
                    .flex()
                    .items_center()
                    .justify_center()
                    .child(
                        div()
                            .w_full()
                            .h_full()
                            .max_w(px(980.0))
                            .max_h(px(680.0))
                            .min_w(px(420.0))
                            .min_h(px(300.0))
                            .flex()
                            .flex_col()
                            .bg(rgb(0xf7f8fa))
                            .text_color(rgb(0x22262d))
                            .border_1()
                            .border_color(rgb(0x707782))
                            .child(
                                div()
                                    .flex_1()
                                    .p_5()
                                    .text_xs()
                                    .text_color(rgb(0x717985))
                                    .child("Electrical schematic viewport"),
                            )
                            .child(
                                div()
                                    .h(px(76.0))
                                    .flex()
                                    .border_t_1()
                                    .border_color(rgb(0x707782))
                                    .child(
                                        div()
                                            .flex_1()
                                            .p_3()
                                            .flex()
                                            .flex_col()
                                            .justify_between()
                                            .child(title)
                                            .child(label),
                                    )
                                    .child(
                                        div()
                                            .w(px(150.0))
                                            .p_3()
                                            .border_l_1()
                                            .border_color(rgb(0x707782))
                                            .text_xs()
                                            .child("ATHENA ELECTRICAL"),
                                    ),
                            ),
                    ),
            )
            .into_any_element()
    }
}

impl Render for NativeShell {
    fn render(&mut self, window: &mut Window, cx: &mut Context<Self>) -> impl IntoElement {
        self.synchronize_inputs(window, cx);
        let view = self.editor.view_state();
        let workspace = view.workspace.clone();
        let project_name = view.project_name.clone().unwrap_or_else(|| "Athena".into());
        let dirty_marker = if view.dirty { " *" } else { "" };
        let status = view.status.clone().unwrap_or_else(|| "Ready".into());
        let outline_count = view.outline.len();

        div()
            .size_full()
            .flex()
            .flex_col()
            .bg(rgb(0x111318))
            .text_color(rgb(0xdfe3e8))
            .child(
                div()
                    .h(px(38.0))
                    .flex()
                    .items_center()
                    .gap_1()
                    .px_2()
                    .bg(rgb(0x20242a))
                    .border_b_1()
                    .border_color(rgb(0x353a43))
                    .child(
                        div()
                            .px_2()
                            .font_semibold()
                            .text_color(rgb(0x5cc8b2))
                            .child("ATHENA"),
                    )
                    .child(
                        div()
                            .flex_1()
                            .px_2()
                            .text_sm()
                            .child(format!("{project_name}{dirty_marker}")),
                    )
                    .child(
                        Button::new("open")
                            .icon(IconName::FolderOpen)
                            .tooltip("Open project")
                            .on_click(cx.listener(|this, _, window, cx| {
                                this.request_open_dialog(window, cx)
                            })),
                    )
                    .child(
                        Button::new("save")
                            .icon(IconName::File)
                            .tooltip("Save project")
                            .on_click(cx.listener(|this, _, window, cx| {
                                this.request_save_dialog(window, cx)
                            })),
                    )
                    .child(
                        Button::new("undo")
                            .icon(IconName::Undo2)
                            .tooltip("Undo")
                            .on_click(cx.listener(|this, _, _, cx| {
                                this.dispatch(DocumentMessage::Undo, cx)
                            })),
                    )
                    .child(
                        Button::new("redo")
                            .icon(IconName::Redo2)
                            .tooltip("Redo")
                            .on_click(cx.listener(|this, _, _, cx| {
                                this.dispatch(DocumentMessage::Redo, cx)
                            })),
                    )
                    .child(
                        Button::new("left-panel")
                            .icon(IconName::PanelLeft)
                            .tooltip("Toggle project outline")
                            .selected(workspace.left_panel.open)
                            .on_click(cx.listener(|this, _, _, cx| {
                                let open = !this.editor.view_state().workspace.left_panel.open;
                                this.dispatch(
                                    LayoutMessage::SetPanelOpen {
                                        panel_id: PanelId::ProjectOutline,
                                        open,
                                    },
                                    cx,
                                );
                            })),
                    )
                    .child(
                        Button::new("right-panel")
                            .icon(IconName::PanelRight)
                            .tooltip("Toggle electrical properties")
                            .selected(workspace.right_panel.open)
                            .on_click(cx.listener(|this, _, _, cx| {
                                let open = !this.editor.view_state().workspace.right_panel.open;
                                this.dispatch(
                                    LayoutMessage::SetPanelOpen {
                                        panel_id: PanelId::Properties,
                                        open,
                                    },
                                    cx,
                                );
                            })),
                    ),
            )
            .child(
                div()
                    .flex_1()
                    .min_h(px(0.0))
                    .flex()
                    .when(workspace.left_panel.open, |this| {
                        this.child(
                            div()
                                .w(px(f32::from(workspace.left_panel.size)))
                                .min_w(px(190.0))
                                .child(self.render_outline(cx)),
                        )
                    })
                    .child(
                        div()
                            .flex_1()
                            .min_w(px(360.0))
                            .child(self.render_viewport()),
                    )
                    .when(workspace.right_panel.open, |this| {
                        this.child(
                            div()
                                .w(px(f32::from(workspace.right_panel.size)))
                                .min_w(px(260.0))
                                .child(self.render_properties(cx)),
                        )
                    }),
            )
            .child(
                div()
                    .h(px(24.0))
                    .flex()
                    .items_center()
                    .justify_between()
                    .px_3()
                    .bg(rgb(0x20242a))
                    .border_t_1()
                    .border_color(rgb(0x353a43))
                    .text_xs()
                    .text_color(rgb(0xaeb5bf))
                    .child(status)
                    .child(format!("{outline_count} folios  |  Local project")),
            )
    }
}

fn panel_heading(label: &'static str) -> AnyElement {
    div()
        .h(px(32.0))
        .flex()
        .items_center()
        .px_3()
        .bg(rgb(0x20242a))
        .border_b_1()
        .border_color(rgb(0x30343b))
        .text_xs()
        .font_semibold()
        .child(label)
        .into_any_element()
}

fn input_text(value: &WidgetValue) -> String {
    match value {
        WidgetValue::Text(value) | WidgetValue::Choice(value) => value.clone(),
        WidgetValue::Template(value) => template_source(value),
        WidgetValue::Variables(_) | WidgetValue::VariableEdit { .. } => String::new(),
    }
}

fn template_source(value: &TemplateText) -> String {
    value
        .0
        .iter()
        .map(|segment| match segment {
            TemplateSegment::Literal(value) => value.clone(),
            TemplateSegment::Variable(athena_domain::VariableReference::Project(key)) => {
                format!("{{project:{key}}}")
            }
            TemplateSegment::Variable(athena_domain::VariableReference::Folio(key)) => {
                format!("{{folio:{key}}}")
            }
        })
        .collect()
}

fn committed_value(kind: InputValueKind, raw: &str) -> Option<WidgetValue> {
    match kind {
        InputValueKind::Text => Some(WidgetValue::Text(raw.into())),
        InputValueKind::Template => Some(WidgetValue::Template(TemplateText::literal(raw))),
        InputValueKind::VariableEdit => {
            let (key, value) = raw.split_once('=')?;
            let key = key.trim();
            if key.is_empty() {
                return None;
            }
            let value = value.trim();
            Some(WidgetValue::VariableEdit {
                key: key.into(),
                value: (!value.is_empty()).then(|| value.into()),
            })
        }
    }
}

/// Starts the desktop application and installs gpui-component services.
pub fn run_native_shell() {
    Application::new().run(|cx: &mut App| {
        gpui_component::init(cx);
        let _ = cx.open_window(WindowOptions::default(), |window, cx| {
            let view = cx.new(|cx| NativeShell::new(window, cx));
            cx.new(|cx| Root::new(view, window, cx))
        });
        cx.activate(true);
    });
}

#[cfg(test)]
mod tests {
    use athena_application::WidgetValue;

    use super::{InputValueKind, committed_value};

    #[test]
    fn variable_input_maps_to_the_backend_owned_variable_callback_value() {
        assert_eq!(
            committed_value(InputValueKind::VariableEdit, "plant = PLANT-A"),
            Some(WidgetValue::VariableEdit {
                key: "plant".into(),
                value: Some("PLANT-A".into()),
            })
        );
        assert_eq!(
            committed_value(InputValueKind::VariableEdit, "plant="),
            Some(WidgetValue::VariableEdit {
                key: "plant".into(),
                value: None,
            })
        );
    }
}
