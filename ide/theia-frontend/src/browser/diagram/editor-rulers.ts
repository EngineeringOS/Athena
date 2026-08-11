export type EditorRulerLabels = {
    rows: string[];
    columns: string[];
};

export function editorRulerLabels(rows: number, columns: number): EditorRulerLabels {
    return {
        rows: Array.from({ length: rows }, (_, index) => rowNumberToLabel(index + 1)),
        columns: Array.from({ length: columns }, (_, index) => String(index + 1)),
    };
}

export function rowNumberToLabel(row: number): string {
    let value = row;
    let label = '';
    while (value > 0) {
        const remainder = (value - 1) % 26;
        label = String.fromCharCode(65 + remainder) + label;
        value = Math.floor((value - 1) / 26);
    }
    return label;
}
