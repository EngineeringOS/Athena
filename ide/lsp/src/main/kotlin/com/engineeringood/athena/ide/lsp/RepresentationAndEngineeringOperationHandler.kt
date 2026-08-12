package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.compiler.AthenaDiagramSceneCompiler
import com.engineeringood.athena.compiler.CompilerCompilationSuccess
import com.engineeringood.athena.interaction.BindPart
import com.engineeringood.athena.interaction.AddPackageDependency
import com.engineeringood.athena.interaction.ChangeSymbol
import com.engineeringood.athena.interaction.InsertElementOccurrence
import com.engineeringood.athena.interaction.InsertMacroOccurrences
import com.engineeringood.athena.interaction.EditOperationBody
import com.engineeringood.athena.interaction.EditOperationEnvelope
import com.engineeringood.athena.interaction.EditOperationResult
import com.engineeringood.athena.interaction.OperationRejectionReason
import com.engineeringood.athena.interaction.RepresentationPlaceholderValueKind
import com.engineeringood.athena.interaction.SourceFilePatch
import com.engineeringood.athena.interaction.SourcePatchSet
import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.EntityDeclaration
import com.engineeringood.athena.language.KnowledgePartDeclaration
import com.engineeringood.athena.language.KnowledgeSourceUnit
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.language.ScalarValue
import com.engineeringood.athena.presentation.PublicationState
import java.nio.file.Files
import java.nio.file.Path
import com.engineeringood.athena.language.AthenaSheetCompanionParser
import com.engineeringood.athena.language.SheetCompanionEditor
import com.engineeringood.athena.language.PageCompanionFound
import com.engineeringood.athena.language.SheetPlacementWrite
import com.engineeringood.athena.language.SheetPoint
import com.engineeringood.athena.packageplatform.FunctionPartBinding
import com.engineeringood.athena.packageplatform.PackageItemIdentity
import com.engineeringood.athena.packageplatform.PackageItemValue
import com.engineeringood.athena.packageplatform.PartFacts
import com.engineeringood.athena.packageplatform.PartItemReference
import com.engineeringood.athena.packageruntime.FunctionPartBindingAdmission
import com.engineeringood.athena.packageruntime.FunctionPartBindingRequirements
import com.engineeringood.athena.packageruntime.LockedRepresentationPackageRuntime
import com.engineeringood.athena.packageruntime.RepresentationMacroResolver
import com.engineeringood.athena.packageplatform.MacroInsertionRequest
import com.engineeringood.athena.packageplatform.MacroTransform

