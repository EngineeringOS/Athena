---
baseline_commit: 179a0a2
---

# Story 2.2: Harden Pan, Zoom, Focus, And Canvas Expansion Behavior

Status: done

## Story

As an engineer,  
I want panning, zooming, focus, and panel-driven canvas expansion to remain usable on larger scenes,  
so that the graph stays the main work surface during dense electrical work.

## Completion Notes

- Added auto-fit versus manual viewport mode tracking in `athena-graph-workbench-widget.tsx`.
- On viewport resize, auto-fit sessions re-fit against canonical scene bounds while manual sessions preserve the same world-center focus.
- Removed the fake fixed sheet rectangle and restored the infinite-canvas-first surface posture.
- Reduced flex/min-height conflicts so the graph stage can expand with the workbench layout.

## Change Log

- 2026-07-12: Completed during M12 viewport hardening pass.
