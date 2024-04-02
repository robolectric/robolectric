package org.robolectric.versioning;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Check versions information aligns with runtime information. Primarily, selected SDK with
 * internally detected version number.
 */
@RunWith(JUnit4.class)
public final class AndroidVersionsTest {

  @Test
  public void testStandardInitializationT() {
    assertThat(AndroidVersions.T.SDK_INT).isEqualTo(33);
    assertThat(AndroidVersions.T.SHORT_CODE).isEqualTo("T");
    assertThat(AndroidVersions.T.VERSION).isEqualTo("13.0");
    assertThat(new AndroidVersions.T().getSdkInt()).isEqualTo(33);
    assertThat(new AndroidVersions.T().getShortCode()).isEqualTo("T");
    assertThat(new AndroidVersions.T().getVersion()).isEqualTo("13.0");
    assertThat(new AndroidVersions.T().isReleased()).isEqualTo(true);
  }

  @Test
  public void testStandardInitializationSv2() {
    assertThat(AndroidVersions.Sv2.SDK_INT).isEqualTo(32);
    assertThat(AndroidVersions.Sv2.SHORT_CODE).isEqualTo("Sv2");
    assertThat(AndroidVersions.Sv2.VERSION).isEqualTo("12.1");
    assertThat(new AndroidVersions.Sv2().getSdkInt()).isEqualTo(32);
    assertThat(new AndroidVersions.Sv2().getShortCode()).isEqualTo("Sv2");
    assertThat(new AndroidVersions.Sv2().getVersion()).isEqualTo("12.1");
    assertThat(new AndroidVersions.Sv2().isReleased()).isEqualTo(true);
  }

  @Test
  public void testStandardInitializationS() {
    assertThat(AndroidVersions.S.SDK_INT).isEqualTo(31);
    assertThat(AndroidVersions.S.SHORT_CODE).isEqualTo("S");
    assertThat(AndroidVersions.S.VERSION).isEqualTo("12.0");
    assertThat(new AndroidVersions.S().getSdkInt()).isEqualTo(31);
    assertThat(new AndroidVersions.S().getShortCode()).isEqualTo("S");
    assertThat(new AndroidVersions.S().getVersion()).isEqualTo("12.0");
    assertThat(new AndroidVersions.S().isReleased()).isEqualTo(true);
  }

  @Test
  public void testStandardInitializationR() {
    assertThat(AndroidVersions.R.SDK_INT).isEqualTo(30);
    assertThat(AndroidVersions.R.SHORT_CODE).isEqualTo("R");
    assertThat(AndroidVersions.R.VERSION).isEqualTo("11.0");
    assertThat(new AndroidVersions.R().getSdkInt()).isEqualTo(30);
    assertThat(new AndroidVersions.R().getShortCode()).isEqualTo("R");
    assertThat(new AndroidVersions.R().getVersion()).isEqualTo("11.0");
    assertThat(new AndroidVersions.R().isReleased()).isEqualTo(true);
  }

  @Test
  public void testStandardInitializationQ() {
    assertThat(AndroidVersions.Q.SDK_INT).isEqualTo(29);
    assertThat(AndroidVersions.Q.SHORT_CODE).isEqualTo("Q");
    assertThat(AndroidVersions.Q.VERSION).isEqualTo("10.0");
    assertThat(new AndroidVersions.Q().getSdkInt()).isEqualTo(29);
    assertThat(new AndroidVersions.Q().getShortCode()).isEqualTo("Q");
    assertThat(new AndroidVersions.Q().getVersion()).isEqualTo("10.0");
    assertThat(new AndroidVersions.Q().isReleased()).isEqualTo(true);
  }

  @Test
  public void testStandardInitializationP() {
    assertThat(AndroidVersions.P.SDK_INT).isEqualTo(28);
    assertThat(AndroidVersions.P.SHORT_CODE).isEqualTo("P");
    assertThat(AndroidVersions.P.VERSION).isEqualTo("9.0");
    assertThat(new AndroidVersions.P().getSdkInt()).isEqualTo(28);
    assertThat(new AndroidVersions.P().getShortCode()).isEqualTo("P");
    assertThat(new AndroidVersions.P().getVersion()).isEqualTo("9.0");
    assertThat(new AndroidVersions.P().isReleased()).isEqualTo(true);
  }

  @Test
  public void testStandardInitializationOMR1() {
    assertThat(AndroidVersions.OMR1.SDK_INT).isEqualTo(27);
    assertThat(AndroidVersions.OMR1.SHORT_CODE).isEqualTo("OMR1");
    assertThat(AndroidVersions.OMR1.VERSION).isEqualTo("8.1");
    assertThat(new AndroidVersions.OMR1().getSdkInt()).isEqualTo(27);
    assertThat(new AndroidVersions.OMR1().getShortCode()).isEqualTo("OMR1");
    assertThat(new AndroidVersions.OMR1().getVersion()).isEqualTo("8.1");
    assertThat(new AndroidVersions.OMR1().isReleased()).isEqualTo(true);
  }

