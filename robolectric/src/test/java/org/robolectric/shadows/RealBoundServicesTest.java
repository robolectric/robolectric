package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.junit.rules.SetSystemPropertyRule;

@RunWith(AndroidJUnit4.class)
public class RealBoundServicesTest {
  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  private static final List<String> events = new ArrayList<>();
  private static IBinder binderToReturn;

  private final Application context = ApplicationProvider.getApplicationContext();
  private final ComponentName component = new ComponentName(context, TestService.class);

  @Before
  public void setUp() {
    setSystemPropertyRule.set("robolectric.bindRealServices", "true");
    shadowOf(context.getPackageManager()).addServiceIfNotPresent(component);
    events.clear();
    binderToReturn = new Binder();
  }

  @Test
  public void bindService_createsTheServiceAndDeliversItsBinder() {
    RecordingConnection connection = new RecordingConnection();

    assertThat(context.bindService(serviceIntent(), connection, Context.BIND_AUTO_CREATE)).isTrue();
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(events).containsExactly("onCreate", "onBind").inOrder();
    assertThat(connection.events).containsExactly("connected " + component.getClassName());
    assertThat(connection.binder).isSameInstanceAs(binderToReturn);
  }

  @Test
  public void bindService_twice_sharesTheServiceAndBinder() {
    RecordingConnection first = new RecordingConnection();
    RecordingConnection second = new RecordingConnection();

    context.bindService(serviceIntent(), first, Context.BIND_AUTO_CREATE);
    context.bindService(serviceIntent(), second, Context.BIND_AUTO_CREATE);
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(events).containsExactly("onCreate", "onBind").inOrder();
    assertThat(second.binder).isSameInstanceAs(first.binder);
  }

  @Test
  public void unbindService_lastConnection_unbindsAndDestroysTheService() {
    RecordingConnection first = new RecordingConnection();
    RecordingConnection second = new RecordingConnection();
    context.bindService(serviceIntent(), first, Context.BIND_AUTO_CREATE);
    context.bindService(serviceIntent(), second, Context.BIND_AUTO_CREATE);
    shadowOf(Looper.getMainLooper()).idle();

    context.unbindService(first);
    shadowOf(Looper.getMainLooper()).idle();
    assertThat(events).containsExactly("onCreate", "onBind").inOrder();

    context.unbindService(second);
    shadowOf(Looper.getMainLooper()).idle();
    assertThat(events).containsExactly("onCreate", "onBind", "onUnbind", "onDestroy").inOrder();
    assertThat(first.events).doesNotContain("disconnected " + component.getClassName());
  }

  @Test
  @Config(minSdk = P)
  public void bindService_nullBinder_callsOnNullBinding() {
    binderToReturn = null;
    RecordingConnection connection = new RecordingConnection();

    context.bindService(serviceIntent(), connection, Context.BIND_AUTO_CREATE);
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(connection.events).containsExactly("null binding " + component.getClassName());
  }

  @Test
  public void bindService_withoutAutoCreate_doesNotCreateTheService() {
    RecordingConnection connection = new RecordingConnection();

    assertThat(context.bindService(serviceIntent(), connection, 0)).isTrue();
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(events).isEmpty();
    assertThat(connection.events).isEmpty();
  }

  @Test
  public void bindService_serviceOfAnotherPackage_keepsTheStubbedBehavior() {
    Intent intent = new Intent().setClassName("com.example.other", "com.example.OtherService");
    RecordingConnection connection = new RecordingConnection();

    assertThat(context.bindService(intent, connection, Context.BIND_AUTO_CREATE)).isTrue();
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(connection.events).containsExactly("connected null");
    assertThat(connection.binder).isNull();
  }

  @Test
  public void bindService_stubbedBinder_takesPrecedence() {
    IBinder stub = new Binder();
    shadowOf(context).setComponentNameAndServiceForBindService(component, stub);
    RecordingConnection connection = new RecordingConnection();

    context.bindService(serviceIntent(), connection, Context.BIND_AUTO_CREATE);
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(events).isEmpty();
    assertThat(connection.binder).isSameInstanceAs(stub);
  }

  private Intent serviceIntent() {
    return new Intent(context, TestService.class);
  }

  /** Records its lifecycle in {@link #events}. */
  public static class TestService extends Service {
    @Override
    public void onCreate() {
      events.add("onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
      events.add("onBind");
      return binderToReturn;
    }

    @Override
    public boolean onUnbind(Intent intent) {
      events.add("onUnbind");
      return false;
    }

    @Override
    public void onDestroy() {
      events.add("onDestroy");
    }
  }

  private static final class RecordingConnection implements ServiceConnection {
    final List<String> events = new ArrayList<>();
    IBinder binder;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      events.add("connected " + (name == null ? null : name.getClassName()));
      binder = service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      events.add("disconnected " + name.getClassName());
    }

    @Override
    public void onNullBinding(ComponentName name) {
      events.add("null binding " + name.getClassName());
    }
  }
}
