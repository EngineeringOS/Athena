# M24 PRD Review Rubric

Use this rubric to review M24 before architecture and story creation.

## Gate Questions

- Does the PRD clearly position M24 as schematic routing fidelity, not generic layout polish?
- Does it address the visible user complaint: wire connections still look like graph edges instead
  of coordinated engineering schematic routes?
- Does it preserve EngineeringOS authority by keeping routing above the renderer?
- Does it introduce routing policy/electrical connection intent instead of only geometry?
- Does it avoid claiming full EPLAN parity?
- Does it explicitly defer cabinet, physical, harness, cable tray, and 3D routing?
- Does it explicitly reject generic graph visualization routing as the M24 architecture?
- Does it require an openable Theia sample project rather than `.mjs`-only proof?

## Architecture Checks

- Route facts must be derived from semantic connection and port identity.
- Terminal anchors must be model/projection facts, not renderer guesses.
- Port sides must come from domain-owned policy, not renderer hardcoding.
- M24 should build Athena route engine v0 before evaluating ELK or external routers.
- The renderer must paint route facts only.
- Route quality fallback must be visible or diagnosable.
- M23 layout block syntax must remain accepted.
- New route syntax, if introduced later, must update ANTLR4 and Tree-sitter together.

## Acceptance Checks

- The M24 sample project opens in Theia.
- Routes attach to terminal anchors, not component centers.
- The M24 proof explicitly compares visible routing behavior against M23.
- Input/output sides are predictable.
- Routes are orthogonal and grid-aligned.
- A terminal-strip route case shows ordered parallel lanes or bundles inspired by
  `../../../draft/screenshort/coffret_cordons_chauffants.png` without claiming full parity.
- Labels and crossings remain readable in the accepted sample.
- Source, Problems, Outline, and Graphical View identity remain coherent.

## Red Flags

- "Make it EPLAN-like" without narrowing to schematic routing.
- Canvas-local route coordinate persistence.
- Renderer fallback to center-to-center graph edges in the accepted proof.
- Renderer-side port-role inference.
- Physical routing hidden under schematic terminology.
- ELK or any external router becoming the architecture.
- Generic graph routing presented as electrical schematic routing intelligence.
- Cabinet-like visual inspiration turning into physical-routing scope.
- M24 docs claiming route editing or route-hint source syntax without parser/compiler/LSP proof.
