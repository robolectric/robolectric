
package org.robolectric.res.android;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ResTableConfigTest {

  public static final int MCC_US_CARRIER = 310;
  public static final int MCC_US_VERIZON = 4;

  /**
   * https://developer.android.com/guide/topics/resources/providing-resources.html#MccQualifier
   * @see <a href="http://mcc-mnc.com/">http://mcc-mnc.com/</a>
   */
  @Test
  public void isBetterThan_mcc() {
    // When a configuration is not specified the result is always false
    assertThat(newBuilder().build().isBetterThan(newBuilder().build(), newBuilder().build())).isFalse();

    // When requested is less of a match
    assertThat(newBuilder().setMcc(MCC_US_CARRIER).build()
        .isBetterThan(newBuilder().setMcc(MCC_US_CARRIER).build(), newBuilder().build()))
        .isFalse();

    // When requested is a better match
    assertThat(newBuilder().setMcc(MCC_US_CARRIER).build()
        .isBetterThan(newBuilder().build(), newBuilder().setMcc(MCC_US_CARRIER).build()))
        .isTrue();
  }

  /**
   * https://developer.android.com/guide/topics/resources/providing-resources.html#MccQualifier
   * @see <a href="http://mcc-mnc.com/">http://mcc-mnc.com/</a>
   */
  @Test
  public void isBetterThan_mnc() {
    // When a configuration is not specified the result is always false
    assertThat(newBuilder().build().isBetterThan(newBuilder().build(), newBuilder().build())).isFalse();

    // When requested is less of a match
    assertThat(newBuilder().setMcc(MCC_US_CARRIER).setMnc(MCC_US_VERIZON).build()
        .isBetterThan(newBuilder().setMcc(MCC_US_CARRIER).build(), newBuilder().build()))
        .isFalse();

    // When requested is a better match - any US Carrier is a better match to US + Verizon
    assertThat(newBuilder().setMcc(MCC_US_CARRIER).setMnc(MCC_US_VERIZON).build()
        .isBetterThan(newBuilder().build(), newBuilder().setMcc(MCC_US_CARRIER).build()))
        .isTrue();

    // When requested is a better match - any US Carrier is a better match to US + Verizon
    assertThat(newBuilder().setMcc(MCC_US_CARRIER).setMnc(MCC_US_VERIZON).build()
        .isBetterThan(newBuilder().setMcc(MCC_US_CARRIER).build(), newBuilder().setMcc(MCC_US_CARRIER).setMnc(MCC_US_VERIZON).build()))
        .isTrue();

    // When requested is a better match - any US Carrier is not a better match to US + Verizon
    assertThat(newBuilder().setMcc(MCC_US_CARRIER).setMnc(MCC_US_VERIZON).build()
        .isBetterThan(newBuilder().setMcc(MCC_US_CARRIER).setMnc(MCC_US_VERIZON).build(), (newBuilder().setMcc(MCC_US_CARRIER).build())))
        .isFalse();
  }

  public static ResTableConfigBuilder newBuilder() {
    return new ResTableConfigBuilder();
  }

  private static class ResTableConfigBuilder {
        int mcc;
        int mnc;
        byte[] language = new byte[2];
        byte[] region = new byte[2];
        int orientation;
        int touchscreen;
        int density;
        int keyboard;
        int navigation;
        int inputFlags;
        int screenWidth;
        int screenHeight;
        int sdkVersion;
        int minorVersion;
        int screenLayout;
        int uiMode;
        int smallestScreenWidthDp;
        int screenWidthDp;
        int screenHeightDp;
        byte[] localeScript = new byte[4];
        byte[] localeVariant = new byte[8];
        int screenLayout2;

    ResTableConfig build() {
      return new ResTableConfig(0, mcc, mnc, language, region, orientation, touchscreen, density, keyboard, navigation, inputFlags, screenWidth,
          screenHeight, sdkVersion, minorVersion, screenLayout, uiMode, smallestScreenWidthDp, screenWidthDp, screenHeightDp, localeScript, localeVariant, screenLayout2, null);
    }

    public ResTableConfigBuilder setMcc(int mcc) {
      this.mcc = mcc;
      return this;
    }

    public ResTableConfigBuilder setMnc(int mnc) {
      this.mnc = mnc;
      return this;
    }
  }
}
