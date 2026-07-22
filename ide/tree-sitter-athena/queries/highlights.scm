; Athena Tree-sitter highlight queries — SYNTAX UX ONLY (AD-107).
; Never used for semantic diagnostics or Engineering IR.

(system_declaration "system" @athenaDeclarationKeyword)
(package_declaration "package" @athenaDeclarationKeyword)
(import_declaration "import" @athenaDeclarationKeyword)
(incomplete_package_declaration "package" @athenaDeclarationKeyword)
(incomplete_import_declaration "import" @athenaDeclarationKeyword)
(device_declaration "device" @athenaDeclarationKeyword)
(port_declaration "port" @athenaDeclarationKeyword)
(nested_port_declaration "port" @athenaDeclarationKeyword)
(connect_declaration "connect" @athenaRelationshipKeyword)
(connect_declaration "->" @operator)
(connect_group_edge "->" @operator)
(layout_declaration "layout" @athenaLayoutKeyword)
(place_statement "place" @athenaLayoutKeyword)
(align_statement "align" @athenaLayoutKeyword)
(group_statement "group" @athenaLayoutKeyword)
(layout_placement_relation) @athenaLayoutKeyword
(layout_axis) @athenaLayoutKeyword
(align_statement "aligned-with" @athenaLayoutOperator)
(align_statement "axis" @athenaLayoutKeyword)
(group_statement "grouped-with" @athenaLayoutOperator)

(name) @variable
(view_family_name) @namespace
((property_name) @athenaPortKeyword
  (#match? @athenaPortKeyword "^(direction|signal)$"))
((property_name) @property
  (#not-match? @property "^(direction|signal)$"))
(package_name) @namespace
((identifier) @athenaPortKeyword
  (#match? @athenaPortKeyword "^(in|out)$"))
((identifier) @variable
  (#not-match? @variable "^(in|out)$"))
(string) @string
(string_content) @string
