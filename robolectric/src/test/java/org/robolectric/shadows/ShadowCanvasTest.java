package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowPath.Point.Type.LINE_TO;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowCanvasTest {
  private Bitmap targetBitmap;
  private Bitmap imageBitmap;

  @Before
  public void setUp() throws Exception {
    targetBitmap = Shadow.newInstanceOf(Bitmap.class);
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
  public void shouldDescribeBitmapDrawing_withDestinationRectF() throws Exception {
    Canvas canvas = new Canvas(targetBitmap);
    canvas.drawBitmap(imageBitmap, new Rect(1,2,3,4), new RectF(5.0f,6.0f,7.5f,8.5f), new Paint());

    assertEquals("Bitmap for file:/an/image.jpg at (5.0,6.0) with height=2.5 and width=2.5 taken from Rect(1, 2 - 3, 4)", shadowOf(canvas).getDescription());
  }

  @Test
  public void shouldDescribeBitmapDrawing_WithMatrix() throws Exception {
    Canvas canvas = new Canvas(targetBitmap);
    canvas.drawBitmap(imageBitmap, new Matrix(), new Paint());
    canvas.drawBitmap(imageBitmap, new Matrix(), new Paint());

    assertEquals("Bitmap for file:/an/image.jpg transformed by Matrix[pre=[], set={}, post=[]]\n" +
        "Bitmap for file:/an/image.jpg transformed by Matrix[pre=[], set={}, post=[]]", shadowOf(canvas).getDescription());

    assertEquals("Bitmap for file:/an/image.jpg transformed by Matrix[pre=[], set={}, post=[]]\n" +
        "Bitmap for file:/an/image.jpg transformed by Matrix[pre=[], set={}, post=[]]", shadowOf(targetBitmap).getDescription());
  }

  @Test
  public void visualize_shouldReturnDescription() throws Exception {
    Canvas canvas = new Canvas(targetBitmap);
    canvas.drawBitmap(imageBitmap, new Matrix(), new Paint());
    canvas.drawBitmap(imageBitmap, new Matrix(), new Paint());

    assertEquals("Bitmap for file:/an/image.jpg transformed by Matrix[pre=[], set={}, post=[]]\n" +
        "Bitmap for file:/an/image.jpg transformed by Matrix[pre=[], set={}, post=[]]", ShadowCanvas.visualize(canvas));

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
    paint.setColor(Color.RED);
    paint.setAlpha(7);
    canvas.drawPath(path, paint);

    // changing the values on this Paint shouldn't affect recorded painted path
    paint.setColor(Color.BLUE);
    paint.setAlpha(8);

    ShadowCanvas shadow = shadowOf(canvas);
    assertThat(shadow.getPathPaintHistoryCount()).isEqualTo(1);
    ShadowPath drawnPath = shadowOf(shadow.getDrawnPath(0));
    assertEquals(drawnPath.getPoints().get(0), new ShadowPath.Point(10, 10, LINE_TO));
    Paint drawnPathPaint = shadow.getDrawnPathPaint(0);
    assertThat(drawnPathPaint.getColor()).isEqualTo(Color.RED);
    assertThat(drawnPathPaint.getAlpha()).isEqualTo(7);
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
    assertThat(shadow.getPathPaintHistoryCount()).isEqualTo(2);
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

    assertThat(shadow.getPathPaintHistoryCount()).isEqualTo(0);
    assertThat(shadow.getTextHistoryCount()).isEqualTo(0);
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

    assertThat(shadowCanvas.getTextHistoryCount()).isEqualTo(2);

    assertEquals(1f, shadowCanvas.getDrawnTextEvent(0).x, 0);
    assertEquals(2f, shadowCanvas.getDrawnTextEvent(0).y, 0);
    assertEquals(4f, shadowCanvas.getDrawnTextEvent(1).x, 0);
    assertEquals(6f, shadowCanvas.getDrawnTextEvent(1).y, 0);

    assertEquals(paint, shadowCanvas.getDrawnTextEvent(0).paint);
    assertEquals(paint2, shadowCanvas.getDrawnTextEvent(1).paint);

    assertEquals("hello", shadowCanvas.getDrawnTextEvent(0).text);
    assertEquals("hello 2", shadowCanvas.getDrawnTextEvent(1).text);
  }

  @Test
  public void shouldRecordText_charArrayOverload() throws Exception {
    Canvas canvas = new Canvas();
    Paint paint = new Paint();
    paint.setColor(1);
    canvas.drawText(new char[]{'h', 'e', 'l', 'l', 'o'}, 2, 3, 1f, 2f, paint);
    ShadowCanvas shadowCanvas = shadowOf(canvas);

    assertThat(shadowCanvas.getTextHistoryCount()).isEqualTo(1);

    assertEquals(1f, shadowCanvas.getDrawnTextEvent(0).x, 0);
    assertEquals(2f, shadowCanvas.getDrawnTextEvent(0).y, 0);

    assertEquals(paint, shadowCanvas.getDrawnTextEvent(0).paint);

    assertEquals("llo", shadowCanvas.getDrawnTextEvent(0).text);
  }

  @Test
  public void shouldRecordText_stringWithRangeOverload() throws Exception {
    Canvas canvas = new Canvas();
    Paint paint = new Paint();
    paint.setColor(1);
    canvas.drawText("hello", 1, 4, 1f, 2f, paint);
    ShadowCanvas shadowCanvas = shadowOf(canvas);

    assertThat(shadowCanvas.getTextHistoryCount()).isEqualTo(1);

    assertEquals(1f, shadowCanvas.getDrawnTextEvent(0).x, 0);
    assertEquals(2f, shadowCanvas.getDrawnTextEvent(0).y, 0);

    assertEquals(paint, shadowCanvas.getDrawnTextEvent(0).paint);

    assertEquals("ell", shadowCanvas.getDrawnTextEvent(0).text);
  }

  @Test
  public void shouldRecordText_charSequenceOverload() throws Exception {
    Canvas canvas = new Canvas();
    Paint paint = new Paint();
    paint.setColor(1);
    // StringBuilder implements CharSequence:
    canvas.drawText(new StringBuilder("hello"), 1, 4, 1f, 2f, paint);
    ShadowCanvas shadowCanvas = shadowOf(canvas);

    assertThat(shadowCanvas.getTextHistoryCount()).isEqualTo(1);

    assertEquals(1f, shadowCanvas.getDrawnTextEvent(0).x, 0);
    assertEquals(2f, shadowCanvas.getDrawnTextEvent(0).y, 0);

    assertEquals(paint, shadowCanvas.getDrawnTextEvent(0).paint);

    assertEquals("ell", shadowCanvas.getDrawnTextEvent(0).text);
  }

  @Test
  public void drawCircle_shouldRecordCirclePaintHistoryEvents() throws Exception {
    Canvas canvas = new Canvas();
    Paint paint0 = new Paint();
    Paint paint1 = new Paint();
    canvas.drawCircle(1, 2, 3, paint0);
    canvas.drawCircle(4, 5, 6, paint1);
    ShadowCanvas shadowCanvas = shadowOf(canvas);

    assertThat(shadowCanvas.getDrawnCircle(0).centerX).isEqualTo(1.0f);
    assertThat(shadowCanvas.getDrawnCircle(0).centerY).isEqualTo(2.0f);
    assertThat(shadowCanvas.getDrawnCircle(0).radius).isEqualTo(3.0f);
    assertThat(shadowCanvas.getDrawnCircle(0).paint).isSameAs(paint0);

    assertThat(shadowCanvas.getDrawnCircle(1).centerX).isEqualTo(4.0f);
    assertThat(shadowCanvas.getDrawnCircle(1).centerY).isEqualTo(5.0f);
    assertThat(shadowCanvas.getDrawnCircle(1).radius).isEqualTo(6.0f);
    assertThat(shadowCanvas.getDrawnCircle(1).paint).isSameAs(paint1);
  }

  @Test
  public void drawArc_shouldRecordArcHistoryEvents() throws Exception {
    Canvas canvas = new Canvas();
    RectF oval0 = new RectF();
    RectF oval1 = new RectF();
    Paint paint0 = new Paint();
    Paint paint1 = new Paint();
    canvas.drawArc(oval0, 1f, 2f, true, paint0);
    canvas.drawArc(oval1, 3f, 4f, false, paint1);
    ShadowCanvas shadowCanvas = shadowOf(canvas);

    assertThat(shadowCanvas.getDrawnArc(0).oval).isEqualTo(oval0);
    assertThat(shadowCanvas.getDrawnArc(0).startAngle).isEqualTo(1f);
    assertThat(shadowCanvas.getDrawnArc(0).sweepAngle).isEqualTo(2f);
    assertThat(shadowCanvas.getDrawnArc(0).useCenter).isTrue();
    assertThat(shadowCanvas.getDrawnArc(0).paint).isSameAs(paint0);

    assertThat(shadowCanvas.getDrawnArc(1).oval).isEqualTo(oval1);
    assertThat(shadowCanvas.getDrawnArc(1).startAngle).isEqualTo(3f);
    assertThat(shadowCanvas.getDrawnArc(1).sweepAngle).isEqualTo(4f);
    assertThat(shadowCanvas.getDrawnArc(1).useCenter).isFalse();
    assertThat(shadowCanvas.getDrawnArc(1).paint).isSameAs(paint1);
  }

  @Test
  public void getArcHistoryCount_shouldReturnTotalNumberOfDrawArcEvents() throws Exception {
    Canvas canvas = new Canvas();
    canvas.drawArc(new RectF(), 0f, 0f, true, new Paint());
    canvas.drawArc(new RectF(), 0f, 0f, true, new Paint());
    ShadowCanvas shadowCanvas = shadowOf(canvas);
    assertThat(shadowCanvas.getArcPaintHistoryCount()).isEqualTo(2);
  }

  @Test
  public void getRectHistoryCount_shouldReturnTotalNumberOfDrawRectEvents() throws Exception {
    Canvas canvas = new Canvas();
    canvas.drawRect(1f, 2f, 3f, 4f, new Paint());
    canvas.drawRect(1f, 2f, 3f, 4f, new Paint());
    ShadowCanvas shadowCanvas = shadowOf(canvas);
    assertThat(shadowCanvas.getRectPaintHistoryCount()).isEqualTo(2);
  }

  @Test
  public void getOvalHistoryCount_shouldReturnTotalNumberOfDrawOvalEvents() throws Exception {
    Canvas canvas = new Canvas();
    canvas.drawOval(new RectF(), new Paint());
    canvas.drawOval(new RectF(), new Paint());
    ShadowCanvas shadowCanvas = shadowOf(canvas);
    assertThat(shadowCanvas.getOvalPaintHistoryCount()).isEqualTo(2);
  }

  @Test
  public void getLineHistoryCount_shouldReturnTotalNumberOfDrawLineEvents() throws Exception {
    Canvas canvas = new Canvas();
    canvas.drawLine(0f, 1f, 2f, 3f, new Paint());
    canvas.drawLine(0f, 1f, 2f, 3f, new Paint());
    ShadowCanvas shadowCanvas = shadowOf(canvas);
    assertThat(shadowCanvas.getLinePaintHistoryCount()).isEqualTo(2);
  }

  @Test
  public void drawLine_shouldRecordLineHistoryEvents() throws Exception {
    Canvas canvas = new Canvas();
    Paint paint0 = new Paint();
    paint0.setColor(Color.RED);
    paint0.setStrokeWidth(1.0f);
    Paint paint1 = new Paint();
    paint1.setColor(Color.WHITE);
    paint1.setStrokeWidth(2.0f);

    canvas.drawLine(0f, 2f, 3f, 4f, paint0);
    canvas.drawLine(5f, 6f, 7f, 8f, paint1);
    ShadowCanvas shadowCanvas = shadowOf(canvas);

    assertThat(shadowCanvas.getDrawnLine(0).startX).isEqualTo(0f);
    assertThat(shadowCanvas.getDrawnLine(0).startY).isEqualTo(2f);
    assertThat(shadowCanvas.getDrawnLine(0).stopX).isEqualTo(3f);
    assertThat(shadowCanvas.getDrawnLine(0).stopY).isEqualTo(4f);
    assertThat(shadowCanvas.getDrawnLine(0).paint.getColor()).isEqualTo(Color.RED);
    assertThat(shadowCanvas.getDrawnLine(0).paint.getStrokeWidth()).isEqualTo(1.0f);

    assertThat(shadowCanvas.getDrawnLine(1).startX).isEqualTo(5f);
    assertThat(shadowCanvas.getDrawnLine(1).startY).isEqualTo(6f);
    assertThat(shadowCanvas.getDrawnLine(1).stopX).isEqualTo(7f);
    assertThat(shadowCanvas.getDrawnLine(1).stopY).isEqualTo(8f);
    assertThat(shadowCanvas.getDrawnLine(1).paint.getColor()).isEqualTo(Color.WHITE);
    assertThat(shadowCanvas.getDrawnLine(1).paint.getStrokeWidth()).isEqualTo(2.0f);
  }

  @Test
  public void drawOval_shouldRecordOvalHistoryEvents() throws Exception {
    Canvas canvas = new Canvas();
    RectF oval0 = new RectF();
    RectF oval1 = new RectF();
    Paint paint0 = new Paint();
    paint0.setColor(Color.RED);
    Paint paint1 = new Paint();
    paint1.setColor(Color.WHITE);

    canvas.drawOval(oval0, paint0);
    canvas.drawOval(oval1, paint1);
    ShadowCanvas shadowCanvas = shadowOf(canvas);

    assertThat(shadowCanvas.getDrawnOval(0).oval).isEqualTo(oval0);
    assertThat(shadowCanvas.getDrawnOval(0).paint.getColor()).isEqualTo(Color.RED);

    assertThat(shadowCanvas.getDrawnOval(1).oval).isEqualTo(oval1);
    assertThat(shadowCanvas.getDrawnOval(1).paint.getColor()).isEqualTo(Color.WHITE);
  }

  @Test
  public void drawRect_shouldRecordRectHistoryEvents() throws Exception {
    Canvas canvas = new Canvas();
    Paint paint0 = new Paint();
    paint0.setColor(Color.WHITE);
    Paint paint1 = new Paint();
    paint1.setColor(Color.BLACK);
    RectF rect0 = new RectF(0f, 2f, 3f, 4f);
    RectF rect1 = new RectF(5f, 6f, 7f, 8f);

    canvas.drawRect(0f, 2f, 3f, 4f, paint0);
    canvas.drawRect(5f, 6f, 7f, 8f, paint1);
    ShadowCanvas shadowCanvas = shadowOf(canvas);

    assertThat(shadowCanvas.getDrawnRect(0).left).isEqualTo(0f);
    assertThat(shadowCanvas.getDrawnRect(0).top).isEqualTo(2f);
    assertThat(shadowCanvas.getDrawnRect(0).right).isEqualTo(3f);
    assertThat(shadowCanvas.getDrawnRect(0).bottom).isEqualTo(4f);
    assertThat(shadowCanvas.getDrawnRect(0).rect).isEqualTo(rect0);
    assertThat(shadowCanvas.getDrawnRect(0).paint.getColor()).isEqualTo(Color.WHITE);

    assertThat(shadowCanvas.getDrawnRect(1).left).isEqualTo(5f);
    assertThat(shadowCanvas.getDrawnRect(1).top).isEqualTo(6f);
    assertThat(shadowCanvas.getDrawnRect(1).right).isEqualTo(7f);
    assertThat(shadowCanvas.getDrawnRect(1).bottom).isEqualTo(8f);
    assertThat(shadowCanvas.getDrawnRect(1).rect).isEqualTo(rect1);
    assertThat(shadowCanvas.getDrawnRect(1).paint.getColor()).isEqualTo(Color.BLACK);
  }
}
