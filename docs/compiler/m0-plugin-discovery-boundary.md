# M0 Plugin Discovery Boundary

## Purpose

Story `2.2` turns Athena's typed plugin contracts into a real local discovery and activation boundary for the JVM-first M0 compiler.

The goal is not dynamic plugin behavior yet. The goal is deterministic discovery, compatibility validation, and a compiler-owned approved inventory.

## Discovery Mechanism

The core discovers plugins through the JVM `ServiceLoader` over the core-owned `AthenaPlugin` contract.

That keeps discovery:

- local
- classpath-based
- zero-extra-dependency
- owned by the compiler rather than by plugin-local frameworks

Plugins do not define their own discovery protocol. They publish service registration metadata and carry the core-owned manifest directly in code.

## Activation Rules

Before activation, the core validates every discovered plugin against:

- manifest identity rules
- declared plugin type
- allowed extension points for that type
- current Athena core runtime version compatibility

Candidates are sorted deterministically by plugin id, plugin version, and implementation class name before approval.

Malformed or incompatible candidates are rejected with inspectable diagnostics. Only approved plugins are attached into the compiler-owned inventory.

## Compiler Ownership

The compiler owns:

- candidate inventory
- rejected candidate diagnostics
- approved plugin inventory
- grouping by extension point
- pass ordering

Approved plugin inventory may be inspected and grouped by extension point, but it does not add, remove, or reorder the M0 passes:

- `PARSE`
- `LOWER`
- `VALIDATE`
- `DOWNSTREAM_DERIVATION`

## Story 2.2 Proof Shape

Story `2.2` proves:

- the sample `ElectricalRuntimeDomainPlugin` is discoverable from the classpath
- malformed and incompatible plugins are rejected before activation
- approved inventory ordering is deterministic
- plugin attachment does not redefine compiler pass ordering

## Non-Goals

Story `2.2` still does not include:

- semantic contributions flowing from plugins into live compilation
- renderer or rule plugin execution
- remote plugin distribution
- hot loading
- plugin sandboxing
- plugin dependency resolution
- standards import or export activation such as `AutomationML`

Those remain later Epic 2 work, especially Story `2.3`.
