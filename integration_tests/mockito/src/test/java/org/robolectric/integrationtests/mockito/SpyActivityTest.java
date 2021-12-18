package org.robolectric.integrationtests.mockito;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.spy;

import android.app.Activity;
import android.graphics.RenderNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

@RunWith(RobolectricTestRunner.class)
public final class SpyActivityTest {
  ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class).setup();

  @Test
  public void testIsFinishing() throws Exception {
    Activity activity = controller.get();
    activity.finish();;
    assertThat(activity.isFinishing()).isTrue();
    assertThat(1).isEqualTo(2);
  }

  @Test
  public void testSpyIsFinishing() throws Exception {
    Activity spyActivity = spy(controller.get());
    spyActivity.finish();
    assertThat(spyActivity.isFinishing()).isTrue();
  }
}

