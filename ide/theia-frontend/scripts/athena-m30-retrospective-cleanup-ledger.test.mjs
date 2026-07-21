import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';

const retrospectivePath = '_bmad-output/implementation-artifacts/m30/m30-retrospective.md';
const cleanupLedgerPath = '_bmad-output/implementation-artifacts/m30/cleanup-ledger.md';

test('M30 retrospective records visual credibility failure causes and renderer lesson', async () => {
  const retrospective = await readFile(retrospectivePath, 'utf8');

  assert.match(retrospective, /pre-M30 visual credibility failure causes/i);
  assert.match(retrospective, /weak representation semantics/i);
  assert.match(retrospective, /generic wrappers/i);
  assert.match(retrospective, /hard-coded viewBox/i);
  assert.match(retrospective, /off-screen duplicate/i);
  assert.match(retrospective, /do not patch renderer around missing representation semantics/i);
});

test('M30 cleanup ledger entries carry owner reason and target milestone', async () => {
  const ledger = await readFile(cleanupLedgerPath, 'utf8');
  const entries = ledger.split(/\n(?=ID: M30-CL-\d+)/).filter((entry) => entry.startsWith('ID: '));

  assert.ok(entries.length >= 3, 'expected ledger entries for retained bridge, screenshot, and retrospective cleanup review');

  for (const entry of entries) {
    assert.match(entry, /^Owner:\s+\S/m);
    assert.match(entry, /^Reason:\s+\S/m);
    assert.match(entry, /^Target milestone:\s+\S/m);
    assert.match(entry, /^Verification:\s+\S/m);
  }
});
