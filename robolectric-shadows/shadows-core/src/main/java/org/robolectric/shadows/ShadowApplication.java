package org.robolectric.shadows;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.internal.Shadow.newInstanceOf;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.view.Display;
import android.view.LayoutInflater;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.Toast;

import org.robolectric.RoboSettings;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.manifest.BroadcastReceiverData;
import org.robolectric.res.ResourceLoader;
import org.robolectric.util.Scheduler;

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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Shadow for {@link android.app.Application}.
 */
@Implements(Application.class)
public class ShadowApplication extends ShadowContextWrapper {
  @RealObject private Application realApplication;

  private AndroidManifest appManifest;
  private ResourceLoader resourceLoader;
  private ContentResolver contentResolver;
  private List<Intent> startedActivities = new ArrayList<>();
  private List<Intent.FilterComparison> startedServices = new ArrayList<>();
  private List<Intent.FilterComparison> stoppedServices = new ArrayList<>();
  private List<Intent> broadcastIntents = new ArrayList<>();
  private List<ServiceConnection> boundServiceConnections = new ArrayList<>();
  private List<ServiceConnection> unboundServiceConnections = new ArrayList<>();
  private List<Wrapper> registeredReceivers = new ArrayList<>();
  private Map<String, Intent> stickyIntents = new LinkedHashMap<>();
  private Looper mainLooper = Looper.myLooper();
  private Handler mainHandler = new Handler(mainLooper);
  private Scheduler backgroundScheduler = RoboSettings.isUseGlobalScheduler() ? getForegroundThreadScheduler() : new Scheduler();
  private Map<String, Map<String, Object>> sharedPreferenceMap = new HashMap<>();
  private ArrayList<Toast> shownToasts = new ArrayList<>();
  private PowerManager.WakeLock latestWakeLock;
  private ShadowAlertDialog latestAlertDialog;
  private ShadowDialog latestDialog;
  private ShadowPopupMenu latestPopupMenu;
  private Object bluetoothAdapter = newInstanceOf("android.bluetooth.BluetoothAdapter");
  private Resources resources;
  private AssetManager assetManager;
  private Set<String> grantedPermissions = new HashSet<>();

  private Map<Intent.FilterComparison, ServiceConnectionDataWrapper> serviceConnectionDataForIntent = new HashMap<>();
  private Map<ServiceConnection, ServiceConnectionDataWrapper> serviceConnectionDataForServiceConnection = new HashMap<>();
  //default values for bindService
  private ServiceConnectionDataWrapper defaultServiceConnectionData = new ServiceConnectionDataWrapper(null, null);

  // these are managed by the AppSingletonizier... kinda gross, sorry [xw]
  LayoutInflater layoutInflater;
  AppWidgetManager appWidgetManager;
  private List<String> unbindableActions = new ArrayList<>();

  private boolean strictI18n = false;
  private boolean checkActivities;
  private PopupWindow latestPopupWindow;
  private ListPopupWindow latestListPopupWindow;

  public static ShadowApplication getInstance() {
    return RuntimeEnvironment.application == null ? null : shadowOf(RuntimeEnvironment.application);
  }

  /**
   * Runs any background tasks previously queued by {@link android.os.AsyncTask#execute(Object[])}.
   *
   * <p>
   * Note: calling this method does not pause or un-pause the scheduler.
   */
  public static void runBackgroundTasks() {
    getInstance().getBackgroundThreadScheduler().advanceBy(0);
  }

  public static void setDisplayMetricsDensity(float densityMultiplier) {
    shadowOf(getInstance().getResources()).setDensity(densityMultiplier);
  }

  public static void setDefaultDisplay(Display display) {
    shadowOf(getInstance().getResources()).setDisplay(display);
  }

  /**
   * Associates a {@code ResourceLoader} with an {@code Application} instance.
   *
   * @param appManifest Android manifest.
   * @param resourceLoader Resource loader.
   */
  public void bind(AndroidManifest appManifest, ResourceLoader resourceLoader) {
    if (this.resourceLoader != null) throw new RuntimeException("ResourceLoader already set!");
    this.appManifest = appManifest;
    this.resourceLoader = resourceLoader;

    if (appManifest != null) {
      setPackageName(appManifest.getPackageName());
      this.registerBroadcastReceivers(appManifest);
    }
  }

