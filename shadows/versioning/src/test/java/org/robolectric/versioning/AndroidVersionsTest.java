package org.robolectric.versioning;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build.VERSION;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.versioning.AndroidVersions.T;

/**
 * Check versions information aligns with runtime information. Primarily, selected SDK with
 * internally detected version number.
 */
@RunWith(RobolectricTestRunner.class)
public final class AndroidVersionsTest {

  @Test
  @Config(sdk = T.SDK_INT)
  public void testStandardInitializationT() {
    assertThat(VERSION.SDK_INT).isEqualTo(33);
    assertThat(VERSION.RELEASE).isEqualTo("13");
    assertThat(VERSION.CODENAME).isEqualTo("REL");
    assertThat(AndroidVersions.T.SHORT_CODE).isEqualTo("T");
    assertThat(new AndroidVersions.T().getVersion()).isEqualTo("13.0");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("T");
  }

  @Test
  @Config(sdk = 32)
  public void testStandardInitializationSv2() {
    assertThat(VERSION.SDK_INT).isEqualTo(32);
    assertThat(VERSION.RELEASE).isEqualTo("12");
    assertThat(VERSION.CODENAME).isEqualTo("REL");
    assertThat(AndroidVersions.Sv2.SHORT_CODE).isEqualTo("Sv2");
    assertThat(new AndroidVersions.Sv2().getVersion()).isEqualTo("12.1");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("Sv2");
  }

  @Test
  @Config(sdk = 31)
  public void testStandardInitializationS() {
    assertThat(VERSION.SDK_INT).isEqualTo(31);
    assertThat(VERSION.RELEASE).isEqualTo("12");
    assertThat(VERSION.CODENAME).isEqualTo("REL");
    assertThat(AndroidVersions.S.SHORT_CODE).isEqualTo("S");
    assertThat(new AndroidVersions.S().getVersion()).isEqualTo("12.0");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("S");
  }

  @Test
  @Config(sdk = 30)
  public void testStandardInitializationR() {
    assertThat(VERSION.SDK_INT).isEqualTo(30);
    assertThat(VERSION.RELEASE).isEqualTo("11");
    assertThat(VERSION.CODENAME).isEqualTo("REL");
    assertThat(AndroidVersions.R.SHORT_CODE).isEqualTo("R");
    assertThat(new AndroidVersions.R().getVersion()).isEqualTo("11.0");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("R");
  }

  @Test
  @Config(sdk = 29)
  public void testStandardInitializationQ() {
    assertThat(VERSION.SDK_INT).isEqualTo(29);
    assertThat(VERSION.RELEASE).isEqualTo("10");
    assertThat(VERSION.CODENAME).isEqualTo("REL");
    assertThat(AndroidVersions.Q.SHORT_CODE).isEqualTo("Q");
    assertThat(new AndroidVersions.Q().getVersion()).isEqualTo("10.0");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("Q");
  }

  @Test
  @Config(sdk = 28)
  public void testStandardInitializationP() {
    assertThat(VERSION.SDK_INT).isEqualTo(28);
    assertThat(VERSION.RELEASE).isEqualTo("9");
    assertThat(VERSION.CODENAME).isEqualTo("REL");
    assertThat(AndroidVersions.P.SHORT_CODE).isEqualTo("P");
    assertThat(new AndroidVersions.P().getVersion()).isEqualTo("9.0");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("P");
  }

  @Test
  @Config(sdk = 27)
  public void testStandardInitializationOMR1() {
    assertThat(VERSION.SDK_INT).isEqualTo(27);
    assertThat(VERSION.RELEASE).isEqualTo("8.1.0");
    assertThat(VERSION.CODENAME).isEqualTo("REL");
    assertThat(AndroidVersions.OMR1.SHORT_CODE).isEqualTo("OMR1");
    assertThat(new AndroidVersions.OMR1().getVersion()).isEqualTo("8.1");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("OMR1");
  }

  @Test
  @Config(sdk = 26)
  public void testStandardInitializationO() {
    assertThat(VERSION.SDK_INT).isEqualTo(26);
    assertThat(VERSION.RELEASE).isEqualTo("8.0.0");
    assertThat(VERSION.CODENAME).isEqualTo("REL");
    assertThat(AndroidVersions.O.SHORT_CODE).isEqualTo("O");
    assertThat(new AndroidVersions.O().getVersion()).isEqualTo("8.0");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("O");
  }

  @Test
  @Config(sdk = 25)
  public void testStandardInitializationNMR1() {
    assertThat(VERSION.SDK_INT).isEqualTo(25);
    assertThat(VERSION.RELEASE).isEqualTo("7.1");
    assertThat(VERSION.CODENAME).isEqualTo("REL");
    assertThat(AndroidVersions.NMR1.SHORT_CODE).isEqualTo("NMR1");
    assertThat(new AndroidVersions.NMR1().getVersion()).isEqualTo("7.1");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("NMR1");
  }

  @Test
  @Config(sdk = 24)
  public void testStandardInitializationN() {
    assertThat(VERSION.SDK_INT).isEqualTo(24);
    assertThat(VERSION.RELEASE).isEqualTo("7.0");
    assertThat(VERSION.CODENAME).isEqualTo("REL");
    assertThat(AndroidVersions.N.SHORT_CODE).isEqualTo("N");
    assertThat(new AndroidVersions.N().getVersion()).isEqualTo("7.0");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("N");
  }

  @Test
  @Config(sdk = 23)
  public void testStandardInitializationM() {
    assertThat(VERSION.SDK_INT).isEqualTo(23);
    assertThat(VERSION.RELEASE).isEqualTo("6.0.1");
    assertThat(VERSION.CODENAME).isEqualTo("REL");
    assertThat(AndroidVersions.M.SHORT_CODE).isEqualTo("M");
    assertThat(new AndroidVersions.M().getVersion()).isEqualTo("6.0");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("M");
  }

  @Test
  @Config(sdk = 22)
  public void testStandardInitializationLMR1() {
    assertThat(VERSION.SDK_INT).isEqualTo(22);
    assertThat(VERSION.RELEASE).isEqualTo("5.1.1");
    assertThat(VERSION.CODENAME).isEqualTo("REL");
    assertThat(AndroidVersions.LMR1.SHORT_CODE).isEqualTo("LMR1");
    assertThat(new AndroidVersions.LMR1().getVersion()).isEqualTo("5.1");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("LMR1");
  }

  @Test
  @Config(sdk = 21)
  public void testStandardInitializationL() {
    assertThat(VERSION.SDK_INT).isEqualTo(21);
    assertThat(VERSION.RELEASE).isEqualTo("5.0.2");
    assertThat(VERSION.CODENAME).isEqualTo("REL");
    assertThat(AndroidVersions.L.SHORT_CODE).isEqualTo("L");
    assertThat(new AndroidVersions.L().getVersion()).isEqualTo("5.0");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("L");
  }

  @Test
  @Config(sdk = 19)
  public void testStandardInitializationK() {
    assertThat(VERSION.SDK_INT).isEqualTo(19);
    assertThat(VERSION.RELEASE).isEqualTo("4.4");
    assertThat(VERSION.CODENAME).isEqualTo("REL");
    assertThat(AndroidVersions.K.SHORT_CODE).isEqualTo("K");
    assertThat(new AndroidVersions.K().getVersion()).isEqualTo("4.4");
    assertThat(AndroidVersions.CURRENT.getShortCode()).isEqualTo("K");
  }
}
