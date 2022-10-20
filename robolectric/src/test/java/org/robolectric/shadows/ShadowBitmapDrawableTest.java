package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Shadows;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowBitmapDrawableTest {
  private final Resources resources = ApplicationProvider.getApplicationContext().getResources();

  @Test
  public void constructors_shouldSetBitmap() {
    Bitmap bitmap = Shadow.newInstanceOf(Bitmap.class);
    BitmapDrawable drawable = new BitmapDrawable(bitmap);
    assertThat(drawable.getBitmap()).isEqualTo(bitmap);

    drawable = new BitmapDrawable(resources, bitmap);
    assertThat(drawable.getBitmap()).isEqualTo(bitmap);
  }

  @Test
  public void getBitmap_shouldReturnBitmapUsedToDraw() {
    BitmapDrawable drawable = (BitmapDrawable) resources.getDrawable(R.drawable.an_image);
    assertThat(shadowOf(drawable.getBitmap()).getDescription())
        .isEqualTo("Bitmap for" + " resource:org.robolectric:drawable/an_image");
  }

  @Test
  public void draw_shouldCopyDescriptionToCanvas() {
    BitmapDrawable drawable = (BitmapDrawable) resources.getDrawable(R.drawable.an_image);
    Canvas canvas = new Canvas();
    drawable.draw(canvas);

    assertThat(shadowOf(canvas).getDescription())
        .isEqualTo("Bitmap for" + " resource:org.robolectric:drawable/an_image");
  }

  @Test
  public void withColorFilterSet_draw_shouldCopyDescriptionToCanvas() {
    BitmapDrawable drawable = (BitmapDrawable) resources.getDrawable(R.drawable.an_image);
    drawable.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix()));
    Canvas canvas = new Canvas();
    drawable.draw(canvas);

    assertThat(shadowOf(canvas).getDescription())
        .isEqualTo(
            "Bitmap for"
                + " resource:org.robolectric:drawable/an_image with ColorMatrixColorFilter");
  }

  @Test
  public void shouldStillHaveShadow() {
    Drawable drawable = resources.getDrawable(R.drawable.an_image);
    assertThat(Shadows.shadowOf(drawable).getCreatedFromResId()).isEqualTo(R.drawable.an_image);
  }

  @Test
  public void shouldSetTileModeXY() {
    BitmapDrawable drawable = (BitmapDrawable) resources.getDrawable(R.drawable.an_image);
    drawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.MIRROR);
    assertThat(drawable.getTileModeX()).isEqualTo(Shader.TileMode.REPEAT);
    assertThat(drawable.getTileModeY()).isEqualTo(Shader.TileMode.MIRROR);
  }

  @Test
  public void constructor_shouldSetTheIntrinsicWidthAndHeightToTheWidthAndHeightOfTheBitmap() {
    Bitmap bitmap = Bitmap.createBitmap(5, 10, Bitmap.Config.ARGB_8888);
    BitmapDrawable drawable =
        new BitmapDrawable(ApplicationProvider.getApplicationContext().getResources(), bitmap);
    assertThat(drawable.getIntrinsicWidth()).isEqualTo(5);
    assertThat(drawable.getIntrinsicHeight()).isEqualTo(10);
  }

  @Test
  public void constructor_shouldAcceptNullBitmap() {
    assertThat(
            new BitmapDrawable(
                ApplicationProvider.getApplicationContext().getResources(), (Bitmap) null))
        .isNotNull();
  }

}
