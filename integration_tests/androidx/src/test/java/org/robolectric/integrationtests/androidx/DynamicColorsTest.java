package org.robolectric.integrationtests.androidx;

import static com.google.common.truth.Truth.assertThat;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.material.R;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.google.android.material.color.HarmonizedColors;
import com.google.android.material.color.HarmonizedColorsOptions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class DynamicColorsTest {

  @Test
  @Config(minSdk = 31)
  public void applyingContentBasedDynamicColors() {
    try (ActivityController<DynamicColorActivity> controller =
        Robolectric.buildActivity(DynamicColorActivity.class).setup()) {
      assertThat(controller.get().dynamicColorsApplied).isTrue();
    }
  }

  public static class DynamicColorActivity extends AppCompatActivity {

    private boolean dynamicColorsApplied;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setTheme(R.style.Theme_Material3_DayNight_NoActionBar);
      DynamicColors.applyToActivityIfAvailable(
          this,
          new DynamicColorsOptions.Builder()
              .setContentBasedSource(Color.GREEN)
              .setOnAppliedCallback(activity -> {
                HarmonizedColors.applyToContextIfAvailable(
                    activity,
                    new HarmonizedColorsOptions.Builder()
                        .setColorResourceIds(
                            new int[] {
                              org.robolectric.testapp.R.color.black,
                              org.robolectric.testapp.R.color.white
                            })
                        .build());
                dynamicColorsApplied = true;
              })
              .build());
    }
  }
}
