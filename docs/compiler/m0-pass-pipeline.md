# M0 Compiler Pass Pipeline

## Purpose

Story `1.5` makes the M0 compiler schedule explicit and inspectable.

- `AthenaCompiler.compile()` now executes a declared ordered pass pipeline.
- Each pass has a stable identity, documented responsibility, declared input state, and declared output state.
- The pipeline report is part of the compiler-facing result so reviewers can see which passes ran, which pass stopped, and why later passes were skipped.

This keeps compiler behavior deterministic and visible without introducing a second semantic authority beyond `Engineering IR`.

## Governed Knowledge Pre-Pass Setup

Story `2.5` adds governed knowledge resolution as compiler-owned setup that happens before the declared public pass pipeline begins.

- reviewed package roots are resolved into a governed knowledge context at compile start
- the resulting active and rejected package views are attached to compiler-facing results
- this setup is intentionally not exposed as a fifth public pass

That distinction preserves the declared pass contract while still allowing the compiler result to carry inspectable governed knowledge provenance.

## Declared Passes

The current M0 pipeline is:

1. `PARSE`
2. `LOWER`
3. `VALIDATE`
4. `DOWNSTREAM_DERIVATION`

Pass descriptors are declared under `compiler/` and currently mean:

| Pass | Responsibility | Input state | Output state |
| --- | --- | --- | --- |
| `PARSE` | Parse authored source into syntax-owned AST | `authored source file` | `syntax-owned source document` |
| `LOWER` | Lower syntax-owned source into canonical `Engineering IR` | `syntax-owned source document` | `canonical Engineering IR` |
| `VALIDATE` | Validate canonical `Engineering IR` and compute continuation policy | `canonical Engineering IR` | `semantic validation result` |
| `DOWNSTREAM_DERIVATION` | Derive the thin render-facing model and emit simple `SVG` when policy allows | `semantic validation result` | `render result` |

## Execution Status

Each pass emits one execution record with:

- the declared pass descriptor
- execution status: `SUCCEEDED`, `FAILED`, or `SKIPPED`
- a deterministic output summary for reviewer inspection

This report is exposed through `CompilerPipelineReport`.

## Current Gate Rules

The M0 gate rules remain intentionally simple:

- if parsing fails, `LOWER`, `VALIDATE`, and `DOWNSTREAM_DERIVATION` are skipped
- if semantic validation returns `STOP_DOWNSTREAM`, `DOWNSTREAM_DERIVATION` fails with `render-blocked`
- if semantic validation returns `CONTINUE`, `DOWNSTREAM_DERIVATION` succeeds with `svg-emitted`

These rules reuse Story `1.4`'s semantic continuation policy instead of inventing a second validity model.

## Determinism Notes

For identical source input and governed contracts, the pipeline must produce the same:

- pass order
- pass statuses
- pass summaries
- diagnostics
- render model
- emitted `SVG`
- final success or failure state

The current compiler tests lock those expectations for:

- valid compilation
- syntax failure
- semantic-invalid continuation stop
- published `SVG` conformance output
- governed knowledge context attachment without public pass reordering
- repeated execution over identical input
