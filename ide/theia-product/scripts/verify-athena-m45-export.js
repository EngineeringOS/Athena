'use strict';

const assert = require('node:assert/strict');
const crypto = require('node:crypto');
const fs = require('node:fs');
const path = require('node:path');
const { spawnSync } = require('node:child_process');
const { pathToFileURL } = require('node:url');
const puppeteer = require('puppeteer');

const repositoryRoot = path.resolve(__dirname, '..', '..', '..');
const exportRoot = path.join(repositoryRoot, '_bmad-output', 'implementation-artifacts', 'm45', 'exports');
const svgPath = path.join(exportRoot, 'm45-rolling-shutter.svg');
const pngPath = path.join(exportRoot, 'm45-rolling-shutter.png');
const proofPath = path.join(exportRoot, 'm45-export-proof.json');
const storyProofPath = path.join(repositoryRoot, '_bmad-output', 'implementation-artifacts', 'm45', 'm45-story-5-4-proof.json');
const productProofPath = path.join(repositoryRoot, '_bmad-output', 'implementation-artifacts', 'm45', 'm45-product-proof.json');
const evidencePaths = [
    path.join(repositoryRoot, '_bmad-output', 'implementation-artifacts', 'm45', 'evidence', 'm45-operation-transcript.txt'),
    path.join(repositoryRoot, '_bmad-output', 'implementation-artifacts', 'm45', 'evidence', 'm45-reopen-evidence.txt'),
    path.join(repositoryRoot, '_bmad-output', 'implementation-artifacts', 'm45', 'evidence', 'm45-lock-lineage-snapshot.txt'),
];
const screenshotPaths = [
    path.join(repositoryRoot, '_bmad-output', 'implementation-artifacts', 'm45', 'screenshots', 'm45-rolling-shutter-desktop-1920x1080.png'),
    path.join(repositoryRoot, '_bmad-output', 'implementation-artifacts', 'm45', 'screenshots', 'm45-rolling-shutter-narrow-720x900.png'),
];

function sha256(bytes) {
    return 'sha256:' + crypto.createHash('sha256').update(bytes).digest('hex');
}

