# M29 Architecture Spine Reality Check

Verdict: needs revision.

The PRD/addendum are broadly coherent. The main problems are in the spine's grounding against the live repo: it promotes a few module placements too early and treats one already-existing app as if it were still future work.

## Findings

1. **`kernel/interaction-model` is asserted as the initial boundary without a real repo anchor.**  
   `ARCHITECTURE-SPINE.md:68, 156-160` makes `kernel/interaction-model` the default home for interaction contracts, but the current Gradle tree in `settings.gradle.kts:35-70` does not include any such project and the kernel tree has no `interaction-model` directory. The PRD/addendum only recommend this as the preferred option (`prd.md:310-312`), so the spine is overstating a proposal as if it were already reality-checked.

2. **The spine treats existing JS package roots as if they were current Gradle modules.**  
   `ARCHITECTURE-SPINE.md:135-160` names `ide/theia-frontend` and `integrations/graph-glsp` as structural modules, but `settings.gradle.kts:35-70` only wires in `:ide:lsp`, `:integrations:scm-git`, and the kernel modules. `ide/theia-frontend/package.json` and `integrations/graph-glsp/package.json` show these are real package roots, but not Gradle subprojects. The module map is therefore mixing present package layout with future build topology.

3. **The deferred list contains a stale repo fact: CLI is not future work.**  
   `ARCHITECTURE-SPINE.md:143-148` defers "Web/3D/VR/CLI adapters," but `settings.gradle.kts:35-70` already includes `:apps:cli` as a live project. If the intent is to defer additional CLI adapters, the wording needs to say that explicitly; otherwise the spine is describing an existing module as if it were still deferred.

## Bottom Line

The PRD/addendum support the milestone direction. The spine needs a tighter reality check on what is already in the build graph versus what is only proposed.
