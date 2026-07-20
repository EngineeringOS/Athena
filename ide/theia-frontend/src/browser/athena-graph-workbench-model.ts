import {
    AthenaGLSPDiagram,
    AthenaGLSPElectricalAnchorSource,
    AthenaGLSPElectricalConnectionEndpointSource,
    AthenaGLSPEdge,
    AthenaGLSPNode,
    AthenaGLSPPoint,
    AthenaGLSPRenderContributionSource
} from '@engineeringood/athena-graph-glsp';
import {
    AthenaGraphResolvedPresentationConnector,
    AthenaGraphResolvedPresentationOccurrence,
    AthenaGraphResolvedPresentationPart,
    resolvePresentationConnectors,
    resolvePresentationOccurrences,
    resolvePresentationRepresentations,
    resolvePresentationReferenceMarkers,
    AthenaGraphResolvedPresentationRepresentation
} from './athena-graph-presentation-model';

export type AthenaGraphSceneBounds = {
    minX: number;
    minY: number;
    maxX: number;
    maxY: number;
    width: number;
    height: number;
    centerX: number;
    centerY: number;
};

export type AthenaGraphViewportSize = {
    width: number;
    height: number;
};

export type AthenaGraphViewportTransform = {
    zoom: number;
    offsetX: number;
    offsetY: number;
};

export type AthenaGraphLayoutAdjustmentKind = 'place' | 'align' | 'group' | 'route' | 'label';

export type AthenaGraphLayoutAdjustmentIntent = {
    intentId: string;
    kind: 'place' | 'align' | 'group';
    subjectSemanticId: string;
    occurrenceId: string;
    viewId: string;
    sheetId: string;
    snapshotId: string;
    sourceUri: string;
    targetSemanticId?: string;
    relation?: 'near' | 'below' | 'aligned-with' | 'grouped-with';
    transientOnly: true;
    persisted: false;
};

export type AthenaGraphAuthoredLayoutIntentRelation = 'near' | 'below' | 'aligned-with' | 'grouped-with';

export type AthenaGraphAuthoredLayoutAxis = 'horizontal' | 'vertical';

export type AthenaGraphAuthoredLayoutIntentStatement = {
    subject: string;
    relation: AthenaGraphAuthoredLayoutIntentRelation;
    target: string;
    axis?: AthenaGraphAuthoredLayoutAxis;
    priority: 'preference';
};

export type AthenaGraphAuthoredLayoutIntent = {
    viewFamily: string;
    statements: AthenaGraphAuthoredLayoutIntentStatement[];
};

export type AthenaGraphLayoutAdjustmentCaptureResult =
    | { accepted: true; intent: AthenaGraphLayoutAdjustmentIntent }
    | { accepted: false; reason: string };

export type AthenaGraphLayoutMutationPreview = {
    previewId: string;
    intentId: string;
    subjectSemanticId: string;
    sourceUri: string;
    title: string;
    authoredIntent: AthenaGraphAuthoredLayoutIntent;
    layoutBlockSnippet: string;
    persisted: false;
};

export type AthenaGraphLayoutSourceEdit = {
    uri: string;
    range: {
        start: { line: number; character: number };
        end: { line: number; character: number };
    };
    newText: string;
    selectionRange?: {
        start: { line: number; character: number };
        end: { line: number; character: number };
    };
    suggestedSemanticId?: string;
};

/** Pure presentation model used by the first Athena graphical workbench panel. */
export type AthenaGraphWorkbenchModel = {
    headerTitle: string;
    viewLabel: string;
    viewFamilyId?: string;
    isElectricalFamily: boolean;
    statusLabel: string;
    statusTone: 'ready' | 'warning' | 'idle';
    semanticPath: string;
    snapshotId: string;
    activeSheetId?: string;
    sheetCount: number;
    sheetViewSelector?: AthenaGraphWorkbenchSheetViewSelector;
    notationPackId?: string;
    crossReferenceCount: number;
    svgViewBox: string;
    metrics: {
        nodeCount: number;
        edgeCount: number;
        supportedViewCount: number;
        diagnosticCount: number;
    };
    supportedViews: Array<{
        viewId: string;
        displayName: string;
        description: string;
        familyId?: string;
        ownershipContract: AthenaGLSPDiagram['supportedViews'][number]['ownershipContract'];
        isActive: boolean;
    }>;
    diagnostics: AthenaGLSPDiagram['diagnostics'];
    activeRenderContributions: AthenaGLSPRenderContributionSource[];
    sheetChrome: AthenaGraphWorkbenchSheetChrome;
    referenceMarkers: AthenaGraphWorkbenchReferenceMarker[];
    nodes: AthenaGraphWorkbenchNode[];
    edges: AthenaGraphWorkbenchEdge[];
    canvas: {
        width: number;
        height: number;
    };
    sceneBounds: AthenaGraphSceneBounds;
    surfaceTokens: AthenaGraphSurfaceTokens;
    emptyState?: {
        title: string;
        message: string;
    };
};

export type AthenaGraphSurfaceTokens = {
    canvas: Record<string, string>;
    node: Record<string, string>;
    edge: Record<string, string>;
};

export type AthenaGraphWorkbenchSheetChrome = {
    frame: AthenaGraphWorkbenchSheetFrame;
    grid: AthenaGraphWorkbenchSheetGrid;
    activeSheet?: AthenaGraphWorkbenchSheetSummary;
    titleBlock?: AthenaGraphWorkbenchSheetTitleBlock;
    crossReferenceMarkers: AthenaGraphWorkbenchCrossReferenceMarker[];
};

export type AthenaGraphWorkbenchSheetViewSelector = {
    activeSheetViewId?: string;
    hasMultipleSheetViews: boolean;
    entries: AthenaGraphWorkbenchSheetViewSelectorEntry[];
};

export type AthenaGraphWorkbenchSheetViewSelectorEntry = {
    sheetViewId: string;
    displayOrder: number;
    title: string;
    role?: string;
    subjectCount: number;
    isActive: boolean;
    label: string;
};

export type AthenaGraphWorkbenchSheetFrame = {
    width: number;
    height: number;
};

export type AthenaGraphWorkbenchSheetGrid = {
    majorStep: number;
    minorStep: number;
};

export type AthenaGraphWorkbenchSheetSummary = {
    sheetId: string;
    displayName: string;
    role?: string;
    order: number;
    previousSheetId?: string;
    nextSheetId?: string;
    subjectSemanticIds: string[];
    subjectCount: number;
    isActive: boolean;
};

export type AthenaGraphWorkbenchSheetTitleBlock = {
    sheetId: string;
    displayName: string;
    order: number;
    previousSheetId?: string;
    nextSheetId?: string;
    subjectCount: number;
    crossReferenceCount: number;
};

export type AthenaGraphWorkbenchCrossReferenceMarker = {
    semanticId: string;
    kind: string;
    markerLabel: string;
    sheetIds: string[];
    occurrenceIds: string[];
    isActiveSheetLinked: boolean;
};

export type AthenaGraphWorkbenchReferenceMarker = {
    markerId: string;
    markerKind: string;
    relationType: string;
    selectedSheetViewId: string;
    sourceOccurrenceId: string;
    targetOccurrenceId: string;
    sourceIdentity: string;
    targetIdentity: string;
    sourceDocumentLocation: AthenaGraphWorkbenchDocumentLocation;
    targetDocumentLocation: AthenaGraphWorkbenchDocumentLocation;
    compactNotation: string;
    sourceProjectionIds: string[];
};

export type AthenaGraphWorkbenchDocumentLocation = {
    sheetViewId: string;
    zoneId?: string;
    displayNotation: string;
};

export type AthenaGraphReferenceMarkerNavigation =
    | {
        status: 'ready';
        markerId: string;
        relationType: string;
        targetSheetViewId: string;
        targetOccurrenceId: string;
        targetCanonicalId: string;
        requiresSheetSwitch: boolean;
        displayNotation: string;
    }
    | {
        status: 'missing-marker';
        markerId: string;
        reason: string;
    };

export type AthenaGraphDocumentReferenceInspection =
    | {
        status: 'ready';
        canonicalIdentity: string;
        references: AthenaGraphDocumentReferenceInspectionEntry[];
        persisted: false;
    }
    | {
        status: 'unavailable';
        canonicalIdentity: string;
        references: [];
        persisted: false;
    };

export type AthenaGraphDocumentReferenceInspectionEntry = {
    markerId: string;
    markerKind: string;
    relationType: string;
    compactNotation: string;
    sourceOccurrenceId: string;
    targetOccurrenceId: string;
    sourceLocation: string;
    targetLocation: string;
    targetSheetViewId: string;
    sourceProjectionIds: string[];
};

