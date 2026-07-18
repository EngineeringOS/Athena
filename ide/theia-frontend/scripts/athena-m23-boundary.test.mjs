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

test('M23 boundaries and usage stay honest across docs and sample proof', () => {
    const prd = readRepoFile('_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-18-m23/prd.md');
    const architecture = readRepoFile('_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-18-m23/ARCHITECTURE-SPINE.md');
    const epics = readRepoFile('_bmad-output/implementation-artifacts/m23/epics.md');
    const usage = readRepoFile('docs/usages/m23-proof-usage.md');
    const retrospective = readRepoFile('_bmad-output/implementation-artifacts/m23/m23-achievement-usage-retrospective-2026-07-18.md');
    const m22Usage = readRepoFile('docs/usages/m22-proof-usage.md');
    const m22Retrospective = readRepoFile('_bmad-output/implementation-artifacts/m22/m22-achievement-usage-retrospective-2026-07-18.md');
    const sampleSource = readRepoFile('examples/m23/sample-project/src/01-layout-hints.athena');

    [
        /EPLAN parity/i,
        /advanced\s+(electrical\s+)?routing/i,
        /AI layout/i,
        /public repository\/import ecosystem|repository\/library ecosystem|repository\/import/i,
        /full IEC\/QElectroTech|broad IEC|library ingestion/i,
        /hidden canvas state|canvas-local state|free-form canvas drawing persistence/i,
        /raw pixel-coordinate/i,
    ].forEach(boundary => {
        assert.match(prd, boundary);
        assert.match(epics, boundary);
        assert.match(usage, boundary);
        assert.match(retrospective, boundary);
    });

    assert.match(architecture, /M23 is language admission, not new layout depth/i);
    assert.match(usage, /M22.*preview-only|preview-only.*M22/i);
    assert.match(usage, /M23 is the first milestone/i);
    assert.match(m22Usage, /real parser\/compiler\/LSP admission is deferred to M23/i);
    assert.match(m22Retrospective, /real parser\/compiler\/LSP admission for `\.athena` layout blocks[\s\S]*deferred to M23/i);
    assert.match(retrospective, /stale installed LSP host/i);
    assert.match(retrospective, /ANTLR4 and Tree-sitter/i);
    assert.match(retrospective, /openable Athena project/i);

    assert.match(sampleSource, /\blayout schematic-sheet\s+\{/);
    assert.match(sampleSource, /\bplace OperatorHMI1 near ControllerPLC1\b/);
    assert.doesNotMatch(sampleSource, /EPLAN parity|AI layout|public repository|marketplace|QElectroTech|IEC library|hidden canvas state|localStorage|sessionStorage|indexedDB/i);
});
