# M004 QElectroTech Behavior Inventory

**Status:** evidence baseline only. This records observable legacy behavior; it does not authorize copying QElectroTech's Qt/C++, XML, SQLite, UI, or architecture.

`Confirmed` means the claim has direct local documentation and/or source evidence. `Unknown` means it must not be assumed by an Athena spec.

## Confirmed Inventory

| ID | Behavior | User-doc evidence | Source entrypoint evidence | Status |
| --- | --- | --- | --- | --- |
| QET-PROJ-001 | A project owns ordered folios, elements, conductors, report/export settings, inter-folio links, and master data. | `reference/qelectrotech-doc/source/users/project/what_is.rst:7-20`; `folio/what_is.rst:7-16` | `reference/qelectrotech-source-mirror/sources/qetproject.h:76`; `qetproject.h:126-158` | Confirmed |
| QET-FOLIO-001 | A folio is a project page. Add it through the Project menu, project panel, folio tabs, or `Ctrl+T`. | `folio/add_folio.rst:7-47` | `sources/qetproject.cpp:1322-1332`, `QETProject::addNewDiagram(int)` | Confirmed |
| QET-FOLIO-002 | A new folio inherits project border, title-block, and conductor defaults. | `folio/properties/folio_title_block.rst:12-15` | `sources/qetproject.cpp:1330-1332` | Confirmed |
| QET-TITLE-001 | A folio selects a project title-block template, positions it bottom/right, and can edit or duplicate it. | `folio/properties/folio_title_block.rst:7-29` | `sources/titleblockproperties.h:29-68`; `sources/bordertitleblock.cpp:288-314` | Confirmed |
| QET-TITLE-002 | Title-block properties include title, author, date, file, folio, plant, location, revision, page number, and custom name/value variables. | `folio/properties/folio_title_block.rst:47-84`; `folio/title_block/what_is.rst:8-37` | `sources/qetproject.cpp:513-565` | Confirmed |
| QET-LIB-001 | Built-in, user, and project element collections are searchable. Users manage the user collection, edit project copies, and place imported elements. | `interface/panels/collections_panel.rst:9-29`; `element/collection/what_is.rst:7-20` | `sources/ElementsCollection/xmlprojectelementcollectionitem.h:26-64` | Confirmed |
| QET-LIB-002 | First use embeds a deduplicated element into the project collection; removing an instance does not remove it. Cleaning removes unused embedded copies. | `element/collection/project_collection.rst:7-27`; `project/clean_project.rst:7-32` | `sources/qetproject.cpp:1568`, `readElementsCollectionXml` | Confirmed |
| QET-ELEM-001 | Placement selects an element-numbering pattern, searches/drags from a collection, supports repeated clicks, and `Esc` ends the tool. | `schema/element/element_add.rst:7-24` | `sources/diagramevent/diagrameventaddelement.cpp`; `sources/factory/elementfactory.cpp:52` | Confirmed |
| QET-ELEM-002 | Master and slave elements can link; cross-reference presentation follows project defaults; users can navigate linked elements across folios. | `schema/element/references/related_items.rst:7-68` | `sources/qetgraphicsitem/masterelement.cpp:50-85` | Confirmed |
| QET-TERM-001 | An element terminal permits conductor connection and has position, cardinal orientation, and a UUID-like name. | `element/element_parts/terminal.rst:7-52` | `sources/qetgraphicsitem/terminal.h:36,148-154` | Confirmed |
| QET-COND-001 | Manual conductors are drawn from one terminal to an automatically identified destination terminal. | `schema/conductor/conductor_creation.rst:7-31` | `sources/qetgraphicsitem/conductor.cpp:129-143` | Confirmed |
| QET-COND-002 | Automatic connection can create conductors when a placed element's terminals are aligned. A different-potential connection warns but is still created. | `schema/conductor/conductor_creation.rst:33-76` | `sources/utils/conductorcreator.cpp:58-68`; `sources/ui/potentialselectordialog.cpp:191-282` | Confirmed |
| QET-COND-003 | Conductors inherit folio defaults, have formula/properties, and property changes can propagate through a potential. | `conductor/properties/conductor_numbering.rst:9-15` | `sources/qetgraphicsitem/conductor.cpp:131-133,1540-1591` | Confirmed |
| QET-NUM-001 | Folios, elements, and conductors each select automatic numbering patterns combining text, variables, and sequential numbers. | `interface/panels/autonumbering_panel.rst:9-52`; `element/properties/element_numbering.rst:7-12`; `conductor/properties/conductor_numbering.rst:9-15` | `sources/qetproject.h:147-165`; `sources/conductorautonumerotation.cpp:42-53` | Confirmed |
| QET-VAR-001 | Percent-prefixed variables resolve in title blocks, text/properties, and numbering. Project, folio, element, and conductor scopes include folio and grid-position data. | `annex/variables.rst:9-78` | `sources/qetproject.cpp:1736-1741` | Confirmed |
| QET-REPORT-001 | A configurable summary/project-index table is generated onto the active folio and explicitly reloaded after folio/property edits. | `reports/summary/create_summary.rst:7-68`; `reports/summary/reload_summary.rst:7-14` | `sources/factory/qetgraphicstablefactory.cpp:56-71,94-106` | Confirmed |
| QET-REPORT-002 | A configurable nomenclature/BOM table is generated onto the active folio and explicitly reloaded after element/property edits. | `reports/nomenclature/create_nomenclature.rst:7-71`; `reports/nomenclature/reload_nomenclature.rst:7-14` | `sources/factory/qetgraphicstablefactory.cpp:37-52`; `sources/dataBase/projectdatabase.cpp:321-331` | Confirmed |
| QET-EXPORT-001 | Schema export chooses folios, size, target directory, format, and rendering options, creating one file per folio. | `export&print/export_schema.rst:6-49` | `sources/exportdialog.cpp:48-58`; `sources/exportdialog.h:65-98` | Confirmed |
| QET-EXPORT-002 | CSV exports exist for conductors and nomenclature; nomenclature export supports selected field order and filters. | `export&print/export_wires.rst:6-15`; `export&print/export_nomenclature.rst:6-47` | `sources/conductornumexport.cpp:43-119`; `sources/ui/bomexportdialog.cpp:60-107` | Confirmed |

