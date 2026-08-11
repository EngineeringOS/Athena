# Architecture Divergence Attack - M46

Verdict: pass after dependency-diagram correction.

Attempted incompatible implementations:

- Renderer infers junctions from crossing segments: blocked by AD-11/AD-12.
- Net compiler flattens multi-endpoint Net into binary edges: blocked by AD-1/AD-7.
- UI reconnects by changing Konva endpoint only: blocked by AD-14/AD-15.
- Package Port anchor supplies endpoint semantics: blocked by inherited M45 AD-7 and M46 AD-2/AD-3.
- Route editor persists viewport X/Y: blocked by inherited M45 AD-20 and M46 AD-9/AD-14.
- Old `SceneRoute` remains beside `SceneConnection`: blocked by AD-18.
- Two planners choose different routes: blocked by AD-10 canonical cost/tie-break.
- Connection annotation becomes data authority: blocked by AD-4/AD-13.

No surviving authority or mutation-path hole found.
