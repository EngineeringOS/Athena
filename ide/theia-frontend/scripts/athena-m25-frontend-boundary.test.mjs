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

test('M25 representation rendering stays inside Theia frontend and transport boundaries', () => {
    const allowedPaths = [
        'ide/theia-frontend/src/browser/athena-graph-presentation-model.ts',
        'ide/theia-frontend/src/browser/athena-graph-workbench-model.ts',
        'ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx',
        'integrations/graph-glsp/src/athena-glsp-projection-source.ts',
        'integrations/graph-glsp/src/athena-glsp-projection-adapter.ts',
        'ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationPayloads.kt',
        'ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaPresentationSessionProtocol.kt',
    ];
    const forbiddenPattern = /(desktop-viewer|desktop\/shared|compose|Compose|KMP|kotlin-multiplatform)/;

    for (const path of allowedPaths) {
        assert.doesNotMatch(readRepoFile(path), forbiddenPattern, `${path} must not depend on deprecated frontend scope`);
    }
});

test('M25 story records do not list desktop viewer or Compose files as implementation output', () => {
    const storyPaths = [
        '_bmad-output/implementation-artifacts/m25/3-1-render-presentation-primitives-and-schematic-symbol-anatomy.md',
        '_bmad-output/implementation-artifacts/m25/3-2-render-presentation-terminals-and-label-facts.md',
        '_bmad-output/implementation-artifacts/m25/3-3-add-representation-terminal-and-label-inspection.md',
        '_bmad-output/implementation-artifacts/m25/3-4-preserve-accepted-graph-workbench-behavior-and-frontend-boundary.md',
    ];
    const forbiddenPathPattern = /`[^`]*(desktop-viewer|desktop\/shared|compose|Compose|KMP|kotlin-multiplatform)[^`]*`/;

    for (const path of storyPaths) {
        const story = readRepoFile(path);
        const fileList = story.split('### File List')[1]?.split('## Change Log')[0] ?? '';
        assert.doesNotMatch(fileList, forbiddenPathPattern, `${path} must not record forbidden frontend files`);
    }
});
