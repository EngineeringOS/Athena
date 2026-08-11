import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { execFileSync } from 'node:child_process';
import { createRequire } from 'node:module';
import test from 'node:test';

const frontend = resolve(import.meta.dirname, '..');
const root = resolve(frontend, '../..');
const require = createRequire(import.meta.url);
const fixture = JSON.parse(readFileSync(resolve(root, 'contracts/presentation/v1/scene/rolling-shutter.json'), 'utf8'));

test('generated diagram contracts match canonical schemas and direct Ajv pin', () => {
  const manifest = JSON.parse(readFileSync(resolve(frontend, 'package.json'), 'utf8'));
  assert.equal(manifest.dependencies.ajv, '8.20.0');
  execFileSync(process.execPath, [resolve(import.meta.dirname, 'generate-diagram-contracts.mjs'), '--check'], { stdio: 'pipe' });
});

test('generated validators accept canonical scene and reject unknown null and bad grid fields', () => {
  const { validateAthenaDiagramScene } = require('../lib/browser/diagram/generated/validators.js');
  assert.equal(validateAthenaDiagramScene(fixture), true, JSON.stringify(validateAthenaDiagramScene.errors));
  assert.equal(validateAthenaDiagramScene({ ...fixture, renderer: 'konva' }), false);
  assert.equal(validateAthenaDiagramScene({ ...fixture, sceneId: null }), false);
  assert.equal(validateAthenaDiagramScene({ ...fixture, snapGrid: { ...fixture.snapGrid, step: 0 } }), false);
});

test('publication and edit operation validators enforce closed alternatives', () => {
  const { validateAthenaScenePublication, validateEditOperation } = require('../lib/browser/diagram/generated/validators.js');
  const revision = fixture.inputRevision;
  const ready = { schemaVersion: 1, state: 'READY', attemptedInputRevision: revision, acceptedInputRevision: revision, scene: fixture, assetBundle: { inputRevision: revision, entries: [] }, diagnostics: [] };
  assert.equal(validateAthenaScenePublication(ready), true, JSON.stringify(validateAthenaScenePublication.errors));
  assert.equal(validateAthenaScenePublication({ schemaVersion: 1, state: 'UNAVAILABLE', attemptedInputRevision: revision, diagnostics: [{ subject: 'sheet', problem: 'missing', correction: 'add companion', code: 'sheet.missing' }] }), true);
  assert.equal(validateAthenaScenePublication({ ...ready, state: 'UNAVAILABLE' }), false);
  const sourceRevision = {
    sceneInputRevision: fixture.inputRevision,
    sourceRootIdentity: `source-root:sha256:${'1'.repeat(64)}`,
    engineeringSourceDigest: '2'.repeat(64),
    sheetDigest: '3'.repeat(64),
    styleDigest: 'absent',
    lockDigest: '4'.repeat(64),
    packageItemDigests: [{ packageId: 'com.engineeringood.m44@0.1.0', itemId: 'snapshot', sha256: '5'.repeat(64) }],
    compilerVersion: 'athena-compiler-1',
    sceneSchemaVersion: 'scene-schema-2',
    profileVersion: 'svg-safe-1',
  };
  const setStyle = {
    schemaVersion: 1,
    operationId: '00000000-0000-4000-8000-000000000044',
    sceneId: fixture.sceneId,
    authorityClass: 'PRESENTATION',
    sourceRevision,
    target: { identities: ['connection'] },
    sourceTrace: { traceId: fixture.traces[0].traceId, subjectId: fixture.traces[0].origins[0].subjectId },
    requestedWritableFiles: ['src/project.sheet.style.athena'],
    body: { kind: 'SET_STYLE', sheetId: fixture.snapGrid.sheetId, target: { kind: 'ROLE', id: 'connection' }, fields: { strokeWidth: 2, routeMarker: 'END_ARROW' } },
  };
  assert.equal(validateEditOperation(setStyle), true, JSON.stringify(validateEditOperation.errors));
  assert.equal(validateEditOperation({ ...setStyle, body: { ...setStyle.body, relationship: 'power' } }), false);
  assert.equal(validateEditOperation({ ...setStyle, authorityClass: 'ENGINEERING' }), false);
  const connect = {
    ...setStyle,
    authorityClass: 'ENGINEERING',
    requestedWritableFiles: ['src/project.athena'],
    body: {
      kind: 'CONNECT_PORTS',
      connectionKind: 'WIRE',
      endpoints: [
        { role: 'SOURCE', portId: 'port:Supply.L1' },
        { role: 'SINK', portId: 'port:Breaker.line' },
      ],
      requirements: [
        { kind: 'CROSS_SECTION', value: { kind: 'QUANTITY', value: '1.5', unit: 'mm2' } },
      ],
    },
  };
  assert.equal(validateEditOperation(connect), true, JSON.stringify(validateEditOperation.errors));
  assert.equal(validateEditOperation({ ...connect, body: { ...connect.body, requirements: [] } }), false);
  assert.equal(validateEditOperation({ ...connect, body: { ...connect.body, requirements: [{ kind: 'CROSS_SECTION', value: { kind: 'SYMBOL', value: '1.5mm2' } }] } }), false);
  const reconnect = {
    ...connect,
    body: {
      kind: 'RECONNECT_CONNECTION_ENDPOINT',
      connectionId: 'connection:src/project.athena:wire:Supply.L1:Breaker.line',
      endpointRole: 'SINK',
      replacementPortId: 'port:Contactor.L1',
    },
  };
  assert.equal(validateEditOperation(reconnect), true, JSON.stringify(validateEditOperation.errors));
  const route = {
    ...setStyle,
    requestedWritableFiles: ['src/project.sheet.athena'],
    body: {
      kind: 'ADJUST_CONNECTION_ROUTE',
      sheetId: 'sheet-main',
      connectionId: 'connection:src/project.athena:wire:Supply.L1:Breaker.line',
      projectionId: 'view/sheet-main/connection/Supply-Breaker',
      target: { kind: 'SEGMENT', ordinal: 2 },
      point: { column: 17, row: 8 },
    },
  };
  assert.equal(validateEditOperation(route), true, JSON.stringify(validateEditOperation.errors));
  assert.equal(validateEditOperation({ ...route, authorityClass: 'ENGINEERING' }), false);
  assert.equal(validateEditOperation({ ...connect, body: { ...connect.body, viewportX: 120 } }), false);
  assert.equal(validateEditOperation({ ...connect, body: { ...connect.body, endpoints: [{ role: 'SOURCE', portId: 'Supply.L1' }, connect.body.endpoints[1]] } }), false);
  assert.equal(validateEditOperation({ ...connect, body: { kind: 'RECONNECT_PORT', relationshipId: 'relationship:power', endpointRole: 'SINK', portId: 'port:Breaker.line' } }), false);
  assert.equal(validateEditOperation({ ...setStyle, requestedWritableFiles: ['../project.athena'] }), false);
});

