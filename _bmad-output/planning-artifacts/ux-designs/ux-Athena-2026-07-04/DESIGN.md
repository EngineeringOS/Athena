---
name: Athena M1
status: final
sources:
  - _bmad-output/planning-artifacts/prds/prd-Athena-2026-07-03/prd.md
  - _bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-03/ARCHITECTURE-SPINE.md
  - _bmad-output/planning-artifacts/epics.md
updated: 2026-07-04
description: Runtime-first engineering workspace for inspecting, compiling, and safely mutating canonical project semantics through a professional shell.
colors:
  surface-base: '#0F1722'
  surface-raised: '#142030'
  surface-panel: '#18263A'
  surface-panel-active: '#203554'
  surface-canvas: '#102031'
  ink-primary: '#D7E2F0'
  ink-secondary: '#93A4BD'
  ink-disabled: '#62748E'
  accent: '#4C8DFF'
  accent-strong: '#8CB8FF'
  success: '#4FBF8F'
  warning: '#F5B545'
  danger: '#E16D6D'
  border-hairline: '#24364F'
  surface-base-light: '#F3F6FB'
  surface-raised-light: '#FFFFFF'
  surface-panel-light: '#EAF0F8'
  surface-panel-active-light: '#DCE6F4'
  surface-canvas-light: '#F5F8FD'
  ink-primary-light: '#1B2636'
  ink-secondary-light: '#50627A'
  ink-disabled-light: '#7A879A'
  accent-light: '#3E6FF4'
  accent-strong-light: '#6A93FF'
  success-light: '#278A64'
  warning-light: '#A56A11'
  danger-light: '#B54D4D'
  border-hairline-light: '#C8D4E6'
typography:
  title:
    fontFamily: 'IBM Plex Sans'
    fontSize: 20px
    fontWeight: '600'
    lineHeight: '1.3'
    letterSpacing: -0.01em
  section:
    fontFamily: 'IBM Plex Sans'
    fontSize: 14px
    fontWeight: '600'
    lineHeight: '1.35'
  body:
    fontFamily: 'IBM Plex Sans'
    fontSize: 13px
    fontWeight: '400'
    lineHeight: '1.45'
  meta:
    fontFamily: 'IBM Plex Sans'
    fontSize: 11px
    fontWeight: '500'
    lineHeight: '1.35'
    letterSpacing: 0.06em
  code:
    fontFamily: 'JetBrains Mono'
    fontSize: 12px
    fontWeight: '400'
    lineHeight: '1.6'
  code-sm:
    fontFamily: 'JetBrains Mono'
    fontSize: 11px
    fontWeight: '400'
    lineHeight: '1.55'
rounded:
  sm: 4px
  md: 8px
  lg: 12px
  full: 9999px
spacing:
  '1': 4px
  '2': 8px
  '3': 12px
  '4': 16px
  '5': 24px
  '6': 32px
  '7': 40px
  gutter-shell: 12px
  gutter-panel: 10px
  gutter-workbench: 16px
components:
  shell-topbar:
    background: '{colors.surface-base}'
    foreground: '{colors.ink-primary}'
    border: '{colors.border-hairline}'
    radius: '{rounded.sm}'
  welcome-surface:
    background: '{colors.surface-raised}'
    foreground: '{colors.ink-primary}'
    border: '{colors.border-hairline}'
    radius: '{rounded.lg}'
  workspace-tree:
    background: '{colors.surface-panel}'
    foreground: '{colors.ink-primary}'
    border: '{colors.border-hairline}'
    radius: '{rounded.md}'
  workbench-tabs:
    background: '{colors.surface-panel}'
    foreground: '{colors.ink-secondary}'
    activeBackground: '{colors.surface-panel-active}'
    activeForeground: '{colors.ink-primary}'
    radius: '{rounded.sm}'
  source-pane:
    background: '{colors.surface-raised}'
    foreground: '{colors.ink-primary}'
    border: '{colors.border-hairline}'
    radius: '{rounded.md}'
  render-pane:
    background: '{colors.surface-canvas}'
    foreground: '{colors.accent}'
    border: '{colors.border-hairline}'
    radius: '{rounded.lg}'
  split-view-toggle:
    background: '{colors.surface-panel-active}'
    foreground: '{colors.ink-primary}'
    radius: '{rounded.sm}'
  inspector:
    background: '{colors.surface-panel}'
    foreground: '{colors.ink-primary}'
    border: '{colors.border-hairline}'
    chipBackground: '{colors.surface-panel-active}'
    chipForeground: '{colors.accent-strong}'
    radius: '{rounded.md}'
  diagnostics-panel:
    background: '{colors.surface-panel}'
    foreground: '{colors.ink-primary}'
    border: '{colors.border-hairline}'
    radius: '{rounded.md}'
  console:
    background: '{colors.surface-base}'
    foreground: '{colors.ink-primary}'
    border: '{colors.border-hairline}'
    fontFamily: '{typography.code.fontFamily}'
    radius: '{rounded.md}'
  history-diff-panel:
    background: '{colors.surface-panel}'
    foreground: '{colors.ink-primary}'
    border: '{colors.border-hairline}'
    radius: '{rounded.md}'
  runtime-extension-view:
    background: '{colors.surface-raised}'
    foreground: '{colors.ink-primary}'
    border: '{colors.border-hairline}'
    accent: '{colors.accent}'
    radius: '{rounded.md}'
