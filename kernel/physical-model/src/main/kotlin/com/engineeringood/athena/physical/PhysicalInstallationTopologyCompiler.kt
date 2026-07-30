package com.engineeringood.athena.physical

object PhysicalInstallationTopologyCompiler {
    fun compile(
        intent: PhysicalInstallationIntent,
        contracts: List<PhysicalInstallationContract>,
    ): PhysicalInstallationTopologyCompilation {
        val diagnostics = mutableListOf<PhysicalInstallationTopologyDiagnostic>()
        val contractsBySubject = contracts.associateBy { contract -> contract.subjectIdentity }
        val enclosures = intent.enclosures.sortedBy { enclosure -> enclosure.id.value }
        val surfaces = intent.surfaces.sortedBy { surface -> surface.id.value }
        val rails = intent.rails.sortedBy { rail -> rail.id.value }
        val ducts = intent.ducts.sortedBy { duct -> duct.id.value }
        val channels = intent.channels.sortedBy { channel -> channel.id.value }
        val terminalGroups = intent.terminalGroups.sortedBy { group -> group.id.value }
        val mounts = intent.mounts.sortedBy { mount -> mount.occurrenceId.value }

        if (enclosures.size != 1) {
            diagnostics += diagnostic(
                code = "physical.topology.enclosure.count",
                subject = intent.installationId.value,
                measured = enclosures.size.toString(),
                expected = "exactly one enclosure",
            )
        }

        diagnoseDuplicateIds(
            listOf(
                enclosures.map { it.id to it.provenance },
                surfaces.map { it.id to it.provenance },
                rails.map { it.id to it.provenance },
                ducts.map { it.id to it.provenance },
                channels.map { it.id to it.provenance },
                terminalGroups.map { it.id to it.provenance },
                mounts.map { it.occurrenceId to it.provenance },
            ).flatten(),
            diagnostics,
        )

        val enclosureIds = enclosures.map { it.id }.toSet()
        val surfaceIds = surfaces.map { it.id }.toSet()
        val railIds = rails.map { it.id }.toSet()
        val ductIds = ducts.map { it.id }.toSet()
        val channelIds = channels.map { it.id }.toSet()
        val terminalGroupIds = terminalGroups.map { it.id }.toSet()

        surfaces.filterNot { surface -> surface.enclosureId in enclosureIds }.forEach { surface ->
            diagnostics += orphan(surface.id.value, surface.enclosureId.value, surface.provenance)
        }
        rails.filterNot { rail -> rail.surfaceId in surfaceIds }.forEach { rail ->
            diagnostics += orphan(rail.id.value, rail.surfaceId.value, rail.provenance)
        }
        ducts.filterNot { duct -> duct.enclosureId in enclosureIds }.forEach { duct ->
            diagnostics += orphan(duct.id.value, duct.enclosureId.value, duct.provenance)
        }
        channels.filterNot { channel -> channel.ductId in ductIds }.forEach { channel ->
            diagnostics += orphan(channel.id.value, channel.ductId.value, channel.provenance)
        }
        terminalGroups.filterNot { group -> group.enclosureId in enclosureIds }.forEach { group ->
            diagnostics += orphan(group.id.value, group.enclosureId.value, group.provenance)
        }

        val legalMountTargets = surfaceIds + railIds + terminalGroupIds
        val illegalMountTargets = enclosureIds + ductIds + channelIds
        mounts.forEach { mount ->
            when (mount.targetId) {
                in illegalMountTargets -> diagnostics += diagnostic(
                    code = "physical.topology.illegal_mount_target",
                    subject = mount.occurrenceId.value,
                    span = mount.provenance.span,
                    measured = mount.targetId.value,
                    expected = "surface, rail, or terminal group target",
                )
                !in legalMountTargets -> diagnostics += orphan(
                    subject = mount.occurrenceId.value,
                    parent = mount.targetId.value,
                    provenance = mount.provenance,
                )
            }
            if (contractsBySubject[mount.semanticSubjectId] == null) {
                diagnostics += diagnostic(
                    code = "physical.topology.contract.missing",
                    subject = mount.semanticSubjectId.value,
                    span = mount.provenance.span,
                    measured = mount.semanticSubjectId.value,
                    expected = "one resolved PhysicalInstallationContract",
                )
            }
        }

        mounts.groupBy { mount -> mount.semanticSubjectId }
            .filterValues { duplicates -> duplicates.size > 1 }
            .forEach { (subject, duplicates) ->
                diagnostics += diagnostic(
                    code = "physical.topology.duplicate_occurrence_subject",
                    subject = subject.value,
                    span = duplicates.first().provenance.span,
                    measured = duplicates.joinToString(",") { it.occurrenceId.value },
                    expected = "one mounted occurrence per semantic subject in one installation",
                )
            }

        intent.routes.forEach { route ->
            route.channelIds.filterNot { channelId -> channelId in channelIds }.forEach { missing ->
                diagnostics += diagnostic(
                    code = "physical.topology.route.channel_missing",
                    subject = route.connectionAlias,
                    span = route.provenance.span,
                    measured = missing.value,
                    expected = "declared route channel",
                )
            }
        }

        if (diagnostics.isNotEmpty()) {
            return PhysicalInstallationTopologyCompilation.Failure(
                diagnostics.sortedWith(compareBy({ it.code }, { it.subject }, { it.measured.orEmpty() })),
            )
        }

        val mountedOccurrences = mounts.map { mount ->
            val target = when (mount.targetId) {
                in surfaceIds -> PhysicalMountTargetRef.Surface(mount.targetId)
                in railIds -> PhysicalMountTargetRef.Rail(mount.targetId)
                else -> PhysicalMountTargetRef.TerminalGroup(mount.targetId)
            }
            PhysicalMountedOccurrence(
                occurrenceId = mount.occurrenceId,
                key = InstallationOccurrenceKey(
                    sourceUnitId = intent.sourceUnitId,
                    installationId = intent.installationId,
                    canonicalSemanticSubjectId = mount.semanticSubjectId,
                ),
                semanticSubjectId = mount.semanticSubjectId,
                target = target,
                at = mount.at,
                selectedOrientation = mount.selectedOrientation,
                contract = requireNotNull(contractsBySubject[mount.semanticSubjectId]),
                provenance = mount.provenance,
            )
        }

        val occurrenceByTerminalGroup = mountedOccurrences
            .filter { occurrence -> occurrence.target is PhysicalMountTargetRef.TerminalGroup }
            .groupBy { occurrence -> occurrence.target.id }

        return PhysicalInstallationTopologyCompilation.Success(
            PhysicalInstallationIR(
                sourceUnitId = intent.sourceUnitId,
                installationId = intent.installationId,
                space = PhysicalInstallationSpace(
                    enclosure = enclosures.single().toIr(),
                    surfaces = surfaces.map { surface -> surface.toIr() },
                    rails = rails.map { rail -> rail.toIr() },
                    ducts = ducts.map { duct -> duct.toIr() },
                    channels = channels.map { channel -> channel.toIr() },
                    terminalGroups = terminalGroups.map { group ->
                        group.toIr(
                            orderedOccurrenceKeys = occurrenceByTerminalGroup[group.id]
                                .orEmpty()
                                .sortedWith(group.terminalOccurrenceOrder())
                                .map { occurrence -> occurrence.key },
                        )
                    },
                    mountedOccurrences = mountedOccurrences,
                ),
                routes = intent.routes
                    .sortedBy { route -> route.connectionAlias }
                    .map { route -> route.toIr() },
            ),
        )
    }
}

