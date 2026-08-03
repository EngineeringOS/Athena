# Tech / Reality-Check Review - Athena M40 Architecture Spine

Run: 2026-08-02
Lens: verify every committed decision was reality-checked or web-researched, not asserted from
training data; confirm named technology still exists and fits.

## Verdict

Pass. M40 commits **no new technology**. Every named version was verified in the repository and
confirmed current on the web at review time. Nothing in the spine leans on training-data
versions or phantom libraries.

## Version verification

| Technology | Spine / repo claim | Repo evidence | Web evidence |
| --- | --- | --- | --- |
| Theia | 1.73.1 (IDE shell) | `ide/theia-frontend/package.json`, `ide/theia-product/package.json` (`@theia/* 1.73.1`) | npm latest is 1.73.1; GitHub tag `v1.73.1` (2026-06-30) |
| Electron | 39.8.7 | `ide/theia-product/package.json` devDependencies | Electron v39 stable line, release 39.8.7 (2026-04-07/08) |
| Kotlin | current repo toolchain | `gradle/libs.versions.toml` `kotlin = "2.4.0"` | Kotlin 2.4.0 current release (2026-06-03) |
| Gradle | 9.6.1 | `gradle/wrapper/gradle-wrapper.properties` | Gradle 9.6.1 patch release (2026-06-26) |
| TypeScript | 5.9 | `ide/package.json`, `integrations/graph-glsp/package.json` (`^5.9.2`) | 5.9 is the current stable line |
| Node / Yarn | >=22 / 1.22 | `ide/package.json` engines + `packageManager: yarn@1.22.22` | Yarn classic 1.22 line, Node >=22 current |
| graph-glsp | translation-only GLSP adapter | `integrations/graph-glsp/package.json`, README | No new dependency; adapter renders Presentation facts only |
| QElectroTech reference | composition target image | `draft/screenshort/equipement_d'un_volet_roulant.png` (1050x720), README marks gallery set as renderer-layer references | gallery.qelectrotech.org source of the image; reference only, no runtime dependency |

## Fit assessment

- M40 composes facts inside the existing kernel (`projection-model`, `spatial-model`,
  `presentation-model`, `compiler`) and paints through the existing Theia/GLSP/SVG surfaces. The
  spine's AD-12/AD-7 paint-only boundary is consistent with the GLSP adapter's translation-only
  role.
- Theia 1.73.1 + Electron 39.8.7 are a coherent current desktop pair and match what the repo
  already builds and verifies in M39.
- The QElectroTech PNG is used only as a structural composition target (rails, rungs, grouped
  logic, terminal strips), never as runtime authority or a rendering input - consistent with the
  repo's "SVG/reference must not own engineering facts" invariant.

## Findings

- **[low]** The spine has no Stack section (it mirrors the M39 spine, which has none). All pinned
  versions live in repo files (`gradle-wrapper.properties`, `gradle/libs.versions.toml`,
  `package.json`). Acceptable for this build-substrate at feature altitude; add a Stack section
  only if a later milestone needs versions pinned inside the spine itself.
- **[info]** `web-tree-sitter ^0.26.0` and other transitive IDE deps are not named in the spine;
  they are not stack-defining at this altitude and are unchanged by M40. No action.
- **[info]** The M40 spine names no new vendor, planner, renderer, or framework; the human-first
  and open-source-contract invariants are unaffected.

## Open items

None from this lens. All stack claims are repo-verified and web-confirmed; the disposition of
`:kernel:drawing-composition` (AD-18) is an ownership decision, not a technology decision.

## Cross-artifact note (added 09:12)

The M40 PRD was renumbered after this review started: FR-1..FR-19 are now strictly sequential and
"one composition authority" moved from FR-19 to FR-6. The spine's `AD` bindings were written
against the old numbering and must be re-checked before finalize:

- AD-9 `Binds: FR-1, FR-2, FR-5, FR-19` - FR-19 is now "Honest M40 Closure"; composition authority
  is FR-6.
- AD-11 `Binds: FR-9, FR-12` - now FR-10, FR-13.
- AD-12 `Binds: FR-14, FR-15` - now FR-15, FR-16.
- AD-14 `Binds: FR-12, FR-13` - now FR-13, FR-14.
- AD-16 `Binds: FR-11` - now FR-12.
- AD-17 `Binds: FR-16 through FR-18` - now FR-17 through FR-19.
- AD-18 `Binds: FR-19` - now FR-6.

AD-10 (FR-2), AD-13 (FR-1..FR-5), AD-15 (FR-5) remain correct.
