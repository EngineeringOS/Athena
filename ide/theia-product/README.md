# `ide/theia-product`

English | [Chinese (Simplified)](README.zh-CN.md)

`ide/theia-product` is the active Athena Theia product composition package.

## Responsibility

- product-level composition
- curated capability-set policy
- desktop launch scripts and Theia application config
- product identity and branding entry points
- later desktop and browser packaging hooks
- additive Theia AI foundation dependency composition for provider configuration and generic assistant chrome

## Boundary

This package owns shell composition, not frontend widgets, backend orchestration internals, or direct kernel calls. It should stay small and product-facing.

## Commands

```powershell
Set-Location ide
yarn workspace @engineeringood/athena-theia-product build
yarn workspace @engineeringood/athena-theia-product start:smoke
yarn workspace @engineeringood/athena-theia-product start
```

## Launch Notes

- On Windows, the Athena Electron wrapper resolves Java 25 automatically before the product shell boots.
- Use `start:smoke` for deterministic proof that the desktop shell reached a real window-ready state.
- Use `start` for the live interactive window after the smoke check passes.
