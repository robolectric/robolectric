package org.robolectric.shadows;

import android.app.ActivityThread;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowApplication.Wrapper;

@Implements(ContextWrapper.class)
public class ShadowContextWrapper {

  @RealObject
  private ContextWrapper realContextWrapper;

  @Implementation
  public void sendBroadcast(Intent intent) {
    getShadowInstrumentation()
        .sendBroadcastWithPermission(intent, null, realContextWrapper.getBaseContext());
  }

  @Implementation
  public void sendBroadcast(Intent intent, String receiverPermission) {
    getShadowInstrumentation().sendBroadcastWithPermission(intent, receiverPermission,
        realContextWrapper.getBaseContext());
  }

  @Implementation
  public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
    getShadowInstrumentation().sendOrderedBroadcastWithPermission(intent, receiverPermission,
        realContextWrapper.getBaseContext());
  }

  @Implementation
  public void sendOrderedBroadcast(Intent intent, String receiverPermission, BroadcastReceiver resultReceiver,
                                   Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
    getShadowInstrumentation().sendOrderedBroadcast(intent, receiverPermission, resultReceiver,
        scheduler, initialCode, initialData, initialExtras, realContextWrapper.getBaseContext());
  }

  public List<Intent> getBroadcastIntents() {
    return getShadowInstrumentation().getBroadcastIntents();
  }

  ShadowInstrumentation getShadowInstrumentation() {
    ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    return Shadow.extract(activityThread.getInstrumentation());
  }

  /**
   * Consumes the most recent {@code Intent} started by {@link
   * ContextWrapper#startActivity(android.content.Intent)} and returns it.
   *
   * @return the most recently started {@code Intent}
   */
  public Intent getNextStartedActivity() {
    return getShadowInstrumentation().getNextStartedActivity();
  }

  /**
   * Returns the most recent {@code Intent} started by {@link
   * ContextWrapper#startActivity(android.content.Intent)} without consuming it.
   *
   * @return the most recently started {@code Intent}
   */
  public Intent peekNextStartedActivity() {
    return getShadowInstrumentation().peekNextStartedActivity();
  }

  @Implementation
  public ComponentName startService(Intent intent) {
    return getShadowInstrumentation().startService(intent);
  }

  @Implementation
  public boolean stopService(Intent name) {
    return getShadowInstrumentation().stopService(name);
  }

  public void setComponentNameAndServiceForBindService(ComponentName name, IBinder service) {
    getShadowInstrumentation().setComponentNameAndServiceForBindService(name, service);
  }

  public void setComponentNameAndServiceForBindServiceForIntent(Intent intent, ComponentName name, IBinder service) {
    getShadowInstrumentation().setComponentNameAndServiceForBindServiceForIntent(intent, name, service);
  }

  @Implementation
  public boolean bindService(final Intent intent, final ServiceConnection serviceConnection, int i) {
    return getShadowInstrumentation().bindService(intent, serviceConnection, i);
  }

  @Implementation
  public void unbindService(final ServiceConnection serviceConnection) {
    getShadowInstrumentation().unbindService(serviceConnection);
  }

  public List<ServiceConnection> getBoundServiceConnections() {
    return getShadowInstrumentation().getBoundServiceConnections();
  }

  public void setUnbindServiceShouldThrowIllegalArgument(boolean flag) {
    getShadowInstrumentation().setUnbindServiceShouldThrowIllegalArgument(flag);
  }

  public List<ServiceConnection> getUnboundServiceConnections() {
    return getShadowInstrumentation().getUnboundServiceConnections();
  }

  public void declareActionUnbindable(String action) {
    getShadowInstrumentation().declareActionUnbindable(action);
  }

  /**
   * Consumes the most recent {@code Intent} started by
   * {@link #startService(android.content.Intent)} and returns it.
   *
   * @return the most recently started {@code Intent}
   */
  public Intent getNextStartedService() {
    return getShadowInstrumentation().getNextStartedService();
  }

  /**
   * Returns the most recent {@code Intent} started by {@link #startService(android.content.Intent)}
   * without consuming it.
   *
   * @return the most recently started {@code Intent}
   */
  public Intent peekNextStartedService() {
    return getShadowInstrumentation().peekNextStartedService();
  }

  /**
   * Clears all {@code Intent} started by {@link #startService(android.content.Intent)}.
   */
  public void clearStartedServices() {
    getShadowInstrumentation().clearStartedServices();
  }

  /**
   * Consumes the {@code Intent} requested to stop a service by {@link #stopService(android.content.Intent)}
   * from the bottom of the stack of stop requests.
   */
  public Intent getNextStoppedService() {
    return getShadowInstrumentation().getNextStoppedService();
  }

  @Implementation
  public void sendStickyBroadcast(Intent intent) {
    getShadowInstrumentation().sendStickyBroadcast(intent, realContextWrapper.getBaseContext());
  }

  /**
   * Always returns {@code null}
   *
   * @return {@code null}
   */
  @Implementation
  public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
    return getShadowInstrumentation()
        .registerReceiver(receiver, filter, realContextWrapper.getBaseContext());
  }

  @Implementation
  public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
    return getShadowInstrumentation().registerReceiver(receiver, filter, broadcastPermission, scheduler,
        realContextWrapper.getBaseContext());
  }

  @Implementation
  public void unregisterReceiver(BroadcastReceiver broadcastReceiver) {
    getShadowInstrumentation().unregisterReceiver(broadcastReceiver);
  }

  /** @deprecated use PackageManager.queryBroadcastReceivers instead */
  @Deprecated
  public boolean hasReceiverForIntent(Intent intent) {
    return getShadowInstrumentation().hasReceiverForIntent(intent);
  }

  /** @deprecated use PackageManager.queryBroadcastReceivers instead */
  @Deprecated
  public List<BroadcastReceiver> getReceiversForIntent(Intent intent) {
    return getShadowInstrumentation().getReceiversForIntent(intent);
  }

  /**
   * @return list of {@link Wrapper}s for registered receivers
   */
  public List<Wrapper> getRegisteredReceivers() {
    return getShadowInstrumentation().getRegisteredReceivers();
  }

  @Implementation
  public int checkPermission(String permission, int pid, int uid) {
    return getShadowInstrumentation().checkPermission(permission, pid, uid);
  }

  public void grantPermissions(String... permissionNames) {
    getShadowInstrumentation().grantPermissions(permissionNames);
  }

  public void denyPermissions(String... permissionNames) {
    getShadowInstrumentation().denyPermissions(permissionNames);
  }

}
