// Athena Tree-sitter grammar — SYNTAX UX ONLY.
//
// AD-107: Tree-sitter owns syntax UX only (highlighting/structure), never semantic truth.
// AD-110: this grammar mirrors the current M18 package/import plus M17 system syntax subset,
// M23 system-scoped layout-block admission, Entity- and Function-owned ports, typed binary connection
// authoring, the frozen M34 native Symbol/Element syntax subset, M35 installation cabinet syntax,
// M37 grouped connectivity Interface syntax, external evidence mapping syntax, and Projection Policy syntax.
// `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt` /
// `AthenaLanguageParser.kt`: optional package, repeated imports, one system block, and the
// existing entity/port/connect, layout, qualified-name, exact scalar, property, and M34
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

  // Enables Tree-sitter's keyword-extraction optimization so the `system`/`entity`/`port`/
  // `connect`/`to`-adjacent literal tokens below take priority over the generic identifier
  // token when they match the same text, mirroring how the handwritten JVM tokenizer treats
  // keywords as identifier lexemes matched positionally rather than as reserved words.
  word: $ => $.identifier,

  conflicts: $ => [
    [$.entity_declaration],
    [$.port_declaration],
    [$.nested_port_declaration],
    [$.function_declaration],
    [$.element_child],
    [$.binding_declaration],
    [$.binding_select],
    [$.installation_declaration],
    [$.interface_declaration],
    [$.interface_ports],
    [$.interface_port_member],
    [$.evidence_declaration],
    [$.projection_policy_declaration],
    [$.connection_specification_declaration],
    [$.net_declaration],
  ],

  rules: {
    source_file: $ => seq(
      optional(choice($.package_declaration, $.incomplete_package_declaration)),
      repeat(choice($.import_declaration, $.incomplete_import_declaration)),
      choice(
        $.sheet_companion,
        $.system_declaration,
        $.domain_declaration,
        repeat1($._representation_declaration),
      ),
    ),

    sheet_companion: $ => seq(
      'sheet',
      field('name', choice($.string, $.identifier)),
      '{',
      repeat(choice($.sheet_page, $.sheet_grid, $.sheet_title, $.sheet_placement)),
      optional('}'),
    ),

    sheet_page: $ => seq('page', 'format', field('format', $.identifier), field('orientation', choice('landscape', 'portrait'))),

    sheet_grid: $ => seq(
      'grid', ':', field('columns', $.positive_integer), '*', field('rows', $.positive_integer),
      'cell', ':', field('cell', $.positive_integer),
    ),

    sheet_title: $ => seq('title', field('value', $.string)),

    sheet_placement: $ => seq(
      field('occurrence', choice($.string, $.identifier)),
      'at', field('cell', $.sheet_cell),
      optional(seq('micro', field('micro', $.sheet_micro))),
      optional('lock'),
    ),

    sheet_cell: $ => alias($.identifier, $.sheet_cell),

    sheet_micro: $ => seq('(', $.positive_integer, ',', $.positive_integer, ')'),

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

    domain_declaration: $ => seq(
      'domain',
      field('name', alias($.identifier, $.name)),
      '{',
      repeat($.knowledge_declaration),
      optional('}'),
    ),

    knowledge_declaration: $ => choice(
      $.knowledge_concept,
      $.knowledge_part,
      $.knowledge_capability,
      $.knowledge_relationship,
      $.knowledge_flow,
      $.knowledge_dimension,
      $.knowledge_unit,
      $.knowledge_formula,
      $.knowledge_constraint,
    ),

    knowledge_concept: $ => seq('concept', field('name', alias($.identifier, $.name)), '{', repeat($.property_assignment), '}'),
    knowledge_part: $ => seq('part', field('name', alias($.identifier, $.name)), 'concept', $.qualified_name, '{', repeat($.property_assignment), '}'),
    knowledge_capability: $ => seq('capability', field('name', alias($.identifier, $.name)), choice('provides', 'requires'), '{', repeat($.property_assignment), '}'),
    knowledge_relationship: $ => seq('relationship', field('name', alias($.identifier, $.name)), '{', repeat(choice($.knowledge_role, $.property_assignment)), '}'),
    knowledge_role: $ => seq('role', field('name', alias($.identifier, $.name)), 'level', choice('entity', 'function', 'port'), optional(seq('requires', $.qualified_name))),
    knowledge_flow: $ => seq('flow', field('name', alias($.identifier, $.name)), 'relationship', $.qualified_name, 'source', $.identifier, 'sink', $.identifier, optional(seq('medium', $.qualified_name))),
    knowledge_dimension: $ => seq('dimension', field('name', alias($.identifier, $.name)), 'bases', '[', $.qualified_name, repeat(seq(',', $.qualified_name)), ']'),
    knowledge_unit: $ => seq('unit', field('name', alias($.identifier, $.name)), 'dimension', $.qualified_name, 'scale', $._scalar_value, optional(seq('offset', $._scalar_value))),
    knowledge_formula: $ => seq('formula', field('name', alias($.identifier, $.name)), '=', $.knowledge_expression),
    knowledge_constraint: $ => seq('constraint', field('name', alias($.identifier, $.name)), $.knowledge_predicate),
    knowledge_predicate: $ => choice(
      seq($.knowledge_expression, choice('>', '>=', '<', '<=', '=', '!='), $.knowledge_expression),
      seq($.knowledge_expression, 'in', $.knowledge_interval),
      seq(choice('all', 'any'), '[', $.knowledge_predicate, repeat(seq(',', $.knowledge_predicate)), ']'),
    ),
    knowledge_interval: $ => seq(choice('[', '('), $.knowledge_expression, ',', $.knowledge_expression, choice(']', ')')),
    knowledge_expression: $ => choice(
      $.number,
      $.qualified_name,
      seq(choice('min', 'max', 'round-up'), '(', $.knowledge_expression, ',', $.knowledge_expression, ')'),
      seq('(', $.knowledge_expression, ')'),
      prec.left(2, seq($.knowledge_expression, choice('*', '/'), $.knowledge_expression)),
      prec.left(1, seq($.knowledge_expression, choice('+', '-'), $.knowledge_expression)),
    ),

    declaration: $ => choice(
      $.entity_declaration,
      $.port_declaration,
      $.connect_declaration,
      $.net_declaration,
      $.connection_specification_declaration,
      $.relation_declaration,
      $.evidence_declaration,
      $.projection_policy_declaration,
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

    binding_subject_kind: _ => choice('entity', 'function'),

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

    entity_declaration: $ => seq(
      'entity',
      field('name', alias($.identifier, $.name)),
      '{',
      repeat($._entity_member),
      optional('}'),
    ),

    _entity_member: $ => choice(
      $.interface_declaration,
      $.property_assignment,
      $.nested_port_declaration,
      $.function_declaration,
      $.structure_assignment,
    ),

    interface_declaration: $ => seq(
      token(prec(1, 'interface')),
      field('name', alias($.identifier, $.name)),
      '{',
      repeat(choice($.property_assignment, $.interface_ports)),
      optional('}'),
    ),

    interface_ports: $ => seq(
      token(prec(1, 'ports')),
      '{',
      repeat($.interface_port_member),
      optional('}'),
    ),

    interface_port_member: $ => seq(
      field('name', alias($.identifier, $.name)),
      optional(seq(
        '{',
        repeat($.property_assignment),
        optional('}'),
      )),
    ),

    function_declaration: $ => seq(
      'function',
      field('name', alias($.identifier, $.name)),
      '{',
      repeat(choice($.function_role, $.nested_port_declaration)),
      optional('}'),
    ),

    function_role: $ => seq(
      'role',
      field('value', $.qualified_name),
    ),

    structure_assignment: $ => seq(
      'structure',
      field('aspect', $.qualified_name),
      field('value', $._scalar_value),
      optional(seq('display', field('display', $.string))),
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
      field('kind', $.connection_kind),
      field('from', $.qualified_name),
      $._connection_separator,
      field('to', $.qualified_name),
      optional(seq('{', repeat($.property_assignment), '}')),
    ),

    net_declaration: $ => seq(
      'net',
      field('name', alias($.identifier, $.name)),
      field('kind', $.connection_kind),
      '{',
      repeat(choice(
        seq('source', $.qualified_name),
        seq('sink', $.qualified_name),
        seq('pass', $.qualified_name),
        seq(choice('potential', 'signal'), $.qualified_name),
        $.property_assignment,
      )),
      optional('}'),
    ),

    connection_specification_declaration: $ => seq(
      'connection-spec',
      field('scope', choice('project', 'potential', 'signal', 'net', 'connection')),
      optional($.qualified_name),
      '{',
      repeat($.property_assignment),
      optional('}'),
    ),

    connection_kind: $ => seq(
      $.identifier,
      optional(seq('-', $.identifier)),
    ),

    relation_declaration: $ => seq(
      field('word', alias($.identifier, $.relation_word)),
      field('from', $.qualified_name),
      $._connection_separator,
      field('targets', $.relation_targets),
    ),

    _connection_separator: _ => choice('to', '->'),

    relation_targets: $ => choice(
      $.qualified_name,
      seq('[', commaSep1($.qualified_name), ']'),
    ),

    evidence_declaration: $ => seq(
      token(prec(1, 'evidence')),
      field('name', alias($.identifier, $.name)),
      '{',
      repeat($._evidence_member),
      optional('}'),
    ),

    _evidence_member: $ => choice(
      $.evidence_namespace,
      $.evidence_reference,
      $.evidence_subject,
      $.evidence_provenance,
    ),

    evidence_namespace: $ => seq(
      'namespace',
      field('value', $.identifier),
    ),

    evidence_reference: $ => seq(
      'reference',
      field('value', $.string),
    ),

    evidence_subject: $ => seq(
      'subject',
      field('kind', $.evidence_subject_kind),
      field('target', $.qualified_name),
    ),

    evidence_subject_kind: _ => choice(
      'contract',
      'interface',
      'port',
      'relation-contract',
      'route-policy',
    ),

    evidence_provenance: $ => seq(
      'provenance',
      field('value', $.string),
    ),

    projection_policy_declaration: $ => seq(
      token(prec(1, 'projection')),
      field('name', alias($.identifier, $.name)),
      '{',
      repeat($._projection_policy_member),
      optional('}'),
    ),

    _projection_policy_member: $ => choice(
      $.projection_policy_target,
      $.projection_policy_layout,
      $.projection_policy_drawing_profile,
      $.projection_policy_route_quality,
      $.projection_policy_proof,
      $.projection_policy_forbidden_truth,
    ),

    projection_policy_target: $ => seq(
      'target',
      field('value', $.profile_value_name),
    ),

    projection_policy_layout: $ => seq(
      'layout',
      field('value', $.profile_value_name),
    ),

    projection_policy_drawing_profile: $ => seq(
      'drawingProfile',
      field('value', $.identifier),
    ),

    projection_policy_route_quality: $ => seq(
      'routeQuality',
      field('value', $.identifier),
    ),

    projection_policy_proof: $ => seq(
      'proof',
      field('value', $.profile_value_name),
    ),

    projection_policy_forbidden_truth: $ => prec.right(choice(
      seq('port', $.qualified_name, optional($.identifier)),
      seq('connect', $.identifier, $.qualified_name, 'to', $.qualified_name),
      seq('evidence', optional($.identifier)),
      seq('anchor', optional($.identifier)),
    )),

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
      $.quantity,
      $.boolean,
      $.reference_value,
      $.identifier,
      $.string,
      $.number,
    ),

    quantity: $ => seq(
      field('value', $.number),
      '[',
      field('unit', $.qualified_name),
      ']',
    ),

    boolean: _ => choice('true', 'false'),

    reference_value: $ => seq(
      '@',
      field('target', $.qualified_name),
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
