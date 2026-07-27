import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';

const idePackage = JSON.parse(await readFile(
    new URL('../../package.json', import.meta.url),
    'utf8',
));

const productPackage = JSON.parse(await readFile(
    new URL('../../theia-product/package.json', import.meta.url),
    'utf8',
));

const verifierSource = await readFile(
    new URL('../../theia-product/scripts/verify-athena-m34-sample-project.js', import.meta.url),
    'utf8',
).catch(() => '');

test('M34 product smoke is a first-class Control Drawing verifier', () => {
    assert.equal(idePackage.scripts['start:m34'], 'yarn workspace @engineeringood/athena-theia-product start:m34');
    assert.equal(idePackage.scripts['start:smoke:m34'], 'yarn workspace @engineeringood/athena-theia-product start:smoke:m34');
    assert.equal(productPackage.scripts['start:m34'], 'electron scripts/athena-electron-open-workspace-main.js ../../examples/m34/professional-control-drawing');
    assert.equal(productPackage.scripts['start:smoke:m34'], 'node scripts/verify-athena-m34-sample-project.js');
    assert.match(verifierSource, /ATHENA_M34_CONTROL_DRAWING_PRODUCT_PROOF=/);
    assert.match(verifierSource, /examples', 'm34', 'professional-control-drawing'/);
    assert.match(verifierSource, /01-control-drawing\.athena/);
    assert.match(verifierSource, /m34-control-drawing-product-smoke-desktop\.png/);
    assert.match(verifierSource, /m34-control-drawing-product-smoke-narrow\.png/);
    assert.match(verifierSource, /xmlRuntimeAuthorityAbsent/);
    assert.match(verifierSource, /rawMarkupAuthorityAbsent/);
    assert.match(verifierSource, /fallbackAuthorityAbsent/);
    assert.doesNotMatch(verifierSource, /m33-cabinet-products\.xml|m33-iec\.xml|m33-cabinet-bindings\.xml|m33-iec-symbols\.xml/);
});
