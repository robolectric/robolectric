package org.robolectric.util.inject;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Priority;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.util.inject.PluginFinder.ServiceFinderAdapter;

@RunWith(JUnit4.class)
public class PluginFinderTest {

  private final List<Class<?>> pluginClasses = new ArrayList<>();
  private PluginFinder pluginFinder;

  @Before
  public void setUp() throws Exception {
    pluginFinder = new PluginFinder(new MyServiceFinderAdapter(pluginClasses));
  }

  @Test
  public void findPlugin_shouldPickHighestPriorityClass() throws Exception {
    pluginClasses.addAll(asList(ImplMinus1.class, ImplZeroA.class, ImplOne.class, ImplZeroB.class));

    assertThat(pluginFinder.findPlugin(Iface.class))
        .isEqualTo(ImplOne.class);
  }

  @Test
  public void findPlugin_shouldThrowIfAmbiguous() throws Exception {
    pluginClasses.addAll(asList(ImplMinus1.class, ImplZeroA.class, ImplZeroB.class));

    try {
      pluginFinder.findPlugin(Iface.class);
      fail();
    } catch (Exception exception) {
      assertThat(exception).isInstanceOf(InjectionException.class);
    }
  }

  @Test
  public void findPlugins_shouldSortClassesInReversePriority() throws Exception {
    pluginClasses.addAll(asList(ImplMinus1.class, ImplZeroA.class, ImplOne.class, ImplZeroB.class));

    assertThat(pluginFinder.findPlugins(Iface.class))
        .containsExactly(ImplOne.class, ImplZeroA.class, ImplZeroB.class, ImplMinus1.class)
        .inOrder();
  }

  ////////////////

  @Priority(-1)
  private static class ImplMinus1 implements Iface {}

  @Priority(0)
  private static class ImplZeroA implements Iface {}

  @Priority(0)
  private static class ImplZeroB implements Iface {}

  @Priority(1)
  private static class ImplOne implements Iface {}

  private interface Iface {}

  private static class MyServiceFinderAdapter extends ServiceFinderAdapter {

    private List<Class<?>> pluginClasses;

    private MyServiceFinderAdapter(List<Class<?>> pluginClasses) {
      this.pluginClasses = pluginClasses;
    }

    @Nonnull
    @Override
    <T> Iterable<Class<? extends T>> load(Class<T> pluginType) {
      return fill();
    }

    @Nonnull
    @Override
    <T> Iterable<Class<? extends T>> load(Class<T> pluginType, ClassLoader classLoader) {
      return fill();
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    private <T> Iterable<Class<? extends T>> fill() {
      List<Class<? extends T>> classes = new ArrayList<>();
      for (Class<?> pluginClass : pluginClasses) {
        classes.add((Class<? extends T>) pluginClass);
      }
      return classes;
    }
  }
}
