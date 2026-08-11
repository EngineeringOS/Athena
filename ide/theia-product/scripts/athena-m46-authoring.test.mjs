import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';
import { fileURLToPath } from 'node:url';

const productRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const scriptsRoot = path.join(productRoot, 'scripts');
const launcherPath = path.join(scriptsRoot, 'athena-m46-authoring-main.js');
const verifierPath = path.join(scriptsRoot, 'verify-athena-m46-authoring.js');
const packageJson = JSON.parse(fs.readFileSync(path.join(productRoot, 'package.json'), 'utf8'));

const read = file => fs.existsSync(file) ? fs.readFileSync(file, 'utf8') : '';
const launcher = read(launcherPath);
const verifier = read(verifierPath);

test('M46 authoring launcher preserves native IDE scale on a visible compositor surface', () => {
    assert.doesNotMatch(launcher, /force-device-scale-factor/);
    assert.match(launcher, /window\.setSkipTaskbar\(true\)/);
    assert.match(launcher, /window\.show\(\)/);
    assert.match(launcher, /x: 80, y: 80/);
    assert.doesNotMatch(launcher, /-32000/);
});

test('M46 launcher proves workspace readiness before authoring', () => {
    assert.match(launcher, /__athenaWorkbenchAutomation/);
    assert.match(launcher, /workspaceOpened/);
    assert.match(launcher, /workspaceRoots/);
    assert.match(launcher, /repositoryLifecycle/);
    assert.match(launcher, /lspRepositoryRoot/);
    assert.match(launcher, /getConnectionReadModel/);
    assert.match(launcher, /publicationState\s*===\s*'READY'/);
});

test('M46 launcher uses typed connection route and journal automation only', () => {
    assert.match(launcher, /executeReconnect/);
    assert.match(launcher, /executeRouteAdjust/);
    assert.match(launcher, /executeUndo/);
    assert.match(launcher, /executeRedo/);
    assert.doesNotMatch(launcher, /writeFileSync\(sourcePath|writeFileSync\(repositoryRoot/);
});

test('M46 verifier isolates active example and enforces authority-owned writes', () => {
    assert.match(verifier, /examples[^\n]+m46[^\n]+rolling-shutter/);
    assert.match(verifier, /mkdtempSync/);
    assert.match(verifier, /cpSync/);
    assert.match(verifier, /activeExampleUnchanged/);
    assert.match(verifier, /changedPaths/);
    assert.match(verifier, /rolling-shutter\.athena/);
    assert.match(verifier, /rolling-shutter\.sheet\.athena/);
    assert.match(verifier, /terminateProcessTree/);
});

test('M46 verifier publishes desktop narrow and reopen evidence', () => {
    assert.match(launcher, /desktop/);
    assert.match(launcher, /narrow/);
    assert.match(verifier, /author/);
    assert.match(verifier, /reopen/);
    assert.equal(packageJson.scripts['verify:m46-authoring'], 'node scripts/verify-athena-m46-authoring.js');
});

test('M46 screenshot proof dismisses transient operation diagnostics before capture', () => {
    assert.match(launcher, /operations\.push\(\{ name: 'staleRedo',[\s\S]*await execute\(window, 'clearEvidence'\);/);
    assert.match(launcher, /await dismissTransientNotifications\(window\);[\s\S]*capture\(window, 'desktop'\)/);
    assert.match(launcher, /function dismissTransientNotifications\(window\)/);
});
