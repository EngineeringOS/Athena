package com.engineeringood.athena.runtime

/**
 * Runtime-owned graph capability that derives queryable engineering-graph projections from active projects.
 */
class AthenaEngineeringGraphService internal constructor() {
    /**
     * Projects the supplied runtime-owned execution context into a graph view derived from the active canonical state.
     */
    fun projectProjection(context: AthenaExecutionContext): AthenaEngineeringGraphProjection {
        return context.compileActiveProject().toEngineeringGraphProjection(projectName = context.project.name)
    }
}
