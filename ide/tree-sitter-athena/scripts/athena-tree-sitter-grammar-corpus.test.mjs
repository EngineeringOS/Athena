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
const m34SymbolFixturePath = path.join(packageRoot, 'test', 'fixtures', 'm34-symbol.athena');
const m34ElementFixturePath = path.join(packageRoot, 'test', 'fixtures', 'm34-element.athena');
const m34FunctionPlacementFixturePath = path.join(packageRoot, 'test', 'fixtures', 'm34-function-placement.athena');
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
    'invalid-installation-kind',
    'invalid-layout-bad-axis',
    'invalid-layout-malformed-place',
    'invalid-layout-missing-target',
    'valid-installation-cabinet',
    'valid-layout-block',
];

const PARITY_FIXTURES = [
    path.join(repoRoot, 'examples', 'm0', 'demo-cabinet.athena'),
    path.join(repoRoot, 'examples', 'm0', 'dual-drive-cabinet.athena'),
    path.join(repoRoot, 'examples', 'm0', 'quoted-properties-cabinet.athena'),
    path.join(repoRoot, 'examples', 'm4', 'open-repository-proof', 'src', 'com', 'engineeringood', 'factoryline', 'factoryline.athena'),
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

const VALID_M34_SYMBOL = readFileSync(m34SymbolFixturePath, 'utf8');
const VALID_M34_ELEMENT = readFileSync(m34ElementFixturePath, 'utf8');
const VALID_M34_SVG_GRAPHIC = readFileSync(path.join(packageRoot, 'test', 'fixtures', 'm34-svg-graphic.athena'), 'utf8');
const VALID_M34_FUNCTION_PLACEMENT = readFileSync(m34FunctionPlacementFixturePath, 'utf8');

for (const [name, source] of Object.entries(INVALID_M18_HEADERS)) {
    test(`M18 header ${name} retains an error node`, () => {
        const parser = new Parser();
        parser.setLanguage(language);
        const tree = parser.parse(source);
        assert.equal(tree.rootNode.hasError, true, `expected syntax error for ${name}:\n${tree.rootNode.toString()}`);
    });
}

test('M34 standalone Symbol exposes the frozen syntax node boundary', () => {
    const parser = new Parser();
    parser.setLanguage(language);
    const tree = parser.parse(VALID_M34_SYMBOL);
    assert.equal(tree.rootNode.hasError, false, tree.rootNode.toString());

    const symbol = tree.rootNode.namedChildren.find(node => node?.type === 'symbol_declaration');
    assert.ok(symbol, tree.rootNode.toString());
    assert.equal(symbol.childForFieldName('name')?.text, 'iec_switch_contact');
    assert.equal(symbol.descendantsOfType('symbol_identity').length, 1);
    assert.equal(symbol.descendantsOfType('symbol_version').length, 1);
    assert.equal(symbol.descendantsOfType('graphic_bounds').length, 1);
    assert.equal(symbol.descendantsOfType('line_primitive').length, 2);
    assert.equal(symbol.descendantsOfType('style_reference').length, 2);
    assert.equal(symbol.descendantsOfType('anchor_declaration').length, 2);
    assert.equal(symbol.descendantsOfType('primitive_reference').length, 2);
    assert.equal(symbol.descendantsOfType('anchor_point').length, 2);
    assert.equal(symbol.descendantsOfType('anchor_role').length, 2);
    assert.equal(symbol.descendantsOfType('anchor_direction').length, 2);
    assert.equal(symbol.descendantsOfType('anchor_signal').length, 2);
});

test('M34 mixed Symbol and Element exposes the frozen syntax node boundary', () => {
    const parser = new Parser();
    parser.setLanguage(language);
    const tree = parser.parse(VALID_M34_ELEMENT);
    assert.equal(tree.rootNode.hasError, false, tree.rootNode.toString());

    const element = tree.rootNode.namedChildren.find(node => node?.type === 'element_declaration');
    assert.ok(element, tree.rootNode.toString());
    assert.equal(element.childForFieldName('name')?.text, 'iec_switch_module');
    assert.equal(element.descendantsOfType('element_identity').length, 1);
    assert.equal(element.descendantsOfType('element_version').length, 1);
    assert.equal(element.descendantsOfType('element_bounds').length, 1);
    assert.equal(element.descendantsOfType('element_child').length, 2);
    assert.equal(element.descendantsOfType('child_symbol_reference').length, 2);
    assert.equal(element.descendantsOfType('child_translate').length, 2);
    assert.equal(element.descendantsOfType('child_rotate').length, 2);
    assert.equal(element.descendantsOfType('child_scale').length, 2);
    assert.equal(element.descendantsOfType('child_z_order').length, 2);
    assert.equal(element.descendantsOfType('element_export_anchor').length, 4);
});

test('M34 mixed SVG graphic source exposes the frozen syntax node boundary', () => {
    const parser = new Parser();
    parser.setLanguage(language);
    const tree = parser.parse(VALID_M34_SVG_GRAPHIC);
    assert.equal(tree.rootNode.hasError, false, tree.rootNode.toString());

    assert.equal(tree.rootNode.descendantsOfType('graphic_declaration').length, 2);
    assert.equal(tree.rootNode.descendantsOfType('string').some(node => node.text === '"./vendor-drive.svg"'), true);
});

test('M34 Tree-sitter leaves duplicate ids and unresolved references to semantic validation', () => {
    const source = VALID_M34_SYMBOL
        .replace('line load from', 'line line from')
        .replace('primitiveRef load', 'primitiveRef missing');
    const parser = new Parser();
    parser.setLanguage(language);
    const tree = parser.parse(source);
    assert.equal(tree.rootNode.hasError, false, tree.rootNode.toString());
});

test('M34 function and fixed drawing placement expose syntax-only nodes without semantic inference', () => {
    const parser = new Parser();
    parser.setLanguage(language);
    const tree = parser.parse(VALID_M34_FUNCTION_PLACEMENT);
    assert.equal(tree.rootNode.hasError, false, tree.rootNode.toString());

    assert.equal(tree.rootNode.descendantsOfType('function_declaration').length, 1);
    assert.equal(tree.rootNode.descendantsOfType('function_role').length, 1);
    assert.equal(tree.rootNode.descendantsOfType('function_ports').length, 1);
    assert.equal(tree.rootNode.descendantsOfType('fixed_place_statement').length, 2);
    assert.deepEqual(
        tree.rootNode.descendantsOfType('fixed_place_statement').map(node => node.childForFieldName('subject')?.text),
        ['KM1.coil', 'KM1'],
    );
});

test('M34 Tree-sitter mirrors ANTLR direction and literal boundaries', () => {
    const validSource = VALID_M34_SYMBOL.replace('accepts direction in', 'accepts direction bidirectional');
    const parser = new Parser();
    parser.setLanguage(language);

    const validTree = parser.parse(validSource);
    assert.equal(validTree.rootNode.hasError, false, validTree.rootNode.toString());
    assert.equal(validTree.rootNode.descendantsOfType('direction_name').some(node => node.text === 'bidirectional'), true);

    for (const [name, source] of Object.entries({
        'leading plus': VALID_M34_SYMBOL.replace('(40, 0)', '(+40, 0)'),
        'leading decimal point': VALID_M34_SYMBOL.replace('(40, 0)', '(.5, 0)'),
        'exponent': VALID_M34_SYMBOL.replace('(40, 0)', '(4e1, 0)'),
        'multiline string': VALID_M34_SYMBOL.replace('"iec.switch_contact"', '"iec.switch\ncontact"'),
    })) {
        const tree = parser.parse(source);
        assert.equal(tree.rootNode.hasError, true, `expected syntax error for ${name}:\n${tree.rootNode.toString()}`);
    }
});

for (const [name, source] of Object.entries({
    'mixed project and representation units': `${VALID_M34_SYMBOL}\nsystem Demo {}`,
    'SVG authority forbidden': VALID_M34_SYMBOL.replace('bounds (0, 0, 80, 80)', 'import svg "contact.svg"'),
    'renderer authority forbidden': VALID_M34_SYMBOL.replace('bounds (0, 0, 80, 80)', 'renderer svg'),
    'descriptor authority forbidden': VALID_M34_SYMBOL.replace('symbol iec_switch_contact', 'descriptor iec_switch_contact'),
    'occurrence authority forbidden': VALID_M34_SYMBOL.replace('symbol iec_switch_contact', 'occurrence iec_switch_contact'),
    'transport authority forbidden': VALID_M34_SYMBOL.replace('symbol iec_switch_contact', 'transport iec_switch_contact'),
    'XML authority forbidden': VALID_M34_SYMBOL.replace('symbol iec_switch_contact', 'XML iec_switch_contact'),
    'unsupported path geometry': VALID_M34_SYMBOL.replace('line line from (40, 0) to (40, 20) style conductor', 'path body data "M0 0 L10 10" style symbol'),
})) {
    test(`M34 ${name} remains outside the frozen Symbol syntax`, () => {
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
