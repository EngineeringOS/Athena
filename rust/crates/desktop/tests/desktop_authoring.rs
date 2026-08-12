use athena_desktop::app::DesktopEditor;
use athena_domain::Point;

#[test]
fn selecting_a_catalog_symbol_and_dropping_it_commits_one_editor_command() {
    let mut editor = DesktopEditor::new("Control cabinet");
    let resistor = editor
        .catalog_symbols()
        .into_iter()
        .find(|symbol| symbol.name == "Resistor")
        .expect("built-in resistor is available");

    editor.begin_placement(resistor.definition_id);
    editor
        .canvas_click(Point::new(100, 80))
        .expect("drop should place the selected symbol");

    assert_eq!(editor.symbol_count(), 1);
    assert_eq!(editor.history_lengths(), (1, 0));
}

#[test]
fn terminal_clicks_commit_a_wire_and_history_can_undo_it() {
    let mut editor = DesktopEditor::new("Control cabinet");
    let resistor = editor
        .catalog_symbols()
        .into_iter()
        .find(|symbol| symbol.name == "Resistor")
        .expect("built-in resistor is available");

    editor.begin_placement(resistor.definition_id);
    editor
        .canvas_click(Point::new(100, 80))
        .expect("first resistor should place");
    editor.begin_placement(resistor.definition_id);
    editor
        .canvas_click(Point::new(200, 80))
        .expect("second resistor should place");
    editor.begin_wiring();
    editor
        .canvas_click(Point::new(120, 80))
        .expect("first terminal starts a wire");
    editor
        .canvas_click(Point::new(180, 80))
        .expect("second terminal completes a wire");

    assert_eq!(editor.wire_count(), 1);
    assert!(editor.undo().expect("wire undo should succeed"));
    assert_eq!(editor.wire_count(), 0);
}

#[test]
fn selected_symbol_can_transform_and_round_trip_through_a_snapshot_file() {
    let mut editor = DesktopEditor::new("Control cabinet");
    let resistor = editor
        .catalog_symbols()
        .into_iter()
        .find(|symbol| symbol.name == "Resistor")
        .expect("built-in resistor is available");
    editor.begin_placement(resistor.definition_id);
    editor
        .canvas_click(Point::new(100, 80))
        .expect("symbol places");
    editor
        .move_selected(Point::new(20, 0))
        .expect("symbol moves");
    editor.rotate_selected(1).expect("symbol rotates");
    editor.mirror_selected().expect("symbol mirrors");

    let path = std::env::temp_dir().join(format!("athena-desktop-{}.json", uuid::Uuid::new_v4()));
    editor.save_to_path(&path).expect("project saves");
    let mut reloaded = DesktopEditor::new("Other project");
    reloaded.load_from_path(&path).expect("project reloads");
    std::fs::remove_file(path).expect("temporary snapshot removes");

    assert_eq!(reloaded.symbol_count(), 1);
}
