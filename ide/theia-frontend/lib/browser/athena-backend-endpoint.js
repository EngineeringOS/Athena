"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.toAthenaBackendUrl = toAthenaBackendUrl;
const endpoint_1 = require("@theia/core/lib/browser/endpoint");
function toAthenaBackendUrl(path, query, location = self.location) {
    const endpoint = new endpoint_1.Endpoint({ path }, location);
    const url = new URL(endpoint.getRestUrl().toString(true));
    Object.entries(query ?? {}).forEach(([key, value]) => {
        if (value !== undefined) {
            url.searchParams.set(key, String(value));
        }
    });
    return url.toString();
}
//# sourceMappingURL=athena-backend-endpoint.js.map