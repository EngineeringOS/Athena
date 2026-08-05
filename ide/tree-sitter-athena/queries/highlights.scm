; Syntax UX only. JVM parser/compiler remain semantic authority.

(system_declaration "system" @athenaDeclarationKeyword)
(package_declaration "package" @athenaDeclarationKeyword)
(import_declaration "import" @athenaDeclarationKeyword)
(incomplete_package_declaration "package" @athenaDeclarationKeyword)
(incomplete_import_declaration "import" @athenaDeclarationKeyword)
(entity_declaration "entity" @athenaDeclarationKeyword)
(structure_assignment "structure" @athenaDeclarationKeyword)
(structure_assignment "display" @athenaDeclarationKeyword)
(function_declaration "function" @athenaFunctionKeyword)
(function_role "role" @athenaFunctionKeyword)
(port_declaration "port" @athenaPortKeyword)
(nested_port_declaration "port" @athenaPortKeyword)
(connect_declaration "connect" @athenaRelationshipKeyword)
(connect_declaration "to" @athenaRelationshipKeyword)
(connect_group_edge "to" @athenaRelationshipKeyword)

(name) @variable
(package_name) @namespace
((property_name) @athenaPortKeyword
  (#match? @athenaPortKeyword "^(direction|flow|minimum|maximum|designationType|designation)$"))
((property_name) @property
  (#not-match? @property "^(direction|flow|minimum|maximum|designationType|designation)$"))
((identifier) @athenaPortKeyword
  (#match? @athenaPortKeyword "^(in|out|bidirectional|passive|unbounded)$"))
((identifier) @variable
  (#not-match? @variable "^(in|out|bidirectional|passive|unbounded)$"))
(direction_name) @athenaPortKeyword
(number) @number
(positive_integer) @number
(string) @string
(string_content) @string
