import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import test from 'node:test';

const repoRoot = [
    process.cwd(),
    resolve(process.cwd(), '..'),
    resolve(process.cwd(), '..', '..'),
].find(candidate => existsSync(resolve(candidate, '_bmad-output'))) ?? process.cwd();

function readRepoFile(path) {
    return readFileSync(resolve(repoRoot, path), 'utf8');
}

test('M24 routing acceptance names M23 baseline, M24 route facts, and deferred parity', () => {
    const acceptance = readRepoFile('docs/usages/m24-routing-acceptance-proof.md');
    const usage = readRepoFile('docs/usages/m24-proof-usage.md');
    const retrospective = readRepoFile('_bmad-output/implementation-artifacts/m24/m24-achievement-usage-retrospective-2026-07-19.md');
    const m23Usage = readRepoFile('docs/usages/m23-proof-usage.md');
    const examplesReadme = readRepoFile('examples/m24/README.md');
    const sampleSource = readRepoFile('examples/m24/sample-project/src/02-terminal-strip-routes.athena');
    const prd = readRepoFile('_bmad-output/planning-artifacts/prds/prd-Athena-2026-07-18-m24/prd.md');
    const architecture = readRepoFile('_bmad-output/planning-artifacts/architecture/architecture-Athena-2026-07-18-m24/ARCHITECTURE-SPINE.md');

    assert.match(acceptance, /M23 made layout intent real `\.athena` language/i);
    assert.match(acceptance, /terminal-anchor route facts/i);
    assert.match(acceptance, /\.\.\/\.\.\/draft\/screenshort\/coffret_cordons_chauffants\.png/);
    assert.match(acceptance, /\.\.\/\.\.\/examples\/m24\/sample-project/);
    assert.match(acceptance, /full EPLAN parity/i);
    assert.match(acceptance, /cabinet routing parity/i);
    assert.match(acceptance, /physical wire routing/i);
    assert.match(acceptance, /route editing or route-hint syntax/i);
    assert.match(usage, /examples\/m24\/sample-project/);
    assert.match(usage, /schematic topology routing only/i);
    assert.match(usage, /centerFallbackRouteIds=\[\]/);
    assert.match(usage, /M24 adds no new route syntax/i);
    assert.match(usage, /ANTLR4 and Tree-sitter/i);
    assert.match(retrospective, /What M24 Actually Proves/i);
    assert.match(retrospective, /What Remains Deferred/i);
    assert.match(retrospective, /duplicate device names across sample files/i);
    assert.match(retrospective, /stale-host lesson/i);
    assert.match(m23Usage, /M23 supports system-scoped layout blocks/i);
    assert.match(examplesReadme, /routing acceptance proof/i);
    assert.match(sampleSource, /connect TerminalRoutePLC1\.do1 -> TerminalStripXT1\.in1/);
    assert.match(prd, /M23.*baseline comparison/i);
    assert.match(architecture, /M24 routing is schematic topology only/i);

    [
        /M24\s+(achieves|delivers|implements)\s+full\s+EPLAN\s+parity/i,
        /M24\s+(achieves|delivers|implements)\s+cabinet\s+routing\s+parity/i,
        /physical\s+wire\s+routing\s+is\s+supported/i,
        /harness\s+routing\s+is\s+supported/i,
        /route-hint\s+syntax\s+is\s+supported/i,
        /ELK\s+is\s+the\s+Athena\s+architecture/i,
    ].forEach(overclaim => {
        assert.doesNotMatch(acceptance, overclaim);
        assert.doesNotMatch(usage, overclaim);
        assert.doesNotMatch(retrospective, overclaim);
    });
});