export type AthenaGraphWorkbenchEdge = AthenaGLSPEdge & {
    routePoints: AthenaGLSPPoint[];
    bendMarkerPoints: AthenaGLSPPoint[];
    crossingMarkerPoints: AthenaGLSPPoint[];
    routeLabels: AthenaGraphWorkbenchRouteLabel[];
    path: string;
    conductorStyle: 'electrical' | 'generic';
    terminals: AthenaGraphWorkbenchEdgeTerminal[];
    presentationConnector?: AthenaGraphResolvedPresentationConnector;
};

export type AthenaGraphWorkbenchRouteLabel = {
    text: string;
    point: AthenaGLSPPoint;
    canvasDisplay: 'always' | 'selection';
};

export type AthenaGraphRouteInspection =
    | {
        status: 'ready';
        connectionId: string;
        sourcePortSemanticId?: string;
        targetPortSemanticId?: string;
        routeQuality: string;
        policySummary: string;
        labels: string[];
        persisted: false;
    }
    | {
        status: 'unavailable';
        reason: string;
        persisted: false;
    };

export type AthenaGraphRepresentationInspection =
    | {
        status: 'ready';
        subjectId: string;
        occurrenceId: string;
        representationId: string;
        symbolFamilyId: string;
        fallback: false;
        terminals: Array<{
            terminalId: string;
            portId: string;
            physicalTerminalId: string;
            anchorId: string;
            side: string;
            number: string;
            marker: string;
        }>;
        labels: Array<{
            labelId: string;
            role: string;
            value: string;
            anchorId: string;
        }>;
        selectedTerminal?: {
            terminalId: string;
            portId: string;
            physicalTerminalId: string;
            anchorId: string;
            side: string;
            number: string;
            marker: string;
        };
        selectedLabel?: {
            labelId: string;
            role: string;
            value: string;
            anchorId: string;
        };
        persisted: false;
    }
    | {
        status: 'unavailable';
        reason: string;
        persisted: false;
    };

export type AthenaGraphWorkbenchEdgeTerminal = {
    role: 'source' | 'target';
    point: AthenaGLSPPoint;
    endpointId?: string;
    anchorId?: string;
    portSemanticId?: string;
    ownerSemanticId?: string;
    nodeId?: string;
    labelId?: string;
};

export type AthenaGraphWorkbenchLeaderSegment = {
    start: AthenaGLSPPoint;
    end: AthenaGLSPPoint;
};

export type AthenaGraphWorkbenchNodeAnchor = {
    anchorId: string;
    point: AthenaGLSPPoint;
    side: string;
    portSemanticId: string;
    labelId?: string;
};

export type AthenaGraphWorkbenchPresentationTerminal = {
    terminalId: string;
    subjectId: string;
    occurrenceId: string;
    portId: string;
    physicalTerminalId: string;
    side: string;
    marker: string;
    number: string;
    point: AthenaGLSPPoint;
    anchorId: string;
};

export type AthenaGraphWorkbenchPresentationLabel = {
    labelId: string;
    subjectId: string;
    occurrenceId: string;
    role: string;
    value: string;
    point: AthenaGLSPPoint;
    anchorId: string;
};

export type AthenaGraphWorkbenchNode = AthenaGLSPNode & {
    renderVariant: 'generic-component' | 'electrical-device' | 'generic-label' | 'electrical-terminal-label';
    notationSymbolKey?: string;
    labelPolicy?: string;
    markerKeys: string[];
    labelLeader?: AthenaGraphWorkbenchLeaderSegment;
    electricalAnchors: AthenaGraphWorkbenchNodeAnchor[];
    presentationOccurrence?: AthenaGraphResolvedPresentationOccurrence;
    presentationRepresentation?: AthenaGraphResolvedPresentationRepresentation;
    presentationParts: AthenaGraphResolvedPresentationPart[];
    presentationTerminals: AthenaGraphWorkbenchPresentationTerminal[];
    presentationLabels: AthenaGraphWorkbenchPresentationLabel[];
};

/** Builds one deterministic workbench-facing view model from the adapter-owned graph diagram. */
export function buildAthenaGraphWorkbenchModel(diagram: AthenaGLSPDiagram): AthenaGraphWorkbenchModel {
    const fallbackCanvasWidth = 960;
    const fallbackCanvasHeight = 540;
    const graph = diagram.graph ?? {
        id: `${diagram.projectName}:${diagram.activeViewId}`,
        type: 'graph' as const,
        canvas: {
            width: 0,
            height: 0,
        },
        nodes: [],
        edges: [],
    };
    const graphNodes = normalizeArray(graph.nodes);
    const graphEdges = normalizeArray(graph.edges);
    const presentationOccurrences = resolvePresentationOccurrences(diagram);
    const presentationConnectors = resolvePresentationConnectors(diagram);
    const presentationRepresentations = resolvePresentationRepresentations(diagram);
    const supportedViews = normalizeArray(diagram.supportedViews);
    const diagnostics = normalizeArray(diagram.diagnostics);
    const sheets = normalizeArray(diagram.sheets);
    const crossReferences = normalizeArray(diagram.crossReferences);
    const notationSubjects = normalizeArray(diagram.notationPack?.subjects);
    const electricalAnchors = normalizeArray(diagram.electricalAnchors);
    const electricalConnectionEndpoints = normalizeArray(diagram.electricalConnectionEndpoints);
    const renderContributions = normalizeArray(diagram.activeRenderContributions);
    const canvasWidth = graph.canvas.width > 0 ? graph.canvas.width : fallbackCanvasWidth;
    const canvasHeight = graph.canvas.height > 0 ? graph.canvas.height : fallbackCanvasHeight;
    const activeView = supportedViews.find(view => view.viewId === diagram.activeViewId);
    const viewLabel = activeView?.displayName ?? diagram.activeViewId ?? 'graph';
    const isElectricalFamily = !!activeView?.familyId?.startsWith('electrical/')
        || !!diagram.notationPack?.packId?.startsWith('electrical-notation/');
    const notationBySemanticId = new Map(
        notationSubjects.map(subject => [subject.semanticId, subject] as const)
    );
    const anchorById = new Map(
        electricalAnchors.map(anchor => [anchor.anchorId, anchor] as const)
    );
    const anchorsByNodeId = groupAnchorsByNodeId(electricalAnchors);
    const anchorByLabelId = new Map(
        electricalAnchors.flatMap(anchor => anchor.labelId ? [[anchor.labelId, anchor] as const] : [])
    );
    const endpointsByConnectionId = groupEndpointsByConnectionId(electricalConnectionEndpoints);
    const representationByProjectionId = new Map(
        presentationRepresentations.flatMap(representation =>
            representation.sourceProjectionIds.map(projectionId => [projectionId, representation] as const)
        ),
    );
    const representationBySubjectId = new Map(
        presentationRepresentations.map(representation => [representation.subjectId, representation] as const),
    );
    const nodes = presentationOccurrences.length > 0
        ? presentationOccurrences.map(occurrence => buildWorkbenchNodeFromPresentation(
            occurrence,
            notationBySemanticId,
            anchorsByNodeId,
            anchorByLabelId,
            isElectricalFamily,
            resolveRepresentationForOccurrence(occurrence, representationByProjectionId, representationBySubjectId),
        ))
        : graphNodes.map(node => buildWorkbenchNode(
            node,
            notationBySemanticId,
            anchorsByNodeId,
            anchorByLabelId,
            isElectricalFamily,
            representationByProjectionId.get(node.id) ?? representationBySubjectId.get(node.semanticId),
        ));
    const rawEdges = presentationConnectors.length > 0
        ? presentationConnectors.map(connector => buildWorkbenchEdgeFromPresentation(
            connector,
            endpointsByConnectionId,
            anchorById,
        ))
        : graphEdges.map(edge => buildWorkbenchEdge(
            edge,
            endpointsByConnectionId.get(edge.id) ?? [],
            anchorById,
        ));
    const edges = withCrossingMarkers(rawEdges);
    const sheetChrome = resolveSheetChrome(diagram, canvasWidth, canvasHeight);
    const sheetViewSelector = resolveSheetViewSelector(diagram);
    const referenceMarkers = resolveWorkbenchReferenceMarkers(diagram);

    return {
        headerTitle: diagram.projectName,
        viewLabel,
        viewFamilyId: activeView?.familyId,
        isElectricalFamily,
        statusLabel: diagram.status,
        statusTone: diagram.status === 'ready' ? 'ready' : 'warning',
        semanticPath: diagram.semanticPath,
        snapshotId: resolveWorkbenchSnapshotId(diagram),
        activeSheetId: diagram.activeSheetId,
        sheetCount: sheets.length,
        ...(sheetViewSelector ? { sheetViewSelector } : {}),
        notationPackId: diagram.notationPack?.packId,
        crossReferenceCount: crossReferences.length,
        svgViewBox: `0 0 ${canvasWidth} ${canvasHeight}`,
        metrics: {
            nodeCount: nodes.length,
            edgeCount: edges.length,
            supportedViewCount: supportedViews.length,
            diagnosticCount: diagnostics.length,
        },
        supportedViews: supportedViews.map(view => ({
            ...view,
            isActive: view.viewId === diagram.activeViewId,
        })),
        diagnostics,
        activeRenderContributions: renderContributions,
        sheetChrome,
        referenceMarkers,
        nodes,
        edges,
        canvas: {
            width: canvasWidth,
            height: canvasHeight,
        },
        sceneBounds: resolveSceneBounds(nodes, edges, canvasWidth, canvasHeight),
        surfaceTokens: resolveSurfaceTokens(renderContributions),
        emptyState: resolveEmptyState(diagram, nodes, edges),
    };
}

