import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { resolve } from 'node:path';
import test from 'node:test';

const repoRoot = resolve(import.meta.dirname, '../../..');

test('production authoring and graph transports contain no legacy connect-ports intent contract', async () => {
    const productionFiles = [
        'kernel/authoring-model/src/main/kotlin/com/engineeringood/athena/authoring/AuthoringIntentModels.kt',
        'kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaAuthoringSessionRuntimeService.kt',
        'kernel/runtime/src/main/kotlin/com/engineeringood/athena/runtime/AthenaGraphCommandIntentRuntimeService.kt',
        'ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaAuthoringProtocol.kt',
        'ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaGraphCommandIntentProtocol.kt',
        'ide/lsp/src/main/kotlin/com/engineeringood/athena/ide/lsp/AthenaLanguageServer.kt',
        'ide/theia-frontend/src/browser/athena-authoring-protocol.ts',
        'ide/theia-frontend/src/browser/athena-graph-command-intent-protocol.ts',
        'ide/theia-frontend/src/browser/athena-graph-adapter-service.ts',
        'ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx',
    ];

    for (const relativePath of productionFiles) {
        const source = await readFile(resolve(repoRoot, relativePath), 'utf8');
        assert.doesNotMatch(
            source,
            /ConnectPortsIntent|connect-ports|buildConnectPorts|supportsConnectPorts|acceptedConnectPorts/,
            relativePath,
        );
    }
});
