use athena_desktop::app::DesktopEditor;
use athena_domain::Point;
use athena_render::DrawPrimitive;

fn desktop_fixture_with_two_symbols_and_one_wire() -> DesktopEditor {
    let mut editor = DesktopEditor::new("Desktop interaction fixture");
    let resistor = editor
        .catalog_symbols()
        .into_iter()
        .find(|symbol| symbol.name == "Resistor")
        .expect("built-in resistor is available");
    editor.begin_placement(resistor.definition_id);
    editor
        .canvas_click(Point::new(100, 80))
        .expect("first symbol places");
    editor.begin_placement(resistor.definition_id);
    editor
        .canvas_click(Point::new(220, 120))
        .expect("second symbol places");
    editor.begin_wiring();
    editor
        .canvas_click(Point::new(120, 80))
        .expect("wire start selects a terminal");
    editor
        .canvas_click(Point::new(200, 120))
        .expect("wire end selects a terminal");
    editor
}

fn desktop_fixture_with_selected_wire() -> DesktopEditor {
    let mut editor = desktop_fixture_with_two_symbols_and_one_wire();
    editor
        .select_wire_for_test(0)
        .expect("fixture contains a wire");
    editor
}

fn desktop_fixture_with_selected_symbol() -> DesktopEditor {
    let mut editor = desktop_fixture_with_two_symbols_and_one_wire();
    editor
        .select_symbol_for_test(0)
        .expect("fixture contains a symbol");
    editor
}

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

#[test]
fn marquee_selects_all_enclosed_schematic_items_in_desktop_shell() {
    let mut editor = desktop_fixture_with_two_symbols_and_one_wire();

    editor
        .pointer_drag_for_test(
            Point::new(80, 60),
            Point::new(260, 140),
            athena_desktop::app::DesktopModifiers::default(),
        )
        .expect("marquee gesture applies through the shared session");

    assert_eq!(editor.selected_count(), 3);
}

#[test]
fn desktop_shell_moves_a_wire_vertex_and_updates_the_scene() {
    let mut editor = desktop_fixture_with_selected_wire();

    editor
        .pointer_down(
            Point::new(200, 80),
            athena_desktop::app::DesktopModifiers::default(),
        )
        .expect("wire vertex drag starts through the desktop pointer adapter");
    editor
        .pointer_move(
            Point::new(160, 120),
            athena_desktop::app::DesktopModifiers::default(),
        )
        .expect("wire vertex drag moves through the desktop pointer adapter");
    editor
        .pointer_up(
            Point::new(160, 120),
            athena_desktop::app::DesktopModifiers::default(),
        )
        .expect("wire vertex drag commits through the desktop pointer adapter");

    assert!(format!("{:?}", editor.scene().expect("active sheet exists")).contains("160"));
}

#[test]
fn inspector_updates_selected_symbol_reference() {
    let mut editor = desktop_fixture_with_selected_symbol();

    editor
        .set_selected_symbol_reference_for_test("K1")
        .expect("selected reference updates");

    assert_eq!(
        editor.selected_symbol_reference_for_test(),
        Some("K1".to_owned())
    );
}

#[test]
fn inspector_updates_symbol_description_wire_label_and_sheet_grid() {
    let mut editor = desktop_fixture_with_selected_symbol();
    editor
        .set_selected_symbol_description_for_test("Motor contactor")
        .expect("selected description updates");
    assert_eq!(
        editor.selected_symbol_description_for_test(),
        Some("Motor contactor".to_owned())
    );

    editor
        .select_wire_for_test(0)
        .expect("fixture contains a wire");
    editor
        .set_selected_wire_label_for_test("W-101")
        .expect("selected wire label updates");
    assert_eq!(
        editor.selected_wire_label_for_test(),
        Some("W-101".to_owned())
    );

    editor
        .set_sheet_properties_for_test("Main control", false, 20)
        .expect("sheet inspector updates");
    assert_eq!(editor.sheet_name_for_test(), "Main control");
    assert_eq!(editor.sheet_grid_for_test(), (false, 20));
    assert!(
        !editor
            .scene()
            .expect("active sheet exists")
            .layers
            .iter()
            .flat_map(|layer| &layer.primitives)
            .any(|primitive| matches!(primitive, DrawPrimitive::Grid { .. }))
    );
}

#[test]
fn desktop_shell_exposes_selected_wire_vertex_insert_and_delete() {
    let mut editor = desktop_fixture_with_selected_wire();
    editor
        .insert_selected_wire_vertex_for_test(0, Point::new(140, 80))
        .expect("selected wire segment accepts an interior vertex");
    editor
        .delete_selected_wire_vertex_for_test(1)
        .expect("selected wire interior vertex deletes");
    assert_eq!(editor.history_lengths(), (5, 0));
}
