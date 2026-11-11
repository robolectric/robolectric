package org.robolectric.shadows;

import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import javax.annotation.concurrent.GuardedBy;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.shadow.api.Shadow;

/**
 * Binds to the app's own services the way the system does, when {@code
 * robolectric.bindRealServices} is true: the service is created on the first bind, {@code onBind}
 * is called once per distinct intent, and the connection receives the binder it returned. When the
 * last connection unbinds, the service gets {@code onUnbind} and {@code onDestroy}.
 *
 * <p>Lifecycle callbacks run on the main looper, as they do on a device. Services that are only
 * started are not run, and services of other packages keep the stubbed behavior.
 */
final class RealBoundServices {
  static boolean isEnabled() {
    return Boolean.getBoolean("robolectric.bindRealServices");
  }

  @GuardedBy("this")
  private final Map<ComponentName, Record> records = new HashMap<>();

  @GuardedBy("this")
  private final Map<ServiceConnection, Record> recordsByConnection = new HashMap<>();

  /**
   * Binds if the intent resolves to a service of this app, and returns whether it did. Other
   * intents, such as those for system services, are left to the stubbed behavior. Without {@link
   * Context#BIND_AUTO_CREATE}, a service that isn't running is not created.
   */
  synchronized boolean tryBind(
      Intent intent, int flags, ServiceConnection connection, Executor callbacks) {
    Application application = RuntimeEnvironment.getApplication();
    ResolveInfo resolveInfo = application.getPackageManager().resolveService(intent, 0);
    if (resolveInfo == null
        || resolveInfo.serviceInfo == null
        || !application.getPackageName().equals(resolveInfo.serviceInfo.packageName)) {
      return false;
    }
    ServiceInfo serviceInfo = resolveInfo.serviceInfo;
    ComponentName component = new ComponentName(serviceInfo.packageName, serviceInfo.name);
    Record record = records.get(component);
    if (record == null) {
      if ((flags & Context.BIND_AUTO_CREATE) == 0) {
        return true;
      }
      record = new Record(component);
      records.put(component, record);
      mainHandler().post(record::create);
    }
    record.connections.add(connection);
    recordsByConnection.put(connection, record);
    Record boundRecord = record;
    Intent bindIntent = new Intent(intent);
    mainHandler()
        .post(() -> {
          IBinder binder = boundRecord.bind(bindIntent);
          callbacks.execute(() -> {
            if (!isBound(connection, boundRecord)) {
              // Unbound before the binder was delivered.
              return;
            }
            if (binder != null) {
              connection.onServiceConnected(component, binder);
            } else if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.P) {
              connection.onNullBinding(component);
            }
          });
        });
    return true;
  }

  /** Returns false if the connection isn't bound to a real service. */
  synchronized boolean unbind(ServiceConnection connection) {
    Record record = recordsByConnection.remove(connection);
    if (record == null) {
      return false;
    }
    record.connections.remove(connection);
    if (record.connections.isEmpty()) {
      records.remove(record.component);
      mainHandler().post(record::unbindAndDestroy);
    }
    return true;
  }

  private synchronized boolean isBound(ServiceConnection connection, Record record) {
    return recordsByConnection.get(connection) == record;
  }

  private static Handler mainHandler() {
    return new Handler(Looper.getMainLooper());
  }

  private static final class Record {
    final ComponentName component;
    final Set<ServiceConnection> connections = new HashSet<>();
    // Only touched on the main looper.
    final Map<Intent.FilterComparison, IBinder> binders = new LinkedHashMap<>();
    Service service;

    Record(ComponentName component) {
      this.component = component;
    }

    void create() {
      service = (Service) Shadow.newInstanceOf(component.getClassName());
      ServiceController.of(service, new Intent().setComponent(component));
      service.onCreate();
    }

    IBinder bind(Intent intent) {
      Intent.FilterComparison key = new Intent.FilterComparison(intent);
      if (!binders.containsKey(key)) {
        binders.put(key, service.onBind(intent));
      }
      return binders.get(key);
    }

    void unbindAndDestroy() {
      for (Intent.FilterComparison key : binders.keySet()) {
        service.onUnbind(key.getIntent());
      }
      service.onDestroy();
    }
  }
}
