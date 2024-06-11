package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = O)
public class ShadowNativeBitmapDrawableTest {
  private static final int EXPECTED_COLOR = 0xff0000fe;

  @Test
  @Config(qualifiers = "hdpi")
  public void bitmapDrawable_highDensity() {
    Drawable drawable = RuntimeEnvironment.getApplication().getDrawable(R.drawable.icon_blue);
    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
    Bitmap output =
        Bitmap.createBitmap(
            drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);
    drawable.draw(canvas);
    // top left and bottom right colors should match
    assertThat(output.getPixel(0, 0)).isEqualTo(EXPECTED_COLOR);
    assertThat(output.getPixel(drawable.getIntrinsicWidth() - 1, drawable.getIntrinsicHeight() - 1))
        .isEqualTo(EXPECTED_COLOR);
  }

  @Test
  public void getCreatedFromResId() {
    Drawable drawable = RuntimeEnvironment.getApplication().getDrawable(R.drawable.icon_blue);
    assertThat(((ShadowDrawable) Shadow.extract(drawable)).getCreatedFromResId())
        .isEqualTo(R.drawable.icon_blue);
  }

  @Test
  public void legacy_createFromResourceId() {
    Drawable drawable = ShadowDrawable.createFromResourceId(100);
    assertThat(((ShadowDrawable) Shadow.extract(drawable)).getCreatedFromResId()).isEqualTo(100);
  }
}
