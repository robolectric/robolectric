package org.robolectric.shadows;

import static android.content.pm.PackageManager.MATCH_DEFAULT_ONLY;
import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.P;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.util.concurrent.Futures.immediateFuture;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.annotation.Nullable;
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
import android.text.TextUtils;
import android.util.Pair;
import com.google.common.collect.ImmutableList;
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
import javax.annotation.concurrent.GuardedBy;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowActivity.IntentForResult;
import org.robolectric.shadows.ShadowApplication.Wrapper;
import org.robolectric.util.Logger;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.WithType;

@Implements(value = Instrumentation.class, looseSignatures = true)
public class ShadowInstrumentation {

  @RealObject private Instrumentation realObject;

  private final List<Intent> startedActivities = Collections.synchronizedList(new ArrayList<>());
  private final List<IntentForResult> startedActivitiesForResults =
      Collections.synchronizedList(new ArrayList<>());
  private final Map<FilterComparison, TargetAndRequestCode> intentRequestCodeMap =
      Collections.synchronizedMap(new HashMap<>());
  private final List<Intent.FilterComparison> startedServices =
      Collections.synchronizedList(new ArrayList<>());
  private final List<Intent.FilterComparison> stoppedServices =
      Collections.synchronizedList(new ArrayList<>());
  private final List<Intent> broadcastIntents = Collections.synchronizedList(new ArrayList<>());
  private final Map<Intent, Bundle> broadcastOptions = Collections.synchronizedMap(new HashMap<>());
  private final Map<UserHandle, List<Intent>> broadcastIntentsForUser =
      Collections.synchronizedMap(new HashMap<>());
  private final List<ServiceConnection> boundServiceConnections =
      Collections.synchronizedList(new ArrayList<>());
  private final List<ServiceConnection> unboundServiceConnections =
      Collections.synchronizedList(new ArrayList<>());

  @GuardedBy("itself")
  private final List<Wrapper> registeredReceivers = new ArrayList<>();
  // map of pid+uid to granted permissions
  private final Map<Pair<Integer, Integer>, Set<String>> grantedPermissionsMap =
      Collections.synchronizedMap(new HashMap<>());
  private boolean unbindServiceShouldThrowIllegalArgument = false;
  private SecurityException exceptionForBindService = null;
  private boolean bindServiceCallsOnServiceConnectedInline;
  private final Map<Intent.FilterComparison, ServiceConnectionDataWrapper>
      serviceConnectionDataForIntent = Collections.synchronizedMap(new HashMap<>());
  // default values for bindService
  private ServiceConnectionDataWrapper defaultServiceConnectionData =
      new ServiceConnectionDataWrapper(null, null);
  private final List<String> unbindableActions = Collections.synchronizedList(new ArrayList<>());
  private final List<ComponentName> unbindableComponents =
      Collections.synchronizedList(new ArrayList<>());
  private final Map<String, Intent> stickyIntents =
      Collections.synchronizedMap(new LinkedHashMap<>());
  private Handler mainHandler;
  private final Map<ServiceConnection, ServiceConnectionDataWrapper>
      serviceConnectionDataForServiceConnection = Collections.synchronizedMap(new HashMap<>());

