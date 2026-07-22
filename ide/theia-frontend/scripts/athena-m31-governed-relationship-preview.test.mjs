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

function methodBody(source, methodName) {
    const methodPattern = new RegExp(`protected\\s+(?:async\\s+)?${methodName}\\b`);
    const match = source.match(methodPattern);
    const start = match?.index ?? -1;
    assert.notEqual(start, -1, `${methodName} should exist`);
    const nextMethod = source.indexOf('\n    protected ', start + methodName.length);
    return source.slice(start, nextMethod === -1 ? source.length : nextMethod);
}

const protocolSource = readRepoFile('ide/theia-frontend/src/browser/athena-authoring-protocol.ts');
const graphWorkbenchSource = readRepoFile('ide/theia-frontend/src/browser/athena-graph-workbench-widget.tsx');

test('M31 relationship preview keeps generic semantic relationship transport only', () => {
    assert.match(protocolSource, /AthenaAuthoringRelationshipEvidencePayload/);
    assert.match(protocolSource, /relationshipEvidence\?: AthenaAuthoringRelationshipEvidencePayload/);
    assert.match(protocolSource, /buildSemanticRelationshipPreviewRequest/);
    assert.match(protocolSource, /intentKind:\s*'semantic-relationship'/);
    assert.doesNotMatch(protocolSource, /ConnectPortsIntent|buildConnectPortsPreviewRequest|intentKind:\s*'connect-ports'/);
    assert.doesNotMatch(graphWorkbenchSource, /ConnectPortsIntent|buildConnectPortsPreviewRequest|intentKind:\s*'connect-ports'/);
});

test('Graphical View relationship candidates are capability-derived adapter evidence', () => {
    const nodeBody = methodBody(graphWorkbenchSource, 'renderGraphNode');
    assert.match(graphWorkbenchSource, /isRelationshipCandidateNode/);
    assert.match(graphWorkbenchSource, /relationshipCandidateReason/);
    assert.match(graphWorkbenchSource, /relationship compatibility evidence/i);
    assert.match(nodeBody, /this\.isRelationshipCandidateNode\(node\)/);
    assert.doesNotMatch(nodeBody, /!!this\.connectPortsSource\s*&&\s*isConnectablePort\s*&&/);
    assert.doesNotMatch(graphWorkbenchSource, /document\.querySelector|querySelector|canvasCoordinates|textContent|innerText|visibleLabel|svgId|classList/i);
});

test('Graphical View renders governed relationship preview evidence and controls', () => {
    assert.match(graphWorkbenchSource, /renderSemanticRelationshipPreview/);
    assert.match(graphWorkbenchSource, /relationshipEvidence/);
    assert.match(graphWorkbenchSource, /sourceSubjectId/);
    assert.match(graphWorkbenchSource, /targetSubjectId/);
    assert.match(graphWorkbenchSource, /relationshipType/);
    assert.match(graphWorkbenchSource, /compatibility/);
    assert.match(graphWorkbenchSource, /routePreview/);
    assert.match(graphWorkbenchSource, /revisionGuard/);
    assert.match(graphWorkbenchSource, /diagnostic\.authority/);
    assert.match(graphWorkbenchSource, /diagnostic\.lifecycleStage/);
    assert.match(graphWorkbenchSource, /acceptConnectPreview/);
    assert.match(graphWorkbenchSource, /rejectConnectPreview/);
    assert.match(graphWorkbenchSource, /cancelConnectPreview/);
    assert.doesNotMatch(graphWorkbenchSource, /guided connection|guided connect|connect ports request/i);
});

test('Graphical View accepts relationship previews only after committed matching source evidence', () => {
    const acceptBody = methodBody(graphWorkbenchSource, 'acceptConnectPreview');
    assert.match(acceptBody, /preview\.relationshipEvidence\?\.sourceEdit/);
    assert.match(acceptBody, /currentEditorMatchesConnectPreview\(preview\)/);
    assert.match(acceptBody, /isCurrentConnectPreview\(preview\)/);
    assert.match(acceptBody, /isAuthoringDecisionCommitted\(decision\)/);
    assert.match(acceptBody, /collectAuthoringDecisionDiagnostics\(decision\)/);
    assert.match(acceptBody, /sourceEditMatchesPreviewEvidence\(decision\.sourceEdit,\s*preview\.relationshipEvidence\.sourceEdit\)/);
    assert.match(acceptBody, /applyAuthoringSourceEdit\(decision\.sourceEdit\)/);
    assert.doesNotMatch(methodBody(graphWorkbenchSource, 'resolveCreatedRelationshipSemanticId'), /targetSubjectId/);
});

test('Graphical View cancels and clears stale relationship previews through decision protocol', () => {
    assert.match(graphWorkbenchSource, /clearConnectPreview/);
    assert.match(graphWorkbenchSource, /cancelConnectPreview/);
    assert.match(graphWorkbenchSource, /cancelStaleConnectPreview/);
    assert.match(graphWorkbenchSource, /connectPreviewRequestToken/);
    assert.match(graphWorkbenchSource, /currentEditorMatchesConnectPreview/);
    assert.match(graphWorkbenchSource, /isCurrentConnectPreview/);
    assert.match(graphWorkbenchSource, /isSameConnectPreviewSession/);
    assert.match(graphWorkbenchSource, /decision:\s*'cancelled'/);
    assert.match(methodBody(graphWorkbenchSource, 'previewSemanticRelationship'), /const requestToken = \+\+this\.connectPreviewRequestToken/);
    assert.match(methodBody(graphWorkbenchSource, 'previewSemanticRelationship'), /if \(requestToken !== this\.connectPreviewRequestToken\)/);
    assert.match(methodBody(graphWorkbenchSource, 'rejectConnectPreview'), /isSameConnectPreviewSession\(preview\)/);
    assert.match(methodBody(graphWorkbenchSource, 'cancelConnectPreview'), /isSameConnectPreviewSession\(preview\)/);
    assert.match(graphWorkbenchSource, /void this\.cancelStaleConnectPreview\('Active Athena editor changed after semantic relationship preview/s);
    assert.match(graphWorkbenchSource, /void this\.cancelStaleConnectPreview\('Athena source changed after semantic relationship preview/s);
    assert.match(graphWorkbenchSource, /await this\.cancelStaleConnectPreview\('Diagram refreshed after semantic relationship preview/s);
    assert.match(methodBody(graphWorkbenchSource, 'cancelStaleConnectPreview'), /requestAuthoringDecision\(/);
    assert.match(methodBody(graphWorkbenchSource, 'cancelStaleConnectPreview'), /decision:\s*'cancelled'/);
});
