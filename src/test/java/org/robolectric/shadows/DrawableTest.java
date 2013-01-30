package com.xtremelabs.robolectric.shadows;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import com.xtremelabs.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

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
        assertThat(shadowOf(drawable).getInputStream(), equalTo((InputStream) byteInputStream));
    }

    @Test
    public void copyBoundsWithPassedRect() {
        Drawable drawable = ShadowDrawable.createFromStream(new ByteArrayInputStream(new byte[0]), "my_source");
        drawable.setBounds(1, 2, 3, 4);
        Rect r = new Rect();
        drawable.copyBounds(r);
        assertThat(r.left, is(1));
        assertThat(r.top, is(2));
        assertThat(r.right, is(3));
        assertThat(r.bottom, is(4));
    }

    @Test
    public void copyBoundsToReturnedRect() {
        Drawable drawable = ShadowDrawable.createFromStream(new ByteArrayInputStream(new byte[0]), "my_source");
        drawable.setBounds(1, 2, 3, 4);
        Rect r = drawable.copyBounds();
        assertThat(r.left, is(1));
        assertThat(r.top, is(2));
        assertThat(r.right, is(3));
        assertThat(r.bottom, is(4));
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
        assertThat(shadowOf(drawable).getLoadedFromResourceId(), is(-1));
    }

    @Test
    public void testSetLoadedFromResourceId() throws Exception {
        Drawable drawable = new TestDrawable();
        ShadowDrawable shadowDrawable = shadowOf(drawable);
        shadowDrawable.setLoadedFromResourceId(99);
        assertThat(shadowDrawable.getLoadedFromResourceId(), is(99));
    }

    @Test
    public void testCreateFromResourceId_shouldSetTheId() throws Exception {
        Drawable drawable = ShadowDrawable.createFromResourceId(34758);
        ShadowDrawable shadowDrawable = shadowOf(drawable);
        assertThat(shadowDrawable.getLoadedFromResourceId(), is(34758));
    }

    @Test
    public void testWasSelfInvalidated() throws Exception {
        Drawable drawable = ShadowDrawable.createFromResourceId(34758);
        ShadowDrawable shadowDrawable = shadowOf(drawable);
        assertFalse(shadowDrawable.wasInvalidated());
        drawable.invalidateSelf();
        assertTrue(shadowDrawable.wasInvalidated());
    }

    private static class TestDrawable extends Drawable {
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
    }
}
