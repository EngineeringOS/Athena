; Athena Tree-sitter highlight queries — SYNTAX UX ONLY (AD-107).
; Never used for semantic diagnostics or Engineering IR.

(system_declaration "system" @keyword)
(package_declaration "package" @keyword)
(import_declaration "import" @keyword)
(incomplete_package_declaration "package" @keyword)
(incomplete_import_declaration "import" @keyword)
(device_declaration "device" @keyword)
(port_declaration "port" @keyword)
(nested_port_declaration "port" @keyword)
(connect_declaration "connect" @keyword)
(connect_declaration "->" @operator)
(connect_group_edge "->" @operator)
(layout_declaration "layout" @keyword)
(place_statement "place" @keyword)
(align_statement "align" @keyword)
(group_statement "group" @keyword)
(layout_placement_relation) @keyword
(layout_axis) @constant
(align_statement "aligned-with" @operator)
(align_statement "axis" @keyword)
(group_statement "grouped-with" @operator)

(name) @variable
(view_family_name) @namespace
(property_name) @property
(package_name) @namespace
(identifier) @variable
(string) @string
(string_content) @string
