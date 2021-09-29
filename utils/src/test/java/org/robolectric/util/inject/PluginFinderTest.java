package org.robolectric.util.inject;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Priority;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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

  @Test
  public void findPlugins_whenAnnotatedSupercedes_shouldExcludeSuperceded() throws Exception {
    pluginClasses.addAll(
        asList(ImplMinus1.class, ImplZeroXSupercedesA.class, ImplZeroA.class, ImplOne.class,
            ImplZeroB.class));

    List<Class<? extends Iface>> plugins = pluginFinder.findPlugins(Iface.class);
    assertThat(plugins)
        .containsExactly(ImplOne.class, ImplZeroB.class, ImplZeroXSupercedesA.class,
            ImplMinus1.class)
        .inOrder();
  }

  ////////////////

  @Priority(-1)
  private static class ImplMinus1 implements Iface {}

  @Priority(0)
  private static class ImplZeroA implements Iface {}

  private static class ImplZeroB implements Iface {}

  @Priority(1)
  private static class ImplOne implements Iface {}

  @Supersedes(ImplZeroA.class)
  private static class ImplZeroXSupercedesA implements Iface {}

  private interface Iface {}

}
