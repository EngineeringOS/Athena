package com.engineeringood.athena.compiler.repository

import com.engineeringood.athena.language.AthenaLanguageParser
import com.engineeringood.athena.language.ElementDeclaration
import com.engineeringood.athena.language.KnowledgePartDeclaration
import com.engineeringood.athena.language.KnowledgeSourceUnit
import com.engineeringood.athena.language.MacroDeclaration
import com.engineeringood.athena.language.ParseSuccess
import com.engineeringood.athena.language.PlaceholderDeclaration
import com.engineeringood.athena.language.RepresentationSourceUnit
import com.engineeringood.athena.language.SymbolDeclaration
import com.engineeringood.athena.language.VariantDeclaration
import com.engineeringood.athena.packageplatform.ElementInterfaceFingerprint
import com.engineeringood.athena.packageplatform.MacroBindingReference
import com.engineeringood.athena.packageplatform.MacroChildPlacement
import com.engineeringood.athena.packageplatform.PackageItemIdentity
import com.engineeringood.athena.packageplatform.PackageItemValue
import com.engineeringood.athena.packageplatform.PlaceholderValueType
import com.engineeringood.athena.packageplatform.RepresentationMacro
import com.engineeringood.athena.packageplatform.RepresentationPlaceholder
import com.engineeringood.athena.packageplatform.RepresentationVariant
import com.engineeringood.athena.packageruntime.RepresentationMacroAdmission
import com.engineeringood.athena.packageruntime.VariantResolver
import com.engineeringood.athena.repository.PackageIdentifier
import com.engineeringood.athena.repository.RepositoryDiagnostic
import com.engineeringood.athena.repository.RepositoryDiagnosticSeverity
import com.engineeringood.athena.repository.RepositoryLockedItem
import com.engineeringood.athena.repository.RepositoryResourceHash
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

