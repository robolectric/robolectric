package org.robolectric.shadows;

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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.widget.Toast;
import org.robolectric.AndroidManifest;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.ResourceLoader;
import org.robolectric.tester.org.apache.http.FakeHttpLayer;
import org.robolectric.util.Scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static org.robolectric.Robolectric.newInstanceOf;
import static org.robolectric.Robolectric.shadowOf;

/**
 * Shadows the {@code android.app.Application} class.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Application.class)
public class ShadowApplication extends ShadowContextWrapper {
  @RealObject private Application realApplication;

  private AndroidManifest appManifest;
  private ResourceLoader resourceLoader;
  private ContentResolver contentResolver;
  private List<Intent> startedActivities = new ArrayList<Intent>();
  private List<Intent> startedServices = new ArrayList<Intent>();
  private List<Intent> stoppedServies = new ArrayList<Intent>();
  private List<Intent> broadcastIntents = new ArrayList<Intent>();
  private List<ServiceConnection> unboundServiceConnections = new ArrayList<ServiceConnection>();
  private List<Wrapper> registeredReceivers = new ArrayList<Wrapper>();
  private Map<String, Intent> stickyIntents = new HashMap<String, Intent>();
  private FakeHttpLayer fakeHttpLayer = new FakeHttpLayer();
  private Looper mainLooper = ShadowLooper.myLooper();
  private Handler mainHandler = new Handler(mainLooper);
  private Scheduler backgroundScheduler = new Scheduler();
  private Map<String, Map<String, Object>> sharedPreferenceMap = new HashMap<String, Map<String, Object>>();
  private ArrayList<Toast> shownToasts = new ArrayList<Toast>();
  private PowerManager.WakeLock latestWakeLock;
  private ShadowAlertDialog latestAlertDialog;
  private ShadowDialog latestDialog;
  private Object bluetoothAdapter = Robolectric.newInstanceOf("android.bluetooth.BluetoothAdapter");
  private Resources resources;
  private AssetManager assetManager;
  private Set<String> grantedPermissions = new HashSet<String>();

  // these are managed by the AppSingletonizier... kinda gross, sorry [xw]
  LayoutInflater layoutInflater;
  AppWidgetManager appWidgetManager;
  private ServiceConnection serviceConnection;
  private ComponentName componentNameForBindService;
  private IBinder serviceForBindService;
  private List<String> unbindableActions = new ArrayList<String>();

  private boolean strictI18n = false;
  private boolean checkActivities;

  /**
   * Associates a {@code ResourceLoader} with an {@code Application} instance
   *
   * @param appManifest
   * @param resourceLoader resource loader
   */
  public void bind(AndroidManifest appManifest, ResourceLoader resourceLoader) {
    if (this.resourceLoader != null) throw new RuntimeException("ResourceLoader already set!");
    this.appManifest = appManifest;
    this.resourceLoader = resourceLoader;

    if (appManifest != null) {
      setPackageName(appManifest.getPackageName());
      setApplicationName(appManifest.getApplicationName());

      this.registerBroadcastReceivers(appManifest);
    }
  }

  private void registerBroadcastReceivers(AndroidManifest androidManifest) {
    for (int i = 0; i < androidManifest.getReceiverCount(); i++) {
      IntentFilter filter = new IntentFilter();
      for (String action : androidManifest.getReceiverIntentFilterActions(i)) {
        filter.addAction(action);
      }
      String receiverClassName = replaceLastDotWith$IfInnerStaticClass(androidManifest.getReceiverClassName(i));
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

  public Scheduler getBackgroundScheduler() {
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
      assetManager = ShadowAssetManager.bind(Robolectric.newInstanceOf(AssetManager.class), appManifest, resourceLoader);
    }
    return assetManager;
  }

  @Override
  @Implementation
  public Resources getResources() {
    if (resources == null ) {
      resources = ShadowResources.bind(new Resources(realApplication.getAssets(), null, new Configuration()), resourceLoader);
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
    if (checkActivities && getPackageManager().resolveActivity(intent, -1) == null) {
      throw new ActivityNotFoundException(intent.getAction());
    } else {
      startedActivities.add(intent);
    }
  }

  @Implementation
  @Override
  public ComponentName startService(Intent intent) {
    startedServices.add(intent);
    if (intent.getComponent() != null) {
      return intent.getComponent();
    }
    return new ComponentName("some.service.package", "SomeServiceName-FIXME");
  }

  @Implementation
  @Override
  public boolean stopService(Intent name) {
    stoppedServies.add(name);

    return startedServices.contains(name);
  }

  public void setComponentNameAndServiceForBindService(ComponentName name, IBinder service) {
    this.componentNameForBindService = name;
    this.serviceForBindService = service;
  }

  @Implementation
  public boolean bindService(Intent intent, final ServiceConnection serviceConnection, int i) {
    if (unbindableActions.contains(intent.getAction())) {
      return false;
    }
    startedServices.add(intent);
    shadowOf(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        serviceConnection.onServiceConnected(componentNameForBindService, serviceForBindService);
      }
    }, 0);
    return true;
  }

  @Override @Implementation
  public void unbindService(final ServiceConnection serviceConnection) {
    unboundServiceConnections.add(serviceConnection);
    shadowOf(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        serviceConnection.onServiceDisconnected(componentNameForBindService);
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
      return startedServices.remove(0);
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
      return startedServices.get(0);
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
    if (stoppedServies.isEmpty()) {
      return null;
    } else {
      return stoppedServies.remove(0);
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

  /**
   * Broadcasts the {@code Intent} by iterating through the registered receivers, invoking their filters including
   * permissions, and calling {@code onReceive(Application, Intent)} as appropriate. Does not enqueue the
   * {@code Intent} for later inspection.
   *
   * @param intent the {@code Intent} to broadcast
   *               todo: enqueue the Intent for later inspection
   */
  private void sendBroadcastWithPermission(Intent intent, String receiverPermission) {
    broadcastIntents.add(intent);

    List<Wrapper> copy = new ArrayList<Wrapper>();
    copy.addAll(registeredReceivers);
    for (Wrapper wrapper : copy) {
      if (hasMatchingPermission(wrapper.broadcastPermission, receiverPermission)
          && wrapper.intentFilter.matchAction(intent.getAction())) {
        final Handler scheduler = (wrapper.scheduler != null) ? wrapper.scheduler : this.mainHandler;
        final BroadcastReceiver receiver = wrapper.broadcastReceiver;
        final Intent broadcastIntent = intent;
        scheduler.post(new Runnable() {
          @Override
          public void run() {
            receiver.onReceive(realApplication, broadcastIntent);
          }
        });
      }
    }
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
    return getStickyIntent(filter);
  }

  private Intent getStickyIntent(IntentFilter filter) {
    for (Intent stickyIntent : stickyIntents.values()) {
      String action = null;
      for (int i = 0; i < filter.countActions(); i++) {
        action = filter.getAction(i);
        if (stickyIntent.getAction().equals(action)) {
          return stickyIntent;
        }
      }
    }

    return null;
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
    ArrayList<BroadcastReceiver> broadcastReceivers = new ArrayList<BroadcastReceiver>();
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

  public FakeHttpLayer getFakeHttpLayer() {
    return fakeHttpLayer;
  }

  public void setFakeHttpLayer(FakeHttpLayer fakeHttpLayer) {
    if (fakeHttpLayer == null) {
      throw new IllegalArgumentException();
    }
    this.fakeHttpLayer = fakeHttpLayer;
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

  private final Map<String, Object> singletons = new HashMap<String, Object>();

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

  public void checkActivities(boolean checkActivities) {
    this.checkActivities = checkActivities;
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
    for (String permissionName : permissionNames) {
      grantedPermissions.add(permissionName);
    }
  }

  public void denyPermissions(String... permissionNames) {
    for (String permissionName : permissionNames) {
      grantedPermissions.remove(permissionName);
    }
  }

  private boolean hasMatchingPermission(String permission1, String permission2) {
    return permission1 == null ? permission2 == null : permission1.equals(permission2);
  }
}
