import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import test from 'node:test';
import { Parser, Language } from 'web-tree-sitter';

// Story 3.3 — proves Tree-sitter stays usable on incomplete/malformed source (AD-107) while
// the compiler/LSP path (see kernel/language's AthenaLanguageIncompleteSourceTest.kt) still
// independently owns semantic/syntax failure reporting for the exact same fixture text.
//
// Fixture-to-expectation index (kept in sync with the Kotlin-side test by fixture name):
//   unclosed-system.athena.txt        -> Tree-sitter: usable tree, well-formed device prefix intact.
//                                         Compiler: fails, "Expected '}' after system body".
//   unclosed-device-block.athena.txt  -> Tree-sitter: usable tree, well-formed system/device prefix intact.
//                                         Compiler: fails, "Expected '}' after device body" (or EOF variant).
//   dangling-connect.athena.txt       -> Tree-sitter: usable tree, connect_declaration node present
//                                         (missing `-> target`) without collapsing the sibling declarations.
//                                         Compiler: fails, "Expected '->' between connection endpoints".
//   unterminated-string.athena.txt    -> Tree-sitter: usable tree, well-formed device/property prefix intact.
//                                         Compiler: fails, "Unterminated string literal".

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const packageRoot = path.resolve(__dirname, '..');
const incompleteDir = path.join(packageRoot, 'test', 'incomplete');

const INCOMPLETE_FIXTURES = [
    'unclosed-system.athena.txt',
    'unclosed-device-block.athena.txt',
    'dangling-connect.athena.txt',
    'unterminated-string.athena.txt',
];

let language;

test.before(async () => {
    await Parser.init();
    language = await Language.load(path.join(packageRoot, 'tree-sitter-athena.wasm'));
});

for (const fixtureName of INCOMPLETE_FIXTURES) {
    test(`${fixtureName} still yields a full-range, usable Tree-sitter tree`, () => {
        const source = readFileSync(path.join(incompleteDir, fixtureName), 'utf8');
        const parser = new Parser();
        parser.setLanguage(language);
        const tree = parser.parse(source);

        assert.ok(tree, `expected a non-null tree for ${fixtureName}`);
        assert.equal(tree.rootNode.startIndex, 0, `expected rootNode to start at 0 for ${fixtureName}`);
        assert.equal(
            tree.rootNode.endIndex,
            source.length,
            `expected rootNode to cover the full source length for ${fixtureName}`
        );

        // The well-formed prefix (the already-typed system/device header) must survive as a
        // real named node rather than the whole document collapsing into one opaque ERROR node.
        const systemDeclaration = tree.rootNode.namedChildren.find(node => node && node.type === 'system_declaration');
        assert.ok(systemDeclaration, `expected a real system_declaration node for ${fixtureName}, got:\n${tree.rootNode.toString()}`);

        const nameField = systemDeclaration.childForFieldName('name');
        assert.ok(nameField, `expected the system name to remain a real (name) node for ${fixtureName}`);
        assert.equal(nameField.type, 'name');
    });
}

test('a syntactically valid but semantically invalid fixture parses with zero ERROR nodes', async () => {
    const repoRoot = path.resolve(packageRoot, '..', '..');
    const fixturePath = path.join(repoRoot, 'examples', 'm0', 'invalid-direction-cabinet.athena');
    const source = readFileSync(fixturePath, 'utf8');
    const parser = new Parser();
    parser.setLanguage(language);
    const tree = parser.parse(source);

    // AD-108: Tree-sitter's clean tree here must never be mistaken for semantic validity —
    // only compiler-owned semantic validation (see AthenaLanguageIncompleteSourceTest.kt) flags
    // this fixture's `in -> in` direction mismatch. This test asserts only the syntax-level claim.
    assert.equal(tree.rootNode.hasError, false, 'invalid-direction-cabinet.athena is syntactically clean');
});

// Epic 5 Story 5.2's checked-in `examples/m17/invalid-and-incomplete-proof/` corpus already
// proves compiler-diagnostic failure quality (`AthenaM17InvalidSourceProofTest`, `:kernel:language`).
// Per `examples/m17/README.md`'s Two-Track Verification section, the Tree-sitter-UX half of that
// same proof is this package's responsibility, run through this grammar's own web-tree-sitter
// harness (not the `tree-sitter` CLI, which would add a native-toolchain dependency this package
// otherwise avoids) and kept in a file distinct from the Kotlin/compiler-side test.
const repoRoot = path.resolve(packageRoot, '..', '..');
const M17_TREE_SITTER_UX_FIXTURES = [
    'examples/m17/invalid-and-incomplete-proof/incomplete-brace.athena',
    'examples/m17/invalid-and-incomplete-proof/missing-arrow.athena',
];

for (const relativeFixturePath of M17_TREE_SITTER_UX_FIXTURES) {
    test(`M17 Tree-sitter UX: ${relativeFixturePath} still yields a usable, full-range tree`, () => {
        const fixturePath = path.join(repoRoot, ...relativeFixturePath.split('/'));
        const source = readFileSync(fixturePath, 'utf8');
        const parser = new Parser();
        parser.setLanguage(language);
        const tree = parser.parse(source);

        assert.ok(tree, `expected a non-null tree for ${relativeFixturePath}`);
        assert.equal(tree.rootNode.startIndex, 0, `expected rootNode to start at 0 for ${relativeFixturePath}`);
        assert.equal(
            tree.rootNode.endIndex,
            source.length,
            `expected rootNode to cover the full source length for ${relativeFixturePath}`
        );
    });
}
