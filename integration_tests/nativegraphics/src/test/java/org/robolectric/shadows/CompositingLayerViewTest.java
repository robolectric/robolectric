package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RecordingCanvas;
import android.graphics.RenderNode;
import android.view.View;
import android.view.ViewGroup;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@Config(minSdk = Q)
@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CompositingLayerViewTest {

  private static final int WIDTH = 100;
  private static final int HEIGHT = 100;

  @Test
  public void test1_drawsCompositingLayer() {
    runCompositingSimulation();
  }

  @Test
  public void test2_drawsCompositingLayerAgain() {
    runCompositingSimulation();
  }

  private void runCompositingSimulation() {
    ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class).setup();
    Activity activity = controller.get();
    ViewGroup parent = activity.findViewById(android.R.id.content);
    CompositingLayerView myView = new CompositingLayerView(activity);
    parent.addView(myView, new ViewGroup.LayoutParams(WIDTH, HEIGHT));

    parent.measure(
        View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(470, View.MeasureSpec.EXACTLY));
    parent.layout(0, 0, 320, 470);

    ShadowLooper.idleMainLooper();

    Bitmap largeBitmap =
        Bitmap.createBitmap(parent.getWidth(), parent.getHeight(), Bitmap.Config.ARGB_8888);
    HardwareRenderingScreenshot.takeScreenshot(parent, largeBitmap);
    Bitmap bitmap = Bitmap.createBitmap(largeBitmap, 0, 0, WIDTH, HEIGHT);
    assertThat(hasColor(bitmap, Color.RED)).isTrue();
    assertThat(hasColor(bitmap, Color.BLUE)).isTrue();
  }

  private static boolean hasColor(Bitmap bitmap, int color) {
    for (int y = 0; y < bitmap.getHeight(); y++) {
      for (int x = 0; x < bitmap.getWidth(); x++) {
        if (bitmap.getPixel(x, y) == color) {
          return true;
        }
      }
    }
    return false;
  }

  private static class CompositingLayerView extends View {
    private RenderNode childNode;

    public CompositingLayerView(Context context) {
      super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);
      canvas.drawColor(Color.RED);

      if (childNode == null) {
        childNode = new RenderNode("ChildNode");
        childNode.setPosition(10, 10, 90, 90);
        childNode.setUseCompositingLayer(true, null);

        RecordingCanvas childCanvas = childNode.beginRecording();
        childCanvas.drawColor(Color.BLUE);
        childNode.endRecording();
      }

      if (canvas.isHardwareAccelerated()) {
        canvas.drawRenderNode(childNode);
      }
    }
  }
}
