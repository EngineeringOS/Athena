# M14 Multi-Surface Authoring Position

## Product Position

Athena is `semantic-first`, not `DSL-first`.

The canonical semantic truth remains:

`authoring producer -> M8 semantic mutation path -> Engineering IR -> M14 resolution -> downstream consumers`

## Producer Surfaces

The following are all valid producers of engineering intent:

- graph
- forms
- templates
- AI
- API
- DSL

These surfaces do not own semantic truth independently. They all converge through the same semantic mutation authority.

## Write Authority

M8 semantic mutation remains the only write authority.

- graph edits must converge through semantic commands
- form edits must converge through semantic commands
- template application must converge through semantic commands
- AI proposals must converge through semantic commands
- API mutations must converge through semantic commands
- direct DSL edits must still re-enter canonical compilation and semantic mutation review

M14 does not open a second write path. It adds read-only component knowledge resolution above canonical `Engineering IR`.

## DSL Position

DSL is the canonical serialization format and source-of-truth representation.

DSL is not the required mainstream default UI for most engineers.

Direct DSL authoring is positioned as an expert surface for:

- power users
- library authors
- domain vendors
- automation engineers
- AI agents

Mainstream engineering workflows are expected to rely primarily on graphical and assisted authoring surfaces while still converging on the same semantic model.

## Why This Matters In M14

M14 proves component knowledge resolution. That proof is valuable only if Athena keeps one semantic authority and lets multiple authoring surfaces feed the same model.

If Athena drifted into a `DSL as mandatory UI` product position, M14 would become a technical demo instead of a usable engineering product.
