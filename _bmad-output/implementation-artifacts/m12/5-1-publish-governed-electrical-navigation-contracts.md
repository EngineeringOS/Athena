---
baseline_commit: 179a0a2
---

# Story 5.1: Publish Governed Electrical Navigation Contracts

Status: done

## Story

As a platform engineer,  
I want Athena to define explicit navigation and related-reveal contracts for electrical subjects,  
so that cross-reference movement is identity-safe and not built on renderer-local object ids.

## Completion Notes

- Added governed related-subject resolution in `athena-semantic-selection-model.ts` over electrical anchors and connection endpoints.
- Kept repeated-reference navigation anchored on published cross-reference metadata and canonical semantic ids.
- Preserved renderer-local ids as aliases only, never as the navigation authority.

## Change Log

- 2026-07-12: Completed during M12 related-navigation pass.
