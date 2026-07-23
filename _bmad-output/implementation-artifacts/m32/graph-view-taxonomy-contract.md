# M32 Graph View Taxonomy Contract

Story 7.1 defines the customer-facing Graph View vocabulary used by the rest of Epic 7.
Internal projection identifiers such as `cabinet`, `documentation`, and `schematic` remain
transport-safe ids only. Normal toolbar text must not expose them as peer product concepts.

## Product Concepts

| Concept | Customer-facing wording | Internal authority | Rule |
| --- | --- | --- | --- |
| Primary View | `View` | active projection payload | The product surface focuses on the professional schematic/sheet experience. Other projection families are compatibility surfaces unless a later story promotes them. |
| Sheet Navigation | `Sheet` | document projection sheet selector | Sheet selection is navigation inside the primary view, not a separate projection mode. |
| Presentation Profile | `Profile` | package/profile binding evidence | Profile choice is visual policy. It must not become semantic source truth. |
| Authoring Action | `Create`, `Create device`, `Create relationship` | semantic action intent and governed mutation authority | UI may request authoring actions, but source planning and serialization remain backend authority. |
| Information | `Projection information` | projection/product proof payload | Inspection text may expose technical evidence in tables, but buttons and titles use product language. |

## Retained Compatibility

Raw view ids may still appear in `data-athena-*` attributes, transport payloads, and diagnostic
evidence. They must not be used as visible toolbar names, normal aria labels, or popover titles.

Stories 7.2 through 7.5 must consume this contract instead of independently renaming controls.
