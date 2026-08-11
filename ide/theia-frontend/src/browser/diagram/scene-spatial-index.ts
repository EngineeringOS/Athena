export type SceneBoundsLike = {
    x: number;
    y: number;
    width: number;
    height: number;
};

export type SceneBoundedEntry = {
    bounds: SceneBoundsLike;
};

/**
 * Deterministic broad-phase index for scene entries.
 *
 * The index only narrows candidates. Final bounds intersection and source-order
 * preservation stay here so culling never changes paint or hit identity order.
 */
export class SceneSpatialIndex<T extends SceneBoundedEntry> {
    private readonly buckets = new Map<string, T[]>();

    constructor(
        private readonly entries: readonly T[],
        private readonly bucketSize = 128,
    ) {
        if (!Number.isFinite(bucketSize) || bucketSize <= 0) {
            throw new Error('Scene spatial index bucket size must be positive.');
        }
        for (const entry of entries) {
            this.add(entry);
        }
    }

    query(viewport: SceneBoundsLike): T[] {
        const candidates = new Set<T>();
        const minColumn = Math.floor(viewport.x / this.bucketSize);
        const maxColumn = Math.floor((viewport.x + Math.max(0, viewport.width)) / this.bucketSize);
        const minRow = Math.floor(viewport.y / this.bucketSize);
        const maxRow = Math.floor((viewport.y + Math.max(0, viewport.height)) / this.bucketSize);

        for (let row = minRow; row <= maxRow; row += 1) {
            for (let column = minColumn; column <= maxColumn; column += 1) {
                for (const entry of this.buckets.get(this.key(column, row)) ?? []) {
                    candidates.add(entry);
                }
            }
        }

        return this.entries.filter(entry => candidates.has(entry) && intersects(entry.bounds, viewport));
    }

    private add(entry: T): void {
        const minColumn = Math.floor(entry.bounds.x / this.bucketSize);
        const maxColumn = Math.floor((entry.bounds.x + Math.max(0, entry.bounds.width)) / this.bucketSize);
        const minRow = Math.floor(entry.bounds.y / this.bucketSize);
        const maxRow = Math.floor((entry.bounds.y + Math.max(0, entry.bounds.height)) / this.bucketSize);
        for (let row = minRow; row <= maxRow; row += 1) {
            for (let column = minColumn; column <= maxColumn; column += 1) {
                const key = this.key(column, row);
                const bucket = this.buckets.get(key);
                if (bucket) {
                    bucket.push(entry);
                } else {
                    this.buckets.set(key, [entry]);
                }
            }
        }
    }

    private key(column: number, row: number): string {
        return `${column}:${row}`;
    }
}

function intersects(left: SceneBoundsLike, right: SceneBoundsLike): boolean {
    return left.x < right.x + right.width
        && left.x + left.width > right.x
        && left.y < right.y + right.height
        && left.y + left.height > right.y;
}