export function buildAthenaGraphRouteInspection(
    model: AthenaGraphWorkbenchModel,
    semanticId: string,
): AthenaGraphRouteInspection {
    const edge = model.edges.find(candidate => candidate.semanticId === semanticId || candidate.id === semanticId);
    if (!edge?.presentationConnector) {
        return {
            status: 'unavailable',
            reason: 'No governed route fact is available for the selected rendered route.',
            persisted: false,
        };
    }
    const routeQuality = edge.presentationConnector.tokenOverrides.routeQuality ?? 'UNKNOWN';
    const routeSegmentCount = edge.presentationConnector.tokenOverrides.routeSegmentCount ?? `${Math.max(0, edge.routePoints.length - 1)}`;
    return {
        status: 'ready',
        connectionId: edge.semanticId,
        sourcePortSemanticId: edge.sourcePortSemanticId,
        targetPortSemanticId: edge.targetPortSemanticId,
        routeQuality,
        policySummary: `m24:route-fact:${routeQuality}:${routeSegmentCount}-segment`,
        labels: edge.routeLabels.map(label => label.text),
        persisted: false,
    };
}

export function buildAthenaGraphRepresentationInspection(
    model: AthenaGraphWorkbenchModel,
    semanticId: string,
): AthenaGraphRepresentationInspection {
    const node = model.nodes.find(candidate =>
        candidate.semanticId === semanticId
        || candidate.id === semanticId
        || candidate.presentationRepresentation?.occurrenceId === semanticId
        || candidate.presentationTerminals.some(terminal =>
            terminal.terminalId === semanticId
            || terminal.anchorId === semanticId
            || terminal.portId === semanticId
        )
        || candidate.presentationLabels.some(label =>
            label.labelId === semanticId
            || label.anchorId === semanticId
        )
    );
    if (!node?.presentationRepresentation) {
        return {
            status: 'unavailable',
            reason: 'No governed representation fact is available for the selected rendered subject.',
            persisted: false,
        };
    }
    const terminalSummaries = node.presentationTerminals.map(terminal => ({
        terminalId: terminal.terminalId,
        portId: terminal.portId,
        physicalTerminalId: terminal.physicalTerminalId,
        anchorId: terminal.anchorId,
        side: terminal.side,
        number: terminal.number,
        marker: terminal.marker,
    }));
    const labelSummaries = node.presentationLabels.map(label => ({
        labelId: label.labelId,
        role: label.role,
        value: label.value,
        anchorId: label.anchorId,
    }));
    return {
        status: 'ready',
        subjectId: node.presentationRepresentation.subjectId,
        occurrenceId: node.presentationRepresentation.occurrenceId,
        representationId: node.presentationRepresentation.representationId,
        symbolFamilyId: node.presentationRepresentation.symbolFamilyId,
        fallback: false,
        terminals: terminalSummaries,
        labels: labelSummaries,
        selectedTerminal: terminalSummaries.find(terminal =>
            terminal.terminalId === semanticId
            || terminal.anchorId === semanticId
            || terminal.portId === semanticId
        ),
        selectedLabel: labelSummaries.find(label =>
            label.labelId === semanticId
            || label.anchorId === semanticId
        ),
        persisted: false,
    };
}

export function captureAthenaGraphLayoutAdjustmentIntent(args: {
    model: AthenaGraphWorkbenchModel;
    node: AthenaGraphWorkbenchNode;
    kind: AthenaGraphLayoutAdjustmentKind;
    targetSemanticId?: string;
    relation?: AthenaGraphLayoutAdjustmentIntent['relation'];
}): AthenaGraphLayoutAdjustmentCaptureResult {
    if (args.kind === 'route' || args.kind === 'label') {
        return {
            accepted: false,
            reason: 'Route and label adjustment persistence is outside M22 scope.',
        };
    }
    const activeView = args.model.supportedViews.find(view => view.isActive);
    const viewId = activeView?.viewId ?? args.model.viewFamilyId ?? args.model.viewLabel;
    const sheetId = args.model.activeSheetId ?? args.model.sheetChrome.activeSheet?.sheetId ?? 'sheet:unknown';
    const occurrenceId = args.node.presentationOccurrence?.occurrenceId ?? args.node.id;
    return {
        accepted: true,
        intent: {
            intentId: [
                'layout-adjustment',
                args.kind,
                args.node.semanticId,
                occurrenceId,
                args.model.snapshotId,
            ].join(':'),
            kind: args.kind,
            subjectSemanticId: args.node.semanticId,
            occurrenceId,
            viewId,
            sheetId,
            snapshotId: args.model.snapshotId,
            sourceUri: args.model.semanticPath,
            ...(args.targetSemanticId ? { targetSemanticId: args.targetSemanticId } : {}),
            ...(args.relation ? { relation: args.relation } : {}),
            transientOnly: true,
            persisted: false,
        },
    };
}

export function buildAthenaGraphLayoutMutationPreview(
    intent: AthenaGraphLayoutAdjustmentIntent,
): AthenaGraphLayoutMutationPreview {
    const authoredIntent = buildAthenaGraphAuthoredLayoutIntent(intent);
    const layoutBlockSnippet = serializeAthenaGraphAuthoredLayoutIntent(authoredIntent);
    const statement = authoredIntent.statements[0];
    const subject = statement?.subject ?? semanticIdToAuthoredName(intent.subjectSemanticId);
    const relation = intent.relation ?? (
        intent.kind === 'align' ? 'aligned-with' : intent.kind === 'group' ? 'grouped-with' : 'near'
    );
    return {
        previewId: `layout-preview:${intent.intentId}`,
        intentId: intent.intentId,
        subjectSemanticId: intent.subjectSemanticId,
        sourceUri: intent.sourceUri,
        title: `Layout ${intent.kind} preview for ${subject}`,
        authoredIntent,
        layoutBlockSnippet,
        persisted: false,
    };
}

export function buildAthenaGraphAuthoredLayoutIntent(
    intent: AthenaGraphLayoutAdjustmentIntent,
): AthenaGraphAuthoredLayoutIntent {
    const relation = intent.relation ?? (
        intent.kind === 'align' ? 'aligned-with' : intent.kind === 'group' ? 'grouped-with' : 'near'
    );
    return {
        viewFamily: resolveLayoutViewFamily(intent.viewId),
        statements: [
            {
                subject: semanticIdToAuthoredName(intent.subjectSemanticId),
                relation,
                target: semanticIdToAuthoredName(intent.targetSemanticId ?? intent.subjectSemanticId),
                ...(relation === 'aligned-with' ? { axis: 'vertical' as const } : {}),
                priority: 'preference' as const,
            },
        ],
    };
}

export function serializeAthenaGraphAuthoredLayoutIntent(intent: AthenaGraphAuthoredLayoutIntent): string {
    return [
        `layout ${intent.viewFamily} {`,
        ...intent.statements.map(statement => `  ${serializeAthenaGraphAuthoredLayoutIntentStatement(statement)}`),
        `}`,
    ].join('\n');
}

function serializeAthenaGraphAuthoredLayoutIntentStatement(statement: AthenaGraphAuthoredLayoutIntentStatement): string {
    if (statement.priority !== 'preference') {
        throw new Error('M23 source syntax admits only default preference layout hints.');
    }
    const subject = semanticIdToAuthoredName(statement.subject);
    const target = semanticIdToAuthoredName(statement.target);
    switch (statement.relation) {
        case 'near':
            return `place ${subject} near ${target}`;
        case 'below':
            return `place ${subject} below ${target}`;
        case 'aligned-with':
            return `align ${subject} aligned-with ${target} axis ${statement.axis ?? 'vertical'}`;
        case 'grouped-with':
            return `group ${subject} grouped-with ${target}`;
    }
}