test('publication validator accepts only schema-aligned nonempty base64 asset bundles', () => {
  const { validateAthenaDiagramScene, validateAthenaScenePublication } = require('../lib/browser/diagram/generated/validators.js');
  const asset = {
    assetId: 'asset:sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
    digest: 'sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb',
    mediaKind: 'SVG',
    profileId: 'svg-safe-1',
    intrinsicBounds: { x: 0, y: 0, width: 4, height: 4 },
    bundleEntryId: 'bundle:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb',
    traceId: 'trace:sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc',
  };
  const scene = { ...fixture, assets: [asset] };
  const entry = { entryId: asset.bundleEntryId, digest: asset.digest, bytesBase64: 'PHN2Zy8+' };
  const ready = { schemaVersion: 1, state: 'READY', attemptedInputRevision: fixture.inputRevision, acceptedInputRevision: fixture.inputRevision, scene, assetBundle: { inputRevision: fixture.inputRevision, entries: [entry] }, diagnostics: [] };

  assert.equal(validateAthenaDiagramScene(scene), true, JSON.stringify(validateAthenaDiagramScene.errors));
  assert.equal(validateAthenaScenePublication(ready), true, JSON.stringify(validateAthenaScenePublication.errors));
  assert.equal(validateAthenaScenePublication({ ...ready, assetBundle: { ...ready.assetBundle, entries: [{ ...entry, bytes: [60, 115, 118, 103, 47, 62] }] } }), false);

  const font = { ...asset, mediaKind: 'WOFF2', assetId: 'asset:sha256:dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd', digest: 'sha256:eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee', bundleEntryId: 'bundle:eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee' };
  delete font.intrinsicBounds;
  assert.equal(validateAthenaDiagramScene({ ...fixture, assets: [font] }), true, JSON.stringify(validateAthenaDiagramScene.errors));
  assert.equal(validateAthenaDiagramScene({ ...fixture, assets: [{ ...font, intrinsicBounds: { x: 0, y: 0, width: 1, height: 1 } }] }), false);
});
