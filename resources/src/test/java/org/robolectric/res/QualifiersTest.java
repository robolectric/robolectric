package org.robolectric.res;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.res.android.ResTable_config;

@RunWith(JUnit4.class)
public class QualifiersTest {
  @Test
  public void testQualifiers() throws Exception {
    assertThat(configFrom("values-land-finger")).isEqualTo("land-finger");
  }

  @Test
  public void testWhenQualifiersFailToParse() throws Exception {
    try {
      configFrom("values-unknown-v23");
      fail("Expected exception");
    } catch(IllegalArgumentException expected) {
      assertThat(expected.getMessage()).contains("failed to parse qualifiers 'unknown-v23");
    }
  }

  private String configFrom(String path) {
    FsFile xmlFile = Fs.newFile(path + "/whatever.xml");
    Qualifiers qualifiers = Qualifiers.fromParentDir(xmlFile.getParent());

    ResTable_config config = new XmlContext("package", xmlFile, qualifiers)
        .getConfig();
    return config.toString();
  }

  ///////// deprecated stuff...

  @Test public void addPlatformVersion() throws Exception {
    assertThat(Qualifiers.addPlatformVersion("", 21)).isEqualTo("v21");
    assertThat(Qualifiers.addPlatformVersion("v23", 21)).isEqualTo("v23");
    assertThat(Qualifiers.addPlatformVersion("foo-v14", 21)).isEqualTo("foo-v14");
  }

  @Test public void addSmallestScreenWidth() throws Exception {
    assertThat(Qualifiers.addSmallestScreenWidth("", 320)).isEqualTo("sw320dp");
    assertThat(Qualifiers.addSmallestScreenWidth("sw160dp", 320)).isEqualTo("sw160dp");
    assertThat(Qualifiers.addSmallestScreenWidth("sw480dp", 320)).isEqualTo("sw480dp");
    assertThat(Qualifiers.addSmallestScreenWidth("en-v23", 320)).isEqualTo("en-v23-sw320dp"); // todo: order is wrong here
    assertThat(Qualifiers.addSmallestScreenWidth("en-sw160dp-v23", 320)).isEqualTo("en-sw160dp-v23");
    assertThat(Qualifiers.addSmallestScreenWidth("en-sw480dp-v23", 320)).isEqualTo("en-sw480dp-v23");
  }

  @Test public void addScreenWidth() throws Exception {
    assertThat(Qualifiers.addScreenWidth("", 320)).isEqualTo("w320dp");
    assertThat(Qualifiers.addScreenWidth("w160dp", 320)).isEqualTo("w160dp");
    assertThat(Qualifiers.addScreenWidth("w480dp", 320)).isEqualTo("w480dp");
    assertThat(Qualifiers.addScreenWidth("en-v23", 320)).isEqualTo("en-v23-w320dp"); // todo: order is wrong here
    assertThat(Qualifiers.addScreenWidth("en-w160dp-v23", 320)).isEqualTo("en-w160dp-v23");
    assertThat(Qualifiers.addScreenWidth("en-w480dp-v23", 320)).isEqualTo("en-w480dp-v23");
  }

  @Test public void getSmallestScreenWidth() {
    assertThat(Qualifiers.getSmallestScreenWidth("sw320dp")).isEqualTo(320);
    assertThat(Qualifiers.getSmallestScreenWidth("sw320dp-v7")).isEqualTo(320);
    assertThat(Qualifiers.getSmallestScreenWidth("en-rUS-sw320dp")).isEqualTo(320);
    assertThat(Qualifiers.getSmallestScreenWidth("en-rUS-sw320dp-v7")).isEqualTo(320);
    assertThat(Qualifiers.getSmallestScreenWidth("en-rUS-v7")).isEqualTo(-1);
    assertThat(Qualifiers.getSmallestScreenWidth("en-rUS-w320dp-v7")).isEqualTo(-1);
  }

  @Test public void getAddSmallestScreenWidth() {
    assertThat(Qualifiers.addSmallestScreenWidth("v7", 320)).isEqualTo("v7-sw320dp");
    assertThat(Qualifiers.addSmallestScreenWidth("sw320dp-v7", 480)).isEqualTo("sw320dp-v7");
  }

  @Test public void getScreenWidth() {
    assertThat(Qualifiers.getScreenWidth("w320dp")).isEqualTo(320);
    assertThat(Qualifiers.getScreenWidth("w320dp-v7")).isEqualTo(320);
    assertThat(Qualifiers.getScreenWidth("en-rUS-w320dp")).isEqualTo(320);
    assertThat(Qualifiers.getScreenWidth("en-rUS-w320dp-v7")).isEqualTo(320);
    assertThat(Qualifiers.getScreenWidth("en-rUS-v7")).isEqualTo(-1);
    assertThat(Qualifiers.getScreenWidth("de-v23-sw320dp-w1024dp")).isEqualTo(1024);
    assertThat(Qualifiers.getScreenWidth("en-rUS-sw320dp-v7")).isEqualTo(-1);
  }

  @Test public void getAddScreenWidth() {
    assertThat(Qualifiers.addScreenWidth("v7", 320)).isEqualTo("v7-w320dp");
    assertThat(Qualifiers.addScreenWidth("w320dp-v7", 480)).isEqualTo("w320dp-v7");
  }

  @Test public void getOrientation() {
    assertThat(Qualifiers.getOrientation("land")).isEqualTo("land");
    assertThat(Qualifiers.getOrientation("en-rUs-land")).isEqualTo("land");
    assertThat(Qualifiers.getOrientation("port")).isEqualTo("port");
    assertThat(Qualifiers.getOrientation("port-v7")).isEqualTo("port");
  }
}
