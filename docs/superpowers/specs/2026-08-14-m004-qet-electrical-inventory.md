# M004 QElectroTech Electrical Inventory

## Evidence Policy

This is an inventory, not a completed parity statement. `EVIDENCED` means the
user documentation or source names the capability; it does not mean Athena has
implemented it. Each later slice must expand its claimed IDs with exact line
level source traces, inputs, state transitions, failures, and workflow tests.

| ID | Functional family and observable outcome | Primary evidence | Athena status |
| --- | --- | --- | --- |
| QET-PROJ-001 | Create, open, save, close, clean, and manage a project directory. | `reference/qelectrotech-doc/source/users/project/index.rst:1` | `PARTIAL` |
| QET-FOLIO-001 | A folio is an ordered project unit/page and becomes one output page/file on export. | `reference/qelectrotech-doc/source/users/folio/what_is.rst:7` | `PARTIAL` |
| QET-FOLIO-002 | Configure folio properties, page types, title blocks, and title-block fields. | `reference/qelectrotech-doc/source/users/folio/index.rst:1`; `reference/qelectrotech-source-mirror/sources/bordertitleblock.h:37` | `UNRESEARCHED` |
| QET-TITLE-001 | Resolve project/folio variables in title-block text, including folio identity/order. | `reference/qelectrotech-doc/source/users/annex/variables.rst:9`; `reference/qelectrotech-source-mirror/sources/bordertitleblock.cpp:919` | `UNRESEARCHED` |
| QET-LIB-001 | Browse element collections/categories and project/user collections. | `reference/qelectrotech-doc/source/users/element/collection/index.rst:1`; `reference/qelectrotech-source-mirror/sources/ElementsCollection/elementscollectionwidget.h:46` | `PARTIAL` |
| QET-ELEM-001 | Place and manipulate electrical elements in the schema workspace. | `reference/qelectrotech-doc/source/users/schema/element/index.rst:1`; `reference/qelectrotech-source-mirror/sources/diagramevent/diagrameventaddelement.h:24` | `PARTIAL` |
| QET-ELEM-002 | Elements have electrical/industrial data, project links, and typed families. | `reference/qelectrotech-doc/source/users/element/what_is.rst:7` | `UNRESEARCHED` |
| QET-ELEM-003 | Support simple, master, slave, previous/next folio reference, and terminal-block element roles. | `reference/qelectrotech-doc/source/users/element/what_is.rst:21` | `UNRESEARCHED` |
| QET-XREF-001 | Build and maintain inter-folio/cross-reference behavior for linked elements. | `reference/qelectrotech-doc/source/users/element/cross_reference/index.rst:1`; `reference/qelectrotech-doc/source/users/schema/element/references/index.rst:1` | `UNRESEARCHED` |
| QET-COND-001 | Create and edit conductors between terminals as electrical transmission paths. | `reference/qelectrotech-doc/source/users/conductor/what_is.rst:6`; `reference/qelectrotech-source-mirror/sources/conductorprofile.h:28` | `PARTIAL` |
| QET-COND-002 | Store typed conductor properties, auto-numbering, labels, and profile behavior. | `reference/qelectrotech-doc/source/users/conductor/properties/index.rst:1`; `reference/qelectrotech-source-mirror/sources/conductorproperties.h:67`; `reference/qelectrotech-source-mirror/sources/conductorautonumerotation.h:34` | `UNRESEARCHED` |
| QET-COND-003 | Support conductor type and electrical semantics, not merely geometric lines. | `reference/qelectrotech-doc/source/users/conductor/type/index.rst:1` | `UNRESEARCHED` |
| QET-NUM-001 | Auto-number elements, conductors, and folios from context variables. | `reference/qelectrotech-doc/source/users/annex/variables.rst:9`; `reference/qelectrotech-source-mirror/sources/autoNum/assignvariables.h:27` | `UNRESEARCHED` |
| QET-SCHEMA-001 | Provide schema basics: selection, copy/cut/paste, delete, rotate, layers, search, replace, tables, text, and pictures. | `reference/qelectrotech-doc/source/users/schema/index.rst:1` | `PARTIAL` |
| QET-REPORT-001 | Generate summary, nomenclature, conductor list, and I/O list reports from electrical project data. | `reference/qelectrotech-doc/source/users/reports/index.rst:1` | `UNRESEARCHED` |
| QET-EXPORT-001 | Export and print folios/projects with folio/page semantics. | `reference/qelectrotech-doc/source/users/export&print/index.rst:1`; `reference/qelectrotech-doc/source/users/folio/what_is.rst:11` | `UNRESEARCHED` |
| QET-EDITOR-001 | Create/edit reusable elements through a dedicated element editor. | `reference/qelectrotech-doc/source/users/element/element_editor/index.rst:1`; `reference/qelectrotech-source-mirror/sources/editor/elementscene.h:45` | `UNRESEARCHED` |
| QET-UI-001 | Expose project tabs, folio tabs, menu/toolbars, workspace, panels, and help/search UI as user workflows. | `reference/qelectrotech-doc/source/users/interface/index.rst:1` | `UNRESEARCHED` |
| QET-PLATE-001 | Define the electrical content plates shown inside modern Graphite-style panels: project, folio/title-block, element, conductor, terminal, cross-reference, numbering, and report fields/groups. | `reference/qelectrotech-doc/source/users/project/properties/index.rst:1`; `reference/qelectrotech-doc/source/users/folio/properties/index.rst:1`; `reference/qelectrotech-doc/source/users/element/properties/index.rst:1`; `reference/qelectrotech-doc/source/users/conductor/properties/index.rst:1`; `reference/qelectrotech-doc/source/users/reports/index.rst:1` | `UNRESEARCHED` |

