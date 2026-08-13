# M002 Retrospective

Date: 2026-08-13

## What Worked

- Keeping `EditorSession` as the only shell-facing mutation boundary prevented
  desktop and browser behavior from drifting while pointer interactions grew.
- Direct QElectroTech and Graphite source review produced concrete invariants:
  orthogonal wire routes, directional marquee semantics, handle hit priority,
  and transient overlays outside persistence.
- Focused red-green contracts caught two real gaps before closeout: native
  pointer events were click-only, and the web controller duplicated editor
  state instead of using the shared session.

## What Changed During Execution

- The original plan assumed wire insert/delete/reconnect commands were already
  shell-reachable. They were only core commands, so Task 6 was expanded to
  expose those operations through both adapters before claiming parity.
- Browser smoke initially failed because the test dependency and root WASM
  output directory were not present. Adding a minimal Playwright manifest and
  building to `web/pkg` made the check reproducible.

## Follow-Up Discipline

- Keep every new shell action as a thin adapter over `EditorSession`.
- Add a failing cross-shell contract before exposing the next electrical
  editing behavior.
- Keep milestone plans honest: acceptance checkboxes are updated only after
  fresh command evidence and the corresponding commit exists.
