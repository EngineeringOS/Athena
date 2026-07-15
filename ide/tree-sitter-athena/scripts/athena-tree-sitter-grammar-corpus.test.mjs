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

const VALID_M18_HEADERS = {
    'package-only': 'package com.root\nsystem Demo {}',
    'import-only': 'import com.controls.Switch2\nsystem Demo {}',
    'keyword-shaped-segments': 'package import.system\nimport package.import\nsystem Demo {}',
};

for (const [name, source] of Object.entries(VALID_M18_HEADERS)) {
    test(`M18 header ${name} parses without ERROR/MISSING nodes`, () => {
        const parser = new Parser();
        parser.setLanguage(language);
        const tree = parser.parse(source);
        assert.equal(tree.rootNode.hasError, false, tree.rootNode.toString());
    });
}

const INVALID_M18_HEADERS = {
    'package-after-import': 'import com.controls\npackage com.root\nsystem Demo {}',
    'duplicate-package': 'package com.one\npackage com.two\nsystem Demo {}',
    'import-after-system': 'system Demo {}\nimport com.controls',
    'alias': 'import com.controls as controls\nsystem Demo {}',
    'wildcard': 'import com.controls.*\nsystem Demo {}',
    'visibility': 'public import com.controls\nsystem Demo {}',
    'spaced-dot': 'import com . controls\nsystem Demo {}',
    'trailing-hyphen': 'import com.controls-\nsystem Demo {}',
    'repeated-hyphen': 'import com.controls--switch\nsystem Demo {}',
};

for (const [name, source] of Object.entries(INVALID_M18_HEADERS)) {
    test(`M18 header ${name} retains an error node`, () => {
        const parser = new Parser();
        parser.setLanguage(language);
        const tree = parser.parse(source);
        assert.equal(tree.rootNode.hasError, true, `expected syntax error for ${name}:\n${tree.rootNode.toString()}`);
    });
}
