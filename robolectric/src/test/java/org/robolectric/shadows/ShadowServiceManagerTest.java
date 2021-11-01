package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;

import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.android.internal.view.IInputMethodManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ShadowServiceManager}. */
@RunWith(AndroidJUnit4.class)
public final class ShadowServiceManagerTest {

  @Test
  public void getService_available_shouldReturnNonNull() {
    assertThat(ServiceManager.getService(Context.INPUT_METHOD_SERVICE)).isNotNull();
  }

  @Test
  public void getService_unavailableService_shouldReturnNull() {
    ShadowServiceManager.setServiceAvailability(Context.INPUT_METHOD_SERVICE, false);
    assertThat(ServiceManager.getService(Context.INPUT_METHOD_SERVICE)).isNull();
  }

  @Test
  public void getService_multipleThreads_binderRace() throws Exception {
    ExecutorService e = Executors.newFixedThreadPool(4);
    final AtomicReference<Exception> thrownException = new AtomicReference<>();
    for (int i = 0; i < 10; i++) {
      e.execute(
          () -> {
            try {
              IBinder b = ServiceManager.getService(Context.INPUT_METHOD_SERVICE);
              IInputMethodManager.Stub.asInterface(b);
            } catch (RuntimeException ex) {
              thrownException.compareAndSet(null, ex);
            }
          });
    }
    e.shutdown();
    e.awaitTermination(10, SECONDS);
    assertThat(thrownException.get()).isNull();
  }
}
