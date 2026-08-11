const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { app } = require('electron');
const { AthenaJvmRuntimeResolver } = require('@engineeringood/athena-theia-backend/lib/node/athena-jvm-runtime-resolver.js');

const workspaceRoot = path.resolve(process.argv[2]);
const sourcePath = path.resolve(process.argv[3]);
const screenshotPath = path.resolve(process.env.ATHENA_M45_SCREENSHOT);
const width = Number(process.env.ATHENA_M45_WIDTH || 1280);
const height = Number(process.env.ATHENA_M45_HEIGHT || 900);
const timeoutMs = 180000;
new AthenaJvmRuntimeResolver().configureProcessEnvironment(process.env, process.platform);
if (process.env.ATHENA_M45_TEMP_USER_DATA === '1') app.setPath('userData', path.join(os.tmpdir(), `athena-m45-proof-${process.pid}`));

app.on('browser-window-created', (_event, window) => {
    window.setSize(width, height);
    window.webContents.once('did-finish-load', async () => {
        try {
            const proof = await waitForReady(window);
            const image = await window.webContents.capturePage();
            fs.mkdirSync(path.dirname(screenshotPath), { recursive: true });
            fs.writeFileSync(screenshotPath, image.toPNG());
            if (image.isEmpty() || fs.statSync(screenshotPath).size < 10000) throw new Error('M45 screenshot is empty.');
            console.log(`ATHENA_M45_PROOF=${JSON.stringify({ ...proof, screenshotPath })}`);
            app.exit(0);
        } catch (error) {
            const failurePath = screenshotPath.replace(/\.png$/i, '.failure.png');
            let captureResult = `Failure screenshot: ${failurePath}`;
            try {
                const failureImage = await window.webContents.capturePage();
                fs.mkdirSync(path.dirname(failurePath), { recursive: true });
                fs.writeFileSync(failurePath, failureImage.toPNG());
            } catch (captureError) {
                captureResult = `Failure screenshot unavailable: ${captureError.stack || String(captureError)}`;
            }
            console.error(`Athena M45 product proof failed: ${error.stack || String(error)}\n${captureResult}`);
            app.exit(1);
        }
    });
});
require('../lib/backend/electron-main.js');

