import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const repoRoot = [
  process.cwd(),
  resolve(process.cwd(), '..'),
  resolve(process.cwd(), '..', '..'),
].find(candidate => existsSync(resolve(candidate, '_bmad-output'))) ?? process.cwd();

const protocol = readFileSync(
  resolve(repoRoot, 'ide/theia-frontend/src/browser/athena-authoring-protocol.ts'),
  'utf8',
);
const bridge = readFileSync(
  resolve(repoRoot, 'ide/theia-frontend/src/browser/athena-lsp-editor-bridge-service.ts'),
  'utf8',
);

assert.match(protocol, /export interface AthenaGovernedGraphicEditIntentRequest/);
assert.match(protocol, /revisionGuard: AthenaAuthoringRevisionGuardPayload/);
assert.match(protocol, /sourceEdit: AthenaAuthoringSourceEditPayload \| undefined/);
assert.match(protocol, /export type AthenaGovernedGraphicMutationTarget/);
assert.match(protocol, /'svg-resource'/);
assert.match(protocol, /'dom-node'/);
assert.match(protocol, /'graphic-primitive-ir'/);
assert.match(protocol, /'placement-fact'/);
assert.match(protocol, /'route-fact'/);
assert.match(protocol, /export const ATHENA_GOVERNED_GRAPHIC_EDIT_PREVIEW_METHOD = 'athena\/graphicEdit\/preview'/);
assert.match(bridge, /requestGovernedGraphicEditPreview/);
assert.match(bridge, /ATHENA_GOVERNED_GRAPHIC_EDIT_PREVIEW_METHOD/);
assert.match(bridge, /sendLanguageRequest<AthenaGovernedGraphicEditPreviewPayload>/);
