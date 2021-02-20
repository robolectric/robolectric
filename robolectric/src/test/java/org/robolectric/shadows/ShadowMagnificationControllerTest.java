package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityService.MagnificationController;
import android.graphics.Region;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

/** Test for ShadowMagnificationController. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = N)
public final class ShadowMagnificationControllerTest {

  private MagnificationController magnificationController;

  @Before
  public void setUp() {
    MyService myService = Robolectric.setupService(MyService.class);
    magnificationController = myService.getMagnificationController();
  }

  @Test
  public void getCenterX_byDefault_returns0() {
    assertThat(magnificationController.getCenterX()).isEqualTo(0.0f);
  }

  @Test
  public void getCenterY_byDefault_returns0() {
    assertThat(magnificationController.getCenterY()).isEqualTo(0.0f);
  }

  @Test
  public void getScale_byDefault_returns1() {
    assertThat(magnificationController.getScale()).isEqualTo(1.0f);
  }

  @Test
  public void setCenter_setsCenterX() {
    float newCenterX = 450.0f;

    magnificationController.setCenter(newCenterX, /*centerY=*/ 0.0f, /*animate=*/ false);

    assertThat(magnificationController.getCenterX()).isEqualTo(newCenterX);
  }

  @Test
  public void setCenter_setsCenterY() {
    float newCenterY = 250.0f;

    magnificationController.setCenter(/*centerX=*/ 0.0f, newCenterY, /*animate=*/ false);

    assertThat(magnificationController.getCenterY()).isEqualTo(newCenterY);
  }

  @Test
  public void setCenter_notifiesListener() {
    float centerX = 55f;
    float centerY = 22.5f;
    TestListener testListener = new TestListener();
    magnificationController.addListener(testListener);

    magnificationController.setCenter(centerX, centerY, /*animate=*/ false);

    shadowOf(Looper.getMainLooper()).idle();
    assertThat(testListener.invoked).isTrue();
    assertThat(testListener.centerX).isEqualTo(centerX);
    assertThat(testListener.centerY).isEqualTo(centerY);
  }

  @Test
  public void setScale_setsScale() {
    float newScale = 5.0f;

    magnificationController.setScale(newScale, /*animate=*/ false);

    assertThat(magnificationController.getScale()).isEqualTo(newScale);
  }

  @Test
  public void setScale_notifiesListener() {
    float scale = 5.0f;
    TestListener testListener = new TestListener();
    magnificationController.addListener(testListener);

    magnificationController.setScale(scale, /*animate=*/ false);

    shadowOf(Looper.getMainLooper()).idle();
    assertThat(testListener.invoked).isTrue();
    assertThat(testListener.scale).isEqualTo(scale);
  }

  @Test
  public void reset_resetsCenterX() {
    magnificationController.setCenter(/*centerX=*/ 100.0f, /*centerY=*/ 0.0f, /*animate=*/ false);

    magnificationController.reset(/*animate=*/ false);

    assertThat(magnificationController.getCenterX()).isEqualTo(0.0f);
  }

  @Test
  public void reset_resetsCenterY() {
    magnificationController.setCenter(/*centerX=*/ 0.0f, /*centerY=*/ 100.0f, /*animate=*/ false);

    magnificationController.reset(/*animate=*/ false);

    assertThat(magnificationController.getCenterY()).isEqualTo(0.0f);
  }

  @Test
  public void reset_resetsScale() {
    magnificationController.setScale(5.0f, /*animate=*/ false);

    magnificationController.reset(/*animate=*/ false);

    assertThat(magnificationController.getScale()).isEqualTo(1.0f);
  }

  @Test
  public void reset_notifiesListener() {
    magnificationController.setCenter(/*centerX=*/ 150.5f, /*centerY=*/ 11.5f, /*animate=*/ false);
    magnificationController.setScale(/*scale=*/ 5.0f, /*animate=*/ false);
    TestListener testListener = new TestListener();
    magnificationController.addListener(testListener);

    magnificationController.reset(/*animate=*/ false);

    shadowOf(Looper.getMainLooper()).idle();
    assertThat(testListener.invoked).isTrue();
    assertThat(testListener.centerX).isEqualTo(0.0f);
    assertThat(testListener.centerY).isEqualTo(0.0f);
    assertThat(testListener.scale).isEqualTo(1.0f);
  }

  @Test
  public void removeListener_removesListener() {
    float scale = 5.0f;
    TestListener testListener = new TestListener();
    magnificationController.addListener(testListener);

    magnificationController.removeListener(testListener);

    magnificationController.setScale(scale, /*animate=*/ false);
    shadowOf(Looper.getMainLooper()).idle();
    assertThat(testListener.invoked).isFalse();
  }

  /** Test OnMagnificationChangedListener that records when it's invoked. */
  private static class TestListener
      implements MagnificationController.OnMagnificationChangedListener {

    private boolean invoked = false;
    private float scale = -1f;
    private float centerX = -1f;
    private float centerY = -1f;

    @Override
    public void onMagnificationChanged(
        MagnificationController controller,
        Region region,
        float scale,
        float centerX,
        float centerY) {
      this.invoked = true;
      this.scale = scale;
      this.centerX = centerX;
      this.centerY = centerY;
    }
  }

  /** Empty implementation of AccessibilityService, for test purposes. */
  private static class MyService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent arg0) {
      // Do nothing
    }

    @Override
    public void onInterrupt() {
      // Do nothing
    }
  }
}
