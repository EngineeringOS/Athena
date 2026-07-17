import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import test from 'node:test';

const repoRoot = [
    process.cwd(),
    resolve(process.cwd(), '..'),
    resolve(process.cwd(), '..', '..'),
].find(candidate => existsSync(resolve(candidate, '_bmad-output'))) ?? process.cwd();

function readRepoFile(path) {
    return readFileSync(resolve(repoRoot, path), 'utf8');
}

test('Graphical View projection keeps the last active Athena source when focus moves to the graph widget', () => {
    const bridgeSource = readRepoFile('ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts');
    const adapterSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-adapter-service.ts');
    const backendProjectionTest = readRepoFile('ide/lsp/src/test/kotlin/com/engineeringood/athena/ide/lsp/AthenaProjectionRequestTest.kt');
    const usage = readRepoFile('docs/usages/m22-proof-usage.md');

    assert.match(bridgeSource, /protected lastAthenaEditorWidget = undefined as EditorWidget \| undefined/);
    assert.match(bridgeSource, /this\.lastAthenaEditorWidget = widget/);
    assert.match(bridgeSource, /const widget = this\.isAthenaEditor\(this\.editorManager\.currentEditor\)\s*\?\s*this\.editorManager\.currentEditor\s*:\s*this\.lastAthenaEditorWidget/);
    assert.match(bridgeSource, /requestProjectionSession\(\): Promise<AthenaProjectionSessionPayload \| undefined>/);
    assert.match(adapterSource, /requestDiagram\(\): Promise<AthenaGLSPDiagram \| undefined>/);
    assert.match(adapterSource, /requestProjectionSession\(\)/);
    assert.match(backendProjectionTest, /projection session request follows latest opened source file in governed repository/);
    assert.match(backendProjectionTest, /Projection should come from the latest opened source, not the repository seed source/);
    assert.match(usage, /active-source projection/i);
    assert.match(usage, /does not fall back to the baseline seed file/i);
});
