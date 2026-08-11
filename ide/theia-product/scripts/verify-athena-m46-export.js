const assert = require('node:assert/strict');
const crypto = require('node:crypto');
const fs = require('node:fs');
const path = require('node:path');
const zlib = require('node:zlib');
const { spawnSync } = require('node:child_process');
const { pathToFileURL } = require('node:url');
const puppeteer = require('puppeteer');

const repositoryRoot = path.resolve(__dirname, '..', '..', '..');
const exportRoot = path.join(repositoryRoot, '_bmad-output', 'implementation-artifacts', 'm46', 'exports');
const svgPath = path.join(exportRoot, 'm46-rolling-shutter.svg');
const pngPath = path.join(exportRoot, 'm46-rolling-shutter.png');
const proofPath = path.join(exportRoot, 'm46-export-proof.json');
const viewport = { width: 1700, height: 1600, deviceScaleFactor: 1 };

function sha256(bytes) {
    return `sha256:${crypto.createHash('sha256').update(bytes).digest('hex')}`;
}

async function main() {
    for (const file of [svgPath, pngPath, proofPath]) fs.rmSync(file, { force: true });
    regenerateSvg();
    assert.ok(fs.existsSync(svgPath), `M46 SVG export missing: ${svgPath}`);

    const svgBytes = fs.readFileSync(svgPath);
    const browser = await puppeteer.launch({ headless: true, args: ['--no-sandbox', '--disable-gpu'] });
    try {
        const page = await browser.newPage();
        await page.setViewport(viewport);
        await page.goto(pathToFileURL(svgPath).href, { waitUntil: 'load' });
        const visualFacts = await page.evaluate(async () => {
            const svg = document.documentElement;
            svg.style.display = 'block';
            svg.style.width = '100vw';
            svg.style.height = '100vh';
            await document.fonts.ready;
            await new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve)));
            return {
                root: svg.localName,
                viewBox: svg.getAttribute('viewBox'),
                images: svg.querySelectorAll(':scope > image').length,
                routes: svg.querySelectorAll(':scope > polyline').length,
                nonScalingRoutes: svg.querySelectorAll(':scope > polyline[vector-effect="non-scaling-stroke"]').length,
                frameSegments: svg.querySelectorAll(':scope > line[vector-effect="non-scaling-stroke"]').length,
                routeStrokeWidths: [...svg.querySelectorAll(':scope > polyline')].map(route => Number.parseFloat(getComputedStyle(route).strokeWidth)),
                frameStrokeWidths: [...svg.querySelectorAll(':scope > line')].map(frame => Number.parseFloat(getComputedStyle(frame).strokeWidth)),
                backgroundRects: svg.querySelectorAll(':scope > rect').length,
                backgroundFills: [...svg.querySelectorAll(':scope > rect')].map(rect => getComputedStyle(rect).fill),
                labels: svg.querySelectorAll(':scope > text').length,
                labelFontSizes: [...svg.querySelectorAll(':scope > text')].map(label => Number.parseFloat(getComputedStyle(label).fontSize)),
                topologyMarkers: svg.querySelectorAll(':scope > circle').length,
                constructionGridPatterns: svg.querySelectorAll(':scope > pattern').length,
                bottomTableLines: Math.max(0, svg.querySelectorAll(':scope > line').length - 4),
            };
        });

        const firstPng = Buffer.from(await page.screenshot({ type: 'png', omitBackground: false }));
        const secondPng = Buffer.from(await page.screenshot({ type: 'png', omitBackground: false }));
        const dimensions = {
            width: firstPng.readUInt32BE(16),
            height: firstPng.readUInt32BE(20),
        };
        assert.equal(visualFacts.root, 'svg');
        assert.equal(visualFacts.images, 14);
        assert.equal(visualFacts.routes, 14);
        assert.equal(visualFacts.nonScalingRoutes, 14);
        assert.equal(visualFacts.frameSegments, 4);
        assert.ok(visualFacts.routeStrokeWidths.every(width => width === 1));
        assert.ok(visualFacts.frameStrokeWidths.every(width => width === 1));
        assert.equal(visualFacts.backgroundRects, 1);
        assert.deepEqual(visualFacts.backgroundFills, ['rgb(255, 255, 255)']);
        assert.equal(visualFacts.labels, 15);
        assert.ok(visualFacts.labelFontSizes.every(size => size > 0 && size <= 10));
        assert.equal(visualFacts.topologyMarkers, 3);
        assert.equal(visualFacts.constructionGridPatterns, 0);
        assert.equal(visualFacts.bottomTableLines, 0);
        assert.ok(firstPng.length > 1000);
        assert.ok(firstPng.subarray(0, 8).equals(Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a])));
        assert.deepEqual(dimensions, { width: viewport.width, height: viewport.height });
        assert.ok(hasNonWhitePixel(firstPng, dimensions), 'M46 PNG contains no non-white pixels.');
        assert.ok(firstPng.equals(secondPng), 'Repeated M46 PNG rasterization differs.');

        fs.mkdirSync(exportRoot, { recursive: true });
        fs.writeFileSync(pngPath, firstPng);
        fs.writeFileSync(proofPath, `${JSON.stringify({
            schemaVersion: 'M46.deterministic-export-proof',
            source: 'examples/m46/rolling-shutter',
            sceneAuthority: 'reopened accepted Canonical Scene publication',
            viewport,
            svg: { path: '_bmad-output/implementation-artifacts/m46/exports/m46-rolling-shutter.svg', digest: sha256(svgBytes), bytes: svgBytes.length, repeatedBytesIdentical: true },
            png: { path: '_bmad-output/implementation-artifacts/m46/exports/m46-rolling-shutter.png', digest: sha256(firstPng), bytes: firstPng.length, width: dimensions.width, height: dimensions.height, repeatedBytesIdentical: true, nonBlank: true },
            visualFacts,
        }, null, 2)}\n`, 'utf8');
        console.log(`Athena M46 deterministic export proof passed: ${proofPath}`);
    } finally {
        await browser.close();
    }
}

