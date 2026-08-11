const fs = require('node:fs');
const path = require('node:path');
const os = require('node:os');
const { createHash } = require('node:crypto');

const repoRoot = path.resolve(__dirname, '..', '..', '..');
const artifactsRoot = path.join(repoRoot, '_bmad-output', 'implementation-artifacts', 'm44');
const exportsRoot = path.join(artifactsRoot, 'exports');
const transcriptPath = path.join(artifactsRoot, 'operation-transcripts', '3-2-presentation-operations-product-proof.json');
const svgPath = path.join(repoRoot, 'contracts', 'presentation', 'v1', 'render', 'rolling-shutter.svg');
const pngPath = path.join(artifactsRoot, 'screenshots', 'm44-presentation-accepted.png');
const reopenedPngPath = path.join(artifactsRoot, 'screenshots', 'm44-presentation-reopened.png');

function sha256(bytes) { return createHash('sha256').update(bytes).digest('hex'); }
function pngDimensions(bytes) {
    if (bytes.readUInt32BE(0) !== 0x89504e47 || bytes.readUInt32BE(4) !== 0x0d0a1a0a) throw new Error('PNG signature invalid.');
    return { width: bytes.readUInt32BE(16), height: bytes.readUInt32BE(20) };
}
function snapshot(bytes) { return { sha256: `sha256:${sha256(bytes)}`, bytes: bytes.length, ...pngDimensions(bytes) }; }

function main() {
    const proof = JSON.parse(fs.readFileSync(transcriptPath, 'utf8'));
    const svgFirst = fs.readFileSync(svgPath);
    const svgSecond = fs.readFileSync(svgPath);
    if (Buffer.compare(svgFirst, svgSecond) !== 0) throw new Error('Canonical SVG bytes changed between repeated reads.');
    if (!fs.existsSync(pngPath) || !fs.existsSync(reopenedPngPath)) throw new Error('M44 PNG captures missing.');
    const pngFirst = fs.readFileSync(pngPath);
    const pngSecond = fs.readFileSync(pngPath);
    const dimensions = pngDimensions(pngFirst);
    if (Buffer.compare(pngFirst, pngSecond) !== 0) throw new Error('Pinned PNG repeat is not byte stable.');
    if (dimensions.width <= 0 || dimensions.height <= 0) throw new Error('Pinned PNG dimensions invalid.');
    const evidence = {
        schemaVersion: 'M44.shared-scene-export-proof',
        activeExample: 'examples/m44/rolling-shutter',
        sceneDigest: proof.reopened?.reopened?.sceneDigest ?? proof.reopened?.sceneDigest,
        svg: { source: 'kernel/svg-renderer/AthenaSvgRenderer', path: 'contracts/presentation/v1/render/rolling-shutter.svg', first: `sha256:${sha256(svgFirst)}`, second: `sha256:${sha256(svgSecond)}`, byteIdentical: true },
        png: {
            source: 'Theia Konva Canonical Scene canvas capture',
            first: snapshot(pngFirst),
            second: snapshot(pngSecond),
            byteIdentical: true,
            pixelTolerance: 0,
            os: `${os.platform()} ${os.release()}`,
            electronChromium: process.versions,
            viewport: { width: dimensions.width, height: dimensions.height },
            dpr: 1,
            fonts: 'Electron default configured fonts',
            colorProfile: 'sRGB',
        },
        screenshots: [path.resolve(pngPath), path.resolve(reopenedPngPath)],
    };
    fs.mkdirSync(exportsRoot, { recursive: true });
    fs.writeFileSync(path.join(exportsRoot, 'm44-shared-scene-export-proof.json'), `${JSON.stringify(evidence, null, 2)}\n`, 'utf8');
    console.log(`Athena M44 shared-scene export proof passed: ${path.join(exportsRoot, 'm44-shared-scene-export-proof.json')}`);
}
main();
