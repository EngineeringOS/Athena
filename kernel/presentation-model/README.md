# kernel:presentation-model

`kernel:presentation-model` defines Athena's neutral `Presentation IR`.

This module exists to keep the M13 ladder explicit:

`Engineering IR -> Projection Model -> Presentation IR -> domain presentation packs -> renderer backend`

What lives here:

- domain-neutral presentation documents
- primitive and composite presentation definitions
- occurrence and connector contracts
- traceability metadata that stays downstream of semantic authority

What does not live here:

- canonical engineering meaning
- semantic macro or engineering assembly
- backend-specific scene trees or draw batching internals

`engineeringood` means "good for engineering", and `ood` also reflects object-oriented development as another expression of the same semantic target that earlier drafts called `engineeringos`.
