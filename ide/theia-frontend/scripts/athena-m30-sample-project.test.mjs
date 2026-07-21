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

test('M30 sample project is an openable semantic customer-demo workspace', () => {
    const sampleRoot = resolve(repoRoot, 'examples/m30/sample-project');
    const source = readRepoFile('examples/m30/sample-project/src/01-rolling-shutter-control-source.athena');
    const readme = readRepoFile('examples/m30/sample-project/README.md');

    assert.equal(existsSync(resolve(sampleRoot, 'athena.yaml')), true);
    assert.equal(existsSync(resolve(sampleRoot, 'athena.lock')), true);
    assert.match(readme, /rolling shutter control/i);
    assert.match(readme, /semantic source/i);
    assert.match(source, /^package com\.engineeringood\.m30\.sample/m);
    assert.match(source, /\bsystem RollingShutterControlProof\b/);
    assert.match(source, /device MainPowerSupplyPS30/);
    assert.match(source, /device ControlRelayK30/);
    assert.match(source, /device FieldTerminalXT30/);
    assert.match(source, /device ShutterMotorM30/);
    assert.match(source, /port lplus \{/);
    assert.match(source, /connect supply_feed \{/);
    assert.match(source, /MainPowerSupplyPS30\.lplus -> MainBreakerQF30\.line/);
    assert.match(source, /connect motor_drive \{/);
    assert.match(source, /FieldTerminalXT30\.motorUp -> ShutterMotorM30\.up/);
    assert.match(source, /connect ControlRelayK30\.status -> PilotLampHL30\.status/);
    assert.match(source, /layout schematic-sheet \{/);

    assert.doesNotMatch(source, /qelectrotech|\.elmt|svg|viewBox|path|rectangle|circle|stroke/i);
});
