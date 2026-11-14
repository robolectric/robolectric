package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.CINNAMON_BUN;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.HardwareRendererObserver;
import android.graphics.HardwareRendererObserver.OnFrameMetricsAvailableListener;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Config(minSdk = R)
@RunWith(RobolectricTestRunner.class)
public class ShadowNativeHardwareRendererObserverTest {

  @SuppressWarnings("CheckReturnValue")
  @Test
  public void test_hardwareRenderer() {
    OnFrameMetricsAvailableListener listener = i -> {};
    createObserver(listener, new long[0], new Handler());
  }

  @Config(minSdk = CINNAMON_BUN)
  @Test
  public void addObserver_receivesFrameMetrics() throws Exception {
    Activity activity = Robolectric.setupActivity(Activity.class);
    View view = new View(activity);
    view.setBackgroundColor(Color.RED);
    activity.setContentView(view);
    shadowOf(Looper.getMainLooper()).idle();

    AtomicInteger framesReported = new AtomicInteger();
    HardwareRendererObserver observer =
        createObserver(
            dropCount -> framesReported.incrementAndGet(),
            new long[frameStatsCount()],
            new Handler(Looper.getMainLooper()));
    ReflectionHelpers.callInstanceMethod(
        ReflectionHelpers.callInstanceMethod(view, "getThreadedRenderer"),
        "addObserver",
        ClassParameter.from(HardwareRendererObserver.class, observer));

    view.invalidate();
    shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(100));
    // HWUI reports metrics from its render thread, which posts them to the observer's handler.
    for (int i = 0; i < 100 && framesReported.get() == 0; i++) {
      Thread.sleep(10);
      shadowOf(Looper.getMainLooper()).idle();
    }

    assertThat(framesReported.get()).isGreaterThan(0);
  }

  /** The length of the frame metrics array that HWUI expects, which varies by SDK. */
  private static int frameStatsCount() throws ClassNotFoundException {
    return ReflectionHelpers.getStaticField(
        Class.forName("android.view.FrameMetrics$Index"), "FRAME_STATS_COUNT");
  }

  private static HardwareRendererObserver createObserver(
      OnFrameMetricsAvailableListener listener, long[] frameMetrics, Handler handler) {
    if (RuntimeEnvironment.getApiLevel() >= S) {
      return new HardwareRendererObserver(listener, frameMetrics, handler, false);
    } else {
      return ReflectionHelpers.callConstructor(
          HardwareRendererObserver.class,
          ClassParameter.from(OnFrameMetricsAvailableListener.class, listener),
          ClassParameter.from(long[].class, frameMetrics),
          ClassParameter.from(Handler.class, handler));
    }
  }
}
