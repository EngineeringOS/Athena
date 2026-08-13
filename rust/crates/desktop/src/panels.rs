//! GPUI workbench composition for the electrical schematic desktop shell.
//!
//! Rendering and controls here delegate all schematic behavior to `DesktopEditor`
//! and its shared editor session.

use gpui::{
    App, Application, Context, Entity, InteractiveElement, IntoElement, MouseButton,
    MouseDownEvent, MouseMoveEvent, MouseUpEvent, Render, Subscription, Window, WindowOptions, div,
    prelude::*, px, rgb,
};
use gpui_component::{
    PixelsExt, Root, Sizable,
    button::Button,
    input::{Input, InputEvent, InputState},
    scroll::ScrollableElement,
};

use crate::{app::DesktopEditor, canvas::render_scene};

pub struct NativeShell {
    editor: DesktopEditor,
    reference_input: Entity<InputState>,
    description_input: Entity<InputState>,
    wire_label_input: Entity<InputState>,
    sheet_name_input: Entity<InputState>,
    grid_spacing_input: Entity<InputState>,
    grid_visible: bool,
    canvas_gesture_active: bool,
    synchronizing_inspector: bool,
    _reference_subscription: Subscription,
    _description_subscription: Subscription,
    _wire_label_subscription: Subscription,
    _sheet_name_subscription: Subscription,
    _grid_spacing_subscription: Subscription,
}

impl NativeShell {
    fn new(window: &mut Window, cx: &mut Context<Self>) -> Self {
        let reference_input = cx.new(|cx| InputState::new(window, cx).placeholder("Reference"));
        let reference_subscription = cx.subscribe(&reference_input, |this, input, event, cx| {
            if !this.synchronizing_inspector && matches!(event, InputEvent::Change) {
                let _ = this
                    .editor
                    .set_selected_symbol_reference(&input.read(cx).value());
                cx.notify();
            }
        });
        let description_input = cx.new(|cx| InputState::new(window, cx).placeholder("Description"));
        let description_subscription =
            cx.subscribe(&description_input, |this, input, event, cx| {
                if !this.synchronizing_inspector && matches!(event, InputEvent::Change) {
                    let _ = this
                        .editor
                        .set_selected_symbol_description(&input.read(cx).value());
                    cx.notify();
                }
            });
        let wire_label_input = cx.new(|cx| InputState::new(window, cx).placeholder("Wire label"));
        let wire_label_subscription = cx.subscribe(&wire_label_input, |this, input, event, cx| {
            if !this.synchronizing_inspector && matches!(event, InputEvent::Change) {
                let _ = this.editor.set_selected_wire_label(&input.read(cx).value());
                cx.notify();
            }
        });
        let sheet_name_input = cx.new(|cx| InputState::new(window, cx).default_value("Sheet 1"));
        let sheet_name_subscription = cx.subscribe(&sheet_name_input, |this, input, event, cx| {
            if !this.synchronizing_inspector && matches!(event, InputEvent::Change) {
                let (_, spacing) = this.editor.sheet_grid_for_test();
                let _ = this.editor.set_sheet_properties(
                    &input.read(cx).value(),
                    this.grid_visible,
                    spacing,
                );
                cx.notify();
            }
        });
        let grid_spacing_input = cx.new(|cx| InputState::new(window, cx).default_value("10"));
        let grid_spacing_subscription =
            cx.subscribe(&grid_spacing_input, |this, input, event, cx| {
                if !this.synchronizing_inspector
                    && matches!(event, InputEvent::Change)
                    && let Ok(spacing) = input.read(cx).value().parse::<i64>()
                {
                    let _ = this.editor.set_sheet_properties(
                        &this.editor.sheet_name_for_test(),
                        this.grid_visible,
                        spacing,
                    );
                    cx.notify();
                }
            });
        Self {
            editor: DesktopEditor::new("Untitled electrical project"),
            reference_input,
            description_input,
            wire_label_input,
            sheet_name_input,
            grid_spacing_input,
            grid_visible: true,
            canvas_gesture_active: false,
            synchronizing_inspector: false,
            _reference_subscription: reference_subscription,
            _description_subscription: description_subscription,
            _wire_label_subscription: wire_label_subscription,
            _sheet_name_subscription: sheet_name_subscription,
            _grid_spacing_subscription: grid_spacing_subscription,
        }
    }

    fn canvas_point(position: gpui::Point<gpui::Pixels>) -> athena_domain::Point {
        // GPUI pointer positions are window-relative. The canvas host starts
        // after the fixed 208px library and 40px toolbar; its 1px border and
        // the renderer's 24px scene inset are the only local offsets.
        const CANVAS_LEFT: f32 = 208.0 + 1.0 + 24.0;
        const CANVAS_TOP: f32 = 40.0 + 1.0 + 24.0;
        athena_domain::Point::new(
            (position.x.as_f32() - CANVAS_LEFT).round() as i64,
            (position.y.as_f32() - CANVAS_TOP).round() as i64,
        )
    }

    fn modifiers(modifiers: gpui::Modifiers) -> crate::app::DesktopModifiers {
        crate::app::DesktopModifiers {
            shift: modifiers.shift,
            command: modifiers.secondary(),
        }
    }