/** Compiles package-local Athena declarations into immutable lock-index entries. */
internal class NativePackageItemIndexCompiler(
    private val parser: AthenaLanguageParser = AthenaLanguageParser(),
) {
    fun compile(
        packageId: PackageIdentifier,
        packageRoot: Path,
        manifest: LocalPackageManifest,
        admittedResources: List<RepositoryResourceHash>,
    ): NativePackageItemIndexBuild {
        val diagnostics = mutableListOf<RepositoryDiagnostic>()
        val candidates = mutableListOf<NativePackageItemCandidate>()
        val symbolResources = linkedMapOf<String, List<String>>()
        val symbolsById = linkedMapOf<String, SymbolDeclaration>()
        val invalidCandidateKeys = linkedSetOf<String>()
        val elementCandidates = mutableListOf<ElementCandidateInput>()
        val macroCandidates = mutableListOf<MacroCandidateInput>()
        val variantCandidates = mutableListOf<VariantCandidateInput>()
        val placeholderCandidates = mutableListOf<PlaceholderCandidateInput>()
        Files.walk(packageRoot).use { paths ->
            paths.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".athena") }
                .sorted()
                .forEach { sourcePath ->
                    val source = Files.readString(sourcePath)
                    val parsed = parser.parse(sourcePath.toString(), source) as? ParseSuccess
                    if (parsed == null) {
                        diagnostics += nativeDiagnostic(
                            sourcePath,
                            "Package source does not parse as Athena.",
                            "Correct this package source before materializing athena.lock.",
                            "package.index.source.invalid",
                        )
                        return@forEach
                    }
                    val declaredPackage = parsed.ast.packageDeclaration?.name?.parts?.joinToString(".")
                    if (declaredPackage != packageId.name) {
                        diagnostics += nativeDiagnostic(
                            sourcePath,
                            "Package source declares `$declaredPackage`, not `${packageId.name}`.",
                            "Declare the owning package identity in every package source file.",
                            "package.index.source.package-mismatch",
                        )
                        return@forEach
                    }
                    val relative = packageRoot.relativize(sourcePath).toString().replace('\\', '/')
                    when (val unit = parsed.ast.unit) {
                        is RepresentationSourceUnit -> unit.declarations.forEach { declaration ->
                            when (declaration) {
                                is SymbolDeclaration -> {
                                    val resources = declaration.resources.map { it.path.value }
                                    declaration.identity?.value?.let {
                                        symbolResources[it] = resources
                                        symbolsById[it] = declaration
                                    }
                                    candidates += candidate(
                                        id = declaration.identity?.value,
                                        version = declaration.version?.value,
                                        kind = "SYMBOL",
                                        exportKind = "symbols",
                                        sourcePath = relative,
                                        source = source,
                                        start = declaration.span.start.offset,
                                        end = declaration.span.end.offset,
                                        resources = resources,
                                    )
                                }
                                is ElementDeclaration -> elementCandidates += ElementCandidateInput(
                                    declaration = declaration,
                                    sourcePath = relative,
                                    source = source,
                                )
                                is MacroDeclaration -> macroCandidates += MacroCandidateInput(declaration, relative, source)
                                is VariantDeclaration -> variantCandidates += VariantCandidateInput(declaration, relative, source)
                                is PlaceholderDeclaration -> placeholderCandidates += PlaceholderCandidateInput(declaration, relative, source)
                                else -> Unit
                            }
                        }
                        is KnowledgeSourceUnit -> unit.declarations.filterIsInstance<KnowledgePartDeclaration>().forEach { declaration ->
                            candidates += candidate(
                                id = declaration.name,
                                version = packageId.version,
                                kind = "PART",
                                exportKind = "parts",
                                sourcePath = relative,
                                source = source,
                                start = declaration.span.start.offset,
                                end = declaration.span.end.offset,
                                resources = emptyList(),
                            )
                        }
                        else -> Unit
                    }
                }
        }
        elementCandidates.forEach { input ->
            val declaration = input.declaration
            var invalid = false
            declaration.children.groupBy { it.id }.filterValues { it.size > 1 }.keys.sorted().forEach { childId ->
                diagnostics += nativeDiagnostic(
                    input.sourcePath,
                    "Element child `$childId` is declared more than once.",
                    "Keep one child declaration for each local child identity.",
                    "package.index.element.child-duplicate",
                )
                invalid = true
            }
            declaration.children.forEach { child ->
                val symbolId = child.symbolIdentity?.value
                if (symbolId == null || symbolsById[symbolId] == null) {
                    diagnostics += nativeDiagnostic(
                        input.sourcePath,
                        "Element child `${child.id}` references unknown Symbol `${symbolId ?: "<missing>"}`.",
                        "Reference a native Symbol declared in this package.",
                        "package.index.element.symbol-unresolved",
                    )
                    invalid = true
                }
            }
            declaration.exportedAnchors.forEach { anchor ->
                val child = declaration.children.singleOrNull { it.id == anchor.childId.value }
                val symbol = child?.symbolIdentity?.value?.let(symbolsById::get)
                if (child == null || symbol?.anchors?.none { it.id == anchor.childAnchorId.value } != false) {
                    diagnostics += nativeDiagnostic(
                        input.sourcePath,
                        "Element anchor `${anchor.id}` does not resolve `${anchor.childId.value}.${anchor.childAnchorId.value}`.",
                        "Export an anchor declared by one resolved child Symbol.",
                        "package.index.element.anchor-unresolved",
                    )
                    invalid = true
                }
            }
            declaration.exportedLabels.forEach { label ->
                val child = declaration.children.singleOrNull { it.id == label.childId.value }
                val symbol = child?.symbolIdentity?.value?.let(symbolsById::get)
                if (child == null || symbol?.graphic?.labels?.none { it.id == label.childLabelId.value } != false) {
                    diagnostics += nativeDiagnostic(
                        input.sourcePath,
                        "Element label `${label.id}` does not resolve `${label.childId.value}.${label.childLabelId.value}`.",
                        "Export a label declared by one resolved child Symbol.",
                        "package.index.element.label-unresolved",
                    )
                    invalid = true
                }
            }
            val childResources = input.declaration.children.flatMap { child ->
                child.symbolIdentity?.value?.let(symbolResources::get).orEmpty()
            }
            candidates += candidate(
                id = input.declaration.identity?.value,
                version = input.declaration.version?.value,
                kind = "ELEMENT",
                exportKind = "elements",
                sourcePath = input.sourcePath,
                source = input.source,
                start = input.declaration.span.start.offset,
                end = input.declaration.span.end.offset,
                resources = (input.declaration.resources.map { it.path.value } + childResources).distinct().sorted(),
                attributes = mapOf(
                    "interfaceFingerprint" to sha256(
                        nativeElementInterface(requireNotNull(input.declaration.identity?.value), elementCandidates),
                    ),
                ),
            )
            if (invalid) candidateKey("ELEMENT", declaration.identity?.value, declaration.version?.value)?.let(invalidCandidateKeys::add)
        }
        val elementsById = elementCandidates.mapNotNull { input ->
            val id = input.declaration.identity?.value ?: return@mapNotNull null
            val version = input.declaration.version?.value ?: return@mapNotNull null
            id to PackageItemIdentity(packageId, id, version)
        }.toMap()
        val readyElementKeys = elementsById.values
            .filterNot { candidateKey("ELEMENT", it.itemId, it.itemVersion) in invalidCandidateKeys }
            .map(PackageItemIdentity::key)
            .toSet()
        macroCandidates.forEach { input ->
            val declaration = input.declaration
            val orderedChildren = declaration.children
            candidates += candidate(
                id = declaration.identity?.value,
                version = declaration.version?.value,
                kind = "MACRO",
                exportKind = "macros",
                sourcePath = input.sourcePath,
                source = input.source,
                start = declaration.span.start.offset,
                end = declaration.span.end.offset,
                resources = emptyList(),
                attributes = buildMap {
                    orderedChildren.forEachIndexed { index, child ->
                        val prefix = "child.${index.toString().padStart(3, '0')}"
                        child.elementIdentity?.value?.let { put("$prefix.element", it) }
                        child.translate?.let {
                            put("$prefix.x", it.x.toStringWithoutDecimalZero())
                            put("$prefix.y", it.y.toStringWithoutDecimalZero())
                        }
                        put("$prefix.rotation", (child.rotate?.value ?: 0.0).toStringWithoutDecimalZero())
                        child.functionSlot?.value?.let { put("$prefix.functionSlot", it) }
                        child.bindingRole?.value?.let { put("$prefix.bindingRole", it) }
                    }
                },
            )
            val macroId = declaration.identity?.value?.let { id ->
                declaration.version?.value?.let { version -> PackageItemIdentity(packageId, id, version) }
            } ?: return@forEach
            val children = declaration.children.mapNotNull { child ->
                val elementId = child.elementIdentity?.value
                val item = elementId?.let(elementsById::get)
                if (item == null) {
                    diagnostics += nativeDiagnostic(
                        input.sourcePath,
                        "Macro child `${child.id}` references unknown Element `${elementId ?: "<missing>"}`.",
                        "Reference an exported native Element in this package.",
                        "package.index.macro.element-unresolved",
                    )
                    null
                } else {
                    val x = child.translate?.x?.toExactInt()
                    val y = child.translate?.y?.toExactInt()
                    val rotation = child.rotate?.value?.toExactInt() ?: 0
                    if (x == null || y == null || rotation !in 0..359) {
                        diagnostics += nativeDiagnostic(
                            input.sourcePath,
                            "Macro child `${child.id}` requires integer logical placement and rotation from 0 through 359.",
                            "Use integer translate coordinates and a supported rotation.",
                            "package.index.macro.placement-invalid",
                        )
                        null
                    } else {
                        MacroChildPlacement(item, x, y, rotation)
                    }
                }
            }
            val bindings = declaration.children.mapNotNull { child ->
                val elementId = child.elementIdentity?.value?.let(elementsById::get) ?: return@mapNotNull null
                val slot = child.functionSlot?.value
                val role = child.bindingRole?.value
                if ((slot == null) != (role == null)) {
                    diagnostics += nativeDiagnostic(
                        input.sourcePath,
                        "Macro child `${child.id}` must declare function and role together.",
                        "Declare both fields or remove both fields.",
                        "package.index.macro.binding-incomplete",
                    )
                    null
                } else if (slot != null && role != null) {
                    MacroBindingReference(elementId, slot, role)
                } else {
                    null
                }
            }
            if (children.size == declaration.children.size) {
                val result = runCatching {
                    RepresentationMacroAdmission.admit(RepresentationMacro(macroId, children, bindings), readyElementKeys)
                }.getOrNull()
                if (result?.isValid != true) {
                    diagnostics += nativeDiagnostic(
                        input.sourcePath,
                        "Macro `${macroId.itemId}` cannot be admitted from its declared children.",
                        "Use unique PACKAGE_READY Element children and explicit Function bindings.",
                        "package.index.macro.invalid",
                    )
                    invalidCandidateKeys += requireNotNull(candidateKey("MACRO", macroId.itemId, macroId.itemVersion))
                }
            } else {
                invalidCandidateKeys += requireNotNull(candidateKey("MACRO", macroId.itemId, macroId.itemVersion))
            }
        }
        variantCandidates.forEach { input ->
            val declaration = input.declaration
            val resources = declaration.resources.map { it.path.value }
            candidates += candidate(
                id = declaration.identity?.value,
                version = declaration.version?.value,
                kind = "VARIANT",
                exportKind = "variants",
                sourcePath = input.sourcePath,
                source = input.source,
                start = declaration.span.start.offset,
                end = declaration.span.end.offset,
                resources = resources,
                attributes = buildMap {
                    declaration.elementIdentity?.value?.let { put("element", it) }
                    declaration.elementIdentity?.value?.let { put("interfaceFingerprint", sha256(nativeElementInterface(it, elementCandidates))) }
                },
            )
            val variantId = declaration.identity?.value?.let { id ->
                declaration.version?.value?.let { version -> PackageItemIdentity(packageId, id, version) }
            } ?: return@forEach
            val elementId = declaration.elementIdentity?.value?.let(elementsById::get)
            if (elementId == null) {
                diagnostics += nativeDiagnostic(
                    input.sourcePath,
                    "Variant `${variantId.itemId}` references an unknown Element `${declaration.elementIdentity?.value ?: "<missing>"}`.",
                    "Reference an exported native Element in this package.",
                    "package.index.variant.element-unresolved",
                )
                invalidCandidateKeys += requireNotNull(candidateKey("VARIANT", variantId.itemId, variantId.itemVersion))
            } else if (resources.size != 1) {
                diagnostics += nativeDiagnostic(
                    input.sourcePath,
                    "Variant `${variantId.itemId}` requires exactly one admitted SVG resource.",
                    "Declare one package-local geometry resource.",
                    "package.index.variant.resource-count",
                )
                invalidCandidateKeys += requireNotNull(candidateKey("VARIANT", variantId.itemId, variantId.itemVersion))
            } else {
                val fingerprint = ElementInterfaceFingerprint(sha256(nativeElementInterface(input.declaration.elementIdentity!!.value, elementCandidates)))
                val result = VariantResolver.resolve(
                    elementId,
                    RepresentationVariant(variantId, elementId, fingerprint, PackageItemValue.ObjectValue(emptyMap())),
                    fingerprint,
                )
                if (!result.isValid) {
                    diagnostics += nativeDiagnostic(
                        input.sourcePath,
                        "Variant `${variantId.itemId}` changes its target Element interface.",
                        "Create a new Element item/version for an interface change.",
                        "package.index.variant.interface-mismatch",
                    )
                    invalidCandidateKeys += requireNotNull(candidateKey("VARIANT", variantId.itemId, variantId.itemVersion))
                }
            }
        }
        placeholderCandidates.forEach { input ->
            val declaration = input.declaration
            candidates += candidate(
                id = declaration.identity?.value,
                version = declaration.version?.value,
                kind = "PLACEHOLDER",
                exportKind = "placeholders",
                sourcePath = input.sourcePath,
                source = input.source,
                start = declaration.span.start.offset,
                end = declaration.span.end.offset,
                resources = emptyList(),
                attributes = buildMap {
                    declaration.targetPath?.value?.let { put("target", it) }
                    declaration.valueType?.value?.uppercase()?.let { put("type", it) }
                },
            )
            val type = declaration.valueType?.value?.uppercase()?.let { runCatching { PlaceholderValueType.valueOf(it) }.getOrNull() }
            val valid = runCatching {
                RepresentationPlaceholder(
                    requireNotNull(declaration.identity?.value),
                    requireNotNull(declaration.targetPath?.value),
                    requireNotNull(type),
                )
            }.isSuccess
            if (!valid) {
                diagnostics += nativeDiagnostic(
                    input.sourcePath,
                    "Placeholder `${declaration.name}` has an invalid identity, target, or type.",
                    "Use a representation target under label/style/property and type text, number, or boolean.",
                    "package.index.placeholder.invalid",
                )
                candidateKey("PLACEHOLDER", declaration.identity?.value, declaration.version?.value)?.let(invalidCandidateKeys::add)
            }
        }
        val admittedPaths = admittedResources.map { it.path }.toSet()
        candidates.forEach { candidate ->
            if (candidate.id == null || candidate.version == null) {
                diagnostics += nativeDiagnostic(candidate.sourcePath, "${candidate.kind} requires explicit identity and version.", "Declare identity and version before publishing this package item.", "package.index.item.identity-missing")
            }
            candidate.resources.filterNot(admittedPaths::contains).forEach { resource ->
                diagnostics += nativeDiagnostic(candidate.sourcePath, "${candidate.kind} references resource `$resource` outside admitted package resources.", "Declare a package-local resource under resources/.", "package.index.item.resource-unadmitted")
            }
        }
        val validCandidates = candidates.filter { it.id != null && it.version != null && it.resources.all(admittedPaths::contains) }
        validCandidates.groupBy { "${it.kind}:${it.id}@${it.version}" }.filterValues { it.size > 1 }.forEach { (key, duplicates) ->
            diagnostics += nativeDiagnostic(duplicates.first().sourcePath, "PackageItem `$key` is declared more than once.", "Keep one native declaration for each PackageItem identity.", "package.index.item.duplicate")
        }
        manifest.exports.forEach { (exportKind, ids) -> ids.forEach { id ->
            if (validCandidates.none { it.exportKind == exportKind && it.id == id }) {
                diagnostics += nativeDiagnostic(packageRoot.resolve("package.yaml"), "Export `$exportKind/$id` has no matching native Athena PackageItem.", "Declare matching identity in package source or remove this export.", "package.index.export.missing")
            }
        } }
        validCandidates.filter { candidate -> manifest.exports[candidate.exportKind].orEmpty().contains(candidate.id).not() }.forEach { candidate ->
            if (manifest.exports[candidate.exportKind].orEmpty().contains(candidate.id).not()) {
                diagnostics += nativeDiagnostic(candidate.sourcePath, "PackageItem `${candidate.id}` is not exported by package.yaml.", "Add it to exports or remove the native declaration.", "package.index.export.unexpected")
            }
        }
        val indexed = validCandidates
            .filter { candidate -> manifest.exports[candidate.exportKind].orEmpty().contains(candidate.id) }
            .filter { candidate -> candidates.count { it.kind == candidate.kind && it.id == candidate.id && it.version == candidate.version } == 1 }
            .filterNot { candidate -> candidateKey(candidate.kind, candidate.id, candidate.version) in invalidCandidateKeys }
            .map { candidate ->
                RepositoryLockedItem(
                    itemId = requireNotNull(candidate.id),
                    itemVersion = requireNotNull(candidate.version),
                    kind = candidate.kind,
                    digest = "sha256:${sha256(candidate.canonicalBytes())}",
                    sourcePath = candidate.sourcePath,
                    resourceReferences = candidate.resources.sorted(),
                    attributes = candidate.attributes.toSortedMap(),
                )
            }
            .sortedBy { "${it.kind}:${it.itemId}@${it.itemVersion}" }
        return NativePackageItemIndexBuild(indexed, diagnostics)
    }

    private fun candidate(
        id: String?,
        version: String?,
        kind: String,
        exportKind: String,
        sourcePath: String,
        source: String,
        start: Int,
        end: Int,
        resources: List<String>,
        attributes: Map<String, String> = emptyMap(),
    ) = NativePackageItemCandidate(id, version, kind, exportKind, sourcePath, source.substring(start, end), resources, attributes)
}

