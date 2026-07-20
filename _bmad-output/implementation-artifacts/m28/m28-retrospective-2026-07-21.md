
# M28 Retrospective

Date: 2026-07-21

## Milestone Intent

M28 set out to turn Athena from a projection/viewer path into an authoring system while preserving
the semantic-first architecture:

```text
.athena source
  -> compiler semantic model
  -> projection facts
  -> semantic relationship intent
  -> governed source edit
  -> recompile and reproject
```

## Wins

- Nested device-owned ports are admitted across ANTLR4, AST, compiler lowering, semantic indexing,
  Tree-sitter, sample source, generated component source, and LSP source ranges.
- Canonical identity remains `port:Device.port`; nested syntax did not create a second identity
  scheme.
- `SemanticRelationshipIntent` is the M28 architectural root. `ConnectPortsIntent` is retained only
  as a compatibility shape.
- The M28 sample uses compact nested anatomy and opens through the Theia product smoke.
- Product-path authoring proof accepts one valid relationship and verifies source-backed projection
  refresh.
- Invalid relationship attempts do not mutate source; invalid accepted requests are blocked by the
  backend source-edit gate.

## Mistakes And Late Findings

- The compatibility validator was initially present only as a model-level contract. It was not
  enforced at the backend source-edit boundary until Epic 4. That meant a stale or malicious client
  could ask to accept an invalid relationship and still reach the serializer path.
- Product-path proof was added late. Earlier stories had unit and LSP coverage, but not a single
  sample-project smoke that exercised valid and invalid attempts together.
- Some older compatibility tests still use `connect-ports` naming. That is acceptable only because
  the retention is explicit and tested as legacy compatibility, not as the M28 root model.

## Blockers

- CodeGraph CLI timed out on broad repository queries during Epic 4, so investigation used targeted
  file inspection after the CodeGraph attempt failed.
- The Theia product smoke requires the installed LSP host. The verification path must run
  `:ide:lsp:installDist` before `yarn start:smoke:m28`.

## Lessons

- Validation contracts must be wired into the final persistence/edit gate, not only tested as
  standalone model behavior.
- Product-path smoke should exist before the final epic, not after most implementation is already
  complete.
- For source-language changes, parser parity and generated mutation output must be verified together.
- Retained legacy contracts need an owner, reason, and target milestone so they do not silently
  become architecture again.

## Verification Evidence

- `:kernel:language:test` focused nested-port parser test passed.
- `:kernel:compiler:test` focused nested-port, sample, and declaration-indexer tests passed.
- `:kernel:authoring-model:test` passed.
- `:ide:lsp:test` focused authoring request and M28 product smoke tests passed.
- `npm test` in `ide/tree-sitter-athena` passed.
- `yarn build` plus M28 frontend node tests in `ide/theia-frontend` passed.
- `:ide:lsp:installDist` passed.
- `yarn start:smoke:m28` in `ide` passed and opened `examples/m28/sample-project`.

## Next Risks

- M29 should formalize the Semantic Interaction Compiler/Interaction IR instead of letting Theia
  relationship mode become the durable interaction model.
- Legacy top-level ports should get a migration/removal decision after enough examples and fixtures
  have moved to nested anatomy.
- Relationship validation will need richer domain policy later, but M28 should not grow into
  standards intelligence.
