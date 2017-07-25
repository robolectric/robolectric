
package org.robolectric.res.android;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ResTableConfigTest {

  /**
   * https://developer.android.com/guide/topics/resources/providing-resources.html#MccQualifier
   */
  @Test
  public void isBetterThan_mccUndefined() {
    ResTableConfig config = new ResTableConfigBuilder().build();

    assertThat(config.isBetterThan(config, config)).isFalse();
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
  }
}
