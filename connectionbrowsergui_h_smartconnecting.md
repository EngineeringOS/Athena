# Using Smart Connect

## Using Smart Connect

Smart connect allows the movement of [symbols](Glossary_o_symbole.md) in the schematic while retaining the autoconnection.

It is also possible to cut out symbols or schematic sections on a page and paste them on another page. In the process, the existing [connections](Glossary_o_verbindungen.md) are maintained, and [interruption points](Glossary_o_abbruchstellen.md) are inserted automatically at the autoconnect [lines](Glossary_o_leitungen.md) of the highlighted schematic area.

### Move schematic elements

Precondition:

You have opened a project page in the graphical editor.

1. Select the following commands: Tab Edit > Command group Options > ![](../Resources/Pictures/Gui/ALL/ribbon_smartconnect_as.png) Smart connect.
2. Hold down the left mouse button and move a symbol in the schematic.   
     
   ![](../Resources/Pictures/Gui/ALL/arrow.png) After releasing the left mouse button, the autoconnect lines are automatically redrawn.
3. If you no longer wish to use Smart connect, select the command Smart connect again.

![](../Resources/Pictures/Gui/ALL/info.png)Tip:

If while moving you realize that it would be better to cut out the different schematic elements (e.g., due to lack of space), simply cut out this schematic section by using the [Ctrl] + [X] shortcut key and paste it, for example, on another page in the project using [Ctrl] + [V].

### Copy, cut, and paste schematic elements

Precondition:

You have opened a project page in the graphical editor.

1. Select the following commands: Tab Edit > Command group Options > ![](../Resources/Pictures/Gui/ALL/ribbon_smartconnect_as.png) Smart connect.
2. Select the desired schematic elements in the schematic.
3. Select the following commands: Tab Home > Command group Clipboard > Cut.  
     
   ![](../Resources/Pictures/Gui/ALL/arrow.png) Interruption points are immediately inserted on the affected autoconnect lines on the source page. These interruption points are numbered automatically according to the [settings](Glossary_o_einstellungen.md) in the [Settings: Smart connect](interruptionpointgui_d_einstellsmartconnecting.md) dialog.
4. If necessary, change to another page.
5. Select the following commands: Tab Home > Command group Clipboard > Insert.
6. In the Insertion mode dialog, select the Do not modify option, and click [OK].
7. Place the schematic section on the target page with a single click.  
     
   ![](../Resources/Pictures/Gui/ALL/arrow.png) Interruption points – in addition to the cut-out schematic elements – are inserted and numbered automatically on the target page. On these interruption points, [cross-references](Glossary_o_querverweise.md) to the counterpieces are displayed on the source page.
8. End the action via the Cancel action popup menu item or via the [Esc] button.

![](../Resources/Pictures/Gui/ALL/example.png)[![Closed](../../Skins/Default/Stylesheets/Images/transparent.gif)Example:](javascript:void(0);)

The illustration shows the smart connect for the cutting out and inserting of schematic elements.

Before:

![](../Resources/Pictures/Visualisation/ALL/connectionbrowsergui_smartconnect1_as.png)

After:

![](../Resources/Pictures/Visualisation/ALL/connectionbrowsergui_smartconnect2_as.png)

The circles clarify the automatically inserted interruption points with the cross-references.

![](../Resources/Pictures/Gui/ALL/info.png)Tip:

On the automatically generated interruption points, cross-references to the counterpieces are displayed on the source page. Press the [F] key in order to jump back and forth between an interruption point and the respective counterpiece.

![](../Resources/Pictures/Gui/ALL/note.png)Note:

Insertion of schematic elements with smart connect in another project is not meaningful, because connections / autoconnect lines cannot be extended beyond the limits of a project, which also means that no cross-references can be displayed in such a case.

See also

[Connections: Operation](connectionbrowsergui_k_arbeitsweiseverbindungen.md)

[Updating Connections](connectionbrowsergui_h_verbindungenbearbeiten.md)
