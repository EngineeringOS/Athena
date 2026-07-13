# M14 Addendum

This addendum sharpens the architecture and planning implications of M14.

## 1. What M14 Is Really About

M14 is not the first knowledge milestone.

M9 already proved:

- derived engineering context
- capability facts
- constraint evaluation
- impact consequences

M14 instead introduces the component-knowledge substrate those later layers need.

Its job is to answer:

- what engineering concept is this component?
- what semantic ports does it own?
- what minimal physical traits does it carry?
- which vendor implementations may realize it?

## 2. Product Position

The strongest product rule in M14 is:

```text
DSL is canonical serialization
not the default human interface
```

Athena should preserve direct DSL authoring for:

- power users
- automation engineers
- library authors
- vendors
- AI agents

But mainstream users should eventually work through:

- graph
- forms
- templates
- AI-assisted creation

All of those surfaces still converge through one semantic command and mutation path before canonical serialization is updated.

## 3. Component Slice Priority

Recommended first proof ordering:

### P0

- PLC CPU
- Digital I/O
- Analog I/O

### P1

- Contactor
- Relay
- Motor

### P2

- 24V power supply
- Breaker
- Cable

### Deferred

- HMI
- drive / VFD
- sensor families

This keeps the proof aligned with prior electrical milestones and avoids catalog sprawl.

## 4. Loading Strategy

M14 should use compile-time deterministic knowledge-pack loading.

Recommended shape:

1. repository package graph resolves through M5 rules
2. active extension packs are determined from that graph
3. compiler builds one deterministic component-knowledge registry
4. authored component references resolve against that registry
5. unresolved or conflicting definitions surface as typed diagnostics

This means:

- no ad hoc runtime network fetch
- no dynamic remote vendor catalog as semantic authority
- no second lockfile as truth source

The existing `athena.lock` package graph remains the reproducibility anchor.

## 5. Conflict Model

If two active packs define:

- the same engineering concept id
- the same vendor part id
- the same canonical definition slot

without an explicit override contract, Athena should fail explicitly.

The first proof should not allow:

- filesystem-order precedence
- classpath-order precedence
- frontend-choice precedence

## 6. Engineering Concept vs Vendor Implementation

The core semantic distinction should be explicit:

```text
Engineering Concept
    ->
semantic ports
    ->
minimal physical meaning
    ->
possible vendor implementations
```

Example:

```text
electrical.plc.cpu
    ->
ports: L+, M, PE, MPI
    ->
physical: DIN rail, width, height, depth
    ->
vendor implementations:
        Siemens.S7300.CPU313C
        Siemens.S7300.CPU314C
```

Vendor parts are not the semantic type system.

## 7. Connection-Model Boundary

`kernel/connection-model` should stay narrow in M14.

It should define:

- semantic connection roles
- direction
- signal family
- optional protocol-bearing metadata

It should not become:

- the full rule engine
- broad electrical compatibility evaluation
- simulation behavior

Rich compatibility reasoning remains downstream M9 knowledge-pack logic.

## 8. Integration With M9

The right flow is:

```text
Engineering IR
    ->
M14 component knowledge resolution
    ->
M9 derived engineering context
    ->
M9 capability facts and constraint evaluation
```

So M14 output becomes later M9 input.

M14 solves:

```text
what is this component?
```

M9 solves:

```text
is this component sufficient, compatible, or impactful in context?
```

## 9. Integration With M13

The right downstream flow is:

```text
resolved engineering concept
    ->
projection family choice and downstream hints
    ->
presentation pack selection
    ->
renderer
```

M14 does not own renderer behavior.

It only provides the richer component identity that later presentation systems can consume.

## 10. Exclusions That Must Stay Explicit

M14 should not absorb:

- behavior model
- simulation
- digital twin execution
- broad standards-platform work
- broad vendor-catalog parity
- marketplace
- mainstream graphical authoring milestone work

Those all belong later.

## 11. The Proof Standard

The first M14 proof is successful if Athena can do all of the following deterministically:

1. resolve an authored component reference into a vendor-neutral engineering concept
2. publish typed semantic ports
3. publish minimal physical traits
4. map the concept to at least one vendor implementation
5. expose the resolved result to later knowledge-runtime and presentation consumers
6. fail explicitly on unresolved or conflicting definitions

That is enough to prove the architecture without pretending to solve the whole catalog problem.