  private boolean checkActivities;
  // This will default to False in the future to correctly mirror real Android behavior.
  private boolean unbindServiceCallsOnServiceDisconnected = true;

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
    logStartedActivity(intent, null, requestCode, options);

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
    logStartedActivity(intent, null, requestCode, options);
    return null;
  }

  private void logStartedActivity(Intent intent, String target, int requestCode, Bundle options) {
    startedActivities.add(intent);
    intentRequestCodeMap.put(
        new FilterComparison(intent), new TargetAndRequestCode(target, requestCode));
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
    logStartedActivity(intent, target, requestCode, options);

    return directlyOn(realObject, Instrumentation.class)
        .execStartActivity(who, contextThread, token, target, intent, requestCode, options);
  }

  /**
   * Behaves as {@link #execStartActivity(Context, IBinder, IBinder, String, Intent, int, Bundle).
   *
   * <p>Currently ignores the user.
   */
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
    return execStartActivity(who, contextThread, token, resultWho, intent, requestCode, options);
  }

  @Implementation(minSdk = M, maxSdk = P)
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

  void sendOrderedBroadcastAsUser(
      Intent intent,
      UserHandle userHandle,
      String receiverPermission,
      BroadcastReceiver resultReceiver,
      Handler scheduler,
      int initialCode,
      String initialData,
      Bundle initialExtras,
      Context context) {
    List<Wrapper> receivers =
        getAppropriateWrappers(
            context, userHandle, intent, receiverPermission, /* broadcastOptions= */ null);
    sortByPriority(receivers);
    if (resultReceiver != null) {
      receivers.add(new Wrapper(resultReceiver, null, context, null, scheduler));
    }
    postOrderedToWrappers(receivers, intent, initialCode, initialData, initialExtras, context);
  }

  void assertNoBroadcastListenersOfActionRegistered(ContextWrapper context, String action) {
    synchronized (registeredReceivers) {
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
  }

  /** Returns the BroadcaseReceivers wrappers, matching intent's action and permissions. */
  private List<Wrapper> getAppropriateWrappers(
      Context context,
      @Nullable UserHandle userHandle,
      Intent intent,
      String receiverPermission,
      @Nullable Bundle broadcastOptions) {
    broadcastIntents.add(intent);
    this.broadcastOptions.put(intent, broadcastOptions);

    if (userHandle != null) {
      List<Intent> intentsForUser = broadcastIntentsForUser.get(userHandle);
      if (intentsForUser == null) {
        intentsForUser = new ArrayList<>();
        broadcastIntentsForUser.put(userHandle, intentsForUser);
      }
      intentsForUser.add(intent);
    }

    List<Wrapper> result = new ArrayList<>();

    List<Wrapper> copy = new ArrayList<>();
    synchronized (registeredReceivers) {
      copy.addAll(registeredReceivers);
    }

    for (Wrapper wrapper : copy) {
      if (broadcastReceiverMatchesIntent(context, wrapper, intent, receiverPermission)) {
        result.add(wrapper);
      }
    }
    System.err.format("Intent = %s; Matching wrappers: %s\n", intent, result);
    return result;
  }

  private static boolean broadcastReceiverMatchesIntent(
      Context broadcastContext, Wrapper wrapper, Intent intent, String receiverPermission) {
    String intentClass =
        intent.getComponent() != null ? intent.getComponent().getClassName() : null;
    boolean matchesIntentClass =
        intentClass != null && intentClass.equals(wrapper.broadcastReceiver.getClass().getName());

    // The receiver must hold the permission specified by sendBroadcast, and the broadcaster must
    // hold the permission specified by registerReceiver.
    boolean hasPermissionFromManifest =
        hasRequiredPermissionForBroadcast(wrapper.context, receiverPermission)
            && hasRequiredPermissionForBroadcast(broadcastContext, wrapper.broadcastPermission);
    // Many existing tests don't declare manifest permissions, relying on the old equality check.
    boolean hasPermissionForBackwardsCompatibility =
        TextUtils.equals(receiverPermission, wrapper.broadcastPermission);
    boolean hasPermission = hasPermissionFromManifest || hasPermissionForBackwardsCompatibility;

    boolean matchesAction = wrapper.intentFilter.matchAction(intent.getAction());

    final int match =
        wrapper.intentFilter.matchData(intent.getType(), intent.getScheme(), intent.getData());
    boolean matchesDataAndType =
        match != IntentFilter.NO_MATCH_DATA && match != IntentFilter.NO_MATCH_TYPE;

    return matchesIntentClass || (hasPermission && matchesAction && matchesDataAndType);
  }

  /** A null {@code requiredPermission} indicates that no permission is required. */
  private static boolean hasRequiredPermissionForBroadcast(
      Context context, @Nullable String requiredPermission) {
    return requiredPermission == null
        || RuntimeEnvironment.application
                .getPackageManager()
                .checkPermission(requiredPermission, context.getPackageName())
            == PERMISSION_GRANTED;
  }

  private void postIntent(
      Intent intent, Wrapper wrapper, final AtomicBoolean abort, Context context, int resultCode) {
    final Handler scheduler =
        (wrapper.scheduler != null) ? wrapper.scheduler : getMainHandler(context);
    final BroadcastReceiver receiver = wrapper.broadcastReceiver;
    final ShadowBroadcastReceiver shReceiver = Shadow.extract(receiver);
    final Intent broadcastIntent = intent;
    scheduler.post(
        new Runnable() {
          @Override
          public void run() {
            receiver.setPendingResult(
                ShadowBroadcastPendingResult.create(resultCode, null, null, false));
            shReceiver.onReceive(context, broadcastIntent, abort);
          }
        });
  }

  private void postToWrappers(
      List<Wrapper> wrappers, Intent intent, Context context, int resultCode) {
    AtomicBoolean abort =
        new AtomicBoolean(false); // abort state is shared among all broadcast receivers
    for (Wrapper wrapper : wrappers) {
      postIntent(intent, wrapper, abort, context, resultCode);
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
  void sendBroadcastWithPermission(
      Intent intent, UserHandle userHandle, String receiverPermission, Context context) {
    sendBroadcastWithPermission(
        intent,
        userHandle,
        receiverPermission,
        context,
        /* broadcastOptions= */ null,
        /* resultCode= */ 0);
  }

  void sendBroadcastWithPermission(
      Intent intent, String receiverPermission, Context context, int resultCode) {
    sendBroadcastWithPermission(
        intent, /*userHandle=*/ null, receiverPermission, context, null, resultCode);
  }

  void sendBroadcastWithPermission(
      Intent intent,
      String receiverPermission,
      Context context,
      @Nullable Bundle broadcastOptions,
      int resultCode) {
    sendBroadcastWithPermission(
        intent, /*userHandle=*/ null, receiverPermission, context, broadcastOptions, resultCode);
  }

  void sendBroadcastWithPermission(
      Intent intent,
      UserHandle userHandle,
      String receiverPermission,
      Context context,
      @Nullable Bundle broadcastOptions,
      int resultCode) {
    List<Wrapper> wrappers =
        getAppropriateWrappers(context, userHandle, intent, receiverPermission, broadcastOptions);
    postToWrappers(wrappers, intent, context, resultCode);
  }

  void sendOrderedBroadcastWithPermission(
      Intent intent, String receiverPermission, Context context) {
    List<Wrapper> wrappers =
        getAppropriateWrappers(
            context,
            /*userHandle=*/ null,
            intent,
            receiverPermission,
            /* broadcastOptions= */ null);
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

  @Nullable
  Bundle getBroadcastOptions(Intent intent) {
    synchronized (broadcastOptions) {
      for (Intent broadcastIntent : broadcastOptions.keySet()) {
        if (broadcastIntent.filterEquals(intent)) {
          return broadcastOptions.get(broadcastIntent);
        }
      }
      return null;
    }
  }

  List<Intent> getBroadcastIntentsForUser(UserHandle userHandle) {
    List<Intent> intentsForUser = broadcastIntentsForUser.get(userHandle);
    if (intentsForUser == null) {
      intentsForUser = new ArrayList<>();
      broadcastIntentsForUser.put(userHandle, intentsForUser);
    }
    return intentsForUser;
  }

  void clearBroadcastIntents() {
    broadcastIntents.clear();
    broadcastOptions.clear();
    broadcastIntentsForUser.clear();
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
    startedActivitiesForResults.clear();
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

  TargetAndRequestCode getTargetAndRequestCodeForIntent(Intent requestIntent) {
    return checkNotNull(
        intentRequestCodeMap.get(new Intent.FilterComparison(requestIntent)),
        "No intent matches %s among %s",
        requestIntent,
        intentRequestCodeMap.keySet());
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

  /**
   * Set the default IBinder implementation that will be returned when the service is bound using
   * the specified Intent. The IBinder can implement the methods to simulate a bound Service. Useful
   * for testing the ServiceConnection implementation.
   *
   * @param name The ComponentName of the Service
   * @param service The IBinder implementation to return when the service is bound.
   */
  void setComponentNameAndServiceForBindService(ComponentName name, IBinder service) {
    defaultServiceConnectionData = new ServiceConnectionDataWrapper(name, service);
  }

  /**
   * Set the IBinder implementation that will be returned when the service is bound using the
   * specified Intent. The IBinder can implement the methods to simulate a bound Service. Useful for
   * testing the ServiceConnection implementation.
   *
   * @param intent The exact Intent used in Context#bindService(...)
   * @param name The ComponentName of the Service
   * @param service The IBinder implementation to return when the service is bound.
   */
  void setComponentNameAndServiceForBindServiceForIntent(
      Intent intent, ComponentName name, IBinder service) {
    serviceConnectionDataForIntent.put(
        new Intent.FilterComparison(intent), new ServiceConnectionDataWrapper(name, service));
  }

  protected boolean bindService(
      final Intent intent, final ServiceConnection serviceConnection, int i) {
    boundServiceConnections.add(serviceConnection);
    unboundServiceConnections.remove(serviceConnection);
    if (exceptionForBindService != null) {
      throw exceptionForBindService;
    }
    final Intent.FilterComparison filterComparison = new Intent.FilterComparison(intent);
    final ServiceConnectionDataWrapper serviceConnectionDataWrapper =
        serviceConnectionDataForIntent.getOrDefault(filterComparison, defaultServiceConnectionData);
    if (unbindableActions.contains(intent.getAction())
        || unbindableComponents.contains(intent.getComponent())
        || unbindableComponents.contains(
            serviceConnectionDataWrapper.componentNameForBindService)) {
      return false;
    }
    startedServices.add(filterComparison);
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable onServiceConnectedRunnable =
        () -> {
          serviceConnectionDataForServiceConnection.put(
              serviceConnection, serviceConnectionDataWrapper);
          serviceConnection.onServiceConnected(
              serviceConnectionDataWrapper.componentNameForBindService,
              serviceConnectionDataWrapper.binderForBindService);
        };

    if (bindServiceCallsOnServiceConnectedInline) {
      onServiceConnectedRunnable.run();
    } else {
      handler.post(onServiceConnectedRunnable);
    }
    return true;
  }

  protected void setUnbindServiceCallsOnServiceDisconnected(boolean flag) {
    unbindServiceCallsOnServiceDisconnected = flag;
  }

  protected void unbindService(final ServiceConnection serviceConnection) {
    if (unbindServiceShouldThrowIllegalArgument) {
      throw new IllegalArgumentException();
    }

    unboundServiceConnections.add(serviceConnection);
    boundServiceConnections.remove(serviceConnection);
    Handler handler = new Handler(Looper.getMainLooper());
    handler.post(
        () -> {
          final ServiceConnectionDataWrapper serviceConnectionDataWrapper;
          if (serviceConnectionDataForServiceConnection.containsKey(serviceConnection)) {
            serviceConnectionDataWrapper =
                serviceConnectionDataForServiceConnection.get(serviceConnection);
          } else {
            serviceConnectionDataWrapper = defaultServiceConnectionData;
          }
          if (unbindServiceCallsOnServiceDisconnected) {
            Logger.warn(
                "Configured to call onServiceDisconnected when unbindService is called. This is"
                    + " not accurate Android behavior. Please update your tests and call"
                    + " ShadowActivity#setUnbindCallsOnServiceDisconnected(false). This will"
                    + " become default behavior in the future, which may break your tests if you"
                    + " are expecting this inaccurate behavior.");
            serviceConnection.onServiceDisconnected(
                serviceConnectionDataWrapper.componentNameForBindService);
          }
        });
  }

  protected List<ServiceConnection> getBoundServiceConnections() {
    return boundServiceConnections;
  }

  void setUnbindServiceShouldThrowIllegalArgument(boolean flag) {
    unbindServiceShouldThrowIllegalArgument = flag;
  }

  void setThrowInBindService(SecurityException e) {
    exceptionForBindService = e;
  }

  void setBindServiceCallsOnServiceConnectedDirectly(
      boolean bindServiceCallsOnServiceConnectedInline) {
    this.bindServiceCallsOnServiceConnectedInline = bindServiceCallsOnServiceConnectedInline;
  }

  protected List<ServiceConnection> getUnboundServiceConnections() {
    return unboundServiceConnections;
  }

  void declareActionUnbindable(String action) {
    unbindableActions.add(action);
  }

  void declareComponentUnbindable(ComponentName component) {
    checkNotNull(component);
    unbindableComponents.add(component);
  }

  public List<String> getUnbindableActions() {
    return unbindableActions;
  }

  List<ComponentName> getUnbindableComponents() {
    return unbindableComponents;
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
    sendBroadcastWithPermission(
        intent, /*userHandle=*/ null, /*receiverPermission=*/ null, context);
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
      synchronized (registeredReceivers) {
        registeredReceivers.add(
            new Wrapper(receiver, filter, context, broadcastPermission, scheduler));
      }
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

    synchronized (registeredReceivers) {
      Iterator<Wrapper> iterator = registeredReceivers.iterator();
      while (iterator.hasNext()) {
        Wrapper wrapper = iterator.next();
        if (wrapper.broadcastReceiver == broadcastReceiver) {
          iterator.remove();
          found = true;
        }
      }
    }

    if (!found) {
      throw new IllegalArgumentException("Receiver not registered: " + broadcastReceiver);
    }
  }

  void clearRegisteredReceivers() {
    synchronized (registeredReceivers) {
      registeredReceivers.clear();
    }
  }

  /** @deprecated use PackageManager.queryBroadcastReceivers instead */
  @Deprecated
  boolean hasReceiverForIntent(Intent intent) {
    synchronized (registeredReceivers) {
      for (Wrapper wrapper : registeredReceivers) {
        if (wrapper.intentFilter.matchAction(intent.getAction())) {
          return true;
        }
      }
    }
    return false;
  }

  /** @deprecated use PackageManager.queryBroadcastReceivers instead */
  @Deprecated
  List<BroadcastReceiver> getReceiversForIntent(Intent intent) {
    ArrayList<BroadcastReceiver> broadcastReceivers = new ArrayList<>();

    synchronized (registeredReceivers) {
      for (Wrapper wrapper : registeredReceivers) {
        if (wrapper.intentFilter.matchAction(intent.getAction())) {
          broadcastReceivers.add(wrapper.getBroadcastReceiver());
        }
      }
    }
    return broadcastReceivers;
  }

  /** @return copy of the list of {@link Wrapper}s for registered receivers */
  ImmutableList<Wrapper> getRegisteredReceivers() {
    ImmutableList<Wrapper> copy;
    synchronized (registeredReceivers) {
      copy = ImmutableList.copyOf(registeredReceivers);
    }

    return copy;
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

  static final class TargetAndRequestCode {
    final String target;
    final int requestCode;

    private TargetAndRequestCode(String target, int requestCode) {
      this.target = target;
      this.requestCode = requestCode;
    }
  }

  public static Instrumentation getInstrumentation() {
    ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    if (activityThread != null) {
      return activityThread.getInstrumentation();
    }
    return null;
  }
}
