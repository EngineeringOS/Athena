# M22 ELK Spike Envelope

Decision: isolated experimental adapter.

M22 may evaluate an ELK-style helper only behind Athena's layout optimization contracts. M22 will
not add ELK as a direct renderer dependency, no direct renderer dependency is allowed, and no
frontend runtime dependency is allowed.

Explicit boundary: no frontend runtime dependency.

## Execution Envelope

- local-only execution
- no remote service tier
- no hidden persistence format
- no renderer-owned layout truth
- no frontend-owned layout truth

## Packaging Envelope

The spike belongs behind an experimental adapter boundary. Adapter input must be derived from Athena
layout intent and constraints. Adapter output must be normalized into Athena layout facts before
comparison or rendering.

## Removal Path

The spike must be removable without changing layout facts and removable without changing renderer
contracts. The rule-based Athena optimizer remains the fallback path.

Explicit removal rule: removable without changing renderer contracts.

## Deferred Decision

M22 does not select ELK as final layout architecture, final solver stack, semantic authority, or
persistence format.
