package org.robolectric.shadows;

import static android.content.pm.PackageManager.MATCH_DEFAULT_ONLY;
import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.P;
import static com.google.common.util.concurrent.Futures.immediateFuture;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.Fragment;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityResult;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.Intent.FilterComparison;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.os.UserHandle;
import android.util.Pair;
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
import org.robolectric.shadows.ShadowActivity.IntentForResult;
import org.robolectric.shadows.ShadowApplication.Wrapper;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.WithType;

@Implements(value = Instrumentation.class, looseSignatures = true)
public class ShadowInstrumentation {

  @RealObject private Instrumentation realObject;

  private List<Intent> startedActivities = new ArrayList<>();
  private List<IntentForResult> startedActivitiesForResults = new ArrayList<>();
  private Map<FilterComparison, Integer> intentRequestCodeMap = new HashMap<>();
  private List<Intent.FilterComparison> startedServices = new ArrayList<>();
  private List<Intent.FilterComparison> stoppedServices = new ArrayList<>();
  private List<Intent> broadcastIntents = new ArrayList<>();
  private List<ServiceConnection> boundServiceConnections = new ArrayList<>();
  private List<ServiceConnection> unboundServiceConnections = new ArrayList<>();
  private List<Wrapper> registeredReceivers = new ArrayList<>();
  // map of pid+uid to granted permissions
  private final Map<Pair<Integer, Integer>, Set<String>> grantedPermissionsMap = new HashMap<>();
  private boolean unbindServiceShouldThrowIllegalArgument = false;
  private Map<Intent.FilterComparison, ServiceConnectionDataWrapper>
      serviceConnectionDataForIntent = new HashMap<>();
  // default values for bindService
  private ServiceConnectionDataWrapper defaultServiceConnectionData =
      new ServiceConnectionDataWrapper(null, null);
  private List<String> unbindableActions = new ArrayList<>();
  private Map<String, Intent> stickyIntents = new LinkedHashMap<>();
  private Handler mainHandler;
  private Map<ServiceConnection, ServiceConnectionDataWrapper>
      serviceConnectionDataForServiceConnection = new HashMap<>();

  private boolean checkActivities;

  @Implementation(minSdk = P)
  protected Activity startActivitySync(Intent intent, Bundle options) {
    throw new UnsupportedOperationException("Implement me!!");
  }

  @Implementation
  protected ActivityResult execStartActivity(
      Context who,
      IBinder contextThread,
      IBinder token,
      Activity target,
      Intent intent,
      int requestCode,
      Bundle options) {

    verifyActivityInManifest(intent);
    logStartedActivity(intent, requestCode, options);

    if (who == null) {
      return null;
    }
    return directlyOn(realObject, Instrumentation.class)
        .execStartActivity(who, contextThread, token, target, intent, requestCode, options);
  }

  @Implementation(maxSdk = LOLLIPOP_MR1)
  protected ActivityResult execStartActivity(
      Context who,
      IBinder contextThread,
      IBinder token,
      Fragment target,
      Intent intent,
      int requestCode,
      Bundle options) {
    verifyActivityInManifest(intent);
    logStartedActivity(intent, requestCode, options);
    return null;
  }

  private void logStartedActivity(Intent intent, int requestCode, Bundle options) {
    startedActivities.add(intent);
    intentRequestCodeMap.put(new FilterComparison(intent), requestCode);
    startedActivitiesForResults.add(new IntentForResult(intent, requestCode, options));
  }

  private void verifyActivityInManifest(Intent intent) {
    if (checkActivities
        && RuntimeEnvironment.application
                .getPackageManager()
                .resolveActivity(intent, MATCH_DEFAULT_ONLY)
            == null) {
      throw new ActivityNotFoundException(intent.getAction());
    }
  }

