import type { AthenaAuthoringSourceEditPayload } from './athena-authoring-protocol';

export type AthenaAuthoringGuardedDocument = {
    uri: string;
    version: number;
    text: string;
};

export async function assertAuthoringRevisionGuard(
    edit: Pick<AthenaAuthoringSourceEditPayload, 'uri' | 'revisionGuard'>,
    document: AthenaAuthoringGuardedDocument,
): Promise<void> {
    const guard = edit.revisionGuard;
    if (!guard) {
        throw new Error('Governed Athena source edit requires a Revision Guard.');
    }
    if (guard.sourceUri !== edit.uri || document.uri !== edit.uri) {
        throw new Error(`Athena authoring Revision Guard URI mismatch for ${edit.uri}.`);
    }
    if (document.version !== guard.documentVersion) {
        throw new Error(
            `Athena authoring Revision Guard version mismatch: expected ${guard.documentVersion}, received ${document.version}.`,
        );
    }
    const digest = await globalThis.crypto.subtle.digest(
        'SHA-256',
        new TextEncoder().encode(document.text),
    );
    const contentSha256 = Array.from(new Uint8Array(digest))
        .map(value => value.toString(16).padStart(2, '0'))
        .join('');
    if (contentSha256 !== guard.contentSha256) {
        throw new Error('Athena authoring Revision Guard content mismatch; refresh the preview before applying it.');
    }
}
