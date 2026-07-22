import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import test from 'node:test';

const repoRoot = [
    process.cwd(),
    resolve(process.cwd(), '..'),
    resolve(process.cwd(), '..', '..'),
].find(candidate => existsSync(resolve(candidate, '_bmad-output'))) ?? process.cwd();

function readJson(path) {
    return JSON.parse(readFileSync(resolve(repoRoot, path), 'utf8'));
}

function readRepoFile(path) {
    return readFileSync(resolve(repoRoot, path), 'utf8');
}

test('M31 sample project is an openable governed authoring workspace', () => {
    const sampleRoot = resolve(repoRoot, 'examples/m31/sample-project');
    const sourcePath = 'examples/m31/sample-project/src/01-governed-authoring-customer-source.athena';
    const source = readRepoFile(sourcePath);
    const readme = readRepoFile('examples/m31/sample-project/README.md');
    const idePackage = readJson('ide/package.json');
    const productPackage = readJson('ide/theia-product/package.json');

    assert.equal(existsSync(resolve(sampleRoot, 'athena.yaml')), true);
    assert.equal(existsSync(resolve(sampleRoot, 'athena.lock')), true);
    assert.match(readme, /M31/i);
    assert.match(readme, /governed engineering model authoring/i);
    assert.match(source, /^package com\.engineeringood\.m31\.sample/m);
    assert.match(source, /\bsystem RollingShutterGovernedAuthoringProof\b/);
    assert.match(source, /device MainPowerSupplyPS31 \{[\s\S]*?\n\s+port lplus \{/);
    assert.match(source, /device SpareTerminalXT31 \{[\s\S]*?\n\s+port in1 \{/);
    assert.match(source, /connect supply_feed \{/);
    assert.match(source, /connect motor_drive \{/);
    assert.match(source, /connect ControlRelayK31\.status -> PilotLampHL31\.status/);
    assert.match(source, /layout schematic-sheet \{/);
    assert.doesNotMatch(source, /^\s*port\s+[A-Za-z0-9_]+\.[A-Za-z0-9_]+\s*\{/m);
    assert.doesNotMatch(source, /qelectrotech|\.elmt|svg|viewBox|path|rectangle|circle|stroke/i);
    assert.doesNotMatch(source, /examples\/m2[9-9]\/sample-project|examples\/m30\/sample-project/i);

    assert.equal(
        idePackage.scripts['start:m31'],
        'yarn workspace @engineeringood/athena-theia-product start:m31',
    );
    assert.equal(
        productPackage.scripts['start:m31'],
        'electron scripts/athena-electron-open-workspace-main.js ../../examples/m31/sample-project',
    );
});
