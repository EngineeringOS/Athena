//! GPUI composition for Athena's Graphite-style electrical workbench.
//!
//! The shell renders the backend-owned workspace and widget plates. Native
//! controls retain focus and dialog handles, but every document action is sent
//! through [`athena_application::AthenaMessage`].

use std::collections::BTreeMap;

use athena_application::{
    DocumentMessage, LayoutMessage, LayoutTarget, PanelRole, PortfolioMessage, ShellMessage,
    ShellNode, TabId, Widget, WidgetId, WidgetKind, WidgetValue, WorkspaceShell,
};
use athena_domain::{FolioId, ProjectId, TemplateSegment, TemplateText, VariableReference};
use gpui::{
    AnyElement, App, Application, Context, Entity, Focusable, IntoElement, PathPromptOptions,
    Render, SharedString, Subscription, Window, WindowOptions, div, prelude::*, px, rgb,
};
use gpui_component::{
    IconName, Root, Selectable, Sizable, StyledExt,
    button::Button,
    input::{Input, InputEvent, InputState},
    scroll::ScrollableElement,
};

use crate::app::DesktopEditor;

#[derive(Clone)]
enum InputValueKind {
    Text,
    TemplateWithSegments(TemplateText),
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
            initial_folio_id: FolioId::new(),
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