export function buildAthenaGraphLayoutSourceEdit(args: {
    preview: AthenaGraphLayoutMutationPreview;
    documentText?: string;
    insertionLine: number;
    insertionCharacter: number;
}): AthenaGraphLayoutSourceEdit {
    const preview = args.preview;
    const position = args.documentText
        ? resolveSystemScopedLayoutInsertionPosition(args.documentText, args.insertionLine, args.insertionCharacter)
        : {
            line: Math.max(0, args.insertionLine),
            character: Math.max(0, args.insertionCharacter),
        };
    const layoutSource = indentLayoutBlock(
        serializeAthenaGraphAuthoredLayoutIntent(preview.authoredIntent),
        '  ',
    );
    return {
        uri: preview.sourceUri,
        range: {
            start: position,
            end: position,
        },
        newText: `

${layoutSource}
`,
        selectionRange: {
            start: position,
            end: position,
        },
        suggestedSemanticId: preview.subjectSemanticId,
    };
}

function resolveSystemScopedLayoutInsertionPosition(
    documentText: string,
    fallbackLine: number,
    fallbackCharacter: number,
): { line: number; character: number } {
    const systemCloseOffset = documentText.lastIndexOf('}');
    if (systemCloseOffset < 0) {
        return {
            line: Math.max(0, fallbackLine),
            character: Math.max(0, fallbackCharacter),
        };
    }
    return offsetToPosition(documentText, systemCloseOffset);
}

function offsetToPosition(text: string, offset: number): { line: number; character: number } {
    const beforeOffset = text.slice(0, Math.max(0, offset));
    const lines = beforeOffset.split(/\r\n|\r|\n/);
    return {
        line: Math.max(0, lines.length - 1),
        character: lines.at(-1)?.length ?? 0,
    };
}

function indentLayoutBlock(source: string, indent: string): string {
    return source
        .split('\n')
        .map(line => `${indent}${line}`)
        .join('\n');
}

function resolveLayoutViewFamily(viewId: string): string {
    if (viewId.includes('schematic')) {
        return 'schematic-sheet';
    }
    return 'schematic-sheet';
}

function semanticIdToAuthoredName(semanticId: string): string {
    const lastSegment = semanticId.split(':').filter(Boolean).at(-1) ?? semanticId;
    return lastSegment.split('.').filter(Boolean).at(-1) ?? lastSegment;
}

function resolveWorkbenchSnapshotId(diagram: AthenaGLSPDiagram): string {
    return [
        'workbench-snapshot',
        diagram.semanticPath || 'unknown-source',
        diagram.activeViewId || 'unknown-view',
        diagram.activeSheetId || 'unknown-sheet',
    ].join(':');
}

export function resolveSheetViewSelector(
    diagram: AthenaGLSPDiagram,
): AthenaGraphWorkbenchSheetViewSelector | undefined {
    const sheetSummaries = normalizeArray(diagram.sheets)
        .map(sheet => buildSheetSummary(sheet, diagram.activeSheetId))
        .sort(compareSheetSummaries);
    if (sheetSummaries.length <= 1) {
        return undefined;
    }

    const activeSheetViewId = sheetSummaries.find(sheet => sheet.isActive)?.sheetId
        ?? diagram.activeSheetId
        ?? sheetSummaries[0]?.sheetId;
    return {
        activeSheetViewId,
        hasMultipleSheetViews: true,
        entries: sheetSummaries.map(sheet => {
            const displayOrder = sheet.order + 1;
            return {
                sheetViewId: sheet.sheetId,
                displayOrder,
                title: sheet.displayName,
                ...(sheet.role ? { role: sheet.role } : {}),
                subjectCount: sheet.subjectCount,
                isActive: sheet.sheetId === activeSheetViewId,
                label: `${displayOrder} - ${sheet.displayName}`,
            };
        }),
    };
}

export function resolveAthenaGraphReferenceMarkerNavigation(
    model: AthenaGraphWorkbenchModel,
    markerId: string,
): AthenaGraphReferenceMarkerNavigation {
    const marker = model.referenceMarkers.find(candidate => candidate.markerId === markerId);
    if (!marker) {
        return {
            status: 'missing-marker',
            markerId,
            reason: `No governed reference marker is available for ${markerId}.`,
        };
    }

    return {
        status: 'ready',
        markerId: marker.markerId,
        relationType: marker.relationType,
        targetSheetViewId: marker.targetDocumentLocation.sheetViewId,
        targetOccurrenceId: marker.targetOccurrenceId,
        targetCanonicalId: marker.targetIdentity,
        requiresSheetSwitch: marker.targetDocumentLocation.sheetViewId !== model.activeSheetId,
        displayNotation: marker.compactNotation,
    };
}

export function buildAthenaGraphDocumentReferenceInspection(
    model: AthenaGraphWorkbenchModel,
    canonicalIdentity: string,
): AthenaGraphDocumentReferenceInspection {
    const references = model.referenceMarkers
        .filter(marker => marker.sourceIdentity === canonicalIdentity || marker.targetIdentity === canonicalIdentity)
        .map(marker => ({
            markerId: marker.markerId,
            markerKind: marker.markerKind,
            relationType: marker.relationType,
            compactNotation: marker.compactNotation,
            sourceOccurrenceId: marker.sourceOccurrenceId,
            targetOccurrenceId: marker.targetOccurrenceId,
            sourceLocation: marker.sourceDocumentLocation.displayNotation,
            targetLocation: marker.targetDocumentLocation.displayNotation,
            targetSheetViewId: marker.targetDocumentLocation.sheetViewId,
            sourceProjectionIds: [...marker.sourceProjectionIds],
        }))
        .sort(compareDocumentReferenceInspectionEntries);

    if (references.length === 0) {
        return {
            status: 'unavailable',
            canonicalIdentity,
            references: [],
            persisted: false,
        };
    }

    return {
        status: 'ready',
        canonicalIdentity,
        references,
        persisted: false,
    };
}

function resolveWorkbenchReferenceMarkers(
    diagram: AthenaGLSPDiagram,
): AthenaGraphWorkbenchReferenceMarker[] {
    return resolvePresentationReferenceMarkers(diagram)
        .map(marker => ({
            markerId: marker.markerId,
            markerKind: marker.markerKind,
            relationType: marker.relationType,
            selectedSheetViewId: marker.selectedSheetViewId,
            sourceOccurrenceId: marker.sourceOccurrenceId,
            targetOccurrenceId: marker.targetOccurrenceId,
            sourceIdentity: marker.sourceIdentity,
            targetIdentity: marker.targetIdentity,
            sourceDocumentLocation: { ...marker.sourceDocumentLocation },
            targetDocumentLocation: { ...marker.targetDocumentLocation },
            compactNotation: marker.compactNotation,
            sourceProjectionIds: [...marker.sourceProjectionIds],
        }))
        .sort(compareReferenceMarkers);
}

export function clampAthenaGraphZoom(zoom: number): number {
    if (!Number.isFinite(zoom)) {
        return 1;
    }
    return Math.min(2.5, Math.max(0.2, zoom));
}

export function fitAthenaGraphViewport(
    bounds: AthenaGraphSceneBounds,
    viewport: AthenaGraphViewportSize
): AthenaGraphViewportTransform {
    const width = Math.max(viewport.width, 1);
    const height = Math.max(viewport.height, 1);
    const padding = Math.max(36, Math.min(width, height) * 0.08);
    const availableWidth = Math.max(1, width - padding * 2);
    const availableHeight = Math.max(1, height - padding * 2);
    const scaleX = availableWidth / Math.max(bounds.width, 1);
    const scaleY = availableHeight / Math.max(bounds.height, 1);
    const zoom = clampAthenaGraphZoom(Math.min(scaleX, scaleY));

    return {
        zoom,
        offsetX: (width / 2) - (bounds.centerX * zoom),
        offsetY: (height / 2) - (bounds.centerY * zoom),
    };
}

export function panAthenaGraphViewport(
    transform: AthenaGraphViewportTransform,
    deltaX: number,
    deltaY: number
): AthenaGraphViewportTransform {
    return {
        ...transform,
        offsetX: transform.offsetX + deltaX,
        offsetY: transform.offsetY + deltaY,
    };
}

export function zoomAthenaGraphViewportAtPoint(
    transform: AthenaGraphViewportTransform,
    screenPoint: { x: number; y: number },
    nextZoom: number
): AthenaGraphViewportTransform {
    const zoom = clampAthenaGraphZoom(nextZoom);
    const worldX = (screenPoint.x - transform.offsetX) / transform.zoom;
    const worldY = (screenPoint.y - transform.offsetY) / transform.zoom;

    return {
        zoom,
        offsetX: screenPoint.x - (worldX * zoom),
        offsetY: screenPoint.y - (worldY * zoom),
    };
}

