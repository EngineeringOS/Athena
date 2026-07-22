const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { spawnSync } = require('node:child_process');

const {
    AthenaTreeSitterHighlightingService,
    athenaSemanticTokensLegend
} = require('@engineeringood/athena-theia-frontend/lib/browser/athena-tree-sitter-highlighting-service.js');

const productRoot = path.resolve(__dirname, '..');
const frontendAssetRoot = path.join(productRoot, 'lib', 'frontend');
const workspaceRoot = path.resolve(productRoot, '..', '..');
const webTreeSitterRoot = path.dirname(require.resolve('web-tree-sitter'));

async function main() {
    verifyDesktopStartup();
    verifyBundledAssetsExist();
    await verifyBundledTreeSitterTokens();
    console.log('Athena Tree-sitter highlighting smoke passed.');
}

function verifyDesktopStartup() {
    const verifierPath = path.join(__dirname, 'verify-athena-start.js');
    const result = spawnSync(process.execPath, [verifierPath], {
        cwd: productRoot,
        env: process.env,
        stdio: 'inherit',
        windowsHide: true
    });
    if (result.status !== 0) {
        throw new Error(`Athena Tree-sitter smoke could not complete desktop startup verification. exitCode=${result.status}`);
    }
}

function verifyBundledAssetsExist() {
    const expectedAssets = [
        path.join(frontendAssetRoot, 'web-tree-sitter.wasm'),
        path.join(frontendAssetRoot, 'tree-sitter-athena.wasm'),
        path.join(frontendAssetRoot, 'athena-tree-sitter-highlights.scm')
    ];
    for (const assetPath of expectedAssets) {
        if (!fs.existsSync(assetPath)) {
            throw new Error(`Athena Tree-sitter smoke could not find bundled frontend asset: ${assetPath}`);
        }
    }
}

async function verifyBundledTreeSitterTokens() {
    const service = new AthenaTreeSitterHighlightingService();
    service.setAssetLocator(assetFileName => {
        if (assetFileName === 'tree-sitter-athena.wasm' || assetFileName === 'athena-tree-sitter-highlights.scm') {
            return path.join(frontendAssetRoot, assetFileName);
        }
        return path.join(webTreeSitterRoot, assetFileName);
    });

    const validSource = fs.readFileSync(
        path.join(workspaceRoot, 'examples', 'm4', 'open-repository-proof', 'src', 'factory-line.athena'),
        'utf8'
    );
    const m31Source = fs.readFileSync(
        path.join(workspaceRoot, 'examples', 'm31', 'sample-project', 'src', '01-governed-authoring-customer-source.athena'),
        'utf8'
    );
    const incompleteSource = fs.readFileSync(
        path.join(workspaceRoot, 'examples', 'm17', 'invalid-and-incomplete-proof', 'incomplete-brace.athena'),
        'utf8'
    );

    const validTokens = await service.provideDocumentSemanticTokens({ getValue: () => validSource });
    assert.ok(validTokens, 'expected bundled Tree-sitter assets to produce semantic tokens for a valid Athena source');
    assert.ok(validTokens.data.length > 0, 'expected bundled Tree-sitter semantic token payload to be non-empty');

    const tokenKinds = decodeTokenKinds(validTokens.data);
    assert.ok(
        tokenKinds.includes('athenaDeclarationKeyword'),
        'expected bundled Tree-sitter semantic tokens to include declaration keywords'
    );
    assert.ok(tokenKinds.includes('variable'), 'expected bundled Tree-sitter semantic tokens to include variables');
    assert.ok(tokenKinds.includes('property'), 'expected bundled Tree-sitter semantic tokens to include properties');
    assert.equal(service.getLastFailureMessage(), undefined, 'expected no Tree-sitter load failure for valid source');

    const m31Tokens = await service.provideDocumentSemanticTokens({ getValue: () => m31Source });
    assert.ok(m31Tokens, 'expected bundled Tree-sitter assets to produce semantic tokens for M31 source');
    const m31TokenKinds = decodeTokenKinds(m31Tokens.data);
    assert.ok(m31TokenKinds.includes('athenaDeclarationKeyword'), 'expected M31 source to include declaration keywords');
    assert.ok(m31TokenKinds.includes('athenaPortKeyword'), 'expected M31 source to include port keywords');
    assert.ok(m31TokenKinds.includes('athenaRelationshipKeyword'), 'expected M31 source to include relationship keywords');
    assert.ok(m31TokenKinds.includes('athenaLayoutKeyword'), 'expected M31 source to include layout keywords');
    assert.ok(m31TokenKinds.includes('athenaLayoutOperator'), 'expected M31 source to include layout operators');

    const incompleteTokens = await service.provideDocumentSemanticTokens({ getValue: () => incompleteSource });
    assert.ok(incompleteTokens, 'expected bundled Tree-sitter assets to stay usable on incomplete Athena source');
    assert.ok(incompleteTokens.data.length > 0, 'expected incomplete Athena source to still produce semantic tokens');
    assert.equal(service.getLastFailureMessage(), undefined, 'expected incomplete-source parse to avoid Tree-sitter runtime failures');
}

function decodeTokenKinds(data) {
    const tokenKinds = [];
    for (let index = 3; index < data.length; index += 5) {
        tokenKinds.push(athenaSemanticTokensLegend.tokenTypes[data[index]]);
    }
    return tokenKinds;
}

main().catch(error => {
    console.error(error.stack || String(error));
    process.exit(1);
});
