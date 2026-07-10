import assert from 'node:assert/strict';
import test from 'node:test';

const endpointModule = await import('../lib/browser/athena-backend-endpoint.js');

test('builds Athena backend URLs against the Theia backend when the renderer origin is file based', () => {
    assert.equal(typeof endpointModule.toAthenaBackendUrl, 'function');

    const url = endpointModule.toAthenaBackendUrl(
        'athena/repository-session/activate',
        {
            repositoryRootPath: 'D:\\Aaron\\workspace\\projects\\2026\\eos\\Athena\\examples\\m4\\open-repository-proof'
        },
        {
            protocol: 'file:',
            host: '',
            pathname: '/index.html',
            search: '?port=4545'
        }
    );

    assert.equal(
        url,
        'http://localhost:4545/athena/repository-session/activate?repositoryRootPath=D%3A%5CAaron%5Cworkspace%5Cprojects%5C2026%5Ceos%5CAthena%5Cexamples%5Cm4%5Copen-repository-proof'
    );
});
