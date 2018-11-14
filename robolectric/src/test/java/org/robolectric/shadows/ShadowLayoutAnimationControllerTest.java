package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.view.animation.LayoutAnimationController;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;

@RunWith(AndroidJUnit4.class)
public class ShadowLayoutAnimationControllerTest {
  private ShadowLayoutAnimationController shadow;

  @Before
  public void setup() {
    LayoutAnimationController controller =
        new LayoutAnimationController(ApplicationProvider.getApplicationContext(), null);
    shadow = Shadows.shadowOf(controller);
  }

  @Test
  public void testResourceId() {
    int id = 1;
    shadow.setLoadedFromResourceId(1);
    assertThat(shadow.getLoadedFromResourceId()).isEqualTo(id);
  }

}
