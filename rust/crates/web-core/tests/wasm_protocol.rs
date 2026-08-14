#[cfg(target_arch = "wasm32")]
mod wasm {
    use athena_application::{AthenaMessage, PortfolioMessage};
    use athena_domain::{FolioId, ProjectId};
    use athena_web_core::WasmAthenaEditor;
    use uuid::Uuid;
    use wasm_bindgen_test::*;

    wasm_bindgen_test_configure!(run_in_browser);

    #[wasm_bindgen_test]
    fn wasm_handle_persists_state_across_dispatch_calls() {
        let mut editor = WasmAthenaEditor::new();
        let create =
            serde_json::to_string(&AthenaMessage::Portfolio(PortfolioMessage::CreateProject {
                project_id: ProjectId::from_uuid(Uuid::from_u128(1)),
                initial_folio_id: FolioId::from_uuid(Uuid::from_u128(2)),
                name: "Browser".into(),
            }))
            .unwrap();
        let first = editor.dispatch(&create).unwrap();
        let second = editor
            .dispatch(
                &serde_json::to_string(&AthenaMessage::Portfolio(PortfolioMessage::RequestSave))
                    .unwrap(),
            )
            .unwrap();
        assert!(first.contains("ProjectOpened"));
        assert!(second.contains("SaveRequested"));
    }
}