    fn canvas_mouse_down(
        &mut self,
        event: &MouseDownEvent,
        _window: &mut Window,
        cx: &mut Context<Self>,
    ) {
        let point = Self::canvas_point(event.position);
        match self.editor.active_tool() {
            crate::input::ActiveTool::Select | crate::input::ActiveTool::Pan => {
                self.canvas_gesture_active = self
                    .editor
                    .pointer_down(point, Self::modifiers(event.modifiers))
                    .is_ok();
            }
            crate::input::ActiveTool::PlaceSymbol(_) | crate::input::ActiveTool::Wire { .. } => {
                let world = self.editor.canvas_point_to_world(point);
                let _ = self.editor.canvas_click(world);
            }
        }
        cx.notify();
    }

    fn canvas_mouse_move(
        &mut self,
        event: &MouseMoveEvent,
        _window: &mut Window,
        cx: &mut Context<Self>,
    ) {
        if self.canvas_gesture_active && event.dragging() {
            let _ = self.editor.pointer_move(
                Self::canvas_point(event.position),
                Self::modifiers(event.modifiers),
            );
            cx.notify();
        }
    }

    fn canvas_mouse_up(
        &mut self,
        event: &MouseUpEvent,
        _window: &mut Window,
        cx: &mut Context<Self>,
    ) {
        if self.canvas_gesture_active {
            let _ = self.editor.pointer_up(
                Self::canvas_point(event.position),
                Self::modifiers(event.modifiers),
            );
            self.canvas_gesture_active = false;
        }
        cx.notify();
    }

    fn synchronize_input(
        input: &Entity<InputState>,
        value: &str,
        window: &mut Window,
        cx: &mut Context<Self>,
    ) {
        if input.read(cx).value().as_ref() != value {
            input.update(cx, |input, cx| {
                input.set_value(value.to_owned(), window, cx)
            });
        }
    }

    fn synchronize_inspector(&mut self, window: &mut Window, cx: &mut Context<Self>) {
        self.synchronizing_inspector = true;
        Self::synchronize_input(
            &self.reference_input,
            self.editor
                .selected_symbol_reference_for_test()
                .as_deref()
                .unwrap_or_default(),
            window,
            cx,
        );
        Self::synchronize_input(
            &self.description_input,
            self.editor
                .selected_symbol_description_for_test()
                .as_deref()
                .unwrap_or_default(),
            window,
            cx,
        );
        Self::synchronize_input(
            &self.wire_label_input,
            self.editor
                .selected_wire_label_for_test()
                .as_deref()
                .unwrap_or_default(),
            window,
            cx,
        );
        Self::synchronize_input(
            &self.sheet_name_input,
            &self.editor.sheet_name_for_test(),
            window,
            cx,
        );
        let (_, spacing) = self.editor.sheet_grid_for_test();
        Self::synchronize_input(&self.grid_spacing_input, &spacing.to_string(), window, cx);
        self.synchronizing_inspector = false;
    }
}

