import * as monaco from '@theia/monaco-editor-core';

export const ATHENA_LANGUAGE_ID = 'athena';

const ATHENA_DECLARATION_KEYWORDS = [
    'package',
    'import',
    'system',
    'device',
    'port',
    'type',
    'model'
] as const;

const ATHENA_PORT_KEYWORDS = [
    'direction',
    'signal',
    'in',
    'out'
] as const;

const ATHENA_RELATIONSHIP_KEYWORDS = [
    'connect'
] as const;

const ATHENA_LAYOUT_KEYWORDS = [
    'layout',
    'place',
    'align',
    'group',
    'near',
    'below',
    'axis',
    'vertical',
    'horizontal'
] as const;

const ATHENA_KEYWORDS = [
    ...ATHENA_DECLARATION_KEYWORDS,
    ...ATHENA_PORT_KEYWORDS,
    ...ATHENA_RELATIONSHIP_KEYWORDS,
    ...ATHENA_LAYOUT_KEYWORDS
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
    declarationKeywords: ATHENA_DECLARATION_KEYWORDS,
    portKeywords: ATHENA_PORT_KEYWORDS,
    relationshipKeywords: ATHENA_RELATIONSHIP_KEYWORDS,
    layoutKeywords: ATHENA_LAYOUT_KEYWORDS,
    tokenizer: {
        root: [
            [/\s+/, 'white'],
            [/->/, 'operator.athena-relationship'],
            [/(?:aligned-with|grouped-with)\b/, 'operator.athena-layout'],
            [/[{}]/, 'delimiter.bracket'],
            [/\./, 'delimiter'],
            [/"/, { token: 'string.quote', bracket: '@open', next: '@string' }],
            [/[A-Za-z_][A-Za-z0-9_]*(?:\.[A-Za-z_][A-Za-z0-9_]*)+/, 'type.identifier'],
            [/[A-Za-z_][A-Za-z0-9_]*(?:-[A-Za-z_][A-Za-z0-9_]*)+/, 'type.identifier'],
            [/[A-Za-z_][A-Za-z0-9_]*/, {
                cases: {
                    '@declarationKeywords': 'keyword.athena-declaration',
                    '@portKeywords': 'keyword.athena-port',
                    '@relationshipKeywords': 'keyword.athena-relationship',
                    '@layoutKeywords': 'keyword.athena-layout',
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
