import { Endpoint } from '@theia/core/lib/browser/endpoint';

export type AthenaBackendLocation = Pick<Location, 'protocol' | 'host' | 'pathname' | 'search'>;

export function toAthenaBackendUrl(
    path: string,
    query?: Record<string, string | number | boolean | undefined>,
    location: AthenaBackendLocation = self.location,
): string {
    const endpoint = new Endpoint({ path }, location as Location);
    const url = new URL(endpoint.getRestUrl().toString(true));
    Object.entries(query ?? {}).forEach(([key, value]) => {
        if (value !== undefined) {
            url.searchParams.set(key, String(value));
        }
    });
    return url.toString();
}
