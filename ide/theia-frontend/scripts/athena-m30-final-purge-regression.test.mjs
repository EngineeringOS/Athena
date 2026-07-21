import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';

const reportPath = '_bmad-output/implementation-artifacts/m30/final-purge-regression-report.md';
const sprintStatusPath = '_bmad-output/implementation-artifacts/m30/sprint-status.yaml';

test('M30 final purge report records stale artifact and regression evidence', async () => {
  const report = await readFile(reportPath, 'utf8');

  assert.match(report, /stale artifact audit/i);
  assert.match(report, /old screenshots/i);
  assert.match(report, /obsolete renderer fallbacks/i);
  assert.match(report, /dead sample paths/i);
  assert.match(report, /misleading design claims/i);
  assert.match(report, /M27 regression smoke/i);
  assert.match(report, /M28 regression smoke/i);
  assert.match(report, /M29 regression smoke/i);
  assert.match(report, /M30 product smoke/i);
  assert.match(report, /encoding audit passed/i);
});

test('M30 sprint status has no duplicate development status keys', async () => {
  const text = await readFile(sprintStatusPath, 'utf8');
  const seen = new Set();
  const duplicates = [];
  let inDevelopmentStatus = false;

  for (const line of text.split(/\r?\n/)) {
    if (/^development_status:\s*$/.test(line)) {
      inDevelopmentStatus = true;
      continue;
    }
    if (inDevelopmentStatus && /^\S/.test(line) && line.trim() !== '') {
      inDevelopmentStatus = false;
    }
    if (!inDevelopmentStatus) {
      continue;
    }
    const match = line.match(/^\s{2}([^:#]+):\s*\S+/);
    if (!match) {
      continue;
    }
    const key = match[1].trim();
    if (seen.has(key)) {
      duplicates.push(key);
    }
    seen.add(key);
  }

  assert.deepEqual(duplicates, []);
});
