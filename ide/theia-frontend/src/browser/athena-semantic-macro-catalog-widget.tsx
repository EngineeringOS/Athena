import * as React from '@theia/core/shared/react';

import { ReactWidget } from '@theia/core/lib/browser/widgets/react-widget';
import { Disposable } from '@theia/core/lib/common';
import { inject, injectable, postConstruct } from '@theia/core/shared/inversify';
import { AthenaLspEditorBridgeService } from './athena-lsp-editor-bridge-service';
import { AthenaRepositorySessionService } from './athena-repository-session-service';
import {
    AthenaSemanticMacroCatalogGroup,
    AthenaSemanticMacroCatalogItem,
    buildAthenaSemanticMacroCatalogGroups,
} from './athena-semantic-macro-model';
import type {
    AthenaSemanticMacroAcceptancePayload,
    AthenaSemanticMacroCatalogPayload,
    AthenaSemanticMacroOriginInspectionPayload,
    AthenaSemanticMacroParameterDefinitionPayload,
    AthenaSemanticMacroParameterValuePayload,
    AthenaSemanticMacroPreviewPayload,
    AthenaSemanticMacroValidationPayload,
} from './athena-semantic-macro-protocol';

@injectable()
export class AthenaSemanticMacroCatalogWidget extends ReactWidget {
    static readonly ID = 'athena.semanticMacroCatalog';
    static readonly LABEL = 'Reuse Catalog';
    static readonly DEFAULT_INSTANTIATION_ID = 'instance:M1';

    @inject(AthenaRepositorySessionService)
    protected readonly repositorySessionService: AthenaRepositorySessionService;

    @inject(AthenaLspEditorBridgeService)
    protected readonly lspEditorBridgeService: AthenaLspEditorBridgeService;

    protected catalog: AthenaSemanticMacroCatalogPayload | undefined;
    protected validation: AthenaSemanticMacroValidationPayload | undefined;
    protected preview: AthenaSemanticMacroPreviewPayload | undefined;
    protected acceptance: AthenaSemanticMacroAcceptancePayload | undefined;
    protected originInspection: AthenaSemanticMacroOriginInspectionPayload | undefined;
    protected groups: AthenaSemanticMacroCatalogGroup[] = [];
    protected selectedMacroId: string | undefined;
    protected loading = false;
    protected validationLoading = false;
    protected previewLoading = false;
    protected acceptanceLoading = false;
    protected originInspectionLoading = false;
    protected errorMessage: string | undefined;
    protected flowMessage: string | undefined;
    protected refreshHandle: number | undefined;
    protected instantiationId = AthenaSemanticMacroCatalogWidget.DEFAULT_INSTANTIATION_ID;
    protected parameterInputValues: Record<string, string> = {};

    @postConstruct()
    protected init(): void {
        this.id = AthenaSemanticMacroCatalogWidget.ID;
        this.title.label = AthenaSemanticMacroCatalogWidget.LABEL;
        this.title.caption = AthenaSemanticMacroCatalogWidget.LABEL;
        this.title.closable = true;
        this.title.iconClass = 'codicon codicon-library';
        this.addClass('athena-semantic-macro-catalog-widget');

        this.toDispose.push(this.repositorySessionService.onDidChangeState(() => this.scheduleRefresh()));
        this.toDispose.push(Disposable.create(() => {
            if (this.refreshHandle !== undefined) {
                window.clearTimeout(this.refreshHandle);
            }
        }));

        this.scheduleRefresh();
    }

    protected scheduleRefresh(): void {
        if (this.refreshHandle !== undefined) {
            window.clearTimeout(this.refreshHandle);
        }
        this.refreshHandle = window.setTimeout(() => {
            this.refreshHandle = undefined;
            void this.refreshCatalog();
        }, 120);
    }

    protected async refreshCatalog(): Promise<void> {
        const sessionState = this.repositorySessionService.state;
        if (sessionState.lifecycle !== 'ready') {
            this.resetState();
            this.update();
            return;
        }

        this.loading = true;
        this.errorMessage = undefined;
        this.update();

        try {
            const catalog = await this.lspEditorBridgeService.requestSemanticMacroCatalog();
            this.catalog = catalog;
            this.groups = buildAthenaSemanticMacroCatalogGroups(catalog?.entries ?? []);
            const availableMacroIds = new Set((catalog?.entries ?? []).map(entry => entry.macroId));
            const nextSelectedMacroId = this.selectedMacroId && availableMacroIds.has(this.selectedMacroId)
                ? this.selectedMacroId
                : catalog?.entries[0]?.macroId;
            const selectionChanged = nextSelectedMacroId !== this.selectedMacroId;
            this.selectedMacroId = nextSelectedMacroId;
            if (!this.selectedMacroId) {
                this.validation = undefined;
                this.preview = undefined;
                this.acceptance = undefined;
                this.originInspection = undefined;
                this.flowMessage = undefined;
                this.parameterInputValues = {};
            } else {
                await this.refreshValidation(selectionChanged);
            }
        } catch (error) {
            this.errorMessage = error instanceof Error ? error.message : String(error);
            this.catalog = undefined;
            this.groups = [];
            this.selectedMacroId = undefined;
            this.validation = undefined;
            this.preview = undefined;
            this.acceptance = undefined;
            this.originInspection = undefined;
            this.flowMessage = undefined;
            this.parameterInputValues = {};
        } finally {
            this.loading = false;
            this.update();
        }
    }

