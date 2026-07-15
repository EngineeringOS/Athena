import * as monaco from '@theia/monaco-editor-core';
import { injectable } from '@theia/core/shared/inversify';

/**
 * Tree-sitter-backed syntax highlighting adapter — SYNTAX UX ONLY (AD-107).
 *
 * This service is a thin adapter: it does not re-implement grammar rules (it consumes the
 * checked-in `tree-sitter-athena.wasm` + `queries/highlights.scm` from Story `3.1`'s
 * `@engineeringood/athena-tree-sitter-grammar` package) and it must never fabricate semantic
 * meaning from capture names. It degrades gracefully — falling back to the existing Monarch
 * highlighting only — if the `.wasm` fails to load or a parse throws (AD-108: this path must
 * never emit diagnostics, markers, or problem-panel entries).
 */

type WebTreeSitterModule = typeof import('web-tree-sitter');
type TreeSitterLanguage = import('web-tree-sitter').Language;
type TreeSitterQuery = import('web-tree-sitter').Query;

const ATHENA_SEMANTIC_TOKEN_TYPES = ['keyword', 'namespace', 'operator', 'variable', 'property', 'string'] as const;
export type AthenaSemanticTokenType = typeof ATHENA_SEMANTIC_TOKEN_TYPES[number];

export const athenaSemanticTokensLegend: monaco.languages.SemanticTokensLegend = {
    tokenTypes: [...ATHENA_SEMANTIC_TOKEN_TYPES],
    tokenModifiers: []
};

const CAPTURE_NAME_TO_TOKEN_TYPE: Readonly<Record<string, AthenaSemanticTokenType>> = {
    keyword: 'keyword',
    namespace: 'namespace',
    operator: 'operator',
    variable: 'variable',
    property: 'property',
    string: 'string'
};

type AthenaTreeSitterContext = {
    module: WebTreeSitterModule;
    language: TreeSitterLanguage;
    query: TreeSitterQuery;
};

export type AthenaTreeSitterAssetLocator = (assetFileName: string) => Promise<string> | string;

/**
 * Default asset resolution: bare relative file names, matching the copied layout under
 * `ide/theia-product/lib/frontend/` (see `scripts/copy-tree-sitter-assets.js`), the same
 * directory the running product already serves worker/asset files from (e.g. `editor.worker.js`).
 * In a Node test/host context (no `window`), assets are resolved from disk instead.
 */
function defaultAssetLocator(assetFileName: string): string {
    if (typeof window === 'undefined') {
        return resolveNodeAssetPath(assetFileName);
    }
    return assetFileName;
}

function resolveNodeAssetPath(assetFileName: string): string {
    // eslint-disable-next-line @typescript-eslint/no-require-imports
    const nodeRequire: NodeJS.Require = require;
    const path: typeof import('node:path') = nodeRequire('node:path');
    if (assetFileName === 'tree-sitter-athena.wasm' || assetFileName === 'athena-tree-sitter-highlights.scm') {
        const grammarPackageJson = nodeRequire.resolve('@engineeringood/athena-tree-sitter-grammar/package.json');
        const grammarRoot = path.dirname(grammarPackageJson);
        return assetFileName === 'athena-tree-sitter-highlights.scm'
            ? path.join(grammarRoot, 'queries', 'highlights.scm')
            : path.join(grammarRoot, assetFileName);
    }
    // web-tree-sitter's own runtime wasm.
    const webTreeSitterEntry = nodeRequire.resolve('web-tree-sitter');
    return path.join(path.dirname(webTreeSitterEntry), assetFileName);
}

@injectable()
export class AthenaTreeSitterHighlightingService {
    protected assetLocator: AthenaTreeSitterAssetLocator = defaultAssetLocator;
    protected initialization?: Promise<AthenaTreeSitterContext | undefined>;
    protected lastFailureMessage: string | undefined;

    getLegend(): monaco.languages.SemanticTokensLegend {
        return athenaSemanticTokensLegend;
    }

    /** Test/host seam to override how asset file names resolve to loadable URLs/paths. */
    setAssetLocator(locator: AthenaTreeSitterAssetLocator): void {
        this.assetLocator = locator;
        this.initialization = undefined;
    }

