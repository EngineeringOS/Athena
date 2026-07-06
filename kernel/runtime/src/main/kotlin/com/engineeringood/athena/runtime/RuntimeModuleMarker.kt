package com.engineeringood.athena.runtime

/** Simple marker used by bootstrap flows to identify the runtime module. */
data class RuntimeModuleMarker(val moduleName: String = "kernel:runtime")
