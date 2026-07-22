import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import test from 'node:test';

const bridgePath = resolve('src/browser/athena-lsp-editor-bridge-service.ts');
const callerPaths = [
    resolve('src/browser/athena-component-panel-widget.tsx'),
    resolve('src/browser/athena-graph-workbench-widget.tsx'),
    resolve('src/browser/athena-semantic-inspection-widget.tsx'),
];

test('authoring revision guard accepts only the exact UTF-8 editor revision', async () => {
    const { assertAuthoringRevisionGuard } = await import('../lib/browser/athena-authoring-revision-guard.js');
    const contentSha256 = 'd904ee3972546d7eeed254f021626a4649d0dd27791ab5b0b89e4bb0c2492764';
    const edit = {
        uri: 'file:///workspace/main.athena',
        revisionGuard: {
            semanticSnapshotId: 'snapshot:m31',
            sourceUri: 'file:///workspace/main.athena',
            documentVersion: 7,
            contentSha256,
        },
    };
    const document = {
        uri: edit.uri,
        version: 7,
        text: 'system M31 {}',
    };

    await assert.doesNotReject(assertAuthoringRevisionGuard(edit, document));
    await assert.rejects(
        assertAuthoringRevisionGuard(edit, { ...document, uri: 'file:///workspace/other.athena' }),
        /URI mismatch/,
    );
    await assert.rejects(
        assertAuthoringRevisionGuard(edit, { ...document, version: 8 }),
        /version mismatch/,
    );
    await assert.rejects(
        assertAuthoringRevisionGuard(edit, { ...document, text: 'system Changed {}' }),
        /content mismatch/,
    );
    await assert.rejects(
        assertAuthoringRevisionGuard({ uri: edit.uri }, document),
        /requires a Revision Guard/,
    );
});

test('editor bridge verifies revision guard before applying and callers await completion', () => {
    const bridge = readFileSync(bridgePath, 'utf8');
    const guardIndex = bridge.indexOf('await assertAuthoringRevisionGuard');
    const editIndex = bridge.indexOf('model.pushEditOperations', guardIndex);
    const authorityGuardIndex = bridge.indexOf('if (edit.appliedByAuthority)');

    assert.ok(authorityGuardIndex >= 0, 'bridge must skip edits already applied by Mutation Authority');
    assert.ok(guardIndex >= 0, 'bridge must await Revision Guard validation');
    assert.ok(editIndex > guardIndex, 'guard validation must happen before editor mutation');
    for (const callerPath of callerPaths) {
        const caller = readFileSync(callerPath, 'utf8');
        assert.doesNotMatch(caller, /(?<!await )this\.lspEditorBridgeService\.applyAuthoringSourceEdit\(/);
    }
});
