package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.shadows.ShadowCountDownTimer.PROPERTY_USE_REAL_IMPL;

import android.os.CountDownTimer;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.Shadows;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ShadowCountDownTimerTest {
  private static final String MESSAGE_TICK = "onTick() is called";
  private static final String MESSAGE_FINISH = "onFinish() is called";

  private ShadowCountDownTimer shadowCountDownTimer;
  private final long millisInFuture = 2000;
  private final long countDownInterval = 1000;
  private final boolean useRealCountDownTimer;
  private String message;
  private String originalUseRealCountDownTimer;

  public ShadowCountDownTimerTest(boolean useRealCountDownTimer) {
    this.useRealCountDownTimer = useRealCountDownTimer;
  }

  @Before
  public void setUp() throws Exception {
    originalUseRealCountDownTimer = System.getProperty(PROPERTY_USE_REAL_IMPL);

    System.setProperty(PROPERTY_USE_REAL_IMPL, Boolean.toString(useRealCountDownTimer));

    CountDownTimer countDownTimer =
        new CountDownTimer(millisInFuture, countDownInterval) {

          @Override
          public void onFinish() {
            message = MESSAGE_FINISH;
          }

          @Override
          public void onTick(long millisUnitilFinished) {
            message = MESSAGE_TICK;
          }
        };
    shadowCountDownTimer = Shadows.shadowOf(countDownTimer);
  }

  @After
  public void tearDown() {
    if (originalUseRealCountDownTimer != null) {
      System.setProperty(PROPERTY_USE_REAL_IMPL, originalUseRealCountDownTimer);
    } else {
      System.clearProperty(PROPERTY_USE_REAL_IMPL);
    }
  }

  @Test
  public void testInvokeOnTick() {
    assertThat(message).isNull();
    shadowCountDownTimer.invokeTick(countDownInterval);
    assertThat(message).isEqualTo(MESSAGE_TICK);
  }

  @Test
  public void testInvokeOnFinish() {
    assertThat(message).isNull();
    shadowCountDownTimer.invokeFinish();
    assertThat(message).isEqualTo(MESSAGE_FINISH);
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
    assertThat(shadowCountDownTimer.isCancelled()).isFalse();
    assertThat(shadowCountDownTimer.hasStarted()).isFalse();
    CountDownTimer timer = shadowCountDownTimer.start();
    assertThat(timer).isNotNull();
    assertThat(shadowCountDownTimer.isCancelled()).isFalse();
    assertThat(shadowCountDownTimer.hasStarted()).isTrue();
    shadowCountDownTimer.cancel();
    assertThat(shadowCountDownTimer.isCancelled()).isTrue();
    assertThat(shadowCountDownTimer.hasStarted()).isFalse();
  }

  @Test
  public void testAccessors() {
    assertThat(shadowCountDownTimer.getCountDownInterval()).isEqualTo(countDownInterval);
    assertThat(shadowCountDownTimer.getMillisInFuture()).isEqualTo(millisInFuture);
  }

  @ParameterizedRobolectricTestRunner.Parameters(name = "useRealCountDownTimer = {0}")
  public static Iterable<?> data() {
    return Arrays.asList(true, false);
  }
}
