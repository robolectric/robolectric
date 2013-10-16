package org.robolectric.shadows;

import android.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class WindowTest {


  @Test
  public void testGetFlag() throws Exception {
    Activity activity = Robolectric.buildActivity(Activity.class).create().get();
    Window window = activity.getWindow();

    assertFalse(shadowOf(window).getFlag(WindowManager.LayoutParams.FLAG_FULLSCREEN));
    window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    assertTrue(shadowOf(window).getFlag(WindowManager.LayoutParams.FLAG_FULLSCREEN));
    window.setFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON, WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
    assertTrue(shadowOf(window).getFlag(WindowManager.LayoutParams.FLAG_FULLSCREEN));
  }

  @Test
  public void testGetTitle() throws Exception {
    Activity activity = Robolectric.buildActivity(Activity.class).create().get();
    Window window = activity.getWindow();
    window.setTitle("My Window Title");
    assertEquals("My Window Title", shadowOf(window).getTitle());
  }

  @Test
  public void getHomeIcon_getsTheIconThatWasSetWithTheActionBar() throws Exception {
    TestActivity activity = Robolectric.buildActivity(TestActivity.class).create().get();
    Window window = activity.getWindow();
    ShadowWindow shadowWindow = shadowOf(window);

    ImageView homeIcon = shadowWindow.getHomeIcon();

    assertThat(homeIcon.getDrawable()).isNotNull();
    int createdFromResId = shadowOf(homeIcon.getDrawable()).getCreatedFromResId();
    assertThat(createdFromResId).isEqualTo(R.drawable.ic_lock_power_off);
  }

  public static class TestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setTheme(R.style.Theme_Holo_Light);
      setContentView(new LinearLayout(this));
      getActionBar().setIcon(R.drawable.ic_lock_power_off);
    }
  }
}
