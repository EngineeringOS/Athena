---
name: Athena M1
status: final
sources:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-03/prd.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-03/ARCHITECTURE-SPINE.md
  - _bmad-output/planning-artifacts/epics.md
updated: 2026-07-04
---

# Athena M1 - Experience Spine

## Foundation

Desktop-primary engineering workbench with a narrower web companion in the first wave. Athena M1 is not a full editor product yet. It is a runtime-proof shell that lets Maya open a workspace, inspect a project, compile through runtime-owned orchestration, view semantic output, inspect diagnostics and history, and perform one explicit GUI mutation path. `DESIGN.md` is the visual identity reference; this spine owns behavior and shell logic.

The UI system inherits from a Compose-native owned-component posture rather than a heavy framework aesthetic. The shell should feel like an engineering tool in the family of IDEA, VS Code, and ECAD workbenches, but without claiming full IDE feature parity in M1. `[ASSUMPTION]` In this phase, the source pane supports authored source visibility and compile-oriented navigation, while rich IDE behaviors such as advanced autocomplete and broad authoring assistance remain backlog scope.

## Information Architecture

| Surface | Reached from | Purpose |
|---|---|---|
| Welcome / Start | App open (cold) | Recent workspaces, quick orientation, and first-run guidance |
| Workspace Shell | Welcome selection or reopen | The main application frame: menus, toolbar, docked panels, and workbench |
| Workspace tree | Left dock in Workspace Shell | Navigate workspace contents and alternate views such as project, files, or graph |
| Workbench | Center of Workspace Shell | Switch or split between source-oriented and render-oriented views |
| Inspector | Right dock in Workspace Shell | Show properties and selected semantic details |
| Diagnostics / Console | Bottom dock in Workspace Shell | Compile output, lint warnings, runtime feedback, and downstream status |
| History / Diff | Dockable utility panel | Inspect command history, semantic diff, undo, and replay consequences |
| Runtime extension view | Dockable tab or panel in Workspace Shell | Host runtime-backed plugin or secondary surfaces that consume canonical state without owning semantics |

The shell opens into a controlled workbench, not into marketing chrome. Modal stacks stay one level deep. Docking behavior is useful and professional, but M1 should not commit to unconstrained panel choreography yet. The trust signal is that the workspace feels under control immediately.

Mockups: [key-workspace-under-control.html](mockups/key-workspace-under-control.html) illustrates the welcome surface and post-compile split shell; [key-connect-ports.html](mockups/key-connect-ports.html) illustrates command selection, runtime validation, and rejection handling. The spines win on conflict.

## Voice and Tone

Microcopy is direct, technical, and calm. Brand posture lives in `DESIGN.md`.

| Do | Don't |
|---|---|
| `Open workspace` | `Let's get started!` |
| `Compile completed with 1 warning.` | `Build succeeded!` |
| `No active project.` | `Nothing here yet!` |
| `Connect ports` | `Create smart link` |
| `Recent workspaces` | `Continue where you left off` |
| Short, exact engineering language | Cheerleading, hype, or artificial friendliness |

Athena should never sound toy-like, promotional, or unstable. It speaks like a serious tool that expects a serious operator.

## Component Patterns

Behavioral rules only. Visual specs live in `DESIGN.md.Components`.

| Component | Use | Behavioral rules |
|---|---|---|
| Shell top bar | Workspace Shell | Holds menus, workspace context, and trusted global actions such as open workspace and compile. It remains stable while panels and workbench content change. |
| Welcome surface | Cold open | Shows recent workspaces, workspace entry, and concise tips. It gets Maya to an active workspace quickly rather than onboarding her like a consumer app. |
| Workspace tree | Left dock | Supports alternate views of project structure. Selection changes what the workbench and inspector show; tree selection alone does not mutate semantics. |
| Workbench tabs | Center | Switch between open source, render, diff, or related surfaces. Tabs are work surfaces, not browser metaphors. |
| Source pane | Workbench | Supports authored source visibility and compile-oriented navigation in M1. Rich IDE-grade editing assistance is deferred. |
| Render pane | Workbench | Shows the active project through runtime-coordinated rendering. Can sit beside or instead of the source pane. |
| Split view toggle | Workbench | Lets Maya compare source and render horizontally without opening a second workspace context. |
| Inspector | Right dock | Shows selected semantic object details, connection state, and editable property surfaces allowed in this phase. |
| Diagnostics panel | Bottom dock | Lists warnings, lint, and compile consequences. Diagnostics are inspectable and tied to the current runtime state. |
| Console | Bottom dock | Shows compile and runtime output. It is factual output, not chatty status. |
| History / Diff panel | Dockable utility | Shows command history, semantic diffs, undo/redo state, and replay consequences when those features are active. |
| Runtime extension view | Dockable tab or panel | Hosts runtime-backed secondary views or plugin surfaces that consume graph, diagnostics, and render outputs through published runtime contracts only. |

