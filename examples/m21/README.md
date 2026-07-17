# M21 Proof Corpus

This directory contains the local proof assets for the M21 engineering layout-intelligence
milestone.

- `sample-project/` is the openable IDE workspace that presents the M21 scenarios as real `.athena`
  source files.

The M21 corpus is local and governed. It does not imply public repository/import ecosystem behavior,
full IEC library ingestion, cabinet authoring, physical routing, AI layout, or final layout-stack
selection.

To inspect the customer-facing proof in the IDE:

```powershell
Set-Location ../../ide
yarn start:m21
```

The sample project starts from the accepted M20 canvas behavior. Later M21 stories add governed
layout intent, schematic routing, and readability facts behind this visible surface.
