package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;

import android.content.Context;
import android.os.Binder;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.os.ServiceManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.android.internal.view.IInputMethodManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.versioning.AndroidVersions.V;

/** Tests for {@link ShadowServiceManager}. */
@RunWith(AndroidJUnit4.class)
public final class ShadowServiceManagerTest {

  @Test
  @Config(sdk = VERSION_CODES.S)
  public void getSensorPrivacyService_notNull() {
    assertThat(ServiceManager.getService(Context.SENSOR_PRIVACY_SERVICE)).isNotNull();
  }

  @Test
  @Config(sdk = V.SDK_INT)
  public void getSensitiveContentProtectionManager_returnsSomething() {
    // TODO: replace with Context.SENSITIVE_CONTENT_PROTECTION_SERVICE once this test compiles
    // against V
    assertThat(ServiceManager.getService("sensitive_content_protection_service")).isNotNull();
  }

  @Test
  public void getService_available_shouldReturnNonNull() {
    assertThat(ServiceManager.getService(Context.INPUT_METHOD_SERVICE)).isNotNull();
  }

  @Test
  public void getService_unavailable_shouldReturnNull() {
    ShadowServiceManager.setServiceAvailability(Context.INPUT_METHOD_SERVICE, false);

    assertThat(ServiceManager.getService(Context.INPUT_METHOD_SERVICE)).isNull();
  }

  @Test
  public void checkService_available_shouldReturnNonNull() {
    assertThat(ServiceManager.checkService(Context.INPUT_METHOD_SERVICE)).isNotNull();
  }

  @Test
  public void checkService_unavailable_shouldReturnNull() {
    ShadowServiceManager.setServiceAvailability(Context.INPUT_METHOD_SERVICE, false);

    assertThat(ServiceManager.checkService(Context.INPUT_METHOD_SERVICE)).isNull();
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

  @Test
  public void addService_concrete_shouldReturnService() throws RemoteException {
    StubbedInterface expectedStub = new StubbedInterface();
    ShadowServiceManager.addBinderService("activity_task", IStubbedInterface.class, expectedStub);

    IBinder iBinder = ServiceManager.getService("activity_task");
    IStubbedInterface foundStub = StubbedInterface.Stub.asInterface(iBinder);

    foundStub.reportCalled();
    assertThat(expectedStub.called).isTrue();
  }

  // Stub interface to test that the service is returned correctly
  private static class StubbedInterface implements IStubbedInterface {

    IBinder binder = new Binder();
    boolean called = false;

    public abstract static class Stub extends Binder implements IStubbedInterface {
      public static IStubbedInterface asInterface(IBinder obj) {
        IInterface inter = obj.queryLocalInterface(IStubbedInterface.class.getCanonicalName());
        return (IStubbedInterface) inter;
      }
    }

    @Override
    public IBinder asBinder() {
      return binder;
    }

    @Override
    public void reportCalled() {
      called = true;
    }
  }

  interface IStubbedInterface extends IInterface {
    void reportCalled() throws RemoteException;
  }
}