    protected resetState(): void {
        this.loading = false;
        this.validationLoading = false;
        this.previewLoading = false;
        this.acceptanceLoading = false;
        this.originInspectionLoading = false;
        this.errorMessage = undefined;
        this.flowMessage = undefined;
        this.catalog = undefined;
        this.validation = undefined;
        this.preview = undefined;
        this.acceptance = undefined;
        this.originInspection = undefined;
        this.groups = [];
        this.selectedMacroId = undefined;
        this.parameterInputValues = {};
        this.instantiationId = AthenaSemanticMacroCatalogWidget.DEFAULT_INSTANTIATION_ID;
    }

    protected selectedItem(): AthenaSemanticMacroCatalogItem | undefined {
        return this.groups.flatMap(group => group.items).find(item => item.macroId === this.selectedMacroId);
    }

    protected packageCount(): number {
        return new Set((this.catalog?.entries ?? []).map(entry => `${entry.packageName}@${entry.packageVersion ?? ''}`)).size;
    }

    protected selectMacro(macroId: string): void {
        if (this.selectedMacroId === macroId) {
            return;
        }
        this.selectedMacroId = macroId;
        this.validation = undefined;
        this.preview = undefined;
        this.acceptance = undefined;
        this.originInspection = undefined;
        this.flowMessage = undefined;
        this.parameterInputValues = {};
        this.instantiationId = AthenaSemanticMacroCatalogWidget.DEFAULT_INSTANTIATION_ID;
        this.update();
        void this.refreshValidation(true);
    }

    protected async validateSelectedMacro(): Promise<void> {
        const validation = await this.refreshValidation(false);
        if (!validation) {
            this.flowMessage = 'Athena could not load the governed parameter contract for the selected Semantic Macro.';
            this.preview = undefined;
            this.acceptance = undefined;
            this.update();
            return;
        }
        this.preview = undefined;
        this.acceptance = undefined;
        if (validation.status === 'valid') {
            this.flowMessage = 'Parameter validation passed. Preview is ready to assemble.';
        } else if (validation.status === 'invalid') {
            this.flowMessage = 'Parameter validation blocked preview. Resolve the reported diagnostics and try again.';
        } else {
            this.flowMessage = validation.reason ?? 'Semantic Macro validation is unavailable for the selected macro.';
        }
        this.update();
    }

    protected async previewSelectedMacro(): Promise<void> {
        const selectedItem = this.selectedItem();
        if (!selectedItem) {
            return;
        }

        const validation = await this.refreshValidation(false);
        if (!validation) {
            this.flowMessage = 'Athena could not resolve the governed parameter contract before preview.';
            this.preview = undefined;
            this.acceptance = undefined;
            this.update();
            return;
        }
        if (validation.status !== 'valid') {
            this.preview = undefined;
            this.acceptance = undefined;
            this.flowMessage = validation.status === 'invalid'
                ? 'Preview is blocked until every required governed parameter validates.'
                : validation.reason ?? 'Preview is unavailable because the governed macro contract could not be resolved.';
            this.update();
            return;
        }

        this.previewLoading = true;
        this.flowMessage = undefined;
        this.acceptance = undefined;
        this.update();
        try {
            const preview = await this.lspEditorBridgeService.requestSemanticMacroPreview({
                macroId: selectedItem.macroId,
                instantiationId: this.instantiationId.trim() || AthenaSemanticMacroCatalogWidget.DEFAULT_INSTANTIATION_ID,
                parameterValues: this.currentParameterValues(validation.parameters),
            });
            this.preview = preview;
            this.flowMessage = preview?.status === 'ready'
                ? undefined
                : preview?.reason ?? 'Athena could not assemble a governed preview for the selected Semantic Macro.';
        } catch (error) {
            this.preview = undefined;
            this.flowMessage = error instanceof Error ? error.message : String(error);
        } finally {
            this.previewLoading = false;
            this.update();
        }
    }

    protected async approvePreview(): Promise<void> {
        const preview = this.preview;
        if (!preview?.previewId) {
            return;
        }
        this.acceptanceLoading = true;
        this.flowMessage = undefined;
        this.update();
        try {
            const acceptance = await this.lspEditorBridgeService.requestSemanticMacroAcceptance({
                previewId: preview.previewId,
                macroId: preview.macroId,
                instantiationId: preview.instantiationId,
            });
            this.acceptance = acceptance;
            this.flowMessage = acceptance?.reason ?? 'Preview approval was recorded.';
            if (acceptance?.status === 'accepted') {
                const primarySubjectId = acceptance.acceptedExpansion?.memberships[0]?.subjectId;
                await this.inspectOrigin(
                    primarySubjectId
                        ? { subjectId: primarySubjectId }
                        : { instantiationId: acceptance.instantiationId },
                );
            }
        } catch (error) {
            this.acceptance = undefined;
            this.flowMessage = error instanceof Error ? error.message : String(error);
        } finally {
            this.acceptanceLoading = false;
            this.update();
        }
    }

    protected cancelPreview(): void {
        this.preview = undefined;
        this.acceptance = undefined;
        this.flowMessage = 'Preview dismissed. Canonical state remains unchanged until a governed acceptance path is completed.';
        this.update();
    }

    protected async inspectOrigin(
        params: { subjectId?: string; instantiationId?: string },
    ): Promise<AthenaSemanticMacroOriginInspectionPayload | undefined> {
        if (!params.subjectId && !params.instantiationId) {
            return undefined;
        }
        this.originInspectionLoading = true;
        this.update();
        try {
            const inspection = await this.lspEditorBridgeService.requestSemanticMacroOriginInspection(params);
            this.originInspection = inspection;
            if (inspection?.status === 'unavailable' && inspection.reason) {
                this.flowMessage = inspection.reason;
            }
            return inspection;
        } catch (error) {
            this.flowMessage = error instanceof Error ? error.message : String(error);
            return undefined;
        } finally {
            this.originInspectionLoading = false;
            this.update();
        }
    }

