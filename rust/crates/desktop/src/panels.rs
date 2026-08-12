use gpui::{
    App, Application, Context, InteractiveElement, IntoElement, MouseButton, MouseDownEvent,
    Render, Window, WindowOptions, div, prelude::*, px, rgb,
};
use gpui_component::{PixelsExt, Root, Sizable, button::Button, scroll::ScrollableElement};

use crate::{app::DesktopEditor, canvas::render_scene};

pub struct NativeShell {
    editor: DesktopEditor,
}

impl NativeShell {
    fn new() -> Self {
        Self {
            editor: DesktopEditor::new("Untitled electrical project"),
        }
    }

    fn canvas_click(
        &mut self,
        event: &MouseDownEvent,
        _window: &mut Window,
        cx: &mut Context<Self>,
    ) {
        // GPUI reports window coordinates. The canvas starts after the top bar,
        // library panel, and its 12px padding; convert back through the scene
        // renderer's world transform before handing the point to the editor.
        const CANVAS_LEFT: f32 = 208.0 + 12.0;
        const CANVAS_TOP: f32 = 40.0 + 12.0;
        let point = athena_domain::Point::new(
            (event.position.x.as_f32() - CANVAS_LEFT - 24.0).round() as i64,
            (event.position.y.as_f32() - CANVAS_TOP - 24.0).round() as i64,
        );
        let _ = self
            .editor
            .canvas_click(self.editor.canvas_point_to_world(point));
        cx.notify();
    }
}

impl Render for NativeShell {
    fn render(&mut self, _window: &mut Window, cx: &mut Context<Self>) -> impl IntoElement {
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
            "{}   |   symbols: {}   |   undo: {} redo: {}",
            self.editor.project_name(),
            self.editor.symbol_count(),
            self.editor.history_lengths().0,
            self.editor.history_lengths().1
        );

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
                    .child("File")
                    .child("Edit")
                    .child("View")
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
                            .p_4()
                            .text_color(rgb(0x334155))
                            .on_mouse_down(MouseButton::Left, cx.listener(Self::canvas_click))
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
                            .child("Select an item to inspect its electrical properties."),
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
            let view = cx.new(|_| NativeShell::new());
            cx.new(|cx| Root::new(view, window, cx))
        });
        cx.activate(true);
    });
}
