import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';

const root = path.resolve(import.meta.dirname, '..');
const adapter = fs.readFileSync(path.join(root, 'src/browser/diagram/konva-diagram-adapter.ts'), 'utf8');
const widget = fs.readFileSync(path.join(root, 'src/browser/athena-presentation-widget.tsx'), 'utf8');
const packageJson = JSON.parse(fs.readFileSync(path.join(root, 'package.json'), 'utf8'));
const browserSources = fs.readdirSync(path.join(root, 'src/browser'), { recursive: true, withFileTypes: true })
    .filter(entry => entry.isFile() && /\.(ts|tsx)$/.test(entry.name))
    .map(entry => fs.readFileSync(path.join(entry.parentPath, entry.name), 'utf8'))
    .join('\n');

test('Konva is one exact direct adapter dependency and import boundary', () => {
    assert.equal(packageJson.dependencies.konva, '10.3.0');
    assert.equal((adapter.match(/from ['"]konva['"]/g) ?? []).length, 1);
    assert.equal((browserSources.match(/from ['"]konva['"]/g) ?? []).length, 1);
    assert.match(adapter, /new Konva\.Stage/);
    assert.match(adapter, /new Konva\.Layer/);
});

test('adapter has one canvas paint owner and no DOM SVG geometry layer', () => {
    assert.match(widget, /KonvaDiagramAdapter/);
    assert.doesNotMatch(widget, /<svg/i);
    assert.doesNotMatch(widget, /<canvas/i);
    assert.match(adapter, /ResizeObserver/);
    assert.match(adapter, /devicePixelRatio|scale/);
    assert.match(adapter, /athenaKind.*port/);
    assert.match(adapter, /traceId/);
});

test('default adapter source keeps semantic and asset authority upstream', () => {
    assert.doesNotMatch(adapter, /\.athena|ProjectionDocument|SpatialDocument|fetch\(/);
    assert.match(adapter, /bytesBase64/);
    assert.match(adapter, /URL\.createObjectURL/);
    assert.match(adapter, /URL\.revokeObjectURL/);
});

test('real symbols render from admitted asset bytes, not visible placeholder rectangles', () => {
    assert.doesNotMatch(adapter, /const occurrenceNode = new Konva\.Rect/);
    assert.match(adapter, /asset\.digest/);
    assert.match(adapter, /entry\.digest/);
    assert.match(adapter, /bundleEntryId/);
    assert.match(adapter, /image\/svg\+xml/);
    assert.match(adapter, /asset\.mediaKind/);
});

test('editor rulers never enter Konva paint', () => {
    assert.doesNotMatch(adapter, /decoration\.kind === 'COORDINATE_LABEL'/);
    assert.doesNotMatch(adapter, /decoration\.kind === 'FRAME_SEGMENT'/);
    assert.doesNotMatch(adapter, /snapGrid\.rows.*new Konva\.Line|new Konva\.Line.*snapGrid\.rows/);
    assert.match(adapter, /scene\.page\.drawingBounds/);
    assert.match(adapter, /pageLayer/);
    assert.match(adapter, /contentGroup/);
    assert.match(adapter, /interactionLayer/);
});

test('asset revision changes clear stale image nodes and object URLs', () => {
    assert.match(adapter, /this\.revokeAssets\(\)/);
    assert.match(adapter, /destroyChildren\(\)/);
    assert.match(adapter, /scene\.inputRevision/);
    assert.match(adapter, /onload/);
    assert.match(adapter, /loadedRevision/);
    assert.match(adapter, /dataset\.expectedAssetCount/);
    assert.match(adapter, /dataset\.loadedAssetCount/);
});

test('asset admission failures stay fail-closed and expose the exact paint error', () => {
    assert.match(adapter, /catch \(error\)/);
    assert.match(adapter, /dataset\.assetError/);
    assert.match(adapter, /console\.error\('Athena representation asset paint failed:', error\)/);
    assert.match(adapter, /this\.clear\(\)/);
});

test('style preview is disposable and resolved fields drive paint', () => {
    assert.match(adapter, /StylePreviewSession/);
    assert.match(adapter, /previewStyle/);
    assert.match(adapter, /discardStylePreview/);
    assert.match(adapter, /style\?\.dash/);
    assert.match(adapter, /style\?\.lineCap/);
    assert.match(adapter, /style\?\.lineJoin/);
    assert.match(adapter, /style\?\.routeMarker === 'END_ARROW'/);
    assert.match(adapter, /style\?\.portDisplay === 'HIDDEN'/);
    assert.doesNotMatch(adapter, /\.sheet\.style\.athena|requestDiagramCommand|writeFile/);
});

test('engineering linework stays thin in screen space while port hit geometry stays invisible', () => {
    assert.match(adapter, /strokeScaleEnabled:\s*false/);
    assert.match(adapter, /PORT_MARKER_RADIUS_PX/);
    assert.match(adapter, /PORT_HIT_DIAMETER_PX/);
    assert.doesNotMatch(adapter, /radius:\s*port\.hitRadius/);
    assert.match(adapter, /radius:\s*this\.screenPixelsToScene\(PORT_MARKER_RADIUS_PX\)/);
    assert.match(adapter, /hitStrokeWidth:\s*PORT_HIT_DIAMETER_PX/);
});

test('SceneConnection paint stays typed explicit and topology exact', () => {
    assert.match(adapter, /for \(const segment of connection\.segments\)/);
    assert.doesNotMatch(adapter, /connection\.segments\.flatMap/);
    assert.match(adapter, /ownsSharedSegment/);
    assert.match(adapter, /segment\.kind === 'SHARED'/);
    assert.match(adapter, /marker\.kind/);
    for (const kind of ['JUNCTION', 'CROSSING', 'INTERRUPTION_START', 'INTERRUPTION_END']) {
        assert.match(adapter, new RegExp(kind));
    }
    assert.match(adapter, /connection\.annotations/);
    assert.match(adapter, /text:\s*annotation\.value/);
    assert.doesNotMatch(adapter, /text:\s*annotation\.(semanticId|displayRole|traceId)/);
});

test('connection selection uses invisible generous hit paint and semantic trace identity', () => {
    assert.match(adapter, /kind:\s*'occurrence'\s*\|\s*'port'\s*\|\s*'connection'/);
    assert.match(adapter, /CONNECTION_HIT_WIDTH_PX/);
    assert.match(adapter, /hitStrokeWidth:\s*CONNECTION_HIT_WIDTH_PX/);
    assert.match(adapter, /setAttr\('athenaKind',\s*'connection'\)/);
    assert.match(adapter, /setAttr\('semanticId',\s*connection\.connectionId\)/);
    assert.match(adapter, /setAttr\('traceId',\s*connection\.traceId\)/);
    assert.match(adapter, /kind !== 'connection'/);
});

test('connection chunks preserve vector line weight while hit geometry stays retained', () => {
    assert.match(adapter, /private readonly routeHitGroup = new Konva\.Group\(\)/);
    assert.match(adapter, /type ConnectionPaintRecord = \{[\s\S]*group: Konva\.Group;[\s\S]*visualGroup: Konva\.Group;/);
    assert.match(adapter, /new Konva\.Group\(\{ name: `connection-vector-chunk:\$\{chunkIndex\}`, listening: false \}\)/);
    assert.match(adapter, /chunk\.add\(record\.visualGroup\)/);
    assert.match(adapter, /this\.routeHitGroup\.add\(record\.group\)/);
    assert.doesNotMatch(adapter, /chunk\.cache\(/);
    assert.doesNotMatch(adapter, /hitCanvasPixelRatio/);
});

test('connection hit geometry stays invisible while selected connections receive visible route overlay', () => {
    const paintConnection = adapter.slice(adapter.indexOf('private paintConnection('), adapter.indexOf('private ownsSharedSegment('));
    assert.match(paintConnection, /for \(const segment of connection\.segments\)/);
    assert.match(paintConnection, /stroke:\s*'rgba\(0,0,0,0\)'/);
    assert.match(paintConnection, /hitStrokeWidth:\s*CONNECTION_HIT_WIDTH_PX/);
    assert.match(adapter, /const selectedConnections = scene\.connections\.filter/);
    assert.match(adapter, /stroke: '#1967d2'/);
    assert.match(adapter, /this\.configureRouteDrag\(hitTarget, connection, \{ kind: 'SEGMENT', ordinal \}, segment\.start\)/);
    assert.match(adapter, /this\.configureRouteDrag\(bendTarget, connection, \{ kind: 'BEND', ordinal \}, bend\)/);
});

test('connection visibility tests segment intersection and removes compatibility paint aliases', () => {
    assert.match(adapter, /segmentIntersectsBounds/);
    assert.doesNotMatch(adapter, /connection\.segments\.some\(segment => this\.pointInBounds/);
    assert.doesNotMatch(adapter, /Compatibility name/);
    assert.doesNotMatch(adapter, /contentLayer = this\.contentGroup/);
});

test('internal port identity and source linkage never paint as canvas text', () => {
    assert.doesNotMatch(adapter, /text:\s*port\.portId/);
    assert.doesNotMatch(adapter, /MARKER_AND_LABEL/);
    assert.match(adapter, /hitStrokeWidth:\s*PORT_HIT_DIAMETER_PX/);
});

test('occurrence drag previews only paint and submits exactly one typed move on release', () => {
    assert.match(adapter, /previewOccurrence\(/);
    assert.match(adapter, /restoreOccurrencePreview\(/);
    assert.match(adapter, /dragstart/);
    assert.match(adapter, /dragmove/);
    assert.match(adapter, /dragend/);
    assert.match(adapter, /pointFromOccurrenceDrag\(/);
    assert.match(adapter, /event\.key === 'Escape'/);
    assert.match(adapter, /this\.onMove\?\.\(\{ occurrenceId/);
    assert.doesNotMatch(adapter, /\.sheet\.athena|writeFile|fetch\(/);
});

test('presentation widget turns one released drag into server-owned Move intent', () => {
    assert.match(widget, /onMove: move => void this\.moveOccurrence\(move\)/);
    assert.match(widget, /requestPresentationEditContext/);
    assert.match(widget, /requestedWritableFiles: context\.placementWritableFiles/);
    assert.match(widget, /kind: 'MOVE_OCCURRENCE'/);
    assert.doesNotMatch(widget, /\.sheet\.athena|writeFile|FileSystem/);
});

test('Ctrl Cmd occurrence selection is stable and paint-only', () => {
    assert.match(adapter, /event\.evt\.ctrlKey \|\| event\.evt\.metaKey/);
    assert.match(adapter, /selectedOccurrenceIds/);
    assert.match(adapter, /renderSelectionOverlay/);
    assert.match(adapter, /strokeScaleEnabled:\s*false/);
    assert.match(widget, /selectedOccurrenceIds/);
    assert.doesNotMatch(adapter, /\.athena|writeFile|fetch\(/);
});

test('Snap and explicit lock intent remain available through the Presentation operation path', () => {
    assert.match(widget, /snapSelectedOccurrence/);
    assert.match(widget, /kind:\s*'SNAP_OCCURRENCE_TO_GRID'/);
    assert.match(widget, /lockSelectedOccurrence/);
    assert.match(widget, /lockAction:\s*action/);
    assert.match(widget, /title='Snap selected occurrence'/);
    assert.match(widget, /title='Lock selected occurrence'/);
    assert.match(widget, /title='Unlock selected occurrence'/);
});