async function waitForReady(window) {
    const start = Date.now();
    let lastObserved;
    while (Date.now() - start < timeoutMs) {
        const prep = await window.webContents.executeJavaScript(`(${prepareWorkbench.toString()})(${JSON.stringify(workspaceRoot)},${JSON.stringify(sourcePath)})`, true);
        if (prep?.error) throw new Error(`Workspace activation failed: ${prep.error}`);
        if (!prep?.ready) { await new Promise(resolve => setTimeout(resolve, 500)); continue; }
        const proof = await window.webContents.executeJavaScript(`(async()=>{try{
            const a=await window.__athenaWorkbenchAutomation?.getState?.();
            const shell=document.querySelector('.athena-presentation__canvas-shell[data-publication-state="READY"]');
            const host=shell?.querySelector('.athena-presentation__canvas-host');
            const assetError=host?.dataset.assetError||'';
            const expectedAssetCount=Number(host?.dataset.expectedAssetCount||0);
            const loadedAssetCount=Number(host?.dataset.loadedAssetCount||0);
            const assetsReady=expectedAssetCount>0&&loadedAssetCount===expectedAssetCount;
            const canvases=[...document.querySelectorAll('.athena-presentation__canvas-host canvas')];
            const hostBounds=host?.getBoundingClientRect();
            const healthyCenter = hostBounds && !(hostBounds.height < window.innerHeight * 0.7);
            let nonWhite=0;
            const painted = canvases.some(canvas => {
                const context=canvas.getContext('2d',{willReadFrequently:true});
                if(!context)return false;
                const sample=context.getImageData(0,0,canvas.width,canvas.height).data;
                for(let index=0;index<sample.length;index+=16) {
                    if(sample[index + 3] >= 250 && (sample[index]<245||sample[index+1]<245||sample[index+2]<245)){ nonWhite++; return true; }
                }
                return false;
            });
            const contentCanvas = canvases[1];
            const stageContent = host?.querySelector('.konvajs-content');
            let contentPoint;
            if(contentCanvas&&stageContent&&assetsReady) {
                const context=contentCanvas.getContext('2d',{willReadFrequently:true});
                const pixels=context?.getImageData(0,0,contentCanvas.width,contentCanvas.height).data;
                if(pixels) {
                    const rect=contentCanvas.getBoundingClientRect();
                    const paintKey=(shell?.dataset.sceneDigest||'')+':'+contentCanvas.width+'x'+contentCanvas.height;
                    if(window.__athenaM45PaintCandidateKey!==paintKey) {
                        const paintCandidates=[];
                        const bucketSize=Math.max(16,Math.round(24*contentCanvas.width/Math.max(1,rect.width)));
                        for(let bucketY=0;bucketY<contentCanvas.height;bucketY+=bucketSize) {
                            for(let bucketX=0;bucketX<contentCanvas.width;bucketX+=bucketSize) {
                                let candidate;
                                for(let y=bucketY+2;y<Math.min(contentCanvas.height,bucketY+bucketSize)&&!candidate;y+=4) {
                                    for(let x=bucketX+2;x<Math.min(contentCanvas.width,bucketX+bucketSize);x+=4) {
                                        const index=(y*contentCanvas.width+x)*4;
                                        const alpha=pixels[index+3];
                                        if(alpha>=250&&(pixels[index]<245||pixels[index+1]<245||pixels[index+2]<245)) {
                                            candidate={x,y};
                                            break;
                                        }
                                    }
                                }
                                if(candidate) paintCandidates.push(candidate);
                            }
                        }
                        window.__athenaM45PaintCandidateKey=paintKey;
                        window.__athenaM45PaintCandidates=paintCandidates;
                        window.__athenaM45PaintCandidateIndex=0;
                    }
                    const paintCandidates=window.__athenaM45PaintCandidates||[];
                    const paintCandidateIndex=Number(window.__athenaM45PaintCandidateIndex||0);
                    const candidate = paintCandidates[paintCandidateIndex];
                    if(candidate&&!host?.dataset.selectedTraceId) {
                        window.__athenaM45PaintCandidateIndex=paintCandidateIndex+1;
                        contentPoint=candidate;
                        const clientX=rect.left+candidate.x*rect.width/contentCanvas.width;
                        const clientY=rect.top+candidate.y*rect.height/contentCanvas.height;
                        stageContent.dispatchEvent(new PointerEvent('pointerdown',{bubbles:true,clientX,clientY,pointerId:1,pointerType:'mouse',isPrimary:true,button:0,buttons:1}));
                        stageContent.dispatchEvent(new PointerEvent('pointerup',{bubbles:true,clientX,clientY,pointerId:1,pointerType:'mouse',isPrimary:true,button:0,buttons:0}));
                        stageContent.dispatchEvent(new MouseEvent('click',{bubbles:true,clientX,clientY,button:0,buttons:0}));
                    }
                }
            }
            document.querySelectorAll('.theia-notification-toasts .codicon-close').forEach(button=>button.click());
            const currentSelectionProof={sourceEditorCount:document.querySelectorAll('.monaco-editor').length,sourceFile:a?.sourceFile||'',contentPoint,selectedKind:host?.dataset.selectedKind||'',selectedTraceId:host?.dataset.selectedTraceId||'',sourceDecorationCount:document.querySelectorAll('.athena-source-selection-decoration').length};
            if(currentSelectionProof.sourceEditorCount>0&&currentSelectionProof.selectedKind==='occurrence'&&currentSelectionProof.selectedTraceId.startsWith('trace:sha256:')&&currentSelectionProof.sourceDecorationCount>0) window.__athenaM45SourceTraceProof=currentSelectionProof;
            const selectedProof=window.__athenaM45SourceTraceProof||currentSelectionProof;
            if(window.__athenaM45SourceTraceProof&&!healthyCenter) await window.__athenaWorkbenchAutomation?.revealEngineeringDocument?.();
            const notificationCount=document.querySelectorAll('.theia-notification-toasts.open .theia-notification-list-item-container').length;
            const observed={shell:!!shell,host:!!host,healthyCenter:!!healthyCenter,painted,nonWhite,assetError,assetsReady,expectedAssetCount,loadedAssetCount,innerHeight:window.innerHeight,hostHeight:hostBounds?.height||0,canvasCount:canvases.length,notificationCount};
            return {ready:!!a?.workspaceOpened&&a?.repositoryLifecycle==='ready'&&!!shell&&!!host&&!assetError&&assetsReady&&!!healthyCenter&&painted&&nonWhite>0,workspaceOpened:!!a?.workspaceOpened,workspaceRoots:a?.workspaceRoots||[],repositoryLifecycle:a?.repositoryLifecycle||'',repositoryRoot:a?.repositoryRoot||'',lspRepositoryRoot:a?.lspRepositoryRoot||'',publicationState:shell?.dataset.publicationState||'',sceneId:shell?.dataset.sceneId||'',sceneDigest:shell?.dataset.sceneDigest||'',gridColumns:Number(shell?.dataset.frameColumns||0),gridRows:Number(shell?.dataset.frameRows||0),occurrenceCount:Number(shell?.dataset.occurrenceCount||0),routeCount:Number(shell?.dataset.routeCount||0),canvas:{width:host?.clientWidth||0,height:host?.clientHeight||0,nonWhiteSamples:nonWhite},selectedProof,observed};
        }catch(error){return {error:String(error?.stack||error)}}})()`, true);
        if (proof?.error) throw new Error(`Browser proof failed: ${proof.error}`);
        if (proof?.observed?.assetError) throw new Error(`Representation asset paint failed: ${proof.observed.assetError}`);
        lastObserved = proof;
        const selectedProof = proof?.selectedProof;
        const expectedRoot = normalizePath(workspaceRoot);
        const actualWorkspaceRoots = (proof?.workspaceRoots || []).map(normalizePath);
        if (proof?.ready &&
            proof.publicationState === 'READY' &&
            actualWorkspaceRoots.length === 1 && actualWorkspaceRoots[0] === expectedRoot &&
            normalizePath(proof.repositoryRoot) === expectedRoot &&
            normalizePath(proof.lspRepositoryRoot) === expectedRoot &&
            selectedProof && selectedProof.sourceEditorCount > 0 &&
            normalizePath(selectedProof.sourceFile) === normalizePath(sourcePath) &&
            selectedProof.selectedKind === 'occurrence' && selectedProof.selectedTraceId.startsWith('trace:sha256:') &&
            selectedProof.sourceDecorationCount > 0 && proof.observed.notificationCount === 0 &&
            proof.gridColumns === 17 && proof.gridRows === 16 &&
            proof.occurrenceCount === 13 && proof.routeCount === 10) return proof;
        await new Promise(resolve => setTimeout(resolve, 500));
    }
    throw new Error(`Timed out waiting for READY M45 Engineering Document. Last observed: ${JSON.stringify(lastObserved)}`);
}

function normalizePath(value) {
    const normalized = path.resolve(String(value || '')).replaceAll('\\', '/').replace(/\/$/, '');
    return process.platform === 'win32' ? normalized.toLowerCase() : normalized;
}

async function prepareWorkbench(workspaceRoot, sourceFile) {
    function resolveTheiaService() {
        return window.__athenaWorkbenchAutomation;
    }
    if (!window.theia?.container) return { ready: false };
    try {
        const automation = resolveTheiaService();
        if (!automation) return { ready: false };
        await automation.openWorkspaceAndSource(workspaceRoot, sourceFile);
        return { ready: true };
    } catch (error) {
        return { error: String(error?.stack || error) };
    }
}
