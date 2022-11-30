package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.L;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.Nullable;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.View;
import android.widget.RemoteViews;
import com.android.internal.appwidget.IAppWidgetService;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.ForType;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AppWidgetManager.class)
public class ShadowAppWidgetManager {

  @RealObject private AppWidgetManager realAppWidgetManager;

  private Context context;
  private final Map<Integer, WidgetInfo> widgetInfos = new HashMap<>();
  private int nextWidgetId = 1;
  private boolean alwaysRecreateViewsDuringUpdate = false;
  private boolean allowedToBindWidgets;
  private boolean requestPinAppWidgetSupported = false;
  private boolean validWidgetProviderComponentName = true;
  private final ArrayList<AppWidgetProviderInfo> installedProviders = new ArrayList<>();
  private Multimap<UserHandle, AppWidgetProviderInfo> installedProvidersForProfile =
      HashMultimap.create();

  // AppWidgetProvider is enabled if at least one widget is active. `isWidgetsEnabled` should be set
  //  to false if the last widget is removed (when removing widgets is implemented).
  private boolean isWidgetsEnabled = false;

  @Implementation(maxSdk = KITKAT)
  protected void __constructor__(Context context) {
    this.context = context;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void __constructor__(Context context, IAppWidgetService service) {
    this.context = context;
  }

  @Implementation
  protected void updateAppWidget(int[] appWidgetIds, RemoteViews views) {
    for (int appWidgetId : appWidgetIds) {
      updateAppWidget(appWidgetId, views);
    }
  }

  /**
   * Simulates updating an {@code AppWidget} with a new set of views
   *
   * @param appWidgetId id of widget
   * @param views views to update
   */
  @Implementation
  protected void updateAppWidget(int appWidgetId, RemoteViews views) {
    WidgetInfo widgetInfo = widgetInfos.get(appWidgetId);
    if (canReapplyRemoteViews(widgetInfo, views)) {
      views.reapply(context, widgetInfo.view);
    } else {
      widgetInfo.view = views.apply(context, new AppWidgetHostView(context));
      widgetInfo.layoutId = getRemoteViewsToApply(views).getLayoutId();
    }
    widgetInfo.lastRemoteViews = views;
  }

  private boolean canReapplyRemoteViews(WidgetInfo widgetInfo, RemoteViews views) {
    if (alwaysRecreateViewsDuringUpdate) {
      return false;
    }
    if (VERSION.SDK_INT < 25 && !hasLandscapeAndPortraitLayouts(views)) {
      return widgetInfo.layoutId == views.getLayoutId();
    }
    RemoteViews remoteViewsToApply = getRemoteViewsToApply(views);
    if (VERSION.SDK_INT >= 25) {
      return widgetInfo.layoutId == remoteViewsToApply.getLayoutId();
    } else {
      return widgetInfo.view != null && widgetInfo.view.getId() == remoteViewsToApply.getLayoutId();
    }
  }

  private RemoteViews getRemoteViewsToApply(RemoteViews views) {
    return reflector(RemoteViewsReflector.class, views).getRemoteViewsToApply(context);
  }

  private static boolean hasLandscapeAndPortraitLayouts(RemoteViews views) {
    return reflector(RemoteViewsReflector.class, views).hasLandscapeAndPortraitLayouts();
  }

  @Implementation
  protected int[] getAppWidgetIds(ComponentName provider) {
    List<Integer> idList = new ArrayList<>();
    for (int id : widgetInfos.keySet()) {
      WidgetInfo widgetInfo = widgetInfos.get(id);
      if (provider.equals(widgetInfo.providerComponent)) {
        idList.add(id);
      }
    }
    int ids[] = new int[idList.size()];
    for (int i = 0; i < idList.size(); i++) {
      ids[i] = idList.get(i);
    }
    return ids;
  }

  @Implementation
  protected List<AppWidgetProviderInfo> getInstalledProviders() {
    return new ArrayList<>(installedProviders);
  }

  @Implementation(minSdk = L)
  protected List<AppWidgetProviderInfo> getInstalledProvidersForProfile(UserHandle profile) {
    return ImmutableList.copyOf(installedProvidersForProfile.get(profile));
  }

  @Implementation(minSdk = O)
  protected List<AppWidgetProviderInfo> getInstalledProvidersForPackage(
      String packageName, UserHandle profile) {
    return ImmutableList.copyOf(
        installedProvidersForProfile.get(profile).stream()
            .filter(
                (AppWidgetProviderInfo providerInfo) ->
                    providerInfo.provider.getPackageName().equals(packageName))
            .collect(Collectors.toList()));
  }

  public void addInstalledProvider(AppWidgetProviderInfo appWidgetProviderInfo) {
    installedProviders.add(appWidgetProviderInfo);
  }

  public boolean removeInstalledProvider(AppWidgetProviderInfo appWidgetProviderInfo) {
    return installedProviders.remove(appWidgetProviderInfo);
  }

  public void addInstalledProvidersForProfile(
      UserHandle userHandle, AppWidgetProviderInfo appWidgetProviderInfo) {
    installedProvidersForProfile.put(userHandle, appWidgetProviderInfo);
  }

  public void addBoundWidget(int appWidgetId, AppWidgetProviderInfo providerInfo) {
    addInstalledProvider(providerInfo);
    bindAppWidgetId(appWidgetId, providerInfo.provider);
    widgetInfos.get(appWidgetId).info = providerInfo;
  }

  @Deprecated
  public void putWidgetInfo(int appWidgetId, AppWidgetProviderInfo expectedWidgetInfo) {
    addBoundWidget(appWidgetId, expectedWidgetInfo);
  }

  @Implementation
  protected AppWidgetProviderInfo getAppWidgetInfo(int appWidgetId) {
    WidgetInfo widgetInfo = widgetInfos.get(appWidgetId);
    if (widgetInfo == null) return null;
    return widgetInfo.info;
  }

  /** Gets the appWidgetOptions Bundle stored in a local cache. */
  @Implementation
  protected Bundle getAppWidgetOptions(int appWidgetId) {
    WidgetInfo widgetInfo = widgetInfos.get(appWidgetId);
    if (widgetInfo == null) {
      return Bundle.EMPTY;
    }
    return (Bundle) widgetInfo.options.clone();
  }

  /**
   * Update the locally cached appWidgetOptions Bundle. Instead of triggering associated
   * AppWidgetProvider.onAppWidgetOptionsChanged through Intent, this implementation calls the
   * method directly.
   */
  @Implementation
  protected void updateAppWidgetOptions(int appWidgetId, Bundle options) {
    WidgetInfo widgetInfo = widgetInfos.get(appWidgetId);
    if (widgetInfo != null && options != null) {
      widgetInfo.options.putAll(options);
      if (widgetInfo.appWidgetProvider != null) {
        widgetInfo.appWidgetProvider.onAppWidgetOptionsChanged(
            context, realAppWidgetManager, appWidgetId, (Bundle) options.clone());
      }
    }
  }

  @HiddenApi
  @Implementation
  public void bindAppWidgetId(int appWidgetId, ComponentName provider) {
    bindAppWidgetId(appWidgetId, provider, null);
  }

  @HiddenApi
  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected void bindAppWidgetId(int appWidgetId, ComponentName provider, Bundle options) {
    WidgetInfo widgetInfo = new WidgetInfo(provider);
    widgetInfos.put(appWidgetId, widgetInfo);
    if (options != null) {
      widgetInfo.options = (Bundle) options.clone();
    }
    for (AppWidgetProviderInfo appWidgetProviderInfo : installedProviders) {
      if (provider != null && provider.equals(appWidgetProviderInfo.provider)) {
        widgetInfo.info = appWidgetProviderInfo;
      }
    }
  }

  /**
   * Create an internal presentation of the widget and cache it locally. This implementation doesn't
   * trigger {@code AppWidgetProvider.onUpdate}
   */
  @Implementation
  protected boolean bindAppWidgetIdIfAllowed(int appWidgetId, ComponentName provider) {
    return bindAppWidgetIdIfAllowed(appWidgetId, provider, null);
  }

  /**
   * Create an internal presentation of the widget locally and store the options {@link Bundle} with
   * it. This implementation doesn't trigger {@code AppWidgetProvider.onUpdate}
   */
  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected boolean bindAppWidgetIdIfAllowed(
      int appWidgetId, ComponentName provider, Bundle options) {
    if (validWidgetProviderComponentName) {
      bindAppWidgetId(appWidgetId, provider, options);
      return allowedToBindWidgets;
    } else {
      throw new IllegalArgumentException("not an appwidget provider");
    }
  }

  /** Returns true if {@link setSupportedToRequestPinAppWidget} is called with {@code true} */
  @Implementation(minSdk = O)
  protected boolean isRequestPinAppWidgetSupported() {
    return requestPinAppWidgetSupported;
  }

  /**
   * This implementation currently uses {@code requestPinAppWidgetSupported} to determine if it
   * should bind the app widget provided and execute the {@code successCallback}.
   *
   * <p>Note: This implementation doesn't trigger {@code AppWidgetProvider.onUpdate}.
   *
   * @param provider The provider for the app widget to bind.
   * @param extras Returned in the callback along with the ID of the newly bound app widget, sent as
   *     {@link AppWidgetManager#EXTRA_APPWIDGET_ID}.
   * @param successCallback Called after binding the app widget, if possible.
   * @return true if the widget was installed, false otherwise.
   */
  @Implementation(minSdk = O)
  protected boolean requestPinAppWidget(
      ComponentName provider, @Nullable Bundle extras, @Nullable PendingIntent successCallback) {
    if (requestPinAppWidgetSupported) {
      int myWidgetId = nextWidgetId++;
      // Bind the widget.
      bindAppWidgetId(myWidgetId, provider);

      // Call the success callback if it exists.
      if (successCallback != null) {
        try {
          successCallback.send(
              context.getApplicationContext(),
              0,
              new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, myWidgetId));
        } catch (CanceledException e) {
          throw new RuntimeException(e);
        }
      }
      return true;
    }

    return false;
  }

