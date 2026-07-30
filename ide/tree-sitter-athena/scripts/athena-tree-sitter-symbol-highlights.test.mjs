import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import test from 'node:test';
import { Language, Parser, Query } from 'web-tree-sitter';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const packageRoot = path.resolve(__dirname, '..');
const sourcePath = path.join(packageRoot, 'test', 'fixtures', 'm34-element.athena');
const svgSourcePath = path.join(packageRoot, 'test', 'fixtures', 'm34-svg-graphic.athena');
const SOURCE = readFileSync(sourcePath, 'utf8');
const SVG_SOURCE = readFileSync(svgSourcePath, 'utf8');
const FUNCTION_SOURCE = readFileSync(path.join(packageRoot, 'test', 'fixtures', 'm34-function-placement.athena'), 'utf8');
const REPRESENTATION_VOCABULARY_SOURCE = readFileSync(
    path.join(packageRoot, 'test', 'fixtures', 'm34-representation-vocabulary.athena'),
    'utf8'
);

let language;

test.before(async () => {
    await Parser.init();
    language = await Language.load(path.join(packageRoot, 'tree-sitter-athena.wasm'));
});

test('M34 Symbol and Element keywords receive syntax-only highlight captures', () => {
    const parser = new Parser();
    parser.setLanguage(language);
    const tree = parser.parse(SOURCE);
    assert.equal(tree.rootNode.hasError, false, tree.rootNode.toString());

    const query = new Query(language, readFileSync(path.join(packageRoot, 'queries', 'highlights.scm'), 'utf8'));
    const captures = query.captures(tree.rootNode).map(capture => `${capture.name}:${capture.node.text}`);

    assert.ok(captures.includes('athenaRepresentationKeyword:symbol'));
    assert.ok(captures.includes('athenaRepresentationKeyword:element'));
    for (const keyword of ['identity', 'version', 'graphic', 'anchor', 'role', 'child', 'translate', 'rotate', 'scale', 'zOrder', 'export', 'symbol']) {
        assert.ok(captures.includes(`athenaRepresentationKeyword:${keyword}`), `missing representation capture for ${keyword}:\n${captures.join('\n')}`);
    }
    for (const keyword of ['bounds', 'line', 'from', 'to', 'style', 'ref', 'point']) {
        assert.ok(captures.includes(`athenaPrimitiveKeyword:${keyword}`), `missing primitive capture for ${keyword}:\n${captures.join('\n')}`);
    }
    for (const keyword of ['direction', 'signal']) {
        assert.ok(captures.includes(`athenaPortKeyword:${keyword}`), `missing port capture for ${keyword}:\n${captures.join('\n')}`);
    }
    assert.ok(captures.includes('number:80'));
    assert.ok(captures.includes('string:"iec.switch_contact"'));
});

test('M34 SVG graphic keyword and resource string receive syntax-only highlight captures', () => {
    const parser = new Parser();
    parser.setLanguage(language);
    const tree = parser.parse(SVG_SOURCE);
    assert.equal(tree.rootNode.hasError, false, tree.rootNode.toString());

    const query = new Query(language, readFileSync(path.join(packageRoot, 'queries', 'highlights.scm'), 'utf8'));
    const captures = query.captures(tree.rootNode).map(capture => `${capture.name}:${capture.node.text}`);

    assert.ok(captures.includes('athenaRepresentationKeyword:graphic'));
    assert.ok(captures.includes('athenaRepresentationKeyword:svg'));
    assert.ok(captures.includes('string:"./vendor-drive.svg"'));
});

test('M34 function and fixed placement keywords receive syntax-only highlight captures', () => {
    const parser = new Parser();
    parser.setLanguage(language);
    const tree = parser.parse(FUNCTION_SOURCE);
    assert.equal(tree.rootNode.hasError, false, tree.rootNode.toString());

    const query = new Query(language, readFileSync(path.join(packageRoot, 'queries', 'highlights.scm'), 'utf8'));
    const captures = query.captures(tree.rootNode).map(capture => `${capture.name}:${capture.node.text}`);
    for (const keyword of ['function', 'role', 'ports', 'at', 'orientation', 'horizontal', 'vertical']) {
        assert.ok(captures.some(capture => capture.endsWith(`:${keyword}`)), `missing capture for ${keyword}`);
    }
});

test('M34 complete representation vocabulary parses and receives category-specific captures', () => {
    const parser = new Parser();
    parser.setLanguage(language);
    const tree = parser.parse(REPRESENTATION_VOCABULARY_SOURCE);
    assert.equal(tree.rootNode.hasError, false, tree.rootNode.toString());

    const query = new Query(language, readFileSync(path.join(packageRoot, 'queries', 'highlights.scm'), 'utf8'));
    const captures = query.captures(tree.rootNode).map(capture => `${capture.name}:${capture.node.text}`);

    for (const keyword of ['symbol', 'element', 'identity', 'graphic', 'anchor', 'child', 'export']) {
        assert.ok(
            captures.includes(`athenaRepresentationKeyword:${keyword}`),
            `missing representation capture for ${keyword}:\n${captures.join('\n')}`
        );
    }
    for (const keyword of [
        'bounds', 'line', 'polyline', 'points', 'arc', 'center', 'radius', 'sweep', 'circle',
        'rectangle', 'at', 'size', 'label', 'from', 'to', 'style', 'ref', 'point'
    ]) {
        assert.ok(
            captures.includes(`athenaPrimitiveKeyword:${keyword}`),
            `missing primitive capture for ${keyword}:\n${captures.join('\n')}`
        );
    }
    for (const keyword of ['profile', 'projection', 'standard', 'fallback', 'fail-closed']) {
        assert.ok(
            captures.includes(`athenaProfileKeyword:${keyword}`),
            `missing profile capture for ${keyword}:\n${captures.join('\n')}`
        );
    }
    for (const keyword of ['binding', 'priority', 'select', 'function', 'where', 'use', 'variant']) {
        assert.ok(
            captures.includes(`athenaBindingKeyword:${keyword}`),
            `missing binding capture for ${keyword}:\n${captures.join('\n')}`
        );
    }
});
