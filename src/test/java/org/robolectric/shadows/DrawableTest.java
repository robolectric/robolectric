package org.robolectric.shadows;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static junit.framework.Assert.assertFalse;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class DrawableTest {
  @Test
  public void createFromStream__shouldReturnNullWhenAskedToCreateADrawableFromACorruptedSourceStream() throws Exception {
    String corruptedStreamSource = "http://foo.com/image.jpg";
    ShadowDrawable.addCorruptStreamSource(corruptedStreamSource);
    assertNull(ShadowDrawable.createFromStream(new ByteArrayInputStream(new byte[0]), corruptedStreamSource));
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
    ShadowDrawable.reset();
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
    assertThat(Robolectric.application.getResources()
        .getDrawable(R.drawable.drawable_with_nine_patch)).isNotNull();
  }

  @Test public void settingBoundsShouldInvokeCallback() {
    TestDrawable drawable = new TestDrawable();
    assertThat(drawable.boundsChanged).isFalse();
    drawable.setBounds(0, 0, 10, 10);
    assertThat(drawable.boundsChanged).isTrue();
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
