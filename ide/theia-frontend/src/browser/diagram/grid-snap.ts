export type SheetPoint = { x: number; y: number };

/** Quantizes a canonical Sheet point without deriving or persisting ruler addresses. */
export function snapSheetPoint(point: SheetPoint, step: number): SheetPoint {
    if (!Number.isInteger(step) || step <= 0) throw new Error('Sheet snap step must be a positive integer.');
    if (!Number.isInteger(point.x) || !Number.isInteger(point.y) || point.x <= 0 || point.y <= 0) {
        throw new Error('Sheet point coordinates must be positive integers.');
    }
    return {
        x: nearestStep(point.x, step),
        y: nearestStep(point.y, step),
    };
}

function nearestStep(value: number, step: number): number {
    return Math.max(step, Math.floor(value / step + 0.5) * step);
}
