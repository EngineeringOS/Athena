import assert from 'node:assert/strict';
import path from 'node:path';
import test from 'node:test';
import { createRequire } from 'node:module';

const require = createRequire(import.meta.url);

const {
    AthenaTreeSitterHighlightingService,
    athenaSemanticTokensLegend
} = await import('../lib/browser/athena-tree-sitter-highlighting-service.js');

test('AthenaTreeSitterHighlightingService produces semantic tokens from the checked-in Tree-sitter grammar assets', async () => {
    const service = new AthenaTreeSitterHighlightingService();
    const tokens = await service.provideDocumentSemanticTokens({
        getValue: () => 'system Demo { device PLC1 { type "Switch" } }'
    });

    assert.ok(tokens, 'expected semantic tokens for valid Athena source');
    assert.ok(tokens.data.length > 0, 'expected non-empty semantic token payload');

    const tokenKinds = decodeTokenKinds(tokens.data);
    assert.ok(tokenKinds.includes('keyword'));
    assert.ok(tokenKinds.includes('variable'));
    assert.ok(tokenKinds.includes('property'));
    assert.ok(tokenKinds.includes('string'));
    assert.equal(service.getLastFailureMessage(), undefined);
});

test('AthenaTreeSitterHighlightingService degrades to no semantic tokens when grammar assets fail to load', async () => {
    const service = new AthenaTreeSitterHighlightingService();
    const webTreeSitterRoot = path.dirname(require.resolve('web-tree-sitter'));
    service.setAssetLocator(assetFileName => {
        if (assetFileName === 'tree-sitter.wasm') {
            return path.join(webTreeSitterRoot, assetFileName);
        }
        return path.join(webTreeSitterRoot, 'missing-athena-asset', assetFileName);
    });

    const tokens = await service.provideDocumentSemanticTokens({
        getValue: () => 'system Demo { device PLC1 { type "Switch" } }'
    });

    assert.equal(tokens, undefined);
    assert.match(service.getLastFailureMessage() ?? '', /ENOENT|Failed to load|no such file/i);
});

function decodeTokenKinds(data) {
    const tokenKinds = [];
    for (let index = 3; index < data.length; index += 5) {
        tokenKinds.push(athenaSemanticTokensLegend.tokenTypes[data[index]]);
    }
    return tokenKinds;
}
