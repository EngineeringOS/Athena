# M34 Retrospective And M35 Handoff

## Status

M34 restored the correct architecture boundary: Athena source is the only metadata authority for
symbols, elements, profiles, bindings, anchors, compatibility, and package-local governed resources.
XML is not part of the active Cabinet runtime authority path.

Fresh Electron E2E evidence now proves the M34 sample project opens in the Athena IDE and renders the
Cabinet product surface through the compiled M34 path:

- command: `yarn --cwd ide workspace @engineeringood/athena-theia-product start:smoke:m34`
- result: pass on 2026-07-25
- desktop screenshot: `_bmad-output/implementation-artifacts/m34/screenshots/m34-cabinet-product-smoke-desktop.png`
- narrow screenshot: `_bmad-output/implementation-artifacts/m34/screenshots/m34-cabinet-product-smoke-narrow.png`
- LSP evidence: `Published 0 diagnostic(s)` for `examples/m34/sample-project/src/01-native-cabinet-proof.athena`
- active product surface: `cabinet`
- visible product surfaces: `cabinet` only
- drawing layer item count: 59
- route count: 7
- route/body intersections: 0
- fallback representation ids: 0
- XML runtime authority: absent
- governed Athena representation sources: present
- governed package-local SVG resources: present

## What M34 Fixed

- Replaced M33 XML/runtime authority with type-checked Athena representation source.
- Kept SVG as package-local governed geometry input, not metadata authority.
- Added native symbols, elements, profiles, and binding rules to the sample package.
- Connected the live Cabinet presentation path to M34 package-backed representation facts.
- Connected the live Cabinet drawing composition path to M34 package-backed facts.
- Fixed the repository contract so generated `.athena/snapshots` state is ignored by source discovery.
- Removed the Workspace Trust modal from the product smoke path by disabling workspace trust in the
  Athena product defaults.
- Fixed the M34 sample ordering/layout hints so the E2E route proof no longer has a conductor crossing
  the shutter motor body.

## Target Image Gap

The M34 E2E screenshot does not 100% match
`draft/screenshort/equipement_d'un_volet_roulant.png`.

This is not a screenshot/mock problem. The remaining gap is architectural and product-level:

- `LayoutIrDeriver` and `GeometryIrDeriver` still mostly arrange structural/Cabinet components by
  semantic source order instead of solving authored `layout cabinet { place/below/align/group }`
  intent into a physical two-dimensional Cabinet drawing.
- Cabinet enclosure, rails, ducts, mounted components, terminals, and routes are still rendered as
  peers in one long horizontal drawing instead of as nested/contained physical occurrences.
- The renderer has enough typed facts to prove authority, but the Cabinet composition model still
  lacks a true physical mounting layout compiler.
- The native symbol pack is sufficient for authority proof, not yet sufficient for QET-level visual
  fidelity.
- The current scale/viewBox produces a very wide shallow canvas; it does not yet produce a
  professional customer-demo sheet composition.

## Required Follow-Up

M35 should not add more parallel views. It should finish the Cabinet physical drawing model:

- make authored Cabinet layout intent a first-class input to the Cabinet geometry/composition solver;
- model enclosure, rail, duct, terminal strip, and mounted element containment explicitly;
- route conductors through route channels and terminal anchors instead of generic mid-point routes;
- add a target-image visual checklist with hard screenshot assertions for scale, aspect ratio,
  enclosure containment, rail placement, duct placement, terminal density, and label readability;
- keep Athena source and compiled typed facts as the single metadata authority.

M35 also needs one package/representation refactor before the system scales to IEC libraries and
vendor/user elements:

- source package layout must match filesystem hierarchy, so governed `.athena` files follow the same
  package discipline as Java sources;
- complex vendor elements need coverage where `.athena` source references package-local SVG geometry
  while Athena remains the sole metadata authority for anchors, labels, compatibility, identity,
  direction, signal, role, version, policy, and binding;
- third-party representation packages need dependency-style resource resolution from their owning
  package directory, not fragile workspace-relative paths;
- rendered graphic occurrences must be editable back to governed `.athena` source through mutation,
  compile, lint, and verification, with no direct renderer-side metadata authority.

## Final Verdict

M34 is ready for review as an architecture recovery and E2E authority proof milestone.

It is not yet a 100% visual match to the QElectroTech target image. That must be treated as the next
Cabinet-layout milestone, not hidden behind the passing structural smoke proof.
