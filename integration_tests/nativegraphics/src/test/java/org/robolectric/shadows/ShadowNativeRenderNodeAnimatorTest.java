package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.CanvasProperty;
import android.graphics.Paint;
import android.view.RenderNodeAnimator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = O)
public class ShadowNativeRenderNodeAnimatorTest {
  @Test
  public void testConstruction() {
    Paint paint = new Paint();
    CanvasProperty<Paint> prop = CanvasProperty.createPaint(paint);
    RenderNodeAnimator opacityAnim =
        new RenderNodeAnimator(prop, RenderNodeAnimator.PAINT_ALPHA, 0);
    opacityAnim.setStartDelay(100L);
    opacityAnim.setStartValue(0f);
  }
}