  /**
   * Triggers a reapplication of the most recent set of actions against the widget, which is what
   * happens when the phone is rotated. Does not attempt to simulate a change in screen geometry.
   *
   * @param appWidgetId the ID of the widget to be affected
   */
  public void reconstructWidgetViewAsIfPhoneWasRotated(int appWidgetId) {
    WidgetInfo widgetInfo = widgetInfos.get(appWidgetId);
    widgetInfo.view = widgetInfo.lastRemoteViews.apply(context, new AppWidgetHostView(context));
  }

  private void enableWidgetsIfNecessary(Class<? extends AppWidgetProvider> appWidgetProviderClass) {
    if (!isWidgetsEnabled) {
      isWidgetsEnabled = true;
      AppWidgetProvider appWidgetProvider =
          ReflectionHelpers.callConstructor(appWidgetProviderClass);
      appWidgetProvider.onReceive(context, new Intent(AppWidgetManager.ACTION_APPWIDGET_ENABLED));
    }
  }

  /**
   * Creates a widget by inflating its layout.
   *
   * @param appWidgetProviderClass the app widget provider class
   * @param widgetLayoutId id of the layout to inflate
   * @return the ID of the new widget
   */
  public int createWidget(
      Class<? extends AppWidgetProvider> appWidgetProviderClass, int widgetLayoutId) {
    return createWidgets(appWidgetProviderClass, widgetLayoutId, 1)[0];
  }

