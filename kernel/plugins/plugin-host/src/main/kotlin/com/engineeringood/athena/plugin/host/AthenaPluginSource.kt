package com.engineeringood.athena.plugin.host

import com.engineeringood.athena.plugin.AthenaPlugin
import java.util.ServiceLoader

/** Core-owned source of plugin implementations available to the hosted plugin layer. */
interface AthenaPluginSource {
    /** Loads the currently available plugin implementations from the configured source. */
    fun loadPlugins(): List<AthenaPlugin>
}

/** JVM-first source that discovers Athena plugins from `ServiceLoader` on the local classpath. */
class ServiceLoaderAthenaPluginSource(
    private val classLoader: ClassLoader = Thread.currentThread().contextClassLoader
        ?: ServiceLoaderAthenaPluginSource::class.java.classLoader,
) : AthenaPluginSource {
    override fun loadPlugins(): List<AthenaPlugin> {
        return ServiceLoader.load(AthenaPlugin::class.java, classLoader).toList()
    }
}