    protected async refreshValidation(resetInputs: boolean): Promise<AthenaSemanticMacroValidationPayload | undefined> {
        const selectedItem = this.selectedItem();
        if (!selectedItem) {
            this.validation = undefined;
            return undefined;
        }
        if (resetInputs) {
            this.parameterInputValues = {};
        }
        const macroId = selectedItem.macroId;
        const instantiationId = this.instantiationId.trim() || AthenaSemanticMacroCatalogWidget.DEFAULT_INSTANTIATION_ID;
        const knownParameters = this.validation?.macroId === macroId ? this.validation.parameters : [];
        this.validationLoading = true;
        this.update();
        try {
            const validation = await this.lspEditorBridgeService.requestSemanticMacroValidation({
                macroId,
                instantiationId,
                parameterValues: this.currentParameterValues(knownParameters),
            });
            if (this.selectedMacroId !== macroId) {
                return validation;
            }
            this.validation = validation;
            this.seedMissingParameterInputs(validation);
            return validation;
        } catch (error) {
            if (this.selectedMacroId === macroId) {
                this.validation = undefined;
                this.flowMessage = error instanceof Error ? error.message : String(error);
            }
            return undefined;
        } finally {
            if (this.selectedMacroId === macroId) {
                this.validationLoading = false;
                this.update();
            }
        }
    }

    protected seedMissingParameterInputs(validation: AthenaSemanticMacroValidationPayload | undefined): void {
        if (!validation) {
            return;
        }
        const nextInputs = { ...this.parameterInputValues };
        validation.parameters.forEach(parameter => {
            if (Object.prototype.hasOwnProperty.call(nextInputs, parameter.name)) {
                return;
            }
            const normalizedValue = validation.normalizedValues[parameter.name] ?? parameter.defaultValue;
            const inputValue = this.parameterValueToInput(normalizedValue);
            if (inputValue !== undefined) {
                nextInputs[parameter.name] = inputValue;
            }
        });
        this.parameterInputValues = nextInputs;
    }

    protected currentParameterValues(
        parameters: AthenaSemanticMacroParameterDefinitionPayload[],
    ): Record<string, AthenaSemanticMacroParameterValuePayload> {
        return Object.fromEntries(
            parameters.flatMap(parameter => {
                const value = this.parameterInputToPayload(parameter);
                return value ? [[parameter.name, value] as const] : [];
            }),
        );
    }

    protected parameterInputToPayload(
        parameter: AthenaSemanticMacroParameterDefinitionPayload,
    ): AthenaSemanticMacroParameterValuePayload | undefined {
        const rawValue = this.parameterInputValues[parameter.name];
        const normalizedValue = rawValue?.trim();
        if (!normalizedValue) {
            return undefined;
        }
        switch (parameter.valueKind) {
            case 'text':
            case 'symbol':
                return {
                    kind: parameter.valueKind,
                    text: normalizedValue,
                };
            case 'boolean':
                if (normalizedValue !== 'true' && normalizedValue !== 'false') {
                    return undefined;
                }
                return {
                    kind: 'boolean',
                    booleanValue: normalizedValue === 'true',
                };
            case 'integer': {
                const integerValue = Number.parseInt(normalizedValue, 10);
                return Number.isNaN(integerValue)
                    ? undefined
                    : {
                        kind: 'integer',
                        integerValue,
                    };
            }
            default:
                return undefined;
        }
    }

    protected parameterValueToInput(
        value: AthenaSemanticMacroParameterValuePayload | undefined,
    ): string | undefined {
        if (!value) {
            return undefined;
        }
        switch (value.kind) {
            case 'text':
            case 'symbol':
                return value.text ?? '';
            case 'boolean':
                return value.booleanValue === undefined ? '' : String(value.booleanValue);
            case 'integer':
                return value.integerValue === undefined ? '' : String(value.integerValue);
            default:
                return '';
        }
    }

    protected parameterValueDisplay(
        value: AthenaSemanticMacroParameterValuePayload | undefined,
    ): string {
        if (!value) {
            return 'unset';
        }
        switch (value.kind) {
            case 'text':
            case 'symbol':
                return value.text ?? 'unset';
            case 'boolean':
                return value.booleanValue === undefined ? 'unset' : String(value.booleanValue);
            case 'integer':
                return value.integerValue === undefined ? 'unset' : String(value.integerValue);
            default:
                return 'unset';
        }
    }

    protected parameterInputValue(parameter: AthenaSemanticMacroParameterDefinitionPayload): string {
        if (Object.prototype.hasOwnProperty.call(this.parameterInputValues, parameter.name)) {
            return this.parameterInputValues[parameter.name];
        }
        return this.parameterValueToInput(this.validation?.normalizedValues[parameter.name] ?? parameter.defaultValue) ?? '';
    }

    protected updateParameterInput(name: string, value: string): void {
        this.parameterInputValues = {
            ...this.parameterInputValues,
            [name]: value,
        };
        this.preview = undefined;
        this.acceptance = undefined;
        this.flowMessage = undefined;
        this.update();
    }

