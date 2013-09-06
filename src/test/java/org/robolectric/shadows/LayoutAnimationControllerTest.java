package org.robolectric.shadows;

import android.view.animation.LayoutAnimationController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class LayoutAnimationControllerTest {
  private ShadowLayoutAnimationController shadow;

  @Before
  public void setup() {
    LayoutAnimationController controller = new LayoutAnimationController(Robolectric.application, null);
    shadow = Robolectric.shadowOf(controller);
  }

  @Test
  public void testResourceId() {
    int id = 1;
    shadow.setLoadedFromResourceId(1);
    assertThat(shadow.getLoadedFromResourceId()).isEqualTo(id);
  }

}
