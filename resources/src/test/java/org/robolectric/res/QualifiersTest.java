package org.robolectric.res;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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

  @Test public void getScreenWidth() {
    assertThat(Qualifiers.getScreenWidth("w320dp")).isEqualTo(320);
    assertThat(Qualifiers.getScreenWidth("w320dp-v7")).isEqualTo(320);
    assertThat(Qualifiers.getScreenWidth("en-rUS-w320dp")).isEqualTo(320);
    assertThat(Qualifiers.getScreenWidth("en-rUS-w320dp-v7")).isEqualTo(320);
    assertThat(Qualifiers.getScreenWidth("en-rUS-v7")).isEqualTo(-1);
    assertThat(Qualifiers.getScreenWidth("de-v23-sw320dp-w1024dp")).isEqualTo(1024);
    assertThat(Qualifiers.getScreenWidth("en-rUS-sw320dp-v7")).isEqualTo(-1);
  }

  @Test public void getOrientation() {
    assertThat(Qualifiers.getOrientation("land")).isEqualTo("land");
    assertThat(Qualifiers.getOrientation("en-rUs-land")).isEqualTo("land");
    assertThat(Qualifiers.getOrientation("port")).isEqualTo("port");
    assertThat(Qualifiers.getOrientation("port-v7")).isEqualTo("port");
  }
}
