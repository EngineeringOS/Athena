import readySheet from '../../m19/schematic-sheet-proof/ready-sheet.diagram.mjs';

const readyDiagram = {
    ...readySheet,
    projectName: 'M20SchematicCompositionProof',
    sheets: readySheet.sheets.map(currentSheet => ({
        ...currentSheet,
        composition: {
            sheetId: currentSheet.sheetId,
            displayName: currentSheet.displayName,
            order: currentSheet.order,
            subjectSemanticIds: currentSheet.subjectSemanticIds,
            representationFamilyId: 'schematic-sheet',
            publication: {
                pageSize: {
                    format: 'A3',
                    orientation: 'landscape',
                },
                frame: {
                    frameId: 'engineering-sheet-frame',
                    style: 'schematic',
                },
                coordinateZones: [
                    {
                        zoneId: 'header',
                        label: 'Header',
                        order: 0,
                    },
                    {
                        zoneId: 'body',
                        label: 'Body',
                        order: 1,
                    },
                    {
                        zoneId: 'title-block',
                        label: 'Title Block',
                        order: 2,
                    },
                ],
                titleBlock: {
                    sheetTitle: currentSheet.displayName,
                    sheetFamily: 'schematic-sheet',
                    sheetNumber: '01-main',
                },
                revisionMetadata: {
                    revisionCode: 'A',
                    revisionNote: 'Initial governed sheet publication',
                },
                viewComposition: {
                    primaryViewId: 'schematic-sheet',
                    primarySheetOrder: currentSheet.order,
                    subjectSemanticIds: currentSheet.subjectSemanticIds,
                },
            },
        },
    })),
};

export default readyDiagram;
