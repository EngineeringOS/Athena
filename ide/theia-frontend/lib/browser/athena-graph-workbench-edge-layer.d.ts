import * as React from '@theia/core/shared/react';
import { AthenaGraphWorkbenchEdge } from './athena-graph-workbench-model';
type AthenaGraphWorkbenchEdgeLayerProps = {
    edges: AthenaGraphWorkbenchEdge[];
    selectedSemanticId: string | undefined;
    onSelectSemanticId: (semanticId: string) => void | Promise<unknown>;
};
/** Pure SVG edge layer for conductor-style Athena graph rendering. */
export declare function AthenaGraphWorkbenchEdgeLayer(props: AthenaGraphWorkbenchEdgeLayerProps): React.ReactNode;
export {};
//# sourceMappingURL=athena-graph-workbench-edge-layer.d.ts.map