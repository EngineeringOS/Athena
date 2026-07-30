# `:kernel:representation-model`

The representation model owns typed symbols, elements, anchors, graphic primitives, binding
results, and renderer-neutral drawing evidence.

## Boundaries

- Symbols and elements describe reusable visual material, not project engineering instances.
- Anchors bind typed semantic ports to geometry references; SVG does not define port meaning.
- Production contracts use responsibility-based names. Milestone demo and proof helpers belong in
  `src/test` only.
- Renderers consume graphic primitives and never infer engineering truth.

## Verification

```powershell
.\gradlew.bat --no-daemon --console=plain :kernel:representation-model:test
```
