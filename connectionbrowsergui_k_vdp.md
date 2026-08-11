# Connection Definition Points

## Connection Definition Points

The connection definition point is used to assign [properties](Glossary_o_eigenschaften.md) to one or more [connections](Glossary_o_verbindungen.md) running below it. These properties enhance or replace the properties that the connection itself possesses. The properties specified here have priority over the connection properties predefined for the potential or in the project [settings](Glossary_o_einstellungen.md). In the single-line representation, or when displaying connection [bundles](Glossary_o_straenge.md), one connection definition point can influence multiple connections simultaneously.

![](../Resources/Pictures/Gui/ALL/note.png)Notes:

- [Reports](Glossary_o_auswertungen.md) should always be generated via connections and not via connection or [potential definition points](Glossary_o_potenzialdefinitionspunkte.md). If a "part" and a "length" are assigned to two connections via one connection definition point, the part must appear twice (once per connection) with that length in the report. For a report about the [connection definition points](Glossary_o_verbindungsdefinitionspunkte.md) the part and the length would only appear once in the reports.
- If you want to execute [check runs](Glossary_o_prueflaeufe.md) for the connections after modifications to connection definition points or connections, we recommend executing these check runs offline after a connection update.  
  Connection definition points do not represent the entire connection, but only assign specific properties to it. All modifications of the connection definition points are transferred to the associated connections only in case of an (automatic or manual) connection update.

A connection can be assigned multiple definition points. Their connection properties cannot only complement each other, but also contradict each other. Inconsistencies are detected during the inspection of [project data](Glossary_o_projektdaten.md) and flagged.

A connection definition point can also display connection properties that are not defined by that point itself. The property can, for example, be taken from the connection. However, these properties can't be changed.

Connection definition points can be copied or inserted (treated like [placed functions](Glossary_o_platziertefunktionen.md)).

When generating connections, a connection designation that consists of just question marks is valued as "undefined". This means that if multiple connection definition points are assigned to one connection, the connection gets the designation from the graphically first connection definition point whose designation does not consist of just question marks. If all of the connection definition points consist of only question marks in the designation or the [designations](Glossary_o_bezeichnungen.md) are empty, then the connects gets the designation from the graphically first connection definition points whose designation is not empty.

### Automatically placed connection definition points

Connection definition points can be generated and placed automatically for different [actions](Glossary_o_aktionen.md). Automatically placed connection definition points can be manually moved later.

- Edit connection properties: When you edit the properties of a placed connection in the connections navigator, the changed or supplemented values are stored at the associated connection definition point. If no connection definition point is assigned to the connection yet, such a connection definition point is generated and placed.
- Connection numbering: Connection definition points can be automatically placed via [connection numbering](Glossary_o_verbindungsnummerierung.md). In the settings for connection numbering you specify where and how often the connection definition points are placed at the connections.
- Generate cable: Connection definition points are also automatically placed when automatically generating [cables](Glossary_o_kabel.md).
- Route connections: When [routing connections](Glossary_o_verlegeverbindungen.md) in the [layout space](Glossary_o_bauraum.md), certain properties (for example the connection length) are transferred from the 3D connection to the associated 2D connection. If no connection definition point has been assigned to the 2D connection, it is generated and placed.

### Connection properties saved in macros

If a connection definition point is included in a macro, the connection properties are also contained in the macro. The connection definition points are saved in the macro but not the connections. The connections are regenerated when the macro is inserted and receive the properties saved in the definition point.

### Default symbols for connection definition points

In the project settings you can specify default [symbols](Glossary_o_symbole.md) for connection definition points, potential definition points and [net definition points](Glossary_o_netzdefinitionspunkte.md) independently of the user in the Settings: Default symbols dialog. This allows uniform and consistent usage of these symbols within a project.

See also

[Connections](connectionbrowsergui_k_start.md)

[Connections: Operation](connectionbrowsergui_k_arbeitsweiseverbindungen.md)

[Unplaced Connections](connectionbrowsergui_k_npv.md)

[Specifying Connection Properties via Connection Definition Points](connectionbrowsergui_h_vdpeigenschaften.md)

[Tab Placement (connection numbering)](wirenumberinggui_r_platzierung.md)

[Dialog Settings: Default symbols](connectionsettingsgui_d_einstellungenstandardsymbole.md)
