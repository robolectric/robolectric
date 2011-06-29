package com.xtremelabs.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class CanvasTest {
    private Bitmap targetBitmap;
    private Bitmap imageBitmap;

    @Before public void setUp() throws Exception {
        targetBitmap = Robolectric.newInstanceOf(Bitmap.class);
        imageBitmap = BitmapFactory.decodeFile("/an/image.jpg");
    }

    @Test
    public void shouldDescribeBitmapDrawing() throws Exception {
        Canvas canvas = new Canvas(targetBitmap);
        canvas.drawBitmap(imageBitmap, 1, 2, new Paint());
        canvas.drawBitmap(imageBitmap, 100, 200, new Paint());

        assertEquals("Bitmap for file:/an/image.jpg at (1,2)\n" +
                "Bitmap for file:/an/image.jpg at (100,200)", shadowOf(canvas).getDescription());

        assertEquals("Bitmap for file:/an/image.jpg at (1,2)\n" +
                "Bitmap for file:/an/image.jpg at (100,200)", shadowOf(targetBitmap).getDescription());
    }

    @Test
    public void shouldDescribeBitmapDrawing_WithMatrix() throws Exception {
        Canvas canvas = new Canvas(targetBitmap);
        canvas.drawBitmap(imageBitmap, new Matrix(), new Paint());
        canvas.drawBitmap(imageBitmap, new Matrix(), new Paint());

        assertEquals("Bitmap for file:/an/image.jpg transformed by matrix\n" +
                "Bitmap for file:/an/image.jpg transformed by matrix", shadowOf(canvas).getDescription());

        assertEquals("Bitmap for file:/an/image.jpg transformed by matrix\n" +
                "Bitmap for file:/an/image.jpg transformed by matrix", shadowOf(targetBitmap).getDescription());
    }

    @Test
    public void visualize_shouldReturnDescription() throws Exception {
        Canvas canvas = new Canvas(targetBitmap);
        canvas.drawBitmap(imageBitmap, new Matrix(), new Paint());
        canvas.drawBitmap(imageBitmap, new Matrix(), new Paint());

        assertEquals("Bitmap for file:/an/image.jpg transformed by matrix\n" +
                "Bitmap for file:/an/image.jpg transformed by matrix", Robolectric.visualize(canvas));

    }
    
    @Test
    public void drawColor_shouldReturnDescription() throws Exception {
        Canvas canvas = new Canvas(targetBitmap);
        canvas.drawColor( Color.WHITE );
        canvas.drawColor( Color.GREEN );
        canvas.drawColor( Color.TRANSPARENT );
        assertEquals( "draw color -1draw color -16711936draw color 0",
        		shadowOf(canvas).getDescription());
    }
}
