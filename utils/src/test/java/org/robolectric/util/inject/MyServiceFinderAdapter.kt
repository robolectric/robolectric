package org.robolectric.util.inject

import javax.annotation.Nonnull
import org.robolectric.util.inject.PluginFinder.ServiceFinderAdapter

internal class MyServiceFinderAdapter(private val pluginClasses: List<Class<*>>) :
  ServiceFinderAdapter(null) {
  @Nonnull
  public override fun <T> load(pluginType: Class<T>): Iterable<Class<out T>> {
    return fill()
  }

  @Nonnull
  private fun <T> fill(): Iterable<Class<out T>> {
    val classes: MutableList<Class<out T>> = ArrayList()
    for (pluginClass in pluginClasses) {
      classes.add(pluginClass as Class<out T>)
    }
    return classes
  }
}
