const fs = require('node:fs');
const path = require('node:path');

const productRoot = path.resolve(__dirname, '..');
const frontendAssetRoot = path.join(productRoot, 'lib', 'frontend');
const grammarRoot = path.dirname(require.resolve('@engineeringood/athena-tree-sitter-grammar/package.json'));

const assets = [
    {
        source: path.join(grammarRoot, 'tree-sitter-athena.wasm'),
        destination: path.join(frontendAssetRoot, 'tree-sitter-athena.wasm')
    },
    {
        source: path.join(grammarRoot, 'queries', 'highlights.scm'),
        destination: path.join(frontendAssetRoot, 'athena-tree-sitter-highlights.scm')
    }
];

function main() {
    fs.mkdirSync(frontendAssetRoot, { recursive: true });
    for (const asset of assets) {
        if (!fs.existsSync(asset.source)) {
            throw new Error(`Tree-sitter asset not found: ${asset.source}`);
        }
        fs.copyFileSync(asset.source, asset.destination);
    }

    console.log(`Copied Athena Tree-sitter assets to ${frontendAssetRoot}`);
}

main();
