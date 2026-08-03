# M39 Sprint Change: Reality Graph From Epic 2 Onward

## Decision

M39 Epic 1 remains complete. M39 Epic 2 onward is redesigned around Reality Graph:

```text
Engineering Reality
  -> Projection Reality
  -> Spatial Reality
  -> Presentation Reality
  -> Theia
```

## Reason

The old Epic 2+ plan centered on Diagram Grammar. That was an improvement over routing-first work, but it still risked another grammar monster. The deeper issue is that Athena currently mixes engineering truth, projection selection, spatial geometry, and presentation style.

## Impact

Removed from active M39 plan:

- Diagram Grammar-first Epic 2;
- electrical rail/rung/terminal-strip story stack as the immediate next work;
- stale rule that forbids `->`;
- any implication that M39 should reach professional drawing quality.

Added to active M39 plan:

- concrete reality ownership boundaries;
- one thin typed transformation interface;
- Spatial compiler authority;
- Presentation compiler authority;
- paint-only renderer proof;
- M39-local screenshots and retrospective.

## Exception

`to` is preferred. `->` is allowed as the same relation alias through one compiler path. This is the only approved compatibility-style exception and does not weaken the pre-1.0 cleanup rule.

## Second Review Scope Cut

M39 must prove the architecture, not build a broad architecture system.

- Epic 2 is Reality Foundations.
- Epic 3 is Reality Transformations.
- A Reality is a coherent domain with one authoritative fact set and one compiler owner.
- Projection Reality is purpose-first: a view-specific engineering document without coordinates or style.
- Routing is a subsystem of Spatial Reality.
- Preserved/derived/discarded transformation metadata is deferred.
