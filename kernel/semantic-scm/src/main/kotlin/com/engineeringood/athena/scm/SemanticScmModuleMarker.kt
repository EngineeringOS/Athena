package com.engineeringood.athena.scm

/** Simple marker used to identify the semantic SCM contract module in bootstrap and verification flows. */
data class SemanticScmModuleMarker(val moduleName: String = "kernel:semantic-scm")
