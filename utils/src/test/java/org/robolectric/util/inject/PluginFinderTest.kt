package org.robolectric.util.inject

import com.google.common.truth.Truth
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.Collections
import java.util.Enumeration
import javax.annotation.Priority
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.robolectric.util.inject.PluginFinder.ServiceFinderAdapter

@RunWith(JUnit4::class)
class PluginFinderTest {
  private val pluginClasses: MutableList<Class<*>> = ArrayList()
  private lateinit var pluginFinder: PluginFinder

  @Before
  @Throws(Exception::class)
  fun setUp() {
    pluginFinder = PluginFinder(MyServiceFinderAdapter(pluginClasses))
  }

  @Test
  @Throws(Exception::class)
  fun findPlugin_shouldPickHighestPriorityClass() {
    pluginClasses.addAll(
      listOf(
        ImplMinus1::class.java,
        ImplZeroA::class.java,
        ImplOne::class.java,
        ImplZeroB::class.java,
      )
    )
    Truth.assertThat(pluginFinder.findPlugin(Iface::class.java)).isEqualTo(ImplOne::class.java)
  }

  @Test
  @Throws(Exception::class)
  fun findPlugin_shouldThrowIfAmbiguous() {
    pluginClasses.addAll(
      listOf(ImplMinus1::class.java, ImplZeroA::class.java, ImplZeroB::class.java)
    )
    try {
      pluginFinder.findPlugin(Iface::class.java)
      Assert.fail()
    } catch (exception: Exception) {
      Truth.assertThat(exception).isInstanceOf(InjectionException::class.java)
    }
  }

  @Test
  @Throws(Exception::class)
  fun findPlugins_shouldSortClassesInReversePriority() {
    pluginClasses.addAll(
      listOf(
        ImplMinus1::class.java,
        ImplZeroA::class.java,
        ImplOne::class.java,
        ImplZeroB::class.java,
      )
    )
    Truth.assertThat(pluginFinder.findPlugins(Iface::class.java))
      .containsExactly(
        ImplOne::class.java,
        ImplZeroA::class.java,
        ImplZeroB::class.java,
        ImplMinus1::class.java,
      )
      .inOrder()
  }

  @Test
  @Throws(Exception::class)
  fun findPlugins_whenAnnotatedSupersedes_shouldExcludeSuperseded() {
    pluginClasses.addAll(
      listOf(
        ImplMinus1::class.java,
        ImplZeroXSupersedesA::class.java,
        ImplZeroA::class.java,
        ImplOne::class.java,
        ImplZeroB::class.java,
      )
    )
    val plugins = pluginFinder.findPlugins(Iface::class.java)
    Truth.assertThat(plugins)
      .containsExactly(
        ImplOne::class.java,
        ImplZeroB::class.java,
        ImplZeroXSupersedesA::class.java,
        ImplMinus1::class.java,
      )
      .inOrder()
  }

  @Test
  fun load_whenNoPluginsAreDeclared_shouldScanClasspathOnlyOnce() {
    val classLoader = FakeServiceClassLoader(javaClass.classLoader, emptyMap())
    val adapter = ServiceFinderAdapter(classLoader)

    Truth.assertThat(adapter.load(Iface::class.java)).isEmpty()
    Truth.assertThat(adapter.load(Iface::class.java)).isEmpty()
    Truth.assertThat(adapter.load(Iface::class.java)).isEmpty()

    // The dominant case: nothing is declared, but without caching every lookup rescans the
    // whole classpath looking for a service file that isn't there.
    Truth.assertThat(classLoader.getResourcesCalls).isEqualTo(1)
  }

  @Test
  fun load_shouldScanClasspathOncePerDistinctPluginType() {
    val classLoader = FakeServiceClassLoader(javaClass.classLoader, emptyMap())
    val adapter = ServiceFinderAdapter(classLoader)

    adapter.load(Iface::class.java)
    adapter.load(Thing::class.java)
    adapter.load(Iface::class.java)
    adapter.load(Thing::class.java)

    Truth.assertThat(classLoader.getResourcesCalls).isEqualTo(2)
  }

  @Test
  fun load_whenPluginIsDeclared_shouldKeepResolvingItFromCache() {
    val classLoader =
      FakeServiceClassLoader(
        javaClass.classLoader,
        mapOf("META-INF/services/" + Iface::class.java.name to ImplOne::class.java.name),
      )
    val adapter = ServiceFinderAdapter(classLoader)

    Truth.assertThat(adapter.load(Iface::class.java)).containsExactly(ImplOne::class.java)
    Truth.assertThat(adapter.load(Iface::class.java)).containsExactly(ImplOne::class.java)

    Truth.assertThat(classLoader.getResourcesCalls).isEqualTo(1)
  }

  /** A ClassLoader that serves the given service files and counts classpath scans. */
  private class FakeServiceClassLoader(
    parent: ClassLoader,
    private val serviceFiles: Map<String, String>,
  ) : ClassLoader(parent) {
    var getResourcesCalls = 0
      private set

    override fun getResources(name: String): Enumeration<URL> {
      getResourcesCalls++
      val contents = serviceFiles[name] ?: return Collections.emptyEnumeration()
      val file = Files.createTempFile("robolectric-services", ".txt")
      file.toFile().deleteOnExit()
      Files.write(file, contents.toByteArray(StandardCharsets.UTF_8))
      return Collections.enumeration(listOf(file.toUri().toURL()))
    }
  }

  ////////////////
  @Priority(-1) private class ImplMinus1 : Iface

  @Priority(0) private class ImplZeroA : Iface

  private class ImplZeroB : Iface

  @Priority(1) private class ImplOne : Iface

  @Supersedes(ImplZeroA::class) private class ImplZeroXSupersedesA : Iface

  private interface Iface
}
