import * as monaco from '@theia/monaco-editor-core';

export const ATHENA_LANGUAGE_ID = 'athena';

const ATHENA_KEYWORDS = [
    'system',
    'device',
    'port',
    'connect'
] as const;

/**
 * Minimal Monaco language configuration for the current Athena DSL surface.
 *
 * This stays presentation-only in M5. The parser and package semantics remain owned by Athena LSP
 * and the JVM stack, while the frontend only improves authored-source readability and editing
 * affordances for `.athena` files.
 */
export const athenaLanguageConfiguration: monaco.languages.LanguageConfiguration = {
    brackets: [
        ['{', '}']
    ],
    autoClosingPairs: [
        { open: '{', close: '}' },
        { open: '"', close: '"' }
    ],
    surroundingPairs: [
        { open: '{', close: '}' },
        { open: '"', close: '"' }
    ],
    colorizedBracketPairs: [
        ['{', '}']
    ]
};

/**
 * Basic M5 tokenizer for `.athena` files.
 *
 * The token categories intentionally mirror the current DSL shape only: control keywords, strings,
 * delimiters/operators, qualified references, and plain identifiers. Richer semantic-token work is
 * deferred beyond M5.
 */
export const athenaMonarchLanguage: monaco.languages.IMonarchLanguage = {
    defaultToken: 'invalid',
    keywords: ATHENA_KEYWORDS,
    tokenizer: {
        root: [
            [/\s+/, 'white'],
            [/->/, 'operator'],
            [/[{}]/, 'delimiter.bracket'],
            [/\./, 'delimiter'],
            [/"/, { token: 'string.quote', bracket: '@open', next: '@string' }],
            [/[A-Za-z_][A-Za-z0-9_]*(?:\.[A-Za-z_][A-Za-z0-9_]*)+/, 'type.identifier'],
            [/[A-Za-z_][A-Za-z0-9_]*/, {
                cases: {
                    '@keywords': 'keyword',
                    '@default': 'identifier'
                }
            }]
        ],
        string: [
            [/[^"\\]+/, 'string'],
            [/\\./, 'string.escape.invalid'],
            [/"/, { token: 'string.quote', bracket: '@close', next: '@pop' }]
        ]
    }
};
