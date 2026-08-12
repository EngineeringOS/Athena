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

test('Athena source highlighting covers M42 engineering anatomy', () => {
    const languageDefinition = readRepoFile('ide/theia-frontend/src/browser/athena-language-definition.ts');
    const mainMonarch = languageDefinition.slice(
        languageDefinition.indexOf('export const athenaMonarchLanguage'),
        languageDefinition.indexOf('export const athenaSheetStyleMonarchLanguage')
    );
    for (const keyword of [
        'package', 'import', 'system', 'entity', 'concept', 'function', 'role', 'port',
        'direction', 'flow', 'in', 'out', 'bidirectional', 'passive', 'structure', 'display'
    ]) {
        assert.match(languageDefinition, new RegExp(`'${keyword}'`));
    }
    for (const retired of ['device', 'layout', 'graphic', 'svg', 'binding', 'profile']) {
        assert.doesNotMatch(mainMonarch, new RegExp(`'${retired}'`));
    }

    const highlightQuery = readRepoFile('ide/tree-sitter-athena/queries/highlights.scm');
    for (const capture of [
        '(entity_declaration "entity" @athenaDeclarationKeyword)',
        '(function_declaration "function" @athenaFunctionKeyword)',
        '(nested_port_declaration "port" @athenaPortKeyword)',
        '(structure_assignment "structure" @athenaDeclarationKeyword)'
    ]) {
        assert.ok(highlightQuery.includes(capture), `missing Tree-sitter capture: ${capture}`);
    }
    assert.doesNotMatch(highlightQuery, /device_declaration|graphic_declaration|binding_declaration/);
});

test('Sheet Style Companions use dedicated Monarch highlighting, not engineering Tree-sitter', () => {
    const bridge = readRepoFile('ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts');
    const languageDefinition = readRepoFile('ide/theia-frontend/src/browser/athena-language-definition.ts');
    assert.match(bridge, /isAthenaStyleCompanionUri/);
    assert.match(bridge, /ATHENA_SHEET_STYLE_LANGUAGE_ID/);
    assert.match(languageDefinition, /athenaSheetStyleMonarchLanguage/);
    assert.match(languageDefinition, /route-marker/);
    assert.match(languageDefinition, /port-display/);
});
