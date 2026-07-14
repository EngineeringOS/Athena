"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.normalizeAthenaFsPath = normalizeAthenaFsPath;
exports.athenaFileUriToFsPath = athenaFileUriToFsPath;
exports.isAthenaDocumentCoveredBySession = isAthenaDocumentCoveredBySession;
function normalizeAthenaFsPath(path) {
    const normalized = path
        .replace(/\\/g, '/')
        .replace(/^\/([A-Za-z]:)/, '$1')
        .replace(/\/+$/, '');
    return normalized.replace(/^([A-Z]):/, (_, drive) => `${drive.toLowerCase()}:`);
}
function athenaFileUriToFsPath(documentUri) {
    try {
        const url = new URL(documentUri);
        if (url.protocol !== 'file:') {
            return undefined;
        }
        return normalizeAthenaFsPath(decodeURIComponent(url.pathname));
    }
    catch {
        return undefined;
    }
}
function isAthenaDocumentCoveredBySession(state, documentUri) {
    if (state.lifecycle !== 'ready') {
        return false;
    }
    const documentPath = athenaFileUriToFsPath(documentUri);
    if (!documentPath) {
        return false;
    }
    const candidateRoots = [state.sourceRootPath, state.repositoryRoot]
        .filter((value) => typeof value === 'string' && value.trim().length > 0)
        .map(normalizeAthenaFsPath);
    return candidateRoots.some(root => documentPath === root || documentPath.startsWith(`${root}/`));
}
//# sourceMappingURL=athena-repository-session-model.js.map