    protected render(): React.ReactNode {
        const sessionState = this.repositorySessionService.state;

        if (sessionState.lifecycle !== 'ready') {
            return <div className='athena-semantic-macro-catalog' data-testid='athena-reuse-catalog'>
                <section className='athena-semantic-macro-catalog__empty'>
                    <h2>Reuse Catalog</h2>
                    <p>{sessionState.message}</p>
                </section>
            </div>;
        }

        if (this.errorMessage) {
            return <div className='athena-semantic-macro-catalog' data-testid='athena-reuse-catalog'>
                <section className='athena-semantic-macro-catalog__empty athena-semantic-macro-catalog__empty--error'>
                    <h2>Reuse Catalog</h2>
                    <p>{this.errorMessage}</p>
                </section>
            </div>;
        }

        if (this.loading && !this.catalog) {
            return <div className='athena-semantic-macro-catalog' data-testid='athena-reuse-catalog'>
                <section className='athena-semantic-macro-catalog__empty'>
                    <h2>Reuse Catalog</h2>
                    <p>Loading governed Semantic Macros from the active Athena runtime session.</p>
                </section>
            </div>;
        }

        const catalog = this.catalog;
        if (!catalog) {
            return <div className='athena-semantic-macro-catalog' data-testid='athena-reuse-catalog'>
                <section className='athena-semantic-macro-catalog__empty'>
                    <h2>Reuse Catalog</h2>
                    <p>No runtime-owned Semantic Macro catalog is available yet for the active repository.</p>
                </section>
            </div>;
        }

        const selectedItem = this.selectedItem();
        const validation = this.validation;
        const preview = this.preview;
        const acceptance = this.acceptance;
        const originInspection = this.originInspection;

        return <div className='athena-semantic-macro-catalog' data-testid='athena-reuse-catalog'>
            <header className='athena-semantic-macro-catalog__header'>
                <div>
                    <div className='athena-semantic-macro-catalog__eyebrow'>Governed reuse catalog</div>
                    <h2>Available Semantic Macros</h2>
                    <p>{catalog.projectName} | <code>{catalog.semanticPath}</code></p>
                </div>
                <div
                    className={`athena-semantic-macro-catalog__status athena-semantic-macro-catalog__status--${catalog.status}`}
                    data-testid='athena-reuse-catalog-status'
                >
                    {catalog.status}
                </div>
            </header>

            <section className='athena-semantic-macro-catalog__summary'>
                <ul className='athena-semantic-macro-catalog__summary-list'>
                    <li><span>Macros</span><strong>{catalog.entries.length}</strong></li>
                    <li><span>Packages</span><strong>{this.packageCount()}</strong></li>
                    <li><span>Diagnostics</span><strong>{catalog.diagnostics.length}</strong></li>
                </ul>
            </section>

            <section className='athena-semantic-macro-catalog__section'>
                <h3>Scope</h3>
                <ul className='athena-semantic-macro-catalog__detail-list'>
                    <li><span>Source</span><strong>Runtime-owned repository catalog</strong></li>
                    <li><span>Reproducibility</span><strong>Locked package graph / <code>athena.lock</code></strong></li>
                    <li><span>Flow</span><strong>Parameter edit, validation, preview, approve/cancel</strong></li>
                </ul>
            </section>

            {selectedItem
                ? <section className='athena-semantic-macro-catalog__section'>
                    <h3>Selected macro</h3>
                    <div className='athena-semantic-macro-catalog__selection'>
                        <strong>{selectedItem.displayName}</strong><br />
                        <code>{selectedItem.macroId}</code>
                    </div>
                    <ul className='athena-semantic-macro-catalog__detail-list'>
                        <li><span>Package</span><strong><code>{selectedItem.packageLabel}</code></strong></li>
                        <li><span>Definition</span><strong><code>{selectedItem.definitionPath}</code></strong></li>
                        <li><span>Summary</span><strong>{selectedItem.summary}</strong></li>
                    </ul>
                    {selectedItem.classificationKeys.length > 0
                        ? <div className='athena-semantic-macro-catalog__pill-row'>
                            {selectedItem.classificationKeys.map(key => <span
                                key={`${selectedItem.macroId}:${key}`}
                                className='athena-semantic-macro-catalog__pill'
                            >
                                {key}
                            </span>)}
                        </div>
                        : undefined}
                </section>
                : undefined}

            {selectedItem
                ? this.renderWorkbenchFlow(selectedItem, validation, preview, acceptance)
                : undefined}

            {originInspection || this.originInspectionLoading
                ? <section
                    className='athena-semantic-macro-catalog__section'
                    data-testid='athena-reuse-origin-panel'
                >
                    <div className='athena-semantic-macro-catalog__section-header'>
                        <h3>Origin traceability</h3>
                        <div
                            className={`athena-semantic-macro-catalog__status athena-semantic-macro-catalog__status--${originInspection?.status ?? 'loading'}`}
                            data-testid='athena-reuse-origin-status'
                        >
                            {this.originInspectionLoading ? 'loading' : originInspection?.status ?? 'loading'}
                        </div>
                    </div>
                    {originInspection?.acceptedExpansion
                        ? <>
                            <ul className='athena-semantic-macro-catalog__detail-list'>
                                <li>
                                    <span>Expansion</span>
                                    <strong><code data-testid='athena-reuse-origin-expansion-id'>{originInspection.acceptedExpansion.expansionId}</code></strong>
                                </li>
                                <li><span>Macro</span><strong><code>{originInspection.acceptedExpansion.macroId}</code></strong></li>
                                <li><span>Instance</span><strong><code>{originInspection.instantiationId}</code></strong></li>
                                <li><span>Package</span><strong><code>{originInspection.acceptedExpansion.packageName}@{originInspection.acceptedExpansion.packageVersion ?? 'unspecified'}</code></strong></li>
                                {originInspection.commandId
                                    ? <li>
                                        <span>Command</span>
                                        <strong><code data-testid='athena-reuse-origin-command-id'>{originInspection.commandId}</code></strong>
                                    </li>
                                    : undefined}
                                {originInspection.matchedMembership
                                    ? <li>
                                        <span>Matched role</span>
                                        <strong data-testid='athena-reuse-origin-matched-role'>{originInspection.matchedMembership.role ?? originInspection.matchedMembership.subjectId}</strong>
                                    </li>
                                    : undefined}
                                <li>
                                    <span>Memberships</span>
                                    <strong data-testid='athena-reuse-origin-membership-count'>{originInspection.acceptedExpansion.memberships.length}</strong>
                                </li>
                            </ul>
                            <div className='athena-semantic-macro-catalog__actions'>
                                <button
                                    className='athena-semantic-macro-catalog__action athena-semantic-macro-catalog__action--secondary'
                                    type='button'
                                    data-testid='athena-reuse-origin-by-instantiation'
                                    disabled={this.originInspectionLoading}
                                    onClick={() => void this.inspectOrigin({
                                        instantiationId: originInspection.instantiationId,
                                    })}
                                >
                                    Inspect by instantiation
                                </button>
                            </div>
                            <div className='athena-semantic-macro-catalog__subsection'>
                                <h4>Parameter values</h4>
                                <ul className='athena-semantic-macro-catalog__dense-list'>
                                    {Object.entries(originInspection.acceptedExpansion.parameterValues).map(([name, value]) => <li key={`origin:${name}`}>
                                        <strong>{name}</strong> {this.parameterValueDisplay(value)}
                                    </li>)}
                                </ul>
                            </div>
                            <div className='athena-semantic-macro-catalog__subsection'>
                                <h4>Expansion membership</h4>
                                <ul className='athena-semantic-macro-catalog__list'>
                                    {originInspection.acceptedExpansion.memberships.map(membership => <li
                                        key={`origin-membership:${membership.subjectId}`}
                                        className='athena-semantic-macro-catalog__item'
                                    >
                                        <div className='athena-semantic-macro-catalog__item-header'>
                                            <span className='athena-semantic-macro-catalog__item-title'>
                                                <code>{membership.subjectId}</code>
                                            </span>
                                            {membership.role
                                                ? <span className='athena-semantic-macro-catalog__pill'>{membership.role}</span>
                                                : undefined}
                                        </div>
                                        <div className='athena-semantic-macro-catalog__actions'>
                                            <button
                                                className='athena-semantic-macro-catalog__action athena-semantic-macro-catalog__action--secondary'
                                                type='button'
                                                data-testid='athena-reuse-origin-membership'
                                                disabled={this.originInspectionLoading}
                                                onClick={() => void this.inspectOrigin({ subjectId: membership.subjectId })}
                                            >
                                                Inspect subject
                                            </button>
                                        </div>
                                    </li>)}
                                </ul>
                            </div>
                        </>
                        : <p>{originInspection?.reason ?? 'Accepted Semantic Macro origin traceability is not available yet.'}</p>}
                </section>
                : undefined}

            {catalog.reason
                ? <section className='athena-semantic-macro-catalog__section'>
                    <h3>Catalog status</h3>
                    <p>{catalog.reason}</p>
                </section>
                : undefined}

            {catalog.diagnostics.length > 0
                ? <section className='athena-semantic-macro-catalog__section'>
                    <h3>Diagnostics</h3>
                    <ul className='athena-semantic-macro-catalog__list'>
                        {catalog.diagnostics.map(diagnostic => <li
                            key={`${diagnostic.code}:${diagnostic.subject}:${diagnostic.message}`}
                            className='athena-semantic-macro-catalog__item athena-semantic-macro-catalog__item--diagnostic'
                        >
                            <div className='athena-semantic-macro-catalog__item-header'>
                                <span className='athena-semantic-macro-catalog__item-title'>{diagnostic.code}</span>
                                <span className='athena-semantic-macro-catalog__pill'>{diagnostic.subject}</span>
                            </div>
                            <div className='athena-semantic-macro-catalog__item-meta'>{diagnostic.message}</div>
                        </li>)}
                    </ul>
                </section>
                : undefined}

            {this.groups.length === 0
                ? <section className='athena-semantic-macro-catalog__section'>
                    <h3>Available Semantic Macros</h3>
                    <p>No active Semantic Macros are currently published from the locked package graph.</p>
                </section>
                : this.groups.map(group => <section
                    key={group.categoryId}
                    className='athena-semantic-macro-catalog__section'
                >
                    <h3>{group.label}</h3>
                    <ul className='athena-semantic-macro-catalog__list'>
                        {group.items.map(item => this.renderItem(item))}
                    </ul>
                </section>)}
        </div>;
    }

