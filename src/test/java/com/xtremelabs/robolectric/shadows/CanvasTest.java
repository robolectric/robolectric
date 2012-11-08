package com.xtremelabs.robolectric.shadows;

import android.graphics.*;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static com.xtremelabs.robolectric.shadows.ShadowPath.Point.Type.LINE_TO;
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
    public void shouldDescribeBitmapDrawing_withDestinationRect() throws Exception {
        Canvas canvas = new Canvas(targetBitmap);
        canvas.drawBitmap(imageBitmap, new Rect(1,2,3,4), new Rect(5,6,7,8), new Paint());

        assertEquals("Bitmap for file:/an/image.jpg at (5,6) with height=2 and width=2 taken from Rect(1, 2 - 3, 4)", shadowOf(canvas).getDescription());
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
        assertEquals(shadowOf(shadow.getDrawnPath(0)).getPoints().get(0), new ShadowPath.Point(10, 10, LINE_TO));
        assertThat(shadow.getDrawnPathPaint(0), equalTo(paint));
    }

    @Test
    public void drawPath_shouldRecordThePointsOfEachPathEvenWhenItIsTheSameInstance() throws Exception {
        Canvas canvas = new Canvas(targetBitmap);
        Paint paint = new Paint();
        Path path = new Path();

        path.lineTo(10, 10);
        canvas.drawPath(path, paint);

        path.reset();
        path.lineTo(20, 20);
        canvas.drawPath(path, paint);

        ShadowCanvas shadow = shadowOf(canvas);
        assertThat(shadow.getPathPaintHistoryCount(), equalTo(2));
        assertEquals(shadowOf(shadow.getDrawnPath(0)).getPoints().get(0), new ShadowPath.Point(10, 10, LINE_TO));
        assertEquals(shadowOf(shadow.getDrawnPath(1)).getPoints().get(0), new ShadowPath.Point(20, 20, LINE_TO));
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

    @Test
    public void resetCanvasHistory_shouldClearTheHistoryAndDescription() throws Exception {
        Canvas canvas = new Canvas();
        canvas.drawPath(new Path(), new Paint());
        canvas.drawText("hi", 1, 2, new Paint());

        ShadowCanvas shadow = shadowOf(canvas);
        shadow.resetCanvasHistory();

        assertThat(shadow.getPathPaintHistoryCount(), equalTo(0));
        assertThat(shadow.getTextHistoryCount(), equalTo(0));
        assertEquals("", shadow.getDescription());
    }

    @Test
    public void shouldGetAndSetHeightAndWidth() throws Exception {
        Canvas canvas = new Canvas();
        shadowOf(canvas).setWidth(99);
        shadowOf(canvas).setHeight(42);

        assertEquals(99, canvas.getWidth());
        assertEquals(42, canvas.getHeight());
    }

    @Test
    public void shouldRecordText() throws Exception {
        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        Paint paint2 = new Paint();
        paint.setColor(1);
        paint2.setColor(5);
        canvas.drawText("hello", 1, 2, paint);
        canvas.drawText("hello 2", 4, 6, paint2);
        ShadowCanvas shadowCanvas = shadowOf(canvas);

        assertThat(shadowCanvas.getTextHistoryCount(), equalTo(2));

        assertEquals(1f, shadowCanvas.getDrawnTextEvent(0).x, 0);
        assertEquals(2f, shadowCanvas.getDrawnTextEvent(0).y, 0);
        assertEquals(4f, shadowCanvas.getDrawnTextEvent(1).x, 0);
        assertEquals(6f, shadowCanvas.getDrawnTextEvent(1).y, 0);

        assertEquals(paint, shadowCanvas.getDrawnTextEvent(0).paint);
        assertEquals(paint2, shadowCanvas.getDrawnTextEvent(1).paint);

        assertEquals("hello", shadowCanvas.getDrawnTextEvent(0).text);
        assertEquals("hello 2", shadowCanvas.getDrawnTextEvent(1).text);
    }
}
