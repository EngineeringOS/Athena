# M22 ELK Comparison

M22 compares ELK-assisted output only after helper output is normalized into normalized Athena layout
facts. The comparison is not raw adapter output and does not allow helper data to become renderer
truth.

Comparison rule: use normalized Athena layout facts.

## Comparison Basis

- Athena rule path: `RuleBasedSchematicLayoutOptimizer`
- ELK-assisted path: `ExperimentalElkSchematicLayoutOptimizer`
- Normalization gate: `SchematicLayoutHelperNormalizer`
- Evidence test: `experimental ELK adapter normalizes output into Athena facts`

## Checklist Comparison

| Checklist item | M22 comparison result |
| --- | --- |
| spacing | No measurable improvement yet; the local adapter currently proves the normalization boundary. |
| grouping | No measurable improvement yet; governed group facts remain produced by Athena constraints. |
| basic routing | No measurable improvement yet; M22 keeps basic schematic route behavior outside the adapter. |

## Decision

M22 does not select ELK as final architecture and the adapter is not the sole layout engine. The
value of the M22 spike is boundary validation: helper output can be normalized, compared, disabled,
and removed while the renderer continues to consume Athena layout facts.
