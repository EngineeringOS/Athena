import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import test from 'node:test';

const repoRoot = resolve(process.cwd(), '..', '..');

function readRepoFile(path) {
    return readFileSync(resolve(repoRoot, path), 'utf8');
}

test('M19 keeps cabinet preview deferred from the schematic-first milestone', () => {
    const prd = readRepoFile('_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/prd.md');
    const addendum = readRepoFile('_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/addendum.md');
    const architecture = readRepoFile('_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m19/ARCHITECTURE-SPINE.md');
    const epics = readRepoFile('_bmad-output/implementation-artifacts/m19/epics.md');
    const sprintStatus = readRepoFile('_bmad-output/implementation-artifacts/m19/sprint-status.yaml');

    assert.match(prd, /FR-4:\s*Defer cabinet preview from M19/);
    assert.match(prd, /Cabinet preview in M19/);
    assert.match(addendum, /cabinet preview is deferred from M19/i);
    assert.match(architecture, /AD-5 - Cabinet Preview Is Deferred/);
    assert.match(architecture, /Cabinet preview is not part of the M19 MVP/);
    assert.match(epics, /FR4:\s*Defer cabinet preview from M19/);
    assert.match(epics, /no M19 story requires it to ship/i);
    assert.match(sprintStatus, /M19 is schematic-first and keeps cabinet preview deferred/);

    assert.doesNotMatch(epics, /Story\s+\d+\.\d+:\s*(Implement|Render|Ship|Support|Build)\b.*cabinet preview/i);
    const activeCabinetStories = sprintStatus
        .split(/\r?\n/)
        .filter(line => /cabinet-preview.*:\s*(ready-for-dev|in-progress|review|done)/i.test(line))
        .filter(line => !/3-1-make-cabinet-preview-a-deferred-boundary/.test(line));
    assert.deepEqual(activeCabinetStories, []);
});

test('M19 keeps ecosystem expansion out of the schematic-first milestone', () => {
    const prd = readRepoFile('_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/prd.md');
    const addendum = readRepoFile('_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m19/addendum.md');
    const architecture = readRepoFile('_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m19/ARCHITECTURE-SPINE.md');
    const epics = readRepoFile('_bmad-output/implementation-artifacts/m19/epics.md');
    const sprintStatus = readRepoFile('_bmad-output/implementation-artifacts/m19/sprint-status.yaml');

    assert.match(prd, /public package repository/i);
    assert.match(prd, /full IEC symbol library/i);
    assert.match(prd, /Non-Users \(v1\)/i);
    assert.match(addendum, /public package repository/i);
    assert.match(addendum, /full IEC element catalog/i);
    assert.match(architecture, /AD-7 - M19 Excludes Ecosystem Expansion/);
    assert.match(architecture, /public repository\/import ecosystem work/i);
    assert.match(epics, /Story 3\.2: Keep ecosystem expansion out of M19/);
    assert.match(epics, /full IEC breadth/i);
    assert.match(sprintStatus, /3-2-keep-ecosystem-expansion-out-of-m19:\s*in-progress/);

    assert.doesNotMatch(epics, /Story\s+\d+\.\d+:\s*(Implement|Render|Ship|Support|Build)\b.*repository\/import/i);
    const activeEcosystemStories = sprintStatus
        .split(/\r?\n/)
        .filter(line => /repository|import|IEC/i.test(line))
        .filter(line => !/3-2-keep-ecosystem-expansion-out-of-m19/.test(line));
    assert.deepEqual(activeEcosystemStories, []);
});
