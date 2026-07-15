import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import test from 'node:test';
import { Parser, Language } from 'web-tree-sitter';

// AD-113: real, checked-in repository fixtures remain the source of truth for grammar parity —
// this script does not fabricate inline-only source snippets for its parity proof.

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const packageRoot = path.resolve(__dirname, '..');
const repoRoot = path.resolve(packageRoot, '..', '..');
const wasmPath = path.join(packageRoot, 'tree-sitter-athena.wasm');

const PARITY_FIXTURES = [
    path.join(repoRoot, 'examples', 'm0', 'demo-cabinet.athena'),
    path.join(repoRoot, 'examples', 'm0', 'dual-drive-cabinet.athena'),
    path.join(repoRoot, 'examples', 'm0', 'quoted-properties-cabinet.athena'),
    path.join(repoRoot, 'examples', 'm4', 'open-repository-proof', 'src', 'factory-line.athena'),
];

let language;

test.before(async () => {
    await Parser.init();
    language = await Language.load(wasmPath);
});

for (const fixturePath of PARITY_FIXTURES) {
    test(`parses ${path.relative(repoRoot, fixturePath)} with zero ERROR/MISSING nodes`, () => {
        const source = readFileSync(fixturePath, 'utf8');
        const parser = new Parser();
        parser.setLanguage(language);
        const tree = parser.parse(source);
        assert.ok(tree, `expected a tree for ${fixturePath}`);
        assert.equal(
            tree.rootNode.hasError,
            false,
            `expected zero ERROR/MISSING nodes for ${fixturePath}, got:\n${tree.rootNode.toString()}`
        );
    });
}
