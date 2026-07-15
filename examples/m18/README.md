# M18 Proof Corpus

This directory accumulates executable M18 evidence by implementation slice.

- `syntax-proof/` proves compiler and Tree-sitter package/import syntax behavior.
- `linking-lowering-proof/` proves compiler-owned semantic linking and canonical lowering fixtures.
- `repository-proof/` proves checked-in governed repository fixtures for M18 closeout evidence.

The corpus uses only local governed repository and source fixtures. It does not imply remote
registry, marketplace, publish, multi-root, or frontend-owned semantic resolution behavior.
