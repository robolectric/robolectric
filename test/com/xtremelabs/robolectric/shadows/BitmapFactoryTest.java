package com.xtremelabs.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class BitmapFactoryTest {
    @Test
    public void decodeResource_shouldSetDescription() throws Exception {
        Bitmap bitmap = BitmapFactory.decodeResource(Robolectric.application.getResources(), R.drawable.an_image);
        assertEquals("Bitmap for resource:drawable/an_image", shadowOf(bitmap).getDescription());
        assertEquals(100, bitmap.getWidth());
        assertEquals(100, bitmap.getHeight());
    }

    @Test
    public void decodeFile_shouldSetDescription() throws Exception {
        Bitmap bitmap = BitmapFactory.decodeFile("/some/file.jpg");
        assertEquals("Bitmap for file:/some/file.jpg", shadowOf(bitmap).getDescription());
        assertEquals(100, bitmap.getWidth());
        assertEquals(100, bitmap.getHeight());
    }

    @Test
    public void decodeStream_shouldSetDescription() throws Exception {
        InputStream inputStream = Robolectric.application.getContentResolver().openInputStream(Uri.parse("content:/path"));
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        assertEquals("Bitmap for content:/path", shadowOf(bitmap).getDescription());
        assertEquals(100, bitmap.getWidth());
        assertEquals(100, bitmap.getHeight());
    }

    @Test
    public void decodeResource_shouldGetWidthAndHeightFromHints() throws Exception {
        ShadowBitmapFactory.provideWidthAndHeightHints(R.drawable.an_image, 123, 456);

        Bitmap bitmap = BitmapFactory.decodeResource(Robolectric.application.getResources(), R.drawable.an_image);
        assertEquals("Bitmap for resource:drawable/an_image", shadowOf(bitmap).getDescription());
        assertEquals(123, bitmap.getWidth());
        assertEquals(456, bitmap.getHeight());
    }

    @Test
    public void decodeFile_shouldGetWidthAndHeightFromHints() throws Exception {
        ShadowBitmapFactory.provideWidthAndHeightHints("/some/file.jpg", 123, 456);

        Bitmap bitmap = BitmapFactory.decodeFile("/some/file.jpg");
        assertEquals("Bitmap for file:/some/file.jpg", shadowOf(bitmap).getDescription());
        assertEquals(123, bitmap.getWidth());
        assertEquals(456, bitmap.getHeight());
    }

    @Test
    public void decodeFileEtc_shouldSetOptionsOutWidthAndOutHeightFromHints() throws Exception {
        ShadowBitmapFactory.provideWidthAndHeightHints("/some/file.jpg", 123, 456);

        BitmapFactory.Options options = new BitmapFactory.Options();
        BitmapFactory.decodeFile("/some/file.jpg", options);
        assertEquals(123, options.outWidth);
        assertEquals(456, options.outHeight);
    }

    @Test
    public void decodeUri_shouldGetWidthAndHeightFromHints() throws Exception {
        ShadowBitmapFactory.provideWidthAndHeightHints(Uri.parse("content:/path"), 123, 456);

        Bitmap bitmap = MediaStore.Images.Media.getBitmap(Robolectric.application.getContentResolver(), Uri.parse("content:/path"));
        assertEquals("Bitmap for content:/path", shadowOf(bitmap).getDescription());
        assertEquals(123, bitmap.getWidth());
        assertEquals(456, bitmap.getHeight());
    }

    @Test
    public void decodeStream_shouldGetWidthAndHeightFromHints() throws Exception {
        ShadowBitmapFactory.provideWidthAndHeightHints(Uri.parse("content:/path"), 123, 456);

        InputStream inputStream = Robolectric.application.getContentResolver().openInputStream(Uri.parse("content:/path"));
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        assertEquals("Bitmap for content:/path", shadowOf(bitmap).getDescription());
        assertEquals(123, bitmap.getWidth());
        assertEquals(456, bitmap.getHeight());
    }
}
