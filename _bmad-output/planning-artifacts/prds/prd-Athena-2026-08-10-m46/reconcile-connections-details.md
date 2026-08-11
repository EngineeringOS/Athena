# Reconciliation: Connection Architecture Notes

All load-bearing input landed:

- Connection is engineering meaning, not graphic line.
- Engineering Port contains no geometry; package anchors map geometry separately.
- Engineering Net is first-class for multi-endpoint equivalence.
- endpoint role is distinct from Port direction.
- physical connection facts remain semantic; graphic line style remains projection.
- topology operators are explicit data, not hardcoded traversal rules.
- semantic and graphic validation remain separate.
- layout planning sits between Connection IR and paint.
- semantic edit and presentation edit use different authority paths.

No gap remains for M46 planning.
