import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import { spawnSync } from 'node:child_process';
import test from 'node:test';

const layout = await import('../lib/browser/athena-product-layout.js');
const extensionsSource = fs.readFileSync(
    path.resolve(import.meta.dirname, '../src/browser/athena-workbench-extensions.ts'),
    'utf8',
);
const automationSource = fs.readFileSync(
    path.resolve(import.meta.dirname, '../src/browser/athena-workbench-automation.ts'),
    'utf8',
);
const proofHarnessSource = fs.readFileSync(
    path.resolve(import.meta.dirname, '../../theia-product/scripts/athena-m45-proof-main.js'),
    'utf8',
);
const proofRunnerSource = fs.readFileSync(
    path.resolve(import.meta.dirname, '../../theia-product/scripts/verify-athena-m45-product-proof.js'),
    'utf8',
);
const styleProofHarnessSource = fs.readFileSync(
    path.resolve(import.meta.dirname, '../../theia-product/scripts/athena-m44-style-proof-main.js'),
    'utf8',
);
const styleProofRunnerSource = fs.readFileSync(
    path.resolve(import.meta.dirname, '../../theia-product/scripts/verify-athena-m44-style-proof.js'),
    'utf8',
);
const lspBridgeSource = fs.readFileSync(
    path.resolve(import.meta.dirname, '../src/browser/athena-lsp-editor-bridge-service.ts'),
    'utf8',
);
const rollingShutterStyleSource = fs.readFileSync(
    path.resolve(import.meta.dirname, '../../../examples/m45/rolling-shutter/src/com/engineeringood/m45/rollingshutter/rolling-shutter.sheet.style.athena'),
    'utf8',
);

test('M45 product proof harness is valid JavaScript', () => {
    const proofHarnessPath = path.resolve(import.meta.dirname, '../../theia-product/scripts/athena-m45-proof-main.js');
    const result = spawnSync(process.execPath, ['--check', proofHarnessPath], { encoding: 'utf8' });
    assert.equal(result.status, 0, result.stderr);
});