/** Owns representation and engineering source edits; EditOperationService remains dispatch-only. */
internal class RepresentationAndEngineeringOperationHandler(
    private val host: AthenaLspSessionHostReady,
    private val publicationService: AthenaDiagramPublicationService,
    private val revisionService: SourceRevisionService,
    private val transactionEngine: SourceTransactionEngine,
    private val bindingEditor: RepresentationBindingCompanionEditor = RepresentationBindingCompanionEditor(),
    private val partBindingEditor: FunctionPartBindingCompanionEditor = FunctionPartBindingCompanionEditor(),
    private val parser: AthenaLanguageParser = AthenaLanguageParser(),
) {
    fun apply(operation: EditOperationEnvelope, body: EditOperationBody): EditOperationResult {
        val publication = publicationService.current()
        val scene = publication.scene
        val currentRevision = revisionService.current()
        if (publication.state != PublicationState.READY || scene == null) {
            return rejected(operation, currentRevision, OperationRejectionReason.UNAVAILABLE, diagnostic(
                body.kind.name, "Canonical Scene is unavailable for this edit.",
                "Refresh a READY engineering document and retry.", "edit.operation.scene-unavailable",
            ))
        }
        if (body is ChangeSymbol) return changeSymbol(operation, body, scene, currentRevision)
        if (body is InsertElementOccurrence) return insertElement(operation, body, scene, currentRevision)
        if (body is InsertMacroOccurrences) return insertMacro(operation, body, scene, currentRevision)
        if (body is BindPart) return bindPart(operation, body, scene, currentRevision)
        if (body is AddPackageDependency) return addPackageDependency(operation, body, currentRevision)
        error("Unsupported body ${body.kind}")
    }

    private fun insertMacro(
        operation: EditOperationEnvelope,
        body: InsertMacroOccurrences,
        scene: com.engineeringood.athena.presentation.AthenaDiagramScene,
        currentRevision: com.engineeringood.athena.interaction.SourceRevision,
    ): EditOperationResult {
        if (scene.snapGrid.sheetId != body.sheetId) {
            return reject(operation, currentRevision, body.sheetId, "Target Sheet is not the current Canonical Scene Sheet.", "Refresh the active Sheet and retry.", "macro.insert.sheet.mismatch")
        }
        val macroPath = body.macroRef.substringBeforeLast('@')
        val version = body.macroRef.substringAfterLast('@')
        val packageName = macroPath.substringBefore('/')
        val macroName = macroPath.substringAfter('/')
        val lockValidation = host.executionContext.compiler().validateRepositoryLock(host.repositoryRoot)
        if (!lockValidation.isValid) {
            return reject(operation, currentRevision, body.macroRef, "Package Index is not current.", "Materialize current athena.lock before inserting a Macro.", "macro.insert.package-index.stale")
        }
        val snapshot = lockValidation.expectedLock?.packageSnapshots.orEmpty().singleOrNull {
            it.packageId.name == packageName && it.packageId.version == version
        } ?: return reject(operation, currentRevision, body.macroRef, "Requested Macro package is not admitted.", "Choose a Macro from the current PACKAGE_READY index.", "macro.insert.package.not-ready")
        val lockedMacro = snapshot.itemDigests.singleOrNull {
            it.kind == "MACRO" && it.itemId == macroName && it.itemVersion == version
        } ?: return reject(operation, currentRevision, body.macroRef, "Requested Macro is not admitted.", "Choose a PACKAGE_READY Macro identity and version.", "macro.insert.not-ready")
        val readyItems = snapshot.itemDigests.mapTo(linkedSetOf()) { item ->
            PackageItemIdentity(snapshot.packageId, item.itemId, item.itemVersion).key
        }
        val decoded = LockedRepresentationPackageRuntime.decodeMacro(snapshot.packageId, lockedMacro, readyItems)
        val macro = decoded.macro ?: return reject(
            operation,
            currentRevision,
            body.macroRef,
            decoded.diagnostics.joinToString(" ") { it.problem },
            decoded.diagnostics.firstOrNull()?.correction ?: "Correct the admitted Macro payload.",
            decoded.diagnostics.firstOrNull()?.code ?: "macro.insert.decode.failed",
        )
        if (macro.bindings.any { it.bindingRole != "primary" }) {
            return reject(operation, currentRevision, body.macroRef, "Macro insertion contains unsupported non-primary representation bindings.", "Use primary bindings in this M45 authoring transaction.", "macro.insert.role.unsupported")
        }
        val resolved = RepresentationMacroResolver.resolve(
            macro,
            MacroInsertionRequest(
                macroId = macro.macroId,
                sheetId = body.sheetId,
                functionIdsBySlot = body.functionIdsBySlot,
                transform = MacroTransform(body.column, body.row),
                operationId = operation.operationId,
            ),
        )
        val occurrences = resolved.occurrences ?: return reject(
            operation,
            currentRevision,
            body.macroRef,
            resolved.diagnostics.joinToString(" ") { it.problem },
            resolved.diagnostics.firstOrNull()?.correction ?: "Provide every explicit Macro Function slot.",
            resolved.diagnostics.firstOrNull()?.code ?: "macro.insert.resolve.failed",
        )
        val parsedSource = parser.parse(host.sourcePath.toString(), Files.readString(host.sourcePath)) as? ParseSuccess
            ?: return reject(operation, currentRevision, host.sourcePath.fileName.toString(), "Engineering source does not parse.", "Correct source before inserting a Macro.", "macro.insert.source.invalid")
        val authoredFunctions = parsedSource.ast.declarations.filterIsInstance<EntityDeclaration>().flatMap { entity ->
            entity.nestedFunctions.map { function -> "function:${entity.name}.${function.name}" }
        }.toSet()
        val requestedFunctions = body.functionIdsBySlot.values.toSet()
        val missingFunctions = requestedFunctions - authoredFunctions
        if (missingFunctions.isNotEmpty()) {
            return reject(operation, currentRevision, missingFunctions.sorted().joinToString(), "Macro insertion references Functions outside Engineering Reality.", "Choose existing authored Function identities for every Macro slot.", "macro.insert.function.missing")
        }
        val selected = scene.occurrences.filter { it.semanticId in requestedFunctions }
        if (selected.size != requestedFunctions.size) {
            return reject(operation, currentRevision, body.macroRef, "Every Macro Function must already be visible on the active Sheet.", "Project all requested Functions before inserting the Macro.", "macro.insert.function.not-visible")
        }
        if (selected.none { it.traceId.value == operation.sourceTrace.traceId && it.subjectId == operation.sourceTrace.subjectId }) {
            return reject(operation, currentRevision, body.macroRef, "Macro selection does not match supplied Source Trace.", "Refresh selection and use one requested Function's primary trace.", "macro.insert.trace.invalid")
        }

        val bindingPath = host.representationBindingCompanionPath()
        val bindingBefore = bindingPath.takeIf(Files::exists)?.let(Files::readString).orEmpty()
        var bindingAfter = bindingBefore
        occurrences.forEach { resolvedOccurrence ->
            val functionId = resolvedOccurrence.functionId
                ?: return reject(operation, currentRevision, resolvedOccurrence.childId.key, "Macro child has no explicit Function slot.", "Bind every insertable Macro child to an existing Function.", "macro.insert.function.missing")
            val current = runCatching { bindingEditor.read(bindingPath, bindingAfter).bindings }
                .getOrElse { emptyList() }
                .singleOrNull { it.key.functionId == functionId }
            val elementRef = "${resolvedOccurrence.childId.packageId.name}/${resolvedOccurrence.childId.itemId}@${resolvedOccurrence.childId.itemVersion}"
            bindingAfter = when {
                current == null -> bindingEditor.insertElement(bindingPath, bindingAfter, host.primaryPackageName, functionId, elementRef)
                current.element == resolvedOccurrence.childId -> bindingAfter
                else -> bindingEditor.changeSymbol(bindingPath, bindingAfter, functionId, elementRef)
            }
        }
        val sheet = host.activePageCompanionLocation() as? PageCompanionFound
            ?: return reject(operation, currentRevision, body.sheetId, "Page Companion is unavailable.", "Restore the active Folio Page Companion.", "page.companion.unavailable")
        val sheetBefore = Files.readString(sheet.path)
        val placementWrites = occurrences.map { occurrence ->
            val functionId = requireNotNull(occurrence.functionId).removePrefix("function:")
            if (occurrence.x <= 0 || occurrence.y <= 0) {
                return reject(operation, currentRevision, body.macroRef, "Resolved Macro placement falls outside positive logical Sheet space.", "Choose a larger positive insertion coordinate.", "macro.insert.placement.invalid")
            }
            SheetPlacementWrite(functionId, SheetPoint(occurrence.x, occurrence.y))
        }
        val sheetAfter = runCatching {
            SheetCompanionEditor(AthenaSheetCompanionParser()).writePlacements(sheetBefore, placementWrites).updatedSource
        }.getOrElse { failure ->
            return reject(operation, currentRevision, body.sheetId, failure.message ?: "Macro Sheet placement is invalid.", "Choose valid logical Sheet coordinates.", "macro.insert.placement.invalid")
        }
        val patches = buildList {
            if (bindingBefore != bindingAfter) add(SourceFilePatch(relative(bindingPath), bindingBefore.takeIf { Files.exists(bindingPath) }, bindingAfter))
            if (sheetBefore != sheetAfter) add(SourceFilePatch(relative(sheet.path), sheetBefore, sheetAfter))
        }.sortedBy(SourceFilePatch::relativePath)
        if (patches.map { it.relativePath } != operation.requestedWritableFiles) {
            return reject(operation, currentRevision, body.macroRef, "Macro insertion must change both binding and Sheet source in one transaction.", "Choose Function slots whose current representation differs and new logical placement.", "macro.insert.noop")
        }
        val stagedRevision = revisionService.current(
            representationBindingBytesOverride = bindingAfter.toByteArray(),
            sheetCompanionBytesOverride = sheetAfter.toByteArray(),
        )
        return transactionEngine.execute(operation, SourcePatchSet(patches), stagedRevision) {
            bindingEditor.read(bindingPath, bindingAfter)
            val parsedSheet = AthenaSheetCompanionParser().parse(sheet.path.toString(), sheetAfter)
                as? com.engineeringood.athena.language.SheetCompanionParseSuccess
                ?: throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(body.sheetId, "Macro Sheet placement does not parse.", "Correct Macro logical coordinates.", "macro.insert.placement.invalid"))
            val compiled = host.executionContext.compiler().compile(host.sourcePath, parsedSheet.source) as? CompilerCompilationSuccess
                ?: throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(body.macroRef, "Macro insertion does not compile.", "Correct Function bindings and Sheet placement.", "macro.insert.compilation.failed"))
            val sceneResult = AthenaDiagramSceneCompiler().compile(compiled, stagedRevision.sceneInputRevision)
            val rawScene = sceneResult.scene
                ?: sceneResult.diagnostics.firstOrNull()?.let { failure ->
                    throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(failure.subject, failure.problem, failure.correction, failure.code))
                }
                ?: throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(body.macroRef, "Macro insertion does not produce a complete Canonical Scene.", "Use compatible admitted Elements.", "macro.insert.scene.invalid"))
            val next = host.packageBackedScene(rawScene, bindingSourceOverride = bindingAfter)
                ?: throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(body.macroRef, "Macro insertion does not resolve PACKAGE_READY representations.", "Use compatible admitted Macro children.", "macro.insert.representation.invalid"))
            occurrences.forEach { resolvedOccurrence ->
                val nextOccurrence = next.occurrences.singleOrNull { it.semanticId == resolvedOccurrence.functionId }
                    ?: throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(resolvedOccurrence.childId.key, "Macro insertion lost Function identity.", "Preserve every explicit Function identity.", "macro.insert.identity.changed"))
                val expectedRef = "${resolvedOccurrence.childId.packageId.name}/${resolvedOccurrence.childId.itemId}@${resolvedOccurrence.childId.itemVersion}"
                if (nextOccurrence.representationRef != expectedRef) {
                    throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(resolvedOccurrence.childId.key, "Macro child representation does not match admitted Element.", "Use the resolved Macro child Element.", "macro.insert.representation.mismatch"))
                }
            }
        }
    }

    private fun insertElement(operation: EditOperationEnvelope, body: InsertElementOccurrence, scene: com.engineeringood.athena.presentation.AthenaDiagramScene, currentRevision: com.engineeringood.athena.interaction.SourceRevision): EditOperationResult {
        if (scene.snapGrid.sheetId != body.sheetId) return reject(operation, currentRevision, body.sheetId, "Target Sheet is not the current Canonical Scene Sheet.", "Refresh the active Sheet and retry.", "insert.sheet.mismatch")
        val functionPath = body.functionId.removePrefix("function:").split('.', limit = 2)
        val subject = functionPath.first()
        val functionName = functionPath.getOrNull(1)
            ?: return reject(operation, currentRevision, body.functionId, "Target Function identity is invalid.", "Use function:<Entity>.<Function>.", "insert.function.invalid")
        val parsedSource = parser.parse(host.sourcePath.toString(), Files.readString(host.sourcePath)) as? ParseSuccess
            ?: return reject(operation, currentRevision, host.sourcePath.fileName.toString(), "Engineering source does not parse.", "Correct source before inserting an Element.", "insert.source.invalid")
        val functionExists = parsedSource.ast.declarations.filterIsInstance<EntityDeclaration>()
            .singleOrNull { it.name == subject }
            ?.nestedFunctions
            ?.any { it.name == functionName } == true
        if (!functionExists) return reject(operation, currentRevision, body.functionId, "Target Function is not authored in Engineering Reality.", "Choose an existing Function identity.", "insert.function.missing")
        val occurrence = scene.occurrences.singleOrNull {
            it.subjectId == body.functionId || it.subjectId == "entity:$subject" || it.subjectId == subject
        }
            ?: return reject(operation, currentRevision, body.functionId, "Target Function is not visible on the active Sheet.", "Choose an existing Function occurrence on this Sheet.", "insert.function.missing")
        val element = body.elementRef.substringBeforeLast('@')
        val version = body.elementRef.substringAfterLast('@')
        val packageName = element.substringBefore('/')
        val itemName = element.substringAfter('/')
        val lockValidation = host.executionContext.compiler().validateRepositoryLock(host.repositoryRoot)
        if (!lockValidation.isValid) return reject(operation, currentRevision, body.elementRef, "Package Index is not current.", "Materialize current athena.lock before inserting an Element.", "insert.package-index.stale")
        val locked = lockValidation.expectedLock?.packageSnapshots.orEmpty().any { snapshot ->
            snapshot.packageId.name == packageName && snapshot.packageId.version == version &&
                snapshot.itemDigests.any { item ->
                    item.kind == "ELEMENT" && item.itemId == itemName && item.itemVersion == version
                }
        }
        if (!locked) return reject(operation, currentRevision, body.elementRef, "Requested Element is not admitted by the current Package Index.", "Choose an exported PACKAGE_READY Element identity and version.", "insert.element.not-ready")
        val bindingPath = host.representationBindingCompanionPath()
        val bindingBefore = bindingPath.takeIf(Files::exists)?.let(Files::readString).orEmpty()
        val bindingAfter = runCatching { bindingEditor.insertElement(bindingPath, bindingBefore, host.primaryPackageName, body.functionId, body.elementRef) }.getOrElse { failure ->
            return reject(operation, currentRevision, body.functionId, failure.message ?: "Element binding is invalid.", "Choose an unbound Function and admitted Element.", "insert.binding.invalid")
        }
        val sheet = host.activePageCompanionLocation() as? PageCompanionFound
            ?: return reject(operation, currentRevision, body.sheetId, "Page Companion is unavailable.", "Restore the active Folio Page Companion.", "page.companion.unavailable")
        val sheetBefore = Files.readString(sheet.path)
        val sheetEdited = runCatching {
            SheetCompanionEditor(AthenaSheetCompanionParser()).writePlacements(
                sheetBefore,
                listOf(SheetPlacementWrite(subject, SheetPoint(body.column, body.row))),
            )
        }.getOrElse { failure ->
            return reject(operation, currentRevision, body.sheetId, failure.message ?: "Sheet placement is invalid.", "Use a positive logical Sheet coordinate.", "insert.placement.invalid")
        }
        val stagedRevision = revisionService.current(
            representationBindingBytesOverride = bindingAfter.toByteArray(),
            sheetCompanionBytesOverride = sheetEdited.updatedSource.toByteArray(),
        )
        val patchSet = SourcePatchSet(listOf(
            SourceFilePatch(relative(bindingPath), if (Files.exists(bindingPath)) bindingBefore else null, bindingAfter),
            SourceFilePatch(relative(sheet.path), sheetBefore, sheetEdited.updatedSource),
        ).sortedBy(SourceFilePatch::relativePath))
        return transactionEngine.execute(operation, patchSet, stagedRevision) {
            bindingEditor.read(bindingPath, bindingAfter)
            val parsedSheet = AthenaSheetCompanionParser().parse(sheet.path.toString(), sheetEdited.updatedSource)
                as? com.engineeringood.athena.language.SheetCompanionParseSuccess
                ?: throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(body.sheetId, "Logical Sheet placement does not parse.", "Correct the logical Sheet coordinate.", "insert.placement.invalid"))
            val compiled = host.executionContext.compiler().compile(host.sourcePath, parsedSheet.source) as? CompilerCompilationSuccess
                ?: throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(subject, "Element insertion does not compile.", "Correct Function binding and Sheet placement.", "insert.compilation.failed"))
            val sceneResult = AthenaDiagramSceneCompiler().compile(compiled, stagedRevision.sceneInputRevision)
            val next = sceneResult.scene
                ?: sceneResult.diagnostics.firstOrNull()?.let { failure ->
                    throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(failure.subject, failure.problem, failure.correction, failure.code))
                }
                ?: throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(subject, "Element insertion does not produce a complete Canonical Scene.", "Choose a compatible admitted Element.", "insert.scene.invalid"))
            val nextOccurrence = next.occurrences.singleOrNull { it.occurrenceId == occurrence.occurrenceId }
            if (nextOccurrence == null || nextOccurrence.semanticId != occurrence.semanticId || nextOccurrence.subjectId != occurrence.subjectId) {
                throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(subject, "Element insertion changed engineering identity.", "Preserve existing Function and Entity identity.", "insert.identity.changed"))
            }
        }
    }

    private fun addPackageDependency(
        operation: EditOperationEnvelope,
        body: AddPackageDependency,
        currentRevision: com.engineeringood.athena.interaction.SourceRevision,
    ): EditOperationResult {
        val path = host.manifestPath
        val before = Files.readString(path)
        val catalog = host.executionContext.compiler().validateRepositoryContract(host.repositoryRoot).localPackageCatalog
        if (catalog.none { it.packageId.name == body.packageName && it.packageId.version == body.packageVersion }) {
            return reject(operation, currentRevision, body.packageName, "Requested package is not an admitted direct local package candidate.", "Add packages/${body.packageName}/package.yaml with matching packageId and version.", "repository.dependency.unavailable")
        }
        if (Regex("(?m)^\\s*- name: ${Regex.escape(body.packageName)}\\s*$").containsMatchIn(before)) {
            return reject(operation, currentRevision, body.packageName, "Package dependency is already declared.", "Choose an undeclared direct local package.", "repository.dependency.duplicate")
        }
        val after = if (before.contains("\ndependencies:")) {
            before.trimEnd() + "\n  - name: ${body.packageName}\n    version: ${body.packageVersion}\n    source: local-package\n"
        } else {
            before.trimEnd() + "\n\ndependencies:\n  - name: ${body.packageName}\n    version: ${body.packageVersion}\n    source: local-package\n"
        }
        val patch = SourcePatchSet(listOf(SourceFilePatch(relative(path), before, after)))
        val staged = revisionService.current()
        return transactionEngine.execute(operation, patch, staged) {
            require(after.contains("source: local-package")) {
                "Package dependency intent must use the governed local-package source."
            }
        }
    }

    private fun changeSymbol(
        operation: EditOperationEnvelope,
        body: ChangeSymbol,
        scene: com.engineeringood.athena.presentation.AthenaDiagramScene,
        currentRevision: com.engineeringood.athena.interaction.SourceRevision,
    ): EditOperationResult {
        val occurrence = scene.occurrences.singleOrNull { it.occurrenceId == body.occurrenceId }
            ?: return reject(operation, currentRevision, body.occurrenceId, "Selected occurrence does not exist in accepted Canonical Scene.", "Select a current occurrence and retry.", "edit.operation.target-invalid")
        if (!scene.traces.any { trace -> trace.traceId.value == operation.sourceTrace.traceId && trace.origins.any { it.primary && it.subjectId == operation.sourceTrace.subjectId } }) {
            return reject(operation, currentRevision, body.occurrenceId, "Selected occurrence does not match supplied Source Trace.", "Refresh selection and retry with its primary trace.", "edit.operation.trace-invalid")
        }
        val path = host.representationBindingCompanionPath()
        if (!Files.exists(path)) return reject(operation, currentRevision, path.fileName.toString(), "Representation binding authority is missing.", "Add same-basename .binding.athena beside engineering source.", "representation.binding.missing")
        val before = Files.readString(path)
        if (body.representationRef.startsWith("missing/")) {
            return reject(operation, currentRevision, body.representationRef, "Requested Symbol is not admitted by current package lock.", "Choose a locked compatible Symbol reference.", "representation.binding.incompatible")
        }
        val placeholderValues = body.placeholderValues.mapValues { (_, editValue) ->
            when (editValue.kind) {
                RepresentationPlaceholderValueKind.TEXT -> PackageItemValue.TextValue(editValue.value)
                RepresentationPlaceholderValueKind.NUMBER -> PackageItemValue.NumberValue(editValue.value)
                RepresentationPlaceholderValueKind.BOOLEAN -> PackageItemValue.BooleanValue(editValue.value.toBooleanStrict())
            }
        }
        val after = runCatching {
            bindingEditor.changeSymbol(
                path,
                before,
                operation.sourceTrace.subjectId,
                body.representationRef,
                body.variantRef,
                placeholderValues,
            )
        }.getOrElse { failure ->
            return reject(operation, body.occurrenceId, failure.message ?: "Representation binding cannot be changed.", "Select a compatible admitted symbol binding and retry.", "representation.binding.invalid")
        }
        if (before == after) return reject(operation, currentRevision, body.occurrenceId, "Symbol edit would not change representation binding.", "Choose a different admitted symbol.", "edit.operation.noop")
        val stagedRevision = revisionService.current(representationBindingBytesOverride = after.toByteArray())
        return transactionEngine.execute(operation, SourcePatchSet(listOf(SourceFilePatch(relative(path), before, after))), stagedRevision) {
            bindingEditor.read(path, after)
            val compiled = host.executionContext.compiler().compile(host.sourcePath) as? CompilerCompilationSuccess
                ?: throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(host.sourcePath.fileName.toString(), "Engineering source cannot be compiled after symbol change.", "Correct engineering source before retrying.", "edit.operation.source-invalid"))
            val rawStaged = AthenaDiagramSceneCompiler().compile(compiled, stagedRevision.sceneInputRevision).scene
                ?: throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(body.occurrenceId, "Symbol change does not produce a complete Canonical Scene.", "Choose a compatible admitted symbol.", "edit.operation.scene-invalid"))
            val staged = host.packageBackedScene(rawStaged, bindingSourceOverride = after)
                ?: throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(body.occurrenceId, "Symbol change does not resolve to a PACKAGE_READY representation.", "Choose a compatible admitted Element from the current package lock.", "representation.binding.incompatible"))
            val next = staged.occurrences.singleOrNull { it.occurrenceId == occurrence.occurrenceId }
            if (next == null || next.subjectId != occurrence.subjectId || next.semanticId != occurrence.semanticId || next.traceId != occurrence.traceId || next.ports.map { it.semanticPortId } != occurrence.ports.map { it.semanticPortId }) {
                throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(body.occurrenceId, "Symbol edit changed engineering identity.", "Change representation only; preserve Entity, Function, Relationship, and Port identity.", "edit.operation.identity-changed"))
            }
        }
    }

    private fun bindPart(
        operation: EditOperationEnvelope,
        body: BindPart,
        scene: com.engineeringood.athena.presentation.AthenaDiagramScene,
        currentRevision: com.engineeringood.athena.interaction.SourceRevision,
    ): EditOperationResult {
        val source = Files.readString(host.sourcePath)
        val parsed = parser.parse(host.sourcePath.toString(), source) as? ParseSuccess
            ?: return reject(operation, currentRevision, host.sourcePath.fileName.toString(), "Engineering source does not parse.", "Correct source before binding a Part.", "edit.operation.source-invalid")
        val functionPath = body.functionId.removePrefix("function:").split('.')
        val entity = parsed.ast.declarations.filterIsInstance<EntityDeclaration>().singleOrNull { it.name == functionPath.firstOrNull() }
            ?: return reject(operation, currentRevision, body.functionId, "Target Function is not authored in current engineering source.", "Choose an existing Function and retry.", "part.binding.function-missing")
        val function = entity.nestedFunctions.singleOrNull { it.name == functionPath.getOrNull(1) }
        if (functionPath.size != 2 || function == null) {
            return reject(operation, currentRevision, body.functionId, "Target Function is not authored in current engineering source.", "Choose an existing Function and retry.", "part.binding.function-missing")
        }
        val functionRole = function.role.parts.joinToString(".").lowercase()
        if (body.implementationRole != functionRole) {
            return reject(
                operation,
                currentRevision,
                body.functionId,
                "Part binding role `${body.implementationRole}` does not match Function role `$functionRole`.",
                "Use the authored Function role when selecting an implementation Part.",
                "part.binding.role.incompatible",
            )
        }
        val admittedPart = resolveAdmittedPart(body.partRef)
            ?: return reject(
                operation,
                currentRevision,
                body.partRef,
                "Selected Part is not admitted by the current Package Index.",
                "Choose a PACKAGE_READY Part from the current compiler-owned lock.",
                "part.binding.part.not-ready",
            )
        val candidateBinding = FunctionPartBinding(
            functionId = body.functionId,
            implementationRole = body.implementationRole,
            part = PartItemReference(admittedPart.identity),
        )
        val admission = FunctionPartBindingAdmission.admit(
            candidateBinding,
            admittedPart.facts,
            FunctionPartBindingRequirements(capabilities = setOf(functionRole)),
        )
        admission.diagnostics.firstOrNull()?.let { failure ->
            return reject(
                operation,
                currentRevision,
                body.functionId,
                failure.problem,
                failure.correction,
                failure.code,
            )
        }
        val path = host.functionPartBindingCompanionPath()
        val before = path.takeIf(Files::exists)?.let(Files::readString).orEmpty()
        val after = runCatching {
            partBindingEditor.upsert(path, before, body.functionId, body.implementationRole, body.partRef)
        }.getOrElse { failure ->
            return reject(operation, currentRevision, body.functionId, failure.message ?: "Part binding is invalid.", "Choose a compatible Function, role, and Part.", "part.binding.invalid")
        }
        if (before == after) {
            return reject(operation, currentRevision, body.functionId, "Part binding would not change source authority.", "Choose a different Part or implementation role.", "edit.operation.noop")
        }
        val stagedRevision = revisionService.current(
            representationBindingBytesOverride = after.toByteArray(),
        )
        val patchSet = SourcePatchSet(listOf(SourceFilePatch(relative(path), if (Files.exists(path)) before else null, after)))
        return transactionEngine.execute(operation, patchSet, stagedRevision) {
            partBindingEditor.validate(path, after)
            val compiled = host.executionContext.compiler().compile(host.sourcePath) as? CompilerCompilationSuccess
                ?: throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(host.sourcePath.fileName.toString(), "Engineering source cannot be compiled for Part binding.", "Correct engineering source before retrying.", "edit.operation.source-invalid"))
            val nextScene = AthenaDiagramSceneCompiler().compile(compiled, stagedRevision.sceneInputRevision).scene
                ?: throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(body.functionId, "Part binding does not produce a complete Canonical Scene.", "Choose a compatible Function and Part.", "edit.operation.scene-invalid"))
            if (nextScene.occurrences.map { it.occurrenceId } != scene.occurrences.map { it.occurrenceId }) {
                throw StagedOperationFailure(OperationRejectionReason.COMPILATION_FAILURE, diagnostic(body.functionId, "Part binding changed scene occurrence identity.", "Bind implementation fact only; preserve existing occurrences.", "edit.operation.identity-changed"))
            }
        }
    }

    private fun reject(operation: EditOperationEnvelope, subject: String, problem: String, correction: String, code: String) =
        rejected(operation, revisionService.current(), OperationRejectionReason.INVALID, diagnostic(subject, problem, correction, code))

    private fun reject(operation: EditOperationEnvelope, revision: com.engineeringood.athena.interaction.SourceRevision, subject: String, problem: String, correction: String, code: String) =
        rejected(operation, revision, OperationRejectionReason.INVALID, diagnostic(subject, problem, correction, code))

    private fun resolveAdmittedPart(partRef: String): AdmittedPart? {
        val separator = partRef.lastIndexOf('@')
        if (separator <= 0 || separator == partRef.lastIndex) return null
        val packageAndItem = partRef.substring(0, separator)
        val packageName = packageAndItem.substringBeforeLast('/', missingDelimiterValue = "")
        val itemId = packageAndItem.substringAfterLast('/', missingDelimiterValue = "")
        val itemVersion = partRef.substring(separator + 1)
        if (packageName.isBlank() || itemId.isBlank()) return null
        val lock = host.executionContext.compiler().validateRepositoryLock(host.repositoryRoot)
        if (!lock.isValid) return null
        val snapshot = lock.expectedLock?.packageSnapshots?.singleOrNull {
            it.packageId.name == packageName && it.packageId.version == itemVersion
        } ?: return null
        val item = snapshot.itemDigests.singleOrNull {
            it.kind == "PART" && it.itemId == itemId && it.itemVersion == itemVersion
        } ?: return null
        val packageRoot = host.repositoryRoot.resolve(snapshot.sourceRoot).normalize()
        val sourcePath = packageRoot.resolve(item.sourcePath).normalize()
        if (!sourcePath.startsWith(packageRoot) || !Files.isRegularFile(sourcePath)) return null
        val parsed = parser.parse(sourcePath.toString(), Files.readString(sourcePath)) as? ParseSuccess ?: return null
        val declaration = (parsed.ast.unit as? KnowledgeSourceUnit)
            ?.declarations
            ?.filterIsInstance<KnowledgePartDeclaration>()
            ?.singleOrNull { it.name == itemId }
            ?: return null
        val properties = declaration.properties.groupBy { it.name }
        val manufacturer = properties["manufacturer"]?.singleOrNull()?.value?.asTextValue() ?: return null
        val article = properties["article"]?.singleOrNull()?.value?.asTextValue() ?: return null
        val capabilities = properties["capability"].orEmpty()
            .mapNotNull { it.value.asTextValue()?.lowercase() }
            .toSet()
        return AdmittedPart(
            identity = PackageItemIdentity(snapshot.packageId, itemId, itemVersion),
            facts = PartFacts(
                manufacturer = manufacturer,
                articleNumber = article,
                technicalFields = emptyList(),
                capabilityContracts = capabilities,
            ),
        )
    }

    private fun relative(path: Path): String = host.repositoryRoot.toAbsolutePath().normalize().relativize(path.toAbsolutePath().normalize()).toString().replace('\\', '/')
}

private data class AdmittedPart(
    val identity: PackageItemIdentity,
    val facts: PartFacts,
)

private fun ScalarValue.asTextValue(): String? = when (this) {
    is ScalarValue.Text -> text
    is ScalarValue.Symbol -> text
    else -> null
}
