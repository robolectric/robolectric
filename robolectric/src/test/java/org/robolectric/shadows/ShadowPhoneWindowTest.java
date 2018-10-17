package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.Window;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

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
  public void getTitle() throws Exception {
    window.setTitle("Some title");
    assertThat(shadowOf(window).getTitle()).isEqualTo("Some title");
  }

  @Test
  public void getBackgroundDrawable() throws Exception {
    Drawable drawable = activity.getResources().getDrawable(android.R.drawable.bottom_bar);
    window.setBackgroundDrawable(drawable);
    assertThat(shadowOf(window).getBackgroundDrawable()).isSameAs(drawable);
  }
}