  private void registerBroadcastReceivers(AndroidManifest androidManifest) {
    for (BroadcastReceiverData receiver : androidManifest.getBroadcastReceivers()) {
      IntentFilter filter = new IntentFilter();
      for (String action : receiver.getActions()) {
        filter.addAction(action);
      }
      String receiverClassName = replaceLastDotWith$IfInnerStaticClass(receiver.getClassName());
      registerReceiver((BroadcastReceiver) newInstanceOf(receiverClassName), filter);
    }
  }

  private static String replaceLastDotWith$IfInnerStaticClass(String receiverClassName) {
    String[] splits = receiverClassName.split("\\.");
    String staticInnerClassRegex = "[A-Z][a-zA-Z]*";
    if (splits[splits.length - 1].matches(staticInnerClassRegex) && splits[splits.length - 2].matches(staticInnerClassRegex)) {
      int lastDotIndex = receiverClassName.lastIndexOf(".");
      StringBuilder buffer = new StringBuilder(receiverClassName);
      buffer.setCharAt(lastDotIndex, '$');
      return buffer.toString();
    }
    return receiverClassName;
  }

  public List<Toast> getShownToasts() {
    return shownToasts;
  }

  /**
   * Return the foreground scheduler.
   *
   * @return  Foreground scheduler.
   */
  public Scheduler getForegroundThreadScheduler() {
    return RuntimeEnvironment.getMasterScheduler();
  }

  /**
   * Return the background scheduler.
   *
   * @return  Background scheduler.
   */
  public Scheduler getBackgroundThreadScheduler() {
    return backgroundScheduler;
  }

  @Override
  @Implementation
  public Context getApplicationContext() {
    return realApplication;
  }

  @Override
  @Implementation
  public AssetManager getAssets() {
    if (assetManager == null) {
      assetManager = ShadowAssetManager.bind(newInstanceOf(AssetManager.class), appManifest, resourceLoader);
    }
    return assetManager;
  }

  @Override
  @Implementation
  public Resources getResources() {
    if (resources == null) {
      resources = new Resources(realApplication.getAssets(), null, new Configuration());
    }
    return resources;
  }

  /**
   * Reset (set to null) resources instance, so they will be reloaded next time they are
   * {@link #getResources gotten}
   */
  public void resetResources(){
    resources = null;
  }

  @Implementation
  @Override
  public ContentResolver getContentResolver() {
    if (contentResolver == null) {
      contentResolver = new ContentResolver(realApplication) {
        @Override
        protected IContentProvider acquireProvider(Context c, String name) {
          return null;
        }

        @Override
        public boolean releaseProvider(IContentProvider icp) {
          return false;
        }

        @Override
        protected IContentProvider acquireUnstableProvider(Context c, String name) {
          return null;
        }

        @Override
        public boolean releaseUnstableProvider(IContentProvider icp) {
          return false;
        }

        @Override
        public void unstableProviderDied(IContentProvider icp) {

        }
      };
    }
    return contentResolver;
  }

  @Implementation
  @Override
  public void startActivity(Intent intent) {
    verifyActivityInManifest(intent);
    startedActivities.add(intent);
  }

  @Implementation
  @Override
  public void startActivity(Intent intent, Bundle options) {
    verifyActivityInManifest(intent);
    startedActivities.add(intent);
  }

  @Implementation
  @Override
  public ComponentName startService(Intent intent) {
    startedServices.add(new Intent.FilterComparison(intent));
    if (intent.getComponent() != null) {
      return intent.getComponent();
    }
    return new ComponentName("some.service.package", "SomeServiceName-FIXME");
  }

