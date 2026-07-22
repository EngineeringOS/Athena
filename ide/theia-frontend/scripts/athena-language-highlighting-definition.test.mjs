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
        'vertical',
        'horizontal'
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
        '(align_statement "align" @athenaLayoutKeyword)',
        '(group_statement "group" @athenaLayoutKeyword)',
        '(layout_placement_relation) @athenaLayoutKeyword',
        '(layout_axis) @athenaLayoutKeyword',
        '(align_statement "aligned-with" @athenaLayoutOperator)',
        '(group_statement "grouped-with" @athenaLayoutOperator)',
        '(connect_declaration "connect" @athenaRelationshipKeyword)',
        '(connect_declaration "->" @operator)',
        '(connect_group_edge "->" @operator)'
    ].forEach(expectedCapture => {
        assert.ok(
            highlightQuery.includes(expectedCapture),
            `missing Tree-sitter highlight capture: ${expectedCapture}`
        );
    });
    assert.match(highlightQuery, /\^\(direction\|signal\)\$/);
    assert.match(highlightQuery, /\^\(in\|out\)\$/);
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
        'operator.athena-layout',
        'operator.athena-relationship',
        'athenaDeclarationKeyword',
        'athenaPortKeyword',
        'athenaRelationshipKeyword',
        'athenaLayoutKeyword',
        'athenaLayoutOperator'
    ].forEach(tokenType => {
        assert.match(lspEditorBridge, new RegExp(tokenType.replace('.', '\\.')));
    });
});
