import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import test from 'node:test';

const repoRoot = fileURLToPath(new URL('../../../', import.meta.url));
const designPath = `${repoRoot}_bmad-output/implementation-artifacts/m30/qet-offline-converter-design.md`;

async function readDesign() {
  return readFile(designPath, 'utf8');
}

test('M30 QET converter design keeps QET offline and targets Representation IR', async () => {
  const design = await readDesign();

  assert.match(design, /QET \.elmt\s*->\s*QET Element AST\s*->\s*Athena Representation Definition IR candidate/);
  assert.match(design, /must not become `.athena` source/i);
  assert.match(design, /must not load QET `.elmt` files at product runtime/i);
  assert.match(design, /must not reference QET file paths from `.athena` semantic source/i);
});

test('M30 QET converter design covers required conversion concerns', async () => {
  const design = await readDesign();
  const requiredConcerns = [
    'primitive normalization',
    'style mapping',
    'terminal orientation',
    'dynamic text',
    'unsupported-feature diagnostics',
    'licensing and provenance',
    'deterministic output',
  ];

  for (const concern of requiredConcerns) {
    assert.match(design.toLowerCase(), new RegExp(concern.replaceAll(' ', '[\\s-]+')));
  }
});
