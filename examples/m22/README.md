# M22 Proof Corpus

This directory contains the local proof assets for the M22 governed layout optimization and
round-trip milestone.

- `sample-project/` is the openable IDE workspace that presents the M22 scenarios as real `.athena`
  source files.

The M22 corpus is local and governed. It does not imply public repository/import ecosystem behavior,
full IEC library ingestion, cabinet authoring, physical routing, AI layout, full EPLAN parity, or
final layout-stack selection.

To inspect the customer-facing proof in the IDE:

```powershell
Set-Location ../../ide
yarn start:m22
```

The sample project starts from the accepted M21 graph workbench behavior. Later M22 stories add the
layout acceptance checklist, governed constraints, optimization facts, optional local ELK adapter,
and reviewable component layout round-trip behind this visible surface.
