# M13 Addendum

This addendum sharpens the planning implications of M13.

## 1. The Core Idea

Athena should treat presentation as a governed downstream language.

That language should start with two downstream levels:

1. **primitive presentation**
2. **composite presentation**

Electrical is the first serious pack built on that language, not the final name of the language itself.

## 2. Primitive Presentation

Primitive presentation atoms are the smallest reusable downstream units.

Electrical examples:

- contact mark
- coil mark
- terminal mark
- conductor segment
- junction mark
- reference arrow
- text slot
- title-block slot

Primitive definitions should carry:

- stable primitive id
- local anchor ids
- connection slots
- text slots
- token references
- optional orientation rules

They should not carry:

- engineering truth
- knowledge logic
- mutation authority

## 3. Composite Presentation

Composite presentation assembles primitives into one richer downstream occurrence.

Electrical examples:

- contactor presentation
- breaker presentation
- motor presentation
- PLC card presentation
- terminal strip presentation

Composite definitions should carry:

- composite id
- parts list
- slot mappings
- canonical port-to-anchor mapping
- local layout rules
- view-family applicability

Composite presentation remains downstream render contract.

It is not the engineering component itself.

## 4. Macro Boundary

The word `macro` must be treated carefully.

There are at least two very different things teams often call `macro`:

### Presentation macro

A reusable visual fragment or page fragment.

### Semantic macro

A reusable engineering assembly such as a DOL starter or repeated control fragment.

For Athena, the second meaning is strategically more important.

That means:

- M13 should **not** put semantic macro inside `Presentation IR`
- if Athena later needs reusable engineering composition, it should live above projection
- that future semantic assembly layer can then project down into `Presentation IR`

So M13 should stay focused on:

- primitives
- composites
- packs
- backend abstraction

Not on semantic macro authoring.

## 5. Why This Belongs To M13

This is too large for M12 because it changes:

- renderer input vocabulary
- domain-neutral downstream language naming
- symbol ownership model
- future backend strategy
- extension-pack architecture

That is a milestone boundary change, not a renderer polish story.

## 6. First Proof Shape

A narrow first M13 proof should likely include:

- one `Presentation IR` contract
- one primitive electrical presentation pack
- one composite electrical presentation pack
- one end-to-end proof repository
- one SVG proof backend

That is enough to prove the model without exploding scope.

## 7. Recommended Future Pack Families

Athena should expect later extension families such as:

- `presentation-pack-electrical-iec-basic`
- `presentation-pack-electrical-house-style-default`
- `presentation-pack-scada-basic`
- `presentation-pack-documentation-default`

The first M13 proof should stay much narrower than this future ecosystem.

## 8. Future Semantic Assembly Watchpoint

If the product later needs reusable engineering assemblies, a later milestone should introduce something closer to:

```text
Engineering IR
    ->
Semantic Assembly
    ->
Projection Model
    ->
Presentation IR
```

That future layer should not be smuggled into M13 under presentation vocabulary.
