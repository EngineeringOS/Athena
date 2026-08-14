# M004 QElectroTech Electrical Inventory

## Evidence Policy

This is an inventory, not a completed parity statement. `EVIDENCED` means the
user documentation or source confirms the capability; it does not mean Athena
has implemented it. Each later slice must expand its claimed IDs with exact
inputs, state transitions, failures, undo behavior, persistence behavior, and
workflow tests. The line-level companion is
[`../research/2026-08-14-m004-qelectrotech-behavior-inventory.md`](../research/2026-08-14-m004-qelectrotech-behavior-inventory.md).

| ID | Functional behavior or family | Primary evidence | Reference status |
| --- | --- | --- | --- |
| QET-PROJ-001 | A project owns ordered folios, electrical objects, report/export settings, inter-folio links, and project master data. | `reference/qelectrotech-doc/source/users/project/what_is.rst:7`; `reference/qelectrotech-source-mirror/sources/qetproject.h:76` | `EVIDENCED` |
| QET-PERSIST-001 | Projects can be created, opened, saved/saved-as, closed with dirty-change prompting, and cleaned. | `reference/qelectrotech-doc/source/users/project/new_project.rst:7`; `reference/qelectrotech-doc/source/users/project/open_project.rst:7`; `reference/qelectrotech-doc/source/users/project/save_project.rst:7`; `reference/qelectrotech-doc/source/users/project/close_project.rst:7`; `reference/qelectrotech-doc/source/users/project/clean_project.rst:7` | `EVIDENCED` at workflow level; format/error semantics remain unresearched |
| QET-FOLIO-001 | A folio is an ordered project page and can be added through project, panel, tab, or keyboard workflows. | `reference/qelectrotech-doc/source/users/folio/add_folio.rst:7`; `reference/qelectrotech-source-mirror/sources/qetproject.cpp:1322` | `EVIDENCED` |
| QET-FOLIO-002 | A new folio inherits project border, title-block, and conductor defaults. | `reference/qelectrotech-doc/source/users/folio/properties/folio_title_block.rst:12`; `reference/qelectrotech-source-mirror/sources/qetproject.cpp:1330` | `EVIDENCED` |
| QET-TITLE-001 | A folio selects a project title-block template, positions it at the bottom or right, and can edit or duplicate the template. | `reference/qelectrotech-doc/source/users/folio/properties/folio_title_block.rst:7`; `reference/qelectrotech-source-mirror/sources/titleblockproperties.h:29` | `EVIDENCED` |
| QET-TITLE-002 | Title-block data includes title, author, date, file, folio, plant, location, revision, page number, and custom variables. | `reference/qelectrotech-doc/source/users/folio/properties/folio_title_block.rst:47`; `reference/qelectrotech-source-mirror/sources/qetproject.cpp:513` | `EVIDENCED` |
| QET-LIB-001 | Built-in, user, and project element collections are searchable and have distinct management behavior. | `reference/qelectrotech-doc/source/users/interface/panels/collections_panel.rst:9`; `reference/qelectrotech-doc/source/users/element/collection/what_is.rst:7` | `EVIDENCED` |
| QET-LIB-002 | First use embeds a deduplicated element definition in the project; project cleaning removes unused embedded definitions. | `reference/qelectrotech-doc/source/users/element/collection/project_collection.rst:7`; `reference/qelectrotech-doc/source/users/project/clean_project.rst:7` | `EVIDENCED` |
| QET-ELEM-001 | Element placement chooses numbering, finds a collection item, supports repeat placement, and exits with `Esc`. | `reference/qelectrotech-doc/source/users/schema/element/element_add.rst:7`; `reference/qelectrotech-source-mirror/sources/diagramevent/diagrameventaddelement.cpp:114-180`; `reference/qelectrotech-source-mirror/sources/diagramevent/diagrameventaddelement.cpp:226-249` | `EVIDENCED` |
| QET-ELEM-002 | Master and slave elements link, use project cross-reference presentation, and support cross-folio navigation. | `reference/qelectrotech-doc/source/users/schema/element/references/related_items.rst:7`; `reference/qelectrotech-source-mirror/sources/qetgraphicsitem/masterelement.cpp:50` | `EVIDENCED` |
| QET-TERM-001 | Element terminals accept conductor connections and carry position, cardinal orientation, and identity. | `reference/qelectrotech-doc/source/users/element/element_parts/terminal.rst:7`; `reference/qelectrotech-source-mirror/sources/qetgraphicsitem/terminal.h:36` | `EVIDENCED` |
| QET-COND-001 | Manual conductors connect a source terminal to an identified destination terminal. | `reference/qelectrotech-doc/source/users/schema/conductor/conductor_creation.rst:7`; `reference/qelectrotech-source-mirror/sources/qetgraphicsitem/conductor.cpp:129` | `EVIDENCED` |
| QET-COND-002 | Aligned placement may auto-create conductors; a different-potential connection warns but remains permitted. | `reference/qelectrotech-doc/source/users/schema/conductor/conductor_creation.rst:33`; `reference/qelectrotech-source-mirror/sources/utils/conductorcreator.cpp:58` | `EVIDENCED` |
| QET-COND-003 | Conductors inherit folio defaults, carry formula/properties, and can propagate property changes through a potential. | `reference/qelectrotech-doc/source/users/conductor/properties/conductor_numbering.rst:9`; `reference/qelectrotech-source-mirror/sources/qetgraphicsitem/conductor.cpp:1540` | `EVIDENCED` |
| QET-NUM-001 | Folios, elements, and conductors select numbering patterns made from text, variables, and sequential numbers. | `reference/qelectrotech-doc/source/users/interface/panels/autonumbering_panel.rst:9`; `reference/qelectrotech-source-mirror/sources/qetproject.h:147` | `EVIDENCED` |
| QET-VAR-001 | Percent-prefixed variables resolve in title blocks, properties/text, and numbering with project, folio, element, and conductor scopes. | `reference/qelectrotech-doc/source/users/annex/variables.rst:9-78`; `reference/qelectrotech-source-mirror/sources/autoNum/assignvariables.cpp:375-398`; `reference/qelectrotech-source-mirror/sources/qetproject.h:297` | `EVIDENCED` |
| QET-HISTORY-001 | Undo and redo restore prior actions; the main undo panel exposes actions since the last save and clears on save. | `reference/qelectrotech-doc/source/users/interface/toolbars.rst:41-43`; `reference/qelectrotech-doc/source/users/interface/panels/undo_panel.rst:9-22` | `EVIDENCED` at observable workflow level; transaction internals remain unresearched |
| QET-REPORT-001 | A configurable project-index table is generated on the active folio and explicitly reloaded after relevant changes. | `reference/qelectrotech-doc/source/users/reports/summary/create_summary.rst:7`; `reference/qelectrotech-doc/source/users/reports/summary/reload_summary.rst:7` | `EVIDENCED` |
| QET-REPORT-002 | A configurable BOM table is generated on the active folio and explicitly reloaded after relevant element changes. | `reference/qelectrotech-doc/source/users/reports/nomenclature/create_nomenclature.rst:7`; `reference/qelectrotech-doc/source/users/reports/nomenclature/reload_nomenclature.rst:7` | `EVIDENCED` |
| QET-EXPORT-001 | Schema export selects folios, size, directory, format, and rendering options and creates one file per folio. | `reference/qelectrotech-doc/source/users/export&print/export_schema.rst:6`; `reference/qelectrotech-source-mirror/sources/exportdialog.cpp:48` | `EVIDENCED` |
| QET-EXPORT-002 | CSV exports exist for conductors and BOM data; BOM export supports ordered fields and filters. | `reference/qelectrotech-doc/source/users/export&print/export_wires.rst:6`; `reference/qelectrotech-doc/source/users/export&print/export_nomenclature.rst:6` | `EVIDENCED` |
| QET-PLATE-001 | QET exposes project, folio/title-block, element, conductor, cross-reference, numbering, and report property groups whose electrical content must be preserved by Athena. | `reference/qelectrotech-doc/source/users/project/properties/index.rst:1`; `reference/qelectrotech-doc/source/users/folio/properties/index.rst:1`; `reference/qelectrotech-doc/source/users/element/properties/index.rst:1`; `reference/qelectrotech-doc/source/users/conductor/properties/index.rst:1` | `EVIDENCED` as QET taxonomy; Athena panel mapping is separate |
| QET-FOLIO-003 | Folio management includes add, delete, properties, type, and title-block workflows. | `reference/qelectrotech-doc/source/users/folio/index.rst:7` | `UNRESEARCHED` beyond QET-FOLIO-001/002 and QET-TITLE-001/002 |
| QET-SCHEMA-001 | Schema editing includes element/conductor/text/table/basic-object/picture creation plus selection, copy/cut/paste, delete, rotate, layers, search, and replace. | `reference/qelectrotech-doc/source/users/schema/index.rst:7` | `UNRESEARCHED` as a complete family |
| QET-XREF-001 | Cross-reference and previous/next folio-reference workflows link and navigate electrical items across folios. | `reference/qelectrotech-doc/source/users/element/cross_reference/index.rst:1`; `reference/qelectrotech-doc/source/users/schema/element/references/index.rst:1` | `UNRESEARCHED` beyond QET-ELEM-002 |
| QET-EDITOR-001 | Reusable electrical elements are created and edited in a dedicated element editor with its own save/quit/editing workflows. | `reference/qelectrotech-doc/source/users/element/element_editor/index.rst:7` | `UNRESEARCHED` |
| QET-PRINT-001 | Projects/folios can be printed or rendered to PDF with folio selection and print options. | `reference/qelectrotech-doc/source/users/export&print/print.rst:7`; `reference/qelectrotech-doc/source/users/export&print/create_pdf.rst:6` | `UNRESEARCHED` beyond workflow existence |
| QET-UI-001 | The main UI exposes menus, toolbars, workspace, panels, project/folio tabs, help, search, and customization. | `reference/qelectrotech-doc/source/users/interface/index.rst:7` | `UNRESEARCHED`; QET does not define Athena shell geometry |
| QET-PREF-001 | User preferences configure appearance, display, language, project defaults, grid, export, printing, element, and text behavior. | `reference/qelectrotech-doc/source/users/preferences/index.rst:7` | `UNRESEARCHED` |
| QET-DRAWING-001 | Drawing outputs include mounting-plate and list-of-parts workflows. | `reference/qelectrotech-doc/source/users/drawing/index.rst:7` | `UNRESEARCHED` |
| QET-BASIC-001 | Folios can contain basic lines, rectangles, ellipses, and polygons in addition to electrical objects. | `reference/qelectrotech-doc/source/users/schema/basics/index.rst:7` | `UNRESEARCHED`; generic drawing remains subordinate to schematic needs |

