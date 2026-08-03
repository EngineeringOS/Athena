import assert from 'node:assert/strict';
import { access, readFile } from 'node:fs/promises';
import path from 'node:path';
import test from 'node:test';
import { fileURLToPath } from 'node:url';

const repositoryRoot = fileURLToPath(new URL('../../../', import.meta.url));
const targetImagePath = path.join(
    repositoryRoot,
    'draft',
    'screenshort',
    "equipement_d'un_volet_roulant.png",
);
const targetContractPath = path.join(
    repositoryRoot,
    '_bmad-output',
    'implementation-artifacts',
    'm34',
    'm34-professional-renderer-target.md',
);
const qetEvidencePath = path.join(
    repositoryRoot,
    'reference',
    'qelectrotech-source-mirror',
    'examples',
    'schema_indus.qet',
);
const dedicatedSampleRoot = path.join(
    repositoryRoot,
    'examples',
    'm34',
    'professional-control-drawing',
);

function pngSize(buffer) {
    assert.equal(buffer.subarray(1, 4).toString('ascii'), 'PNG');
    return {
        width: buffer.readUInt32BE(16),
        height: buffer.readUInt32BE(20),
    };
}

test('approved professional drawing target freezes measurable sheet geometry and tolerance', async () => {
    const [image, contract] = await Promise.all([
        readFile(targetImagePath),
        readFile(targetContractPath, 'utf8'),
    ]);

    assert.deepEqual(pngSize(image), { width: 1050, height: 720 });
    assert.match(contract, /17 numbered columns/);
    assert.match(contract, /8 lettered rows/);
    assert.match(contract, /target raster:\s*`1050 x 720`/i);
    assert.match(contract, /aspect-ratio tolerance:\s*`2%`/i);
    assert.match(contract, /separate power and control circuit regions/);
    assert.match(contract, /author, title, file, date, and folio title-block regions/);
});

test('offline QET evidence covers the required engineering families without becoming runtime input', async () => {
    const qet = await readFile(qetEvidencePath, 'utf8');

    assert.match(qet, /<diagram\b[^>]*\bcols="17"/);
    assert.match(qet, /<diagram\b[^>]*\brows="8"/);
    for (const family of [
        'src_3p_pe_n',
        'disjoncteur_magneto-thermique',
        'sectionneur_fusible_bi',
        'transfo_mono_2',
        'contacteur_inverseur',
        'bobine',
        'contact_nc',
        'bouton_poussoir',
        'fin_course_nc',
        'voyant2',
        'borne_continuite',
        'moteur_tri_2',
        'terre',
    ]) {
        assert.match(qet, new RegExp(family.replaceAll('-', '\\-')), `missing offline family ${family}`);
    }
});

test('dedicated sample is an admitted Athena project with package-local representation material', async () => {
    const manifestPath = path.join(dedicatedSampleRoot, 'athena.yaml');
    await access(manifestPath);
    const manifest = await readFile(manifestPath, 'utf8');

    assert.match(manifest, /sourceRoot:\s+src/);
    assert.match(manifest, /representationPackageRoots:\s*\r?\n\s*- packages\/representation/);
});
