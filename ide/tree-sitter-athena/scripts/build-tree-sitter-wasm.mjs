import fs from 'node:fs';
import path from 'node:path';
import { createHash } from 'node:crypto';
import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

const packageRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const repoRoot = path.resolve(packageRoot, '..', '..');
const treeSitterCli = path.join(
    packageRoot,
    'node_modules',
    'tree-sitter-cli',
    process.platform === 'win32' ? 'tree-sitter.exe' : 'tree-sitter'
);
const outputPath = path.join(packageRoot, 'tree-sitter-athena.wasm');
const stubDirectory = path.join(packageRoot, 'build', 'wasm-stubs');
const stubHeaderPath = path.join(stubDirectory, 'stdlib.h');
const zigVersion = '0.16.0';
const windowsZigArchiveName = `zig-x86_64-windows-${zigVersion}.zip`;
const windowsZigArchiveUrl = `https://ziglang.org/download/${zigVersion}/${windowsZigArchiveName}`;
const windowsZigArchiveSha256 = '68659eb5f1e4eb1437a722f1dd889c5a322c9954607f5edcf337bc3684a75a7e';

main();

function main() {
    run(treeSitterCli, ['generate'], 'Tree-sitter parser generation failed.');

    fs.mkdirSync(stubDirectory, { recursive: true });
    fs.writeFileSync(
        stubHeaderPath,
        [
            '#ifndef ATHENA_WASM_STDLIB_H_',
            '#define ATHENA_WASM_STDLIB_H_',
            '#define NULL ((void*)0)',
            'typedef __SIZE_TYPE__ size_t;',
            '#endif'
        ].join('\n'),
        'utf8'
    );

    const zig = resolveZigExecutable();
    run(
        zig,
        [
            'cc',
            '-target', 'wasm32-freestanding',
            '-Os',
            '-Wl,--export=tree_sitter_athena',
            '-o', outputPath,
            '-fPIC',
            '-shared',
            '-Wl,--no-entry',
            '-nostdlib',
            '-fno-exceptions',
            '-fvisibility=hidden',
            '-I', stubDirectory,
            '-I', path.join(packageRoot, 'src'),
            path.join(packageRoot, 'src', 'parser.c')
        ],
        'Athena Tree-sitter wasm build failed.'
    );

    console.log(`Built ${outputPath}`);
}

function resolveZigExecutable() {
    const candidates = [
        process.env.ATHENA_ZIG,
        process.env.ATHENA_ZIG_BIN,
        process.platform === 'win32'
            ? path.join(repoRoot, '.tools', 'zig', 'zig-x86_64-windows-0.16.0', 'zig.exe')
            : path.join(repoRoot, '.tools', 'zig', 'zig'),
        process.platform === 'win32' ? 'zig.exe' : 'zig'
    ].filter(Boolean);

    for (const candidate of candidates) {
        const probe = spawnSync(candidate, ['version'], {
            cwd: packageRoot,
            stdio: 'pipe',
            windowsHide: true
        });
        if (probe.status === 0) {
            return candidate;
        }
    }

    const bootstrapped = bootstrapRepoLocalZig();
    if (bootstrapped) {
        return bootstrapped;
    }

    throw new Error(
        'Could not find a working Zig executable. Set ATHENA_ZIG or ATHENA_ZIG_BIN, ' +
        'install `zig` on PATH, or place it under .tools/zig/. Windows x64 can auto-bootstrap ' +
        `the pinned Zig ${zigVersion} toolchain when network access is available.`
    );
}

function run(command, args, failureMessage) {
    const result = spawnSync(command, args, {
        cwd: packageRoot,
        stdio: 'inherit',
        windowsHide: true
    });
    if (result.status !== 0) {
        throw new Error(`${failureMessage} exitCode=${result.status ?? 'null'}`);
    }
}

function bootstrapRepoLocalZig() {
    if (process.platform !== 'win32' || process.arch !== 'x64') {
        return undefined;
    }

    const downloadsRoot = path.join(repoRoot, '.tools', 'downloads');
    const zigRoot = path.join(repoRoot, '.tools', 'zig');
    const zigArchivePath = path.join(downloadsRoot, windowsZigArchiveName);
    const zigExtractedRoot = path.join(zigRoot, `zig-x86_64-windows-${zigVersion}`);
    const zigExecutable = path.join(zigExtractedRoot, 'zig.exe');

    if (probeCommand(zigExecutable)) {
        return zigExecutable;
    }

    fs.mkdirSync(downloadsRoot, { recursive: true });
    fs.mkdirSync(zigRoot, { recursive: true });

    if (!fs.existsSync(zigArchivePath) || computeSha256(zigArchivePath) !== windowsZigArchiveSha256) {
        downloadFile(windowsZigArchiveUrl, zigArchivePath);
        const downloadedSha = computeSha256(zigArchivePath);
        if (downloadedSha !== windowsZigArchiveSha256) {
            throw new Error(
                `Downloaded Zig archive checksum mismatch. expected=${windowsZigArchiveSha256} actual=${downloadedSha}`
            );
        }
    }

    fs.rmSync(zigExtractedRoot, { recursive: true, force: true });
    run('tar', ['-xf', zigArchivePath, '-C', zigRoot], 'Failed to extract Zig bootstrap archive.');

    if (!probeCommand(zigExecutable)) {
        throw new Error(`Bootstrapped Zig executable was not found at ${zigExecutable}`);
    }

    return zigExecutable;
}

function probeCommand(command) {
    if (!command) {
        return false;
    }
    const probe = spawnSync(command, ['version'], {
        cwd: packageRoot,
        stdio: 'pipe',
        windowsHide: true
    });
    return probe.status === 0;
}

function computeSha256(filePath) {
    const hash = createHash('sha256');
    hash.update(fs.readFileSync(filePath));
    return hash.digest('hex');
}

function downloadFile(url, destinationPath) {
    const powershell = process.platform === 'win32' ? 'powershell.exe' : 'powershell';
    run(
        powershell,
        [
            '-NoProfile',
            '-ExecutionPolicy', 'Bypass',
            '-Command',
            `Invoke-WebRequest -Uri '${url}' -OutFile '${destinationPath.replace(/'/g, "''")}'`
        ],
        'Failed to download Zig bootstrap archive.'
    );
}
