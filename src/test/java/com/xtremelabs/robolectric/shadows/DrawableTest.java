package com.xtremelabs.robolectric.shadows;

import android.graphics.drawable.Drawable;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
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
}
