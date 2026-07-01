package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.Application;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class DensityMutationTest {

  private Application context = ApplicationProvider.getApplicationContext();

  @Test
  @Config(qualifiers = "mdpi") // mdpi is 160dpi
  public void testDensityMutationBug() {
    // Load a drawable with density override (xhdpi = 320dpi). There was a bug that previously
    // caused this to permanently mutate the AssetManager's configuration density to 320.
    Object unused = context.getResources().getDrawableForDensity(R.drawable.trigger_override, 320);

    // Load the test drawable with default density.
    // If the bug is present, the xhdpi png (blue) will be loaded.
    // If fixed, the correct mdpi png (red) will be loaded.
    BitmapDrawable drawable =
        (BitmapDrawable) context.getResources().getDrawable(R.drawable.test_density_mutation);
    int color = drawable.getBitmap().getPixel(0, 0);
    assertThat(color).isEqualTo(Color.RED);
  }
}