export function resizeAthenaGraphViewport(
    transform: AthenaGraphViewportTransform,
    previousViewport: AthenaGraphViewportSize,
    nextViewport: AthenaGraphViewportSize,
): AthenaGraphViewportTransform {
    const zoom = transform.zoom <= 0 ? 1 : transform.zoom;
    const previousCenterX = previousViewport.width > 0 ? previousViewport.width / 2 : 0;
    const previousCenterY = previousViewport.height > 0 ? previousViewport.height / 2 : 0;
    const worldCenterX = (previousCenterX - transform.offsetX) / zoom;
    const worldCenterY = (previousCenterY - transform.offsetY) / zoom;

    return {
        zoom: transform.zoom,
        offsetX: (nextViewport.width / 2) - (worldCenterX * zoom),
        offsetY: (nextViewport.height / 2) - (worldCenterY * zoom),
    };
}

export function keepAthenaGraphViewportFocusedOnSelection(
    transform: AthenaGraphViewportTransform,
    viewport: AthenaGraphViewportSize,
    nodes: AthenaGraphWorkbenchNode[],
    edges: AthenaGraphWorkbenchEdge[],
    semanticId: string,
    padding: number = 48,
): AthenaGraphViewportTransform {
    const selectedNode = nodes.find(node => node.semanticId === semanticId);
    if (selectedNode) {
        return keepAthenaGraphViewportFocusedOnBounds(
            transform,
            viewport,
            resolveAthenaGraphNodeBounds(selectedNode),
            padding,
        );
    }

    const selectedEdge = edges.find(edge => edge.semanticId === semanticId);
    if (selectedEdge) {
        const routeBounds = resolveAthenaGraphRouteBounds(selectedEdge.routePoints);
        return routeBounds
            ? keepAthenaGraphViewportFocusedOnBounds(transform, viewport, routeBounds, padding)
            : transform;
    }

    return transform;
}

export function keepAthenaGraphViewportFocusedOnBounds(
    transform: AthenaGraphViewportTransform,
    viewport: AthenaGraphViewportSize,
    bounds: AthenaGraphSceneBounds,
    padding: number = 48,
): AthenaGraphViewportTransform {
    if (viewport.width <= 0 || viewport.height <= 0) {
        return transform;
    }
    if (isAthenaGraphBoundsVisible(transform, viewport, bounds, padding)) {
        return transform;
    }
    return centerAthenaGraphViewportOnBounds(transform, viewport, bounds);
}

function resolveAthenaGraphNodeBounds(
    node: Pick<AthenaGraphWorkbenchNode, 'position' | 'size'>,
): AthenaGraphSceneBounds {
    const minX = node.position.x;
    const minY = node.position.y;
    const maxX = node.position.x + node.size.width;
    const maxY = node.position.y + node.size.height;
    return {
        minX,
        minY,
        maxX,
        maxY,
        width: Math.max(maxX - minX, 1),
        height: Math.max(maxY - minY, 1),
        centerX: minX + (Math.max(maxX - minX, 1) / 2),
        centerY: minY + (Math.max(maxY - minY, 1) / 2),
    };
}

function resolveAthenaGraphRouteBounds(routePoints: AthenaGLSPPoint[]): AthenaGraphSceneBounds | undefined {
    if (routePoints.length === 0) {
        return undefined;
    }

    let minX = Number.POSITIVE_INFINITY;
    let minY = Number.POSITIVE_INFINITY;
    let maxX = Number.NEGATIVE_INFINITY;
    let maxY = Number.NEGATIVE_INFINITY;

    for (const point of routePoints) {
        minX = Math.min(minX, point.x);
        minY = Math.min(minY, point.y);
        maxX = Math.max(maxX, point.x);
        maxY = Math.max(maxY, point.y);
    }

    if (!Number.isFinite(minX) || !Number.isFinite(minY) || !Number.isFinite(maxX) || !Number.isFinite(maxY)) {
        return undefined;
    }

    const width = Math.max(maxX - minX, 1);
    const height = Math.max(maxY - minY, 1);
    return {
        minX,
        minY,
        maxX,
        maxY,
        width,
        height,
        centerX: minX + (width / 2),
        centerY: minY + (height / 2),
    };
}

function centerAthenaGraphViewportOnBounds(
    transform: AthenaGraphViewportTransform,
    viewport: AthenaGraphViewportSize,
    bounds: AthenaGraphSceneBounds,
): AthenaGraphViewportTransform {
    const zoom = transform.zoom <= 0 ? 1 : transform.zoom;
    return {
        zoom,
        offsetX: (viewport.width / 2) - (bounds.centerX * zoom),
        offsetY: (viewport.height / 2) - (bounds.centerY * zoom),
    };
}

function isAthenaGraphBoundsVisible(
    transform: AthenaGraphViewportTransform,
    viewport: AthenaGraphViewportSize,
    bounds: AthenaGraphSceneBounds,
    padding: number,
): boolean {
    const zoom = transform.zoom <= 0 ? 1 : transform.zoom;
    const left = (bounds.minX * zoom) + transform.offsetX;
    const top = (bounds.minY * zoom) + transform.offsetY;
    const right = (bounds.maxX * zoom) + transform.offsetX;
    const bottom = (bounds.maxY * zoom) + transform.offsetY;
    const inset = Math.max(0, padding);
    return left >= inset &&
        top >= inset &&
        right <= Math.max(viewport.width - inset, inset) &&
        bottom <= Math.max(viewport.height - inset, inset);
}

function resolveEmptyState(
    diagram: AthenaGLSPDiagram,
    nodes: Array<{ id: string }>,
    edges: Array<{ id: string }>,
): AthenaGraphWorkbenchModel['emptyState'] {
    if (diagram.status !== 'ready') {
        return {
            title: 'Projection unavailable',
            message: diagram.unavailableReason ?? 'Athena did not publish a usable graphical projection for the active workbench session.',
        };
    }

    if (nodes.length === 0 && edges.length === 0) {
        return {
            title: 'Projection is empty',
            message: 'Athena published an active graphical view, but no nodes or relationships are currently visible in that projection.',
        };
    }

    return undefined;
}

function resolveSheetChrome(
    diagram: AthenaGLSPDiagram,
    canvasWidth: number,
    canvasHeight: number,
): AthenaGraphWorkbenchSheetChrome {
    const sheetSummaries = normalizeArray(diagram.sheets)
        .map(sheet => buildSheetSummary(sheet, diagram.activeSheetId))
        .sort(compareSheetSummaries);
    const activeSheet = sheetSummaries.find(sheet => sheet.isActive) ?? sheetSummaries[0];
    const crossReferenceMarkers = normalizeArray(diagram.crossReferences)
        .map(reference => ({
            semanticId: reference.semanticId,
            kind: reference.kind,
            markerLabel: humanizeMarkerKind(reference.kind),
            sheetIds: [...reference.sheetIds],
            occurrenceIds: [...reference.occurrenceIds],
            isActiveSheetLinked: activeSheet ? reference.sheetIds.includes(activeSheet.sheetId) : false,
        }))
        .sort(compareCrossReferenceMarkers);

    return {
        frame: {
            width: canvasWidth,
            height: canvasHeight,
        },
        grid: {
            majorStep: 120,
            minorStep: 24,
        },
        activeSheet,
        titleBlock: activeSheet ? {
            sheetId: activeSheet.sheetId,
            displayName: activeSheet.displayName,
            order: activeSheet.order,
            subjectCount: activeSheet.subjectCount,
            crossReferenceCount: crossReferenceMarkers.filter(marker => marker.isActiveSheetLinked).length,
            ...(activeSheet.previousSheetId ? { previousSheetId: activeSheet.previousSheetId } : {}),
            ...(activeSheet.nextSheetId ? { nextSheetId: activeSheet.nextSheetId } : {}),
        } : undefined,
        crossReferenceMarkers,
    };
}

function buildSheetSummary(
    sheet: AthenaGLSPDiagram['sheets'][number],
    activeSheetId: string | undefined,
): AthenaGraphWorkbenchSheetSummary {
    const subjectSemanticIds = normalizeArray(sheet.subjectSemanticIds);
    return {
        sheetId: sheet.sheetId,
        displayName: sheet.displayName,
        ...(sheet.role ? { role: sheet.role } : {}),
        order: sheet.order,
        subjectSemanticIds,
        subjectCount: subjectSemanticIds.length,
        isActive: sheet.sheetId === activeSheetId,
        ...(sheet.previousSheetId ? { previousSheetId: sheet.previousSheetId } : {}),
        ...(sheet.nextSheetId ? { nextSheetId: sheet.nextSheetId } : {}),
    };
}

