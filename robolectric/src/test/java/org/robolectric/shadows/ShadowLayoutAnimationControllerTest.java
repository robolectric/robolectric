package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.view.animation.LayoutAnimationController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowLayoutAnimationControllerTest {
  private ShadowLayoutAnimationController shadow;

  @Before
  public void setup() {
    LayoutAnimationController controller = new LayoutAnimationController(RuntimeEnvironment.application, null);
    shadow = Shadows.shadowOf(controller);
  }

  @Test
  public void testResourceId() {
    int id = 1;
    shadow.setLoadedFromResourceId(1);
    assertThat(shadow.getLoadedFromResourceId()).isEqualTo(id);
  }

}