## Current Certified Scope

None. `PARTIAL` entries indicate prototype foundations only. They must not be
marketed or tested as QElectroTech parity.

## QET-PLATE-001 Panel-Plate Evidence

These groups are the minimum content taxonomy for future Athena contextual
property panels. They are not instructions to clone the old Qt dialog layout.
Graphite determines panel geometry, docking, widget lifecycle, and refresh;
QElectroTech determines the electrical group names and domain fields.

| Electrical plate | Required QET groups | Primary evidence | Future Athena panel trigger |
| --- | --- | --- | --- |
| Project plate | Display, general properties, new-folio defaults, numbering. | `reference/qelectrotech-doc/source/users/project/properties/index.rst:1`; `reference/qelectrotech-source-mirror/sources/ui/projectpropertiesdialog.h:30` | Active project/no specific selected entity. |
| Folio plate | Display, size, title block, type, appearance. | `reference/qelectrotech-doc/source/users/folio/properties/index.rst:1`; `reference/qelectrotech-source-mirror/sources/ui/borderpropertieswidget.h:33`; `reference/qelectrotech-source-mirror/sources/ui/titleblockpropertieswidget.h:39` | Active folio/sheet. |
| Element plate | Display, general, texts, information, author, numbering; specialized master plate where applicable. | `reference/qelectrotech-doc/source/users/element/properties/index.rst:1`; `reference/qelectrotech-source-mirror/sources/ui/elementpropertieswidget.h:31`; `reference/qelectrotech-source-mirror/sources/ui/masterpropertieswidget.h:44` | One or more selected electrical elements. |
| Conductor plate | Display, type, appearance, numbering. | `reference/qelectrotech-doc/source/users/conductor/properties/index.rst:1`; `reference/qelectrotech-source-mirror/sources/ui/conductorpropertieswidget.h:32`; `reference/qelectrotech-source-mirror/sources/conductorproperties.h:67` | One or more selected conductors. |
| Cross-reference plate | Default and element-specific cross-reference properties. | `reference/qelectrotech-doc/source/users/project/properties/new_folio/cross_references.rst:1`; `reference/qelectrotech-source-mirror/sources/ui/xrefpropertieswidget.h:34` | Project/folio defaults or linked element selection. |
| Report plate | Summary, nomenclature, conductor list, and I/O list generation inputs/results. | `reference/qelectrotech-doc/source/users/reports/index.rst:1` | Report workspace/panel command. |

## Plate Certification Rule

A future panel is `MAPPED` only after all its QET groups have field-level
evidence. A panel may not show generic `Reference`, `Description`, or
`Wire label` fields merely because those are convenient prototype fields.

## Required Next Research Per ID

Before an ID can become `MAPPED`, capture the exact user sequence, source
entry point, source state owner, persistent fields, validation/failure paths,
undo semantics, save/reopen semantics, and expected desktop/web visible result.
