package org.robolectric.shadows;

import android.graphics.Outline;
import android.graphics.Path;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowOutlineTest {

  @Test
  public void setConvexPath_doesNothing() {
    final Outline outline = new Outline();
    outline.setConvexPath(new Path());
  }
}
