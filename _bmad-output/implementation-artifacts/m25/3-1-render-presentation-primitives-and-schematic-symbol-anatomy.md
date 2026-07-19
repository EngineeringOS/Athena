---
status: ready-for-dev
epic: 3
story: 3.1
title: Render presentation primitives and schematic symbol anatomy
---

# Story 3.1: Render presentation primitives and schematic symbol anatomy

## Story

As an IDE user,
I want components to render as governed engineering representations,
So that the sheet no longer looks like generic graph boxes.

## Acceptance Criteria

- Supported symbols render from Presentation IR primitives, bounds, and hotspots.
- Generic fallback styling is absent from the accepted proof.
- Renderer code remains paint-only.
- Rendering remains inside the Theia IDE frontend only.

## Tasks/Subtasks

- [ ] Locate Graphical View rendering code using CodeGraph.
- [ ] Render M25 primitives from Presentation IR facts.
- [ ] Add fallback-free accepted-proof DOM markers.
- [ ] Verify no desktop-viewer/KMP/Compose frontend files are touched.

## Dev Notes

- Governed by AD-1, AD-2, AD-7, AD-9.

## Dev Agent Record

### Debug Log

### Completion Notes

### File List

## Change Log

## Status

ready-for-dev
