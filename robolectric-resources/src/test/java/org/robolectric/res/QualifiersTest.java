package org.robolectric.res;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class QualifiersTest {

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
}