  @Implementation
  protected void execStartActivities(
      Context who,
      IBinder contextThread,
      IBinder token,
      Activity target,
      Intent[] intents,
      Bundle options) {
    for (Intent intent : intents) {
      execStartActivity(who, contextThread, token, target, intent, -1, options);
    }
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void execStartActivityFromAppTask(
      Context who, IBinder contextThread, Object appTask, Intent intent, Bundle options) {
    throw new UnsupportedOperationException("Implement me!!");
  }

  @Implementation(minSdk = M)
  protected ActivityResult execStartActivity(
      Context who,
      IBinder contextThread,
      IBinder token,
      String target,
      Intent intent,
      int requestCode,
      Bundle options) {
    verifyActivityInManifest(intent);
    logStartedActivity(intent, requestCode, options);

    return directlyOn(realObject, Instrumentation.class)
        .execStartActivity(who, contextThread, token, target, intent, requestCode, options);
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected ActivityResult execStartActivity(
      Context who,
      IBinder contextThread,
      IBinder token,
      String resultWho,
      Intent intent,
      int requestCode,
      Bundle options,
      UserHandle user) {
    throw new UnsupportedOperationException("Implement me!!");
  }

  @Implementation(minSdk = M)
  protected ActivityResult execStartActivityAsCaller(
      Context who,
      IBinder contextThread,
      IBinder token,
      Activity target,
      Intent intent,
      int requestCode,
      Bundle options,
      boolean ignoreTargetSecurity,
      int userId) {
    throw new UnsupportedOperationException("Implement me!!");
  }

  void sendOrderedBroadcast(
      Intent intent,
      String receiverPermission,
      BroadcastReceiver resultReceiver,
      Handler scheduler,
      int initialCode,
      String initialData,
      Bundle initialExtras,
      Context context) {
    List<Wrapper> receivers = getAppropriateWrappers(intent, receiverPermission);
    sortByPriority(receivers);
    receivers.add(new Wrapper(resultReceiver, null, context, null, scheduler));
    postOrderedToWrappers(receivers, intent, initialCode, initialData, initialExtras, context);
  }

  void assertNoBroadcastListenersOfActionRegistered(ContextWrapper context, String action) {
    for (Wrapper registeredReceiver : registeredReceivers) {
      if (registeredReceiver.context == context.getBaseContext()) {
        Iterator<String> actions = registeredReceiver.intentFilter.actionsIterator();
        while (actions.hasNext()) {
          if (actions.next().equals(action)) {
            RuntimeException e =
                new IllegalStateException(
                    "Unexpected BroadcastReceiver on "
                        + context
                        + " with action "
                        + action
                        + " "
                        + registeredReceiver.broadcastReceiver
                        + " that was originally registered here:");
            e.setStackTrace(registeredReceiver.exception.getStackTrace());
            throw e;
          }
        }
      }
    }
  }

  /** Returns the BroadcaseReceivers wrappers, matching intent's action and permissions. */
  private List<Wrapper> getAppropriateWrappers(Intent intent, String receiverPermission) {
    broadcastIntents.add(intent);

    List<Wrapper> result = new ArrayList<>();

    List<Wrapper> copy = new ArrayList<>();
    copy.addAll(registeredReceivers);
    for (Wrapper wrapper : copy) {
      if (hasMatchingPermission(wrapper.broadcastPermission, receiverPermission)
          && wrapper.intentFilter.matchAction(intent.getAction())) {
        final int match =
            wrapper.intentFilter.matchData(intent.getType(), intent.getScheme(), intent.getData());
        if (match != IntentFilter.NO_MATCH_DATA && match != IntentFilter.NO_MATCH_TYPE) {
          result.add(wrapper);
        }
      }
    }
    return result;
  }

  private void postIntent(
      Intent intent, Wrapper wrapper, final AtomicBoolean abort, Context context) {
    final Handler scheduler =
        (wrapper.scheduler != null) ? wrapper.scheduler : getMainHandler(context);
    final BroadcastReceiver receiver = wrapper.broadcastReceiver;
    final ShadowBroadcastReceiver shReceiver = Shadow.extract(receiver);
    final Intent broadcastIntent = intent;
    scheduler.post(
        new Runnable() {
          @Override
          public void run() {
            receiver.setPendingResult(ShadowBroadcastPendingResult.create(0, null, null, false));
            shReceiver.onReceive(context, broadcastIntent, abort);
          }
        });
  }

  private void postToWrappers(List<Wrapper> wrappers, Intent intent, Context context) {
    AtomicBoolean abort =
        new AtomicBoolean(false); // abort state is shared among all broadcast receivers
    for (Wrapper wrapper : wrappers) {
      postIntent(intent, wrapper, abort, context);
    }
  }

  private void postOrderedToWrappers(
      List<Wrapper> wrappers,
      final Intent intent,
      int initialCode,
      String data,
      Bundle extras,
      final Context context) {
    final AtomicBoolean abort =
        new AtomicBoolean(false); // abort state is shared among all broadcast receivers
    ListenableFuture<BroadcastResultHolder> future =
        immediateFuture(new BroadcastResultHolder(initialCode, data, extras));
    for (final Wrapper wrapper : wrappers) {
      future = postIntent(wrapper, intent, future, abort, context);
    }
    final ListenableFuture<?> finalFuture = future;
    future.addListener(
        new Runnable() {
          @Override
          public void run() {
            getMainHandler(context)
                .post(
                    new Runnable() {
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
        },
        directExecutor());
  }

  /**
   * Enforces that BroadcastReceivers invoked during an ordered broadcast run serially, passing
   * along their results.
   */
  private ListenableFuture<BroadcastResultHolder> postIntent(
      final Wrapper wrapper,
      final Intent intent,
      ListenableFuture<BroadcastResultHolder> oldResult,
      final AtomicBoolean abort,
      final Context context) {
    final Handler scheduler =
        (wrapper.scheduler != null) ? wrapper.scheduler : getMainHandler(context);
    return Futures.transformAsync(
        oldResult,
        new AsyncFunction<BroadcastResultHolder, BroadcastResultHolder>() {
          @Override
          public ListenableFuture<BroadcastResultHolder> apply(
              BroadcastResultHolder broadcastResultHolder) throws Exception {
            final BroadcastReceiver.PendingResult result =
                ShadowBroadcastPendingResult.create(
                    broadcastResultHolder.resultCode,
                    broadcastResultHolder.resultData,
                    broadcastResultHolder.resultExtras,
                    true /*ordered */);
            wrapper.broadcastReceiver.setPendingResult(result);
            scheduler.post(
                () -> {
                  ShadowBroadcastReceiver shadowBroadcastReceiver =
                      Shadow.extract(wrapper.broadcastReceiver);
                  shadowBroadcastReceiver.onReceive(context, intent, abort);
                });
            return BroadcastResultHolder.transform(result);
          }
        },
        directExecutor());
  }

  /**
   * Broadcasts the {@code Intent} by iterating through the registered receivers, invoking their
   * filters including permissions, and calling {@code onReceive(Application, Intent)} as
   * appropriate. Does not enqueue the {@code Intent} for later inspection.
   *
   * @param context
   * @param intent the {@code Intent} to broadcast todo: enqueue the Intent for later inspection
   */
  void sendBroadcastWithPermission(Intent intent, String receiverPermission, Context context) {
    List<Wrapper> wrappers = getAppropriateWrappers(intent, receiverPermission);
    postToWrappers(wrappers, intent, context);
  }

  void sendOrderedBroadcastWithPermission(
      Intent intent, String receiverPermission, Context context) {
    List<Wrapper> wrappers = getAppropriateWrappers(intent, receiverPermission);
    // sort by the decrease of priorities
    sortByPriority(wrappers);

    postOrderedToWrappers(wrappers, intent, 0, null, null, context);
  }

  private void sortByPriority(List<Wrapper> wrappers) {
    Collections.sort(
        wrappers,
        new Comparator<Wrapper>() {
          @Override
          public int compare(Wrapper o1, Wrapper o2) {
            return Integer.compare(
                o2.getIntentFilter().getPriority(), o1.getIntentFilter().getPriority());
          }
        });
  }

  List<Intent> getBroadcastIntents() {
    return broadcastIntents;
  }

  Intent getNextStartedActivity() {
    if (startedActivities.isEmpty()) {
      return null;
    } else {
      return startedActivities.remove(startedActivities.size() - 1);
    }
  }

  Intent peekNextStartedActivity() {
    if (startedActivities.isEmpty()) {
      return null;
    } else {
      return startedActivities.get(startedActivities.size() - 1);
    }
  }

  /**
   * Clears all {@code Intent}s started by {@link #execStartActivity(Context, IBinder, IBinder,
   * Activity, Intent, int, Bundle)}, {@link #execStartActivity(Context, IBinder, IBinder, Fragment,
   * Intent, int, Bundle)}, and {@link #execStartActivity(Context, IBinder, IBinder, String, Intent,
   * int, Bundle)}.
   */
  void clearNextStartedActivities() {
    startedActivities.clear();
  }

  IntentForResult getNextStartedActivityForResult() {
    if (startedActivitiesForResults.isEmpty()) {
      return null;
    } else {
      return startedActivitiesForResults.remove(startedActivitiesForResults.size() - 1);
    }
  }

  IntentForResult peekNextStartedActivityForResult() {
    if (startedActivitiesForResults.isEmpty()) {
      return null;
    } else {
      return startedActivitiesForResults.get(startedActivitiesForResults.size() - 1);
    }
  }

  void checkActivities(boolean checkActivities) {
    this.checkActivities = checkActivities;
  }

  int getRequestCodeForIntent(Intent requestIntent) {
    Integer requestCode = intentRequestCodeMap.get(new Intent.FilterComparison(requestIntent));
    if (requestCode == null) {
      throw new RuntimeException(
          "No intent matches " + requestIntent + " among " + intentRequestCodeMap.keySet());
    }
    return requestCode;
  }

  protected ComponentName startService(Intent intent) {
    startedServices.add(new Intent.FilterComparison(intent));
    if (intent.getComponent() != null) {
      return intent.getComponent();
    }
    return new ComponentName("some.service.package", "SomeServiceName-FIXME");
  }

  boolean stopService(Intent name) {
    stoppedServices.add(new Intent.FilterComparison(name));
    return startedServices.contains(new Intent.FilterComparison(name));
  }

  void setComponentNameAndServiceForBindService(ComponentName name, IBinder service) {
    defaultServiceConnectionData = new ServiceConnectionDataWrapper(name, service);
  }

  void setComponentNameAndServiceForBindServiceForIntent(
      Intent intent, ComponentName name, IBinder service) {
    serviceConnectionDataForIntent.put(
        new Intent.FilterComparison(intent), new ServiceConnectionDataWrapper(name, service));
  }

  protected boolean bindService(
      final Intent intent, final ServiceConnection serviceConnection, int i) {
    boundServiceConnections.add(serviceConnection);
    unboundServiceConnections.remove(serviceConnection);
    if (unbindableActions.contains(intent.getAction())) {
      return false;
    }
    startedServices.add(new Intent.FilterComparison(intent));
    ShadowLooper shadowLooper = Shadow.extract(Looper.getMainLooper());
    shadowLooper.post(
        () -> {
          final ServiceConnectionDataWrapper serviceConnectionDataWrapper;
          final Intent.FilterComparison filterComparison = new Intent.FilterComparison(intent);
          if (serviceConnectionDataForIntent.containsKey(filterComparison)) {
            serviceConnectionDataWrapper = serviceConnectionDataForIntent.get(filterComparison);
          } else {
            serviceConnectionDataWrapper = defaultServiceConnectionData;
          }
          serviceConnectionDataForServiceConnection.put(
              serviceConnection, serviceConnectionDataWrapper);
          serviceConnection.onServiceConnected(
              serviceConnectionDataWrapper.componentNameForBindService,
              serviceConnectionDataWrapper.binderForBindService);
        },
        0);
    return true;
  }

  protected void unbindService(final ServiceConnection serviceConnection) {
    if (unbindServiceShouldThrowIllegalArgument) {
      throw new IllegalArgumentException();
    }

    unboundServiceConnections.add(serviceConnection);
    boundServiceConnections.remove(serviceConnection);
    ShadowLooper shadowLooper = Shadow.extract(Looper.getMainLooper());
    shadowLooper.post(
        () -> {
          final ServiceConnectionDataWrapper serviceConnectionDataWrapper;
          if (serviceConnectionDataForServiceConnection.containsKey(serviceConnection)) {
            serviceConnectionDataWrapper =
                serviceConnectionDataForServiceConnection.get(serviceConnection);
          } else {
            serviceConnectionDataWrapper = defaultServiceConnectionData;
          }
          serviceConnection.onServiceDisconnected(
              serviceConnectionDataWrapper.componentNameForBindService);
        },
        0);
  }

  protected List<ServiceConnection> getBoundServiceConnections() {
    return boundServiceConnections;
  }

  void setUnbindServiceShouldThrowIllegalArgument(boolean flag) {
    unbindServiceShouldThrowIllegalArgument = flag;
  }

  protected List<ServiceConnection> getUnboundServiceConnections() {
    return unboundServiceConnections;
  }

  void declareActionUnbindable(String action) {
    unbindableActions.add(action);
  }

  public List<String> getUnbindableActions() {
    return unbindableActions;
  }

  /**
   * Consumes the most recent {@code Intent} started by {@link
   * #startService(android.content.Intent)} and returns it.
   *
   * @return the most recently started {@code Intent}
   */
  Intent getNextStartedService() {
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
  Intent peekNextStartedService() {
    if (startedServices.isEmpty()) {
      return null;
    } else {
      return startedServices.get(0).getIntent();
    }
  }

  /** Clears all {@code Intent} started by {@link #startService(android.content.Intent)}. */
  void clearStartedServices() {
    startedServices.clear();
  }

  /**
   * Consumes the {@code Intent} requested to stop a service by {@link
   * #stopService(android.content.Intent)} from the bottom of the stack of stop requests.
   */
  Intent getNextStoppedService() {
    if (stoppedServices.isEmpty()) {
      return null;
    } else {
      return stoppedServices.remove(0).getIntent();
    }
  }

  void sendStickyBroadcast(Intent intent, Context context) {
    stickyIntents.put(intent.getAction(), intent);
    sendBroadcast(intent, context);
  }

  void sendBroadcast(Intent intent, Context context) {
    sendBroadcastWithPermission(intent, null, context);
  }

  Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, Context context) {
    return registerReceiver(receiver, filter, null, null, context);
  }

  Intent registerReceiver(
      BroadcastReceiver receiver,
      IntentFilter filter,
      String broadcastPermission,
      Handler scheduler,
      Context context) {
    return registerReceiverWithContext(receiver, filter, broadcastPermission, scheduler, context);
  }

  Intent registerReceiverWithContext(
      BroadcastReceiver receiver,
      IntentFilter filter,
      String broadcastPermission,
      Handler scheduler,
      Context context) {
    if (receiver != null) {
      registeredReceivers.add(
          new Wrapper(receiver, filter, context, broadcastPermission, scheduler));
    }
    return processStickyIntents(filter, receiver, context);
  }

  private Intent processStickyIntents(
      IntentFilter filter, BroadcastReceiver receiver, Context context) {
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

  void unregisterReceiver(BroadcastReceiver broadcastReceiver) {
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
  boolean hasReceiverForIntent(Intent intent) {
    for (Wrapper wrapper : registeredReceivers) {
      if (wrapper.intentFilter.matchAction(intent.getAction())) {
        return true;
      }
    }
    return false;
  }

  /** @deprecated use PackageManager.queryBroadcastReceivers instead */
  @Deprecated
  List<BroadcastReceiver> getReceiversForIntent(Intent intent) {
    ArrayList<BroadcastReceiver> broadcastReceivers = new ArrayList<>();
    for (Wrapper wrapper : registeredReceivers) {
      if (wrapper.intentFilter.matchAction(intent.getAction())) {
        broadcastReceivers.add(wrapper.getBroadcastReceiver());
      }
    }
    return broadcastReceivers;
  }

  /** @return list of {@link Wrapper}s for registered receivers */
  List<Wrapper> getRegisteredReceivers() {
    return registeredReceivers;
  }

  int checkPermission(String permission, int pid, int uid) {
    Set<String> grantedPermissionsForPidUid = grantedPermissionsMap.get(new Pair(pid, uid));
    return grantedPermissionsForPidUid != null && grantedPermissionsForPidUid.contains(permission)
        ? PERMISSION_GRANTED
        : PERMISSION_DENIED;
  }

  void grantPermissions(String... permissionNames) {
    grantPermissions(Process.myPid(), Process.myUid(), permissionNames);
  }

  void grantPermissions(int pid, int uid, String... permissions) {
    Set<String> grantedPermissionsForPidUid = grantedPermissionsMap.get(new Pair<>(pid, uid));
    if (grantedPermissionsForPidUid == null) {
      grantedPermissionsForPidUid = new HashSet<>();
      grantedPermissionsMap.put(new Pair<>(pid, uid), grantedPermissionsForPidUid);
    }
    Collections.addAll(grantedPermissionsForPidUid, permissions);
  }

  void denyPermissions(String... permissionNames) {
    denyPermissions(Process.myPid(), Process.myUid(), permissionNames);
  }

  void denyPermissions(int pid, int uid, String... permissions) {
    Set<String> grantedPermissionsForPidUid = grantedPermissionsMap.get(new Pair<>(pid, uid));
    if (grantedPermissionsForPidUid != null) {
      for (String permissionName : permissions) {
        grantedPermissionsForPidUid.remove(permissionName);
      }
    }
  }

  private boolean hasMatchingPermission(String permission1, String permission2) {
    return permission1 == null ? permission2 == null : permission1.equals(permission2);
  }

  private Handler getMainHandler(Context context) {
    if (mainHandler == null) {
      mainHandler = new Handler(context.getMainLooper());
    }
    return mainHandler;
  }

  /** Accessor interface for {@link Instrumentation}'s internals. */
  @ForType(Instrumentation.class)
  public interface _Instrumentation_ {
    // <= JELLY_BEAN_MR1:
    void init(
        ActivityThread thread,
        Context instrContext,
        Context appContext,
        ComponentName component,
        @WithType("android.app.IInstrumentationWatcher") Object watcher);

    // > JELLY_BEAN_MR1:
    void init(
        ActivityThread thread,
        Context instrContext,
        Context appContext,
        ComponentName component,
        @WithType("android.app.IInstrumentationWatcher") Object watcher,
        @WithType("android.app.IUiAutomationConnection") Object uiAutomationConnection);
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

    private static ListenableFuture<BroadcastResultHolder> transform(
        BroadcastReceiver.PendingResult result) {
      ShadowBroadcastPendingResult shadowBroadcastPendingResult = Shadow.extract(result);
      return Futures.transform(
          shadowBroadcastPendingResult.getFuture(),
          pendingResult ->
              new BroadcastResultHolder(
                  pendingResult.getResultCode(),
                  pendingResult.getResultData(),
                  pendingResult.getResultExtras(false)),
          directExecutor());
    }
  }

  private static class ServiceConnectionDataWrapper {
    public final ComponentName componentNameForBindService;
    public final IBinder binderForBindService;

    private ServiceConnectionDataWrapper(
        ComponentName componentNameForBindService, IBinder binderForBindService) {
      this.componentNameForBindService = componentNameForBindService;
      this.binderForBindService = binderForBindService;
    }
  }

  public static Instrumentation getInstrumentation() {
    ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    return activityThread.getInstrumentation();
  }
}
