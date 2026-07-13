package com.engineeringood.athena.domain.electricalruntime

import com.engineeringood.athena.connection.ResolvedSemanticPortDefinition
import com.engineeringood.athena.connection.SemanticPortDefinition
import com.engineeringood.athena.connection.SemanticPortDirection
import com.engineeringood.athena.connection.SemanticPortRoleId
import com.engineeringood.athena.connection.SemanticPortTypeId
import com.engineeringood.athena.connection.SemanticProtocolId
import com.engineeringood.athena.connection.SemanticSignalFamilyId
import com.engineeringood.athena.ir.StableSemanticIdentity
import com.engineeringood.athena.physical.PhysicalInstallationMarkerId
import com.engineeringood.athena.physical.PhysicalMountingTypeId
import com.engineeringood.athena.physical.PhysicalSize
import com.engineeringood.athena.physical.PhysicalTraitDefinition
import com.engineeringood.athena.physical.ResolvedPhysicalTraitDefinition

/**
 * First resolved semantic-port slice for the PLC CPU proof subject in M14.
 *
 * The slice stays extension-owned and intentionally narrow. It proves that resolved semantic ports
 * can publish reusable engineering meaning without pushing graph anchors or compatibility logic
 * back into the kernel contract.
 */
fun plcCpuResolvedSemanticPorts(): List<ResolvedSemanticPortDefinition> {
    val ownerSemanticId = StableSemanticIdentity("component:PLC1")
    return listOf(
        ResolvedSemanticPortDefinition(
            portSemanticId = StableSemanticIdentity("port:PLC1.lplus"),
            ownerSemanticId = ownerSemanticId,
            definition = SemanticPortDefinition(
                portTypeId = SemanticPortTypeId("electrical.power.dc24-output"),
                displayName = "24V DC positive supply",
                roleId = SemanticPortRoleId("l+"),
                direction = SemanticPortDirection.OUTPUT,
                signalFamilyId = SemanticSignalFamilyId("electrical.power"),
            ),
        ),
        ResolvedSemanticPortDefinition(
            portSemanticId = StableSemanticIdentity("port:PLC1.m"),
            ownerSemanticId = ownerSemanticId,
            definition = SemanticPortDefinition(
                portTypeId = SemanticPortTypeId("electrical.power.dc24-return"),
                displayName = "24V DC return",
                roleId = SemanticPortRoleId("m"),
                direction = SemanticPortDirection.PASSIVE,
                signalFamilyId = SemanticSignalFamilyId("electrical.power"),
            ),
        ),
        ResolvedSemanticPortDefinition(
            portSemanticId = StableSemanticIdentity("port:PLC1.pe"),
            ownerSemanticId = ownerSemanticId,
            definition = SemanticPortDefinition(
                portTypeId = SemanticPortTypeId("electrical.protection.earth"),
                displayName = "Protective earth",
                roleId = SemanticPortRoleId("pe"),
                direction = SemanticPortDirection.PASSIVE,
                signalFamilyId = SemanticSignalFamilyId("electrical.protection"),
            ),
        ),
        ResolvedSemanticPortDefinition(
            portSemanticId = StableSemanticIdentity("port:PLC1.mpi"),
            ownerSemanticId = ownerSemanticId,
            definition = SemanticPortDefinition(
                portTypeId = SemanticPortTypeId("electrical.communication.mpi"),
                displayName = "MPI communication port",
                roleId = SemanticPortRoleId("mpi"),
                direction = SemanticPortDirection.BIDIRECTIONAL,
                signalFamilyId = SemanticSignalFamilyId("electrical.communication"),
                protocolIds = setOf(SemanticProtocolId("mpi")),
            ),
        ),
    )
}

/**
 * First resolved physical-trait slice for the Siemens-first proof components in M14.
 *
 * The slice proves that at least one targeted component can publish minimal dimensions and
 * mounting metadata without making the physical model a geometry engine.
 */
fun siemensProofResolvedPhysicalTraits(): List<ResolvedPhysicalTraitDefinition> {
    return listOf(
        ResolvedPhysicalTraitDefinition(
            semanticSubjectId = StableSemanticIdentity("component:PLC1"),
            definition = PhysicalTraitDefinition(
                displayName = "Siemens proof PLC CPU physical traits",
                size = PhysicalSize(
                    widthMillimeters = 80,
                    heightMillimeters = 125,
                    depthMillimeters = 130,
                ),
                mountingTypeId = PhysicalMountingTypeId("din-rail"),
                installationMarkerIds = setOf(
                    PhysicalInstallationMarkerId("cabinet-interior"),
                    PhysicalInstallationMarkerId("front-clearance-required"),
                ),
                summary = "Proof-only Siemens-first PLC CPU physical traits.",
            ),
        ),
    )
}
