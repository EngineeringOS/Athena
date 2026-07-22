import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import test from 'node:test';

const modelPath = resolve('src/browser/athena-graph-workbench-model.ts');
const widgetPath = resolve('src/browser/athena-graph-workbench-widget.tsx');
const protocolPath = resolve('src/browser/athena-graph-command-intent-protocol.ts');

test('layout authoring transports structured intent and backend source edit only', async () => {
    const protocol = await import('../lib/browser/athena-graph-command-intent-protocol.js');
    const authoredLayoutIntent = {
        viewFamily: 'documentation',
        statements: [{
            subject: 'M1',
            relation: 'near',
            target: 'PLC1',
            priority: 'preference',
        }],
    };
    const request = protocol.buildAdjustLayoutPlacementIntentRequest({
        viewId: 'documentation',
        semanticId: 'component:M1',
        subjectKind: 'component',
        x: 100,
        y: 200,
        authoredLayoutIntent,
    });

    assert.deepEqual(request.params.authoredLayoutIntent, authoredLayoutIntent);
});

test('frontend production code owns no Athena layout serializer or insertion scanner', () => {
    const model = readFileSync(modelPath, 'utf8');
    const widget = readFileSync(widgetPath, 'utf8');
    const protocol = readFileSync(protocolPath, 'utf8');

    assert.doesNotMatch(model, /serializeAthenaGraphAuthoredLayoutIntent|buildAthenaGraphLayoutSourceEdit|lastIndexOf\(['"]}['"]\)/);
    assert.doesNotMatch(widget, /document\.getText\(\)|insertionLine|insertionCharacter/);
    assert.match(protocol, /authoredLayoutIntent/);
    assert.match(protocol, /sourceEdit/);
    assert.match(widget, /applyAuthoringSourceEdit\(preview\.sourceEdit\)/);
});
