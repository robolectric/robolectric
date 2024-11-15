package org.robolectric.versioning;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowBuild;

/**
 * Check versions information aligns with runtime information. Primarily, selected SDK with
 * internally detected version number.
 */
@RunWith(RobolectricTestRunner.class)
public final class AndroidVersionsTest {

  @Test
  @Config(sdk = 33)
  public void ignoreShadowBuildValues() {
    ShadowBuild.setVersionCodename("_*&^%%&");
    assertThat(AndroidVersions.T.SDK_INT).isEqualTo(33);
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("T");
  }

  @Test
  @Config(sdk = 35)
  public void testStandardInitializationV() {
    assertThat(AndroidVersions.V.SDK_INT).isEqualTo(35);
    assertThat(AndroidVersions.V.SHORT_CODE).isEqualTo("V");
    assertThat(new AndroidVersions.V().getVersion()).isEqualTo("15");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("V");
  }

  @Test
  @Config(sdk = 34)
  public void testStandardInitializationU() {
    assertThat(AndroidVersions.U.SDK_INT).isEqualTo(34);
    assertThat(AndroidVersions.U.SHORT_CODE).isEqualTo("U");
    assertThat(new AndroidVersions.U().getVersion()).isEqualTo("14");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("U");
  }

  @Test
  @Config(sdk = 33)
  public void testStandardInitializationT() {
    assertThat(AndroidVersions.T.SDK_INT).isEqualTo(33);
    assertThat(AndroidVersions.T.SHORT_CODE).isEqualTo("T");
    assertThat(new AndroidVersions.T().getVersion()).isEqualTo("13");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("T");
  }

  @Test
  @Config(sdk = 32)
  public void testStandardInitializationSv2() {
    assertThat(AndroidVersions.Sv2.SDK_INT).isEqualTo(32);
    assertThat(AndroidVersions.Sv2.SHORT_CODE).isEqualTo("Sv2");
    assertThat(new AndroidVersions.Sv2().getVersion()).isEqualTo("12.1");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("Sv2");
  }

  @Test
  @Config(sdk = 31)
  public void testStandardInitializationS() {
    assertThat(AndroidVersions.S.SDK_INT).isEqualTo(31);
    assertThat(AndroidVersions.S.SHORT_CODE).isEqualTo("S");
    assertThat(new AndroidVersions.S().getVersion()).isEqualTo("12");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("S");
  }

  @Test
  @Config(sdk = 30)
  public void testStandardInitializationR() {
    assertThat(AndroidVersions.R.SDK_INT).isEqualTo(30);
    assertThat(AndroidVersions.R.SHORT_CODE).isEqualTo("R");
    assertThat(new AndroidVersions.R().getVersion()).isEqualTo("11");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("R");
  }

  @Test
  @Config(sdk = 29)
  public void testStandardInitializationQ() {
    assertThat(AndroidVersions.Q.SDK_INT).isEqualTo(29);
    assertThat(AndroidVersions.Q.SHORT_CODE).isEqualTo("Q");
    assertThat(new AndroidVersions.Q().getVersion()).isEqualTo("10");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("Q");
  }

  @Test
  @Config(sdk = 28)
  public void testStandardInitializationP() {
    assertThat(AndroidVersions.P.SDK_INT).isEqualTo(28);
    assertThat(AndroidVersions.P.SHORT_CODE).isEqualTo("P");
    assertThat(new AndroidVersions.P().getVersion()).isEqualTo("9");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("P");
  }

  @Test
  @Config(sdk = 27)
  public void testStandardInitializationOMR1() {
    assertThat(AndroidVersions.OMR1.SDK_INT).isEqualTo(27);
    assertThat(AndroidVersions.OMR1.SHORT_CODE).isEqualTo("OMR1");
    assertThat(new AndroidVersions.OMR1().getVersion()).isEqualTo("8.1");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("OMR1");
  }

  @Test
  @Config(sdk = 26)
  public void testStandardInitializationO() {
    assertThat(AndroidVersions.O.SDK_INT).isEqualTo(26);
    assertThat(AndroidVersions.O.SHORT_CODE).isEqualTo("O");
    assertThat(new AndroidVersions.O().getVersion()).isEqualTo("8.0");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("O");
  }

  @Test
  @Config(sdk = 25)
  public void testStandardInitializationNMR1() {
    assertThat(AndroidVersions.NMR1.SDK_INT).isEqualTo(25);
    assertThat(AndroidVersions.NMR1.SHORT_CODE).isEqualTo("NMR1");
    assertThat(new AndroidVersions.NMR1().getVersion()).isEqualTo("7.1");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("NMR1");
  }

  @Test
  @Config(sdk = 24)
  public void testStandardInitializationN() {
    assertThat(AndroidVersions.N.SDK_INT).isEqualTo(24);
    assertThat(AndroidVersions.N.SHORT_CODE).isEqualTo("N");
    assertThat(new AndroidVersions.N().getVersion()).isEqualTo("7.0");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("N");
  }

  @Test
  @Config(sdk = 23)
  public void testStandardInitializationM() {
    assertThat(AndroidVersions.M.SDK_INT).isEqualTo(23);
    assertThat(AndroidVersions.M.SHORT_CODE).isEqualTo("M");
    assertThat(new AndroidVersions.M().getVersion()).isEqualTo("6.0");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("M");
  }

  @Test
  @Config(sdk = 22)
  public void testStandardInitializationLMR1() {
    assertThat(AndroidVersions.LMR1.SDK_INT).isEqualTo(22);
    assertThat(AndroidVersions.LMR1.SHORT_CODE).isEqualTo("LMR1");
    assertThat(new AndroidVersions.LMR1().getVersion()).isEqualTo("5.1");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("LMR1");
  }

  @Test
  @Config(sdk = 21)
  public void testStandardInitializationL() {
    assertThat(AndroidVersions.L.SDK_INT).isEqualTo(21);
    assertThat(AndroidVersions.L.SHORT_CODE).isEqualTo("L");
    assertThat(new AndroidVersions.L().getVersion()).isEqualTo("5.0");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("L");
  }
}
