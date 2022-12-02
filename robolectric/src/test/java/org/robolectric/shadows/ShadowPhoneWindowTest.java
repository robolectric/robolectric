package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION_CODES;
import android.view.Window;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowPhoneWindowTest {

  private Window window;
  private Activity activity;

  @Before
  public void setUp() throws Exception {
    activity = Robolectric.setupActivity(Activity.class);
    window = activity.getWindow();
  }

  @Test
  public void getTitle() {
    window.setTitle("Some title");
    assertThat(shadowOf(window).getTitle().toString()).isEqualTo("Some title");
  }

  @Test
  public void getBackgroundDrawable() {
    Drawable drawable = activity.getResources().getDrawable(android.R.drawable.bottom_bar);
    window.setBackgroundDrawable(drawable);
    assertThat(shadowOf(window).getBackgroundDrawable()).isSameInstanceAs(drawable);
  }

  @Test
  @Config(minSdk = VERSION_CODES.R)
  public void getDecorFitsSystemWindows_noCall_returnsDefault() {
    ShadowWindow candidate = shadowOf(window);
    assertThat(candidate).isInstanceOf(ShadowPhoneWindow.class);

    assertThat(((ShadowPhoneWindow) candidate).getDecorFitsSystemWindows()).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.R)
  public void getDecorFitsSystemWindows_recordsLastValue() {
    ShadowWindow candidate = shadowOf(window);
    assertThat(candidate).isInstanceOf(ShadowPhoneWindow.class);

    window.setDecorFitsSystemWindows(true);
    assertThat(((ShadowPhoneWindow) candidate).getDecorFitsSystemWindows()).isTrue();

    window.setDecorFitsSystemWindows(false);
    assertThat(((ShadowPhoneWindow) candidate).getDecorFitsSystemWindows()).isFalse();
  }
}