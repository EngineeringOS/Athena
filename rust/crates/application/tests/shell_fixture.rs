use athena_application::{
    AthenaEditor, AthenaFrontendMessage, AthenaMessage, ShellEffect, ShellMessage,
    canonical_m006_shell_messages,
};

#[test]
fn canonical_m006_fixture_is_complete_deterministic_and_valid() {
    let first = canonical_m006_shell_messages();
    let second = canonical_m006_shell_messages();

    assert_eq!(first, second);
    assert!(matches!(
        first.first(),
        Some(AthenaMessage::Shell(ShellMessage::Request))
    ));
    assert!(first.iter().any(|message| matches!(
        message,
        athena_application::AthenaMessage::Shell(ShellMessage::SplitGroup { .. })
    )));
    assert!(first.iter().any(|message| matches!(
        message,
        athena_application::AthenaMessage::Shell(ShellMessage::AbortResize)
    )));
    assert!(first.iter().any(|message| matches!(
        message,
        athena_application::AthenaMessage::Shell(ShellMessage::SetDockPreview { target: Some(_) })
    )));

    let mut editor = AthenaEditor::default();
    let effects = first
        .into_iter()
        .flat_map(|message| editor.handle_message(message))
        .collect::<Vec<_>>();

    assert_eq!(effects.len(), 16);
    assert!(effects.iter().all(|effect| matches!(
        effect,
        AthenaFrontendMessage::Shell(
            ShellEffect::Replaced(_)
                | ShellEffect::ValuesChanged { .. }
                | ShellEffect::DockPreview(_)
        )
    )));
    editor
        .shell_snapshot()
        .validate()
        .expect("canonical fixture preserves a valid recursive shell");
}
