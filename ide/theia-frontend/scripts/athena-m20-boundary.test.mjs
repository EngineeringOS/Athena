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

test('M20 keeps deferred boundaries explicit in docs and stories', () => {
    const prd = readRepoFile('_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-16-m20/prd.md');
    const architecture = readRepoFile('_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-16-m20/ARCHITECTURE-SPINE.md');
    const epics = readRepoFile('_bmad-output/implementation-artifacts/m20/epics.md');
    const story = readRepoFile('_bmad-output/implementation-artifacts/m20/4-3-keep-the-deferred-boundaries-explicit.md');
    const examples = readRepoFile('examples/m20/README.md');
    const acceptanceReadme = readRepoFile('examples/m20/acceptance-sheet-proof/README.md');
    const sprintStatus = readRepoFile('_bmad-output/implementation-artifacts/m20/sprint-status.yaml');

    assert.match(prd, /cabinet preview/i);
    assert.match(prd, /repository\/import ecosystem work/i);
    assert.match(prd, /final layout-stack decision/i);
    assert.match(architecture, /AD-8 - M20 Excludes Ecosystem Expansion/);
    assert.match(architecture, /AD-9 - No New Stack Decision In M20/);
    assert.match(architecture, /AD-10 - Layout Intelligence Remains Deferred/);
    assert.match(epics, /Story 4\.3: Keep the deferred boundaries explicit/);
    assert.match(epics, /cabinet preview/i);
    assert.match(epics, /repository\/import ecosystem work/i);
    assert.match(story, /No story in M20 requires cabinet preview authoring\./);
    assert.match(story, /No story in M20 requires repository\/import ecosystem behavior\./);
    assert.match(story, /No story in M20 chooses a final layout stack or layout engine\./);
    assert.match(story, /No story in M20 expands to full IEC breadth or frontend-owned semantic resolution\./);
    assert.match(examples, /customer-facing acceptance baseline/);
    assert.match(acceptanceReadme, /customer-facing acceptance baseline/);
    assert.match(sprintStatus, /4-3-keep-the-deferred-boundaries-explicit:\s*(ready-for-dev|review)/);

    assert.doesNotMatch(epics, /Story\s+\d+\.\d+:\s*(Implement|Render|Ship|Support|Build)\b.*cabinet preview/i);
    assert.doesNotMatch(epics, /Story\s+\d+\.\d+:\s*(Implement|Render|Ship|Support|Build)\b.*repository\/import/i);
    assert.doesNotMatch(epics, /Story\s+\d+\.\d+:\s*(Implement|Render|Ship|Support|Build)\b.*final layout/i);
});