private fun diagnoseDuplicateIds(
    ids: List<Pair<PhysicalObjectId, PhysicalSourceProvenance>>,
    diagnostics: MutableList<PhysicalInstallationTopologyDiagnostic>,
) {
    ids.groupBy { (id, _) -> id }
        .filterValues { duplicates -> duplicates.size > 1 }
        .forEach { (id, duplicates) ->
            diagnostics += diagnostic(
                code = "physical.topology.duplicate_id",
                subject = id.value,
                span = duplicates.first().second.span,
                measured = duplicates.size.toString(),
                expected = "unique physical object id",
            )
        }
}

private fun PhysicalEnclosureIntent.toIr(): PhysicalEnclosure = PhysicalEnclosure(
    id = id,
    size = size,
    provenance = provenance,
)

private fun PhysicalMountingSurfaceIntent.toIr(): PhysicalMountingSurface = PhysicalMountingSurface(
    id = id,
    enclosureId = enclosureId,
    at = at,
    size = size,
    acceptedMountingTypes = acceptedMountingTypes.toSortedSet(compareBy { it.value }),
    provenance = provenance,
)

private fun PhysicalRailIntent.toIr(): PhysicalRail = PhysicalRail(
    id = id,
    surfaceId = surfaceId,
    at = at,
    length = length,
    orientation = orientation,
    mountingType = mountingType,
    frame = when (orientation) {
        PhysicalInfrastructureOrientation.Horizontal -> PhysicalRigidFrame2i(
            origin = at,
            alongAxis = PhysicalVector2i(1, 0),
            normalAxis = PhysicalVector2i(0, 1),
        )
        PhysicalInfrastructureOrientation.Vertical -> PhysicalRigidFrame2i(
            origin = at,
            alongAxis = PhysicalVector2i(0, 1),
            normalAxis = PhysicalVector2i(-1, 0),
        )
    },
    provenance = provenance,
)

