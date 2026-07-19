import assert from 'node:assert/strict';
import { existsSync, readdirSync, readFileSync } from 'node:fs';
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

test('M24 sample project is an openable IDE proof with real Athena sources', () => {
    const sampleRoot = 'examples/m24/sample-project';
    const sourceRoot = `${sampleRoot}/src`;
    const sources = readdirSync(resolve(repoRoot, sourceRoot))
        .filter(file => file.endsWith('.athena'))
        .sort();
    const sampleReadme = readRepoFile(`${sampleRoot}/README.md`);
    const rootReadme = readRepoFile('examples/m24/README.md');
    const manifest = readRepoFile(`${sampleRoot}/athena.yaml`);
    const lock = readRepoFile(`${sampleRoot}/athena.lock`);
    const idePackage = readRepoFile('ide/package.json');
    const productPackage = readRepoFile('ide/theia-product/package.json');
    const joinedSources = sources.map(file => readRepoFile(`${sourceRoot}/${file}`)).join('\n');

    assert.deepEqual(sources, [
        '01-control-route.athena',
        '02-terminal-strip-routes.athena',
        '03-power-protection-load.athena'
    ]);
    assert.match(manifest, /name:\s*com\.engineeringood\.m24\.sample/);
    assert.match(lock, /name:\s*com\.engineeringood\.m24\.sample/);
    assert.match(idePackage, /"start:m24":\s*"yarn workspace @engineeringood\/athena-theia-product start:m24"/);
    assert.match(idePackage, /"start:smoke:m24":\s*"yarn workspace @engineeringood\/athena-theia-product start:smoke:m24"/);
    assert.match(productPackage, /"start:m24":\s*"electron scripts\/athena-electron-open-workspace-main\.js \.\.\/\.\.\/examples\/m24\/sample-project"/);
    assert.match(productPackage, /"start:smoke:m24":\s*"node scripts\/verify-athena-m24-sample-project\.js"/);
    assert.match(sampleReadme, /openable Athena workspace/i);
    assert.match(rootReadme, /yarn --cwd ide start:m24/);
    assert.doesNotMatch(sampleReadme, /\.mjs/);

    assert.match(joinedSources, /^package com\.engineeringood\.m24\.sample/m);
    assert.match(joinedSources, /system ControlRouteProof/);
    assert.match(joinedSources, /system TerminalStripRouteProof/);
    assert.match(joinedSources, /system PowerProtectionRouteProof/);
    assert.match(joinedSources, /connect ControllerPLC1\.statusOut -> OperatorHMI1\.statusIn/);
    assert.match(joinedSources, /connect TerminalRoutePLC1\.do1 -> TerminalStripXT1\.in1/);
    assert.match(joinedSources, /connect TerminalStripXT1\.out1 -> ValveY1\.command/);
    assert.match(joinedSources, /connect PowerSupplyPS1\.lplus -> BreakerQF1\.line/);
    assert.match(joinedSources, /connect ContactorKM1\.t1 -> MotorM1\.u1/);
    assert.match(joinedSources, /layout schematic-sheet/);
});
