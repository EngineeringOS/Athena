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

test('M21 keeps deferred boundaries explicit in planning docs and usage', () => {
    const prd = readRepoFile('_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-17-m21/prd.md');
    const architecture = readRepoFile('_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-17-m21/ARCHITECTURE-SPINE.md');
    const epics = readRepoFile('_bmad-output/implementation-artifacts/m21/epics.md');
    const usage = readRepoFile('docs/usages/m21-proof-usage.md');
    const sprintStatus = readRepoFile('_bmad-output/implementation-artifacts/m21/sprint-status.yaml');

    [
        /repository\/import ecosystem/i,
        /full IEC/i,
        /cabinet authoring/i,
        /desktop viewer/i,
        /AI[- ](?:driven )?layout/i,
        /final (?:ELK\/)?layout-stack/i,
        /(?:sheet-local drag-save truth|direct canvas drag-save authoring)/i,
    ].forEach(pattern => assert.match(prd, pattern));

    assert.match(architecture, /AD-6 - Routing Is Schematic Topology Only/);
    assert.match(architecture, /AD-11 - M21 Excludes Ecosystem And Authoring Expansion/);
    assert.match(epics, /Story 4\.3: Keep M21 deferred boundaries explicit/);
    assert.match(usage, /Not supported in M21:/);
    assert.match(usage, /desktop-viewer scope/);
    assert.match(usage, /cabinet, harness, cable tray, 3D installation, or physical wire routing/);
    assert.match(sprintStatus, /4-3-keep-m21-deferred-boundaries-explicit:\s*(ready-for-dev|in-progress|review|done)/);
});

test('M21 active stories do not turn deferred boundaries into implementation scope', () => {
    const epics = readRepoFile('_bmad-output/implementation-artifacts/m21/epics.md');
    const sprintStatus = readRepoFile('_bmad-output/implementation-artifacts/m21/sprint-status.yaml');
    const activeStoryLines = sprintStatus
        .split(/\r?\n/)
        .filter(line => /:\s*(ready-for-dev|in-progress|review|done)\s*$/i.test(line))
        .filter(line => /^\s+\d+-\d+-/.test(line));

    assert.doesNotMatch(epics, /Story\s+\d+\.\d+:\s*(Implement|Render|Ship|Support|Build)\b.*repository\/import/i);
    assert.doesNotMatch(epics, /Story\s+\d+\.\d+:\s*(Implement|Render|Ship|Support|Build)\b.*full IEC/i);
    assert.doesNotMatch(epics, /Story\s+\d+\.\d+:\s*(Implement|Render|Ship|Support|Build)\b.*cabinet authoring/i);
    assert.doesNotMatch(epics, /Story\s+\d+\.\d+:\s*(Implement|Render|Ship|Support|Build)\b.*physical/i);
    assert.doesNotMatch(epics, /Story\s+\d+\.\d+:\s*(Implement|Render|Ship|Support|Build)\b.*AI layout/i);
    assert.doesNotMatch(epics, /Story\s+\d+\.\d+:\s*(Implement|Render|Ship|Support|Build)\b.*final layout/i);

    const illegalActiveStories = activeStoryLines
        .filter(line => !/(boundary|deferred|guard)/i.test(line))
        .filter(line =>
            /(repository|marketplace|full-iec|cabinet-authoring|desktop-viewer|physical-routing|ai-layout|final-layout-stack|drag-save)/i.test(line)
        );
    assert.deepEqual(illegalActiveStories, []);
});

test('M21 checked-in contracts do not persist arbitrary canvas edits as semantic truth', () => {
    const layoutEngine = readRepoFile('kernel/layout-engine/src/main/kotlin/com/engineeringood/athena/layout/engine/SchematicLayoutEngine.kt');
    const routingModel = readRepoFile('kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/SchematicRoutingModel.kt');
    const labelModel = readRepoFile('kernel/routing-model/src/main/kotlin/com/engineeringood/athena/routing/SchematicLabelModel.kt');
    const combined = `${layoutEngine}\n${routingModel}\n${labelModel}`.toLowerCase();

    assert.doesNotMatch(combined, /canvas/);
    assert.doesNotMatch(combined, /drag/);
    assert.doesNotMatch(combined, /save.*position/);
    assert.doesNotMatch(combined, /dom/);
    assert.doesNotMatch(combined, /css/);
});
