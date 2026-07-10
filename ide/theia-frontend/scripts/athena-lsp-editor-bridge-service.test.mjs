import assert from 'node:assert/strict';
import test from 'node:test';

const { buildAthenaSourceMutationRequest } = await import('../lib/browser/athena-source-mutation-protocol.js');

test('buildAthenaSourceMutationRequest keeps the Athena request contract and carries the current editor model', async () => {
    const model = {
        id: 'current-athena-model'
    };
    const request = buildAthenaSourceMutationRequest('file:///workspace/demo.athena', model);

    assert.equal(request.method, 'athena/sourceMutationEvaluation');
    assert.deepEqual(request.params, {
        textDocument: {
            uri: 'file:///workspace/demo.athena'
        }
    });
    assert.equal(request.model, model);
});
