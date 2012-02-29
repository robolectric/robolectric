package com.xtremelabs.robolectric.shadows;

import android.graphics.*;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class CanvasTest {
    private Bitmap targetBitmap;
    private Bitmap imageBitmap;

    @Before
    public void setUp() throws Exception {
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
        canvas.drawColor(Color.WHITE);
        canvas.drawColor(Color.GREEN);
        canvas.drawColor(Color.TRANSPARENT);
        assertEquals("draw color -1draw color -16711936draw color 0",
                shadowOf(canvas).getDescription());
    }

    @Test
    public void drawPath_shouldRecordThePathAndThePaint() throws Exception {
        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.lineTo(10, 10);

        Paint paint = new Paint();
        paint.setAlpha(7);
        canvas.drawPath(path, paint);

        ShadowCanvas shadow = shadowOf(canvas);
        assertThat(shadow.getPathPaintHistoryCount(), equalTo(1));
        assertThat(shadow.getDrawnPath(0), equalTo(path));
        assertThat(shadow.getDrawnPathPaint(0), equalTo(paint));
    }

    @Test
    public void drawPath_shouldAppendDescriptionToBitmap() throws Exception {
        Canvas canvas = new Canvas(targetBitmap);
        Path path1 = new Path();
        path1.lineTo(10, 10);
        path1.moveTo(20, 15);
        Path path2 = new Path();
        path2.moveTo(100, 100);
        path2.lineTo(150, 140);

        Paint paint = new Paint();
        canvas.drawPath(path1, paint);
        canvas.drawPath(path2, paint);

        assertEquals("Path " + shadowOf(path1).getPoints().toString() + "\n"
                + "Path " + shadowOf(path2).getPoints().toString(), shadowOf(canvas).getDescription());

        assertEquals("Path " + shadowOf(path1).getPoints().toString() + "\n"
                + "Path " + shadowOf(path2).getPoints().toString(), shadowOf(targetBitmap).getDescription());
    }
}