internal data class NativePackageItemIndexBuild(
    val items: List<RepositoryLockedItem>,
    val diagnostics: List<RepositoryDiagnostic>,
)

private data class NativePackageItemCandidate(
    val id: String?,
    val version: String?,
    val kind: String,
    val exportKind: String,
    val sourcePath: String,
    val declarationSource: String,
    val resources: List<String>,
    val attributes: Map<String, String>,
) {
    fun canonicalBytes(): ByteArray = buildString {
        appendLine("kind=$kind")
        appendLine("id=$id")
        appendLine("version=$version")
        appendLine("sourcePath=$sourcePath")
        resources.sorted().forEach { appendLine("resource=$it") }
        attributes.toSortedMap().forEach { (key, value) -> appendLine("attribute.$key=$value") }
        append(declarationSource)
    }.toByteArray(Charsets.UTF_8)
}

private data class ElementCandidateInput(
    val declaration: ElementDeclaration,
    val sourcePath: String,
    val source: String,
)

private data class MacroCandidateInput(val declaration: MacroDeclaration, val sourcePath: String, val source: String)
private data class VariantCandidateInput(val declaration: VariantDeclaration, val sourcePath: String, val source: String)
private data class PlaceholderCandidateInput(val declaration: PlaceholderDeclaration, val sourcePath: String, val source: String)

