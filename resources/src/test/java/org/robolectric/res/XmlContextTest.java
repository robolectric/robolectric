package org.robolectric.res;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.res.android.ResTable_config;

@RunWith(JUnit4.class)
public class XmlContextTest {

  @Test
  public void testQualifiers() throws Exception {
    assertThat(configFrom("values-land-finger")).isEqualTo("land-finger");
  }

  @Test
  public void testVersionFallbackWhenQualifiersFailToParse() throws Exception {
    assertThat(configFrom("values-unknown-v30")).isEqualTo("v30");
  }

  private String configFrom(String path) {
    ResTable_config config = new XmlContext("package", Fs.newFile(path + "/whatever.xml")).getConfig();
    return config.toString();
  }
}