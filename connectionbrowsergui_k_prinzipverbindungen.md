# Connections: Principle

## Connections: Principle

In circuit diagram pages, [connection points](Glossary_o_anschluesse.md) of components or [connection symbols](Glossary_o_verbindungssymbole.md) which are precisely opposite one another, horizontally or vertically, will automatically be connected. This is called autoconnecting, and the connection [lines](Glossary_o_leitungen.md) created this way are called autoconnect lines. When inserting and moving [symbols](Glossary_o_symbole.md), a preview of the [autoconnecting](Glossary_o_autoconnecting.md) lines is shown.

The autoconnect lines are first purely graphic until the [connections](Glossary_o_verbindungen.md) are generated. This occurs automatically for many [actions](Glossary_o_aktionen.md) (e.g., when a page is opened) but can also be performed manually at any time. The individual connection can get its data from project [settings](Glossary_o_einstellungen.md), [potentials](Glossary_o_potenziale.md), or [connection definition points](Glossary_o_verbindungsdefinitionspunkte.md).

You use connection symbols such as T-nodes or double junctions to draw the course of the connections. Connection junctions and double junctions can be represented either as simple points or with target specification. In the latter case, the connection sequence is directly visible and is determined by the internal logic of the connection symbol ([target tracking](Glossary_o_zielverfolgung.md)).

Connections are displayed in the schematic according to the specified line data. You can specify the line data for the project, for potentials, or for individual connections.

### Connection sources and targets

A connection always has information about what it is connected to at both ends, the source and the target. The device tags of the connected [functions](Glossary_o_funktionen.md) are compared by default to determine the source and target. The sorting of the structure identifiers in the [structure identifier management](Glossary_o_strukturkennzeichen_verwaltung.md) will be used for these comparisons; the "smaller" [device tag](Glossary_o_betriebsmittelkennzeichen.md) will become the source. In addition, unplaced connections will be considered as well. If there are more than one connection between two functions, then one function is always the source for all connections, and the other function is always the target for all connections.

![](../Resources/Pictures/Gui/ALL/example.png)[![Closed](../../Skins/Default/Stylesheets/Images/transparent.gif)Example:](javascript:void(0);)

A "smaller" DT with identical structure identifiers means a lower number as counter / connection point designation (e.g. EB3+ET4-X1:2 < EB3+ET4-X1:5) or an identifier that is alphabetically located before the other identifier (e.g. EB3+ET4-K1:2 < EB3+ET4-X1:2).

The position of the identifier in the [structure identifier](Glossary_o_strukturkennzeichen.md) management is significant if the structure identifiers do not match. Here, a "smaller" identifier has a higher position in the respective table. This means that the [location designation](Glossary_o_ortskennzeichen.md) ET1 is positioned by default before the location designation ET3. Therefore EB3+ET1-X1:10 is, for example, the source and EB3+ET3-X1:4 the target of a connection.

![](../Resources/Pictures/Gui/ALL/note.png)Notes:

- Connection symbols (e.g. angles, T nodes) are not targets. [Shields](Glossary_o_abschirmungen.md) are often connected on one side, in which case the shield is itself the target.
- The setting Determine the source and target of connections from the placement can be used to specify that the source and target of the connections are not determined via the DT, but rather via the graphical position of the connected functions (command path: File > Settings > Projects > "Project name" > Connections > General). This is the same behavior as in Eplan before version 1.9 SP 1. [Cable connections](Glossary_o_kabelverbindungen.md) are not affected by this setting.

#### Exchange source and target

You can use the command Exchange source and target to respectively exchange the source and target including the source- and target-dependent connection properties for the connections marked in the graphical editor, in the connections navigator, in the page navigator or in the [layout space](Glossary_o_bauraum.md) navigator (Tab Connections > Command group Connections > Source and target).

In addition, the sequence of the [routing paths](Glossary_o_strecken.md) passed through is reversed in the [properties](Glossary_o_eigenschaften.md) Layout space: Routing track ([ID](Glossary_o_id.md) 31095) and Topology: Routing track (ID 20237).

If a connection exists in several [representation types](Glossary_o_darstellungsarten.md) in a project, source and target are exchanged globally. The direction of the connection is then the same at all representation types.

The following connection properties are exchanged:

| Source | | Target | |
| --- | --- | --- | --- |
| ID | Property | ID | Property |
| 31098 | Dual sleeve prescribed at 2 targets at the source | 31099 | Dual sleeve prescribed at 2 targets at the target |
| 31114 | Routing direction source | 31115 | Routing direction target |
| 31148 | Routing track source: X coordinate | 31151 | Routing track target: X coordinate |
| 31149 | Routing track source: Y coordinate | 31152 | Routing track target: Y coordinate |
| 31150 | Routing track source: Z coordinate | 31153 | Routing track target: Z coordinate |
| 31053 | Source: Sleeve cross-section | 31054 | Target: Sleeve cross-section |
| 31080 | Connection: Connection point length source | 31083 | Connection: Connection point length target |
| 31055 | Stripping length source | 31056 | Stripping length target |
| 31051 | Wire termination processing source | 31052 | Wire termination processing target |
| 31096 | Connection dimension source | 31097 | Connection dimension target |
| 31100 | Layout space: Specification for entry into routing path network (source) | 31116 | Layout space: Specification for entry into routing path network (target) |

#### Predefining functions as the source / target of the connection

You can also determine the source and target of connections manually. To do this use the Source / Target connection property at the connected functions. This property is available to you in the [property dialog](Glossary_o_eigenschaftendialog.md) of functions in the subsequent dialog Connection point logic. Specify here whether the connection point determines the source or the target of the connection. If the "Undefined" entry is selected, source and target of the connection are determined via the DT or - if the Determine the source and target of connections from the placement project setting is activated - page-oriented in accordance with the graphical sequence.

The property can, for example, be used in device [macros](Glossary_o_makros.md) or stored in symbols that you created yourself. For Cable connections you can control the source and target via this property, if the Determine source and target of the cables via the cable connections project setting is activated.

See also

[Connections](connectionbrowsergui_k_start.md)

[Connections: Operation](connectionbrowsergui_k_arbeitsweiseverbindungen.md)

[Using Smart Connect](connectionbrowsergui_h_smartconnecting.md)

[Unplaced Connections](connectionbrowsergui_k_npv.md)

[Specifying Connection Properties via Connection Definition Points](connectionbrowsergui_h_vdpeigenschaften.md)

[Cable Behavior](cablegui_k_prinzip.md)
