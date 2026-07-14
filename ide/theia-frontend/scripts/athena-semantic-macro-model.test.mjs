import assert from 'node:assert/strict';
import test from 'node:test';

const {
    buildAthenaSemanticMacroCatalogGroups,
    classifyAthenaSemanticMacroCategory
} = await import('../lib/browser/athena-semantic-macro-model.js');

test('classifyAthenaSemanticMacroCategory keeps the first electrical proof slice in stable reuse groups', () => {
    assert.equal(classifyAthenaSemanticMacroCategory({
        macroId: 'macro:dol-starter',
        displayName: 'DOL Starter',
        summary: 'Starter assembly',
        packageName: 'com.engineeringood.electrical',
        definitionPath: 'macros/dol-starter.macro',
        classificationKeys: ['electrical', 'starter', 'dol']
    }), 'starter');
    assert.equal(classifyAthenaSemanticMacroCategory({
        macroId: 'macro:plc-rack',
        displayName: 'PLC Rack',
        summary: 'Rack assembly',
        packageName: 'com.engineeringood.electrical',
        definitionPath: 'macros/plc-rack.macro',
        classificationKeys: ['electrical', 'plc', 'rack']
    }), 'plc');
    assert.equal(classifyAthenaSemanticMacroCategory({
        macroId: 'macro:distribution-24v',
        displayName: '24V Distribution Unit',
        summary: 'Power distribution',
        packageName: 'com.engineeringood.electrical',
        definitionPath: 'macros/distribution-24v.macro',
        classificationKeys: ['electrical', 'power', 'distribution', '24v']
    }), 'power-distribution');
});

test('buildAthenaSemanticMacroCatalogGroups keeps governed catalog entries deterministic and proof-slice visible', () => {
    const groups = buildAthenaSemanticMacroCatalogGroups([
        {
            macroId: 'macro:distribution-24v',
            displayName: '24V Distribution Unit',
            summary: 'Power distribution',
            packageName: 'com.engineeringood.electrical',
            packageVersion: '0.1.0',
            definitionPath: 'macros/distribution-24v.macro',
            classificationKeys: ['electrical', 'distribution', '24v']
        },
        {
            macroId: 'macro:plc-rack',
            displayName: 'PLC Rack',
            summary: 'PLC assembly',
            packageName: 'com.engineeringood.electrical',
            packageVersion: '0.1.0',
            definitionPath: 'macros/plc-rack.macro',
            classificationKeys: ['electrical', 'plc', 'rack']
        },
        {
            macroId: 'macro:dol-starter',
            displayName: 'DOL Starter',
            summary: 'Starter assembly',
            packageName: 'com.engineeringood.electrical',
            packageVersion: '0.1.0',
            definitionPath: 'macros/dol-starter.macro',
            classificationKeys: ['electrical', 'starter', 'dol']
        }
    ]);

    assert.deepEqual(groups.map(group => group.label), ['Starter', 'PLC', 'Power Distribution']);
    assert.equal(groups[0].items[0].displayName, 'DOL Starter');
    assert.equal(groups[1].items[0].displayName, 'PLC Rack');
    assert.equal(groups[2].items[0].displayName, '24V Distribution Unit');
    assert.equal(groups[0].items[0].packageLabel, 'com.engineeringood.electrical@0.1.0');
});
