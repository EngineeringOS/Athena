import assert from 'node:assert/strict';
import test from 'node:test';

const {
    buildAthenaComponentPanelGroups,
    classifyAthenaComponentCategory
} = await import('../lib/browser/athena-component-panel-model.js');

test('classifyAthenaComponentCategory keeps the first electrical proof slice in stable narrow groups', () => {
    assert.equal(classifyAthenaComponentCategory({
        conceptId: 'electrical.plc.cpu',
        displayName: 'PLC CPU',
        classificationKeys: ['electrical', 'control', 'plc', 'cpu'],
        implementations: []
    }), 'plc');
    assert.equal(classifyAthenaComponentCategory({
        conceptId: 'electrical.power-supply.dc24',
        displayName: '24V DC power supply',
        classificationKeys: ['electrical', 'power', 'supply', '24vdc'],
        implementations: []
    }), 'power-supply');
    assert.equal(classifyAthenaComponentCategory({
        conceptId: 'electrical.motor.ac',
        displayName: 'AC motor',
        classificationKeys: ['electrical', 'load', 'motor', 'ac'],
        implementations: []
    }), 'motor');
    assert.equal(classifyAthenaComponentCategory({
        conceptId: 'electrical.contactor.power',
        displayName: 'Power contactor',
        classificationKeys: ['electrical', 'power-control', 'contactor'],
        implementations: []
    }), 'contactor');
    assert.equal(classifyAthenaComponentCategory({
        conceptId: 'electrical.relay.overload',
        displayName: 'Overload relay',
        classificationKeys: ['electrical', 'protection', 'relay', 'overload'],
        implementations: []
    }), 'protection');
});

test('buildAthenaComponentPanelGroups groups available components in professional left-panel order', () => {
    const groups = buildAthenaComponentPanelGroups([
        {
            conceptId: 'electrical.motor.ac',
            authoringTemplateIds: ['electrical.motor.ac.default'],
            displayName: 'AC motor',
            classificationKeys: ['electrical', 'load', 'motor', 'ac'],
            summary: 'Motor',
            implementations: [{
                implementationId: 'impl/motor',
                vendorId: 'siemens',
                vendorPartNumber: 'proof.motor.ac',
                displayName: 'Siemens proof AC motor'
            }]
        },
        {
            conceptId: 'electrical.plc.cpu',
            authoringTemplateIds: ['electrical.plc.cpu.default'],
            displayName: 'PLC CPU',
            classificationKeys: ['electrical', 'control', 'plc', 'cpu'],
            summary: 'Controller',
            implementations: [{
                implementationId: 'impl/plc',
                vendorId: 'siemens',
                vendorPartNumber: 'proof.cpu.313c',
                displayName: 'Siemens proof PLC CPU 313C'
            }]
        },
        {
            conceptId: 'electrical.contactor.power',
            authoringTemplateIds: ['electrical.contactor.power.default'],
            displayName: 'Power contactor',
            classificationKeys: ['electrical', 'power-control', 'contactor'],
            summary: 'Contactor',
            implementations: [{
                implementationId: 'impl/contactor',
                vendorId: 'siemens',
                vendorPartNumber: 'proof.contactor.3pole',
                displayName: 'Siemens proof power contactor'
            }]
        }
    ]);

    assert.deepEqual(groups.map(group => group.label), ['PLC', 'Motor', 'Contactor']);
    assert.equal(groups[0].items[0].displayName, 'PLC CPU');
    assert.equal(groups[0].items[0].conceptTemplateId, 'electrical.plc.cpu.default');
    assert.equal(groups[0].items[0].preferredImplementation?.vendorPartNumber, 'proof.cpu.313c');
    assert.equal(groups[2].items[0].conceptId, 'electrical.contactor.power');
});
