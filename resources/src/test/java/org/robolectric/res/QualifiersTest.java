package org.robolectric.res;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.res.android.ResTable_config;

@RunWith(JUnit4.class)
public class QualifiersTest {
  @Test
  public void testQualifiers() {
    assertThat(configFrom("values-land-finger")).isEqualTo("land-finger");
  }

  @Test
  public void testWhenQualifiersFailToParse() {
    try {
      configFrom("values-unknown-v23");
      fail("Expected exception");
    } catch (IllegalArgumentException expected) {
      assertThat(expected.getMessage()).contains("failed to parse qualifiers 'unknown-v23");
    }
  }

  private String configFrom(String path) {
    Path xmlFile = Paths.get(path, "whatever.xml");
    Qualifiers qualifiers = Qualifiers.fromParentDir(xmlFile.getParent());

    ResTable_config config = new XmlContext("package", xmlFile, qualifiers).getConfig();
    return config.toString();
  }
}
