import { Range } from '@theia/core/shared/vscode-languageserver-protocol';
import URI from '@theia/core/lib/common/uri';
import { AthenaConnectionReadModelSourceTrace, AthenaSemanticInspectionPayload } from './athena-lsp-editor-bridge-service';
import { SceneTrace } from './diagram/generated/types';

export type AthenaActiveSemanticSelection = {
    semanticId: string;
    label?: string;
    kind?: 'entity' | 'port' | 'relationship' | 'trace';
    sourceUri?: string;
    sourceRange?: Range;
};

export function resolveSemanticSelectionFromSceneTrace(
    repositoryRoot: string,
    trace: SceneTrace,
    semanticId: string,
): AthenaActiveSemanticSelection | undefined {
    const origin = trace.origins.find(candidate => candidate.primary) ?? trace.origins[0];
    if (!origin || !repositoryRoot || !isRepositoryRelativePath(origin.relativePath)) return undefined;
    const start = { line: origin.startLine, character: origin.startCharacter };
    const unchangedEnd = origin.endLine === start.line && origin.endCharacter === start.character;
    const end = unchangedEnd
        ? { line: start.line, character: start.character + 1 }
        : { line: origin.endLine, character: origin.endCharacter };
    const sourcePath = `${repositoryRoot.replace(/[\\/]+$/, '')}/${origin.relativePath.replaceAll('\\', '/')}`;
    return {
        semanticId,
        label: origin.subjectId,
        kind: 'trace',
        sourceUri: URI.fromFilePath(sourcePath).toString(),
        sourceRange: { start, end },
    };
}

export function resolveSemanticSelectionFromPublishedSourceTrace(
    repositoryRoot: string,
    trace: AthenaConnectionReadModelSourceTrace,
    semanticId: string,
): AthenaActiveSemanticSelection | undefined {
    if (!repositoryRoot || !isRepositoryRelativePath(trace.relativePath)) return undefined;
    const start = {
        line: Math.max(0, trace.startLine - 1),
        character: Math.max(0, trace.startCharacter - 1),
    };
    const end = {
        line: Math.max(start.line, trace.endLine - 1),
        character: Math.max(0, trace.endCharacter - 1),
    };
    if (end.line === start.line && end.character <= start.character) end.character = start.character + 1;
    const sourcePath = `${repositoryRoot.replace(/[\\/]+$/, '')}/${trace.relativePath.replaceAll('\\', '/')}`;
    return {
        semanticId,
        label: trace.subjectId,
        kind: 'trace',
        sourceUri: URI.fromFilePath(sourcePath).toString(),
        sourceRange: { start, end },
    };
}

type AthenaSemanticInspectionEntry = {
    semanticId: string;
    label: string;
    kind: 'entity' | 'port' | 'relationship';
    sourceRange: Range;
};

export function resolveSemanticSelectionFromInspection(
    inspection: AthenaSemanticInspectionPayload | undefined,
    semanticId: string
): AthenaActiveSemanticSelection | undefined {
    if (!inspection) {
        return undefined;
    }

    const entity = inspection.entities.find(entry => entry.semanticId === semanticId);
    if (entity) {
        return {
            semanticId,
            label: entity.name,
            kind: 'entity',
            sourceUri: inspection.uri,
            sourceRange: entity.sourceRange
        };
    }

    const port = inspection.ports.find(entry => entry.semanticId === semanticId);
    if (port) {
        return {
            semanticId,
            label: port.path,
            kind: 'port',
            sourceUri: inspection.uri,
            sourceRange: port.sourceRange
        };
    }

    const relationship = inspection.relationships.find(entry => entry.semanticId === semanticId);
    if (relationship) {
        return {
            semanticId,
            label: relationshipLabel(relationship),
            kind: 'relationship',
            sourceUri: inspection.uri,
            sourceRange: relationship.sourceRange
        };
    }
    return undefined;
}

export function resolveSemanticSelectionFromSourceRange(
    inspection: AthenaSemanticInspectionPayload | undefined,
    sourceUri: string,
    selectionRange: Range
): AthenaActiveSemanticSelection | undefined {
    if (!inspection || inspection.uri !== sourceUri) {
        return undefined;
    }

    const matchingEntry = inspectionEntries(inspection)
        .filter(entry => rangeContainsRange(entry.sourceRange, selectionRange))
        .sort((left, right) => rangeWeight(left.sourceRange) - rangeWeight(right.sourceRange))[0];
    return matchingEntry
        ? {
            semanticId: matchingEntry.semanticId,
            label: matchingEntry.label,
            kind: matchingEntry.kind,
            sourceUri: inspection.uri,
            sourceRange: matchingEntry.sourceRange
        }
        : undefined;
}

function inspectionEntries(inspection: AthenaSemanticInspectionPayload): AthenaSemanticInspectionEntry[] {
    return [
        ...inspection.entities.map(entity => ({
            semanticId: entity.semanticId,
            label: entity.name,
            kind: 'entity' as const,
            sourceRange: entity.sourceRange
        })),
        ...inspection.ports.map(port => ({
            semanticId: port.semanticId,
            label: port.path,
            kind: 'port' as const,
            sourceRange: port.sourceRange
        })),
        ...inspection.relationships.map(relationship => ({
            semanticId: relationship.semanticId,
            label: relationshipLabel(relationship),
            kind: 'relationship' as const,
            sourceRange: relationship.sourceRange
        }))
    ];
}

function relationshipLabel(relationship: AthenaSemanticInspectionPayload['relationships'][number]): string {
    const participants = relationship.participants
        .map(participant => `${participant.role}=${participant.subjectPath}`)
        .join(', ');
    return `${relationship.definition} (${participants})`;
}

function rangeContainsRange(container: Range, candidate: Range): boolean {
    return comparePosition(container.start, candidate.start) <= 0 &&
        comparePosition(container.end, candidate.end) >= 0;
}

function comparePosition(
    left: { line: number; character: number },
    right: { line: number; character: number }
): number {
    return left.line !== right.line ? left.line - right.line : left.character - right.character;
}

function rangeWeight(range: Range): number {
    const lineSpan = Math.max(range.end.line - range.start.line, 0);
    const characterSpan = lineSpan === 0
        ? Math.max(range.end.character - range.start.character, 0)
        : Math.max(range.end.character + range.start.character, 0);
    return (lineSpan * 10_000) + characterSpan;
}

function isRepositoryRelativePath(value: string): boolean {
    const normalized = value.replaceAll('\\', '/');
    return normalized.length > 0 &&
        !normalized.startsWith('/') &&
        !/^[A-Za-z]:/.test(normalized) &&
        normalized.split('/').every(segment => segment.length > 0 && segment !== '.' && segment !== '..');
}
