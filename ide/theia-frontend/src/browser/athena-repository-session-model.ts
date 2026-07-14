export type AthenaRepositorySessionCoverageState = {
    lifecycle: 'idle' | 'activating' | 'ready' | 'unavailable';
    repositoryRoot?: string;
    sourceRootPath?: string;
};

export function normalizeAthenaFsPath(path: string): string {
    const normalized = path
        .replace(/\\/g, '/')
        .replace(/^\/([A-Za-z]:)/, '$1')
        .replace(/\/+$/, '');
    return normalized.replace(/^([A-Z]):/, (_, drive: string) => `${drive.toLowerCase()}:`);
}

export function athenaFileUriToFsPath(documentUri: string): string | undefined {
    try {
        const url = new URL(documentUri);
        if (url.protocol !== 'file:') {
            return undefined;
        }
        return normalizeAthenaFsPath(decodeURIComponent(url.pathname));
    } catch {
        return undefined;
    }
}

export function isAthenaDocumentCoveredBySession(
    state: AthenaRepositorySessionCoverageState,
    documentUri: string
): boolean {
    if (state.lifecycle !== 'ready') {
        return false;
    }

    const documentPath = athenaFileUriToFsPath(documentUri);
    if (!documentPath) {
        return false;
    }

    const candidateRoots = [state.sourceRootPath, state.repositoryRoot]
        .filter((value): value is string => typeof value === 'string' && value.trim().length > 0)
        .map(normalizeAthenaFsPath);

    return candidateRoots.some(root => documentPath === root || documentPath.startsWith(`${root}/`));
}