function compareSheetSummaries(
    left: AthenaGraphWorkbenchSheetSummary,
    right: AthenaGraphWorkbenchSheetSummary,
): number {
    return (left.order - right.order) || compareStrings(left.sheetId, right.sheetId);
}

function compareCrossReferenceMarkers(
    left: AthenaGraphWorkbenchCrossReferenceMarker,
    right: AthenaGraphWorkbenchCrossReferenceMarker,
): number {
    return compareStrings(left.kind, right.kind) || compareStrings(left.semanticId, right.semanticId);
}

function compareReferenceMarkers(
    left: AthenaGraphWorkbenchReferenceMarker,
    right: AthenaGraphWorkbenchReferenceMarker,
): number {
    return compareStrings(left.markerId, right.markerId);
}

function compareDocumentReferenceInspectionEntries(
    left: AthenaGraphDocumentReferenceInspectionEntry,
    right: AthenaGraphDocumentReferenceInspectionEntry,
): number {
    return compareStrings(left.relationType, right.relationType)
        || compareStrings(left.markerId, right.markerId);
}

function compareStrings(left: string, right: string): number {
    return left < right ? -1 : left > right ? 1 : 0;
}

function humanizeMarkerKind(kind: string): string {
    return kind.replace(/[_-]+/g, ' ');
}

function resolveSceneBounds(
    nodes: AthenaGraphWorkbenchNode[],
    edges: AthenaGraphWorkbenchEdge[],
    canvasWidth: number,
    canvasHeight: number
): AthenaGraphSceneBounds {
    let minX = Number.POSITIVE_INFINITY;
    let minY = Number.POSITIVE_INFINITY;
    let maxX = Number.NEGATIVE_INFINITY;
    let maxY = Number.NEGATIVE_INFINITY;

    for (const node of nodes) {
        minX = Math.min(minX, node.position.x);
        minY = Math.min(minY, node.position.y);
        maxX = Math.max(maxX, node.position.x + node.size.width);
        maxY = Math.max(maxY, node.position.y + node.size.height);
    }

    for (const edge of edges) {
        for (const point of edge.routePoints) {
            minX = Math.min(minX, point.x);
            minY = Math.min(minY, point.y);
            maxX = Math.max(maxX, point.x);
            maxY = Math.max(maxY, point.y);
        }
    }

    if (!Number.isFinite(minX) || !Number.isFinite(minY) || !Number.isFinite(maxX) || !Number.isFinite(maxY)) {
        minX = 0;
        minY = 0;
        maxX = canvasWidth;
        maxY = canvasHeight;
    }

    const width = Math.max(maxX - minX, 1);
    const height = Math.max(maxY - minY, 1);

    return {
        minX,
        minY,
        maxX,
        maxY,
        width,
        height,
        centerX: minX + (width / 2),
        centerY: minY + (height / 2),
    };
}

function buildWorkbenchNode(
    node: AthenaGLSPNode,
    notationBySemanticId: Map<string, NonNullable<AthenaGLSPDiagram['notationPack']>['subjects'][number]>,
    anchorsByNodeId: Map<string, AthenaGLSPElectricalAnchorSource[]>,
    anchorByLabelId: Map<string, AthenaGLSPElectricalAnchorSource>,
    isElectricalFamily: boolean,
    representation: AthenaGraphResolvedPresentationRepresentation | undefined,
): AthenaGraphWorkbenchNode {
    const notation = notationBySemanticId.get(node.semanticId);
    const nodeAnchors = normalizeArray(anchorsByNodeId.get(node.id)).map(anchor => ({
        anchorId: anchor.anchorId,
        point: { x: anchor.x, y: anchor.y },
        side: anchor.side,
        portSemanticId: anchor.portSemanticId,
        labelId: anchor.labelId,
    }));
    const labelAnchor = node.kind === 'label' ? anchorByLabelId.get(node.id) : undefined;
    const renderVariant = representation
        ? 'electrical-device'
        : resolveNodeRenderVariant(node, notation?.symbolKey, labelAnchor, isElectricalFamily);

    return {
        ...node,
        renderVariant,
        notationSymbolKey: notation?.symbolKey,
        labelPolicy: notation?.labelPolicy,
        markerKeys: normalizeArray(notation?.markerKeys),
        labelLeader: renderVariant === 'electrical-terminal-label' && labelAnchor
            ? buildLabelLeader(node, labelAnchor)
            : undefined,
        electricalAnchors: nodeAnchors,
        presentationOccurrence: undefined,
        presentationRepresentation: representation,
        presentationParts: representation
            ? scaleRepresentationPartsToNode(representation.parts, node)
            : [],
        presentationTerminals: representation
            ? scaleRepresentationTerminalsToNode(representation, node)
            : [],
        presentationLabels: representation
            ? scaleRepresentationLabelsToNode(representation, node)
            : [],
    };
}

function buildWorkbenchNodeFromPresentation(
    occurrence: AthenaGraphResolvedPresentationOccurrence,
    notationBySemanticId: Map<string, NonNullable<AthenaGLSPDiagram['notationPack']>['subjects'][number]>,
    anchorsByNodeId: Map<string, AthenaGLSPElectricalAnchorSource[]>,
    anchorByLabelId: Map<string, AthenaGLSPElectricalAnchorSource>,
    isElectricalFamily: boolean,
    representation: AthenaGraphResolvedPresentationRepresentation | undefined,
): AthenaGraphWorkbenchNode {
    const notation = notationBySemanticId.get(occurrence.semanticId);
    const sourceProjectionIds = normalizeArray(occurrence.sourceProjectionIds);
    const nodeAnchors = sourceProjectionIds
        .flatMap(sourceProjectionId => normalizeArray(anchorsByNodeId.get(sourceProjectionId)))
        .map(anchor => ({
            anchorId: anchor.anchorId,
            point: { x: anchor.x, y: anchor.y },
            side: anchor.side,
            portSemanticId: anchor.portSemanticId,
            labelId: anchor.labelId,
        }));
    const labelAnchor = sourceProjectionIds
        .map(sourceProjectionId => anchorByLabelId.get(sourceProjectionId))
        .find(Boolean);
    const kind = occurrence.layer === 'label' ? 'label' as const : 'component' as const;
    const baseNode: AthenaGLSPNode = {
        id: occurrence.occurrenceId,
        semanticId: occurrence.semanticId,
        type: 'node',
        kind,
        label: occurrence.displayLabel ?? occurrence.semanticId,
        position: {
            x: occurrence.bounds.x,
            y: occurrence.bounds.y,
        },
        size: {
            width: occurrence.bounds.width,
            height: occurrence.bounds.height,
        },
    };
    const renderVariant = representation
        ? 'electrical-device'
        : resolveNodeRenderVariant(baseNode, notation?.symbolKey, labelAnchor, isElectricalFamily);
    return {
        ...baseNode,
        renderVariant,
        notationSymbolKey: notation?.symbolKey,
        labelPolicy: notation?.labelPolicy,
        markerKeys: normalizeArray(notation?.markerKeys.length ? notation.markerKeys : occurrence.markerKeys),
        labelLeader: renderVariant === 'electrical-terminal-label' && labelAnchor
            ? buildLabelLeader(baseNode, labelAnchor)
            : undefined,
        electricalAnchors: nodeAnchors,
        presentationOccurrence: occurrence,
        presentationRepresentation: representation,
        presentationParts: representation
            ? scaleRepresentationPartsToNode(representation.parts, baseNode)
            : normalizeArray(occurrence.parts),
        presentationTerminals: representation
            ? scaleRepresentationTerminalsToNode(representation, baseNode)
            : [],
        presentationLabels: representation
            ? scaleRepresentationLabelsToNode(representation, baseNode)
            : [],
    };
}

function resolveRepresentationForOccurrence(
    occurrence: AthenaGraphResolvedPresentationOccurrence,
    representationByProjectionId: Map<string, AthenaGraphResolvedPresentationRepresentation>,
    representationBySubjectId: Map<string, AthenaGraphResolvedPresentationRepresentation>,
): AthenaGraphResolvedPresentationRepresentation | undefined {
    for (const sourceProjectionId of normalizeArray(occurrence.sourceProjectionIds)) {
        const representation = representationByProjectionId.get(sourceProjectionId);
        if (representation) {
            return representation;
        }
    }
    return representationBySubjectId.get(occurrence.semanticId);
}

