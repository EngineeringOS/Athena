import assert from 'node:assert/strict';
import { readFile, readdir } from 'node:fs/promises';
import path from 'node:path';
import test from 'node:test';
import { fileURLToPath } from 'node:url';

const repositoryRoot = fileURLToPath(new URL('../../../', import.meta.url));
const sampleRoot = path.join(repositoryRoot, 'examples', 'm34', 'professional-control-drawing');
const sampleSourcePath = path.join(sampleRoot, 'src', '01-control-drawing.athena');
const layoutDeriverPath = path.join(
    repositoryRoot,
    'kernel',
    'compiler',
    'src',
    'main',
    'kotlin',
    'com',
    'engineeringood',
    'athena',
    'compiler',
    'semantic',
    'ProjectSemanticSchematicLayoutFactDeriver.kt',
);

async function filesBelow(root) {
    const entries = await readdir(root, { withFileTypes: true });
    const nested = await Promise.all(entries.map(async entry => {
        const candidate = path.join(root, entry.name);
        return entry.isDirectory() ? filesBelow(candidate) : [candidate];
    }));
    return nested.flat();
}

test('corrective sample authors engineering devices only, never drawing structure as devices', async () => {
    const source = await readFile(sampleSourcePath, 'utf8');

    assert.doesNotMatch(
        source,
        /\bdevice\s+\w*(?:Enclosure|Rail|RouteChannel|Frame|Zone|TitleLabel)\w*\s*\{/i,
    );
    assert.match(source, /\bdevice\s+PowerSourceG34\b/);
    assert.match(source, /\btype\s+PowerSource\b/);
    assert.match(source, /\bdevice\s+ShutterMotorM34\b/);
});

test('active schematic layout derivation does not infer engineering roles from identifiers', async () => {
    const source = await readFile(layoutDeriverPath, 'utf8');

    assert.doesNotMatch(source, /substringAfterLast\([^)]*\)\.uppercase\(\)/);
    assert.doesNotMatch(source, /startsWith\("(?:PLC|HMI|XT|QF|M)"\)/);
});

test('corrective sample has no QET, ELMT, or XML runtime material', async () => {
    const files = await filesBelow(sampleRoot);
    const forbidden = files.filter(file => /\.(?:qet|elmt|xml)$/i.test(file));

    assert.deepEqual(forbidden, []);
});
