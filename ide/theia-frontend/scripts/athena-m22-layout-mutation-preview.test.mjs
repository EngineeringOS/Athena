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

test('graph workbench prepares reviewable layout mutation previews before source edits', () => {
    const modelSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-model.ts');
    const widgetSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx');

    assert.match(modelSource, /AthenaGraphLayoutMutationPreview/);
    assert.match(modelSource, /buildAthenaGraphLayoutMutationPreview/);
    assert.match(modelSource, /previewId/);
    assert.match(modelSource, /layoutBlockSnippet/);
    assert.match(modelSource, /layout schematic-sheet/);
    assert.match(modelSource, /persisted:\s*false/);
    assert.match(widgetSource, /layoutMutationPreview/);
    assert.match(widgetSource, /renderLayoutMutationPreview/);
    assert.match(widgetSource, /rejectLayoutMutationPreview/);
    assert.match(widgetSource, /this\.layoutMutationPreview = undefined/);
    assert.doesNotMatch(widgetSource, /applyAuthoringSourceEdit\([^)]*layoutMutationPreview/);
    assert.match(widgetSource, /acceptLayoutMutationPreview/);
    const renderPreviewStart = widgetSource.indexOf('protected renderLayoutMutationPreview');
    const acceptPreviewStart = widgetSource.indexOf('protected acceptLayoutMutationPreview');
    assert.ok(renderPreviewStart >= 0 && acceptPreviewStart > renderPreviewStart);
    const renderPreviewBody = widgetSource.slice(renderPreviewStart, acceptPreviewStart);
    assert.doesNotMatch(renderPreviewBody, /applyAuthoringSourceEdit/);
});