private fun Double.toExactInt(): Int? = takeIf { it.isFinite() && it % 1.0 == 0.0 && it in Int.MIN_VALUE.toDouble()..Int.MAX_VALUE.toDouble() }?.toInt()

private fun Double.toStringWithoutDecimalZero(): String = toExactInt()?.toString() ?: toString()

private fun candidateKey(kind: String, id: String?, version: String?): String? =
    if (id == null || version == null) null else "$kind:$id@$version"

private fun nativeElementInterface(elementId: String, inputs: List<ElementCandidateInput>): ByteArray {
    val declaration = inputs.singleOrNull { it.declaration.identity?.value == elementId }?.declaration
        ?: return "element=$elementId\n".toByteArray(Charsets.UTF_8)
    return buildString {
        append("athena-native-element-interface-v1\n")
        declaration.children.sortedBy { it.id }.forEach { child ->
            append("child=").append(child.id).append('|').append(child.symbolIdentity?.value).append('\n')
        }
        declaration.exportedAnchors.sortedBy { it.id }.forEach { anchor ->
            append("anchor=").append(anchor.id).append('|').append(anchor.childId.value).append('|').append(anchor.childAnchorId.value).append('\n')
        }
    }.toByteArray(Charsets.UTF_8)
}

private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

private fun nativeDiagnostic(path: Any, problem: String, correction: String, code: String) = RepositoryDiagnostic(
    code = code,
    message = "$problem $correction",
    severity = RepositoryDiagnosticSeverity.ERROR,
    sourcePath = path.toString().replace('\\', '/'),
)
