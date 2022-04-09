package org.robolectric.util.inject

import com.google.common.truth.Truth
import javax.annotation.Priority
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

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
        ImplZeroB::class.java
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
        ImplZeroB::class.java
      )
    )
    Truth.assertThat(pluginFinder.findPlugins(Iface::class.java))
      .containsExactly(
        ImplOne::class.java,
        ImplZeroA::class.java,
        ImplZeroB::class.java,
        ImplMinus1::class.java
      )
      .inOrder()
  }

  @Test
  @Throws(Exception::class)
  fun findPlugins_whenAnnotatedSupercedes_shouldExcludeSuperceded() {
    pluginClasses.addAll(
      listOf(
        ImplMinus1::class.java,
        ImplZeroXSupercedesA::class.java,
        ImplZeroA::class.java,
        ImplOne::class.java,
        ImplZeroB::class.java
      )
    )
    val plugins = pluginFinder.findPlugins(Iface::class.java)
    Truth.assertThat(plugins)
      .containsExactly(
        ImplOne::class.java,
        ImplZeroB::class.java,
        ImplZeroXSupercedesA::class.java,
        ImplMinus1::class.java
      )
      .inOrder()
  }

  ////////////////
  @Priority(-1) private class ImplMinus1 : Iface

  @Priority(0) private class ImplZeroA : Iface
  private class ImplZeroB : Iface

  @Priority(1) private class ImplOne : Iface

  @Supercedes(ImplZeroA::class) private class ImplZeroXSupercedesA : Iface
  private interface Iface
}
