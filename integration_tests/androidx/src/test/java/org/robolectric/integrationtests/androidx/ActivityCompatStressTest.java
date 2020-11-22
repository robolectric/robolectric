package org.robolectric.integrationtests.androidx;

import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.R;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link AppCompatActivity} in Robolectric. */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class ActivityCompatStressTest {
  static class AppCompatThemeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle bundle) {
      super.onCreate(bundle);
      setTheme(R.style.Theme_AppCompat);
    }
  }

  /**
   * Checks that {@link ActivityController#destroy()} ()} cleans up Activity state. If it doesn't,
   * memory usage will blow up quickly and this test is very likely to OOM on SDK > P. Native theme
   * resources are what eats up the most memory.
   */
  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void destroy_cleansUpAllActivityState() {
    for (int i = 0; i < 1000; i++) {
      Robolectric.buildActivity(AppCompatThemeActivity.class).setup().destroy();
    }
  }
}
