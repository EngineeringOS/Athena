import { AthenaScenePublication, ResolvedStyle } from './generated/types';

export type StylePreviewTarget = { kind: 'ROLE' | 'OCCURRENCE'; id: string };

export type StylePreviewFields = {
    strokeRgba?: string;
    fillRgba?: string;
    strokeWidth?: number;
    dash?: number[];
    lineCap?: ResolvedStyle['lineCap'];
    lineJoin?: ResolvedStyle['lineJoin'];
    opacity?: number;
    fontSize?: number;
    fontWeight?: number;
    routeMarker?: ResolvedStyle['routeMarker'];
    portDisplay?: ResolvedStyle['portDisplay'];
};

/** Disposable paint overlay. Never owns scene, source, or engineering identity. */
export class StylePreviewSession {
    private acceptedInputRevision: string | undefined;
    private readonly overlays = new Map<string, Readonly<StylePreviewFields>>();

    preview(target: StylePreviewTarget, fields: StylePreviewFields, acceptedInputRevision: string): void {
        if (this.acceptedInputRevision !== acceptedInputRevision) {
            this.overlays.clear();
            this.acceptedInputRevision = acceptedInputRevision;
        }
        this.overlays.set(key(target), Object.freeze({ ...fields, dash: fields.dash ? [...fields.dash] : undefined }));
    }

    discard(): void {
        this.overlays.clear();
    }

    clearForPublication(publication: AthenaScenePublication | undefined): void {
        const revision = publication && publication.state !== 'UNAVAILABLE' ? publication.acceptedInputRevision : undefined;
        if (revision !== this.acceptedInputRevision) this.overlays.clear();
        this.acceptedInputRevision = revision;
    }

    fields(target: StylePreviewTarget): Readonly<StylePreviewFields> | undefined {
        return this.overlays.get(key(target));
    }

    resolve(base: ResolvedStyle | undefined, role: string, occurrenceId?: string): ResolvedStyle | undefined {
        if (!base) return undefined;
        const roleFields = this.fields({ kind: 'ROLE', id: role });
        const occurrenceFields = occurrenceId ? this.fields({ kind: 'OCCURRENCE', id: occurrenceId }) : undefined;
        return { ...base, ...roleFields, ...occurrenceFields };
    }
}

function key(target: StylePreviewTarget): string {
    return `${target.kind}:${target.id}`;
}
