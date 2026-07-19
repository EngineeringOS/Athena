# M25 PRD Review Rubric

Use this rubric to review M25 before architecture and story creation.

## Gate Questions

- Does the PRD clearly position M25 as representation and presentation policy fidelity, not generic UI
  polish?
- Does it follow naturally after M24 routing fidelity?
- Does it preserve Athena's meaning-to-symbol direction?
- Does it treat symbol anatomy as a schematic subset of a broader presentation anatomy model?
- Does it explicitly keep Presentation IR between component knowledge and rendering?
- Does it avoid claiming full EPLAN, IEC, or QElectroTech parity?
- Does it require an openable Theia sample project rather than `.mjs`-only proof?
- Does it explicitly exclude deprecated desktop-viewer, Compose, and KMP frontend work?
- Does it avoid new `.athena` syntax unless ANTLR4 and Tree-sitter are upgraded together?
- Does it keep renderer authority paint-only?

## Architecture Checks

- Presentation anatomy must be derived from component knowledge and presentation policy.
- Symbol anatomy must be derived as the electrical schematic subset of presentation anatomy.
- Presentation IR must carry symbol, terminal, label, route, and occurrence facts to Theia.
- Terminal notation must be model/projection facts, not renderer guesses.
- Terminal modeling must keep semantic port, physical terminal, and presentation terminal distinct.
- Device tags, terminal labels, and dynamic text anchors must carry canonical identity where
  applicable.
- Labels must be semantic `LabelFact`-style objects, not raw renderer text.
- M24 route facts must attach to M25 terminal facts without center fallback in the accepted proof.
- QElectroTech should be a reference vocabulary only, not the architecture.
- Generic fallback symbol use must be diagnosable.
- Presentation policy profiles must be small, explicit, and swappable later.
- Theia should consume representation and notation facts; it must not resolve component family semantics on
  its own.

## Acceptance Checks

- The M25 sample project opens in Theia.
- The visible sheet has professional symbol and terminal notation improvement beyond M24.
- PLC/controller, HMI/operator, terminal block, power supply, protection, and load/actuator are
  represented by a small governed subset.
- The mandatory acceptance path proves PLC/controller, terminal block, power supply, and
  load/actuator.
- The active presentation policy profile is `athena-industrial-control-v0`.
- Terminal markers and terminal labels are visible and readable.
- Minimum terminal notation is terminal marker shape plus terminal number.
- Device tags and component labels are anchored predictably.
- Route labels and route attachments remain coherent with M24.
- Source, Outline, Problems, inspector, Graphical View, symbol, terminal, and route identity remain
  coherent for accepted sample interactions.
- Usage documentation explains how to present the proof in the IDE.

## Red Flags

- "Make it EPLAN-like" without narrowing to representation and notation policy foundation.
- Direct QElectroTech import presented as the M25 architecture.
- Renderer-side component-family or terminal-role inference.
- Canvas-local symbol drawing saved as truth.
- Broad IEC library ingestion hidden inside the sample.
- New syntax documented but not accepted by both ANTLR4 and Tree-sitter.
- A sample that only works through scripts, not the Theia IDE.
- Any change to deprecated desktop-viewer, Compose, or KMP frontend modules.
- Generic fallback symbols in the accepted proof.