function scaleRepresentationPartsToNode(
    parts: AthenaGraphResolvedPresentationPart[],
    node: AthenaGLSPNode,
): AthenaGraphResolvedPresentationPart[] {
    return parts.map(part => ({
        ...part,
        bounds: {
            x: node.position.x,
            y: node.position.y,
            width: node.size.width,
            height: node.size.height,
        },
        commands: part.commands.map(command => scaleRepresentationCommand(command, part.bounds, node)),
        textSlots: part.textSlots.map(slot => ({
            ...slot,
            x: scaleWithinNode(slot.x, part.bounds.width, node.position.x, node.size.width),
            y: scaleWithinNode(slot.y, part.bounds.height, node.position.y, node.size.height),
        })),
    }));
}

function scaleRepresentationCommand(
    command: AthenaGraphResolvedPresentationPart['commands'][number],
    sourceBounds: AthenaGraphResolvedPresentationPart['bounds'],
    node: AthenaGLSPNode,
): AthenaGraphResolvedPresentationPart['commands'][number] {
    return {
        ...command,
        bounds: command.bounds
            ? {
                x: scaleWithinNode(command.bounds.x, sourceBounds.width, node.position.x, node.size.width),
                y: scaleWithinNode(command.bounds.y, sourceBounds.height, node.position.y, node.size.height),
                width: scaleLengthWithinNode(command.bounds.width, sourceBounds.width, node.size.width),
                height: scaleLengthWithinNode(command.bounds.height, sourceBounds.height, node.size.height),
            }
            : undefined,
        start: command.start
            ? {
                x: scaleWithinNode(command.start.x, sourceBounds.width, node.position.x, node.size.width),
                y: scaleWithinNode(command.start.y, sourceBounds.height, node.position.y, node.size.height),
            }
            : undefined,
        end: command.end
            ? {
                x: scaleWithinNode(command.end.x, sourceBounds.width, node.position.x, node.size.width),
                y: scaleWithinNode(command.end.y, sourceBounds.height, node.position.y, node.size.height),
            }
            : undefined,
        center: command.center
            ? {
                x: scaleWithinNode(command.center.x, sourceBounds.width, node.position.x, node.size.width),
                y: scaleWithinNode(command.center.y, sourceBounds.height, node.position.y, node.size.height),
            }
            : undefined,
        radius: command.radius === undefined
            ? undefined
            : Math.max(1, Math.round(command.radius * Math.min(node.size.width / sourceBounds.width, node.size.height / sourceBounds.height))),
    };
}

function scaleWithinNode(value: number, sourceLength: number, targetOrigin: number, targetLength: number): number {
    return targetOrigin + Math.round((value / sourceLength) * targetLength);
}

function scaleLengthWithinNode(value: number, sourceLength: number, targetLength: number): number {
    return Math.round((value / sourceLength) * targetLength);
}

function scaleRepresentationTerminalsToNode(
    representation: AthenaGraphResolvedPresentationRepresentation,
    node: AthenaGLSPNode,
): AthenaGraphWorkbenchPresentationTerminal[] {
    const bounds = representation.parts[0]?.bounds ?? { x: 0, y: 0, width: node.size.width, height: node.size.height };
    return normalizeArray(representation.terminals).map(terminal => ({
        terminalId: terminal.presentationTerminalId,
        subjectId: terminal.subjectId,
        occurrenceId: terminal.occurrenceId,
        portId: terminal.portId,
        physicalTerminalId: terminal.physicalTerminalId,
        side: terminal.side,
        marker: terminal.notation.marker,
        number: terminal.notation.number,
        point: {
            x: scaleWithinNode(terminal.routeAnchor.point.x, bounds.width, node.position.x, node.size.width),
            y: scaleWithinNode(terminal.routeAnchor.point.y, bounds.height, node.position.y, node.size.height),
        },
        anchorId: terminal.routeAnchor.anchorId,
    }));
}

function scaleRepresentationLabelsToNode(
    representation: AthenaGraphResolvedPresentationRepresentation,
    node: AthenaGLSPNode,
): AthenaGraphWorkbenchPresentationLabel[] {
    const bounds = representation.parts[0]?.bounds ?? { x: 0, y: 0, width: node.size.width, height: node.size.height };
    return normalizeArray(representation.labels).map(label => ({
        labelId: label.labelId,
        subjectId: label.subjectId,
        occurrenceId: label.occurrenceId,
        role: label.role,
        value: label.value,
        point: {
            x: scaleWithinNode(label.anchor.point.x, bounds.width, node.position.x, node.size.width),
            y: scaleWithinNode(label.anchor.point.y, bounds.height, node.position.y, node.size.height),
        },
        anchorId: label.anchor.anchorId,
    }));
}

function resolveNodeRenderVariant(
    node: AthenaGLSPNode,
    symbolKey: string | undefined,
    labelAnchor: AthenaGLSPElectricalAnchorSource | undefined,
    isElectricalFamily: boolean,
): AthenaGraphWorkbenchNode['renderVariant'] {
    if (!isElectricalFamily) {
        return node.kind === 'component' ? 'generic-component' : 'generic-label';
    }
    if (node.kind === 'component' && symbolKey?.startsWith('device.')) {
        return 'electrical-device';
    }
    if (node.kind === 'label' && labelAnchor && symbolKey?.startsWith('port.')) {
        return 'electrical-terminal-label';
    }
    return node.kind === 'component' ? 'generic-component' : 'generic-label';
}

function buildLabelLeader(
    node: AthenaGLSPNode,
    anchor: AthenaGLSPElectricalAnchorSource,
): AthenaGraphWorkbenchLeaderSegment {
    return {
        start: {
            x: anchor.x,
            y: anchor.y,
        },
        end: clampPointToNodeBounds(
            {
                x: anchor.x,
                y: anchor.y,
            },
            node,
        ),
    };
}

function clampPointToNodeBounds(
    point: AthenaGLSPPoint,
    node: AthenaGLSPNode,
): AthenaGLSPPoint {
    return {
        x: point.x < node.position.x
            ? node.position.x
            : point.x > node.position.x + node.size.width
                ? node.position.x + node.size.width
                : point.x,
        y: point.y < node.position.y
            ? node.position.y
            : point.y > node.position.y + node.size.height
                ? node.position.y + node.size.height
                : point.y,
    };
}

function resolveSurfaceTokens(
    contributions: AthenaGLSPRenderContributionSource[]
): AthenaGraphSurfaceTokens {
    const tokens: AthenaGraphSurfaceTokens = {
        canvas: {},
        node: {},
        edge: {},
    };

    for (const contribution of contributions) {
        for (const mapping of contribution.surfaceMappings) {
            if (mapping.surface === 'canvas') {
                Object.assign(tokens.canvas, mapping.tokens);
            } else if (mapping.surface === 'node') {
                Object.assign(tokens.node, mapping.tokens);
            } else if (mapping.surface === 'edge') {
                Object.assign(tokens.edge, mapping.tokens);
            }
        }
    }

    return tokens;
}

function buildWorkbenchEdge(
    edge: AthenaGLSPEdge,
    endpoints: AthenaGLSPElectricalConnectionEndpointSource[],
    anchorById: Map<string, AthenaGLSPElectricalAnchorSource>,
): AthenaGraphWorkbenchEdge {
    const bendPoints = edge.bendPoints ?? [];
    const routePoints = [edge.sourcePoint, ...bendPoints, edge.targetPoint].map(point => ({
        x: point.x,
        y: point.y,
    }));
    return {
        ...edge,
        bendPoints,
        routePoints,
        bendMarkerPoints: bendPoints.map(point => ({ x: point.x, y: point.y })),
        crossingMarkerPoints: [],
        routeLabels: [],
        path: buildEdgePath(routePoints),
        conductorStyle: edge.routingStyle === 'orthogonal' || bendPoints.length > 0 ? 'electrical' : 'generic',
        terminals: [
            buildWorkbenchTerminal('source', edge.sourcePoint, edge.sourceAnchorId, edge.sourcePortSemanticId, endpoints, anchorById),
            buildWorkbenchTerminal('target', edge.targetPoint, edge.targetAnchorId, edge.targetPortSemanticId, endpoints, anchorById),
        ],
        presentationConnector: undefined,
    };
}

