import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import test from 'node:test';

const closure = await import('./verify-athena-m41-closure.js');

const repoRoot = path.resolve(import.meta.dirname, '..', '..', '..');
const artifactRoot = path.join(repoRoot, '_bmad-output', 'implementation-artifacts', 'm41');
const tempRoots = [];

test.afterEach(() => {
    for (const root of tempRoots.splice(0)) fs.rmSync(root, { recursive: true, force: true });
});

function loadProof() {
    return JSON.parse(fs.readFileSync(path.join(artifactRoot, 'm41-product-proof.json'), 'utf8'));
}

function proofViewport(proof, viewportName) {
    return proof.viewports.find(viewport => viewport.viewportName === viewportName);
}

function mutateActiveSheet(viewport, mutation) {
    mutation(viewport.spatialReality.activeSheet);
    mutation(viewport.spatialReality.proof.sheets[0]);
}

function tempStatus(contents) {
    const root = fs.mkdtempSync(path.join(os.tmpdir(), 'athena-m41-closure-'));
    tempRoots.push(root);
    const statusPath = path.join(root, 'sprint-status.yaml');
    fs.writeFileSync(statusPath, contents, 'utf8');
    return { root, statusPath };
}

function reviewReadyStory(status = 'review') {
    return `# Story 5.3

Status: ${status}

## Tasks / Subtasks

- [x] Complete closure

## Dev Agent Record

### Debug Log References

- RED/GREEN recorded.

### Completion Notes List

- Closure implemented.

### File List

- \`closure.js\`

### Change Log

- Closure implemented.
`;
}

