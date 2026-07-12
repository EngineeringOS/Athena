import { Range } from '@theia/core/shared/vscode-languageserver-protocol';
import { AthenaSemanticFactReferencePayload, AthenaSemanticInspectionPayload } from './athena-lsp-editor-bridge-service';
/** Transient cross-surface semantic selection used only for frontend synchronization. */
export type AthenaActiveSemanticSelection = {
    semanticId: string;
    label?: string;
    kind?: 'component' | 'port' | 'connection';
    sourceUri?: string;
    sourceRange?: Range;
};
export type AthenaProjectionOccurrenceResolution = {
    semanticId: string;
    status: 'resolved' | 'ambiguous' | 'unresolved';
    occurrenceIds: string[];
};
export type AthenaProjectionCrossReferenceResolution = {
    semanticId: string;
    kind: string;
    sheetIds: string[];
    occurrenceIds: string[];
};
export type AthenaProjectionEndpointAliasResolution = {
    semanticId: string;
    status: 'resolved' | 'ambiguous' | 'unresolved';
    endpointIds: string[];
    anchorIds: string[];
    connectionIds: string[];
};
export type AthenaProjectionRelatedSubjectResolution = {
    semanticId: string;
    relatedSemanticId: string;
    relation: 'owner' | 'owned-port' | 'connection' | 'source-port' | 'target-port';
};
type AthenaSemanticScmContextCarrier = {
    subjectIdentity?: string;
    factReferences: AthenaSemanticFactReferencePayload[];
};
type AthenaProjectionSelectionCarrier = {
    crossReferences?: Array<{
        semanticId: string;
        kind: string;
        sheetIds: string[];
        occurrenceIds: string[];
    }>;
    electricalAnchors?: Array<{
        anchorId: string;
        portSemanticId: string;
        ownerSemanticId: string;
        nodeId: string;
        labelId?: string;
    }>;
    electricalConnectionEndpoints?: Array<{
        endpointId: string;
        projectionConnectionId: string;
        connectionSemanticId: string;
        endpointRole: string;
        portSemanticId: string;
        anchorId: string;
    }>;
    graph: {
        nodes: Array<{
            id: string;
            semanticId?: string;
        }>;
        edges: Array<{
            id: string;
            semanticId?: string;
        }>;
    };
};
/** Resolves one canonical semantic selection from the current inspection payload, if the document publishes it. */
export declare function resolveSemanticSelectionFromInspection(inspection: AthenaSemanticInspectionPayload | undefined, semanticId: string): AthenaActiveSemanticSelection | undefined;
/** Resolves the most specific semantic subject that contains the current source-editor selection. */
export declare function resolveSemanticSelectionFromSourceRange(inspection: AthenaSemanticInspectionPayload | undefined, sourceUri: string, selectionRange: Range): AthenaActiveSemanticSelection | undefined;
/** Reuses M6 semantic SCM subject-identity vocabulary to determine whether one SCM context matches the active selection. */
export declare function matchesSemanticScmContext(carrier: AthenaSemanticScmContextCarrier, semanticId: string | undefined): boolean;
/** Returns the first canonical semantic id that an SCM context can reveal back into the workbench. */
export declare function selectableSemanticIdFromScmContext(carrier: AthenaSemanticScmContextCarrier): string | undefined;
/** Returns whether the current graph snapshot already exposes the canonical semantic id. */
export declare function graphContainsSemanticId(diagram: AthenaProjectionSelectionCarrier | undefined, semanticId: string): boolean;
/** Resolves repeated-reference status for one canonical semantic id inside current graph snapshot. */
export declare function resolveProjectionOccurrence(diagram: AthenaProjectionSelectionCarrier | undefined, semanticId: string): AthenaProjectionOccurrenceResolution;
/** Returns published repeated-reference metadata for one canonical subject, if available. */
export declare function resolveProjectionCrossReference(diagram: AthenaProjectionSelectionCarrier | undefined, semanticId: string): AthenaProjectionCrossReferenceResolution | undefined;
/** Resolves governed endpoint and anchor aliases for one canonical port selection. */
export declare function resolveProjectionEndpointAlias(diagram: AthenaProjectionSelectionCarrier | undefined, semanticId: string): AthenaProjectionEndpointAliasResolution;
/** Resolves governed related semantic subjects without inventing a renderer-owned navigation graph. */
export declare function resolveProjectionRelatedSubjects(diagram: AthenaProjectionSelectionCarrier | undefined, semanticId: string): AthenaProjectionRelatedSubjectResolution[];
/** Keeps transient selection only while the refreshed projection still contains the same canonical semantic id. */
export declare function retainSelectionIfPresent(diagram: AthenaProjectionSelectionCarrier, selection: AthenaActiveSemanticSelection | undefined): AthenaActiveSemanticSelection | undefined;
export {};
//# sourceMappingURL=athena-semantic-selection-model.d.ts.map