    protected renderWorkbenchFlow(
        selectedItem: AthenaSemanticMacroCatalogItem,
        validation: AthenaSemanticMacroValidationPayload | undefined,
        preview: AthenaSemanticMacroPreviewPayload | undefined,
        acceptance: AthenaSemanticMacroAcceptancePayload | undefined,
    ): React.ReactNode {
        const parameters = validation?.parameters ?? [];
        const hasPreview = preview?.status === 'ready';
        return <>
            <section className='athena-semantic-macro-catalog__section'>
                <div className='athena-semantic-macro-catalog__section-header'>
                    <h3>Reuse flow</h3>
                    <div
                        className={`athena-semantic-macro-catalog__status athena-semantic-macro-catalog__status--${validation?.status ?? 'idle'}`}
                    >
                        {this.validationLoading ? 'loading' : validation?.status ?? 'idle'}
                    </div>
                </div>
                <p>Parameter edits stay inside the shared runtime-owned reuse path. The workbench never expands this macro locally.</p>
                <div className='athena-semantic-macro-catalog__control-grid'>
                    <div className='athena-semantic-macro-catalog__control athena-semantic-macro-catalog__control--wide'>
                        <label htmlFor='athena-reuse-instantiation-id'>Instantiation ID</label>
                        <input
                            id='athena-reuse-instantiation-id'
                            type='text'
                            value={this.instantiationId}
                            data-testid='athena-reuse-instance-id'
                            onChange={event => {
                                this.instantiationId = event.target.value;
                                this.preview = undefined;
                                this.acceptance = undefined;
                                this.flowMessage = undefined;
                                this.update();
                            }}
                        />
                        <div className='athena-semantic-macro-catalog__hint'>
                            Stable instantiation ids keep preview identity deterministic across repeated review runs.
                        </div>
                    </div>
                    {this.validationLoading && !validation
                        ? <div className='athena-semantic-macro-catalog__section-note'>Loading governed parameter contract...</div>
                        : undefined}
                    {parameters.map(parameter => this.renderParameterEditor(parameter))}
                </div>
                <div className='athena-semantic-macro-catalog__actions'>
                    <button
                        className='athena-semantic-macro-catalog__action athena-semantic-macro-catalog__action--secondary'
                        type='button'
                        data-testid='athena-reuse-validate'
                        disabled={this.validationLoading || this.previewLoading || this.acceptanceLoading}
                        onClick={() => void this.validateSelectedMacro()}
                    >
                        {this.validationLoading ? 'Validating...' : 'Validate'}
                    </button>
                    <button
                        className='athena-semantic-macro-catalog__action'
                        type='button'
                        data-testid='athena-reuse-preview'
                        disabled={this.validationLoading || this.previewLoading || this.acceptanceLoading}
                        onClick={() => void this.previewSelectedMacro()}
                    >
                        {this.previewLoading ? 'Building preview...' : 'Preview reuse'}
                    </button>
                </div>
                {validation
                    ? <div className='athena-semantic-macro-catalog__subsection'>
                        <h4>Validation</h4>
                        <ul className='athena-semantic-macro-catalog__detail-list'>
                            <li><span>Macro</span><strong><code>{validation.macroId}</code></strong></li>
                            <li><span>Instance</span><strong><code>{validation.instantiationId}</code></strong></li>
                            <li><span>Parameters</span><strong>{validation.parameters.length}</strong></li>
                        </ul>
                        {validation.reason
                            ? <p>{validation.reason}</p>
                            : undefined}
                        {validation.diagnostics.length > 0
                            ? <ul className='athena-semantic-macro-catalog__list'>
                                {validation.diagnostics.map(diagnostic => <li
                                    key={`${diagnostic.code}:${diagnostic.parameterName ?? ''}:${diagnostic.message}`}
                                    className='athena-semantic-macro-catalog__item athena-semantic-macro-catalog__item--diagnostic'
                                >
                                    <div className='athena-semantic-macro-catalog__item-header'>
                                        <span className='athena-semantic-macro-catalog__item-title'>{diagnostic.code}</span>
                                        {diagnostic.parameterName
                                            ? <span className='athena-semantic-macro-catalog__pill'>{diagnostic.parameterName}</span>
                                            : undefined}
                                    </div>
                                    <div className='athena-semantic-macro-catalog__item-meta'>{diagnostic.message}</div>
                                </li>)}
                            </ul>
                            : <p>No validation diagnostics are active for this parameter set.</p>}
                    </div>
                    : undefined}
            </section>

            {(preview || acceptance || this.flowMessage)
                ? <section
                    className='athena-semantic-macro-catalog__section athena-semantic-macro-catalog__preview'
                    data-testid='athena-reuse-preview-panel'
                >
                    <div className='athena-semantic-macro-catalog__preview-header'>
                        <div>
                            <h3>Preview review</h3>
                            <p>{preview?.title ?? selectedItem.displayName}</p>
                        </div>
                        {preview
                            ? <div
                                className={`athena-semantic-macro-catalog__status athena-semantic-macro-catalog__status--${preview.status}`}
                                data-testid='athena-reuse-preview-status'
                            >
                                {preview.status}
                            </div>
                            : undefined}
                    </div>
                    {this.flowMessage
                        ? <p data-testid='athena-reuse-flow-message'>{this.flowMessage}</p>
                        : undefined}
                    {preview
                        ? <>
                            <ul className='athena-semantic-macro-catalog__summary-list'>
                                <li><span>Changes</span><strong>{preview.changes.length}</strong></li>
                                <li><span>Components</span><strong>{preview.components.length}</strong></li>
                                <li><span>Ports</span><strong>{preview.ports.length}</strong></li>
                                <li><span>Connections</span><strong>{preview.connections.length}</strong></li>
                            </ul>
                            {preview.reason
                                ? <p>{preview.reason}</p>
                                : undefined}
                            {preview.changes.length > 0
                                ? <div className='athena-semantic-macro-catalog__subsection'>
                                    <h4>Changes</h4>
                                    <ul className='athena-semantic-macro-catalog__list'>
                                        {preview.changes.map(change => <li
                                            key={`${change.kind}:${change.title}`}
                                            className='athena-semantic-macro-catalog__item'
                                        >
                                            <div className='athena-semantic-macro-catalog__item-header'>
                                                <span className='athena-semantic-macro-catalog__item-title'>{change.title}</span>
                                                <span className='athena-semantic-macro-catalog__pill'>{change.kind}</span>
                                            </div>
                                            {change.summary
                                                ? <div className='athena-semantic-macro-catalog__item-meta'>{change.summary}</div>
                                                : undefined}
                                        </li>)}
                                    </ul>
                                </div>
                                : undefined}
                            {preview.components.length > 0
                                ? <div className='athena-semantic-macro-catalog__subsection'>
                                    <h4>Components</h4>
                                    <ul className='athena-semantic-macro-catalog__list'>
                                        {preview.components.map(component => <li
                                            key={component.templateId}
                                            className='athena-semantic-macro-catalog__item'
                                            data-testid='athena-reuse-preview-component'
                                        >
                                            <div className='athena-semantic-macro-catalog__item-header'>
                                                <span className='athena-semantic-macro-catalog__item-title'>{component.title}</span>
                                                <span className='athena-semantic-macro-catalog__pill'>{component.templateId}</span>
                                            </div>
                                            <div className='athena-semantic-macro-catalog__item-meta'>
                                                <span><code>{component.conceptId}</code></span>
                                                {component.implementationId ? <span><code>{component.implementationId}</code></span> : undefined}
                                                <span>anchor <code>{component.originAnchorId}</code></span>
                                            </div>
                                            {component.summary
                                                ? <p className='athena-semantic-macro-catalog__item-summary'>{component.summary}</p>
                                                : undefined}
                                            <ul className='athena-semantic-macro-catalog__dense-list'>
                                                {Object.entries(component.properties).map(([name, value]) => <li key={`${component.templateId}:${name}`}>
                                                    <strong>{name}</strong> {this.parameterValueDisplay(value)}
                                                </li>)}
                                            </ul>
                                        </li>)}
                                    </ul>
                                </div>
                                : undefined}
                            {preview.connections.length > 0
                                ? <div className='athena-semantic-macro-catalog__subsection'>
                                    <h4>Connections</h4>
                                    <ul className='athena-semantic-macro-catalog__list'>
                                        {preview.connections.map(connection => <li
                                            key={connection.templateId}
                                            className='athena-semantic-macro-catalog__item'
                                        >
                                            <div className='athena-semantic-macro-catalog__item-header'>
                                                <span className='athena-semantic-macro-catalog__item-title'>{connection.title}</span>
                                                <span className='athena-semantic-macro-catalog__pill'>{connection.templateId}</span>
                                            </div>
                                            <div className='athena-semantic-macro-catalog__item-meta'>
                                                <span>{connection.fromComponentTemplateId}:{connection.fromPortRoleId}</span>
                                                <span>-&gt;</span>
                                                <span>{connection.toComponentTemplateId}:{connection.toPortRoleId}</span>
                                            </div>
                                        </li>)}
                                    </ul>
                                </div>
                                : undefined}
                            {preview.presentationConsequences.length > 0
                                ? <div className='athena-semantic-macro-catalog__subsection'>
                                    <h4>Presentation consequences</h4>
                                    <ul className='athena-semantic-macro-catalog__dense-list'>
                                        {preview.presentationConsequences.map(consequence => <li
                                            key={`${consequence.originAnchorId}:${consequence.hintType}`}
                                        >
                                            <strong>{consequence.hintType}</strong> {consequence.scope}
                                        </li>)}
                                    </ul>
                                </div>
                                : undefined}
                            {preview.warnings.length > 0
                                ? <div className='athena-semantic-macro-catalog__subsection'>
                                    <h4>Warnings</h4>
                                    <ul className='athena-semantic-macro-catalog__dense-list'>
                                        {preview.warnings.map(warning => <li key={warning}>{warning}</li>)}
                                    </ul>
                                </div>
                                : undefined}
                            <div className='athena-semantic-macro-catalog__actions'>
                                <button
                                    className='athena-semantic-macro-catalog__action'
                                    type='button'
                                    data-testid='athena-reuse-accept'
                                    disabled={!hasPreview || this.acceptanceLoading}
                                    onClick={() => void this.approvePreview()}
                                >
                                    {this.acceptanceLoading ? 'Submitting approval...' : 'Approve preview'}
                                </button>
                                <button
                                    className='athena-semantic-macro-catalog__action athena-semantic-macro-catalog__action--secondary'
                                    type='button'
                                    data-testid='athena-reuse-cancel'
                                    disabled={this.acceptanceLoading}
                                    onClick={() => this.cancelPreview()}
                                >
                                    Cancel
                                </button>
                            </div>
                        </>
                        : undefined}
                    {acceptance
                        ? <div
                            className='athena-semantic-macro-catalog__subsection'
                            data-testid='athena-reuse-acceptance-panel'
                        >
                            <h4>Approval status</h4>
                            <ul className='athena-semantic-macro-catalog__detail-list'>
                                <li><span>Status</span><strong>{acceptance.status}</strong></li>
                                <li><span>Preview</span><strong><code>{acceptance.previewId}</code></strong></li>
                                {acceptance.bundleId
                                    ? <li>
                                        <span>Bundle</span>
                                        <strong>
                                            <code data-testid='athena-reuse-acceptance-bundle-id'>{acceptance.bundleId}</code>
                                        </strong>
                                    </li>
                                    : undefined}
                                <li>
                                    <span>Operations</span>
                                    <strong data-testid='athena-reuse-acceptance-operation-count'>{acceptance.operations.length}</strong>
                                </li>
                                <li><span>Affected IDs</span><strong>{acceptance.affectedSemanticIds.length}</strong></li>
                                {acceptance.execution
                                    ? <li>
                                        <span>Command</span>
                                        <strong><code data-testid='athena-reuse-acceptance-command-id'>{acceptance.execution.commandId}</code></strong>
                                    </li>
                                    : undefined}
                                {acceptance.acceptedExpansion
                                    ? <li>
                                        <span>Expansion</span>
                                        <strong><code>{acceptance.acceptedExpansion.expansionId}</code></strong>
                                    </li>
                                    : undefined}
                            </ul>
                            {acceptance.reason
                                ? <p>{acceptance.reason}</p>
                                : undefined}
                            {acceptance.execution
                                ? <ul className='athena-semantic-macro-catalog__detail-list'>
                                    <li><span>Outcome</span><strong>{acceptance.execution.outcome}</strong></li>
                                    <li><span>Changed IDs</span><strong data-testid='athena-reuse-acceptance-changed-count'>{acceptance.execution.changedSemanticIds.length}</strong></li>
                                </ul>
                                : undefined}
                            {acceptance.semanticReview
                                ? <ul className='athena-semantic-macro-catalog__detail-list'>
                                    <li><span>Review changes</span><strong>{acceptance.semanticReview.authoredChangeCount}</strong></li>
                                    <li><span>Derived consequences</span><strong>{acceptance.semanticReview.derivedConsequenceCount}</strong></li>
                                    <li><span>Impact entries</span><strong>{acceptance.semanticReview.engineeringImpactCount}</strong></li>
                                </ul>
                                : undefined}
                            {acceptance.operations.length > 0
                                ? <div className='athena-semantic-macro-catalog__subsection'>
                                    <h5>Prepared bundle</h5>
                                    <ul className='athena-semantic-macro-catalog__dense-list'>
                                        {acceptance.operations.map(operation => <li key={operation.operationId}>
                                            <strong>{operation.kind}</strong> {operation.summary ?? operation.subjectId ?? operation.operationId}
                                        </li>)}
                                        </ul>
                                    </div>
                                : undefined}
                            {acceptance.acceptedExpansion?.memberships.length
                                ? <div className='athena-semantic-macro-catalog__subsection'>
                                    <h5>Inspect accepted structure</h5>
                                    <div className='athena-semantic-macro-catalog__actions'>
                                        <button
                                            className='athena-semantic-macro-catalog__action athena-semantic-macro-catalog__action--secondary'
                                            type='button'
                                            disabled={this.originInspectionLoading}
                                            onClick={() => void this.inspectOrigin({
                                                instantiationId: acceptance.instantiationId,
                                            })}
                                        >
                                            Inspect origin by instantiation
                                        </button>
                                    </div>
                                </div>
                                : undefined}
                        </div>
                        : undefined}
                </section>
                : undefined}
        </>;
    }