test('blocks closure when prior M41 story is not done and writes nothing', () => {
    const actual = fs.readFileSync(path.join(artifactRoot, 'sprint-status.yaml'), 'utf8')
        .replace(/^(\s{2}5-2-build-the-m41-e2e-evidence:\s*)[^\s#]+/m, '$1review');
    const { root, statusPath } = tempStatus(actual);
    const retrospectivePath = path.join(root, 'm41-retrospective.md');
    const storyPath = path.join(root, 'story.md');
    const beforeStatus = fs.readFileSync(statusPath, 'utf8');
    assert.throws(
        () => closure.closeM41({ statusPath, retrospectivePath, storyPath, write: true }),
        error => error.failedAuthority === 'status' && error.message.includes('5-2-build-the-m41-e2e-evidence=review')
    );
    assert.equal(fs.existsSync(retrospectivePath), false);
    assert.equal(fs.existsSync(storyPath), false);
    assert.equal(fs.readFileSync(statusPath, 'utf8'), beforeStatus);
});

test('rejects missing evidence after statuses are ready', () => {
    const statusPath = path.join(repoRoot, '_bmad-output', 'implementation-artifacts', 'm41', 'sprint-status.yaml');
    assert.throws(
        () => closure.assertM41ClosureReady({ statusPath, proofPath: path.join(path.dirname(statusPath), 'missing.json') }),
        error => error.failedAuthority === 'evidence' && error.message.includes('missing.json')
    );
    assert.throws(
        () => closure.assertM41ClosureReady({ statusPath, verificationPath: path.join(path.dirname(statusPath), 'missing-verification.json') }),
        error => error.failedAuthority === 'evidence' && error.message.includes('missing-verification.json')
    );
    const root = fs.mkdtempSync(path.join(os.tmpdir(), 'athena-m41-verification-'));
    tempRoots.push(root);
    const verificationPath = path.join(root, 'verification.json');
    const verification = JSON.parse(fs.readFileSync(path.join(artifactRoot, 'm41-verification-results.json'), 'utf8'));
    verification.commands.find(command => command.id === 'repository-test').status = 'FAIL';
    fs.writeFileSync(verificationPath, JSON.stringify(verification), 'utf8');
    assert.throws(
        () => closure.assertM41ClosureReady({ statusPath, verificationPath }),
        error => error.failedAuthority === 'evidence' && error.message.includes('repository-test')
    );
});

test('accepts current M41 evidence without mutating files', () => {
    const retrospectivePath = path.join(repoRoot, '_bmad-output', 'implementation-artifacts', 'm41', 'm41-retrospective-2026-08-04.md');
    const before = fs.readFileSync(retrospectivePath, 'utf8');
    const result = closure.assertM41ClosureReady({ repoRoot });
    assert.ok(['ready-for-dev', 'in-progress', 'review', 'done'].includes(result.storyStatus['5-3-close-m41-with-an-honest-retrospective']));
    assert.equal(result.proof.schemaVersion, 'M41.product-proof');
    assert.equal(result.proof.sourceSha256, result.baseline.sourceDigest);
    assert.equal(result.verification.schemaVersion, 'M41.verification-results');
    assert.equal(result.verification.sourceSha256, result.proof.sourceSha256);
    assert.equal(result.sheet.sheetId, 'schematic/sheet/S1');
    assert.deepEqual(result.proof.validationSummary.runtimeCounts.map(item => item.usedLanes), [7, 7]);
    assert.deepEqual(result.proof.validationSummary.spatialSpan.map(item => [item.width, item.height]), [[848, 592], [848, 592]]);
    assert.ok(result.proof.viewports.every(viewport => viewport.pixelReality.renderedComponents.length === 8));
    assert.ok(result.proof.viewports.every(viewport => viewport.pixelReality.renderedRoutes.length === 9));
    assert.equal(result.screenshots.desktop.screenshot.viewportName, 'desktop-1920x1080');
    assert.equal(result.screenshots.narrow.screenshot.viewportName, 'narrow');
    assert.equal(fs.readFileSync(retrospectivePath, 'utf8'), before);
});

test('blocks closure when a required prior story status is missing', () => {
    const actual = fs.readFileSync(path.join(artifactRoot, 'sprint-status.yaml'), 'utf8');
    const { statusPath } = tempStatus(actual.replace(/^\s{2}2-2-build-orthogonal-route-facts-and-lanes:.*\r?\n/m, ''));
    assert.throws(
        () => closure.assertM41ClosureReady({ repoRoot, statusPath }),
        error => error.failedAuthority === 'status' && error.message.includes('2-2-build-orthogonal-route-facts-and-lanes=missing')
    );
    const missingFinal = tempStatus(actual.replace(/^\s{2}m41-e5-retrospective:.*\r?\n/m, ''));
    assert.throws(
        () => closure.assertM41ClosureReady({ repoRoot, statusPath: missingFinal.statusPath }),
        error => error.failedAuthority === 'status' && error.message.includes('m41-e5-retrospective=missing')
    );
    const shadowed = tempStatus(actual
        .replace(/^(\s{2}m41-e1:\s*)done/m, '$1review')
        .concat('\nother_section:\n  m41-e1: done\n'));
    assert.throws(
        () => closure.assertM41ClosureReady({ repoRoot, statusPath: shadowed.statusPath }),
        error => error.failedAuthority === 'status' && error.message.includes('m41-e1=review')
    );
});

test('rejects changed Golden quality value', () => {
    const root = fs.mkdtempSync(path.join(os.tmpdir(), 'athena-m41-quality-'));
    const proofPath = path.join(root, 'proof.json');
    const proof = loadProof();
    for (const viewport of proof.viewports) {
        mutateActiveSheet(viewport, sheet => { sheet.quality.occupancy = 0; });
    }
    fs.writeFileSync(proofPath, JSON.stringify(proof), 'utf8');
    assert.throws(
        () => closure.assertM41ClosureReady({ repoRoot, proofPath }),
        error => error.failedAuthority === 'quality' && error.message.includes('differs from Golden value')
    );
});

test('rejects changed screenshot dimensions', () => {
    const root = fs.mkdtempSync(path.join(os.tmpdir(), 'athena-m41-screenshot-'));
    const proofPath = path.join(root, 'proof.json');
    const proof = loadProof();
    proofViewport(proof, 'narrow').screenshot.pngWidth = 721;
    fs.writeFileSync(proofPath, JSON.stringify(proof), 'utf8');
    assert.throws(
        () => closure.assertM41ClosureReady({ repoRoot, proofPath }),
        error => error.failedAuthority === 'screenshot' && error.message.includes('dimensions')
    );
    const verificationPath = path.join(root, 'verification.json');
    const verification = JSON.parse(fs.readFileSync(path.join(artifactRoot, 'm41-verification-results.json'), 'utf8'));
    verification.screenshots.find(item => item.viewportName === 'narrow').sha256 = 'sha256:bad';
    fs.writeFileSync(verificationPath, JSON.stringify(verification), 'utf8');
    assert.throws(
        () => closure.assertM41ClosureReady({ repoRoot, verificationPath }),
        error => error.failedAuthority === 'evidence' && error.message.includes('narrow')
    );
});

test('rejects screenshot evidence outside the M41 screenshot directory', () => {
    const root = fs.mkdtempSync(path.join(os.tmpdir(), 'athena-m41-external-shot-'));
    const proofPath = path.join(root, 'proof.json');
    const externalScreenshot = path.join(root, 'm41-rolling-shutter-narrow.png');
    fs.copyFileSync(path.join(artifactRoot, 'screenshots', 'm41-rolling-shutter-narrow.png'), externalScreenshot);
    const proof = loadProof();
    proofViewport(proof, 'narrow').screenshot.screenshotPath = externalScreenshot;
    fs.writeFileSync(proofPath, JSON.stringify(proof), 'utf8');
    assert.throws(
        () => closure.assertM41ClosureReady({ repoRoot, proofPath }),
        error => error.failedAuthority === 'screenshot' && error.message.includes('required M41 artifact path')
    );
});

test('rejects baseline digest that does not match the Golden source', () => {
    const root = fs.mkdtempSync(path.join(os.tmpdir(), 'athena-m41-digest-'));
    tempRoots.push(root);
    const baselinePath = path.join(root, 'baseline.properties');
    const baseline = fs.readFileSync(path.join(artifactRoot, 'm41-spatial-quality-baseline.properties'), 'utf8')
        .replace(/^fixture\.source\.sha256=.*$/m, 'fixture.source.sha256=sha256\\:bad');
    fs.writeFileSync(baselinePath, baseline, 'utf8');
    assert.throws(
        () => closure.assertM41ClosureReady({ repoRoot, baselinePath }),
        error => error.failedAuthority === 'evidence' && error.message.includes('source digest')
    );
    const incompletePath = path.join(root, 'incomplete.properties');
    const incomplete = fs.readFileSync(path.join(artifactRoot, 'm41-spatial-quality-baseline.properties'), 'utf8')
        .replace(/^generation\.command=.*\r?\n/m, '')
        .replace(/^sheet\.0\.metric\..*\r?\n/gm, '');
    fs.writeFileSync(incompletePath, incomplete, 'utf8');
    assert.throws(
        () => closure.assertM41ClosureReady({ repoRoot, baselinePath: incompletePath }),
        error => error.failedAuthority === 'evidence' && error.message.includes('baseline properties missing')
    );
});

test('blocks final write until Story 5.3 reaches review', () => {
    const root = fs.mkdtempSync(path.join(os.tmpdir(), 'athena-m41-review-gate-'));
    tempRoots.push(root);
    const statusPath = path.join(root, 'sprint-status.yaml');
    const retrospectivePath = path.join(root, 'retrospective.md');
    const storyPath = path.join(root, 'story.md');
    fs.copyFileSync(path.join(artifactRoot, 'sprint-status.yaml'), statusPath);
    fs.writeFileSync(storyPath, '# Story 5.3\n\nStatus: in-progress\n', 'utf8');
    assert.throws(
        () => closure.closeM41({ repoRoot, statusPath, retrospectivePath, storyPath, write: true }),
        error => error.failedAuthority === 'review' && error.message.includes('in-progress')
    );
    assert.equal(fs.existsSync(retrospectivePath), false);
    fs.writeFileSync(storyPath, reviewReadyStory().replace('- [x] Complete closure', '- [ ] Complete closure'), 'utf8');
    assert.throws(
        () => closure.closeM41({ repoRoot, statusPath, retrospectivePath, storyPath, write: true }),
        error => error.failedAuthority === 'review' && error.message.includes('unchecked task')
    );
    fs.writeFileSync(storyPath, `${reviewReadyStory()}\nStatus: done\n`, 'utf8');
    assert.throws(
        () => closure.closeM41({ repoRoot, statusPath, retrospectivePath, storyPath, write: true }),
        error => error.failedAuthority === 'status' && error.message.includes('exactly one Status')
    );
    fs.writeFileSync(storyPath, reviewReadyStory(), 'utf8');
    assert.throws(
        () => closure.closeM41({ repoRoot, statusPath, retrospectivePath: path.join(root, 'outside', 'retrospective.md'), storyPath, write: true }),
        error => error.failedAuthority === 'path' && error.message.includes('same M41 artifact directory')
    );
});

test('closure write is idempotent after validation', () => {
    const root = fs.mkdtempSync(path.join(os.tmpdir(), 'athena-m41-write-'));
    tempRoots.push(root);
    const statusPath = path.join(root, 'sprint-status.yaml');
    const retrospectivePath = path.join(root, 'retrospective.md');
    const storyPath = path.join(root, 'story.md');
    const status = fs.readFileSync(path.join(artifactRoot, 'sprint-status.yaml'), 'utf8')
        .replace(/^(\s{2}5-3-close-m41-with-an-honest-retrospective:\s*)[^\s#]+/m, '$1review');
    fs.writeFileSync(statusPath, status, 'utf8');
    fs.writeFileSync(storyPath, reviewReadyStory(), 'utf8');
    const firstTimestamp = '2026-08-04T18:30:00+08:00';
    const first = closure.closeM41({ repoRoot, statusPath, retrospectivePath, storyPath, write: true, now: firstTimestamp });
    const firstText = fs.readFileSync(retrospectivePath, 'utf8');
    const firstStatusText = fs.readFileSync(statusPath, 'utf8');
    const second = closure.closeM41({ repoRoot, statusPath, retrospectivePath, storyPath, write: true, now: '2026-08-04T19:00:00+08:00' });
    assert.equal(second.storyStatus['5-3-close-m41-with-an-honest-retrospective'], 'done');
    assert.equal(fs.readFileSync(retrospectivePath, 'utf8'), firstText);
    assert.equal(fs.readFileSync(statusPath, 'utf8'), firstStatusText);
    assert.match(firstStatusText, new RegExp(`^# last_updated: ${firstTimestamp.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}$`, 'm'));
    assert.match(firstStatusText, new RegExp(`^last_updated: ${firstTimestamp.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}$`, 'm'));
    assert.match(fs.readFileSync(storyPath, 'utf8'), /^Status: done$/m);
    assert.equal(first.retrospectivePath, retrospectivePath);
    fs.writeFileSync(retrospectivePath, '', 'utf8');
    assert.throws(
        () => closure.closeM41({ repoRoot, statusPath, retrospectivePath, storyPath, write: true, now: '2026-08-04T19:30:00+08:00' }),
        error => error.failedAuthority === 'evidence' && error.message.includes('retrospective differs')
    );

    const future = tempStatus(status.replace(/\n  m41-e5-retrospective: [^\r\n]+/, '\n  5-4-future-story: backlog\n  m41-e5-retrospective: optional'));
    const futureStoryPath = path.join(future.root, 'story.md');
    const futureRetrospectivePath = path.join(future.root, 'retrospective.md');
    fs.writeFileSync(futureStoryPath, reviewReadyStory(), 'utf8');
    assert.throws(
        () => closure.closeM41({ repoRoot, statusPath: future.statusPath, retrospectivePath: futureRetrospectivePath, storyPath: futureStoryPath, write: true }),
        error => error.failedAuthority === 'status' && error.message.includes('unexpected M41 story')
    );

    const invalidTime = tempStatus(status);
    const invalidTimeStoryPath = path.join(invalidTime.root, 'story.md');
    fs.writeFileSync(invalidTimeStoryPath, reviewReadyStory(), 'utf8');
    assert.throws(
        () => closure.closeM41({ repoRoot, statusPath: invalidTime.statusPath, retrospectivePath: path.join(invalidTime.root, 'retrospective.md'), storyPath: invalidTimeStoryPath, write: true, now: '2026-99-99T99:99:99+08:00' }),
        error => error.failedAuthority === 'status' && error.message.includes('timestamp')
    );

    const missingDoneTime = tempStatus(firstStatusText
        .replace(/^# last_updated:.*\r?\n/m, '')
        .replace(/^last_updated:.*\r?\n/m, ''));
    const missingDoneTimeStoryPath = path.join(missingDoneTime.root, 'story.md');
    const missingDoneTimeRetrospectivePath = path.join(missingDoneTime.root, 'retrospective.md');
    fs.writeFileSync(missingDoneTimeStoryPath, reviewReadyStory('done'), 'utf8');
    fs.writeFileSync(missingDoneTimeRetrospectivePath, firstText, 'utf8');
    assert.throws(
        () => closure.closeM41({ repoRoot, statusPath: missingDoneTime.statusPath, retrospectivePath: missingDoneTimeRetrospectivePath, storyPath: missingDoneTimeStoryPath, write: true }),
        error => error.failedAuthority === 'status' && error.message.includes('last_updated')
    );
});
