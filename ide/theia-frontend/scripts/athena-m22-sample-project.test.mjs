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

test('M22 sample project is an openable IDE proof with real Athena sources', () => {
    const sampleRoot = 'examples/m22/sample-project';
    const expectedFiles = [
        'examples/m22/README.md',
        `${sampleRoot}/README.md`,
        `${sampleRoot}/athena.yaml`,
        `${sampleRoot}/athena.lock`,
        `${sampleRoot}/src/01-baseline-sheet.athena`,
        `${sampleRoot}/src/02-layout-optimization-acceptance.athena`,
        `${sampleRoot}/src/03-component-round-trip.athena`,
        `${sampleRoot}/src/04-boundary-scope.athena`,
        'docs/usages/m22-proof-usage.md',
        'ide/theia-product/scripts/verify-athena-m22-sample-project.js'
    ];
    expectedFiles.forEach(assertFile);

    const idePackage = readRepoFile('ide/package.json');
    const productPackage = readRepoFile('ide/theia-product/package.json');
    const usage = readRepoFile('docs/usages/m22-proof-usage.md');
    const sampleReadme = readRepoFile(`${sampleRoot}/README.md`);
    const rootReadme = readRepoFile('examples/m22/README.md');
    const smoke = readRepoFile('ide/theia-product/scripts/verify-athena-m22-sample-project.js');

    assert.match(idePackage, /"start:m22":\s*"yarn workspace @engineeringood\/athena-theia-product start:m22"/);
    assert.match(idePackage, /"start:smoke:m22":\s*"yarn workspace @engineeringood\/athena-theia-product start:smoke:m22"/);
    assert.match(productPackage, /"start:m22":\s*"electron scripts\/athena-electron-open-workspace-main\.js \.\.\/\.\.\/examples\/m22\/sample-project"/);
    assert.match(productPackage, /"start:smoke:m22":\s*"node scripts\/verify-athena-m22-sample-project\.js"/);
    assert.match(smoke, /examples', 'm22', 'sample-project'/);
    assert.match(usage, /yarn start:m22/);
    assert.match(usage, /That command opens the sample project at `\.\.\/\.\.\/examples\/m22\/sample-project\/`\./);
    assert.match(rootReadme, /sample-project\/` is the openable IDE workspace/);
    assert.match(sampleReadme, /real `\.athena` source/);
    assert.doesNotMatch(sampleReadme, /users should inspect .*\.mjs/i);
    assert.doesNotMatch(usage, /inspect .*\.mjs/i);
});

test('M22 sample sources stay inside the accepted local Athena syntax', () => {
    const sources = [
        'examples/m22/sample-project/src/01-baseline-sheet.athena',
        'examples/m22/sample-project/src/02-layout-optimization-acceptance.athena',
        'examples/m22/sample-project/src/03-component-round-trip.athena',
        'examples/m22/sample-project/src/04-boundary-scope.athena'
    ];

    sources.forEach(sourcePath => {
        const source = readRepoFile(sourcePath);
        assert.match(source, /^package com\.engineeringood\.m22\.sample/m);
        assert.match(source, /\bsystem\s+\w+\s+\{/);
        assert.match(source, /\bdevice\s+\w+\s+\{/);
        assert.match(source, /\bport\s+\w+\.\w+\s+\{/);
        assert.match(source, /\bconnect\s+\w+\.\w+\s+->\s+\w+\.\w+/);
        assert.doesNotMatch(source, /\bimport\b/);
        assert.doesNotMatch(source, /\bregistry\b|\bmarketplace\b|\bcabinet\b|\bharness\b|\bcable tray\b|\b3D installation\b/i);
    });
});

test('M22 sample names expose layout optimization and round-trip subjects', () => {
    const optimizationSource = readRepoFile('examples/m22/sample-project/src/02-layout-optimization-acceptance.athena');
    const roundTripSource = readRepoFile('examples/m22/sample-project/src/03-component-round-trip.athena');
    const expectedOptimizationSubjects = [
        'PowerSupply24V',
        'ProtectionQF1',
        'ControllerPLC1',
        'OperatorHMI1',
        'TerminalBlockXT1',
        'PrimaryLoadM1'
    ];
    const expectedRoundTripSubjects = [
        'RoundTripControllerPLC1',
        'RoundTripHMI1',
        'RoundTripTerminalXT2',
        'RoundTripLoadM2'
    ];

    expectedOptimizationSubjects.forEach(identity => {
        assert.match(optimizationSource, new RegExp(`\\b${identity}\\b`));
    });
    expectedRoundTripSubjects.forEach(identity => {
        assert.match(roundTripSource, new RegExp(`\\b${identity}\\b`));
    });
});
