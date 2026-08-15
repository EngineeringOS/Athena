# M007 Graphite Source-base Transplant Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use
> `superpowers:subagent-driven-development` (recommended) or
> `superpowers:executing-plans` to implement this plan task-by-task. Steps use
> checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the complete local Graphite checkout Athena's exact runnable
source baseline before replacing any graphics function with electrical logic.

**Architecture:** Graphite's root Rust workspace, Svelte/WASM frontend, editor
backend, and desktop wrapper are copied unchanged into Athena's root with
Apache-2.0 provenance. Existing Athena `rust/` crates remain isolated migration
donors until later milestones connect one electrical workflow at a time.

**Tech Stack:** Graphite's pinned Rust workspace; Svelte 5; Vite 8; WASM;
Graphite desktop wrapper; PowerShell SHA-256 manifest verification.

---

### Task 1: Freeze Governance And Provenance

**Files:**
- Modify: `AGENTS.md`
- Create: `docs/superpowers/specs/2026-08-15-m007-graphite-source-base-transplant.md`
- Create: `docs/superpowers/plans/2026-08-15-m007-graphite-source-base-transplant.md`

- [x] **Step 1: Replace the clean-room Graphite rule with the explicit
  Apache-2.0 source-base rule.**
- [x] **Step 2: Record the exact source revision, copy boundary, baseline gate,
  and one-function-at-a-time replacement rule.**
- [ ] **Step 3: Commit only M007 governance documents.**

### Task 2: Import The Complete Graphite Tree

**Files:**
- Copy: `reference/Graphite/*` to repository root
- Exclude: `reference/Graphite/.git`
- Create: `docs/superpowers/verification/2026-08-15-m007-graphite-source-manifest.csv`

- [ ] **Step 1: Record the reference revision with `git -C reference/Graphite rev-parse HEAD`.**
- [ ] **Step 2: Copy every top-level Graphite directory and file, including
  hidden configuration, except `.git`, into the repository root.**
- [ ] **Step 3: Generate relative-path, byte-length, and SHA-256 rows for every
  reference file and its imported counterpart.**
- [ ] **Step 4: Fail the Copy Gate if any imported file is missing or differs.**
- [ ] **Step 5: Commit the unchanged Graphite baseline and manifest.**

### Task 3: Install The Pinned Graphite Toolchain

**Files:**
- Use unchanged: `Cargo.toml`, `Cargo.lock`, `frontend/package.json`,
  `frontend/package-lock.json`, `tools/cargo-run/`

- [ ] **Step 1: Run `npm ci` from `frontend/`.**
- [ ] **Step 2: Install or verify the exact `wasm-bindgen-cli 0.2.121` required
  by Graphite's workspace.**
- [ ] **Step 3: Verify Graphite's other reported build prerequisites without
  changing source or lockfiles.**
- [ ] **Step 4: Record exact tool versions and any prerequisite blocker.**

### Task 4: Run The Unmodified Web Baseline

**Files:**
- Use unchanged: `frontend/`, `frontend/wrapper/`, `editor/`, `node-graph/`
- Create: `docs/superpowers/verification/assets/m007/graphite-web-1440x900.png`

- [ ] **Step 1: Run the imported Graphite root's documented Web development command.**
- [ ] **Step 2: Verify the editor loads without page or console errors.**
- [ ] **Step 3: Capture the unmodified editor at 1440x900.**
- [ ] **Step 4: Compare the capture with the user-provided Graphite reference
  and keep the Web Baseline Gate open if it materially differs.**
- [ ] **Step 5: Commit Web baseline evidence.**

### Task 5: Run The Unmodified Desktop Baseline

**Files:**
- Use unchanged: `desktop/`
- Create when runnable: `docs/superpowers/verification/assets/m007/graphite-desktop-1440x900.png`

- [ ] **Step 1: Run the imported desktop command with its exact Windows prerequisites.**
- [ ] **Step 2: Capture the full 1440x900 desktop client if it starts.**
- [ ] **Step 3: If blocked, record the exact command, error, and prerequisite;
  do not substitute Athena's old GPUI shell.**

### Task 6: Close M007 Baseline Gates

**Files:**
- Create: `docs/superpowers/verification/2026-08-15-m007-graphite-source-base-transplant.md`
- Modify: `docs/superpowers/plans/2026-08-15-m007-graphite-source-base-transplant.md`

- [ ] **Step 1: Record Copy, License, Web, Desktop, Visual, and User gates.**
- [ ] **Step 2: Keep the imported Web and Desktop targets running when stable.**
- [ ] **Step 3: Commit and push verified M007 evidence to `next-001`.**
- [ ] **Step 4: Start M008 with exactly one QElectroTech electrical workflow;
  preserve the certified Graphite shell throughout the replacement.**
