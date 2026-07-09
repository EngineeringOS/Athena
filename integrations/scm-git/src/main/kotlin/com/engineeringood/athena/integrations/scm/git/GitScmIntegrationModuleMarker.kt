package com.engineeringood.athena.integrations.scm.git

/** Simple marker used to identify the first SCM integration module in verification flows. */
data class GitScmIntegrationModuleMarker(val moduleName: String = "integrations:scm-git")