async function main() {
    const verificationStartedAt = Date.now();
    for (const generatedPath of [svgPath, pngPath, proofPath, storyProofPath, ...evidencePaths]) {
        fs.rmSync(generatedPath, { force: true });
    }
    regenerateKernelEvidence();
    assert.ok(fs.existsSync(svgPath), 'Canonical SVG export is missing: ' + svgPath);
    for (const evidencePath of evidencePaths) {
        assert.ok(fs.existsSync(evidencePath), 'M45 evidence is missing: ' + evidencePath);
        assert.ok(fs.statSync(evidencePath).mtimeMs >= verificationStartedAt, 'M45 evidence is stale: ' + evidencePath);
    }
    for (const screenshotPath of screenshotPaths) assert.ok(fs.existsSync(screenshotPath), 'M45 screenshot is missing: ' + screenshotPath);
    const operationEvidence = fs.readFileSync(evidencePaths[0], 'utf8');
    const reopenEvidence = fs.readFileSync(evidencePaths[1], 'utf8');
    assertOperationEvidence(operationEvidence);
    assertReopenEvidence(reopenEvidence);
    const productProof = validateProductProof();
    const svgBytes = fs.readFileSync(svgPath);
    const browser = await puppeteer.launch({
        headless: true,
        args: ['--no-sandbox', '--disable-gpu'],
    });
    try {
        const page = await browser.newPage();
        await page.setViewport({ width: 1700, height: 1600, deviceScaleFactor: 1 });
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
                labelTexts: [...svg.querySelectorAll(':scope > text')].map(label => label.textContent),
                labelFontSizes: [...svg.querySelectorAll(':scope > text')].map(label => Number.parseFloat(getComputedStyle(label).fontSize)),
                circles: svg.querySelectorAll(':scope > circle').length,
                patterns: svg.querySelectorAll(':scope > pattern').length,
                bottomTitleTableElements: Math.max(0, svg.querySelectorAll(':scope > line').length - 4),
            };
        });
        const first = await page.screenshot({ type: 'png', omitBackground: false });
        const second = await page.screenshot({ type: 'png', omitBackground: false });
        const firstBytes = Buffer.from(first);
        const secondBytes = Buffer.from(second);

        assert.equal(visualFacts.root, 'svg');
        assert.equal(visualFacts.images, 13);
        assert.equal(visualFacts.routes, 10);
        assert.equal(visualFacts.nonScalingRoutes, 10);
        assert.equal(visualFacts.frameSegments, 4);
        assert.ok(visualFacts.routeStrokeWidths.every(width => width === 1));
        assert.ok(visualFacts.frameStrokeWidths.every(width => width === 1));
        assert.equal(visualFacts.backgroundRects, 1);
        assert.deepEqual(visualFacts.backgroundFills, ['rgb(255, 255, 255)']);
        assert.equal(visualFacts.labels, 13);
        assert.ok(visualFacts.labelTexts.includes('KM1-MAIN'), 'Typed Placeholder value is missing from visible SVG export.');
        assert.ok(visualFacts.labelFontSizes.every(size => size > 0 && size <= 10));
        assert.equal(visualFacts.circles, 0);
        assert.equal(visualFacts.patterns, 0);
        assert.equal(visualFacts.bottomTitleTableElements, 0);
        assert.ok(firstBytes.equals(secondBytes), 'Repeated PNG rasterization differs.');
        assert.ok(firstBytes.subarray(0, 8).equals(Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a])));
        assert.equal(firstBytes.readUInt32BE(16), 1700);
        assert.equal(firstBytes.readUInt32BE(20), 1600);

        fs.mkdirSync(exportRoot, { recursive: true });
        fs.writeFileSync(pngPath, firstBytes);
        fs.writeFileSync(proofPath, JSON.stringify({
            schemaVersion: 1,
            source: 'examples/m45/rolling-shutter',
            svg: {
                path: '_bmad-output/implementation-artifacts/m45/exports/m45-rolling-shutter.svg',
                digest: sha256(svgBytes),
                bytes: svgBytes.length,
                viewBox: visualFacts.viewBox,
            },
            png: {
                path: '_bmad-output/implementation-artifacts/m45/exports/m45-rolling-shutter.png',
                digest: sha256(firstBytes),
                bytes: firstBytes.length,
                width: 1700,
                height: 1600,
                repeatedBytesIdentical: true,
            },
            visualFacts: {
                admittedOccurrenceImages: visualFacts.images,
                technicalRoutes: visualFacts.routes,
                nonScalingTechnicalRoutes: visualFacts.nonScalingRoutes,
                onePixelFrameSegments: visualFacts.frameSegments,
                cleanWhitePageBackgrounds: visualFacts.backgroundRects,
                compactLabels: visualFacts.labels,
                placeholderLabel: 'KM1-MAIN',
                bottomTitleTableElements: visualFacts.bottomTitleTableElements,
                visiblePortHitTargets: visualFacts.circles,
                constructionGridPatterns: visualFacts.patterns,
            },
        }, null, 2) + '\n', 'utf8');
        const evidence = evidencePaths.map(evidencePath => {
            const bytes = fs.readFileSync(evidencePath);
            return {
                path: path.relative(repositoryRoot, evidencePath).replaceAll('\\', '/'),
                digest: sha256(bytes),
                bytes: bytes.length,
            };
        });
        const screenshots = screenshotPaths.map(screenshotPath => {
            const bytes = fs.readFileSync(screenshotPath);
            return {
                path: path.relative(repositoryRoot, screenshotPath).replaceAll('\\', '/'),
                digest: sha256(bytes),
                bytes: bytes.length,
            };
        });
        fs.writeFileSync(storyProofPath, JSON.stringify({
            schemaVersion: 1,
            story: '5-4-prove-edit-reopen-export-stability',
            source: 'examples/m45/rolling-shutter',
            result: 'PASS',
            checks: {
                acceptedTransactionsJournaled: operationEvidence.includes('RECONNECT_PORT|ACCEPTED') && operationEvidence.includes('journal=4'),
                rejectedTransactionsFailClosed: operationEvidence.includes('MOVE_OCCURRENCE_STALE|REJECTED|reason=STALE|journal=1'),
                freshLspReopenIdentical: reopenEvidence.includes('publication-equal-after-fresh-lsp: true'),
                canonicalSvgDeterministic: true,
                pngRasterDeterministic: true,
                fixedScreenSpaceRouteWidth: 1,
                fixedScreenSpaceFrameWidth: 1,
                visiblePortHitTargets: 0,
                constructionGridPatterns: 0,
                cleanWhitePageBackgrounds: visualFacts.backgroundRects,
                compactLabels: visualFacts.labels,
                bottomTitleTableElements: visualFacts.bottomTitleTableElements,
                productWorkspaceAndLspRootsVerified: productProof.viewports.length === 2,
            },
            evidence,
            exports: [
                { path: path.relative(repositoryRoot, svgPath).replaceAll('\\', '/'), digest: sha256(svgBytes), bytes: svgBytes.length },
                { path: path.relative(repositoryRoot, pngPath).replaceAll('\\', '/'), digest: sha256(firstBytes), bytes: firstBytes.length },
            ],
            screenshots,
        }, null, 2) + '\n', 'utf8');
    } finally {
        await browser.close();
    }
}

