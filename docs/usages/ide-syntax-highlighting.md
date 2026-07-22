# Athena IDE Syntax Highlighting

Athena owns `.athena` syntax token colors in the frontend language bridge, not in user preferences.

To customize the colors, edit these constants:

- `ATHENA_LIGHT_TOKEN_COLORS`
- `ATHENA_DARK_TOKEN_COLORS`

They live in:

```text
ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts
```

Each palette has these entries:

```typescript
declaration          // package, import, system, device, port, type, model
port                 // direction, signal, in, out
relationship         // connect
layout               // layout, place, align, group, near, below, axis, vertical, horizontal
layoutOperator       // aligned-with, grouped-with
relationshipOperator // ->
```

Use six-digit hex colors without `#`, for example:

```typescript
const ATHENA_LIGHT_TOKEN_COLORS = {
    declaration: '0B5CAD',
    port: '197A3A',
    relationship: 'B35C00',
    layout: '7A3EA1',
    layoutOperator: 'C43C84',
    relationshipOperator: '6B7280'
} as const;
```

After changing colors, rebuild and verify the real Electron editor:

```powershell
yarn --cwd ide build
yarn --cwd ide/theia-product start:smoke:syntax-colors
```

Then restart the sample IDE:

```powershell
yarn --cwd ide start:m31
```

Do not use `editor.semanticTokenColorCustomizations` in `ide/theia-product/package.json` for this. Theia 1.73 does not register that preference in this product, so it is ignored.