function buildWorkbenchEdgeFromPresentation(
    connector: AthenaGraphResolvedPresentationConnector,
    endpointsByConnectionId: Map<string, AthenaGLSPElectricalConnectionEndpointSource[]>,
    anchorById: Map<string, AthenaGLSPElectricalAnchorSource>,
): AthenaGraphWorkbenchEdge {
    const routePoints = normalizeArray(connector.routePoints).map(point => ({ x: point.x, y: point.y }));
    const sourcePoint = routePoints[0] ?? { x: 0, y: 0 };
    const targetPoint = routePoints[routePoints.length - 1] ?? sourcePoint;
    const anchorScopedEndpoints = [...endpointsByConnectionId.values()]
        .flat()
        .filter(endpoint => endpoint.connectionSemanticId === connector.semanticId);
    const edge: AthenaGLSPEdge = {
        id: connector.occurrenceId,
        semanticId: connector.semanticId,
        type: 'edge',
        sourcePoint,
        targetPoint,
        routingStyle: 'orthogonal',
        bendPoints: routePoints.slice(1, Math.max(routePoints.length - 1, 1)),
        sourceAnchorId: connector.sourceAnchorId,
        targetAnchorId: connector.targetAnchorId,
        sourcePortSemanticId: connector.sourcePortSemanticId,
        targetPortSemanticId: connector.targetPortSemanticId,
    };
    const built = buildWorkbenchEdge(edge, anchorScopedEndpoints, anchorById);
    return {
        ...built,
        routePoints,
        bendMarkerPoints: routePoints.slice(1, Math.max(routePoints.length - 1, 1)),
        crossingMarkerPoints: [],
        routeLabels: buildRouteLabels(connector, routePoints),
        path: buildEdgePath(routePoints),
        presentationConnector: connector,
    };
}

function buildRouteLabels(
    connector: AthenaGraphResolvedPresentationConnector,
    routePoints: AthenaGLSPPoint[],
): AthenaGraphWorkbenchRouteLabel[] {
    const labelTexts = (connector.tokenOverrides.routeLabels ?? '')
        .split('|')
        .map(text => text.trim())
        .filter(Boolean);
    if (labelTexts.length === 0 || routePoints.length < 2) {
        return [];
    }
    const anchorSegment = routePointSegments(routePoints)
        .sort((left, right) => routePointSegmentLength(right) - routePointSegmentLength(left))[0];
    if (!anchorSegment) {
        return [];
    }
    return labelTexts.map((text, index) => ({
        text,
        point: routeLabelPoint(anchorSegment, index),
        canvasDisplay: resolveRouteLabelCanvasDisplay(text),
    }));
}

function resolveRouteLabelCanvasDisplay(text: string): AthenaGraphWorkbenchRouteLabel['canvasDisplay'] {
    const normalized = text.trim();
    if (normalized.includes('->') || normalized.length > 24) {
        return 'selection';
    }
    return 'always';
}

function routeLabelPoint(
    segment: [AthenaGLSPPoint, AthenaGLSPPoint],
    index: number,
): AthenaGLSPPoint {
    const [start, end] = segment;
    const midpoint = {
        x: Math.trunc((start.x + end.x) / 2),
        y: Math.trunc((start.y + end.y) / 2),
    };
    const offset = 16 + (index * 14);
    if (start.y === end.y) {
        return { x: midpoint.x, y: Math.max(0, midpoint.y - offset) };
    }
    return { x: midpoint.x + offset, y: midpoint.y };
}

function withCrossingMarkers(edges: AthenaGraphWorkbenchEdge[]): AthenaGraphWorkbenchEdge[] {
    const crossingsByEdgeId = new Map<string, AthenaGLSPPoint[]>();
    for (let leftIndex = 0; leftIndex < edges.length; leftIndex += 1) {
        for (let rightIndex = leftIndex + 1; rightIndex < edges.length; rightIndex += 1) {
            for (const leftSegment of routePointSegments(edges[leftIndex].routePoints)) {
                for (const rightSegment of routePointSegments(edges[rightIndex].routePoints)) {
                    const crossing = routeSegmentCrossing(leftSegment, rightSegment);
                    if (!crossing) {
                        continue;
                    }
                    appendUniquePoint(crossingsByEdgeId, edges[leftIndex].id, crossing);
                    appendUniquePoint(crossingsByEdgeId, edges[rightIndex].id, crossing);
                }
            }
        }
    }
    return edges.map(edge => ({
        ...edge,
        crossingMarkerPoints: crossingsByEdgeId.get(edge.id) ?? [],
    }));
}

function routePointSegments(routePoints: AthenaGLSPPoint[]): Array<[AthenaGLSPPoint, AthenaGLSPPoint]> {
    const segments: Array<[AthenaGLSPPoint, AthenaGLSPPoint]> = [];
    for (let index = 0; index < routePoints.length - 1; index += 1) {
        segments.push([routePoints[index], routePoints[index + 1]]);
    }
    return segments;
}

function routePointSegmentLength(segment: [AthenaGLSPPoint, AthenaGLSPPoint]): number {
    return Math.abs(segment[0].x - segment[1].x) + Math.abs(segment[0].y - segment[1].y);
}

function routeSegmentCrossing(
    left: [AthenaGLSPPoint, AthenaGLSPPoint],
    right: [AthenaGLSPPoint, AthenaGLSPPoint],
): AthenaGLSPPoint | undefined {
    const leftHorizontal = left[0].y === left[1].y;
    const rightHorizontal = right[0].y === right[1].y;
    if (leftHorizontal === rightHorizontal) {
        return undefined;
    }
    const horizontal = leftHorizontal ? left : right;
    const vertical = leftHorizontal ? right : left;
    const y = horizontal[0].y;
    const x = vertical[0].x;
    const horizontalMinX = Math.min(horizontal[0].x, horizontal[1].x);
    const horizontalMaxX = Math.max(horizontal[0].x, horizontal[1].x);
    const verticalMinY = Math.min(vertical[0].y, vertical[1].y);
    const verticalMaxY = Math.max(vertical[0].y, vertical[1].y);
    if (x <= horizontalMinX || x >= horizontalMaxX || y <= verticalMinY || y >= verticalMaxY) {
        return undefined;
    }
    return { x, y };
}

function appendUniquePoint(
    pointMap: Map<string, AthenaGLSPPoint[]>,
    edgeId: string,
    point: AthenaGLSPPoint,
): void {
    const points = pointMap.get(edgeId) ?? [];
    if (!points.some(existing => existing.x === point.x && existing.y === point.y)) {
        points.push(point);
    }
    pointMap.set(edgeId, points);
}

function buildWorkbenchTerminal(
    role: 'source' | 'target',
    point: AthenaGLSPPoint,
    fallbackAnchorId: string | undefined,
    fallbackPortSemanticId: string | undefined,
    endpoints: AthenaGLSPElectricalConnectionEndpointSource[],
    anchorById: Map<string, AthenaGLSPElectricalAnchorSource>,
): AthenaGraphWorkbenchEdgeTerminal {
    const endpoint = endpoints.find(candidate => candidate.endpointRole === role);
    const anchor = endpoint?.anchorId
        ? anchorById.get(endpoint.anchorId)
        : fallbackAnchorId
            ? anchorById.get(fallbackAnchorId)
            : undefined;
    return {
        role,
        point: {
            x: point.x,
            y: point.y,
        },
        endpointId: endpoint?.endpointId,
        anchorId: endpoint?.anchorId ?? fallbackAnchorId,
        portSemanticId: endpoint?.portSemanticId ?? fallbackPortSemanticId ?? anchor?.portSemanticId,
        ownerSemanticId: anchor?.ownerSemanticId,
        nodeId: anchor?.nodeId,
        labelId: anchor?.labelId,
    };
}

function groupEndpointsByConnectionId(
    endpoints: AthenaGLSPElectricalConnectionEndpointSource[],
): Map<string, AthenaGLSPElectricalConnectionEndpointSource[]> {
    const grouped = new Map<string, AthenaGLSPElectricalConnectionEndpointSource[]>();
    for (const endpoint of endpoints) {
        const current = grouped.get(endpoint.projectionConnectionId);
        if (current) {
            current.push(endpoint);
        } else {
            grouped.set(endpoint.projectionConnectionId, [endpoint]);
        }
    }
    return grouped;
}

function groupAnchorsByNodeId(
    anchors: AthenaGLSPElectricalAnchorSource[],
): Map<string, AthenaGLSPElectricalAnchorSource[]> {
    const grouped = new Map<string, AthenaGLSPElectricalAnchorSource[]>();
    for (const anchor of anchors) {
        const current = grouped.get(anchor.nodeId);
        if (current) {
            current.push(anchor);
        } else {
            grouped.set(anchor.nodeId, [anchor]);
        }
    }
    return grouped;
}

function buildEdgePath(points: AthenaGLSPPoint[]): string {
    if (points.length === 0) {
        return '';
    }
    return points.map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x} ${point.y}`).join(' ');
}

function normalizeArray<T>(value: readonly T[] | T[] | undefined): T[] {
    return Array.isArray(value) ? [...value] : [];
}
