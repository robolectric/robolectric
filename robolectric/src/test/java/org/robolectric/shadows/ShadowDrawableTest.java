package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
  public void createFromStream__shouldReturnNullWhenAskedToCreateADrawableFromACorruptedSourceStream() throws Exception {
    String corruptedStreamSource = "http://foo.com/image.jpg";
    ShadowDrawable.addCorruptStreamSource(corruptedStreamSource);
    assertNull(ShadowDrawable.createFromStream(new ByteArrayInputStream(new byte[0]), corruptedStreamSource));
  }

  @Test
  public void createFromResourceStream_shouldWorkWithoutSourceName() {
    Drawable drawable =
        Drawable.createFromResourceStream(
            context.getResources(),
            null,
            new ByteArrayInputStream(new byte[0]),
            null,
            new BitmapFactory.Options());
    assertNotNull(drawable);
  }

  @Test
  public void createFromStream__shouldReturnDrawableWithSpecificSource() throws Exception {
    Drawable drawable = ShadowDrawable.createFromStream(new ByteArrayInputStream(new byte[0]), "my_source");
    assertNotNull(drawable);
    assertEquals("my_source", ((ShadowBitmapDrawable) shadowOf(drawable)).getSource());
  }

  @Test
  public void reset__shouldClearStaticState() throws Exception {
    String src = "source1";
    ShadowDrawable.addCorruptStreamSource(src);
    assertTrue(ShadowDrawable.corruptStreamSources.contains(src));
    ShadowDrawable.clearCorruptStreamSources();
    assertFalse(ShadowDrawable.corruptStreamSources.contains(src));
  }

  @Test
  public void testCreateFromStream_shouldSetTheInputStreamOnTheReturnedDrawable() throws Exception {
    ByteArrayInputStream byteInputStream = new ByteArrayInputStream(new byte[0]);
    Drawable drawable = Drawable.createFromStream(byteInputStream, "src name");
    assertThat(shadowOf(drawable).getInputStream()).isEqualTo((InputStream) byteInputStream);
  }

  @Test
  public void copyBoundsWithPassedRect() {
    Drawable drawable = ShadowDrawable.createFromStream(new ByteArrayInputStream(new byte[0]), "my_source");
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
    Drawable drawable = ShadowDrawable.createFromStream(new ByteArrayInputStream(new byte[0]), "my_source");
    drawable.setBounds(1, 2, 3, 4);
    Rect r = drawable.copyBounds();
    assertThat(r.left).isEqualTo(1);
    assertThat(r.top).isEqualTo(2);
    assertThat(r.right).isEqualTo(3);
    assertThat(r.bottom).isEqualTo(4);
  }

  @Test
  public void createFromPath__shouldReturnDrawableWithSpecificPath() throws Exception {
    Drawable drawable = ShadowDrawable.createFromPath("/foo");
    assertNotNull(drawable);
    assertEquals("/foo", ((ShadowBitmapDrawable) shadowOf(drawable)).getPath());
  }

  @Test
  public void testGetLoadedFromResourceId_shouldDefaultToNegativeOne() throws Exception {
    Drawable drawable = new TestDrawable();
    assertThat(shadowOf(drawable).getCreatedFromResId()).isEqualTo(-1);
  }

  @Test
  public void testCreateFromResourceId_shouldSetTheId() throws Exception {
    Drawable drawable = ShadowDrawable.createFromResourceId(34758);
    ShadowDrawable shadowDrawable = shadowOf(drawable);
    assertThat(shadowDrawable.getCreatedFromResId()).isEqualTo(34758);
  }

  @Test
  public void testWasSelfInvalidated() throws Exception {
    Drawable drawable = ShadowDrawable.createFromResourceId(34758);
    ShadowDrawable shadowDrawable = shadowOf(drawable);
    assertThat(shadowDrawable.wasInvalidated()).isFalse();
    drawable.invalidateSelf();
    assertThat(shadowDrawable.wasInvalidated()).isTrue();
  }

  @Test public void shouldLoadNinePatchFromDrawableXml() throws Exception {
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
}
