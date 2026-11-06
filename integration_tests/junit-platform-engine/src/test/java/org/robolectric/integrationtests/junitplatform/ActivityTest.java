package org.robolectric.integrationtests.junitplatform;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.widget.TextView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.junit.jupiter.RobolectricExtension;

@ExtendWith(RobolectricExtension.class)
@Config(sdk = {Config.OLDEST_SDK, Config.NEWEST_SDK})
class ActivityTest {

  @Test
  void activityGoesThroughLifecycle() {
    try (ActivityController<TestActivity> controller =
        Robolectric.buildActivity(TestActivity.class)) {
      TestActivity activity = controller.setup().get();
      assertThat(activity.textView.getText().toString()).isEqualTo("Hello");

      controller.pause().stop().destroy();
      assertThat(activity.isDestroyed()).isTrue();
    }
  }

  @Test
  void runsOnMainThread() {
    assertThat(Looper.getMainLooper().isCurrentThread()).isTrue();
  }

  /** An activity with a programmatic layout. */
  public static class TestActivity extends Activity {
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      textView = new TextView(this);
      textView.setText("Hello");
      setContentView(textView);
    }
  }
}
