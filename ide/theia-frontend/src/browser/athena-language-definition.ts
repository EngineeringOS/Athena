import * as monaco from '@theia/monaco-editor-core';

export const ATHENA_LANGUAGE_ID = 'athena';

const ATHENA_DECLARATION_KEYWORDS = [
    'package',
    'import',
    'system',
    'entity',
    'concept',
    'structure',
    'display'
] as const;

const ATHENA_FUNCTION_KEYWORDS = ['function', 'role'] as const;

const ATHENA_PORT_KEYWORDS = [
    'port',
    'direction',
    'flow',
    'in',
    'out',
    'bidirectional',
    'passive',
    'minimum',
    'maximum',
    'unbounded',
    'designationType',
    'designation'
] as const;

const ATHENA_RELATIONSHIP_KEYWORDS = ['connect', 'to'] as const;

const ATHENA_KEYWORDS = [
    ...ATHENA_DECLARATION_KEYWORDS,
    ...ATHENA_FUNCTION_KEYWORDS,
    ...ATHENA_PORT_KEYWORDS,
    ...ATHENA_RELATIONSHIP_KEYWORDS
] as const;

export const athenaLanguageConfiguration: monaco.languages.LanguageConfiguration = {
    brackets: [['{', '}'], ['[', ']']],
    autoClosingPairs: [
        { open: '{', close: '}' },
        { open: '[', close: ']' },
        { open: '"', close: '"' }
    ],
    surroundingPairs: [
        { open: '{', close: '}' },
        { open: '[', close: ']' },
        { open: '"', close: '"' }
    ],
    colorizedBracketPairs: [['{', '}'], ['[', ']']]
};
export const athenaMonarchLanguage: monaco.languages.IMonarchLanguage = {
    defaultToken: 'invalid',
    keywords: ATHENA_KEYWORDS,
    declarationKeywords: ATHENA_DECLARATION_KEYWORDS,
    functionKeywords: ATHENA_FUNCTION_KEYWORDS,
    portKeywords: ATHENA_PORT_KEYWORDS,
    relationshipKeywords: ATHENA_RELATIONSHIP_KEYWORDS,
    tokenizer: {
        root: [
            [/\s+/, 'white'],
            [/@[A-Za-z_][A-Za-z0-9_.-]*/, 'type.identifier'],
            [/[{}\[\]]/, 'delimiter.bracket'],
            [/\./, 'delimiter'],
            [/-?[0-9]+(?:\.[0-9]+)?/, 'number'],
            [/"/, { token: 'string.quote', bracket: '@open', next: '@string' }],
            [/[A-Za-z_][A-Za-z0-9_]*(?:[.-][A-Za-z_][A-Za-z0-9_]*)+/, 'type.identifier'],
            [/[A-Za-z_][A-Za-z0-9_]*/, {
                cases: {
                    '@declarationKeywords': 'keyword.athena-declaration',
                    '@functionKeywords': 'keyword.athena-function',
                    '@portKeywords': 'keyword.athena-port',
                    '@relationshipKeywords': 'keyword.athena-relationship',
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
