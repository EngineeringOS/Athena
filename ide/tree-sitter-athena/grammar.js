// Athena Tree-sitter grammar — SYNTAX UX ONLY.
//
// AD-107: Tree-sitter owns syntax UX only (highlighting/structure), never semantic truth.
// AD-110: this grammar mirrors the current M18 package/import plus M17 system syntax subset.
// `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt` /
// `AthenaLanguageParser.kt`: optional package, repeated imports, one system block, and the
// existing device/port/connect, qualified-name, string, identifier, and property syntax.
//
// Do NOT add aliases, wildcards, visibility, comments, numeric literals, or expressions. Widening this
// grammar beyond the frozen subset (AD-104) is an explicit future-story decision, not an
// incidental addition.
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
  ],

  rules: {
    source_file: $ => seq(
      optional(choice($.package_declaration, $.incomplete_package_declaration)),
      repeat(choice($.import_declaration, $.incomplete_import_declaration)),
      $.system_declaration,
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
    ),

    device_declaration: $ => seq(
      'device',
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
      field('from', $.qualified_name),
      '->',
      field('to', $.qualified_name),
    ),

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

    string: $ => seq(
      '"',
      optional($.string_content),
      '"',
    ),

    string_content: $ => /[^"]+/,
  },
});