  /**
   * Creates a bunch of widgets by inflating the same layout multiple times.
   *
   * @param appWidgetProviderClass the app widget provider class
   * @param widgetLayoutId id of the layout to inflate
   * @param howManyToCreate number of new widgets to create
   * @return the IDs of the new widgets
   */
  public int[] createWidgets(
      Class<? extends AppWidgetProvider> appWidgetProviderClass,
      int widgetLayoutId,
      int howManyToCreate) {
    AppWidgetProvider appWidgetProvider = ReflectionHelpers.callConstructor(appWidgetProviderClass);

    int[] newWidgetIds = new int[howManyToCreate];
    for (int i = 0; i < howManyToCreate; i++) {
      int myWidgetId = nextWidgetId++;
      RemoteViews remoteViews = new RemoteViews(context.getPackageName(), widgetLayoutId);
      View widgetView = remoteViews.apply(context, new AppWidgetHostView(context));
      WidgetInfo widgetInfo =
          new WidgetInfo(widgetView, widgetLayoutId, context, appWidgetProvider);
      widgetInfo.lastRemoteViews = remoteViews;
      widgetInfos.put(myWidgetId, widgetInfo);
      newWidgetIds[i] = myWidgetId;
    }

    // Enable widgets if we are creating the first widget.
    enableWidgetsIfNecessary(appWidgetProviderClass);

    Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, newWidgetIds);
    appWidgetProvider.onReceive(context, intent);
    return newWidgetIds;
  }

  /**
   * @param widgetId id of the desired widget
   * @return the widget associated with {@code widgetId}
   */
  public View getViewFor(int widgetId) {
    return widgetInfos.get(widgetId).view;
  }

  /**
   * @param widgetId id of the widget whose provider is to be returned
   * @return the {@code AppWidgetProvider} associated with {@code widgetId}
   */
  public AppWidgetProvider getAppWidgetProviderFor(int widgetId) {
    return widgetInfos.get(widgetId).appWidgetProvider;
  }

  /**
   * Enables testing of widget behavior when all of the views are recreated on every update. This is
   * useful for ensuring that your widget will behave correctly even if it is restarted by the OS
   * between events.
   *
   * @param alwaysRecreate whether or not to always recreate the views
   */
  public void setAlwaysRecreateViewsDuringUpdate(boolean alwaysRecreate) {
    alwaysRecreateViewsDuringUpdate = alwaysRecreate;
  }

  /**
   * @return the state of the{@code alwaysRecreateViewsDuringUpdate} flag
   */
  public boolean getAlwaysRecreateViewsDuringUpdate() {
    return alwaysRecreateViewsDuringUpdate;
  }

  public void setAllowedToBindAppWidgets(boolean allowed) {
    allowedToBindWidgets = allowed;
  }

  public void setRequestPinAppWidgetSupported(boolean supported) {
    requestPinAppWidgetSupported = supported;
  }

  public void setValidWidgetProviderComponentName(boolean validWidgetProviderComponentName) {
    this.validWidgetProviderComponentName = validWidgetProviderComponentName;
  }

  private static class WidgetInfo {
    View view;
    int layoutId;
    final AppWidgetProvider appWidgetProvider;
    RemoteViews lastRemoteViews;
    final ComponentName providerComponent;
    AppWidgetProviderInfo info;
    Bundle options = new Bundle();

    public WidgetInfo(
        View view, int layoutId, Context context, AppWidgetProvider appWidgetProvider) {
      this.view = view;
      this.layoutId = layoutId;
      this.appWidgetProvider = appWidgetProvider;
      providerComponent = new ComponentName(context, appWidgetProvider.getClass());
    }

    public WidgetInfo(ComponentName providerComponent) {
      this.providerComponent = providerComponent;
      this.appWidgetProvider = null;
    }
  }

  @ForType(RemoteViews.class)
  interface RemoteViewsReflector {
    RemoteViews getRemoteViewsToApply(Context context);

    boolean hasLandscapeAndPortraitLayouts();
  }
}
