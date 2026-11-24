package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.PixelCopy;
import android.view.View;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.GraphicsMode.Mode;
import org.robolectric.junit.rules.SetSystemPropertyRule;

/**
 * Checks the pixels that PixelCopy produces from a hardware rendered window, for destinations that
 * receive the rendered frame as it is and for destinations that need it cropped, scaled or
 * converted.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 36)
@GraphicsMode(Mode.NATIVE)
public class ShadowPixelCopyHardwareCopyTest {
  private static final int TRANSLUCENT_BLUE = 0x800000FF;

  @Rule public SetSystemPropertyRule properties = new SetSystemPropertyRule();

  @Before
  public void selectLegacyHardwareCapturePath() {
    // This suite verifies the legacy copy path; SDK 37 always uses real draw traversals.
    properties.set("robolectric.useRealDrawTraversals", "false");
    ShadowView.setUseRealDrawTraversals(false);
    assertThat(ShadowView.areRealDrawTraversalsEnabled()).isFalse();
    properties.set("robolectric.pixelCopyRenderMode", "hardware");
  }

  @Test
  public void windowSizedDestination_receivesEveryPixel() {
    try (ActivityController<RedAndBlueActivity> controller = launch(RedAndBlueActivity.class)) {
      Activity activity = controller.get();
      View decorView = activity.getWindow().getDecorView();
      Bitmap destination =
          Bitmap.createBitmap(decorView.getWidth(), decorView.getHeight(), Bitmap.Config.ARGB_8888);

      assertThat(copy(activity, null, destination)).isEqualTo(PixelCopy.SUCCESS);

      assertRedAndBlueHalves(destination, contentBounds(activity));
    }
  }

  @Test
  public void windowSizedDestination_wholeWindowSourceRect_receivesEveryPixel() {
    try (ActivityController<RedAndBlueActivity> controller = launch(RedAndBlueActivity.class)) {
      Activity activity = controller.get();
      View decorView = activity.getWindow().getDecorView();
      Rect wholeWindow = new Rect(0, 0, decorView.getWidth(), decorView.getHeight());
      Bitmap destination =
          Bitmap.createBitmap(wholeWindow.width(), wholeWindow.height(), Bitmap.Config.ARGB_8888);

      assertThat(copy(activity, wholeWindow, destination)).isEqualTo(PixelCopy.SUCCESS);

      assertRedAndBlueHalves(destination, contentBounds(activity));
    }
  }

  @Test
  public void windowSizedDestination_replacesPreviousContent() {
    try (ActivityController<TranslucentActivity> controller = launch(TranslucentActivity.class)) {
      Activity activity = controller.get();
      View decorView = activity.getWindow().getDecorView();
      Bitmap destination =
          Bitmap.createBitmap(decorView.getWidth(), decorView.getHeight(), Bitmap.Config.ARGB_8888);
      destination.eraseColor(Color.GREEN);

      assertThat(copy(activity, null, destination)).isEqualTo(PixelCopy.SUCCESS);

      Rect content = contentBounds(activity);
      assertThat(destination.getPixel(content.centerX(), content.centerY()))
          .isEqualTo(TRANSLUCENT_BLUE);
    }
  }

  @Test
  public void croppedSourceRect_replacesPreviousContent() {
    try (ActivityController<TranslucentActivity> controller = launch(TranslucentActivity.class)) {
      Activity activity = controller.get();
      Bitmap destination = Bitmap.createBitmap(8, 6, Bitmap.Config.ARGB_8888);
      destination.eraseColor(Color.GREEN);

      assertThat(copy(activity, middleRows(contentBounds(activity)), destination))
          .isEqualTo(PixelCopy.SUCCESS);

      assertEveryPixel(destination, TRANSLUCENT_BLUE);
    }
  }

  @Test
  public void windowSizedDestination_remainsWritable() {
    try (ActivityController<RedAndBlueActivity> controller = launch(RedAndBlueActivity.class)) {
      Activity activity = controller.get();
      View decorView = activity.getWindow().getDecorView();
      Bitmap destination =
          Bitmap.createBitmap(decorView.getWidth(), decorView.getHeight(), Bitmap.Config.ARGB_8888);

      assertThat(copy(activity, null, destination)).isEqualTo(PixelCopy.SUCCESS);

      assertThat(destination.isRecycled()).isFalse();
      destination.eraseColor(Color.GREEN);
      assertThat(destination.getPixel(0, 0)).isEqualTo(Color.GREEN);
    }
  }

  @Test
  public void windowSizedDestination_otherConfig_isConverted() {
    try (ActivityController<RedAndBlueActivity> controller = launch(RedAndBlueActivity.class)) {
      Activity activity = controller.get();
      View decorView = activity.getWindow().getDecorView();
      Bitmap destination =
          Bitmap.createBitmap(decorView.getWidth(), decorView.getHeight(), Bitmap.Config.RGB_565);

      assertThat(copy(activity, null, destination)).isEqualTo(PixelCopy.SUCCESS);

      assertRedAndBlueHalves(destination, contentBounds(activity));
    }
  }

  @Test
  public void croppedSourceRect_isScaledToDestination() {
    try (ActivityController<RedAndBlueActivity> controller = launch(RedAndBlueActivity.class)) {
      Activity activity = controller.get();
      Rect blueHalf = middleRows(contentBounds(activity));
      blueHalf.left = blueHalf.centerX();
      Bitmap destination = Bitmap.createBitmap(8, 6, Bitmap.Config.ARGB_8888);

      assertThat(copy(activity, blueHalf, destination)).isEqualTo(PixelCopy.SUCCESS);

      assertEveryPixel(destination, Color.BLUE);
    }
  }

  private static <T extends Activity> ActivityController<T> launch(Class<T> activityClass) {
    ActivityController<T> controller = Robolectric.buildActivity(activityClass).setup();
    assertThat(
            HardwareRenderingScreenshot.canTakeScreenshot(
                controller.get().getWindow().getDecorView()))
        .isTrue();
    return controller;
  }

  private static int copy(Activity activity, Rect sourceRect, Bitmap destination) {
    AtomicInteger result = new AtomicInteger(-1);
    PixelCopy.request(
        activity.getWindow(),
        sourceRect,
        destination,
        result::set,
        new Handler(Looper.getMainLooper()));
    return result.get();
  }

  /** The bounds of the activity's content view in its window. */
  private static Rect contentBounds(Activity activity) {
    View content = activity.findViewById(android.R.id.content);
    int[] location = new int[2];
    content.getLocationInWindow(location);
    Rect bounds =
        new Rect(
            location[0],
            location[1],
            location[0] + content.getWidth(),
            location[1] + content.getHeight());
    assertThat(bounds.width()).isAtLeast(2);
    assertThat(bounds.height()).isAtLeast(4);
    return bounds;
  }

  /** The middle rows of the content, which the system bars do not cover. */
  private static Rect middleRows(Rect content) {
    return new Rect(
        content.left,
        content.centerY() - content.height() / 4,
        content.right,
        content.centerY() + content.height() / 4);
  }

  private static void assertEveryPixel(Bitmap bitmap, int color) {
    for (int y = 0; y < bitmap.getHeight(); y++) {
      for (int x = 0; x < bitmap.getWidth(); x++) {
        assertThat(bitmap.getPixel(x, y)).isEqualTo(color);
      }
    }
  }

  /**
   * Checks a window sized copy at both ends of each half of {@link RedAndBlueView}, in a row that
   * the system bars at the top and bottom of the window do not cover.
   */
  private static void assertRedAndBlueHalves(Bitmap windowCopy, Rect content) {
    int middle = content.centerX();
    int row = content.centerY();
    assertThat(windowCopy.getPixel(content.left, row)).isEqualTo(Color.RED);
    assertThat(windowCopy.getPixel(middle - 1, row)).isEqualTo(Color.RED);
    assertThat(windowCopy.getPixel(middle, row)).isEqualTo(Color.BLUE);
    assertThat(windowCopy.getPixel(content.right - 1, row)).isEqualTo(Color.BLUE);
  }

  /** Shows a {@link RedAndBlueView} as its content. */
  static final class RedAndBlueActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(new RedAndBlueView(this));
    }
  }

  /** Shows a translucent blue view in a window that has no background. */
  static final class TranslucentActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      setTheme(android.R.style.Theme_Translucent_NoTitleBar);
      super.onCreate(savedInstanceState);
      View content = new View(this);
      content.setBackgroundColor(TRANSLUCENT_BLUE);
      setContentView(content);
    }
  }

  /** Draws its left half red and its right half blue. */
  private static final class RedAndBlueView extends View {
    private final Paint paint = new Paint();

    RedAndBlueView(Context context) {
      super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
      int middle = getWidth() / 2;
      paint.setColor(Color.RED);
      canvas.drawRect(0, 0, middle, getHeight(), paint);
      paint.setColor(Color.BLUE);
      canvas.drawRect(middle, 0, getWidth(), getHeight(), paint);
    }
  }
}
