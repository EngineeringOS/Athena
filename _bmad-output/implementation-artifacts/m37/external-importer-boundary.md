# M37 External Importer Boundary

M37 admits external standards and classifications only as typed evidence attached to Athena-owned facts.

## Boundary

- Athena source remains the single source of truth.
- `evidence` declarations may cite `iec` and neutral `classification` references.
- Evidence can attach to Connectivity Contract, Interface, Port, Connection Intent, and route-policy subjects.
- Evidence cannot create Components, Ports, Interfaces, Anchors, Connection Intent, compatibility, RouteFacts, Projection Policy, or rendered truth.
- AML, XML, ECLASS, vendor catalogs, and other external schemas are not runtime authority.

## Future Importers

Future importers must lower external material into reviewed Athena source or canonical Athena facts before product use.

Allowed future flow:

```text
External file/catalog -> importer -> Athena source/facts -> compiler validation -> derived evidence/projection
```

Forbidden flow:

```text
External file/catalog -> runtime resolver -> engineering truth
```

The importer may preserve source provenance and citation references, but Athena validation owns acceptance.
