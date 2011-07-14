package com.xtremelabs.robolectric.shadows;

import android.graphics.drawable.Drawable;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ShadowDrawableTest {
    @Test
    public void testCreateFromStream_shouldSetTheInputStreamOnTheReturnedDrawable() throws Exception {
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(new byte[0]);
        Drawable drawable = Drawable.createFromStream(byteInputStream, "src name");
        assertThat(shadowOf(drawable).getInputStream(), equalTo((InputStream) byteInputStream));
    }
}
