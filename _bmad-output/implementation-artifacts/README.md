# Implementation Artifacts

- This directory is milestone-indexed.
- Every milestone must live under its own `m*` folder.
- The root of `implementation-artifacts/` is reserved for shared index files only and must not hold milestone story files directly.

## Current Layout

- `m0/`: completed M0 implementation artifacts and archive notes
- `m1/`: completed M1 implementation artifacts and sprint tracking
- `m2/`: completed M2 implementation artifacts and sprint tracking
- `m3/`: completed M3 implementation artifacts and sprint tracking
- `m4/`: completed M4 implementation artifacts and sprint tracking
- `m5/`: completed M5 implementation artifacts and sprint tracking
- `m6/`: completed M6 implementation artifacts and sprint tracking
- `m7/`: completed M7 implementation artifacts and sprint tracking

## Standard

- Milestone files must be grouped under `m0/`, `m1/`, `m2/`, and future `mN/` folders.
- Each active milestone should keep its own `sprint-status.yaml`.
- Story files, retrospectives, and milestone-local README files belong inside the milestone folder, not at the root.

## Naming Rule

- Do not mix different milestone cycles that restart `1.1`, `1.2`, and similar indices in one flat directory.
- When a milestone is closed, keep its references valid by updating in-repo links to the milestone folder instead of leaving legacy root paths behind.
- All future milestone execution cycles must follow this folder standard from the beginning.
