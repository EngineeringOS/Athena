import type {
    AthenaAvailableComponentImplementationPayload,
    AthenaAvailableComponentPayload
} from './athena-component-knowledge-protocol';

export type AthenaComponentPanelCategoryId =
    | 'plc'
    | 'power-supply'
    | 'motor'
    | 'contactor'
    | 'protection'
    | 'other';

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

const CATEGORY_ORDER: AthenaComponentPanelCategoryId[] = [
    'plc',
    'power-supply',
    'motor',
    'contactor',
    'protection',
    'other'
];

const CATEGORY_LABELS: Record<AthenaComponentPanelCategoryId, string> = {
    plc: 'PLC',
    'power-supply': 'Power Supply',
    motor: 'Motor',
    contactor: 'Contactor',
    protection: 'Protection',
    other: 'Other'
};

export function buildAthenaComponentPanelGroups(
    availableComponents: AthenaAvailableComponentPayload[]
): AthenaComponentPanelGroup[] {
    const groups = new Map<AthenaComponentPanelCategoryId, AthenaComponentPanelItem[]>();

    for (const component of [...availableComponents].sort(compareAvailableComponents)) {
        const categoryId = classifyAthenaComponentCategory(component);
        const implementations = [...component.implementations].sort(compareImplementations);
        const existing = groups.get(categoryId) ?? [];
        existing.push({
            conceptId: component.conceptId,
            displayName: component.displayName,
            summary: component.summary,
            classificationKeys: [...component.classificationKeys].sort((left, right) => left.localeCompare(right)),
            implementations,
            preferredImplementation: implementations[0]
        });
        groups.set(categoryId, existing);
    }

    return CATEGORY_ORDER
        .map(categoryId => ({
            categoryId,
            label: CATEGORY_LABELS[categoryId],
            items: groups.get(categoryId) ?? []
        }))
        .filter(group => group.items.length > 0);
}

export function classifyAthenaComponentCategory(
    component: AthenaAvailableComponentPayload
): AthenaComponentPanelCategoryId {
    const classificationKeys = new Set(component.classificationKeys.map(key => key.toLowerCase()));
    const normalizedConceptId = component.conceptId.toLowerCase();

    if (classificationKeys.has('plc') || classificationKeys.has('cpu') || normalizedConceptId.includes('.plc.')) {
        return 'plc';
    }
    if (classificationKeys.has('supply') || classificationKeys.has('24vdc') || normalizedConceptId.includes('power-supply')) {
        return 'power-supply';
    }
    if (classificationKeys.has('motor') || normalizedConceptId.includes('.motor.')) {
        return 'motor';
    }
    if (classificationKeys.has('contactor') || normalizedConceptId.includes('contactor')) {
        return 'contactor';
    }
    if (classificationKeys.has('protection') || classificationKeys.has('relay') || classificationKeys.has('overload')) {
        return 'protection';
    }
    return 'other';
}

function compareAvailableComponents(
    left: AthenaAvailableComponentPayload,
    right: AthenaAvailableComponentPayload
): number {
    return left.displayName.localeCompare(right.displayName)
        || left.conceptId.localeCompare(right.conceptId);
}

function compareImplementations(
    left: AthenaAvailableComponentImplementationPayload,
    right: AthenaAvailableComponentImplementationPayload
): number {
    return left.vendorId.localeCompare(right.vendorId)
        || left.vendorPartNumber.localeCompare(right.vendorPartNumber)
        || left.displayName.localeCompare(right.displayName)
        || left.implementationId.localeCompare(right.implementationId);
}
