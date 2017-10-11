package org.robolectric.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Plugin;
import org.robolectric.Plugin.UnsuitablePluginException;

public class PluginLoaderTest {

  private PluginLoader<SomePlugin> pluginLoader;
  private List<SomePlugin> plugins;

  @Before
  public void setUp() throws Exception {
    plugins = new ArrayList<>();

    pluginLoader = new PluginLoader<SomePlugin>(SomePlugin.class) {
      @Override
      Iterable<SomePlugin> loadServices(Class<SomePlugin> pluginClass) {
        return plugins;
      }
    };
  }

  @Test
  public void invoke_shouldUseHighestSuitablePriorityPlugin_Lower() throws Exception {
    plugins.add(new MyPlugin(Plugin.DEFAULT_PLUGIN_PRIORITY, "default", false));
    plugins.add(new MyPlugin(1, "higher", false));
    plugins.add(new MyPlugin(-1, "lower", true));

    String result = pluginLoader.invoke(plugin -> plugin.getString("the arg"));
    assertThat(result).isEqualTo("lower(the arg)");
  }

  @Test
  public void invoke_shouldUseHighestSuitablePriorityPlugin() throws Exception {
    plugins.add(new MyPlugin(Plugin.DEFAULT_PLUGIN_PRIORITY, "default", true));
    plugins.add(new MyPlugin(1, "higher", true));
    plugins.add(new MyPlugin(-1, "lower", true));

    String result = pluginLoader.invoke(plugin -> plugin.getString("the arg"));
    assertThat(result).isEqualTo("higher(the arg)");
  }

  @Test
  public void invoke_shouldThrowWhenNoPluginSuitable() throws Exception {
    plugins.add(new MyPlugin(Plugin.DEFAULT_PLUGIN_PRIORITY, "default", false));
    plugins.add(new MyPlugin(1, "higher", false));
    plugins.add(new MyPlugin(-1, "lower", false));

    try {
      pluginLoader.invoke(plugin -> plugin.getString("the arg"));
      fail("should fail with UnsuitablePluginException");
    } catch (UnsuitablePluginException e) {
      // expected
    }
  }

  interface SomePlugin extends Plugin {
    String getString(String arg);
  }

  class MyPlugin implements SomePlugin {

    private final int priority;
    private final String message;
    private final boolean isSuitable;

    MyPlugin(int priority, String message, boolean isSuitable) {
      this.priority = priority;
      this.message = message;
      this.isSuitable = isSuitable;
    }

    @Override
    public float getPriority() {
      return priority;
    }

    @Override
    public String getString(String arg) {
      if (!isSuitable) {
        throw new UnsuitablePluginException();
      }
      return message + "(" + arg + ")";
    }
  }

}