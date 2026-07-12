# M10 Retrospective

## Milestone Result

M10 proved that Athena can deliver AI-assisted engineering reasoning without moving engineering truth into prompts, frontend state, or provider SDKs.

## Main Wins

- deterministic reasoning context assembly above governed M9 outputs
- provider-neutral session and proposal lifecycle
- additive LSP and Theia workbench delivery
- explicit audit, decision, and proof-corpus paths
- live IDE verification against the installed JVM LSP host, not only direct tests

## Main Risks Remaining

- current proof provider is deterministic and narrow; live-model providers still need later hardening
- Theia AI foundation is wired additively, but rich assistant chrome remains later work
- broader domain depth still depends on later milestones, not M10
- one unrelated Theia backend RPC startup warning still appears and should be resolved separately

## What Went Wrong

- The committed M10 proof corpus locks were semantically valid but not rendered in canonical lock form, so semantic SCM stayed `baseline-unresolved` and impact-summary reasoning looked broken even though the runtime path was correct.
- The first live desktop bring-up still reported `Unsupported request method: athena/aiReasoningState` because the installed `athena-lsp-host` distribution was stale even though direct LSP tests were already green.

## What We Learned

- Athena proof repositories must preserve canonical `athena.lock` rendering, not only semantic equivalence, because semantic SCM treats noncanonical lock state as unresolved input.
- When LSP request surfaces change, `:ide:lsp:build` is part of the real closeout path because Theia launches the installed host distribution, not loose test classes.
- M10 is strong enough to verify end to end at the IDE boundary: proof corpus -> semantic SCM -> reasoning proposal -> workbench panels.

## Follow-Up Actions

1. Trace and remove the remaining Theia backend RPC startup warning.
2. Keep `examples/m10/reasoning-proof/*/athena.lock` canonical whenever the proof corpus changes.
3. Treat installed-host rebuild as a required verification step whenever LSP transport contracts change.
