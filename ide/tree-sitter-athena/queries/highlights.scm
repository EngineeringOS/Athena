; Athena Tree-sitter highlight queries — SYNTAX UX ONLY (AD-107).
; Never used for semantic diagnostics or Engineering IR.

(system_declaration "system" @keyword)
(package_declaration "package" @keyword)
(import_declaration "import" @keyword)
(incomplete_package_declaration "package" @keyword)
(incomplete_import_declaration "import" @keyword)
(device_declaration "device" @keyword)
(port_declaration "port" @keyword)
(connect_declaration "connect" @keyword)
(connect_declaration "->" @operator)

(name) @variable
(property_name) @property
(package_name) @namespace
(identifier) @variable
(string) @string
(string_content) @string
