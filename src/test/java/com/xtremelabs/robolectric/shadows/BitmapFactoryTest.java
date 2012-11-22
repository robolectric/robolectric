package com.xtremelabs.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class BitmapFactoryTest {
    @Test
    public void decodeResource_shouldSetDescription() throws Exception {
        Bitmap bitmap = BitmapFactory.decodeResource(Robolectric.application.getResources(), R.drawable.an_image);
        assertEquals("Bitmap for resource:com.xtremelabs.robolectric:drawable/an_image", shadowOf(bitmap).getDescription());
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
    public void decodeFile_ifFileExists_shouldSetDescriptionToContentsOfFile() throws Exception {
        File tempFile = File.createTempFile("temp-image", ".jpg");
        writeTo(tempFile, "image bytes", " more image bytes");

        Bitmap bitmap = BitmapFactory.decodeFile(tempFile.getPath());
        assertEquals("Bitmap for image bytes more image bytes", shadowOf(bitmap).getDescription());
        assertEquals(100, bitmap.getWidth());
        assertEquals(100, bitmap.getHeight());
    }

    @Test
    public void decodeFile_ifFileExists_shouldSetDescriptionToContentsOfFile_UsingOptions() throws Exception {
        File tempFile = File.createTempFile("temp-image", ".jpg");
        writeTo(tempFile, "image bytes", " more image bytes");

        Bitmap bitmap = BitmapFactory.decodeFile(tempFile.getPath(), new BitmapFactory.Options());
        assertEquals("Bitmap for image bytes more image bytes", shadowOf(bitmap).getDescription());
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
    public void decodeStream_shouldSetDescriptionWithNullOptions() throws Exception {
        InputStream inputStream = Robolectric.application.getContentResolver().openInputStream(Uri.parse("content:/path"));
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, null);
        assertEquals("Bitmap for content:/path", shadowOf(bitmap).getDescription());
        assertEquals(100, bitmap.getWidth());
        assertEquals(100, bitmap.getHeight());
    }

    @Test
    public void decodeResource_shouldGetWidthAndHeightFromHints() throws Exception {
        ShadowBitmapFactory.provideWidthAndHeightHints(R.drawable.an_image, 123, 456);

        Bitmap bitmap = BitmapFactory.decodeResource(Robolectric.application.getResources(), R.drawable.an_image);
        assertEquals("Bitmap for resource:com.xtremelabs.robolectric:drawable/an_image", shadowOf(bitmap).getDescription());
        assertEquals(123, bitmap.getWidth());
        assertEquals(456, bitmap.getHeight());
    }

    @Test
    public void decodeResource_canTakeOptions() throws Exception {
    	BitmapFactory.Options options = new BitmapFactory.Options();
    	options.inSampleSize = 100;
        Bitmap bitmap = BitmapFactory.decodeResource(Robolectric.application.getResources(), R.drawable.an_image, options);
        assertEquals(true, shadowOf(bitmap).getDescription().contains("inSampleSize=100"));
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
    public void decodeByteArray_shouldGetWidthAndHeightFromHints() throws Exception {
        String data = "arbitrary bytes";
        ShadowBitmapFactory.provideWidthAndHeightHints(Uri.parse(data), 123, 456);

        byte[] bytes = data.getBytes();
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        assertEquals("Bitmap for " + data, shadowOf(bitmap).getDescription());
        assertEquals(123, bitmap.getWidth());
        assertEquals(456, bitmap.getHeight());
    }

    @Test
    public void decodeByteArray_shouldIncludeOffsets() throws Exception {
        String data = "arbitrary bytes";
        ShadowBitmapFactory.provideWidthAndHeightHints(Uri.parse(data), 123, 456);

        byte[] bytes = data.getBytes();
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 1, bytes.length - 2);
        assertEquals("Bitmap for " + data + " bytes 1..13", shadowOf(bitmap).getDescription());
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
    
    @Test
    public void decodeByteArray_shouldSetDataChecksum() throws Exception {
        byte[] data = { 23, -125, 0, 52, 23, 18, 76, 43 };
         
        Bitmap bitmap = ShadowBitmapFactory.decodeByteArray(data, 0, data.length);
        assertThat( bitmap, notNullValue() );
        assertThat( shadowOf(bitmap).getDescription(), equalTo( "Bitmap for byte array, checksum: 3693078531" ) );
        assertThat( bitmap.getWidth(), equalTo(100) );
        assertThat( bitmap.getHeight(), equalTo(100) );
    
    }
    
    @Test
    public void decodeByteArray_withOptionsShouldSetDataChecksum() throws Exception {
        byte[] data = { 23, -125, 0, 52, 23, 18, 76, 43 };
    
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap bitmap = ShadowBitmapFactory.decodeByteArray(data, 0, data.length - 1, options);
        assertThat( shadowOf(bitmap).getDescription(), equalTo( "Bitmap for byte array, checksum: 3693078531 bytes 0..7 with options inSampleSize=4" ) );
        assertThat( bitmap.getWidth(), equalTo(25) );
        assertThat( bitmap.getHeight(), equalTo(25) );
    }
    
    @Test
    public void decodeWithDifferentSampleSize() {
        String name = "test";
        BitmapFactory.Options options = new BitmapFactory.Options();
        
        options.inSampleSize = 0;    	
        Bitmap bm = ShadowBitmapFactory.create(name, options);
        assertThat( bm.getWidth(), equalTo(100) );
        assertThat( bm.getHeight(), equalTo(100) );
    
        options.inSampleSize = 2;    	
        bm = ShadowBitmapFactory.create(name, options);
        assertThat( bm.getWidth(), equalTo(50) );
        assertThat( bm.getHeight(), equalTo(50) );
        
        options.inSampleSize = 101;    	
        bm = ShadowBitmapFactory.create(name, options);
        assertThat( bm.getWidth(), equalTo(1) );
        assertThat( bm.getHeight(), equalTo(1) );
    }

    //////////////////

    private void writeTo(File tempFile, String... strings) throws IOException {
        FileWriter fileWriter = new FileWriter(tempFile);
        for (String s : strings) {
            fileWriter.write(s);
        }
        fileWriter.close();
    }
}