## Current Certified Scope

None. `EVIDENCED` describes the old-world reference only. It does not describe
Athena implementation status and must not be marketed or tested as parity.

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
| Report plate | Summary/project-index and nomenclature/BOM generation inputs/results. Conductor-list and I/O-list reports are explicit 0.8 limits, not required fields. | `reference/qelectrotech-doc/source/users/reports/summary/create_summary.rst:7`; `reference/qelectrotech-doc/source/users/reports/nomenclature/create_nomenclature.rst:7`; `reference/qelectrotech-doc/source/users/reports/conductor_list.rst:7`; `reference/qelectrotech-doc/source/users/reports/io_list.rst:7` | Report workspace/panel command. |

### Field-Level Minimums

| Plate | Required fields/semantic groups before it can be implemented | Primary evidence |
| --- | --- | --- |
| Element | General: definition name, folio/coordinates/rotation, size, terminal count, definition location. Information/BOM: label, system location, article number, manufacturer, supplier. Text, author, and numbering groups remain separate. | `reference/qelectrotech-doc/source/users/element/properties/element_general.rst:7`; `reference/qelectrotech-doc/source/users/element/properties/element_information.rst:7`; `reference/qelectrotech-doc/source/users/element/properties/element_numbering.rst:7`; `reference/qelectrotech-source-mirror/sources/ui/elementpropertieswidget.h:31` |
| Conductor | Multiline display text/formula/color/orientation; electrical function, voltage/protocol, color, section, cable, bus; single-line ground/neutral/PEN/phases; visual line type/colors/width; numbering. | `reference/qelectrotech-doc/source/users/conductor/properties/conductor_type.rst:9`; `reference/qelectrotech-doc/source/users/conductor/properties/conductor_appearance.rst:7`; `reference/qelectrotech-doc/source/users/conductor/properties/conductor_numbering.rst:7`; `reference/qelectrotech-source-mirror/sources/ui/conductorpropertieswidget.h:32` |
| Folio/title block | Access from menu/workspace/toolbar/project panel/tabs/shortcut; dimensions: column count/width/header visibility and row count/height/header visibility; template collection/selection, bottom/right placement, edit/duplicate; Title/Author/Date/File/Folio/Plant/Location/Rev index/Page Num; custom name/value variables; default conductor type/properties and appearance. | `reference/qelectrotech-doc/source/users/folio/properties/display.rst:7-19`; `reference/qelectrotech-doc/source/users/folio/properties/folio_size.rst:7-24`; `reference/qelectrotech-doc/source/users/folio/properties/folio_title_block.rst:7-84`; `reference/qelectrotech-doc/source/users/folio/properties/folio_type.rst:7-12`; `reference/qelectrotech-doc/source/users/folio/properties/folio_appearance.rst:7-12` |
| Project | Access from active-project menu or any project-panel item; general default/save variables and custom project variables; new-folio defaults for dimensions, title block, default/custom folio variables, conductor type/appearance, cross-reference presentation, and folio-reference labels; numbering-pattern sets for elements, conductors, and folios. | `reference/qelectrotech-doc/source/users/project/properties/display.rst:7-31`; `reference/qelectrotech-doc/source/users/project/properties/general_prop.rst:7-30`; `reference/qelectrotech-doc/source/users/project/properties/new_folio/folio.rst:7-31`; `reference/qelectrotech-doc/source/users/project/properties/new_folio/conductor.rst:7-26`; `reference/qelectrotech-doc/source/users/project/properties/new_folio/cross_references.rst:7-31`; `reference/qelectrotech-doc/source/users/project/properties/new_folio/folio_referencing.rst:7-20`; `reference/qelectrotech-doc/source/users/project/properties/numbering_prop.rst:7-29` |

## Plate Certification Rule

A future panel is `MAPPED` only after all its QET groups have field-level
evidence. A panel may not show generic `Reference`, `Description`, or
`Wire label` fields merely because those are convenient prototype fields.

## Required Next Research Per ID

Before an ID can become `MAPPED`, capture the exact user sequence, source
entry point, source state owner, persistent fields, validation/failure paths,
undo semantics, save/reopen semantics, and expected desktop/web visible result.
