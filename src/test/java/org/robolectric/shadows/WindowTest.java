package org.robolectric.shadows;

import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.tester.android.view.RoboWindow;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class WindowTest {

  private Context context;

  @Before
  public void setup() throws Exception {
    context = Robolectric.application;
  }

  @Test
  public void testGetFlag() throws Exception {
    Window window = new RoboWindow(context);

    assertFalse(shadowOf(window).getFlag(WindowManager.LayoutParams.FLAG_FULLSCREEN));
    window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    assertTrue(shadowOf(window).getFlag(WindowManager.LayoutParams.FLAG_FULLSCREEN));
    window.setFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON, WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
    assertTrue(shadowOf(window).getFlag(WindowManager.LayoutParams.FLAG_FULLSCREEN));
  }
}
