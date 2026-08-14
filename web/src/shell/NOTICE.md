# Graphite Shell Adaptation Notice

Files in this directory adapt the shell-only structure and presentation from
Graphite revision `461ddbc8726c587a8abc536cab301b0b2206a54c`:

- `frontend/src/components/window/MainWindow.svelte`
- `frontend/src/components/window/TitleBar.svelte`
- `frontend/src/components/window/StatusBar.svelte`
- `frontend/src/components/window/PanelSubdivision.svelte`
- `frontend/src/components/window/Panel.svelte`
- `frontend/src/components/Editor.svelte`

Graphite is licensed under the Apache License, Version 2.0. The license text is
available at `reference/Graphite/LICENSE.txt`. These files are modified for
Athena and retain only the title/workspace/status hierarchy, recursive splits,
panel tabs, gutters, docking feedback, and measured visual tokens.

Graphite portfolio, document, graph, node, raster/vector, storage, dialog,
branding, and product stores are removed. Athena's Rust `WorkspaceShell` is the
only layout authority, and these Svelte components are presentation adapters.