## Confirmed Limits and Conflicts

| ID | Finding | Evidence | Consequence |
| --- | --- | --- | --- |
| QET-LIMIT-001 | Conductor-list and I/O-list reports are marked unavailable in QElectroTech 0.8. | `reports/conductor_list.rst:7`; `reports/io_list.rst:7` | Not a certified parity requirement without version-specific evidence. |
| QET-CONFLICT-001 | Docs call a CAD format `DWX`, while the source includes `createdxf.cpp`. | `export&print/export_schema.rst:6-7`; `sources/createdxf.cpp:40-64` | Exact CAD format matrix is unresolved. |

## Unknown / Not Yet Certified

- Potential identity/network traversal and propagation rules beyond the documented warning and property-propagation entrypoint.
- Junction, multiline/single-line, routing, terminal-strip, and report integration semantics.
- Master/slave cardinality, deletion edge cases, cross-reference rendering, and update rules.
- Numbering grammar, collisions, renumber lifecycle, and undo transactions.
- Title-block template grammar/rendering/migration and project-cleaning edge cases.
- Full export/print/PDF error handling and format behavior.
- Collection import/category/editing edge cases.

## Certification Rule

No Athena slice may claim QElectroTech parity for an inventory ID until a later spec supplies an Athena domain mapping, Desktop and WASM workflow tests, persistence evidence, and user-visible acceptance evidence. This inventory is an old-world behavior oracle, not an implementation blueprint.