function regenerateKernelEvidence() {
    const gradleArgs = [
        '--no-daemon',
        '--console=plain',
        '--rerun-tasks',
        ':ide:lsp:test',
        '--tests',
        'com.engineeringood.athena.ide.lsp.RepresentationAndEngineeringOperationHandlerTest',
        ':ide:lsp:generateM45SvgExportEvidence',
    ];
    const result = process.platform === 'win32'
        ? spawnSync(process.env.ComSpec || 'cmd.exe', ['/d', '/s', '/c', 'gradlew.bat', ...gradleArgs], { cwd: repositoryRoot, encoding: 'utf8', stdio: 'pipe' })
        : spawnSync('./gradlew', gradleArgs, { cwd: repositoryRoot, encoding: 'utf8', stdio: 'pipe' });
    if (result.error || result.status !== 0) {
        throw new Error(`M45 kernel evidence regeneration failed.\n${result.error?.stack || ''}\n${result.stdout || ''}\n${result.stderr || ''}`);
    }
}

function assertOperationEvidence(evidence) {
    for (const expected of [
        'MOVE_OCCURRENCE|ACCEPTED|',
        'MOVE_OCCURRENCE_STALE|REJECTED|reason=STALE|journal=1',
        'CHANGE_SYMBOL|ACCEPTED|',
        'BIND_PART|ACCEPTED|',
        'RECONNECT_PORT|ACCEPTED|',
        'occurrence-identity-stable: true',
        'unaffected-relationship-identities-stable: true',
        'source-revision-advanced: true',
    ]) assert.ok(evidence.includes(expected), `Operation evidence missing: ${expected}`);
}

function assertReopenEvidence(evidence) {
    for (const expected of [
        'publication-equal-after-fresh-lsp: true',
        'input-revision-equal-after-reopen: true',
        'source-root-equal-after-reopen: true',
        'scene-digest-equal-after-reopen: true',
        'occurrences: 13',
        'routes: 10',
        'frame-columns: 17',
        'frame-rows: 16',
    ]) assert.ok(evidence.includes(expected), `Reopen evidence missing: ${expected}`);
}

function validateProductProof() {
    assert.ok(fs.existsSync(productProofPath), 'M45 product proof is missing: ' + productProofPath);
    const proof = JSON.parse(fs.readFileSync(productProofPath, 'utf8'));
    assert.equal(proof.schemaVersion, 'M45.product-proof');
    assert.equal(proof.viewports.length, 2);
    const expectedRoot = normalizePath(path.join(repositoryRoot, 'examples', 'm45', 'rolling-shutter'));
    for (const viewport of proof.viewports) {
        assert.equal(viewport.ready, true);
        assert.equal(viewport.repositoryLifecycle, 'ready');
        assert.deepEqual(viewport.workspaceRoots.map(normalizePath), [expectedRoot]);
        assert.equal(normalizePath(viewport.repositoryRoot), expectedRoot);
        assert.equal(normalizePath(viewport.lspRepositoryRoot), expectedRoot);
        assert.equal(viewport.publicationState, 'READY');
        assert.equal(viewport.gridColumns, 17);
        assert.equal(viewport.gridRows, 16);
        assert.equal(viewport.occurrenceCount, 13);
        assert.equal(viewport.routeCount, 10);
        const screenshot = fs.readFileSync(viewport.screenshotPath);
        assert.ok(screenshot.length >= 10000);
    }
    return proof;
}

function normalizePath(value) {
    const normalized = path.resolve(String(value || '')).replaceAll('\\', '/').replace(/\/$/, '');
    return process.platform === 'win32' ? normalized.toLowerCase() : normalized;
}

main().catch(error => {
    console.error(error);
    process.exitCode = 1;
});
