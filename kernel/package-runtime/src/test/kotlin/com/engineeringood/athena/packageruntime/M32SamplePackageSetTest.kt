package com.engineeringood.athena.packageruntime

import com.engineeringood.athena.packageplatform.PresentationProfileId
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class M32SamplePackageSetTest {
    @Test
    fun `sample project contains synthetic package inventory and athena owned source`() {
        val sample = M32SamplePackageSet.loadDefault()

        assertTrue(sample.engineeringPackages.size >= 3)
        assertTrue(sample.representationPackages.size >= 3)
        assertEquals(
            sample.engineeringPackages.map { it.packageId.value }.toSet(),
            sample.manifests.map { it.engineeringPackageId }.toSet(),
        )
        assertTrue(sample.sourceFile.exists())
        assertTrue(sample.readmeFile.exists())
        assertTrue(sample.projectRoot.resolve("athena.yaml").exists())
        assertTrue(sample.projectRoot.resolve("athena.lock").exists())
        assertTrue(sample.resourceFiles.all { it.exists() })
        assertTrue(sample.sourceFile.readText().contains("device ShutterMotorM32"))
        assertFalse(sample.sourceFile.readText().contains("qelectrotech", ignoreCase = true))
        assertFalse(sample.sourceFile.readText().contains("ABB", ignoreCase = true))
        assertFalse(sample.sourceFile.readText().contains("Siemens", ignoreCase = true))
    }

    @Test
    fun `profile switch changes representation facts without changing athena source`() {
        val sample = M32SamplePackageSet.loadDefault()
        val sourceBefore = sample.sourceFile.readText()

        val iec = sample.resolveSubject(
            semanticSubjectId = "device:ShutterMotorM32",
            profileId = PresentationProfileId("m32-iec"),
        )
        val compact = sample.resolveSubject(
            semanticSubjectId = "device:ShutterMotorM32",
            profileId = PresentationProfileId("m32-compact"),
        )

        assertTrue(iec.isValid)
        assertTrue(compact.isValid)
        assertEquals(sourceBefore, sample.sourceFile.readText())
        assertEquals(iec.resolution?.semanticSubjectId, compact.resolution?.semanticSubjectId)
        assertEquals(iec.resolution?.engineeringPackageId, compact.resolution?.engineeringPackageId)
        assertNotEquals(iec.resolution?.presentationProfileId, compact.resolution?.presentationProfileId)
        assertNotEquals(iec.resolution?.representationPackageId, compact.resolution?.representationPackageId)
        assertNotEquals(iec.resolution?.descriptorId, compact.resolution?.descriptorId)
        assertFalse(iec.rendererFallbackAccepted)
        assertFalse(compact.rendererFallbackAccepted)
    }
}
