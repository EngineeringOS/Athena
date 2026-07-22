# M31 Sample Project

This project is the M31 governed engineering model authoring customer sample
for a rolling-shutter control slice.

The Athena source is semantic persistence only. It defines devices, nested
ports, relationships, and layout intent that can be recompiled into the
governed two-sheet customer projection. The workspace is designed to exercise
entity creation, relationship authoring, cross-sheet reference facts, and
reopen-stable projection identity without making source text own renderer
geometry or sheet membership.

Open the sample:

```powershell
yarn --cwd ide start:m31
```

Run the structured M31 product smoke:

```powershell
yarn --cwd ide start:smoke:m31
```

The smoke writes its secondary human-review screenshot to
`_bmad-output/implementation-artifacts/m31/screenshots/m31-graph-workbench-smoke.png`.

M31 keeps Engineering Concept Templates and representation definitions
separate. This sample does not depend on imported visual assets or runtime
diagram-library files.