  @Implementation
  @Override
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
    shadowOf(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        final ServiceConnectionDataWrapper serviceConnectionDataWrapper;
        final Intent.FilterComparison filterComparison = new Intent.FilterComparison(intent);
        if (serviceConnectionDataForIntent.containsKey(filterComparison)) {
          serviceConnectionDataWrapper = serviceConnectionDataForIntent.get(filterComparison);
        } else {
          serviceConnectionDataWrapper = defaultServiceConnectionData;
        }
        serviceConnectionDataForServiceConnection.put(serviceConnection, serviceConnectionDataWrapper);
        serviceConnection.onServiceConnected(serviceConnectionDataWrapper.componentNameForBindService, serviceConnectionDataWrapper.binderForBindService);
      }
    }, 0);
    return true;
  }

  public List<ServiceConnection> getBoundServiceConnections() {
    return boundServiceConnections;
  }

  @Override @Implementation
  public void unbindService(final ServiceConnection serviceConnection) {
    unboundServiceConnections.add(serviceConnection);
    boundServiceConnections.remove(serviceConnection);
    shadowOf(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        final ServiceConnectionDataWrapper serviceConnectionDataWrapper;
        if (serviceConnectionDataForServiceConnection.containsKey(serviceConnection)) {
          serviceConnectionDataWrapper = serviceConnectionDataForServiceConnection.get(serviceConnection);
        } else {
          serviceConnectionDataWrapper = defaultServiceConnectionData;
        }
        serviceConnection.onServiceDisconnected(serviceConnectionDataWrapper.componentNameForBindService);
      }
    }, 0);
  }

  public List<ServiceConnection> getUnboundServiceConnections() {
    return unboundServiceConnections;
  }
  /**
   * Consumes the most recent {@code Intent} started by {@link #startActivity(android.content.Intent)} and returns it.
   *
   * @return the most recently started {@code Intent}
   */
  @Override
  public Intent getNextStartedActivity() {
    if (startedActivities.isEmpty()) {
      return null;
    } else {
      return startedActivities.remove(0);
    }
  }

  /**
   * Returns the most recent {@code Intent} started by {@link #startActivity(android.content.Intent)} without
   * consuming it.
   *
   * @return the most recently started {@code Intent}
   */
  @Override
  public Intent peekNextStartedActivity() {
    if (startedActivities.isEmpty()) {
      return null;
    } else {
      return startedActivities.get(0);
    }
  }

  /**
   * Consumes the most recent {@code Intent} started by {@link #startService(android.content.Intent)} and returns it.
   *
   * @return the most recently started {@code Intent}
   */
  @Override
  public Intent getNextStartedService() {
    if (startedServices.isEmpty()) {
      return null;
    } else {
      return startedServices.remove(0).getIntent();
    }
  }

  /**
   * Returns the most recent {@code Intent} started by {@link #startService(android.content.Intent)} without
   * consuming it.
   *
   * @return the most recently started {@code Intent}
   */
  @Override
  public Intent peekNextStartedService() {
    if (startedServices.isEmpty()) {
      return null;
    } else {
      return startedServices.get(0).getIntent();
    }
  }

  /**
   * Clears all {@code Intent} started by {@link #startService(android.content.Intent)}
   */
  @Override
  public void clearStartedServices() {
    startedServices.clear();
  }

  /**
   * Consumes the {@code Intent} requested to stop a service by {@link #stopService(android.content.Intent)}
   * from the bottom of the stack of stop requests.
   */
  @Override
  public Intent getNextStoppedService() {
    if (stoppedServices.isEmpty()) {
      return null;
    } else {
      return stoppedServices.remove(0).getIntent();
    }
  }

  /**
   * Non-Android accessor (and a handy way to get a working {@code ResourceLoader}
   *
   * @return the {@code ResourceLoader} associated with this Application
   */
  public ResourceLoader getResourceLoader() {
    return resourceLoader;
  }

  @Override
  @Implementation
  public void sendBroadcast(Intent intent) {
    sendBroadcastWithPermission(intent, null);
  }

  @Override
  @Implementation
  public void sendBroadcast(Intent intent, String receiverPermission) {
    sendBroadcastWithPermission(intent, receiverPermission);
  }

  @Override
  @Implementation
  public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
    sendOrderedBroadcastWithPermission(intent, receiverPermission);
  }

  /*
    Returns the BroadcaseReceivers wrappers, matching intent's action and permissions.
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
    final Handler scheduler = (wrapper.scheduler != null) ? wrapper.scheduler : this.mainHandler;
    final BroadcastReceiver receiver = wrapper.broadcastReceiver;
    final ShadowBroadcastReceiver shReceiver = Shadows.shadowOf(receiver);
    final Intent broadcastIntent = intent;
    scheduler.post(new Runnable() {
      @Override
      public void run() {
        shReceiver.onReceive(realApplication, broadcastIntent, abort);
      }
    });
  }

  private void postToWrappers(List<Wrapper> wrappers, Intent intent, String receiverPermission) {
    AtomicBoolean abort = new AtomicBoolean(false); // abort state is shared among all broadcast receivers
    for (Wrapper wrapper: wrappers) {
      postIntent(intent, wrapper, abort);
    }
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
    postToWrappers(wrappers, intent, receiverPermission);
  }

  private void sendOrderedBroadcastWithPermission(Intent intent, String receiverPermission) {
    List<Wrapper> wrappers = getAppropriateWrappers(intent, receiverPermission);
    // sort by the decrease of priorities
    Collections.sort(wrappers, new Comparator<Wrapper>() {
      @Override
      public int compare(Wrapper o1, Wrapper o2) {
        return Integer.compare(o2.getIntentFilter().getPriority(), o1.getIntentFilter().getPriority());
      }
    });

    postToWrappers(wrappers, intent, receiverPermission);
  }

  public List<Intent> getBroadcastIntents() {
    return broadcastIntents;
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
  @Override
  @Implementation
  public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
    return registerReceiverWithContext(receiver, filter, null, null, realApplication);
  }

  @Override
  @Implementation
  public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
    return registerReceiverWithContext(receiver, filter, broadcastPermission, scheduler, realApplication);
  }

  Intent registerReceiverWithContext(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler, Context context) {
    if (receiver != null) {
      registeredReceivers.add(new Wrapper(receiver, filter, context, broadcastPermission, scheduler));
    }
    return processStickyIntents(filter, receiver, context);
  }

  private void verifyActivityInManifest(Intent intent) {
    if (checkActivities && getPackageManager().resolveActivity(intent, -1) == null) {
      throw new ActivityNotFoundException(intent.getAction());
    }
  }

  private Intent processStickyIntents(IntentFilter filter, BroadcastReceiver receiver, Context context) {
    Intent result = null;
    for (Intent stickyIntent : stickyIntents.values()) {
      if (filter.matchAction(stickyIntent.getAction())) {
        if (result == null) {
          result = stickyIntent;
        }
        if (receiver != null) {
          receiver.onReceive(context, stickyIntent);
        } else if (result != null) {
          break;
        }
      }
    }
    return result;
  }

  @Override
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

  /**
   * Iterates through all of the registered receivers on this {@code Application} and if any of them match the given
   * {@code Context} object throws a {@code RuntimeException}
   *
   * @param context the {@code Context} to check for on each of the remaining registered receivers
   * @param type    the type to report for the context if an exception is thrown
   * @throws RuntimeException if there are any recievers registered with the given {@code Context}
   */
  public void assertNoBroadcastListenersRegistered(Context context, String type) {
    for (Wrapper registeredReceiver : registeredReceivers) {
      if (registeredReceiver.context == context) {
        RuntimeException e = new IllegalStateException(type + " " + context + " leaked has leaked IntentReceiver "
            + registeredReceiver.broadcastReceiver + " that was originally registered here. " +
            "Are you missing a call to unregisterReceiver()?");
        e.setStackTrace(registeredReceiver.exception.getStackTrace());
        throw e;
      }
    }
  }

  public void assertNoBroadcastListenersOfActionRegistered(Context context, String action) {
    for (Wrapper registeredReceiver : registeredReceivers) {
      if (registeredReceiver.context == context) {
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

  public boolean hasReceiverForIntent(Intent intent) {
    for (Wrapper wrapper : registeredReceivers) {
      if (wrapper.intentFilter.matchAction(intent.getAction())) {
        return true;
      }
    }
    return false;
  }

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
   * Non-Android accessor.
   *
   * @return list of {@link Wrapper}s for registered receivers
   */
  public List<Wrapper> getRegisteredReceivers() {
    return registeredReceivers;
  }

  /**
   * Non-Android accessor.
   *
   * @return the layout inflater used by this {@code Application}
   */
  public LayoutInflater getLayoutInflater() {
    return layoutInflater;
  }

  /**
   * Non-Android accessor.
   *
   * @return the app widget manager used by this {@code Application}
   */
  public AppWidgetManager getAppWidgetManager() {
    return appWidgetManager;
  }

  @Override
  @Implementation
  public Looper getMainLooper() {
    return mainLooper;
  }

  public Map<String, Map<String, Object>> getSharedPreferenceMap() {
    return sharedPreferenceMap;
  }

  public ShadowAlertDialog getLatestAlertDialog() {
    return latestAlertDialog;
  }

  public void setLatestAlertDialog(ShadowAlertDialog latestAlertDialog) {
    this.latestAlertDialog = latestAlertDialog;
  }

  public ShadowDialog getLatestDialog() {
    return latestDialog;
  }

  public void setLatestDialog(ShadowDialog latestDialog) {
    this.latestDialog = latestDialog;
  }

  public Object getBluetoothAdapter() {
    return bluetoothAdapter;
  }

  public void declareActionUnbindable(String action) {
    unbindableActions.add(action);
  }

  @Deprecated
  public void setSystemService(String key, Object service) {
    ((ShadowContextImpl) shadowOf(realApplication.getBaseContext())).setSystemService(key, service);
  }

  public PowerManager.WakeLock getLatestWakeLock() {
    return latestWakeLock;
  }

  public void addWakeLock( PowerManager.WakeLock wl ) {
    latestWakeLock = wl;
  }

  public void clearWakeLocks() {
    latestWakeLock = null;
  }

  public boolean isStrictI18n() {
    return strictI18n;
  }

  public void setStrictI18n(boolean strictI18n) {
    this.strictI18n = strictI18n;
  }

  public AndroidManifest getAppManifest() {
    return appManifest;
  }

  private final Map<String, Object> singletons = new HashMap<>();

  public <T> T getSingleton(Class<T> clazz, Provider<T> provider) {
    synchronized (singletons) {
      //noinspection unchecked
      T item = (T) singletons.get(clazz.getName());
      if (item == null) {
        singletons.put(clazz.getName(), item = provider.get());
      }
      return item;
    }
  }

  /**
   * Set to true if you'd like Robolectric to strictly simulate the real Android behavior when
   * calling {@link Context#startActivity(android.content.Intent)}. Real Android throws a
   * {@link android.content.ActivityNotFoundException} if given
   * an {@link Intent} that is not known to the {@link android.content.pm.PackageManager}
   *
   * By default, this behavior is off (false).
   *
   * @param checkActivities True to validate activities.
   */
  public void checkActivities(boolean checkActivities) {
    this.checkActivities = checkActivities;
  }

  public ShadowPopupMenu getLatestPopupMenu() {
    return latestPopupMenu;
  }

  public void setLatestPopupMenu(ShadowPopupMenu latestPopupMenu) {
    this.latestPopupMenu = latestPopupMenu;
  }

  public PopupWindow getLatestPopupWindow() {
    return latestPopupWindow;
  }

  public void setLatestPopupWindow(PopupWindow latestPopupWindow) {
    this.latestPopupWindow = latestPopupWindow;
  }

  public ListPopupWindow getLatestListPopupWindow() {
    return latestListPopupWindow;
  }

  public void setLatestListPopupWindow(ListPopupWindow latestListPopupWindow) {
    this.latestListPopupWindow = latestListPopupWindow;
  }

  public class Wrapper {
    public BroadcastReceiver broadcastReceiver;
    public IntentFilter intentFilter;
    public Context context;
    public Throwable exception;
    public String broadcastPermission;
    public Handler scheduler;

    public Wrapper(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter, Context context, String broadcastPermission, Handler scheduler) {
      this.broadcastReceiver = broadcastReceiver;
      this.intentFilter = intentFilter;
      this.context = context;
      this.broadcastPermission = broadcastPermission;
      this.scheduler = scheduler;
      exception = new Throwable();
    }

    public BroadcastReceiver getBroadcastReceiver() {
      return broadcastReceiver;
    }

    public IntentFilter getIntentFilter() {
      return intentFilter;
    }

    public Context getContext() {
      return context;
    }
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

  private static class ServiceConnectionDataWrapper {
    public final ComponentName componentNameForBindService;
    public final IBinder binderForBindService;

    private ServiceConnectionDataWrapper(ComponentName componentNameForBindService, IBinder binderForBindService) {
      this.componentNameForBindService = componentNameForBindService;
      this.binderForBindService = binderForBindService;
    }
  }
}
