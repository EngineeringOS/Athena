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

function assertFile(path) {
    assert.equal(existsSync(resolve(repoRoot, path)), true, `${path} should exist`);
}

test('M21 sample project is an openable IDE proof with real Athena sources', () => {
    const sampleRoot = 'examples/m21/sample-project';
    const expectedFiles = [
        'examples/m21/README.md',
        `${sampleRoot}/README.md`,
        `${sampleRoot}/athena.yaml`,
        `${sampleRoot}/athena.lock`,
        `${sampleRoot}/src/01-baseline-sheet.athena`,
        `${sampleRoot}/src/02-layout-intelligence-acceptance.athena`,
        `${sampleRoot}/src/03-routing-and-label-readability.athena`,
        `${sampleRoot}/src/04-boundary-scope.athena`,
        'docs/usages/m21-proof-usage.md'
    ];
    expectedFiles.forEach(assertFile);

    const idePackage = readRepoFile('ide/package.json');
    const productPackage = readRepoFile('ide/theia-product/package.json');
    const usage = readRepoFile('docs/usages/m21-proof-usage.md');
    const sampleReadme = readRepoFile(`${sampleRoot}/README.md`);
    const rootReadme = readRepoFile('examples/m21/README.md');
    const opener = readRepoFile('ide/theia-product/scripts/athena-electron-open-workspace-main.js');
    const smoke = readRepoFile('ide/theia-product/scripts/verify-athena-m21-sample-project.js');

    assert.match(idePackage, /"start:m21":\s*"yarn workspace @engineeringood\/athena-theia-product start:m21"/);
    assert.match(idePackage, /"start:smoke:m21":\s*"yarn workspace @engineeringood\/athena-theia-product start:smoke:m21"/);
    assert.match(productPackage, /"start:m21":\s*"electron scripts\/athena-electron-open-workspace-main\.js \.\.\/\.\.\/examples\/m21\/sample-project"/);
    assert.match(productPackage, /"start:smoke:m21":\s*"node scripts\/verify-athena-m21-sample-project\.js"/);
    assert.match(opener, /process\.argv\[2\]/);
    assert.match(opener, /target workspace URL fragment/);
    assert.doesNotMatch(opener, /WorkspaceService/);
    assert.match(opener, /ATHENA_WORKSPACE_OPENED=/);
    assert.match(smoke, /examples', 'm21', 'sample-project'/);
    assert.match(usage, /yarn start:m21/);
    assert.match(usage, /That command opens the sample project at `\.\.\/\.\.\/examples\/m21\/sample-project\/`\./);
    assert.match(rootReadme, /sample-project\/` is the openable IDE workspace/);
    assert.match(sampleReadme, /real `\.athena` source/);
    assert.doesNotMatch(sampleReadme, /users should inspect .*\.mjs/i);
    assert.doesNotMatch(usage, /inspect .*\.mjs/i);
});

test('M21 sample sources stay inside the accepted local Athena syntax', () => {
    const sources = [
        'examples/m21/sample-project/src/01-baseline-sheet.athena',
        'examples/m21/sample-project/src/02-layout-intelligence-acceptance.athena',
        'examples/m21/sample-project/src/03-routing-and-label-readability.athena',
        'examples/m21/sample-project/src/04-boundary-scope.athena'
    ];

    sources.forEach(sourcePath => {
        const source = readRepoFile(sourcePath);
        assert.match(source, /^package com\.engineeringood\.m21\.sample/m);
        assert.match(source, /\bsystem\s+\w+\s+\{/);
        assert.match(source, /\bdevice\s+\w+\s+\{/);
        assert.match(source, /\bport\s+\w+\.\w+\s+\{/);
        assert.match(source, /\bconnect\s+\w+\.\w+\s+->\s+\w+\.\w+/);
        assert.doesNotMatch(source, /\bimport\b/);
        assert.doesNotMatch(source, /\bregistry\b|\bmarketplace\b|\bcabinet\b|\bharness\b|\bcable tray\b|\b3D installation\b/i);
    });
});

test('M21 routing sample preserves source endpoint identity vocabulary', () => {
    const routingSource = readRepoFile('examples/m21/sample-project/src/03-routing-and-label-readability.athena');
    const expectedIdentities = [
        'RouteSensorS1',
        'RoutePLC1',
        'RouteTerminalXT2',
        'RouteActuatorY1',
        'SensorSignal',
        'ActuatorCommand'
    ];

    expectedIdentities.forEach(identity => {
        assert.match(routingSource, new RegExp(`\\b${identity}\\b`));
    });
    assert.match(routingSource, /connect RouteSensorS1\.out -> RoutePLC1\.input/);
    assert.match(routingSource, /connect RoutePLC1\.output -> RouteTerminalXT2\.in/);
    assert.match(routingSource, /connect RouteTerminalXT2\.out -> RouteActuatorY1\.in/);
});
