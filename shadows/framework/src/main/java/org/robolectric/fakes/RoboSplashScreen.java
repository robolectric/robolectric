package org.robolectric.fakes;

import android.annotation.StyleRes;
import android.window.SplashScreen;

/** Robolectric implementation of {@link android.window.SplashScreen}. */
public class RoboSplashScreen implements SplashScreen {

  @StyleRes private int themeId;

  @Override
  public void setOnExitAnimationListener(SplashScreen.OnExitAnimationListener listener) {}

  @Override
  public void clearOnExitAnimationListener() {}

  @Override
  public void setSplashScreenTheme(@StyleRes int themeId) {
    this.themeId = themeId;
  }

  @StyleRes
  public int getSplashScreenTheme() {
    return themeId;
  }
}
