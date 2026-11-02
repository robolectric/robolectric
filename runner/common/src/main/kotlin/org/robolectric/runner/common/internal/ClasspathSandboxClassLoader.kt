package org.robolectric.runner.common.internal

import java.io.InputStream
import java.net.URL
import java.nio.file.Path
import java.util.Collections
import java.util.Enumeration
import org.robolectric.internal.AndroidSandbox
import org.robolectric.internal.bytecode.ClassInstrumentor
import org.robolectric.internal.bytecode.InstrumentationConfiguration
import org.robolectric.internal.bytecode.UrlResourceProvider
import org.robolectric.pluginapi.Sdk

/**
 * A sandbox class loader that finds classes and resources in jars and directories of its own,
 * before those of the class path: for an app whose classes are not on the class path.
 */
internal class ClasspathSandboxClassLoader(
  config: InstrumentationConfiguration,
  sdk: Sdk,
  instrumentor: ClassInstrumentor,
  classpath: List<Path>,
) : AndroidSandbox.SdkSandboxClassLoader(config, sdk, instrumentor) {
  private val entries = UrlResourceProvider(*classpath.map { it.toUri().toURL() }.toTypedArray())

  override fun getClassBytesFromAlternateClassLoader(classResName: String): InputStream? =
    entries.getResourceAsStream(classResName)

  override fun getResourceUrl(name: String): URL? =
    entries.getResource(name) ?: super.getResourceUrl(name)

  override fun findResources(name: String): Enumeration<URL> =
    Collections.enumeration(
      Collections.list(entries.findResources(name)) + Collections.list(super.findResources(name))
    )
}