---

## Brand & Style

Athena M1 should feel like a professional engineering environment that is calm under pressure. The shell gives Maya the sense that the workspace is under control before she acts: structure is obvious, navigation is legible, status is visible, and nothing feels improvised. The emotional target is not excitement. It is quiet competence, serious engineering control, and the confidence that runtime, viewer, and diagnostics are all accountable surfaces.

The design language follows that posture. Athena is not consumer software, not a flashy motion demo, and not a nostalgic enterprise relic. It takes the owned-component discipline suggested by Compose-native systems such as RikkaUi, the dense pro-desktop seriousness of tools like gpui-component, and the clear hierarchy of editors like Rive, then filters all of that into a narrower runtime-proof shell. M1 should look expensive in the sense that it feels considered, stable, and exacting, never ornamental.

There is one strong default skin with light and dark variants. The product does not begin with multiple skins. The visual system must feel cohesive enough that a later product built on Athena could inherit the same engineering grammar without inheriting every exact panel arrangement.

## Colors

The color system is blueprint-adjacent rather than neon-technical. Blue is used as the signal color for runtime action, selection, and viewer emphasis because it reads as precise and inspectable rather than urgent or playful.

Load-bearing shell combinations target at least WCAG AA contrast for normal text and controls in both light and dark variants, with the compile action, diagnostics, inspector metadata, and console output treated as non-negotiable pairs.

- **Deep Night (`#0F1722`)** is the default shell base in dark mode. It gives the workbench seriousness without dropping into pure black.
- **Panel Blue-Gray (`#18263A`)** and **Panel Active (`#203554`)** create tonal hierarchy between stable chrome and active focus surfaces. These values do most of the structural work.
- **Signal Blue (`#4C8DFF`)** and **Signal Blue Strong (`#8CB8FF`)** are for active runtime intent: compile actions, selected runtime affordances, viewer emphasis, and trusted highlights. They are never used decoratively.
- **Success Green, Warning Amber, and Danger Red** exist for diagnostics, but Athena should avoid bathing the shell in status color. Color is a signal, not a theme.
- **Light mode** keeps the same hierarchy and blue signaling, but shifts to cool mineral surfaces (`#F3F6FB`, `#EAF0F8`, `#DCE6F4`) so it still reads as engineering-grade instead of generic SaaS.

Avoid: candy-bright accents, purple futurism, gamer neon, faux-industrial orange overload, and cheap "dark mode by saturation" tricks. Athena must not look like a toy, a beta experiment, or a dated legacy control panel.

## Typography

Typography should feel engineered, not branded for fashion. **IBM Plex Sans** provides the shell voice: rational, technical, and readable at dense panel sizes. **JetBrains Mono** is reserved for code, diagnostics, console, and any place where Maya expects authored or generated text to behave like an engineering artifact.

The hierarchy is restrained:
- `title` for workspace and welcome-level headings
- `section` for panel titles and stronger labels
- `body` for normal shell content
- `meta` for status lines, tabs, and panel chrome
- `code` and `code-sm` for authored DSL, diagnostics, history records, and machine-shaped output

Avoid oversized hero text, marketing-display typography, or all-caps shouting. Athena is a tool workbench; it earns authority through legibility and exactness.

## Layout & Spacing

