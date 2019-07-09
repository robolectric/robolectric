package android.app;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

import android.os.Handler;
import android.os.Looper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.internal.DoNotInstrument;

/**
 * Tests to verify android.app.Instrumentation APIs behave consistently between Robolectric and
 * device.
 */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
@LooperMode(PAUSED)
public final class InstrumentationTest {

  /**
   * Verify that runOnMainSync main looper synchronization is consistent between on device and
   * robolectric.
   */
  @Test
  public void runOnMainSync() {
    final List<String> events = new ArrayList<>();
    Handler mainHandler = new Handler(Looper.getMainLooper());

    mainHandler.post(() -> events.add("before runOnMainSync"));
    getInstrumentation()
        .runOnMainSync(
            new Runnable() {
              @Override
              public void run() {
                events.add("in runOnMainSync");
                // as expected, on device tests become flaky and fail deterministically on
                // Robolectric with this line, as runOnMainSync does not drain the main looper
                // after runnable executes
                // mainHandler.post(() -> events.add("post from runOnMainSync"));
              }
            });

    assertThat(events).containsExactly("before runOnMainSync", "in runOnMainSync").inOrder();
  }
}