    async provideDocumentSemanticTokens(model: monaco.editor.ITextModel): Promise<monaco.languages.SemanticTokens | undefined> {
        const context = await this.ensureInitialized();
        if (!context) {
            return undefined;
        }

        const parser = new context.module.Parser();
        try {
            parser.setLanguage(context.language);
            const text = model.getValue();
            const tree = parser.parse(text);
            if (!tree) {
                return undefined;
            }
            try {
                const captures = context.query.captures(tree.rootNode);
                return { data: this.toSemanticTokensData(captures) };
            } finally {
                tree.delete();
            }
        } catch (error) {
            this.reportFailure(error);
            return undefined;
        } finally {
            parser.delete();
        }
    }

    protected toSemanticTokensData(
        captures: import('web-tree-sitter').QueryCapture[]
    ): Uint32Array {
        type PositionedToken = { line: number; startChar: number; length: number; tokenType: number };
        const tokens: PositionedToken[] = [];

        for (const capture of captures) {
            const tokenType = CAPTURE_NAME_TO_TOKEN_TYPE[capture.name];
            if (tokenType === undefined) {
                // Unclassified/default: skip rather than fabricate a semantic guess (Story 3.3).
                continue;
            }
            const node = capture.node;
            if (node.startPosition.row !== node.endPosition.row) {
                // Multi-line captures do not occur in the current grammar (no multi-line strings);
                // skip defensively rather than emit an incorrect single-line token span.
                continue;
            }
            tokens.push({
                line: node.startPosition.row,
                startChar: node.startPosition.column,
                length: node.endPosition.column - node.startPosition.column,
                tokenType: athenaSemanticTokensLegend.tokenTypes.indexOf(tokenType)
            });
        }

        tokens.sort((a, b) => (a.line - b.line) || (a.startChar - b.startChar));

        const data = new Uint32Array(tokens.length * 5);
        let previousLine = 0;
        let previousStartChar = 0;
        tokens.forEach((token, index) => {
            const deltaLine = token.line - previousLine;
            const deltaStartChar = deltaLine === 0 ? token.startChar - previousStartChar : token.startChar;
            const offset = index * 5;
            data[offset] = deltaLine;
            data[offset + 1] = deltaStartChar;
            data[offset + 2] = token.length;
            data[offset + 3] = token.tokenType;
            data[offset + 4] = 0;
            previousLine = token.line;
            previousStartChar = token.startChar;
        });
        return data;
    }

    protected async ensureInitialized(): Promise<AthenaTreeSitterContext | undefined> {
        if (!this.initialization) {
            this.initialization = this.initialize().catch(error => {
                this.reportFailure(error);
                return undefined;
            });
        }
        return this.initialization;
    }

    protected async initialize(): Promise<AthenaTreeSitterContext> {
        // eslint-disable-next-line @typescript-eslint/no-require-imports
        const module: WebTreeSitterModule = await import('web-tree-sitter');
        await module.Parser.init({
            locateFile: (scriptName: string) => this.resolveAssetSync(scriptName)
        });

        const grammarWasmPath = await this.assetLocator('tree-sitter-athena.wasm');
        const language = await module.Language.load(grammarWasmPath);

        const highlightsQuerySource = await this.loadHighlightsQuerySource();
        const query = new module.Query(language, highlightsQuerySource);

        return { module, language, query };
    }

    protected resolveAssetSync(scriptName: string): string {
        // `Parser.init`'s `locateFile` must return synchronously. Custom locators may still use an
        // async path for grammar/query assets, so fall back to the raw file name in that case.
        const result = this.assetLocator(scriptName);
        return typeof result === 'string' ? result : scriptName;
    }

    protected async loadHighlightsQuerySource(): Promise<string> {
        const highlightsPath = await this.assetLocator('athena-tree-sitter-highlights.scm');
        if (typeof window === 'undefined') {
            // eslint-disable-next-line @typescript-eslint/no-require-imports
            const nodeRequire: NodeJS.Require = require;
            const fs: typeof import('node:fs') = nodeRequire('node:fs');
            return fs.readFileSync(highlightsPath, 'utf8');
        }
        const response = await fetch(highlightsPath);
        if (!response.ok) {
            throw new Error(`Failed to load Athena Tree-sitter highlight query from ${highlightsPath}`);
        }
        return response.text();
    }

    protected reportFailure(error: unknown): void {
        this.lastFailureMessage = error instanceof Error ? error.message : String(error);
    }

    /** Exposed for tests/diagnostics; never surfaced as an editor diagnostic/marker (AD-108). */
    getLastFailureMessage(): string | undefined {
        return this.lastFailureMessage;
    }
}
