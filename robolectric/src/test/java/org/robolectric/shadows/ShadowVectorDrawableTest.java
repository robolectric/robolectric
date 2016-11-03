package org.robolectric.shadows;

import android.content.res.Resources;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowVectorDrawableTest {
  @Test
  public void shouldLoadVectorDrawables() throws Exception {
    Resources resources = RuntimeEnvironment.application.getResources();
    int drawableId = resources.getIdentifier("btn_checkbox_checked_mtrl", "drawable", "android");
    resources.getDrawable(drawableId);
  }
}