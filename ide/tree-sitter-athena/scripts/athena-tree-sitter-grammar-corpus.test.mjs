import assert from 'node:assert/strict';
import { readFileSync, readdirSync } from 'node:fs';
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
const m18SyntaxProofDir = path.join(repoRoot, 'examples', 'm18', 'syntax-proof');
const m23ParserParityDir = path.join(repoRoot, 'examples', 'm23', 'parser-parity-proof');
const M18_SYNTAX_FIXTURE_NAMES = [
    'invalid-alias',
    'invalid-missing-target',
    'invalid-visibility',
    'invalid-wildcard',
    'valid-package-import',
    'valid-package-only',
];
const M23_LAYOUT_FIXTURE_NAMES = [
    'invalid-file-global-layout',
    'invalid-layout-bad-axis',
    'invalid-layout-malformed-place',
    'invalid-layout-missing-target',
    'valid-layout-block',
];

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

const VALID_M18_SYNTAX_FIXTURES = {
    'valid-package-import': { packageCount: 1, importTargets: ['com.engineeringood.controls', 'com.engineeringood.controls.Switch2'] },
    'valid-package-only': { packageCount: 1, importTargets: [] },
};

for (const [name, expectation] of Object.entries(VALID_M18_SYNTAX_FIXTURES)) {
    test(`M18 syntax fixture ${name} parses without ERROR/MISSING nodes`, () => {
        const source = readFileSync(path.join(m18SyntaxProofDir, `${name}.athena`), 'utf8');
        const parser = new Parser();
        parser.setLanguage(language);
        const tree = parser.parse(source);
        assert.equal(tree.rootNode.hasError, false);
        const packages = tree.rootNode.namedChildren.filter(node => node?.type === 'package_declaration');
        const imports = tree.rootNode.namedChildren.filter(node => node?.type === 'import_declaration');
        assert.equal(packages.length, expectation.packageCount);
        assert.deepEqual(imports.map(node => node.childForFieldName('target')?.text), expectation.importTargets);
    });
}

const INVALID_M18_SYNTAX_FIXTURES = {
    'invalid-alias': 'as controls',
    'invalid-visibility': 'public',
    'invalid-wildcard': '.*',
};

for (const [name, forbiddenText] of Object.entries(INVALID_M18_SYNTAX_FIXTURES)) {
    test(`M18 syntax fixture ${name} retains an error node`, () => {
        const source = readFileSync(path.join(m18SyntaxProofDir, `${name}.athena`), 'utf8');
        const parser = new Parser();
        parser.setLanguage(language);
        const tree = parser.parse(source);
        assert.equal(tree.rootNode.hasError, true);
        assert.ok(
            tree.rootNode.descendantsOfType('ERROR').some(node => node.text.includes(forbiddenText)),
            `expected ERROR node containing '${forbiddenText}':\n${tree.rootNode.toString()}`
        );
    });
}

test('M18 missing-target fixture preserves incomplete import and system structure', () => {
    const source = readFileSync(path.join(m18SyntaxProofDir, 'invalid-missing-target.athena'), 'utf8');
    const parser = new Parser();
    parser.setLanguage(language);
    const tree = parser.parse(source);
    assert.ok(tree.rootNode.namedChildren.some(node => node?.type === 'incomplete_import_declaration'));
    assert.ok(tree.rootNode.namedChildren.some(node => node?.type === 'system_declaration'));
});

test('M18 Tree-sitter fixture coverage matches the checked-in syntax inventory', () => {
    const discovered = readdirSync(m18SyntaxProofDir)
        .filter(name => name.endsWith('.athena'))
        .map(name => name.slice(0, -'.athena'.length))
        .sort();
    assert.deepEqual(discovered, M18_SYNTAX_FIXTURE_NAMES);
});

test('M23 Tree-sitter fixture coverage matches the checked-in layout syntax inventory', () => {
    const discovered = readdirSync(m23ParserParityDir)
        .filter(name => name.endsWith('.athena'))
        .map(name => name.slice(0, -'.athena'.length))
        .sort();
    assert.deepEqual(discovered, M23_LAYOUT_FIXTURE_NAMES);
});

test('M23 valid layout fixture parses without ERROR or MISSING nodes', () => {
    const source = readFileSync(path.join(m23ParserParityDir, 'valid-layout-block.athena'), 'utf8');
    const parser = new Parser();
    parser.setLanguage(language);
    const tree = parser.parse(source);
    assert.equal(tree.rootNode.hasError, false, tree.rootNode.toString());
});

for (const fixtureName of M23_LAYOUT_FIXTURE_NAMES.filter(name => name.startsWith('invalid-'))) {
    test(`M23 invalid layout fixture ${fixtureName} retains an error node`, () => {
        const source = readFileSync(path.join(m23ParserParityDir, `${fixtureName}.athena`), 'utf8');
        const parser = new Parser();
        parser.setLanguage(language);
        const tree = parser.parse(source);
        assert.equal(tree.rootNode.hasError, true, `expected syntax error for ${fixtureName}:\n${tree.rootNode.toString()}`);
    });
}
