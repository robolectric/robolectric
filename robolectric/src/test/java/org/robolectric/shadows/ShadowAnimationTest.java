package org.robolectric.shadows;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowAnimationTest {

  @Test
  public void getSetLoadedFromResourceId() {
    final Animation animation = new TestAnimation(RuntimeEnvironment.application, null);
    shadowOf(animation).setLoadedFromResourceId(42);

    assertThat(shadowOf(animation).getLoadedFromResourceId()).isEqualTo(42);
  }

  private static class TestAnimation extends Animation {

    public TestAnimation(Context context, AttributeSet attrs) {
      super(context, attrs);
    }
  }
}