    fn toggle_panel(&mut self, role: PanelRole, cx: &mut Context<Self>) {
        let open_tab = tab_id_for_role(&self.editor.view_state().shell.root, role);
        let stable_tab =
            open_tab.or_else(|| tab_id_for_role(&WorkspaceShell::default().root, role));
        let Some(tab_id) = stable_tab else {
            return;
        };
        let message = if open_tab.is_some() {
            ShellMessage::ClosePanel { tab_id }
        } else {
            ShellMessage::ReopenPanel { tab_id }
        };
        self.dispatch(message, cx);
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
                (WidgetKind::TextInput, WidgetValue::Template(template)) => {
                    Some(InputValueKind::TemplateWithSegments(template.clone()))
                }
                (WidgetKind::VariableTable, _) => Some(InputValueKind::VariableEdit),
                _ => None,
            };
            let Some(kind) = kind else { continue };
            let key = Self::control_key(target, &widget.id);
            if let std::collections::btree_map::Entry::Vacant(entry) = self.inputs.entry(key) {
                let initial = input_text(&widget.value);
                let placeholder = match &kind {
                    InputValueKind::VariableEdit => "variable=value",
                    _ => "Enter value",
                };
                let state = cx.new(|cx| {
                    InputState::new(window, cx)
                        .default_value(initial)
                        .placeholder(placeholder)
                });
                let widget_id = widget.id.clone();
                let callback_kind = kind.clone();
                let subscription = cx.subscribe(&state, move |this, input, event, cx| {
                    let commit = matches!(event, InputEvent::PressEnter { .. } | InputEvent::Blur);
                    if !commit {
                        return;
                    }
                    let raw = input.read(cx).value().to_string();
                    let Some(value) = committed_value(callback_kind.clone(), &raw) else {
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
            } else {
                let expected = input_text(&widget.value);
                let input = &self.inputs[&Self::control_key(target, &widget.id)].state;
                let is_focused = input.read(cx).focus_handle(cx).is_focused(window);
                if !is_focused && input.read(cx).value().as_ref() != expected {
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
        let outline_len = outline.len();
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
                div().debug_selector(|| "project-properties".into()).child(
                    Button::new("project-properties")
                        .w_full()
                        .justify_start()
                        .label(project_name)
                        .selected(matches!(self.properties_target, LayoutTarget::Project))
                        .on_click(cx.listener(|this, _, _, cx| {
                            this.properties_target = LayoutTarget::Project;
                            this.dispatch(LayoutMessage::RequestProjectPlate, cx);
                        })),
                ),
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
                div().flex_1().overflow_y_scrollbar().px_1().children(
                    outline
                        .into_iter()
                        .enumerate()
                        .map(|(index, (folio_id, label))| {
                            let folio_selector = format!("folio-{folio_id:?}");
                            let move_up_selector = format!("move-up-{folio_id:?}");
                            let move_down_selector = format!("move-down-{folio_id:?}");
                            div()
                                .flex()
                                .items_center()
                                .gap_1()
                                .child(
                                    div().flex_1().debug_selector(move || folio_selector).child(
                                        Button::new(("folio", folio_id.as_uuid().as_u128() as u64))
                                            .w_full()
                                            .justify_start()
                                            .label(label)
                                            .selected(active == Some(folio_id))
                                            .on_click(cx.listener(move |this, _, _, cx| {
                                                this.properties_target =
                                                    LayoutTarget::Folio(folio_id);
                                                this.dispatch(
                                                    DocumentMessage::ActivateFolio { folio_id },
                                                    cx,
                                                );
                                            })),
                                    ),
                                )
                                .when(index > 0, |row| {
                                    row.child(
                                        div().debug_selector(move || move_up_selector).child(
                                            Button::new((
                                                "move-up",
                                                folio_id.as_uuid().as_u128() as u64,
                                            ))
                                            .small()
                                            .icon(IconName::ArrowUp)
                                            .tooltip("Move folio up")
                                            .on_click(cx.listener(move |this, _, _, cx| {
                                                this.dispatch(
                                                    DocumentMessage::MoveFolio {
                                                        folio_id,
                                                        to: index - 1,
                                                    },
                                                    cx,
                                                );
                                            })),
                                        ),
                                    )
                                })
                                .when(index + 1 < outline_len, |row| {
                                    row.child(
                                        div().debug_selector(move || move_down_selector).child(
                                            Button::new((
                                                "move-down",
                                                folio_id.as_uuid().as_u128() as u64,
                                            ))
                                            .small()
                                            .icon(IconName::ArrowDown)
                                            .tooltip("Move folio down")
                                            .on_click(cx.listener(move |this, _, _, cx| {
                                                this.dispatch(
                                                    DocumentMessage::MoveFolio {
                                                        folio_id,
                                                        to: index + 1,
                                                    },
                                                    cx,
                                                );
                                            })),
                                        ),
                                    )
                                })
                        }),
                ),
            )
            .child(
                div().p_2().child(
                    div().debug_selector(|| "add-folio".into()).child(
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
                    .debug_selector(|| "properties-scroll".into())
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
        let widget_selector = format!("widget-{}", widget.id.0.replace(['.', '_'], "-"));
        let template_references = matches!(widget.value, WidgetValue::Template(_))
            .then(|| self.variable_references(target))
            .unwrap_or_default();
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
                    .child(
                        div()
                            .debug_selector(move || widget_selector.clone())
                            .child(Input::new(&input.state).small()),
                    )
                    .child(
                        div()
                            .text_xs()
                            .text_color(rgb(0x7f8997))
                            .child("Enter or leave field to set; empty value removes"),
                    )
                    .into_any_element()
            }
            WidgetKind::TextInput => {
                let key = Self::control_key(target, &widget.id);
                let input = self.inputs.get(&key).expect("input synchronized");
                let input = div()
                    .debug_selector(move || widget_selector)
                    .child(Input::new(&input.state).w_full().small());
                if template_references.is_empty() {
                    input.into_any_element()
                } else {
                    let widget_id = widget.id.clone();
                    let template = match &widget.value {
                        WidgetValue::Template(template) => template.clone(),
                        _ => unreachable!("reference pickers are only built for template widgets"),
                    };
                    div()
                        .flex()
                        .flex_col()
                        .gap_1()
                        .child(input)
                        .child(
                            div().flex().flex_wrap().gap_1().children(
                                template_references
                                    .into_iter()
                                    .map(|(scope, key, reference)| {
                                        let selector = format!(
                                            "reference-{}-{scope}-{key}",
                                            widget_id.0.replace(['.', '_'], "-")
                                        );
                                        let mut next = template.clone();
                                        next.0.push(TemplateSegment::Variable(reference));
                                        let callback_id = widget_id.clone();
                                        div().debug_selector(move || selector).child(
                                            Button::new(SharedString::from(format!(
                                                "reference-{}-{scope}-{key}",
                                                callback_id.0
                                            )))
                                            .small()
                                            .label(format!("{scope}: {key}"))
                                            .on_click(cx.listener(move |this, _, _, cx| {
                                                this.dispatch(
                                                    LayoutMessage::CommitWidget {
                                                        target,
                                                        widget_id: callback_id.clone(),
                                                        value: WidgetValue::Template(next.clone()),
                                                    },
                                                    cx,
                                                );
                                            })),
                                        )
                                    }),
                            ),
                        )
                        .into_any_element()
                }
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

    fn variable_references(
        &self,
        target: LayoutTarget,
    ) -> Vec<(&'static str, String, VariableReference)> {
        let mut references = variables_from_widgets(
            self.editor.view_state().widgets(LayoutTarget::Project),
            "project.variables",
        )
        .map(|key| ("project", key.clone(), VariableReference::Project(key)))
        .collect::<Vec<_>>();
        if let LayoutTarget::Folio(folio_id) = target {
            references.extend(
                variables_from_widgets(
                    self.editor
                        .view_state()
                        .widgets(LayoutTarget::Folio(folio_id)),
                    "folio.variables",
                )
                .map(|key| ("folio", key.clone(), VariableReference::Folio(key))),
            );
        }
        references
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
        let resolved =
            active.and_then(|folio_id| self.editor.view_state().resolved_title_block(folio_id));
        let title = resolved
            .map(|display| display.title.text.clone())
            .filter(|title| !title.is_empty())
            .unwrap_or_else(|| "Untitled schematic".into());
        let plant_location = resolved
            .map(|display| {
                [display.plant.text.as_str(), display.location.text.as_str()]
                    .into_iter()
                    .filter(|value| !value.is_empty())
                    .collect::<Vec<_>>()
                    .join(" / ")
            })
            .unwrap_or_default();
        let page_revision = resolved
            .map(|display| {
                [
                    (!display.page_number.text.is_empty())
                        .then(|| format!("Page {}", display.page_number.text)),
                    (!display.revision.text.is_empty())
                        .then(|| format!("Rev {}", display.revision.text)),
                ]
                .into_iter()
                .flatten()
                .collect::<Vec<_>>()
                .join("  ")
            })
            .unwrap_or_default();

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
                                            .child(
                                                div()
                                                    .debug_selector(|| {
                                                        "viewport-resolved-title".into()
                                                    })
                                                    .child(title),
                                            )
                                            .child(
                                                div()
                                                    .flex()
                                                    .justify_between()
                                                    .child(label)
                                                    .child(plant_location),
                                            ),
                                    )
                                    .child(
                                        div()
                                            .w(px(150.0))
                                            .p_3()
                                            .border_l_1()
                                            .border_color(rgb(0x707782))
                                            .text_xs()
                                            .flex()
                                            .flex_col()
                                            .justify_between()
                                            .child("ATHENA ELECTRICAL")
                                            .child(page_revision),
                                    ),
                            ),
                    ),
            )
            .into_any_element()
    }
}

fn tab_id_for_role(node: &ShellNode, role: PanelRole) -> Option<TabId> {
    match node {
        ShellNode::PanelGroup(group) => group
            .tabs
            .iter()
            .find(|tab| tab.role == role)
            .map(|tab| tab.id),
        ShellNode::Split(split) => split
            .children
            .iter()
            .find_map(|child| tab_id_for_role(&child.node, role)),
    }
}

impl Render for NativeShell {
    fn render(&mut self, window: &mut Window, cx: &mut Context<Self>) -> impl IntoElement {
        self.synchronize_inputs(window, cx);
        let view = self.editor.view_state();
        let shell = view.shell.clone();
        let left_open = tab_id_for_role(&shell.root, PanelRole::Project).is_some();
        let right_open = tab_id_for_role(&shell.root, PanelRole::SelectionProperties).is_some();
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
                        div().debug_selector(|| "reopen".into()).child(
                            Button::new("reopen")
                                .icon(IconName::FolderOpen)
                                .tooltip("Reopen recent project")
                                .on_click(cx.listener(|this, _, _, cx| {
                                    let _ = this.editor.reopen_recent_project();
                                    cx.notify();
                                })),
                        ),
                    )
                    .child(
                        div().debug_selector(|| "close".into()).child(
                            Button::new("close")
                                .icon(IconName::Close)
                                .tooltip("Close project")
                                .on_click(cx.listener(|this, _, _, cx| {
                                    this.dispatch(PortfolioMessage::CloseProject, cx)
                                })),
                        ),
                    )
                    .child(
                        div().debug_selector(|| "save".into()).child(
                            Button::new("save")
                                .icon(IconName::File)
                                .tooltip("Save project")
                                .on_click(cx.listener(|this, _, window, cx| {
                                    this.request_save_dialog(window, cx)
                                })),
                        ),
                    )
                    .child(
                        div().debug_selector(|| "undo".into()).child(
                            Button::new("undo")
                                .icon(IconName::Undo2)
                                .tooltip("Undo")
                                .on_click(cx.listener(|this, _, _, cx| {
                                    this.dispatch(DocumentMessage::Undo, cx)
                                })),
                        ),
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
                            .selected(left_open)
                            .on_click(cx.listener(|this, _, _, cx| {
                                this.toggle_panel(PanelRole::Project, cx);
                            })),
                    )
                    .child(
                        Button::new("right-panel")
                            .icon(IconName::PanelRight)
                            .tooltip("Toggle electrical properties")
                            .selected(right_open)
                            .on_click(cx.listener(|this, _, _, cx| {
                                this.toggle_panel(PanelRole::SelectionProperties, cx);
                            })),
                    ),
            )
            .child(
                div()
                    .flex_1()
                    .min_h(px(0.0))
                    .flex()
                    .when(left_open, |this| {
                        this.child(
                            div()
                                .w(px(240.0))
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
                    .when(right_open, |this| {
                        this.child(
                            div()
                                .w(px(300.0))
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
        WidgetValue::Template(value) => template_literal_text(value),
        WidgetValue::Variables(_) | WidgetValue::VariableEdit { .. } => String::new(),
    }
}

fn template_literal_text(value: &TemplateText) -> String {
    value
        .0
        .iter()
        .filter_map(|segment| match segment {
            TemplateSegment::Literal(value) => Some(value.as_str()),
            TemplateSegment::Variable(_) => None,
        })
        .collect()
}

fn variables_from_widgets<'a>(
    widgets: &'a [Widget],
    widget_id: &str,
) -> impl Iterator<Item = String> + 'a {
    widgets
        .iter()
        .find(|widget| widget.id.0 == widget_id)
        .and_then(|widget| match &widget.value {
            WidgetValue::Variables(values) => Some(values.keys().cloned().collect::<Vec<_>>()),
            _ => None,
        })
        .unwrap_or_default()
        .into_iter()
}

fn replace_template_literals(previous: TemplateText, raw: &str) -> TemplateText {
    let mut replaced_literal = false;
    let mut segments = previous
        .0
        .into_iter()
        .filter_map(|segment| match segment {
            TemplateSegment::Literal(_) if replaced_literal => None,
            TemplateSegment::Literal(_) => {
                replaced_literal = true;
                (!raw.is_empty()).then(|| TemplateSegment::Literal(raw.into()))
            }
            reference @ TemplateSegment::Variable(_) => Some(reference),
        })
        .collect::<Vec<_>>();
    if !replaced_literal && !raw.is_empty() {
        segments.insert(0, TemplateSegment::Literal(raw.into()));
    }
    TemplateText(segments)
}

fn committed_value(kind: InputValueKind, raw: &str) -> Option<WidgetValue> {
    match kind {
        InputValueKind::Text => Some(WidgetValue::Text(raw.into())),
        InputValueKind::TemplateWithSegments(previous) => Some(WidgetValue::Template(
            replace_template_literals(previous, raw),
        )),
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
    use std::{cell::RefCell, fs, path::PathBuf, rc::Rc, time::SystemTime};

    use athena_application::WidgetValue;
    use athena_domain::{TemplateSegment, TemplateText, VariableReference};
    use gpui::{
        Action as _, AppContext as _, Keystroke, Modifiers, ScrollDelta, ScrollWheelEvent,
        TestAppContext, VisualTestContext, point, px,
    };
    use gpui_component::input::Enter as InputEnter;

    use super::{InputValueKind, NativeShell, committed_value};

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

    #[test]
    fn template_input_keeps_references_structured_and_treats_typed_tokens_as_literal_text() {
        assert_eq!(
            committed_value(
                InputValueKind::TemplateWithSegments(TemplateText(Vec::new())),
                "Feed {project:plant} / {folio:area}",
            ),
            Some(WidgetValue::Template(athena_domain::TemplateText(vec![
                TemplateSegment::Literal("Feed {project:plant} / {folio:area}".into()),
            ])))
        );

        assert_eq!(
            committed_value(
                InputValueKind::TemplateWithSegments(TemplateText(vec![
                    TemplateSegment::Literal("Feed ".into()),
                    TemplateSegment::Variable(VariableReference::Project("plant".into())),
                ])),
                "Main feed",
            ),
            Some(WidgetValue::Template(TemplateText(vec![
                TemplateSegment::Literal("Main feed".into()),
                TemplateSegment::Variable(VariableReference::Project("plant".into())),
            ])))
        );
    }

    #[test]
    fn template_input_keeps_unknown_or_unclosed_tokens_literal() {
        assert_eq!(
            committed_value(
                InputValueKind::TemplateWithSegments(TemplateText(Vec::new())),
                "{system:plant} / {project:} / {folio:area",
            ),
            Some(WidgetValue::Template(athena_domain::TemplateText(vec![
                TemplateSegment::Literal("{system:plant} / {project:} / {folio:area".into(),),
            ])))
        );
    }

    fn redraw(cx: &mut VisualTestContext) {
        cx.update(|window, cx| {
            let _ = window.draw(cx);
        });
    }

    fn click(cx: &mut VisualTestContext, selector: &'static str) {
        redraw(cx);
        let bounds = cx
            .debug_bounds(selector)
            .unwrap_or_else(|| panic!("missing rendered control: {selector}"));
        cx.simulate_click(bounds.center(), Modifiers::none());
    }

    fn enter_text(cx: &mut VisualTestContext, selector: &'static str, value: &str) {
        click(cx, selector);
        cx.simulate_keystrokes("ctrl-a");
        cx.simulate_input(value);
        cx.dispatch_action(InputEnter { secondary: false });
    }

    fn enter_text_without_parking(cx: &mut VisualTestContext, selector: &'static str, value: &str) {
        click(cx, selector);
        for key in std::iter::once("ctrl-a").chain(value.split("")) {
            let keystroke = Keystroke::parse(key).expect("valid test keystroke");
            cx.update(|window, cx| window.dispatch_keystroke(keystroke, cx));
        }
        cx.update(|window, cx| {
            window.dispatch_action(InputEnter { secondary: false }.boxed_clone(), cx)
        });
    }

    fn scroll_properties(cx: &mut VisualTestContext, delta_y: f32) {
        redraw(cx);
        let bounds = cx
            .debug_bounds("properties-scroll")
            .expect("missing rendered properties scroll region");
        for _ in 0..12 {
            cx.simulate_event(ScrollWheelEvent {
                position: bounds.center(),
                delta: ScrollDelta::Pixels(point(px(0.0), px(delta_y))),
                ..Default::default()
            });
        }
    }

    fn temporary_project_path() -> PathBuf {
        let unique = SystemTime::now()
            .duration_since(SystemTime::UNIX_EPOCH)
            .expect("the clock is after the Unix epoch")
            .as_nanos();
        std::env::temp_dir().join(format!(
            "athena-gpui-workflow-{}-{unique}.athena.json",
            std::process::id()
        ))
    }

    #[gpui::test]
    fn gpui_controls_drive_the_m005_workflow_through_ui_events(cx: &mut TestAppContext) {
        cx.update(gpui_component::init);
        let shell_slot = Rc::new(RefCell::new(None));
        let captured_shell = shell_slot.clone();
        let (_, cx) = cx.add_window_view(move |window, cx| {
            let shell = cx.new(|cx| NativeShell::new(window, cx));
            *captured_shell.borrow_mut() = Some(shell.clone());
            gpui_component::Root::new(shell, window, cx)
        });
        let shell = shell_slot
            .borrow_mut()
            .take()
            .expect("test window exposes its NativeShell");

        click(cx, "add-folio");
        click(cx, "add-folio");
        let (project_id, control, io) = shell.read_with(cx, |shell, _| {
            let snapshot = shell.editor.state_snapshot().unwrap();
            (
                snapshot.project.id,
                snapshot.project.folio_order()[1],
                snapshot.project.folio_order()[2],
            )
        });
        let control_selector = Box::leak(format!("folio-{control:?}").into_boxed_str());
        click(cx, control_selector);
        enter_text(cx, "widget-folio-label", "Control");
        let io_selector = Box::leak(format!("folio-{io:?}").into_boxed_str());
        click(cx, io_selector);
        enter_text(cx, "widget-folio-label", "I/O");
        let move_selector = Box::leak(format!("move-up-{io:?}").into_boxed_str());
        click(cx, move_selector);

        click(cx, "project-properties");
        enter_text(cx, "widget-project-variables", "plant=PLANT-A");
        enter_text(cx, "widget-project-variables", "designer=A. Engineer");
        click(cx, io_selector);
        enter_text(cx, "widget-folio-title", "Main control");
        click(cx, "reference-folio-author-project-designer");
        scroll_properties(cx, -80.0);
        enter_text(cx, "widget-folio-location", "MCC-01");
        enter_text(cx, "widget-folio-revision", "A");
        enter_text(cx, "widget-folio-page-number", "2");
        enter_text(cx, "widget-folio-variables", "area=MCC-01");

        let state = shell.read_with(cx, |shell, _| shell.editor.state_snapshot().unwrap());
        let expected_order = state.project.folio_order().to_vec();
        assert_eq!(state.project.folio_order()[1], io);
        assert_eq!(state.project.folio(control).unwrap().label, "Control");
        assert_eq!(state.project.folio(io).unwrap().label, "I/O");
        assert_eq!(state.project.variables.get("plant").unwrap(), "PLANT-A");
        assert_eq!(
            state.project.variables.get("designer").unwrap(),
            "A. Engineer"
        );
        assert_eq!(
            state
                .project
                .folio(io)
                .unwrap()
                .variables
                .get("area")
                .unwrap(),
            "MCC-01"
        );
        let resolved = shell.read_with(cx, |shell, _| {
            shell
                .editor
                .view_state()
                .resolved_title_block(io)
                .unwrap()
                .clone()
        });
        assert_eq!(resolved.title.text, "Main control");
        assert_eq!(resolved.author.text, "A. Engineer");
        assert_eq!(resolved.location.text, "MCC-01");
        assert_eq!(resolved.revision.text, "A");
        assert_eq!(resolved.page_number.text, "2");
        let expected_resolved = resolved;

        let saved_path = temporary_project_path();
        click(cx, "save");
        assert!(cx.did_prompt_for_new_path());
        assert!(shell.read_with(cx, |shell, _| {
            shell.editor.state_snapshot().unwrap().save_pending
        }));
        scroll_properties(cx, 80.0);
        enter_text_without_parking(cx, "widget-folio-label", "I/O pending edit");
        let state = shell.read_with(cx, |shell, _| shell.editor.state_snapshot().unwrap());
        assert!(state.save_pending);
        assert!(state.dirty);
        assert_eq!(state.project.folio(io).unwrap().label, "I/O pending edit");

        let selected_path = saved_path.clone();
        cx.simulate_new_path_selection(move |_| Some(selected_path));
        cx.run_until_parked();
        let state = shell.read_with(cx, |shell, _| shell.editor.state_snapshot().unwrap());
        assert!(!state.save_pending);
        assert!(
            state.dirty,
            "post-request edit stays dirty after save success"
        );
        click(cx, "undo");
        let state = shell.read_with(cx, |shell, _| shell.editor.state_snapshot().unwrap());
        assert!(!state.dirty);
        assert_eq!(state.project.folio(io).unwrap().label, "I/O");

        click(cx, "close");
        assert!(shell.read_with(cx, |shell, _| shell.editor.state_snapshot().is_none()));
        click(cx, "reopen");
        cx.run_until_parked();
        let reopened = shell.read_with(cx, |shell, _| shell.editor.state_snapshot().unwrap());
        assert_eq!(reopened.project.id, project_id);
        assert_eq!(reopened.project.folio_order(), expected_order);
        assert_eq!(reopened.active_folio_id, io);
        assert_eq!(reopened.project.folio(io).unwrap().label, "I/O");
        assert_eq!(reopened.project.variables.get("plant").unwrap(), "PLANT-A");
        assert_eq!(
            reopened.project.variables.get("designer").unwrap(),
            "A. Engineer"
        );
        assert_eq!(
            reopened
                .project
                .folio(io)
                .unwrap()
                .variables
                .get("area")
                .unwrap(),
            "MCC-01"
        );
        assert!(!reopened.dirty);
        assert_eq!(
            shell.read_with(cx, |shell, _| {
                shell
                    .editor
                    .view_state()
                    .resolved_title_block(io)
                    .unwrap()
                    .clone()
            }),
            expected_resolved
        );
        fs::remove_file(saved_path).expect("temporary GPUI workflow project removes");
    }
}
