import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';

const source = fs.readFileSync(path.resolve(import.meta.dirname, '../src/browser/athena-workbench-automation.ts'), 'utf8');

test('product proof opens and focuses Explorer before source and Engineering Document', () => {
    assert.match(source, /FileNavigatorCommands\.FOCUS\.id/);
    assert.match(source, /this\.shell\.activateWidget\('files'\)/);
    assert.match(source, /FileNavigatorCommands\.REFRESH_NAVIGATOR\.id/);
    const explorer = source.indexOf('FileNavigatorCommands.FOCUS.id');
    const refresh = source.indexOf('FileNavigatorCommands.REFRESH_NAVIGATOR.id');
    const sourceOpen = source.indexOf('await open(this.openerService');
    const document = source.indexOf('AthenaCommands.REVEAL_PRESENTATION.id', sourceOpen);
    assert.ok(explorer >= 0 && explorer < refresh && refresh < sourceOpen && sourceOpen < document);
    assert.equal((source.match(/this\.shell\.activateWidget\('files'\)/g) || []).length, 2);
});
