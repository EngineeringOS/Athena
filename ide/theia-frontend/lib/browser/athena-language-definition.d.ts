import * as monaco from '@theia/monaco-editor-core';
export declare const ATHENA_LANGUAGE_ID = "athena";
/**
 * Minimal Monaco language configuration for the current Athena DSL surface.
 *
 * This stays presentation-only in M5. The parser and package semantics remain owned by Athena LSP
 * and the JVM stack, while the frontend only improves authored-source readability and editing
 * affordances for `.athena` files.
 */
export declare const athenaLanguageConfiguration: monaco.languages.LanguageConfiguration;
/**
 * Basic M5 tokenizer for `.athena` files.
 *
 * The token categories intentionally mirror the current DSL shape only: control keywords, strings,
 * delimiters/operators, qualified references, and plain identifiers. Richer semantic-token work is
 * deferred beyond M5.
 */
export declare const athenaMonarchLanguage: monaco.languages.IMonarchLanguage;
//# sourceMappingURL=athena-language-definition.d.ts.map