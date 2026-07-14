import type { AthenaSemanticMacroCatalogEntryPayload } from './athena-semantic-macro-protocol';

export type AthenaSemanticMacroCatalogCategoryId =
    | 'starter'
    | 'plc'
    | 'power-distribution'
    | 'other';

export type AthenaSemanticMacroCatalogItem = AthenaSemanticMacroCatalogEntryPayload & {
    classificationKeys: string[];
    packageLabel: string;
};

export type AthenaSemanticMacroCatalogGroup = {
    categoryId: AthenaSemanticMacroCatalogCategoryId;
    label: string;
    items: AthenaSemanticMacroCatalogItem[];
};

const CATEGORY_ORDER: AthenaSemanticMacroCatalogCategoryId[] = [
    'starter',
    'plc',
    'power-distribution',
    'other'
];

const CATEGORY_LABELS: Record<AthenaSemanticMacroCatalogCategoryId, string> = {
    starter: 'Starter',
    plc: 'PLC',
    'power-distribution': 'Power Distribution',
    other: 'Other'
};

export function buildAthenaSemanticMacroCatalogGroups(
    entries: AthenaSemanticMacroCatalogEntryPayload[]
): AthenaSemanticMacroCatalogGroup[] {
    const groups = new Map<AthenaSemanticMacroCatalogCategoryId, AthenaSemanticMacroCatalogItem[]>();

    for (const entry of [...entries].sort(compareCatalogEntries)) {
        const categoryId = classifyAthenaSemanticMacroCategory(entry);
        const classificationKeys = [...entry.classificationKeys].sort((left, right) => left.localeCompare(right));
        const existing = groups.get(categoryId) ?? [];
        existing.push({
            ...entry,
            classificationKeys,
            packageLabel: entry.packageVersion
                ? `${entry.packageName}@${entry.packageVersion}`
                : entry.packageName
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

export function classifyAthenaSemanticMacroCategory(
    entry: AthenaSemanticMacroCatalogEntryPayload
): AthenaSemanticMacroCatalogCategoryId {
    const classificationKeys = new Set(entry.classificationKeys.map(key => key.toLowerCase()));
    const normalizedDisplayName = entry.displayName.toLowerCase();
    const normalizedMacroId = entry.macroId.toLowerCase();

    if (
        classificationKeys.has('starter')
        || classificationKeys.has('dol')
        || normalizedDisplayName.includes('starter')
        || normalizedMacroId.includes('starter')
    ) {
        return 'starter';
    }
    if (
        classificationKeys.has('plc')
        || classificationKeys.has('rack')
        || normalizedDisplayName.includes('plc')
        || normalizedDisplayName.includes('rack')
        || normalizedMacroId.includes('plc')
    ) {
        return 'plc';
    }
    if (
        classificationKeys.has('24v')
        || classificationKeys.has('24vdc')
        || classificationKeys.has('distribution')
        || classificationKeys.has('power')
        || normalizedDisplayName.includes('24v')
        || normalizedDisplayName.includes('distribution')
        || normalizedMacroId.includes('distribution')
    ) {
        return 'power-distribution';
    }
    return 'other';
}

function compareCatalogEntries(
    left: AthenaSemanticMacroCatalogEntryPayload,
    right: AthenaSemanticMacroCatalogEntryPayload
): number {
    return left.displayName.localeCompare(right.displayName)
        || left.macroId.localeCompare(right.macroId)
        || left.packageName.localeCompare(right.packageName)
        || (left.packageVersion ?? '').localeCompare(right.packageVersion ?? '')
        || left.definitionPath.localeCompare(right.definitionPath);
}