## State Patterns

| State | Surface | Treatment |
|---|---|---|
| Cold open | Welcome / Start | Show recent workspaces, one clear open action, and concise tips. No dashboard clutter. |
| No workspace open | Welcome / Start | `Open a workspace to begin.` Secondary affordance: reopen recent workspace. |
| Workspace active, no project selected | Workspace Shell | Shell remains visible; workbench shows controlled empty state rather than a blank frame. |
| Workspace tree view switched | Workspace tree | The active alternate view is obvious, and changing views updates navigation context without mutating semantics. |
| Compile running | Diagnostics / Console | Show deterministic in-progress status in the console area; do not freeze the shell. |
| Compile success with warnings | Diagnostics / Console | Console reports success; problems panel remains visible with warning count and entries. |
| Compile success without findings | Diagnostics / Console | Show the last successful compile and a quiet `No active diagnostics.` state instead of leaving the panel visually empty. |
| No commands recorded yet | History / Diff | Show `No commands recorded yet.` Undo and replay affordances stay visible but disabled until the first runtime-backed mutation occurs. |
| Render refreshed after change | Workbench | Viewer updates while preserving shell stability; no flashy confirmation animation. |
| GUI `connect ports` selection pending | Workbench + Inspector | Viewer and inspector clearly show the current source and target selection context before the command commits. |
| Invalid connection attempt | Diagnostics + Inspector | Connection is rejected through runtime rules; diagnostics explain why, and canonical state remains unchanged. |
| Undo available | History / Diff | Undo visibility is obvious through command history state rather than hidden in a menu only. |
| Runtime extension active | Runtime extension view | The extension shows runtime-backed state as a derived surface and stays visibly subordinate to canonical shell authority. |
| Runtime extension activation rejected | Runtime extension view + Diagnostics | An extension that attempts to bypass runtime contracts is blocked before activation and the project remains unchanged. |

## Interaction Primitives

M1 interaction should feel professional and familiar, but it must not promise the full behavior surface of a mature IDE.

- Click to select workspace items, semantic objects, tabs, and panels.
- Double-click or explicit surface actions may open source or related details where appropriate.
- Compile is available from the top menu and toolbar.
- Standard undo / redo behaviors are visible and consistent with command history.
- Workbench can switch between source-oriented and render-oriented views, or show them in a horizontal split.
- One explicit GUI mutation path is supported in M1: `connect two existing compatible ports`.
- Panel visibility can be toggled in a controlled way, but arbitrary advanced layout choreography is not required for M1.

`[ASSUMPTION]` Advanced command palette workflows, deep keyboard-chord systems, rich autocomplete, and broad editor-authoring ergonomics are backlog items, even though the shell language should leave room for them later.

## Accessibility Floor

Behavioral floor only. Visual contrast belongs in `DESIGN.md`.

- Keyboard navigation must reach all primary shell surfaces in a sane order.
- Focus must remain visible across menus, toolbar controls, tree rows, tabs, inspector fields, and dock panels.
- The viewer and inspector must expose selection state in ways that do not rely on color alone.
- Diagnostics and console entries must be readable and distinguishable without animation.
- Desktop remains the primary accessibility target in M1, but web must inherit the same shell hierarchy and semantic labeling discipline.

## Inspiration & Anti-patterns

- **Lifted from professional IDEs:** recent workspaces, top menus, docked utility panels, central workbench discipline, and the expectation that the shell feels stable before it feels clever.
- **Lifted from Rive:** the stage-centered relationship between hierarchy, toolbar, and inspector.
- **Lifted from ECAD thinking:** the domain model belongs in the work objects and inspector behavior, not in novelty chrome.
- **Rejected - toy software posture:** no playful badges, no soft-SaaS emptiness, no hype motion, and no "friendly assistant" tone.
- **Rejected - fake power-user complexity:** M1 should not simulate mature IDE breadth through empty affordances or half-working editor promises.
- **Rejected - crowded legacy enterprise feel:** too many simultaneous status surfaces, over-labeled chrome, and visual noise make the tool feel old-fashioned and unstable.

