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

test('M25 product smoke is wired to the openable sample project', () => {
    const idePackage = readJson('ide/package.json');
    const productPackage = readJson('ide/theia-product/package.json');
    const smokeScript = resolve(repoRoot, 'ide/theia-product/scripts/verify-athena-m25-sample-project.js');

    assert.equal(
        idePackage.scripts['start:smoke:m25'],
        'yarn workspace @engineeringood/athena-theia-product start:smoke:m25',
    );
    assert.equal(
        productPackage.scripts['start:smoke:m25'],
        'node scripts/verify-athena-m25-sample-project.js',
    );
    assert.equal(
        productPackage.scripts['start:m25'],
        'electron scripts/athena-electron-open-workspace-main.js ../../examples/m25/sample-project',
    );
    assert.ok(existsSync(smokeScript), 'Missing M25 product smoke script.');
});
