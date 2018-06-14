package org.robolectric.shadows;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.google.common.util.concurrent.Futures.immediateFuture;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

import android.app.ActivityThread;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
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
  private List<Intent.FilterComparison> startedServices = new ArrayList<>();
  private List<Intent.FilterComparison> stoppedServices = new ArrayList<>();
  private List<Intent> broadcastIntents = new ArrayList<>();
  private List<ServiceConnection> boundServiceConnections = new ArrayList<>();
  private List<ServiceConnection> unboundServiceConnections = new ArrayList<>();
  private List<Wrapper> registeredReceivers = new ArrayList<>();
  private Set<String> grantedPermissions = new HashSet<>();
  private boolean unbindServiceShouldThrowIllegalArgument = false;
  private Map<Intent.FilterComparison, ServiceConnectionDataWrapper> serviceConnectionDataForIntent = new HashMap<>();
  //default values for bindService
  private ServiceConnectionDataWrapper defaultServiceConnectionData = new ServiceConnectionDataWrapper(null, null);
  private List<String> unbindableActions = new ArrayList<>();
  private Map<String, Intent> stickyIntents = new LinkedHashMap<>();
  private Handler mainHandler;
  private Map<ServiceConnection, ServiceConnectionDataWrapper> serviceConnectionDataForServiceConnection = new HashMap<>();

  @Implementation
  public void sendBroadcast(Intent intent) {
    sendBroadcastWithPermission(intent, null);
  }

  @Implementation
  public void sendBroadcast(Intent intent, String receiverPermission) {
    sendBroadcastWithPermission(intent, receiverPermission);
  }

  @Implementation
  public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
    sendOrderedBroadcastWithPermission(intent, receiverPermission);
  }

  @Implementation
  public void sendOrderedBroadcast(Intent intent, String receiverPermission, BroadcastReceiver resultReceiver,
                                   Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
    List<Wrapper> receivers = getAppropriateWrappers(intent, receiverPermission);
    sortByPriority(receivers);
    receivers.add(new Wrapper(resultReceiver, null, this.realContextWrapper, null, scheduler));
    postOrderedToWrappers(receivers, intent, initialCode, initialData, initialExtras);
  }

  public void assertNoBroadcastListenersOfActionRegistered(ContextWrapper context, String action) {
    for (Wrapper registeredReceiver : registeredReceivers) {
      if (registeredReceiver.context == context.getBaseContext()) {
        Iterator<String> actions = registeredReceiver.intentFilter.actionsIterator();
        while (actions.hasNext()) {
          if (actions.next().equals(action)) {
            RuntimeException e = new IllegalStateException("Unexpected BroadcastReceiver on " + context +
                " with action " + action + " "
                + registeredReceiver.broadcastReceiver + " that was originally registered here:");
            e.setStackTrace(registeredReceiver.exception.getStackTrace());
            throw e;
          }
        }
      }
    }
  }

  /**
   * Returns the BroadcaseReceivers wrappers, matching intent's action and permissions.
   */
  private List<Wrapper> getAppropriateWrappers(Intent intent, String receiverPermission) {
    broadcastIntents.add(intent);

    List<Wrapper> result = new ArrayList<>();

    List<Wrapper> copy = new ArrayList<>();
    copy.addAll(registeredReceivers);
    for (Wrapper wrapper : copy) {
      if (hasMatchingPermission(wrapper.broadcastPermission, receiverPermission)
          && wrapper.intentFilter.matchAction(intent.getAction())) {
        final int match = wrapper.intentFilter.matchData(intent.getType(), intent.getScheme(), intent.getData());
        if (match != IntentFilter.NO_MATCH_DATA && match != IntentFilter.NO_MATCH_TYPE) {
          result.add(wrapper);
        }
      }
    }
    return result;
  }

  private void postIntent(Intent intent, Wrapper wrapper, final AtomicBoolean abort) {
    final Handler scheduler = (wrapper.scheduler != null) ? wrapper.scheduler : getMainHandler();
    final BroadcastReceiver receiver = wrapper.broadcastReceiver;
    final ShadowBroadcastReceiver shReceiver = Shadow.extract(receiver);
    final Intent broadcastIntent = intent;
    scheduler.post(new Runnable() {
      @Override
      public void run() {
        receiver.setPendingResult(ShadowBroadcastPendingResult.create(0, null, null, false));
        shReceiver.onReceive(realContextWrapper, broadcastIntent, abort);
      }
    });
  }

  private void postToWrappers(List<Wrapper> wrappers, Intent intent) {
    AtomicBoolean abort = new AtomicBoolean(false); // abort state is shared among all broadcast receivers
    for (Wrapper wrapper: wrappers) {
      postIntent(intent, wrapper, abort);
    }
  }

  private void postOrderedToWrappers(List<Wrapper> wrappers, final Intent intent, int initialCode, String data, Bundle extras) {
    final AtomicBoolean abort = new AtomicBoolean(false); // abort state is shared among all broadcast receivers
    ListenableFuture<BroadcastResultHolder> future = immediateFuture(new BroadcastResultHolder(initialCode, data, extras));
    for (final Wrapper wrapper : wrappers) {
      future = postIntent(wrapper, intent, future, abort);
    }
    final ListenableFuture<?> finalFuture = future;
    future.addListener(new Runnable() {
      @Override
      public void run() {
        getMainHandler().post(new Runnable() {
          @Override
          public void run() {
            try {
              finalFuture.get();
            } catch (InterruptedException | ExecutionException e) {
              throw new RuntimeException(e);
            }
          }
        });
      }
    }, directExecutor());
  }

  /** Enforces that BroadcastReceivers invoked during an ordered broadcast run serially, passing along their results.*/
  private ListenableFuture<BroadcastResultHolder> postIntent(final Wrapper wrapper,
                                                             final Intent intent,
                                                             ListenableFuture<BroadcastResultHolder> oldResult,
                                                             final AtomicBoolean abort) {
    final Handler scheduler = (wrapper.scheduler != null) ? wrapper.scheduler : getMainHandler();
    return Futures
        .transformAsync(oldResult, new AsyncFunction<BroadcastResultHolder, BroadcastResultHolder>() {
      @Override
      public ListenableFuture<BroadcastResultHolder> apply(BroadcastResultHolder broadcastResultHolder) throws Exception {
        final BroadcastReceiver.PendingResult result = ShadowBroadcastPendingResult.create(
                broadcastResultHolder.resultCode,
                broadcastResultHolder.resultData,
                broadcastResultHolder.resultExtras,
                true /*ordered */);
        wrapper.broadcastReceiver.setPendingResult(result);
        scheduler.post(() -> {
          ShadowBroadcastReceiver shadowBroadcastReceiver =
              Shadow.extract(wrapper.broadcastReceiver);
          shadowBroadcastReceiver.onReceive(realContextWrapper, intent, abort);
        });
        return BroadcastResultHolder.transform(result);
      }

    }, directExecutor());
  }

  /**
   * Broadcasts the {@code Intent} by iterating through the registered receivers, invoking their filters including
   * permissions, and calling {@code onReceive(Application, Intent)} as appropriate. Does not enqueue the
   * {@code Intent} for later inspection.
   *
   * @param intent the {@code Intent} to broadcast
   *               todo: enqueue the Intent for later inspection
   */
  private void sendBroadcastWithPermission(Intent intent, String receiverPermission) {
    List<Wrapper> wrappers = getAppropriateWrappers(intent, receiverPermission);
    postToWrappers(wrappers, intent);
  }

  private void sendOrderedBroadcastWithPermission(Intent intent, String receiverPermission) {
    List<Wrapper> wrappers = getAppropriateWrappers(intent, receiverPermission);
    // sort by the decrease of priorities
    sortByPriority(wrappers);

    postOrderedToWrappers(wrappers, intent, 0, null, null);
  }

  private void sortByPriority(List<Wrapper> wrappers) {
    Collections.sort(wrappers, new Comparator<Wrapper>() {
      @Override
      public int compare(Wrapper o1, Wrapper o2) {
        return Integer.compare(o2.getIntentFilter().getPriority(), o1.getIntentFilter().getPriority());
      }
    });
  }

  public List<Intent> getBroadcastIntents() {
    return broadcastIntents;
  }

  /**
   * Consumes the most recent {@code Intent} started by {@link
   * ContextWrapper#startActivity(android.content.Intent)} and returns it.
   *
   * @return the most recently started {@code Intent}
   */
  public Intent getNextStartedActivity() {
    ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    ShadowInstrumentation shadowInstrumentation = Shadow.extract(activityThread.getInstrumentation());
    return shadowInstrumentation.getNextStartedActivity();
  }

  /**
   * Returns the most recent {@code Intent} started by {@link
   * ContextWrapper#startActivity(android.content.Intent)} without consuming it.
   *
   * @return the most recently started {@code Intent}
   */
  public Intent peekNextStartedActivity() {
    ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    ShadowInstrumentation shadowInstrumentation = Shadow.extract(activityThread.getInstrumentation());
    return shadowInstrumentation.peekNextStartedActivity();
  }

  @Implementation
  public ComponentName startService(Intent intent) {
    startedServices.add(new Intent.FilterComparison(intent));
    if (intent.getComponent() != null) {
      return intent.getComponent();
    }
    return new ComponentName("some.service.package", "SomeServiceName-FIXME");
  }

  @Implementation
  public boolean stopService(Intent name) {
    stoppedServices.add(new Intent.FilterComparison(name));
    return startedServices.contains(new Intent.FilterComparison(name));
  }

  public void setComponentNameAndServiceForBindService(ComponentName name, IBinder service) {
    defaultServiceConnectionData = new ServiceConnectionDataWrapper(name, service);
  }

  public void setComponentNameAndServiceForBindServiceForIntent(Intent intent, ComponentName name, IBinder service) {
    serviceConnectionDataForIntent.put(new Intent.FilterComparison(intent),
        new ServiceConnectionDataWrapper(name, service));
  }

  @Implementation
  public boolean bindService(final Intent intent, final ServiceConnection serviceConnection, int i) {
    boundServiceConnections.add(serviceConnection);
    unboundServiceConnections.remove(serviceConnection);
    if (unbindableActions.contains(intent.getAction())) {
      return false;
    }
    startedServices.add(new Intent.FilterComparison(intent));
    ShadowLooper shadowLooper = Shadow.extract(Looper.getMainLooper());
    shadowLooper.post(() -> {
      final ServiceConnectionDataWrapper serviceConnectionDataWrapper;
      final Intent.FilterComparison filterComparison = new Intent.FilterComparison(intent);
      if (serviceConnectionDataForIntent.containsKey(filterComparison)) {
        serviceConnectionDataWrapper = serviceConnectionDataForIntent.get(filterComparison);
      } else {
        serviceConnectionDataWrapper = defaultServiceConnectionData;
      }
      serviceConnectionDataForServiceConnection.put(serviceConnection, serviceConnectionDataWrapper);
      serviceConnection.onServiceConnected(serviceConnectionDataWrapper.componentNameForBindService, serviceConnectionDataWrapper.binderForBindService);
    }, 0);
    return true;
  }

  @Implementation
  public void unbindService(final ServiceConnection serviceConnection) {
    if (unbindServiceShouldThrowIllegalArgument) {
      throw new IllegalArgumentException();
    }

    unboundServiceConnections.add(serviceConnection);
    boundServiceConnections.remove(serviceConnection);
    ShadowLooper shadowLooper = Shadow.extract(Looper.getMainLooper());
    shadowLooper.post(() -> {
      final ServiceConnectionDataWrapper serviceConnectionDataWrapper;
      if (serviceConnectionDataForServiceConnection.containsKey(serviceConnection)) {
        serviceConnectionDataWrapper = serviceConnectionDataForServiceConnection.get(serviceConnection);
      } else {
        serviceConnectionDataWrapper = defaultServiceConnectionData;
      }
      serviceConnection.onServiceDisconnected(serviceConnectionDataWrapper.componentNameForBindService);
    }, 0);
  }

  public List<ServiceConnection> getBoundServiceConnections() {
    return boundServiceConnections;
  }

  public void setUnbindServiceShouldThrowIllegalArgument(boolean flag) {
    unbindServiceShouldThrowIllegalArgument = flag;
  }

  public List<ServiceConnection> getUnboundServiceConnections() {
    return unboundServiceConnections;
  }

  public void declareActionUnbindable(String action) {
    unbindableActions.add(action);
  }

  /**
   * Consumes the most recent {@code Intent} started by
   * {@link #startService(android.content.Intent)} and returns it.
   *
   * @return the most recently started {@code Intent}
   */
  public Intent getNextStartedService() {
    if (startedServices.isEmpty()) {
      return null;
    } else {
      return startedServices.remove(0).getIntent();
    }
  }

  /**
   * Returns the most recent {@code Intent} started by {@link #startService(android.content.Intent)}
   * without consuming it.
   *
   * @return the most recently started {@code Intent}
   */
  public Intent peekNextStartedService() {
    if (startedServices.isEmpty()) {
      return null;
    } else {
      return startedServices.get(0).getIntent();
    }
  }

  /**
   * Clears all {@code Intent} started by {@link #startService(android.content.Intent)}.
   */
  public void clearStartedServices() {
    startedServices.clear();
  }

  /**
   * Consumes the {@code Intent} requested to stop a service by {@link #stopService(android.content.Intent)}
   * from the bottom of the stack of stop requests.
   */
  public Intent getNextStoppedService() {
    if (stoppedServices.isEmpty()) {
      return null;
    } else {
      return stoppedServices.remove(0).getIntent();
    }
  }

  @Implementation
  public void sendStickyBroadcast(Intent intent) {
    stickyIntents.put(intent.getAction(), intent);
    sendBroadcast(intent);
  }

  /**
   * Always returns {@code null}
   *
   * @return {@code null}
   */
  @Implementation
  public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
    return registerReceiverWithContext(receiver, filter, null, null, realContextWrapper);
  }

  @Implementation
  public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
    return registerReceiverWithContext(receiver, filter, broadcastPermission, scheduler,
        realContextWrapper);
  }

  Intent registerReceiverWithContext(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler, Context context) {
    if (receiver != null) {
      registeredReceivers.add(new Wrapper(receiver, filter, context, broadcastPermission, scheduler));
    }
    return processStickyIntents(filter, receiver, context);
  }

  private Intent processStickyIntents(IntentFilter filter, BroadcastReceiver receiver, Context context) {
    Intent result = null;
    for (Intent stickyIntent : stickyIntents.values()) {
      if (filter.matchAction(stickyIntent.getAction())) {
        if (result == null) {
          result = stickyIntent;
        }
        if (receiver != null) {
          receiver.setPendingResult(ShadowBroadcastPendingResult.createSticky(stickyIntent));
          receiver.onReceive(context, stickyIntent);
          receiver.setPendingResult(null);
        } else if (result != null) {
          break;
        }
      }
    }
    return result;
  }

  @Implementation
  public void unregisterReceiver(BroadcastReceiver broadcastReceiver) {
    boolean found = false;
    Iterator<Wrapper> iterator = registeredReceivers.iterator();
    while (iterator.hasNext()) {
      Wrapper wrapper = iterator.next();
      if (wrapper.broadcastReceiver == broadcastReceiver) {
        iterator.remove();
        found = true;
      }
    }
    if (!found) {
      throw new IllegalArgumentException("Receiver not registered: " + broadcastReceiver);
    }
  }

  /** @deprecated use PackageManager.queryBroadcastReceivers instead */
  @Deprecated
  public boolean hasReceiverForIntent(Intent intent) {
    for (Wrapper wrapper : registeredReceivers) {
      if (wrapper.intentFilter.matchAction(intent.getAction())) {
        return true;
      }
    }
    return false;
  }

  /** @deprecated use PackageManager.queryBroadcastReceivers instead */
  @Deprecated
  public List<BroadcastReceiver> getReceiversForIntent(Intent intent) {
    ArrayList<BroadcastReceiver> broadcastReceivers = new ArrayList<>();
    for (Wrapper wrapper : registeredReceivers) {
      if (wrapper.intentFilter.matchAction(intent.getAction())) {
        broadcastReceivers.add(wrapper.getBroadcastReceiver());
      }
    }
    return broadcastReceivers;
  }

  /**
   * @return list of {@link Wrapper}s for registered receivers
   */
  public List<Wrapper> getRegisteredReceivers() {
    return registeredReceivers;
  }

  @Implementation
  public int checkPermission(String permission, int pid, int uid) {
    return grantedPermissions.contains(permission) ? PERMISSION_GRANTED : PERMISSION_DENIED;
  }

  public void grantPermissions(String... permissionNames) {
    Collections.addAll(grantedPermissions, permissionNames);
  }

  public void denyPermissions(String... permissionNames) {
    for (String permissionName : permissionNames) {
      grantedPermissions.remove(permissionName);
    }
  }

  private boolean hasMatchingPermission(String permission1, String permission2) {
    return permission1 == null ? permission2 == null : permission1.equals(permission2);
  }

  private Handler getMainHandler() {
    if (mainHandler == null) {
      mainHandler = new Handler(realContextWrapper.getMainLooper());
    }
    return mainHandler;
  }

  private static final class BroadcastResultHolder {
    private final int resultCode;
    private final String resultData;
    private final Bundle resultExtras;

    private BroadcastResultHolder(int resultCode, String resultData, Bundle resultExtras) {
      this.resultCode = resultCode;
      this.resultData = resultData;
      this.resultExtras = resultExtras;
    }

    private static ListenableFuture<BroadcastResultHolder> transform(BroadcastReceiver.PendingResult result) {
      ShadowBroadcastPendingResult shadowBroadcastPendingResult = Shadow.extract(result);
      return Futures.transform(shadowBroadcastPendingResult.getFuture(),
          pendingResult -> new BroadcastResultHolder(pendingResult.getResultCode(),
              pendingResult.getResultData(),
              pendingResult.getResultExtras(false)), directExecutor());
    }
  }

  private static class ServiceConnectionDataWrapper {
    public final ComponentName componentNameForBindService;
    public final IBinder binderForBindService;

    private ServiceConnectionDataWrapper(ComponentName componentNameForBindService, IBinder binderForBindService) {
      this.componentNameForBindService = componentNameForBindService;
      this.binderForBindService = binderForBindService;
    }
  }
}
