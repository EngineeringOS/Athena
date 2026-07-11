# M9 Addendum

This addendum captures useful M9 planning detail that informs architecture and epic shaping but is intentionally more implementation-shaped than the main PRD body.

## 1. Agreed Milestone Position

The active sequence is now:

- **M8** - unified semantic mutation model
- **M9** - engineering knowledge runtime

This means M9 is not:

- another IDE shell milestone
- another graph framework milestone
- another workbench-density milestone

It is the first milestone where Athena must prove that canonical semantic meaning can be transformed into governed engineering context, capability, constraint, and impact understanding.

## 2. Why M9 Is Kernel-First

The current M9 draft thesis is:

> Athena must know engineering, not only store engineering structure.

That is a kernel claim, not a renderer claim.

The existing screenshots under `draft/screenshort/` and the QElectroTech source mirror under `reference/qelectrotech-source-mirror/` are valuable references, but they point at:

- renderer depth
- diagram editor depth
- sheet and symbol workflows
- properties and project navigation

Those are later milestone concerns.

For M9, they should be treated only as deferred context.

## 3. Narrowed Proof Shape

The strategic draft suggests several large fronts:

- capability model
- constraint engine
- impact analysis
- knowledge facts
- semantic diagnostics

That is too broad for one honest milestone if all of them are treated as platforms.

The narrowed implementation reading is:

1. derive a first governed engineering context layer
2. promote selected meanings into capability facts
3. evaluate a first fixed governed knowledge-pack rule slice
4. publish impact and diagnostics through existing product paths

That keeps the proof architectural rather than aspirational.

## 4. Recommended First Electrical Fact Slice

The first proof should likely stay within a single understandable electrical sufficiency family.

Good candidate inputs:

- motor power
- voltage
- power factor
- efficiency
- breaker rated current
- cable allowed current
- relay rated current

Good candidate derived context:

- full-load current
- starting current
- thermal load

Good candidate capability facts:

- required protection current
- cable demand
- relay sizing demand

Good candidate constraint results:

- undersized protection
- undersized cable
- incompatible relay sizing
- downstream affected subjects

The key rule is:

- derived context is not yet a fact
- capability facts are engineering judgements
- not vendor-part facts

## 5. Knowledge Pack Shape

The first M9 proof should introduce an explicit object:

- `knowledge-pack-electrical-basic`

The first pack should own:

- a narrow set of derived-context formulas
- a narrow set of capability-fact semantics
- a fixed rule slice
- typed result vocabulary

It should not yet become:

- a general end-user rule-authoring system
- a broad standards-pack ecosystem
- a company-policy pack framework

## 6. Constraint Style

The first rule pack should feel like:

- engineering lint
- engineering sufficiency checks
- engineering impact graph

It should not yet feel like:

- a full expert system
- a standards encyclopedia
- a procurement optimizer

## 7. Product Delivery Strategy

M9 should reuse the current product surfaces:

- LSP diagnostics
- Problems panel
- semantic inspection
- semantic SCM or review output

That is enough to prove the knowledge-runtime architecture.

M9 should avoid:

- new large editor surfaces
- new notation-heavy renderer commitments
- new page or sheet UX systems

## 8. Example Proof Statements

### Example A

Input:

```eos
motor M1 {
    power = 7.5kw
}

breaker QF1 {
    current = 10A
}

cable C1 {
    current = 12A
}
```

Output:

```text
ERROR:
QF1 rated current insufficient for M1

WARNING:
C1 utilization exceeds recommended margin

INFO:
Affected subjects:
FR1
KM1
C1
QF1
```

### Example B

Input change:

```text
motor power
5.5kw -> 7.5kw
```

Output:

```text
Derived context:
FullLoadCurrent=14.1A

Capability facts:
RequiredProtectionCurrent>=18A

Impact:
QF1
FR1
KM1
C1
```

### Example C

Review output:

```text
Change:
upgrade motor M1

Engineering impact:
- protection sizing affected
- cable sizing affected
- overload relay review required
```

## 9. Risks That Must Stay Explicit

- rule sprawl can make M9 look impressive while hiding whether the first rule slice is correct
- vendor or standards richness can prematurely drag M9 into catalog and compliance-platform work
- renderer references can tempt the milestone back toward IDE depth instead of knowledge proof
- diagnostics language can blur structural invalidity with engineering insufficiency unless typed clearly

## 10. Recommended Carry-Forward Split

If M9 succeeds, the recommended next ownership split is:

- **M9** - engineering knowledge runtime
- **M10** - AI-assisted reasoning above governed knowledge outputs
- **M11** - knowledge-pack ecosystem
- **M12** - company policy and standards packs

That keeps the roadmap honest:

- first prove engineering understanding
- then add higher-order assistance
- then widen governed knowledge packaging
- then widen company-specific policy and standards depth
