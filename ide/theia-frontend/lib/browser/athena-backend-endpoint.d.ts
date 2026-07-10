export type AthenaBackendLocation = Pick<Location, 'protocol' | 'host' | 'pathname' | 'search'>;
export declare function toAthenaBackendUrl(path: string, query?: Record<string, string | number | boolean | undefined>, location?: AthenaBackendLocation): string;
//# sourceMappingURL=athena-backend-endpoint.d.ts.map