// Athena Tree-sitter grammar — SYNTAX UX ONLY.
//
// AD-107: Tree-sitter owns syntax UX only (highlighting/structure), never semantic truth.
// AD-110: this grammar is frozen to the current M17 supported syntax subset and must mirror
// `kernel/language/src/main/kotlin/com/engineeringood/athena/language/AthenaLanguageModel.kt` /
// `AthenaLanguageParser.kt` exactly: one top-level `system <name> { ... }` block, `device`,
// `port`, and `connect` declarations, dotted qualified names (`owner.port`), string literals,
// bare identifiers, and `name value` property assignments — no more, no less.
//
// Do NOT add comments, numeric literals, expressions, or `import` syntax here. Widening this
// grammar beyond the frozen subset (AD-104) is an explicit future-story decision, not an
// incidental addition. See Story 5.3 / `kernel/language/docs/future-syntax-landing-zone.md`.
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
    source_file: $ => $.system_declaration,

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