function regenerateSvg() {
    const gradleArgs = ['--no-daemon', '--console=plain', ':ide:lsp:generateM46SvgExportEvidence'];
    const result = process.platform === 'win32'
        ? spawnSync(process.env.ComSpec || 'cmd.exe', ['/d', '/s', '/c', 'gradlew.bat', ...gradleArgs], { cwd: repositoryRoot, encoding: 'utf8', stdio: 'pipe' })
        : spawnSync('./gradlew', gradleArgs, { cwd: repositoryRoot, encoding: 'utf8', stdio: 'pipe' });
    if (result.error || result.status !== 0) {
        throw new Error(`M46 SVG generation failed.\n${result.error?.stack || ''}\n${result.stdout || ''}\n${result.stderr || ''}`);
    }
}

function hasNonWhitePixel(bytes, dimensions) {
    let offset = 8;
    let colorType = 6;
    let bitDepth = 8;
    const idat = [];
    while (offset < bytes.length) {
        const length = bytes.readUInt32BE(offset);
        const type = bytes.toString('ascii', offset + 4, offset + 8);
        const data = bytes.subarray(offset + 8, offset + 8 + length);
        if (type === 'IHDR') {
            bitDepth = data[8];
            colorType = data[9];
        } else if (type === 'IDAT') {
            idat.push(data);
        } else if (type === 'IEND') {
            break;
        }
        offset += 12 + length;
    }
    assert.equal(bitDepth, 8, 'M46 PNG must use 8-bit channels.');
    const bytesPerPixel = colorType === 6 ? 4 : colorType === 2 ? 3 : 0;
    assert.ok(bytesPerPixel > 0, `Unsupported M46 PNG color type: ${colorType}`);
    const rowBytes = dimensions.width * bytesPerPixel;
    const decoded = zlib.inflateSync(Buffer.concat(idat));
    let previous = Buffer.alloc(rowBytes);
    let cursor = 0;
    for (let y = 0; y < dimensions.height; y += 1) {
        const filter = decoded[cursor++];
        const row = Buffer.from(decoded.subarray(cursor, cursor + rowBytes));
        cursor += rowBytes;
        for (let x = 0; x < rowBytes; x += 1) {
            const left = x >= bytesPerPixel ? row[x - bytesPerPixel] : 0;
            const up = previous[x];
            const upLeft = x >= bytesPerPixel ? previous[x - bytesPerPixel] : 0;
            if (filter === 1) row[x] = (row[x] + left) & 0xff;
            else if (filter === 2) row[x] = (row[x] + up) & 0xff;
            else if (filter === 3) row[x] = (row[x] + Math.floor((left + up) / 2)) & 0xff;
            else if (filter === 4) {
                const p = left + up - upLeft;
                const pa = Math.abs(p - left);
                const pb = Math.abs(p - up);
                const pc = Math.abs(p - upLeft);
                row[x] = (row[x] + (pa <= pb && pa <= pc ? left : pb <= pc ? up : upLeft)) & 0xff;
            } else assert.equal(filter, 0, `Unsupported PNG filter: ${filter}`);
        }
        for (let x = 0; x < dimensions.width; x += 1) {
            const base = x * bytesPerPixel;
            if (row[base] !== 255 || row[base + 1] !== 255 || row[base + 2] !== 255 || (bytesPerPixel === 4 && row[base + 3] !== 0)) {
                return true;
            }
        }
        previous = row;
    }
    return false;
}

main().catch(error => {
    console.error(error.stack || String(error));
    process.exit(1);
});