    protected renderParameterEditor(parameter: AthenaSemanticMacroParameterDefinitionPayload): React.ReactNode {
        const value = this.parameterInputValue(parameter);
        const allowedValues = parameter.validationRules.allowedValues;
        return <div
            key={parameter.name}
            className={`athena-semantic-macro-catalog__control ${parameter.description ? 'athena-semantic-macro-catalog__control--wide' : ''}`}
        >
            <label htmlFor={`athena-reuse-parameter-${parameter.name}`}>
                {parameter.label}
                {parameter.required ? <span className='athena-semantic-macro-catalog__required'>required</span> : undefined}
            </label>
            {this.renderParameterInput(parameter, value, allowedValues)}
            <div className='athena-semantic-macro-catalog__hint'>
                <span>{parameter.name} / {parameter.valueKind}</span>
                {parameter.description ? <span>{parameter.description}</span> : undefined}
                {parameter.validationRules.pattern ? <span>pattern {parameter.validationRules.pattern}</span> : undefined}
                {parameter.defaultValue ? <span>default {this.parameterValueDisplay(parameter.defaultValue)}</span> : undefined}
            </div>
        </div>;
    }

    protected renderParameterInput(
        parameter: AthenaSemanticMacroParameterDefinitionPayload,
        value: string,
        allowedValues: string[],
    ): React.ReactNode {
        const commonProps = {
            id: `athena-reuse-parameter-${parameter.name}`,
            'data-testid': `athena-reuse-parameter-${parameter.name}`,
        };
        if (parameter.valueKind === 'boolean') {
            return <select
                {...commonProps}
                value={value}
                onChange={event => this.updateParameterInput(parameter.name, event.target.value)}
            >
                <option value=''>Unset</option>
                <option value='true'>true</option>
                <option value='false'>false</option>
            </select>;
        }
        if (parameter.valueKind === 'integer') {
            return <input
                {...commonProps}
                type='number'
                step={1}
                value={value}
                onChange={event => this.updateParameterInput(parameter.name, event.target.value)}
            />;
        }
        if (allowedValues.length > 0) {
            return <select
                {...commonProps}
                value={value}
                onChange={event => this.updateParameterInput(parameter.name, event.target.value)}
            >
                <option value=''>Use governed default / unset</option>
                {allowedValues.map(allowedValue => <option key={`${parameter.name}:${allowedValue}`} value={allowedValue}>
                    {allowedValue}
                </option>)}
            </select>;
        }
        return <input
            {...commonProps}
            type='text'
            value={value}
            onChange={event => this.updateParameterInput(parameter.name, event.target.value)}
        />;
    }

    protected renderItem(item: AthenaSemanticMacroCatalogItem): React.ReactNode {
        const selected = item.macroId === this.selectedMacroId;
        return <li
            key={item.macroId}
            className={`athena-semantic-macro-catalog__item ${selected ? 'athena-semantic-macro-catalog__item--selected' : ''}`}
        >
            <button
                className='athena-semantic-macro-catalog__selectable'
                type='button'
                data-testid='athena-reuse-macro-item'
                onClick={() => this.selectMacro(item.macroId)}
            >
                <span className='athena-semantic-macro-catalog__item-title'>{item.displayName}</span>
                <span className='athena-semantic-macro-catalog__item-meta'>
                    <code>{item.macroId}</code>
                    <span><code>{item.packageLabel}</code></span>
                </span>
            </button>
            {item.summary
                ? <p className='athena-semantic-macro-catalog__item-summary'>{item.summary}</p>
                : undefined}
        </li>;
    }
}
