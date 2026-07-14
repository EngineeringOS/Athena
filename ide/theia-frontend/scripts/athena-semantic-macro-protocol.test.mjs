import assert from 'node:assert/strict';
import test from 'node:test';

const {
    buildAthenaSemanticMacroAcceptanceRequest,
    buildAthenaSemanticMacroCatalogRequest,
    buildAthenaSemanticMacroOriginInspectionRequest,
    buildAthenaSemanticMacroPreviewRequest,
    buildAthenaSemanticMacroValidationRequest
} = await import('../lib/browser/athena-semantic-macro-protocol.js');

test('buildAthenaSemanticMacroCatalogRequest keeps the Athena reuse catalog contract transport-safe', async () => {
    const model = {
        id: 'current-athena-model'
    };
    const request = buildAthenaSemanticMacroCatalogRequest({}, model);

    assert.equal(request.method, 'athena/semanticMacroCatalog');
    assert.deepEqual(request.params, {});
    assert.equal(request.model, model);
});

test('buildAthenaSemanticMacroValidationRequest keeps macro validation parameters typed and transport-safe', async () => {
    const request = buildAthenaSemanticMacroValidationRequest({
        macroId: 'macro:dol-starter',
        instantiationId: 'instance:M1',
        parameterValues: {
            motorPower: {
                kind: 'symbol',
                text: '7.5kW'
            }
        }
    });

    assert.equal(request.method, 'athena/semanticMacroValidation');
    assert.deepEqual(request.params, {
        macroId: 'macro:dol-starter',
        instantiationId: 'instance:M1',
        parameterValues: {
            motorPower: {
                kind: 'symbol',
                text: '7.5kW'
            }
        }
    });
});

test('buildAthenaSemanticMacroPreviewRequest keeps deterministic preview requests transport-safe', async () => {
    const request = buildAthenaSemanticMacroPreviewRequest({
        macroId: 'macro:dol-starter',
        instantiationId: 'instance:M1',
        parameterValues: {
            tagPrefix: {
                kind: 'symbol',
                text: 'M1'
            }
        }
    });

    assert.equal(request.method, 'athena/semanticMacroPreview');
    assert.deepEqual(request.params, {
        macroId: 'macro:dol-starter',
        instantiationId: 'instance:M1',
        parameterValues: {
            tagPrefix: {
                kind: 'symbol',
                text: 'M1'
            }
        }
    });
});

test('buildAthenaSemanticMacroAcceptanceRequest keeps review-first approval requests transport-safe', async () => {
    const request = buildAthenaSemanticMacroAcceptanceRequest({
        previewId: 'preview:dol-starter:M1',
        macroId: 'macro:dol-starter',
        instantiationId: 'instance:M1'
    });

    assert.equal(request.method, 'athena/semanticMacroAccept');
    assert.deepEqual(request.params, {
        previewId: 'preview:dol-starter:M1',
        macroId: 'macro:dol-starter',
        instantiationId: 'instance:M1'
    });
});

test('buildAthenaSemanticMacroOriginInspectionRequest keeps accepted expansion traceability requests transport-safe', async () => {
    const request = buildAthenaSemanticMacroOriginInspectionRequest({
        subjectId: 'component:instance:M1:template:starter.contactor'
    });

    assert.equal(request.method, 'athena/semanticMacroOriginInspection');
    assert.deepEqual(request.params, {
        subjectId: 'component:instance:M1:template:starter.contactor'
    });
});