test('M45 golden sheet defaults to compact black IEC linework', () => {
    assert.match(rollingShutterStyleSource, /style "default" \{\n  stroke: #000000ff\n  width: 1\n  dash: \[\]\n  font-size: 1\n  font-weight: 400/s);
    assert.match(rollingShutterStyleSource, /style "route" \{\n  stroke: #000000ff\n  width: 1\n  dash: \[\]\n  font-size: 1\n  font-weight: 400/s);
    assert.doesNotMatch(rollingShutterStyleSource, /#1180eeff|#225588ff|marker-and-label|occurrence:/);
});

test('M45 product proof runner buffers partial stdout lines', () => {
    assert.match(proofRunnerSource, /let lineBuffer = ''/);
    assert.match(proofRunnerSource, /lineBuffer \+= String\(chunk\)/);
    assert.match(proofRunnerSource, /lineBuffer\.indexOf\('\\n'\)/);
    assert.doesNotMatch(proofRunnerSource, /String\(chunk\)\.split/);
});

test('does not expand the main workbench area', () => {
    assert.equal(layout.isExpandableWorkbenchArea('main'), false);
    assert.equal(layout.isExpandableWorkbenchArea('left'), true);
    assert.equal(layout.isExpandableWorkbenchArea('bottom'), true);
});

test('repository graph stays available without stealing center width at startup', () => {
    const graphBlock = extensionsSource.match(
        /\{\s*command:\s*AthenaCommands\.REVEAL_REPOSITORY_GRAPH,[\s\S]*?\n\s*\},/,
    )?.[0];
    assert.ok(graphBlock, 'repository graph extension block missing');
    assert.doesNotMatch(graphBlock, /startupRank\s*:/);
});

test('engineering document starts with bottom dock closed and proof requires full center height', () => {
    const problemsBlock = extensionsSource.match(
        /\{\s*command:\s*AthenaCommands\.REVEAL_PROBLEMS,[\s\S]*?\n\s*\},/,
    )?.[0];
    const outputBlock = extensionsSource.match(
        /\{\s*command:\s*AthenaCommands\.REVEAL_OUTPUT,[\s\S]*?\n\s*\},/,
    )?.[0];

    assert.ok(problemsBlock, 'Problems extension block missing');
    assert.ok(outputBlock, 'Output extension block missing');
    assert.doesNotMatch(problemsBlock, /startupRank\s*:/);
    assert.doesNotMatch(outputBlock, /startupRank\s*:/);
    assert.doesNotMatch(lspBridgeSource, /outputChannel\.show\(\{\s*preserveFocus:\s*true\s*\}\)/);
    assert.match(proofHarnessSource, /hostBounds\.height\s*<\s*window\.innerHeight\s*\*\s*0\.7/);
});

test('product proof opens workspace when browser require is unavailable', () => {
    assert.match(proofHarnessSource, /resolveTheiaService/);
    assert.doesNotMatch(proofHarnessSource, /if\s*\(typeof require !== 'function'\)\s*return true/);
});

test('product proof checks composite Konva paint across all canvas layers', () => {
    assert.match(proofHarnessSource, /querySelectorAll\('\.athena-presentation__canvas-host canvas'\)/);
    assert.match(proofHarnessSource, /\.some\(canvas\s*=>/);
    assert.match(proofHarnessSource, /sample\[index \+ 3\]\s*>=\s*250/);
    assert.match(proofHarnessSource, /assetError/);
    assert.match(proofHarnessSource, /Representation asset paint failed/);
});

test('product proof derives occurrence clicks from rendered content pixels', () => {
    assert.match(proofHarnessSource, /const contentCanvas = canvases\[1\]/);
    assert.match(proofHarnessSource, /alpha\s*>=\s*250/);
    assert.match(proofHarnessSource, /\.konvajs-content/);
    assert.match(proofHarnessSource, /pointerdown/);
    assert.match(proofHarnessSource, /pointerup/);
    assert.match(proofHarnessSource, /assetsReady/);
    assert.match(proofHarnessSource, /loadedAssetCount/);
    assert.match(proofHarnessSource, /__athenaM45PaintCandidates/);
    assert.match(proofHarnessSource, /__athenaM45PaintCandidateIndex/);
    assert.match(proofHarnessSource, /const candidate = paintCandidates\[paintCandidateIndex\]/);
    assert.doesNotMatch(proofHarnessSource, /for\s*\([^)]*y[^)]*\)\s*for\s*\([^)]*x[^)]*\)/);
});

test('product proof waits for source editor trace surface after selection', () => {
    assert.match(proofHarnessSource, /selectedProof\.sourceEditorCount\s*>\s*0/);
    assert.match(proofHarnessSource, /sourceEditorCount:\s*document\.querySelectorAll\('\.monaco-editor'\)\.length/);
    assert.match(proofHarnessSource, /__athenaM45SourceTraceProof/);
    assert.match(proofHarnessSource, /revealEngineeringDocument/);
    assert.match(automationSource, /revealEngineeringDocument/);
});

test('M44 style proof drives disposable preview and server-owned solidify', () => {
    assert.match(styleProofHarnessSource, /title='Preview style'/);
    assert.match(styleProofHarnessSource, /title='Discard style preview'/);
    assert.match(styleProofHarnessSource, /title='Solidify style'/);
    assert.match(styleProofHarnessSource, /acceptedSceneDigest === discardedSceneDigest/);
    assert.match(styleProofHarnessSource, /stableSamples\s*>=\s*2/);
    assert.match(styleProofHarnessSource, /state\.nonWhiteSamples\s*>\s*0/);
    assert.match(styleProofHarnessSource, /window\.isVisible\(\)/);
    assert.match(styleProofRunnerSource, /mkdtempSync/);
    assert.match(styleProofRunnerSource, /rolling-shutter\.sheet\.style\.athena/);
    assert.match(styleProofRunnerSource, /Only Style Companion may change during solidify/);
});

test('M44 product proof checks aligned editor rulers at desktop and narrow sizes', () => {
    assert.match(styleProofHarnessSource, /rulerGeometry/);
    assert.match(styleProofHarnessSource, /capture\(window, 'rulers-desktop'\)/);
    assert.match(styleProofHarnessSource, /capture\(window, 'rulers-narrow'\)/);
    assert.match(styleProofHarnessSource, /toolbar\.bottom\s*<=\s*frame\.top/);
    assert.match(styleProofHarnessSource, /Math\.abs\(columns\.left\s*-\s*canvas\.left\)/);
    assert.match(styleProofHarnessSource, /Math\.abs\(rows\.right\s*-\s*canvas\.left\)/);
    assert.match(styleProofHarnessSource, /Math\.abs\(frame\.left\s*-\s*viewport\.left\)/);
    assert.match(styleProofHarnessSource, /columns\.height\s*<=\s*20/);
    assert.match(styleProofHarnessSource, /rows\.width\s*<=\s*20/);
    assert.match(styleProofHarnessSource, /window\.unmaximize\(\)/);
    assert.match(styleProofHarnessSource, /window\.setBounds\(\{\s*width:\s*760,\s*height:\s*720/);
    assert.match(styleProofHarnessSource, /window\.getBounds\(\)/);
});
