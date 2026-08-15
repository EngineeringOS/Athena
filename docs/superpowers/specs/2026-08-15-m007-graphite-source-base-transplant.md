# M007 Graphite Source-base Transplant

**Status:** APPROVED by direct user instruction on 2026-08-15.

## Intent

Replace Athena's hand-built shell as the product baseline with the complete
Graphite repository already available at `reference/Graphite`. The first M007
runtime must be Graphite itself, unchanged except for its location in Athena's
repository. Athena electrical behavior is integrated only after this baseline
runs and is captured.

## Source And License Contract

- Source revision: `461ddbc8726c587a8abc536cab301b0b2206a54c`.
- Copy every Graphite file and directory into the Athena repository root except
  the nested `reference/Graphite/.git` administrative directory.
- Preserve `LICENSE.txt`, copyright notices, lockfiles, build tools, frontend,
  editor, desktop, document, libraries, node graph, website, and CI metadata.
- Record a deterministic SHA-256 manifest proving copied files equal the
  reference checkout before any Athena substitution.
- Graphite-derived files are not clean-room and must retain provenance.

## Baseline Contract

1. The imported root workspace builds with Graphite's documented toolchain.
2. The Graphite Web editor starts from the imported root without using Athena's
   existing `web/` shell.
3. The Graphite desktop wrapper starts from the imported root when its platform
   prerequisites are available.
4. A fresh 1440x900 screenshot is visually the original Graphite editor, not an
   Athena approximation.
5. No QElectroTech labels or Athena logic are introduced before baseline proof.

## Replacement Contract

- Existing Athena Rust crates under `rust/` are migration donors only.
- Replace Graphite functions one bounded electrical workflow at a time.
- Each replacement begins with behavior evidence from QElectroTech, preserves
  the Graphite shell and interaction surface, and adds Rust tests plus a visual
  regression where the UI can change.
- Remove a Graphite graphics-domain subsystem only after its Athena replacement
  is connected and verified. Never replace the shell with another custom UI.

## Gates

- `Copy Gate`: reference/import manifest has zero missing or changed files.
- `License Gate`: Graphite Apache-2.0 license and provenance are present.
- `Web Baseline Gate`: imported Graphite Web editor runs and is captured.
- `Desktop Baseline Gate`: imported desktop runs or is recorded `BLOCKED` with
  the exact missing prerequisite; Web success cannot be relabeled desktop pass.
- `User Gate`: the user explicitly confirms the imported baseline looks like
  Graphite before the first electrical-function replacement.

## Non-goals

- Repairing or polishing the M006 custom shell.
- Recreating Graphite components from screenshots or measurements.
- Porting Graphite UI to GPUI during M007.
- Copying QElectroTech, Zed, gpui-component, or OpenCADStudio source.
