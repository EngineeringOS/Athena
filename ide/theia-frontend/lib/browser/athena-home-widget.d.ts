import * as React from 'react';
import { CommandService } from '@theia/core/lib/common';
import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { AthenaRepositorySessionService } from './athena-repository-session-service';
export declare class AthenaHomeWidget extends ReactWidget {
    static readonly ID = "athena.home";
    static readonly LABEL = "Athena Home";
    protected readonly commandService: CommandService;
    protected readonly repositorySessionService: AthenaRepositorySessionService;
    protected init(): void;
    protected render(): React.ReactNode;
}
//# sourceMappingURL=athena-home-widget.d.ts.map