  @Test
  public void testStandardInitializationO() {
    assertThat(AndroidVersions.O.SDK_INT).isEqualTo(26);
    assertThat(AndroidVersions.O.SHORT_CODE).isEqualTo("O");
    assertThat(AndroidVersions.O.VERSION).isEqualTo("8.0");
    assertThat(new AndroidVersions.O().getSdkInt()).isEqualTo(26);
    assertThat(new AndroidVersions.O().getShortCode()).isEqualTo("O");
    assertThat(new AndroidVersions.O().getVersion()).isEqualTo("8.0");
    assertThat(new AndroidVersions.O().isReleased()).isEqualTo(true);
  }

  @Test
  public void testStandardInitializationNMR1() {
    assertThat(AndroidVersions.NMR1.SDK_INT).isEqualTo(25);
    assertThat(AndroidVersions.NMR1.SHORT_CODE).isEqualTo("NMR1");
    assertThat(AndroidVersions.NMR1.VERSION).isEqualTo("7.1");
    assertThat(new AndroidVersions.NMR1().getSdkInt()).isEqualTo(25);
    assertThat(new AndroidVersions.NMR1().getShortCode()).isEqualTo("NMR1");
    assertThat(new AndroidVersions.NMR1().getVersion()).isEqualTo("7.1");
    assertThat(new AndroidVersions.NMR1().isReleased()).isEqualTo(true);
  }

  @Test
  public void testStandardInitializationN() {
    assertThat(AndroidVersions.N.SDK_INT).isEqualTo(24);
    assertThat(AndroidVersions.N.SHORT_CODE).isEqualTo("N");
    assertThat(AndroidVersions.N.VERSION).isEqualTo("7.0");
    assertThat(new AndroidVersions.N().getSdkInt()).isEqualTo(24);
    assertThat(new AndroidVersions.N().getShortCode()).isEqualTo("N");
    assertThat(new AndroidVersions.N().getVersion()).isEqualTo("7.0");
    assertThat(new AndroidVersions.N().isReleased()).isEqualTo(true);
  }

  @Test
  public void testStandardInitializationM() {
    assertThat(AndroidVersions.M.SDK_INT).isEqualTo(23);
    assertThat(AndroidVersions.M.SHORT_CODE).isEqualTo("M");
    assertThat(AndroidVersions.M.VERSION).isEqualTo("6.0");
    assertThat(new AndroidVersions.M().getSdkInt()).isEqualTo(23);
    assertThat(new AndroidVersions.M().getShortCode()).isEqualTo("M");
    assertThat(new AndroidVersions.M().getVersion()).isEqualTo("6.0");
    assertThat(new AndroidVersions.M().isReleased()).isEqualTo(true);
  }

  @Test
  public void testStandardInitializationLMR1() {
    assertThat(AndroidVersions.LMR1.SDK_INT).isEqualTo(22);
    assertThat(AndroidVersions.LMR1.SHORT_CODE).isEqualTo("LMR1");
    assertThat(AndroidVersions.LMR1.VERSION).isEqualTo("5.1");
    assertThat(new AndroidVersions.LMR1().getSdkInt()).isEqualTo(22);
    assertThat(new AndroidVersions.LMR1().getShortCode()).isEqualTo("LMR1");
    assertThat(new AndroidVersions.LMR1().getVersion()).isEqualTo("5.1");
    assertThat(new AndroidVersions.LMR1().isReleased()).isEqualTo(true);
  }

  @Test
  public void testStandardInitializationL() {
    assertThat(AndroidVersions.L.SDK_INT).isEqualTo(21);
    assertThat(AndroidVersions.L.SHORT_CODE).isEqualTo("L");
    assertThat(AndroidVersions.L.VERSION).isEqualTo("5.0");
    assertThat(new AndroidVersions.L().getSdkInt()).isEqualTo(21);
    assertThat(new AndroidVersions.L().getShortCode()).isEqualTo("L");
    assertThat(new AndroidVersions.L().getVersion()).isEqualTo("5.0");
    assertThat(new AndroidVersions.L().isReleased()).isEqualTo(true);
  }

  @Test
  public void testStandardInitializationK() {
    assertThat(AndroidVersions.K.SDK_INT).isEqualTo(19);
    assertThat(AndroidVersions.K.SHORT_CODE).isEqualTo("K");
    assertThat(AndroidVersions.K.VERSION).isEqualTo("4.4");
    assertThat(new AndroidVersions.K().getSdkInt()).isEqualTo(19);
    assertThat(new AndroidVersions.K().getShortCode()).isEqualTo("K");
    assertThat(new AndroidVersions.K().getVersion()).isEqualTo("4.4");
    assertThat(new AndroidVersions.K().isReleased()).isEqualTo(true);
  }
}
