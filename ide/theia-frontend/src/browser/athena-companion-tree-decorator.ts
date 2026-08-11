import { Tree, TreeDecorator, TreeDecoration, TreeNode } from '@theia/core/lib/browser';
import { Emitter } from '@theia/core/lib/common';
import { injectable } from '@theia/core/shared/inversify';
import { FileStatNode } from '@theia/filesystem/lib/browser';

/** Marks source/form pairs in navigator while preserving normal file nodes and open behavior. */
@injectable()
export class AthenaCompanionTreeDecorator implements TreeDecorator {
    readonly id = 'athena.companion-tree-decorator';

    protected readonly changeEmitter = new Emitter<(tree: Tree) => Map<string, TreeDecoration.Data>>();
    readonly onDidChangeDecorations = this.changeEmitter.event;

    decorations(tree: Tree): Map<string, TreeDecoration.Data> {
        const result = new Map<string, TreeDecoration.Data>();
        const nodes = this.collect(tree.root);
        const paths = new Set(nodes.map(node => FileStatNode.getUri(node)?.toLowerCase()).filter(Boolean));
        for (const node of nodes) {
            const uri = FileStatNode.getUri(node);
            if (!uri) continue;
            const lower = uri.toLowerCase();
            if (lower.endsWith('.sheet.athena')) {
                result.set(node.id, {
                    priority: 10,
                    captionSuffixes: [{ data: '  [designer]' }],
                    tooltip: 'Sheet Companion designer file'
                });
            } else if (lower.endsWith('.athena')) {
                const companion = `${lower.slice(0, -'.athena'.length)}.sheet.athena`;
                if (paths.has(companion)) {
                    result.set(node.id, {
                        priority: 10,
                        captionSuffixes: [{ data: '  [source + sheet]' }],
                        tooltip: 'Athena source with colocated Sheet Companion'
                    });
                }
            }
        }
        return result;
    }

    protected collect(node: TreeNode | undefined): FileStatNode[] {
        if (!node) return [];
        const children = 'children' in node ? (node as { children?: TreeNode[] }).children ?? [] : [];
        return [
            ...(FileStatNode.is(node) ? [node] : []),
            ...children.flatMap(child => this.collect(child)),
        ];
    }
}