The shell should read as dense and controlled, but not cramped. The correct posture is "dense shell, calmer canvas." Docked chrome can be efficient; the central work surface must still breathe enough to support inspection and split views without visual panic.

The spacing scale is tight and regular: `4 / 8 / 12 / 16 / 24 / 32 / 40`. The shell uses `gutter-shell` and `gutter-panel` for compact tool surfaces, while the workbench itself uses `gutter-workbench` so the source and viewer areas do not feel compressed into the frame.

The layout pattern is:
- top menu and toolbar
- left workspace tree and alternate views
- central workbench
- right inspector
- bottom diagnostics and console zone

The Rive-like principle applies: the main stage sits between hierarchy and inspector, with chrome clearly framing the work instead of competing with it.

## Elevation & Depth

Athena should use tonal layering more than shadow. Depth exists to separate work zones, not to create consumer-style card drama.

- Primary separation comes from surface steps: base, raised, panel, and active panel.
- Shadows, when used, should be soft and minimal, mostly for floating overlays or transient surfaces.
- Viewer and render surfaces can use a slightly more recessed treatment than the shell, making the central work area feel anchored.

Avoid thick borders, heavy drop shadows, and exaggerated glass effects. Those make engineering tools feel cheap or unstable.

## Shapes

The shell geometry should be crisp with slight softening. `4px / 8px / 12px` radii are enough to prevent harshness without becoming consumer-rounded. The system should feel machined, not bubbly.

- `sm` for tabs, chips, row selections, and compact controls
- `md` for dock panels and buttons
- `lg` for viewer stage containers and larger framed surfaces
- `full` only for small status chips or pills

Avoid large pill-heavy surfaces or overly sharp zero-radius austerity. Athena should feel modern and precise, not fashionable and not austere-for-its-own-sake.

## Components

- **Shell top bar** uses `{components.shell-topbar}` and carries menus, workspace state, and trusted global actions. It must read as structural chrome rather than promotional header content.
- **Welcome surface** uses `{components.welcome-surface}`. Recent workspaces, first-run tips, and entry actions should feel framed and calm, not dashboard-like.
- **Workspace tree** uses `{components.workspace-tree}`. It inherits the dock language and must stay visually subordinate to the central workbench while remaining easy to scan at dense sizes.
- **Workbench tabs** use `{components.workbench-tabs}`. Active state is a focus cue, not a browser metaphor, and tab chrome should never overpower the source or render content beneath it.
- **Source pane** uses `{components.source-pane}`. The authored surface should feel exact, readable, and stable enough for compile-oriented review without over-signaling full IDE parity.
- **Render pane** uses `{components.render-pane}`. The viewer must look like a trustworthy engineering viewport framed by the shell, not a decorative canvas.
- **Split view toggle** uses `{components.split-view-toggle}`. It should feel like a precise mode control, not a playful segmented control.
- **Inspector** uses `{components.inspector}`. Selected semantic state, properties, and compact chips all belong to one coherent dock grammar.
- **Diagnostics panel** uses `{components.diagnostics-panel}`. Warnings and problems must read as accountable evidence, not ambient noise.
- **Console** uses `{components.console}`. Runtime and compile output should feel machine-shaped and reliable, with monospace reserved for actual output and commands.
- **History / Diff panel** uses `{components.history-diff-panel}`. The visual treatment should support inspectable before-and-after consequences without turning diff review into a second editor product.
- **Runtime extension view** uses `{components.runtime-extension-view}`. Runtime-hosted views should inherit the same shell grammar as first-party panels so extension surfaces feel sanctioned, not bolted on.

Panels and controls should inherit a common design grammar even when their contents differ. Compose-native ownership matters here: the shell should be themable and skinable through tokens, not through one-off hand styling.

## Do's and Don'ts

| Do | Don't |
|---|---|
| Use blue as a precision signal for runtime and viewer emphasis | Use accent color as decoration or ambient theme wash |
| Keep the shell dense and the workbench calmer | Make every panel equally loud |
| Frame the stage between hierarchy and inspector | Let panels visually compete with the central work |
| Make the tool feel stable, deliberate, and expensive | Make it feel flashy, cheap, unstable, or toy-like |
| Use monospace only where text is authored, computed, or diagnostic | Put monospace everywhere as a "developer aesthetic" gimmick |
| Maintain one strong skin with light and dark variants | Start with many skins or ad hoc visual modes |