## Responsive & Platform

| Platform | M1 behavior |
|---|---|
| Desktop | Primary surface. Full shell with left tree, central workbench, right inspector, bottom diagnostics, and controlled docking. |
| Web | Narrower first-wave peer. Same shell logic where practical, but with reduced layout complexity and fewer simultaneous panels. |

On web, the first-wave posture is "same shell, fewer capabilities." The main reductions are layout richness and docking freedom, not semantic truth. The canonical runtime path still matters, but the desktop workbench remains the reference surface.

Suggested web reductions for first wave:
- Collapse some docked panels into tabs or drawers
- Prefer view switching over persistent multi-pane density on smaller widths
- Preserve workspace trust, diagnostics, and viewer inspection before chasing desktop parity

## Key Flows

### Flow 1 - Workspace Under Control (Maya, opening Athena to inspect a real project)

Traceability: realizes source UJ-1 runtime-managed project activation and compile orchestration.

Mockup: [key-workspace-under-control.html](mockups/key-workspace-under-control.html) illustrates the welcome surface and the post-compile split shell.

1. Maya opens Athena.
2. She lands on a welcome surface with recent workspaces and concise tips, not a blank frame.
3. She opens her last workspace.
4. The workspace shell appears with tree, workbench, inspector, and diagnostics already framed clearly.
5. She selects the active project and sees the workbench show source on one side and rendered output on the other.
6. She runs compile from the toolbar.
7. **Climax:** The console reports compile completion, warnings appear in diagnostics, and the shell still feels orderly while project activation and compile orchestration remain visibly runtime-owned. Maya trusts that the workspace is under control.

Failure: the workspace opens but no project is active. The shell stays stable, the workbench shows a controlled empty state, and the next required action is obvious.

### Flow 2 - Inspect And Connect Ports (Maya, validating the first GUI mutation path)

Traceability: realizes source UJ-2 command-driven semantic mutation.

Mockup: [key-connect-ports.html](mockups/key-connect-ports.html) illustrates pending selection state and runtime rejection handling for the first GUI mutation path.

1. Maya opens an active project in the workspace shell.
2. She uses the tree and workbench to inspect the relevant source and rendered view.
3. She selects two existing compatible ports through the viewer-oriented surface.
4. The inspector reflects the current selection context before commit.
5. She triggers `connect ports`.
6. Runtime processes the command; the viewer refreshes, diagnostics update, and history records the change.
7. **Climax:** Maya sees the new connection, understands the change through shell surfaces, and can immediately trust undo and diff visibility.

Failure: the ports are incompatible. The shell rejects the action cleanly, diagnostics explain the failure, and no phantom connection appears in the viewer.

### Flow 3 - Review What Changed (Maya, after mutation)

Traceability: extends source UJ-2 into diff, history, and undo inspection.

1. Maya opens the history / diff panel after a command-backed change.
2. She sees which command executed and the semantic consequence tied to it.
3. She triggers undo.
4. The viewer and diagnostics return to the prior state.
5. **Climax:** The shell proves that change in Athena is safe because it is inspectable and reversible, not hidden in UI-local state.

Failure: no runtime-backed command has been recorded yet. Athena shows `No commands recorded yet.`, keeps undo disabled, and makes the next eligible mutation path obvious.

### Flow 4 - Attach A Runtime Surface (Priya, validating the platform extension posture)

Traceability: realizes source UJ-3 runtime-backed surface extension.

1. Priya opens Athena with an active project already loaded through the runtime.
2. She enables a runtime-hosted extension view from the workspace shell.
3. The extension view reads canonical `Engineering IR`, graph queries, diagnostics, and render outputs through published runtime contracts.
4. Maya or Priya triggers compile or inspects an already recorded command-backed change.
5. The extension view refreshes from the same runtime-owned state without introducing a private semantic model.
6. **Climax:** Priya can explain the new surface as `frontend -> runtime -> semantic services`, not as a second semantic authority beside the runtime.

Failure: the extension attempts to bypass runtime contracts or mutate canonical state directly. Athena rejects activation, reports the contract violation in diagnostics, and keeps project state unchanged.

