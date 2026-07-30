// Athena Tree-sitter grammar — SYNTAX UX ONLY.
//
// AD-107: Tree-sitter owns syntax UX only (highlighting/structure), never semantic truth.
// AD-110: this grammar mirrors the current M18 package/import plus M17 system syntax subset,
// M23 system-scoped layout-block admission, M28 nested device-owned ports, compact grouped connect
// authoring, the frozen M34 native Symbol/Element syntax subset, and M35 installation cabinet syntax.
// `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt` /
// `AthenaLanguageParser.kt`: optional package, repeated imports, one system block, and the
// existing device/port/connect, layout, qualified-name, string, identifier, property, and M34
// Symbol/Element/Profile/Binding syntax.
//
// Do NOT add aliases, wildcards, visibility, comments, generic expressions, or renderer/transport
// vocabulary. Widening this grammar beyond the frozen subset
// (AD-104) is an explicit future-story decision, not an incidental addition.
//
// This grammar relies on Tree-sitter's built-in error recovery (no hand-rolled error
// productions) so that partial/incomplete input still yields a best-effort, usable tree —
// Story 3.3 proves this concretely.

module.exports = grammar({
  name: 'athena',

  extras: $ => [/\s/],

  // Enables Tree-sitter's keyword-extraction optimization so the `system`/`device`/`port`/
  // `connect`/`->`-adjacent literal tokens below take priority over the generic identifier
  // token when they match the same text, mirroring how the handwritten JVM tokenizer treats
  // keywords as identifier lexemes matched positionally rather than as reserved words.
  word: $ => $.identifier,

  conflicts: $ => [
    [$.device_declaration],
    [$.port_declaration],
    [$.nested_port_declaration],
    [$.function_declaration],
    [$.element_child],
    [$.binding_declaration],
    [$.binding_select],
    [$.installation_declaration],
  ],

  rules: {
    source_file: $ => seq(
      optional(choice($.package_declaration, $.incomplete_package_declaration)),
      repeat(choice($.import_declaration, $.incomplete_import_declaration)),
      choice(
        $.system_declaration,
        repeat1($._representation_declaration),
      ),
    ),

    _representation_declaration: $ => choice(
      $.symbol_declaration,
      $.element_declaration,
      $.profile_declaration,
      $.binding_declaration,
    ),

    package_declaration: $ => seq(
      'package',
      $._header_space,
      field('name', $.package_name),
    ),

    import_declaration: $ => seq(
      'import',
      $._header_space,
      field('target', $.package_name),
    ),

    // Explicit low-precedence nodes keep following declarations usable while the author types.
    // They are syntax recovery only and never produce compiler/LSP diagnostics or package meaning.
    incomplete_package_declaration: _ => prec(-1, 'package'),

    incomplete_import_declaration: _ => prec(-1, 'import'),

    // One contiguous token rejects skipped trivia around dots and hyphens, matching the compiler.
    package_name: _ => token.immediate(seq(
      /[A-Za-z_][A-Za-z0-9_]*/,
      repeat(seq('-', /[A-Za-z_][A-Za-z0-9_]*/)),
      repeat(seq(
        '.',
        /[A-Za-z_][A-Za-z0-9_]*/,
        repeat(seq('-', /[A-Za-z_][A-Za-z0-9_]*/)),
      )),
    )),

    _header_space: _ => token.immediate(/[ \t]+/),

    system_declaration: $ => seq(
      'system',
      field('name', alias($.identifier, $.name)),
      '{',
      repeat($.declaration),
      optional('}'),
    ),

    declaration: $ => choice(
      $.device_declaration,
      $.port_declaration,
      $.connect_declaration,
      $.layout_declaration,
      $.installation_declaration,
    ),

    symbol_declaration: $ => seq(
      'symbol',
      field('name', alias($.identifier, $.name)),
      '{',
      repeat($._symbol_member),
      optional('}'),
    ),

    _symbol_member: $ => choice(
      $.symbol_identity,
      $.symbol_version,
      $.resource_declaration,
      $.graphic_declaration,
      $.anchor_declaration,
    ),

    symbol_identity: $ => seq(
      'identity',
      field('value', $.string),
    ),

    symbol_version: $ => seq(
      'version',
      field('value', $.string),
    ),

    element_declaration: $ => seq(
      'element',
      field('name', alias($.identifier, $.name)),
      '{',
      repeat($._element_member),
      optional('}'),
    ),

    _element_member: $ => choice(
      $.element_identity,
      $.element_version,
      $.resource_declaration,
      $.graphic_declaration,
      $.element_bounds,
      $.element_child,
      $.element_export_anchor,
      $.element_export_label,
    ),

    element_identity: $ => seq(
      'identity',
      field('value', $.string),
    ),

    element_version: $ => seq(
      'version',
      field('value', $.string),
    ),

    resource_declaration: $ => seq(
      'resource',
      field('name', alias($.identifier, $.name)),
      '{',
      repeat($._resource_member),
      '}',
    ),

    _resource_member: $ => choice(
      $.resource_kind,
      $.resource_path,
    ),

    resource_kind: $ => seq(
      'kind',
      'svg',
    ),

    resource_path: $ => seq(
      'path',
      field('value', $.string),
    ),

    element_bounds: $ => seq(
      'bounds',
      field('value', $.bounds_literal),
    ),

    element_child: $ => seq(
      'child',
      field('name', alias($.identifier, $.name)),
      '{',
      repeat($._element_child_member),
      optional('}'),
    ),

    _element_child_member: $ => choice(
      $.child_symbol_reference,
      $.child_translate,
      $.child_rotate,
      $.child_scale,
      $.child_z_order,
    ),

    child_symbol_reference: $ => seq(
      'symbol',
      field('identity', $.string),
    ),

    child_translate: $ => seq(
      'translate',
      field('value', $.point_literal),
    ),

    child_rotate: $ => seq(
      'rotate',
      field('value', $.number),
    ),

    child_scale: $ => seq(
      'scale',
      field('value', $.point_literal),
    ),

    child_z_order: $ => seq(
      'zOrder',
      field('value', $.number),
    ),

    element_export_anchor: $ => seq(
      'export',
      'anchor',
      field('name', alias($.identifier, $.name)),
      'from',
      field('source', $.qualified_name),
    ),

    element_export_label: $ => seq(
      'export',
      'label',
      field('name', alias($.identifier, $.name)),
      'from',
      field('source', $.qualified_name),
    ),

    graphic_declaration: $ => choice(
      seq(
        'graphic',
        '{',
        repeat($._graphic_member),
        '}',
      ),
      seq(
        'graphic',
        'svg',
        'resource',
        field('resource', $.identifier),
      ),
    ),

    _graphic_member: $ => choice(
      $.graphic_bounds,
      $.line_primitive,
      $.polyline_primitive,
      $.arc_primitive,
      $.circle_primitive,
      $.rectangle_primitive,
      $.label_slot,
    ),

    graphic_bounds: $ => seq(
      'bounds',
      field('value', $.bounds_literal),
    ),

    bounds_literal: $ => seq(
      '(',
      $.number,
      ',',
      $.number,
      ',',
      $.number,
      ',',
      $.number,
      ')',
    ),

    line_primitive: $ => seq(
      'line',
      field('name', alias($.identifier, $.name)),
      'from',
      field('from', $.point_literal),
      'to',
      field('to', $.point_literal),
      $.style_reference,
    ),

    polyline_primitive: $ => seq(
      'polyline',
      field('name', alias($.identifier, $.name)),
      'points',
      field('points', $.point_list),
      $.style_reference,
    ),

    point_list: $ => seq(
      '(',
      commaSep1($.point_literal),
      ')',
    ),

    arc_primitive: $ => seq(
      'arc',
      field('name', alias($.identifier, $.name)),
      'center',
      field('center', $.point_literal),
      'radius',
      field('radius', $.number),
      'from',
      field('start', $.number),
      'sweep',
      field('sweep', $.number),
      $.style_reference,
    ),

    circle_primitive: $ => seq(
      'circle',
      field('name', alias($.identifier, $.name)),
      'center',
      field('center', $.point_literal),
      'radius',
      field('radius', $.number),
      $.style_reference,
    ),

    rectangle_primitive: $ => seq(
      'rectangle',
      field('name', alias($.identifier, $.name)),
      'at',
      field('origin', $.point_literal),
      'size',
      field('size', $.size_literal),
      $.style_reference,
    ),

    label_slot: $ => seq(
      'label',
      field('name', alias($.identifier, $.name)),
      'at',
      field('origin', $.point_literal),
      'size',
      field('size', $.size_literal),
      'role',
      field('role', $.profile_value_name),
      $.style_reference,
    ),

    style_reference: $ => seq(
      'style',
      field('name', $.profile_value_name),
    ),

    anchor_declaration: $ => seq(
      'anchor',
      field('name', alias($.identifier, $.name)),
      '{',
      repeat($._anchor_member),
      '}',
    ),

    _anchor_member: $ => choice(
      $.primitive_reference,
      $.anchor_port,
      $.anchor_point,
      $.anchor_role,
      $.anchor_direction,
      $.anchor_signal,
    ),

    primitive_reference: $ => seq(
      'ref',
      field('target', $.string),
    ),

    anchor_port: $ => seq(
      'port',
      field('value', $.qualified_name),
    ),

    anchor_point: $ => seq(
      'point',
      field('value', $.point_literal),
    ),

    anchor_role: $ => seq(
      'role',
      field('value', $.identifier),
    ),

    anchor_direction: $ => seq(
      'direction',
      field('value', $.direction_name),
    ),

    direction_name: _ => token(prec(1, /(?:in|out|bidirectional)/)),

    anchor_signal: $ => seq(
      'signal',
      field('value', $.qualified_name),
    ),

    point_literal: $ => seq(
      '(',
      $.number,
      ',',
      $.number,
      ')',
    ),

    size_literal: $ => seq(
      '(',
      $.number,
      ',',
      $.number,
      ')',
    ),

    profile_declaration: $ => seq(
      'profile',
      field('name', alias($.identifier, $.name)),
      '{',
      repeat($._profile_member),
      optional('}'),
    ),

    _profile_member: $ => choice(
      $.profile_projection,
      $.profile_standard,
      $.profile_style,
      $.profile_fallback,
    ),

    profile_projection: $ => seq(
      'projection',
      field('value', $.profile_value_name),
    ),

    profile_standard: $ => seq(
      'standard',
      field('value', $.profile_value_name),
    ),

    profile_style: $ => seq(
      'style',
      field('value', $.profile_value_name),
    ),

    profile_fallback: $ => seq(
      'fallback',
      field('value', $.profile_value_name),
    ),

    profile_value_name: _ => token(/[A-Za-z_][A-Za-z0-9_]*(?:-[A-Za-z_][A-Za-z0-9_]*)*/),

    binding_declaration: $ => seq(
      'binding',
      field('name', alias($.identifier, $.name)),
      '{',
      repeat($._binding_member),
      optional('}'),
    ),

    _binding_member: $ => choice(
      $.binding_profile,
      $.binding_priority,
      $.binding_select,
      $.binding_use_element,
      $.binding_variant,
    ),

    binding_profile: $ => seq(
      'profile',
      field('target', $.identifier),
    ),

    binding_priority: $ => seq(
      'priority',
      field('value', $.number),
    ),

    binding_select: $ => seq(
      'select',
      field('subject_kind', $.binding_subject_kind),
      'where',
      '{',
      repeat($.property_assignment),
      optional('}'),
    ),

    binding_subject_kind: _ => choice('device', 'function'),

    binding_use_element: $ => seq(
      'use',
      'element',
      field('identity', $.string),
      'version',
      field('version', $.string),
    ),

    binding_variant: $ => seq(
      'variant',
      field('value', $.string),
    ),

    device_declaration: $ => seq(
      'device',
      field('name', alias($.identifier, $.name)),
      '{',
      repeat($._device_member),
      optional('}'),
    ),

    _device_member: $ => choice(
      $.property_assignment,
      $.nested_port_declaration,
      $.function_declaration,
    ),

    function_declaration: $ => seq(
      'function',
      field('name', alias($.identifier, $.name)),
      '{',
      repeat(choice($.function_role, $.function_ports)),
      optional('}'),
    ),

    function_role: $ => seq(
      'role',
      field('value', $.identifier),
    ),

    function_ports: $ => seq(
      'ports',
      '(',
      commaSep1($.function_port_reference),
      ')',
    ),

    function_port_reference: $ => seq(
      $.identifier,
      optional(seq('.', $.identifier)),
    ),

    nested_port_declaration: $ => seq(
      'port',
      field('name', alias($.identifier, $.name)),
      '{',
      repeat($.property_assignment),
      optional('}'),
    ),

    port_declaration: $ => seq(
      'port',
      $.qualified_name,
      '{',
      repeat($.property_assignment),
      optional('}'),
    ),

    connect_declaration: $ => seq(
      'connect',
      field('name', alias($.identifier, $.name)),
      choice(
        seq(
          field('from', $.qualified_name),
          '->',
          field('to', $.qualified_name),
        ),
        seq(
          '{',
          repeat($.connect_group_edge),
          '}',
        ),
      ),
    ),

    connect_group_edge: $ => seq(
      field('name', alias($.identifier, $.name)),
      field('from', $.qualified_name),
      '->',
      field('to', $.qualified_name),
    ),

    layout_declaration: $ => seq(
      'layout',
      field('view_family', $.view_family_name),
      '{',
      repeat($.layout_statement),
      '}',
    ),

    view_family_name: _ => token(/[A-Za-z_][A-Za-z0-9_]*(?:-[A-Za-z_][A-Za-z0-9_]*)*/),

    layout_statement: $ => choice(
      $.place_statement,
      $.fixed_place_statement,
      $.align_statement,
      $.group_statement,
    ),

    place_statement: $ => seq(
      'place',
      field('subject', $.identifier),
      $.layout_placement_relation,
      field('target', $.identifier),
    ),

    fixed_place_statement: $ => seq(
      'place',
      field('subject', $.layout_subject_reference),
      'at',
      field('position', $.drawing_grid_position),
      'orientation',
      field('orientation', $.layout_orientation),
    ),

    layout_subject_reference: $ => seq(
      $.identifier,
      optional(seq('.', $.identifier)),
    ),

    drawing_grid_position: $ => seq(
      '(',
      field('column', $.positive_integer),
      ',',
      field('row', $.positive_integer),
      ')',
    ),

    positive_integer: _ => /[1-9][0-9]*/,

    layout_orientation: _ => choice(
      'horizontal',
      'vertical',
    ),

    layout_placement_relation: _ => choice(
      'near',
      'below',
    ),

    align_statement: $ => seq(
      'align',
      field('subject', $.identifier),
      'aligned-with',
      field('target', $.identifier),
      'axis',
      field('axis', $.layout_axis),
    ),

    layout_axis: _ => choice(
      'horizontal',
      'vertical',
    ),

    group_statement: $ => seq(
      'group',
      field('subject', $.identifier),
      'grouped-with',
      field('target', $.identifier),
    ),

    installation_declaration: $ => seq(
      token(prec(1, 'installation')),
      token(prec(1, 'cabinet')),
      field('name', alias($.identifier, $.name)),
      '{',
      repeat($._installation_member),
      optional('}'),
    ),

    _installation_member: $ => choice(
      $.installation_enclosure,
      $.installation_surface,
      $.installation_rail,
      $.installation_duct,
      $.installation_channel,
      $.installation_terminal_group,
      $.installation_mount,
      $.installation_route,
    ),

    installation_enclosure: $ => seq(
      'enclosure',
      field('name', alias($.identifier, $.name)),
      'size',
      field('size', $.length_tuple3),
    ),

    installation_surface: $ => seq(
      'surface',
      field('name', alias($.identifier, $.name)),
      'in',
      field('enclosure', $.identifier),
      'at',
      field('origin', $.length_point),
      'size',
      field('size', $.length_size),
      'accepts',
      field('mounting_types', $.identifier_list),
    ),

    installation_rail: $ => seq(
      'rail',
      field('name', alias($.identifier, $.name)),
      'on',
      field('surface', $.identifier),
      'at',
      field('origin', $.length_point),
      'length',
      field('length', $.length_literal),
      'orientation',
      field('orientation', $.layout_orientation),
      'mounting',
      field('mounting_type', $.identifier),
    ),

    installation_duct: $ => seq(
      'duct',
      field('name', alias($.identifier, $.name)),
      'in',
      field('enclosure', $.identifier),
      'at',
      field('origin', $.length_point),
      'size',
      field('size', $.length_size),
      'orientation',
      field('orientation', $.layout_orientation),
      'wall',
      field('wall', $.length_literal),
    ),

    installation_channel: $ => seq(
      'channel',
      field('name', alias($.identifier, $.name)),
      'in',
      field('duct', $.identifier),
      'at',
      field('origin', $.length_point),
      'size',
      field('size', $.length_size),
      'lanes',
      field('lanes', $.positive_integer),
      'margin',
      field('margin', $.length_literal),
    ),

    installation_terminal_group: $ => seq(
      'terminal-group',
      field('name', alias($.identifier, $.name)),
      'in',
      field('enclosure', $.identifier),
      'at',
      field('origin', $.length_point),
      'size',
      field('size', $.length_size),
      'orientation',
      field('orientation', $.layout_orientation),
      'accepts',
      field('mounting_types', $.identifier_list),
    ),

    installation_mount: $ => seq(
      'mount',
      field('device', $.identifier),
      'as',
      field('name', alias($.identifier, $.name)),
      'on',
      field('target', $.identifier),
      'at',
      field('origin', $.length_point),
      '{',
      repeat(choice(
        seq('footprint', field('footprint', $.length_tuple3)),
        seq('mounting', field('mounting_type', $.identifier)),
        seq('orientation', field('orientation', $.mount_orientation)),
        seq('allowed-orientations', field('allowed_orientations', $.mount_orientation_list)),
        seq('clearance', field('clearance', $.length_tuple4)),
        seq('compatible-containers', field('compatible_containers', $.identifier_list)),
      )),
      '}',
    ),

    mount_orientation: _ => choice('deg0', 'deg90', 'deg180', 'deg270'),

    mount_orientation_list: $ => seq(
      '[',
      commaSep1($.mount_orientation),
      ']',
    ),

    installation_route: $ => seq(
      'route',
      field('connection', $.identifier),
      'through',
      field('channels', $.identifier_list),
    ),

    identifier_list: $ => seq(
      '[',
      commaSep1($.identifier),
      ']',
    ),

    length_point: $ => seq(
      '(',
      $.length_literal,
      ',',
      $.length_literal,
      ')',
    ),

    length_size: $ => seq(
      '(',
      $.length_literal,
      ',',
      $.length_literal,
      ')',
    ),

    length_tuple4: $ => seq(
      '(',
      $.length_literal,
      ',',
      $.length_literal,
      ',',
      $.length_literal,
      ',',
      $.length_literal,
      ')',
    ),

    length_tuple3: $ => seq(
      '(',
      $.length_literal,
      ',',
      $.length_literal,
      ',',
      $.length_literal,
      ')',
    ),

    length_literal: _ => token(prec(1, /-?[0-9]+(?:\.[0-9]+)?mm/)),

    qualified_name: $ => seq(
      $.identifier,
      repeat(seq('.', $.identifier)),
    ),

    property_assignment: $ => seq(
      alias($.identifier, $.property_name),
      $._scalar_value,
    ),

    _scalar_value: $ => choice(
      $.identifier,
      $.string,
    ),

    identifier: $ => /[A-Za-z_][A-Za-z0-9_]*/,

    number: _ => /-?[0-9]+(?:\.[0-9]+)?/,

    string: $ => seq(
      '"',
      optional($.string_content),
      '"',
    ),

    string_content: $ => /[^"\r\n]+/,
  },
});

function commaSep1(rule) {
  return seq(rule, repeat(seq(',', rule)));
}
