export type AthenaRepositorySessionCoverageState = {
    lifecycle: 'idle' | 'activating' | 'ready' | 'unavailable';
    repositoryRoot?: string;
    sourceRootPath?: string;
};
export declare function normalizeAthenaFsPath(path: string): string;
export declare function athenaFileUriToFsPath(documentUri: string): string | undefined;
export declare function isAthenaDocumentCoveredBySession(state: AthenaRepositorySessionCoverageState, documentUri: string): boolean;
//# sourceMappingURL=athena-repository-session-model.d.ts.map