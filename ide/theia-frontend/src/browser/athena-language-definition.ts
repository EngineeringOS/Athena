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
    'out',
    'bidirectional'
] as const;

const ATHENA_RELATIONSHIP_KEYWORDS = [
    'connect'
] as const;

const ATHENA_FUNCTION_KEYWORDS = [
    'function',
    'role',
    'ports'
] as const;

const ATHENA_LAYOUT_KEYWORDS = [
    'layout',
    'place',
    'align',
    'group',
    'near',
    'below',
    'axis',
    'at',
    'orientation',
    'vertical',
    'horizontal'
] as const;

const ATHENA_REPRESENTATION_KEYWORDS = [
    'symbol',
    'element',
    'identity',
    'version',
    'graphic',
    'svg',
    'anchor',
    'primitiveRef',
    'accepts',
    'child',
    'translate',
    'rotate',
    'scale',
    'zOrder',
    'export'
] as const;

const ATHENA_PRIMITIVE_KEYWORDS = [
    'bounds',
    'line',
    'polyline',
    'points',
    'arc',
    'center',
    'radius',
    'sweep',
    'circle',
    'rectangle',
    'label',
    'from',
    'to',
    'size',
    'style',
    'point'
] as const;

const ATHENA_PROFILE_KEYWORDS = [
    'profile',
    'projection',
    'standard',
    'fallback',
    'fail-closed'
] as const;

const ATHENA_BINDING_KEYWORDS = [
    'binding',
    'priority',
    'select',
    'where',
    'use',
    'variant'
] as const;

const ATHENA_KEYWORDS = [
    ...ATHENA_DECLARATION_KEYWORDS,
    ...ATHENA_PORT_KEYWORDS,
    ...ATHENA_RELATIONSHIP_KEYWORDS,
    ...ATHENA_FUNCTION_KEYWORDS,
    ...ATHENA_LAYOUT_KEYWORDS,
    ...ATHENA_REPRESENTATION_KEYWORDS,
    ...ATHENA_PRIMITIVE_KEYWORDS,
    ...ATHENA_PROFILE_KEYWORDS,
    ...ATHENA_BINDING_KEYWORDS
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
    functionKeywords: ATHENA_FUNCTION_KEYWORDS,
    layoutKeywords: ATHENA_LAYOUT_KEYWORDS,
    representationKeywords: ATHENA_REPRESENTATION_KEYWORDS,
    primitiveKeywords: ATHENA_PRIMITIVE_KEYWORDS,
    profileKeywords: ATHENA_PROFILE_KEYWORDS,
    bindingKeywords: ATHENA_BINDING_KEYWORDS,
    tokenizer: {
        root: [
            [/\s+/, 'white'],
            [/->/, 'operator.athena-relationship'],
            [/(?:aligned-with|grouped-with)\b/, 'operator.athena-layout'],
            [/fail-closed\b/, 'keyword.athena-profile'],
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
                    '@functionKeywords': 'keyword.athena-function',
                    '@layoutKeywords': 'keyword.athena-layout',
                    '@representationKeywords': 'keyword.athena-representation',
                    '@primitiveKeywords': 'keyword.athena-primitive',
                    '@profileKeywords': 'keyword.athena-profile',
                    '@bindingKeywords': 'keyword.athena-binding',
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
