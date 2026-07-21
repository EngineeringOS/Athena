import type { AthenaActiveSemanticSelection } from './athena-semantic-selection-model';

export type AthenaInteractionSubjectKind =
    | 'component'
    | 'port'
    | 'connection'
    | 'route'
    | 'sheetOccurrence'
    | 'referenceMarker'
    | 'diagnostic'
    | 'sourceRange'
    | 'workspace';

export type AthenaInteractionSubjectKey = {
    canonicalSubjectId: string;
    subjectKind: AthenaInteractionSubjectKind;
    sourceContextId?: string;
};

export type AthenaInteractionOccurrenceKey = AthenaInteractionSubjectKey & {
    projectionViewId?: string;
    sheetId?: string;
    documentProjectionId?: string;
    occurrenceId?: string;
    sourceRevision?: string;
};

export type AthenaInteractionSelectionPayload = {
    subjectKey?: AthenaInteractionSubjectKey;
    occurrence?: AthenaInteractionOccurrenceKey;
    adapterMetadata: Record<string, string>;
};

export type AthenaInteractionSelectionContext = {
    projectionViewId?: string;
    sheetId?: string;
    documentProjectionId?: string;
    occurrenceId?: string;
    sourceRevision?: string;
    adapterMetadata?: Record<string, string>;
};

export function toInteractionSelectionPayload(
    selection: AthenaActiveSemanticSelection | undefined,
    context: AthenaInteractionSelectionContext = {},
): AthenaInteractionSelectionPayload {
    if (!selection?.semanticId) {
        return {
            subjectKey: undefined,
            occurrence: undefined,
            adapterMetadata: context.adapterMetadata ?? {},
        };
    }

    const subjectKind = subjectKindFromSemanticId(selection.semanticId);
    if (!subjectKind) {
        return {
            subjectKey: undefined,
            occurrence: undefined,
            adapterMetadata: context.adapterMetadata ?? {},
        };
    }

    const subjectKey: AthenaInteractionSubjectKey = {
        canonicalSubjectId: selection.semanticId,
        subjectKind,
        sourceContextId: selection.sourceUri,
    };

    const occurrence: AthenaInteractionOccurrenceKey = {
        ...subjectKey,
    };
    assignIfPresent(occurrence, 'projectionViewId', context.projectionViewId);
    assignIfPresent(occurrence, 'sheetId', context.sheetId);
    assignIfPresent(occurrence, 'documentProjectionId', context.documentProjectionId);
    assignIfPresent(occurrence, 'occurrenceId', context.occurrenceId);
    assignIfPresent(occurrence, 'sourceRevision', context.sourceRevision);

    return {
        subjectKey,
        occurrence,
        adapterMetadata: context.adapterMetadata ?? {},
    };
}

function assignIfPresent<T extends Record<string, string | undefined>>(
    target: T,
    key: keyof T,
    value: string | undefined,
): void {
    if (value !== undefined) {
        target[key] = value as T[keyof T];
    }
}

function subjectKindFromSemanticId(semanticId: string): AthenaInteractionSubjectKind | undefined {
    if (semanticId.startsWith('component:')) {
        return 'component';
    }
    if (semanticId.startsWith('port:')) {
        return 'port';
    }
    if (semanticId.startsWith('connection:')) {
        return 'connection';
    }
    if (semanticId.startsWith('route:')) {
        return 'route';
    }
    if (semanticId.startsWith('diagnostic:')) {
        return 'diagnostic';
    }
    return undefined;
}
