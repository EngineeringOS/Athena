import type { AthenaAvailableComponentImplementationPayload, AthenaAvailableComponentPayload } from './athena-component-knowledge-protocol';
export type AthenaComponentPanelCategoryId = 'plc' | 'power-supply' | 'motor' | 'contactor' | 'protection' | 'other';
export type AthenaComponentPanelItem = {
    conceptId: string;
    displayName: string;
    summary?: string;
    classificationKeys: string[];
    implementations: AthenaAvailableComponentImplementationPayload[];
    preferredImplementation?: AthenaAvailableComponentImplementationPayload;
};
export type AthenaComponentPanelGroup = {
    categoryId: AthenaComponentPanelCategoryId;
    label: string;
    items: AthenaComponentPanelItem[];
};
export declare function buildAthenaComponentPanelGroups(availableComponents: AthenaAvailableComponentPayload[]): AthenaComponentPanelGroup[];
export declare function classifyAthenaComponentCategory(component: AthenaAvailableComponentPayload): AthenaComponentPanelCategoryId;
//# sourceMappingURL=athena-component-panel-model.d.ts.map