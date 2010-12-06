package com.xtremelabs.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class BitmapFactoryTest {
    @Test
    public void decodeResource_shouldSetDescription() throws Exception {
        Bitmap bitmap = BitmapFactory.decodeResource(Robolectric.application.getResources(), R.drawable.an_image);
        assertEquals("Bitmap for resource drawable/an_image", shadowOf(bitmap).getDescription());
        assertEquals(100, bitmap.getWidth());
        assertEquals(100, bitmap.getHeight());
    }

    @Test
    public void decodeFile_shouldSetDescription() throws Exception {
        Bitmap bitmap = BitmapFactory.decodeFile("/some/file.jpg");
        assertEquals("Bitmap for file /some/file.jpg", shadowOf(bitmap).getDescription());
        assertEquals(100, bitmap.getWidth());
        assertEquals(100, bitmap.getHeight());
    }
}
