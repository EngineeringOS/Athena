import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import test from 'node:test';
import { fileURLToPath } from 'node:url';

const scriptDirectory = path.dirname(fileURLToPath(import.meta.url));
const launcherSource = fs.readFileSync(path.join(scriptDirectory, 'athena-m46-performance-main.js'), 'utf8');

test('M46 pressure launcher preserves native IDE display scale', () => {
    assert.doesNotMatch(launcherSource, /force-device-scale-factor/);
});

test('M46 pressure launcher uses an active compositor surface', () => {
    assert.match(launcherSource, /window\.setSkipTaskbar\(true\)/);
    assert.match(launcherSource, /window\.show\(\)/);
    assert.match(launcherSource, /window\.focus\(\)/);
    assert.doesNotMatch(launcherSource, /window\.setPosition\(/);
});