private fun PhysicalDuctIntent.toIr(): PhysicalDuct = PhysicalDuct(
    id = id,
    enclosureId = enclosureId,
    at = at,
    size = size,
    orientation = orientation,
    wall = wall,
    provenance = provenance,
)

private fun PhysicalRouteChannelIntent.toIr(): PhysicalRouteChannel = PhysicalRouteChannel(
    id = id,
    ductId = ductId,
    at = at,
    size = size,
    orientation = orientation,
    lanes = lanes,
    margin = margin,
    provenance = provenance,
)

private fun PhysicalTerminalGroupIntent.toIr(
    orderedOccurrenceKeys: List<InstallationOccurrenceKey>,
): PhysicalTerminalGroup = PhysicalTerminalGroup(
    id = id,
    enclosureId = enclosureId,
    at = at,
    size = size,
    orientation = orientation,
    acceptedMountingTypes = acceptedMountingTypes.toSortedSet(compareBy { it.value }),
    orderedOccurrenceKeys = orderedOccurrenceKeys,
    provenance = provenance,
)

private fun PhysicalTerminalGroupIntent.terminalOccurrenceOrder(): Comparator<PhysicalMountedOccurrence> =
    compareBy<PhysicalMountedOccurrence>(
        { it.at.x },
        { it.at.y },
        { it.key.canonicalSemanticSubjectId.value },
    )

private fun PhysicalRouteIntentSource.toIr(): PhysicalRouteIntent = PhysicalRouteIntent(
    connectionAlias = connectionAlias,
    channelIds = channelIds,
    provenance = provenance,
)

private fun orphan(
    subject: String,
    parent: String,
    provenance: PhysicalSourceProvenance,
): PhysicalInstallationTopologyDiagnostic = diagnostic(
    code = "physical.topology.orphan",
    subject = subject,
    span = provenance.span,
    measured = parent,
    expected = "declared typed parent",
)

private fun diagnostic(
    code: String,
    subject: String,
    span: PhysicalSourceSpan? = null,
    measured: String? = null,
    expected: String,
): PhysicalInstallationTopologyDiagnostic = PhysicalInstallationTopologyDiagnostic(
    code = code,
    subject = subject,
    span = span,
    measured = measured,
    expected = expected,
)
