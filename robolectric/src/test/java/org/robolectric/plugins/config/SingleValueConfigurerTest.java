package org.robolectric.plugins.config;

import static com.google.common.truth.Truth.assertThat;

import java.util.Properties;
import javax.annotation.Nonnull;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.plugins.PackagePropertiesLoader;

@RunWith(JUnit4.class)
public final class SingleValueConfigurerTest {

  public enum Value {
    ON,
    OFF,
  }

  public @interface ValueConfig {
    Value value() default Value.ON;
  }

  public static class ValueConfigReader extends SingleValueConfigurer<ValueConfig, Value> {
    public ValueConfigReader(
        Properties systemProps, PackagePropertiesLoader ppl, Value defaultValue) {
      super(ValueConfig.class, Value.class, defaultValue, ppl, systemProps);
    }
  }

  public static class OverridePackagePropertiesLoader extends PackagePropertiesLoader {
    private final Properties propsToFind;

    public OverridePackagePropertiesLoader(Properties propsToFind) {
      this.propsToFind = propsToFind;
    }

    @Override
    public Properties getConfigProperties(@Nonnull String packageName) {
      return propsToFind;
    }
  }

  @Test
  public void testReadingPropertyFile() {
    String valueName = "valueConfig";
    Properties props = new Properties();
    props.setProperty(valueName, Value.OFF.name());
    PackagePropertiesLoader ppl = new OverridePackagePropertiesLoader(props);
    Properties systemProps = new Properties();
    systemProps.setProperty(valueName, Value.ON.name());
    ValueConfigReader reader = new ValueConfigReader(systemProps, ppl, Value.ON);
    assertThat(reader.propertyName()).isEqualTo(valueName);
    assertThat(reader.defaultConfig()).isEqualTo(Value.ON);
    assertThat(reader.getConfigFor("foo")).isEqualTo(Value.OFF);
  }

  @Test
  public void testNonStandardCasing() {
    String valueName = "valueConfig";
    Properties props = new Properties();
    props.setProperty(valueName, "oFF");
    PackagePropertiesLoader ppl = new OverridePackagePropertiesLoader(props);
    Properties systemProps = new Properties();
    systemProps.setProperty(valueName, "On");
    ValueConfigReader reader = new ValueConfigReader(systemProps, ppl, Value.ON);
    assertThat(reader.propertyName()).isEqualTo(valueName);
    assertThat(reader.defaultConfig()).isEqualTo(Value.ON);
    assertThat(reader.getConfigFor("foo")).isEqualTo(Value.OFF);
  }

  @Test
  public void testIllegalValueThrows() {
    String valueName = "valueConfig";
    Properties props = new Properties();
    props.setProperty(valueName, "none");
    PackagePropertiesLoader ppl = new OverridePackagePropertiesLoader(props);
    Properties systemProps = new Properties();
    systemProps.setProperty(valueName, "none");
    ValueConfigReader reader = new ValueConfigReader(systemProps, ppl, Value.ON);
    Assert.assertThrows(IllegalArgumentException.class, () -> reader.getConfigFor("foo"));
  }

  @Test
  public void testReadingPropertyFileFlippedDefaultValue() {
    String valueName = "valueConfig";
    Properties props = new Properties();
    props.setProperty(valueName, Value.ON.name());
    PackagePropertiesLoader ppl = new OverridePackagePropertiesLoader(props);
    Properties systemProps = new Properties();
    systemProps.setProperty(valueName, Value.OFF.name());
    ValueConfigReader reader = new ValueConfigReader(systemProps, ppl, Value.OFF);
    assertThat(reader.propertyName()).isEqualTo(valueName);
    assertThat(reader.defaultConfig()).isEqualTo(Value.OFF);
    assertThat(reader.getConfigFor("foo")).isEqualTo(Value.ON);
  }

  @Test
  public void testCachesPackageInfo() {
    SingleValueConfigurer.PACKAGE_INFO_CACHE.clear();

    PackagePropertiesLoader ppl = new OverridePackagePropertiesLoader(new Properties());
    ValueConfigReader reader = new ValueConfigReader(new Properties(), ppl, Value.ON);

    String nonexistentPkg = "org.robolectric.nonexistent.pkg";
    assertThat(SingleValueConfigurer.PACKAGE_INFO_CACHE.containsKey(nonexistentPkg)).isFalse();
    reader.getConfigFor(nonexistentPkg);
    assertThat(SingleValueConfigurer.PACKAGE_INFO_CACHE.containsKey(nonexistentPkg)).isTrue();
    assertThat(SingleValueConfigurer.PACKAGE_INFO_CACHE.get(nonexistentPkg)).isNull();

    String existingPkg = "org.robolectric.plugins.config";
    assertThat(SingleValueConfigurer.PACKAGE_INFO_CACHE.containsKey(existingPkg)).isFalse();
    reader.getConfigFor(existingPkg);
    assertThat(SingleValueConfigurer.PACKAGE_INFO_CACHE.containsKey(existingPkg)).isTrue();
    assertThat(SingleValueConfigurer.PACKAGE_INFO_CACHE.get(existingPkg)).isNotNull();
  }
}
