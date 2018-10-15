package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.CountDownTimer;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;

@RunWith(AndroidJUnit4.class)
public class ShadowCountDownTimerTest {

  private ShadowCountDownTimer shadowCountDownTimer;
  private CountDownTimer countDownTimer;
  private long millisInFuture = 2000;
  private long countDownInterval = 1000;
  private String msg = null;

  @Before
  public void setUp() throws Exception {

    countDownTimer = new CountDownTimer(millisInFuture, countDownInterval) {

      @Override
      public void onFinish() {
        msg = "onFinish() is called";
      }

      @Override
      public void onTick(long millisUnitilFinished) {
        msg = "onTick() is called";
      }
    };
    shadowCountDownTimer = Shadows.shadowOf(countDownTimer);
  }


  @Test
  public void testInvokeOnTick() {
    assertThat(msg).isNotEqualTo("onTick() is called");
    shadowCountDownTimer.invokeTick(countDownInterval);
    assertThat(msg).isEqualTo("onTick() is called");
  }

  @Test
  public void testInvokeOnFinish() {
    assertThat(msg).isNotEqualTo("onFinish() is called");
    shadowCountDownTimer.invokeFinish();
    assertThat(msg).isEqualTo("onFinish() is called");
  }

  @Test
  public void testStart() {
    assertThat(shadowCountDownTimer.hasStarted()).isFalse();
    CountDownTimer timer = shadowCountDownTimer.start();
    assertThat(timer).isNotNull();
    assertThat(shadowCountDownTimer.hasStarted()).isTrue();
  }

  @Test
  public void testCancel() {
    CountDownTimer timer = shadowCountDownTimer.start();
    assertThat(timer).isNotNull();
    assertThat(shadowCountDownTimer.hasStarted()).isTrue();
    shadowCountDownTimer.cancel();
    assertThat(shadowCountDownTimer.hasStarted()).isFalse();
  }

  @Test
  public void testAccessors() {
    assertThat(shadowCountDownTimer.getCountDownInterval()).isEqualTo(countDownInterval);
    assertThat(shadowCountDownTimer.getMillisInFuture()).isEqualTo(millisInFuture);
  }
}
