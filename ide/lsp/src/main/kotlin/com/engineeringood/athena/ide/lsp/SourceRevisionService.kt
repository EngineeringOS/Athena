package com.engineeringood.athena.ide.lsp

import com.engineeringood.athena.interaction.PackageItemDigest
import com.engineeringood.athena.interaction.RevisionDigest
import com.engineeringood.athena.interaction.SourceRevision
import com.engineeringood.athena.language.SheetCompanionFound
import com.engineeringood.athena.language.SheetCompanionLocator
import com.engineeringood.athena.language.SheetStyleCompanionLocator
import com.engineeringood.athena.presentation.AthenaDiagramScene
import com.engineeringood.athena.presentation.InputRevision
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

/** Computes the complete M44 compare-and-set tuple from repository authority. */
class SourceRevisionService(private val host: AthenaLspSessionHostReady) {
    fun current(
        sheetCompanionBytesOverride: ByteArray? = null,
        styleCompanionBytesOverride: ByteArray? = null,
        representationBindingBytesOverride: ByteArray? = null,
        engineeringSourceBytesOverride: ByteArray? = null,
    ): SourceRevision = revision(
        sheetCompanionBytesOverride = sheetCompanionBytesOverride,
        styleCompanionBytesOverride = styleCompanionBytesOverride,
        representationBindingBytesOverride = representationBindingBytesOverride,
        engineeringSourceBytesOverride = engineeringSourceBytesOverride,
        requireValidLock = true,
    )

    /** Computes read-only attempted input identity even when repository lock authority is currently invalid. */
    fun attempted(engineeringSourceBytesOverride: ByteArray? = null): SourceRevision = revision(
        engineeringSourceBytesOverride = engineeringSourceBytesOverride,
        requireValidLock = false,
    )

    private fun revision(
        sheetCompanionBytesOverride: ByteArray? = null,
        styleCompanionBytesOverride: ByteArray? = null,
        representationBindingBytesOverride: ByteArray? = null,
        engineeringSourceBytesOverride: ByteArray? = null,
        requireValidLock: Boolean,
    ): SourceRevision {
        val sourceDigest = engineeringAuthorityDigest(representationBindingBytesOverride, engineeringSourceBytesOverride)
        val sheetLocation = SheetCompanionLocator.locate(host.sourcePath)
        val sheetDigest = when (sheetLocation) {
            is SheetCompanionFound -> (sheetCompanionBytesOverride ?: Files.readAllBytes(sheetLocation.path)).sha256Hex()
            else -> sheetLocation::class.simpleName.orEmpty().toByteArray(StandardCharsets.UTF_8).sha256Hex()
        }
        val styleDigest = when {
            styleCompanionBytesOverride != null -> RevisionDigest.present(styleCompanionBytesOverride.sha256Hex())
            sheetLocation is SheetCompanionFound -> when (val style = SheetStyleCompanionLocator.locate(sheetLocation.path)) {
                is SheetCompanionFound -> RevisionDigest.present(Files.readAllBytes(style.path).sha256Hex())
                else -> RevisionDigest.absent()
            }
            else -> RevisionDigest.absent()
        }
        val lockDigest = if (Files.exists(host.lockPath)) RevisionDigest.present(Files.readAllBytes(host.lockPath).sha256Hex()) else RevisionDigest.absent()
        val packageItems = packageItemDigests(requireValidLock)
        val sourceRootIdentity = "source-root:sha256:${normalizedIdentity(host.sourceRootPath).sha256Hex()}"
        val sceneSchemaVersion = "scene-schema:${contractResource("/schema/athena-diagram-scene.schema.json").sha256Hex()}"
        val profileVersion = "presentation-profile:${contractResource("/profile/svg-safe-1.json").sha256Hex()}"
        val compilerVersion = "athena-compiler-1"
        val inputDigest = MessageDigest.getInstance("SHA-256").apply {
            listOf(
                sourceRootIdentity,
                sourceDigest,
                sheetDigest,
                styleDigest.canonicalValue(),
                lockDigest.canonicalValue(),
                compilerVersion,
                sceneSchemaVersion,
                profileVersion,
            ).forEach(::updateRecord)
            packageItems.forEach { updateRecord("${it.key}:${it.sha256}") }
        }.digest().toHex()
        return SourceRevision(
            sceneInputRevision = InputRevision("input:sha256:$inputDigest"),
            sourceRootIdentity = sourceRootIdentity,
            engineeringSourceDigest = sourceDigest,
            sheetDigest = sheetDigest,
            styleDigest = styleDigest,
            lockDigest = lockDigest,
            packageItemDigests = packageItems,
            compilerVersion = compilerVersion,
            sceneSchemaVersion = sceneSchemaVersion,
            profileVersion = profileVersion,
        )
    }

    private fun engineeringAuthorityDigest(representationBindingBytesOverride: ByteArray?, engineeringSourceBytesOverride: ByteArray?): String {
        val sourceBytes = engineeringSourceBytesOverride ?: Files.readAllBytes(host.sourcePath)
        val manifestBytes = Files.readAllBytes(host.manifestPath)
        val bindingBytes = representationBindingBytesOverride ?: host.representationBindingCompanionPath()
            .takeIf(Files::exists)
            ?.let(Files::readAllBytes)
        return MessageDigest.getInstance("SHA-256").apply {
            update(sourceBytes)
            update(0)
            update(manifestBytes)
            update(0)
            bindingBytes?.let {
                update(it)
                update(0)
            }
        }.digest().toHex()
    }

    private fun packageItemDigests(requireValidLock: Boolean): List<PackageItemDigest> {
        val validation = host.executionContext.compiler().validateRepositoryLock(host.repositoryRoot)
        val lock = validation.expectedLock
            ?: if (requireValidLock) {
                error("Cannot compute SourceRevision: expected athena-lock-v3 is unavailable.")
            } else {
                return emptyList()
            }
        check(!requireValidLock || validation.isValid) { "Cannot compute SourceRevision: athena-lock-v3 validation failed." }
        return lock.packageSnapshots.flatMap { locked ->
            val packageId = listOfNotNull(locked.packageId.name, locked.packageId.version).joinToString("@")
            locked.itemDigests.map { item ->
                PackageItemDigest(packageId, "${item.itemId}@${item.itemVersion}", item.digest.digestHex())
            }
        }.sortedBy(PackageItemDigest::key)
    }

    private fun contractResource(path: String): ByteArray = requireNotNull(AthenaDiagramScene::class.java.getResourceAsStream(path)) {
        "Missing committed Presentation contract resource `$path`."
    }.use { it.readBytes() }

    private fun normalizedIdentity(path: Path): ByteArray = path.toAbsolutePath().normalize().toString().replace('\\', '/').toByteArray(StandardCharsets.UTF_8)
}

private fun MessageDigest.updateRecord(value: String) {
    update(value.toByteArray(StandardCharsets.UTF_8))
    update(0)
}

private fun ByteArray.sha256Hex(): String = MessageDigest.getInstance("SHA-256").digest(this).toHex()
private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
private fun String.digestHex(): String = substringAfterLast(':').also { value ->
    require(Regex("^[0-9a-f]{64}$").matches(value)) { "Repository lock digest is not canonical SHA-256: `$this`." }
}
