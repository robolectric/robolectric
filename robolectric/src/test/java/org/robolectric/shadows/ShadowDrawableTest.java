package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowDrawableTest {

  private Application context;

  @Before
  public void setUp() throws Exception {
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void createFromResourceStream_shouldWorkWithoutSourceName() {
    Drawable drawable =
        Drawable.createFromResourceStream(
            context.getResources(), null, createImageStream(), null, new BitmapFactory.Options());
    assertNotNull(drawable);
  }

  @Test
  public void copyBoundsWithPassedRect() {
    Drawable drawable = Drawable.createFromStream(createImageStream(), "my_source");
    drawable.setBounds(1, 2, 3, 4);
    Rect r = new Rect();
    drawable.copyBounds(r);
    assertThat(r.left).isEqualTo(1);
    assertThat(r.top).isEqualTo(2);
    assertThat(r.right).isEqualTo(3);
    assertThat(r.bottom).isEqualTo(4);
  }

  @Test
  public void copyBoundsToReturnedRect() {
    Drawable drawable = Drawable.createFromStream(createImageStream(), "my_source");
    drawable.setBounds(1, 2, 3, 4);
    Rect r = drawable.copyBounds();
    assertThat(r.left).isEqualTo(1);
    assertThat(r.top).isEqualTo(2);
    assertThat(r.right).isEqualTo(3);
    assertThat(r.bottom).isEqualTo(4);
  }

  @Test
  public void testGetLoadedFromResourceId_shouldDefaultToNegativeOne() {
    Drawable drawable = new TestDrawable();
    assertThat(shadowOf(drawable).getCreatedFromResId()).isEqualTo(-1);
  }

  @Test
  public void testCreateFromResourceId_shouldSetTheId() {
    Drawable drawable = ShadowDrawable.createFromResourceId(34758);
    ShadowDrawable shadowDrawable = shadowOf(drawable);
    assertThat(shadowDrawable.getCreatedFromResId()).isEqualTo(34758);
  }

  @Test
  public void testWasSelfInvalidated() {
    Drawable drawable = ShadowDrawable.createFromResourceId(34758);
    ShadowDrawable shadowDrawable = shadowOf(drawable);
    assertThat(shadowDrawable.wasInvalidated()).isFalse();
    drawable.invalidateSelf();
    assertThat(shadowDrawable.wasInvalidated()).isTrue();
  }

  @Test
  public void shouldLoadNinePatchFromDrawableXml() {
    assertThat(context.getResources().getDrawable(R.drawable.drawable_with_nine_patch)).isNotNull();
  }

  @Test public void settingBoundsShouldInvokeCallback() {
    TestDrawable drawable = new TestDrawable();
    assertThat(drawable.boundsChanged).isFalse();
    drawable.setBounds(0, 0, 10, 10);
    assertThat(drawable.boundsChanged).isTrue();
  }

  @Test
  public void drawableIntrinsicWidthAndHeightShouldBeCorrect() {
    final Drawable anImage = context.getResources().getDrawable(R.drawable.an_image);

    assertThat(anImage.getIntrinsicHeight()).isEqualTo(53);
    assertThat(anImage.getIntrinsicWidth()).isEqualTo(64);
  }

  @Test
  @Config(qualifiers = "mdpi")
  public void drawableShouldLoadImageOfCorrectSizeWithMdpiQualifier() {
    final Drawable anImage = context.getResources().getDrawable(R.drawable.robolectric);

    assertThat(anImage.getIntrinsicHeight()).isEqualTo(167);
    assertThat(anImage.getIntrinsicWidth()).isEqualTo(198);
  }

  @Test
  @Config(qualifiers = "hdpi")
  public void drawableShouldLoadImageOfCorrectSizeWithHdpiQualifier() {
    if (Build.VERSION.SDK_INT >= 28) {
      // getDrawable depends on ImageDecoder, which depends on binary resources
      assume().that(ShadowAssetManager.useLegacy()).isFalse();
    }

    final Drawable anImage = context.getResources().getDrawable(R.drawable.robolectric);

    assertThat(anImage.getIntrinsicHeight()).isEqualTo(251);
    assertThat(anImage.getIntrinsicWidth()).isEqualTo(297);
  }

  @Test
  @Config(maxSdk = KITKAT_WATCH)
  public void testGetBitmapOrVectorDrawableAt19() {
    // at API 21+ and mdpi, the drawable-anydpi-v21/image_or_vector.xml should be loaded instead
    // of drawable/image_or_vector.png
    final Drawable aDrawable = context.getResources().getDrawable(R.drawable.an_image_or_vector);
    assertThat(aDrawable).isInstanceOf(BitmapDrawable.class);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void testGetBitmapOrVectorDrawableAt21() {
    final Drawable aDrawable = context.getResources().getDrawable(R.drawable.an_image_or_vector);
    assertThat(aDrawable).isInstanceOf(VectorDrawable.class);
  }

  private static class TestDrawable extends Drawable {
    public boolean boundsChanged;

    @Override
    public void draw(Canvas canvas) {
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }

    @Override
    public int getOpacity() {
      return 0;
    }

    @Override protected void onBoundsChange(Rect bounds) {
      boundsChanged = true;
      super.onBoundsChange(bounds);
    }
  }

  private static ByteArrayInputStream createImageStream() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    bitmap.compress(CompressFormat.PNG, 100, outputStream);
    return new ByteArrayInputStream(outputStream.toByteArray());
  }
}
