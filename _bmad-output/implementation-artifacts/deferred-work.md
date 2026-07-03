## Deferred from: code review of 1-3-lower-parsed-intent-into-canonical-engineering-ir.md (2026-07-02)

- Accumulate multiple syntax diagnostics instead of stopping at the first parser error - the current parser still returns only the first syntax issue, but that behavior predates Story 1.3 and is not uniquely caused by this lowering change.
