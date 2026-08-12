use athena_domain::Point;
use athena_web_core::WebEditorCore;

#[test]
fn project_scene_and_snapshot_round_trip_through_the_web_editor_contract() {
    let mut editor = WebEditorCore::new("Browser project");
    assert!(
        editor
            .render_active_sheet()
            .expect("scene serializes")
            .contains("layers")
    );

    let snapshot = editor.encode_snapshot().expect("project snapshot encodes");
    editor.load_snapshot(&snapshot).expect("snapshot reloads");

    assert!(
        editor
            .render_active_sheet()
            .expect("scene serializes")
            .contains("PageAndGrid")
    );
}

#[test]
fn placement_and_wire_commands_stay_inside_the_wasm_controller() {
    let mut editor = WebEditorCore::new("Browser project");
    editor
        .begin_placement_by_name("Resistor")
        .expect("resistor is available");
    editor.pointer_click(100, 80).expect("first symbol places");
    editor
        .begin_placement_by_name("Resistor")
        .expect("resistor is available");
    editor.pointer_click(200, 80).expect("second symbol places");
    editor
        .apply_command_json(
            &serde_json::to_string(&athena_editor::EditorCommand::CreateWire {
                sheet_id: editor.active_sheet_id(),
                wire: athena_domain::Wire::new(
                    athena_domain::WireEndpoint::Terminal(editor.terminal_ids()[0]),
                    athena_domain::WireEndpoint::Terminal(editor.terminal_ids()[2]),
                    vec![Point::new(120, 80), Point::new(180, 80)],
                ),
            })
            .expect("wire command serializes"),
        )
        .expect("wire command applies");
    assert!(
        editor
            .render_active_sheet()
            .expect("scene serializes")
            .contains("Polyline")
    );
}