impl Render for NativeShell {
    fn render(&mut self, window: &mut Window, cx: &mut Context<Self>) -> impl IntoElement {
        self.synchronize_inspector(window, cx);
        let symbol_buttons = self
            .editor
            .catalog_symbols()
            .into_iter()
            .map(|symbol| {
                let definition_id = symbol.definition_id;
                Button::new(("place", definition_id.as_uuid().as_u128() as u64))
                    .small()
                    .label(symbol.name)
                    .on_click(cx.listener(move |this, _, _, cx| {
                        this.editor.begin_placement(definition_id);
                        cx.notify();
                    }))
            })
            .collect::<Vec<_>>();
        let scene = self.editor.scene();
        let status = format!(
            "{}   |   symbols: {}   |   wires: {}   |   selected: {}   |   undo: {} redo: {}",
            self.editor.project_name(),
            self.editor.symbol_count(),
            self.editor.wire_count(),
            self.editor.selected_count(),
            self.editor.history_lengths().0,
            self.editor.history_lengths().1
        );
        let selected_reference = self.editor.selected_symbol_reference_for_test();
        let inspector_summary = match selected_reference {
            Some(reference) => format!("Selected symbol: {reference}"),
            None => "Select a symbol to edit its reference".to_owned(),
        };
        let (grid_visible, _) = self.editor.sheet_grid_for_test();
        self.grid_visible = grid_visible;

        div()
            .size_full()
            .flex()
            .flex_col()
            .bg(rgb(0xf1f5f9))
            .text_color(rgb(0x0f172a))
            .child(
                div()
                    .h(px(40.0))
                    .flex()
                    .items_center()
                    .gap_2()
                    .px_3()
                    .bg(rgb(0x0f172a))
                    .text_color(rgb(0xf8fafc))
                    .child("Athena Electrical")
                    .child("Project")
                    .child("Sheet 1")
                    .child(
                        Button::new("wire-tool")
                            .small()
                            .label("Wire")
                            .on_click(cx.listener(|this, _, _, cx| {
                                this.editor.begin_wiring();
                                cx.notify();
                            })),
                    )
                    .child(
                        Button::new("undo")
                            .small()
                            .label("Undo")
                            .on_click(cx.listener(|this, _, _, cx| {
                                let _ = this.editor.undo();
                                cx.notify();
                            })),
                    )
                    .child(
                        Button::new("redo")
                            .small()
                            .label("Redo")
                            .on_click(cx.listener(|this, _, _, cx| {
                                let _ = this.editor.redo();
                                cx.notify();
                            })),
                    )
                    .child(
                        Button::new("rotate")
                            .small()
                            .label("Rotate")
                            .on_click(cx.listener(|this, _, _, cx| {
                                let _ = this.editor.rotate_selected(1);
                                cx.notify();
                            })),
                    )
                    .child(
                        Button::new("mirror")
                            .small()
                            .label("Mirror")
                            .on_click(cx.listener(|this, _, _, cx| {
                                let _ = this.editor.mirror_selected();
                                cx.notify();
                            })),
                    )
                    .child(
                        Button::new("delete")
                            .small()
                            .label("Delete")
                            .on_click(cx.listener(|this, _, _, cx| {
                                let _ = this.editor.delete_selected();
                                cx.notify();
                            })),
                    )
                    .child(
                        Button::new("zoom-in")
                            .small()
                            .label("+")
                            .on_click(cx.listener(|this, _, _, cx| {
                                this.editor.zoom(1.2);
                                cx.notify();
                            })),
                    )
                    .child(
                        Button::new("zoom-out")
                            .small()
                            .label("-")
                            .on_click(cx.listener(|this, _, _, cx| {
                                this.editor.zoom(0.8);
                                cx.notify();
                            })),
                    )
                    .child(
                        Button::new("save")
                            .small()
                            .label("Save")
                            .on_click(cx.listener(|this, _, _, cx| {
                                let _ = this.editor.save_to_path("athena-project.json");
                                cx.notify();
                            })),
                    )
                    .child(
                        Button::new("open")
                            .small()
                            .label("Open")
                            .on_click(cx.listener(|this, _, _, cx| {
                                let _ = this.editor.load_from_path("athena-project.json");
                                cx.notify();
                            })),
                    ),
            )
            .child(
                div()
                    .flex_1()
                    .min_h(px(0.0))
                    .flex()
                    .child(
                        div()
                            .w(px(208.0))
                            .h_full()
                            .overflow_y_scrollbar()
                            .p_3()
                            .flex()
                            .flex_col()
                            .gap_2()
                            .bg(rgb(0xffffff))
                            .border_r_1()
                            .border_color(rgb(0xcbd5e1))
                            .child("Symbols")
                            .child("Library")
                            .children(symbol_buttons),
                    )
                    .child(
                        div()
                            .id("schematic-canvas")
                            .flex_1()
                            .h_full()
                            .min_w(px(360.0))
                            .bg(rgb(0xf8fafc))
                            .border_1()
                            .border_color(rgb(0xcbd5e1))
                            .text_color(rgb(0x334155))
                            .on_mouse_down(MouseButton::Left, cx.listener(Self::canvas_mouse_down))
                            .on_mouse_move(cx.listener(Self::canvas_mouse_move))
                            .on_mouse_up(MouseButton::Left, cx.listener(Self::canvas_mouse_up))
                            .on_mouse_up_out(MouseButton::Left, cx.listener(Self::canvas_mouse_up))
                            .child(render_scene(scene)),
                    )
                    .child(
                        div()
                            .w(px(224.0))
                            .h_full()
                            .p_3()
                            .flex()
                            .flex_col()
                            .gap_2()
                            .bg(rgb(0xffffff))
                            .border_l_1()
                            .border_color(rgb(0xcbd5e1))
                            .child("Inspector")
                            .child("Electrical properties")
                            .child(inspector_summary)
                            .child("Reference")
                            .child(Input::new(&self.reference_input).w_full().small())
                            .child("Description")
                            .child(Input::new(&self.description_input).w_full().small())
                            .child("Wire label")
                            .child(Input::new(&self.wire_label_input).w_full().small())
                            .child("Sheet")
                            .child(Input::new(&self.sheet_name_input).w_full().small())
                            .child("Grid spacing")
                            .child(Input::new(&self.grid_spacing_input).w_full().small())
                            .child(
                                Button::new("toggle-grid")
                                    .small()
                                    .label(if grid_visible {
                                        "Hide grid"
                                    } else {
                                        "Show grid"
                                    })
                                    .on_click(cx.listener(|this, _, _, cx| {
                                        let (_, spacing) = this.editor.sheet_grid_for_test();
                                        this.grid_visible = !this.grid_visible;
                                        let _ = this.editor.set_sheet_properties(
                                            &this.editor.sheet_name_for_test(),
                                            this.grid_visible,
                                            spacing,
                                        );
                                        cx.notify();
                                    })),
                            ),
                    ),
            )
            .child(
                div()
                    .h(px(28.0))
                    .px_3()
                    .flex()
                    .items_center()
                    .bg(rgb(0xe2e8f0))
                    .text_sm()
                    .child(status),
            )
    }
}

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
