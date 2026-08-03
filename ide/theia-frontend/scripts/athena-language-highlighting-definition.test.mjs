import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import path from 'node:path';
import test from 'node:test';
import { fileURLToPath } from 'node:url';

const scriptDir = path.dirname(fileURLToPath(import.meta.url));
const repoRoot = path.resolve(scriptDir, '..', '..', '..');

function readRepoFile(relativePath) {
    return readFileSync(path.join(repoRoot, relativePath), 'utf8');
}

test('Athena Monaco fallback highlights current DSL keywords and operators', () => {
    const languageDefinition = readRepoFile('ide/theia-frontend/src/browser/athena-language-definition.ts');
    [
        'package',
        'import',
        'system',
        'device',
        'port',
        'type',
        'model',
        'direction',
        'signal',
        'in',
        'out',
        'connect',
        'layout',
        'place',
        'align',
        'group',
        'near',
        'below',
        'axis',
        'at',
        'orientation',
        'vertical',
        'horizontal',
        'function',
        'role',
        'ports',
        'symbol',
        'element',
        'identity',
        'version',
        'graphic',
        'svg',
        'anchor',
        'ref',
        'accepts',
        'child',
        'translate',
        'rotate',
        'scale',
        'zOrder',
        'export',
        'bounds',
        'line',
        'polyline',
        'arc',
        'circle',
        'rectangle',
        'label',
        'points',
        'center',
        'radius',
        'sweep',
        'size',
        'style',
        'profile',
        'projection',
        'standard',
        'fallback',
        'fail-closed',
        'binding',
        'priority',
        'select',
        'where',
        'use',
        'variant'
    ].forEach(keyword => {
        assert.match(languageDefinition, new RegExp(`'${keyword}'`));
    });
    assert.match(languageDefinition, /\[\/->\/,\s*'operator\.athena-relationship'\]/);
    assert.match(languageDefinition, /operator\.athena-relationship/);
    assert.match(languageDefinition, /operator\.athena-layout/);
    assert.match(languageDefinition, /keyword\.athena-declaration/);
    assert.match(languageDefinition, /keyword\.athena-port/);
    assert.match(languageDefinition, /keyword\.athena-relationship/);
    assert.match(languageDefinition, /keyword\.athena-layout/);
    assert.match(languageDefinition, /keyword\.athena-function/);
    assert.match(languageDefinition, /keyword\.athena-representation/);
    assert.match(languageDefinition, /keyword\.athena-primitive/);
    assert.match(languageDefinition, /keyword\.athena-profile/);
    assert.match(languageDefinition, /keyword\.athena-binding/);
    assert.match(languageDefinition, /aligned-with\|grouped-with/);
    assert.match(languageDefinition, /\?:-\[A-Za-z_\]/);
});

test('Athena Tree-sitter syntax highlighting covers layout and port authoring tokens', () => {
    const highlightQuery = readRepoFile('ide/tree-sitter-athena/queries/highlights.scm');
    [
        '(system_declaration "system" @athenaDeclarationKeyword)',
        '(device_declaration "device" @athenaDeclarationKeyword)',
        '(layout_declaration "layout" @athenaLayoutKeyword)',
        '(place_statement "place" @athenaLayoutKeyword)',
        '(function_declaration "function" @athenaFunctionKeyword)',
        '(function_role "role" @athenaFunctionKeyword)',
        '(function_ports "ports" @athenaFunctionKeyword)',
        '(fixed_place_statement "place" @athenaLayoutKeyword)',
        '(fixed_place_statement "at" @athenaLayoutKeyword)',
        '(fixed_place_statement "orientation" @athenaLayoutKeyword)',
        '(layout_orientation) @athenaLayoutKeyword',
        '(align_statement "align" @athenaLayoutKeyword)',
        '(group_statement "group" @athenaLayoutKeyword)',
        '(layout_placement_relation) @athenaLayoutKeyword',
        '(layout_axis) @athenaLayoutKeyword',
        '(align_statement "aligned-with" @athenaLayoutOperator)',
        '(group_statement "grouped-with" @athenaLayoutOperator)',
        '(connect_declaration "connect" @athenaRelationshipKeyword)',
        '(connect_declaration "to" @athenaRelationshipKeyword)',
        '(connect_group_edge "to" @athenaRelationshipKeyword)'
    ].forEach(expectedCapture => {
        assert.ok(
            highlightQuery.includes(expectedCapture),
            `missing Tree-sitter highlight capture: ${expectedCapture}`
        );
    });
    assert.match(highlightQuery, /\^\(direction\|signal\)\$/);
    assert.match(highlightQuery, /\^\(in\|out\|bidirectional\|passive\)\$/);
});

test('Athena product exposes color rules for custom syntax token classes', () => {
    const productPackage = JSON.parse(readRepoFile('ide/theia-product/package.json'));
    const lspEditorBridge = readRepoFile('ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts');

    assert.equal(productPackage.theia.frontend.config.preferences['editor.semanticHighlighting.enabled'], true);
    assert.equal(productPackage.theia.frontend.config.preferences['editor.semanticTokenColorCustomizations'], undefined);
    [
        'keyword.athena-declaration',
        'keyword.athena-port',
        'keyword.athena-relationship',
        'keyword.athena-layout',
        'keyword.athena-function',
        'operator.athena-layout',
        'operator.athena-relationship',
        'athenaDeclarationKeyword',
        'athenaPortKeyword',
        'athenaFunctionKeyword',
        'athenaRelationshipKeyword',
        'athenaLayoutKeyword',
        'athenaLayoutOperator',
        'athenaRepresentationKeyword',
        'athenaPrimitiveKeyword',
        'athenaProfileKeyword',
        'athenaBindingKeyword'
    ].forEach(tokenType => {
        assert.match(lspEditorBridge, new RegExp(tokenType.replace('.', '\\.')));
    });
});
