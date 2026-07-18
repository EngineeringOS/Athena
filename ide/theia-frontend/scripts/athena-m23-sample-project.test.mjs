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

test('M23 sample project is an openable IDE proof with real admitted layout syntax', () => {
    const sampleRoot = 'examples/m23/sample-project';
    [
        'examples/m23/README.md',
        `${sampleRoot}/README.md`,
        `${sampleRoot}/athena.yaml`,
        `${sampleRoot}/athena.lock`,
        `${sampleRoot}/src/01-layout-hints.athena`,
        'ide/theia-product/scripts/verify-athena-m23-sample-project.js',
    ].forEach(assertFile);

    const idePackage = readRepoFile('ide/package.json');
    const productPackage = readRepoFile('ide/theia-product/package.json');
    const source = readRepoFile(`${sampleRoot}/src/01-layout-hints.athena`);
    const sampleReadme = readRepoFile(`${sampleRoot}/README.md`);

    assert.match(idePackage, /"start:m23":\s*"yarn workspace @engineeringood\/athena-theia-product start:m23"/);
    assert.match(productPackage, /"start:m23":\s*"electron scripts\/athena-electron-open-workspace-main\.js \.\.\/\.\.\/examples\/m23\/sample-project"/);
    assert.match(productPackage, /"start:smoke:m23":\s*"node scripts\/verify-athena-m23-sample-project\.js"/);
    assert.match(source, /^package com\.engineeringood\.m23\.sample/m);
    assert.match(source, /\bsystem\s+LayoutHintRoundTrip\s+\{/);
    assert.match(source, /\blayout schematic-sheet\s+\{/);
    assert.match(source, /\bplace OperatorHMI1 near ControllerPLC1\b/);
    assert.match(source, /\bplace TerminalBlockXT1 below ControllerPLC1\b/);
    assert.match(source, /\balign OperatorHMI1 aligned-with ControllerPLC1 axis vertical\b/);
    assert.match(source, /\bgroup OperatorHMI1 grouped-with ControllerPLC1\b/);
    assert.ok(source.indexOf('layout schematic-sheet {') < source.lastIndexOf('}'));
    assert.match(sampleReadme, /real `\.athena` layout block/);
    assert.doesNotMatch(sampleReadme, /users should inspect .*\.mjs/i);
});
