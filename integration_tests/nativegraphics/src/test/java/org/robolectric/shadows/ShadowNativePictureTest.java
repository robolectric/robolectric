package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Picture;
import android.graphics.Rect;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativePictureTest {

  private static final int TEST_WIDTH = 4; // must be >= 2
  private static final int TEST_HEIGHT = 3; // must >= 2

  private final Rect mClipRect = new Rect(0, 0, 2, 2);

  // This method tests out some edge cases w.r.t. Picture creation.
  // In particular, this test verifies that, in the following situations,
  // the created picture (effectively) has balanced saves and restores:
  //   - copy constructed picture from actively recording picture
  //   - actively recording picture after draw call
  @Test
  public void testSaveRestoreBalance() {
    Picture original = new Picture();
    Canvas canvas = original.beginRecording(TEST_WIDTH, TEST_HEIGHT);
    assertNotNull(canvas);
    createImbalance(canvas);

    int expectedSaveCount = canvas.getSaveCount();

    Picture copy = new Picture(original);
    verifyBalance(copy);

    assertEquals(expectedSaveCount, canvas.getSaveCount());

    Bitmap bitmap = Bitmap.createBitmap(TEST_WIDTH, TEST_HEIGHT, Bitmap.Config.ARGB_8888);
    Canvas drawDest = new Canvas(bitmap);
    original.draw(drawDest);
    verifyBalance(original);
  }

  // Add an extra save with a transform and clip
  private void createImbalance(Canvas canvas) {
    canvas.save();
    canvas.clipRect(mClipRect);
    canvas.translate(1.0f, 1.0f);
    Paint paint = new Paint();
    paint.setColor(Color.GREEN);
    canvas.drawRect(0, 0, 10, 10, paint);
  }

  private void verifyBalance(Picture picture) {
    Bitmap bitmap = Bitmap.createBitmap(TEST_WIDTH, TEST_HEIGHT, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);

    int beforeSaveCount = canvas.getSaveCount();

    final Matrix beforeMatrix = canvas.getMatrix();

    Rect beforeClip = new Rect();
    assertTrue(canvas.getClipBounds(beforeClip));

    canvas.drawPicture(picture);

    assertEquals(beforeSaveCount, canvas.getSaveCount());

    assertEquals(beforeMatrix, canvas.getMatrix());

    Rect afterClip = new Rect();

    assertTrue(canvas.getClipBounds(afterClip));
    assertEquals(beforeClip, afterClip);
  }

  @Test
  public void testPicture() {
    Picture picture = new Picture();

    Canvas canvas = picture.beginRecording(TEST_WIDTH, TEST_HEIGHT);
    assertNotNull(canvas);
    drawPicture(canvas);
    picture.endRecording();

    Bitmap bitmap = Bitmap.createBitmap(TEST_WIDTH, TEST_HEIGHT, Bitmap.Config.ARGB_8888);
    canvas = new Canvas(bitmap);
    picture.draw(canvas);
    verifySize(picture);
    verifyBitmap(bitmap);

    Picture pic = new Picture(picture);
    bitmap = Bitmap.createBitmap(TEST_WIDTH, TEST_HEIGHT, Bitmap.Config.ARGB_8888);
    canvas = new Canvas(bitmap);
    pic.draw(canvas);
    verifySize(pic);
    verifyBitmap(bitmap);
  }

  @Test(expected = IllegalStateException.class)
  @Config(minSdk = P) // This did not exist in O or O_MR1
  public void testBeginRecordingTwice() {
    Picture picture = new Picture();
    picture.beginRecording(10, 10);
    picture.beginRecording(10, 10);
  }

  private void verifySize(Picture picture) {
    assertEquals(TEST_WIDTH, picture.getWidth());
    assertEquals(TEST_HEIGHT, picture.getHeight());
  }

  private void drawPicture(Canvas canvas) {
    Paint paint = new Paint();
    // GREEN rectangle covering the entire canvas
    paint.setColor(Color.GREEN);
    paint.setStyle(Style.FILL);
    paint.setAntiAlias(false);
    canvas.drawRect(0, 0, TEST_WIDTH, TEST_HEIGHT, paint);
    // horizontal red line starting from (0,0); overwrites first line of the rectangle
    paint.setColor(Color.RED);
    canvas.drawLine(0, 0, TEST_WIDTH, 0, paint);
    // overwrite (0,0) with a blue dot
    paint.setColor(Color.BLUE);
    canvas.drawPoint(0, 0, paint);
  }

  private void verifyBitmap(Bitmap bitmap) {
    // first pixel is BLUE, rest of the line is RED
    assertEquals(Color.BLUE, bitmap.getPixel(0, 0));
    for (int x = 1; x < TEST_WIDTH; x++) {
      assertEquals(Color.RED, bitmap.getPixel(x, 0));
    }
    // remaining lines are all green
    for (int y = 1; y < TEST_HEIGHT; y++) {
      for (int x = 0; x < TEST_WIDTH; x++) {
        assertEquals(Color.GREEN, bitmap.getPixel(x, y));
      }
    }
  }
}
