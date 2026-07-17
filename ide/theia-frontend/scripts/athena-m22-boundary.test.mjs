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

test('M22 boundaries stay explicit across planning docs, stories, usage, and sample sources', () => {
    const prd = readRepoFile('_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-17-m22/prd.md');
    const addendum = readRepoFile('_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-17-m22/addendum.md');
    const architecture = readRepoFile('_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-17-m22/ARCHITECTURE-SPINE.md');
    const epics = readRepoFile('_bmad-output/implementation-artifacts/m22/epics.md');
    const usage = readRepoFile('docs/usages/m22-proof-usage.md');
    const sourceEditTest = readRepoFile('ide/theia-frontend/scripts/athena-m22-layout-source-edit.test.mjs');
    const sampleSources = [
        'examples/m22/sample-project/src/01-baseline-sheet.athena',
        'examples/m22/sample-project/src/02-layout-optimization-acceptance.athena',
        'examples/m22/sample-project/src/03-component-round-trip.athena',
        'examples/m22/sample-project/src/04-boundary-scope.athena'
    ].map(readRepoFile).join('\n');

    [
        /public repository\/import ecosystem/i,
        /full IEC(?:\/| or )QElectroTech (library )?ingestion|broad IEC\/QElectroTech library ingestion/i,
        /cabinet authoring/i,
        /physical routing/i,
        /AI layout/i,
        /final\s+(ELK\/layout-stack|solver-stack|layout-stack|stack|external solver)\s+decision/i,
        /full EPLAN parity/i,
        /hidden canvas state|canvas-local state|sheet-local drag-save truth/i
    ].forEach(boundary => {
        assert.match(prd, boundary);
        assert.match(architecture, boundary);
        assert.match(epics, boundary);
        assert.match(usage, boundary);
    });

    assert.match(addendum, /Layout Constraint Model/i);
    assert.match(addendum, /ELK/i);
    assert.match(sourceEditTest, /localStorage\|sessionStorage\|indexedDB/);
    assert.match(usage, /no hidden canvas state persists layout truth/i);
    assert.match(usage, /deferred domains for future milestones/i);

    assert.doesNotMatch(sampleSources, /\bregistry\b|\bmarketplace\b|public repository/i);
    assert.doesNotMatch(sampleSources, /QElectroTech|IEC library|IEC breadth/i);
    assert.doesNotMatch(sampleSources, /\bcabinet authoring\b|\bphysical routing\b|\bharness\b|\bcable tray\b|\b3D installation\b/i);
    assert.doesNotMatch(sampleSources, /\bAI layout\b|EPLAN parity|final solver-stack|final layout-stack/i);
    assert.doesNotMatch(sampleSources, /localStorage|sessionStorage|indexedDB|canvas-local state|hidden canvas state/i);
});
