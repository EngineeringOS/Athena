import assert from 'node:assert/strict';
import test from 'node:test';

const { AthenaTreeSitterHighlightingService } = await import('../lib/browser/athena-tree-sitter-highlighting-service.js');

test('Tree-sitter highlights M42 Entity anatomy without owning diagnostics', async () => {
    const service = new AthenaTreeSitterHighlightingService();
    const source = [
        'system Anatomy {',
        '  entity Drive {',
        '    concept core.Drive',
        '    function main {',
        '      role core.main',
        '      port powerIn { direction in flow core.Power }',
        '    }',
        '  }',
        '}'
    ].join('\n');
    const tokens = await service.provideDocumentSemanticTokens({ getValue: () => source });

    assert.ok(tokens);
    assert.ok(tokens.data.length > 0);
    assert.equal(service.getLastFailureMessage(), undefined);
});
