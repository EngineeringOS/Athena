export type AthenaGraphPanState = {
    pointerId: number;
    lastClientX: number;
    lastClientY: number;
};

export type AthenaGraphNodeDragState = {
    pointerId: number;
    semanticId: string;
    subjectKind: 'component';
    originX: number;
    originY: number;
    currentX: number;
    currentY: number;
    startClientX: number;
    startClientY: number;
    moved: boolean;
};

export type AthenaGraphPortConnectSource = {
    semanticId: string;
    label: string;
};
