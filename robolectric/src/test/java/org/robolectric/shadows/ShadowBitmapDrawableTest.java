package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
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
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Shadows;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowBitmapDrawableTest {
  private final Resources resources =
      ((Application) ApplicationProvider.getApplicationContext()).getResources();

  @Test
  public void constructors_shouldSetBitmap() throws Exception {
    Bitmap bitmap = Shadow.newInstanceOf(Bitmap.class);
    BitmapDrawable drawable = new BitmapDrawable(bitmap);
    assertThat(drawable.getBitmap()).isEqualTo(bitmap);

    drawable = new BitmapDrawable(resources, bitmap);
    assertThat(drawable.getBitmap()).isEqualTo(bitmap);
  }

  @Test
  public void getBitmap_shouldReturnBitmapUsedToDraw() throws Exception {
    BitmapDrawable drawable = (BitmapDrawable) resources.getDrawable(R.drawable.an_image);
    assertThat(shadowOf(drawable.getBitmap()).getDescription()).isEqualTo("Bitmap for resource:org.robolectric:drawable/an_image");
  }

  @Test
  public void mutate_createsDeepCopy() throws Exception {
    BitmapDrawable original = (BitmapDrawable) resources.getDrawable(R.drawable.an_image);
    Drawable mutated = original.mutate();
    assertThat(original).isNotSameAs(mutated);
    assertThat(mutated instanceof BitmapDrawable).isTrue();
    assertThat(mutated.getIntrinsicHeight()).isEqualTo(original.getIntrinsicHeight());
    assertThat(mutated.getIntrinsicWidth()).isEqualTo(original.getIntrinsicWidth());
    assertThat(mutated.getBounds()).isEqualTo(original.getBounds());
  }

  @Test
  public void draw_shouldCopyDescriptionToCanvas() throws Exception {
    BitmapDrawable drawable = (BitmapDrawable) resources.getDrawable(R.drawable.an_image);
    Canvas canvas = new Canvas();
    drawable.draw(canvas);

    assertThat(shadowOf(canvas).getDescription()).isEqualTo("Bitmap for resource:org.robolectric:drawable/an_image");
  }

  @Test
  public void shouldInheritSourceStringFromDrawableDotCreateFromStream() throws Exception {
    InputStream emptyInputStream = new ByteArrayInputStream("".getBytes(UTF_8));
    BitmapDrawable drawable = (BitmapDrawable) Drawable.createFromStream(emptyInputStream, "source string value");
    assertThat(shadowOf(drawable).getSource()).isEqualTo("source string value");
  }

  @Test
  public void withColorFilterSet_draw_shouldCopyDescriptionToCanvas() throws Exception {
    BitmapDrawable drawable = (BitmapDrawable) resources.getDrawable(R.drawable.an_image);
    drawable.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix()));
    Canvas canvas = new Canvas();
    drawable.draw(canvas);

    assertThat(shadowOf(canvas).getDescription()).isEqualTo("Bitmap for resource:org.robolectric:drawable/an_image with ColorMatrixColorFilter<1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0>");
  }

  @Test
  public void shouldStillHaveShadow() throws Exception {
    Drawable drawable = resources.getDrawable(R.drawable.an_image);
    assertThat(Shadows.shadowOf(drawable).getCreatedFromResId()).isEqualTo(R.drawable.an_image);
  }

  @Test
  public void shouldSetTileModeXY() throws Exception {
    BitmapDrawable drawable = (BitmapDrawable) resources.getDrawable(R.drawable.an_image);
    drawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.MIRROR);
    assertThat(drawable.getTileModeX()).isEqualTo(Shader.TileMode.REPEAT);
    assertThat(drawable.getTileModeY()).isEqualTo(Shader.TileMode.MIRROR);
  }

  @Test
  public void constructor_shouldSetTheIntrinsicWidthAndHeightToTheWidthAndHeightOfTheBitmap() throws Exception {
    Bitmap bitmap = Bitmap.createBitmap(5, 10, Bitmap.Config.ARGB_8888);
    BitmapDrawable drawable =
        new BitmapDrawable(ApplicationProvider.getApplicationContext().getResources(), bitmap);
    assertThat(drawable.getIntrinsicWidth()).isEqualTo(5);
    assertThat(drawable.getIntrinsicHeight()).isEqualTo(10);
  }

  @Test
  public void constructor_shouldAcceptNullBitmap() throws Exception {
    assertThat(
            new BitmapDrawable(
                ApplicationProvider.getApplicationContext().getResources(), (Bitmap) null))
        .isNotNull();
  }
}
