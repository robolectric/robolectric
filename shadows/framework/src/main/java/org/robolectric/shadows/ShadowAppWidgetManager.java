package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RemoteViews;
import com.android.internal.appwidget.IAppWidgetService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AppWidgetManager.class)
public class ShadowAppWidgetManager {

  @RealObject
  private AppWidgetManager realAppWidgetManager;

  private Context context;
  private final Map<Integer, WidgetInfo> widgetInfos = new HashMap<>();
  private int nextWidgetId = 1;
  private boolean alwaysRecreateViewsDuringUpdate = false;
  private boolean allowedToBindWidgets;
  private boolean validWidgetProviderComponentName = true;
  private final ArrayList<AppWidgetProviderInfo> installedProviders = new ArrayList<>();

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
    int layoutId = views.getLayoutId();
    if (widgetInfo.layoutId != layoutId || alwaysRecreateViewsDuringUpdate) {
      widgetInfo.view = createWidgetView(layoutId);
      widgetInfo.layoutId = layoutId;
    }
    widgetInfo.lastRemoteViews = views;
    views.reapply(context, widgetInfo.view);
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

  public void addInstalledProvider(AppWidgetProviderInfo appWidgetProviderInfo) {
    installedProviders.add(appWidgetProviderInfo);
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

  @HiddenApi @Implementation
  public void bindAppWidgetId(int appWidgetId, ComponentName provider) {
    WidgetInfo widgetInfo = new WidgetInfo(provider);
    widgetInfos.put(appWidgetId, widgetInfo);
    for (AppWidgetProviderInfo appWidgetProviderInfo : installedProviders) {
      if (provider != null && provider.equals(appWidgetProviderInfo.provider)) {
        widgetInfo.info = appWidgetProviderInfo;
      }
    }
  }

  @Implementation
  protected boolean bindAppWidgetIdIfAllowed(int appWidgetId, ComponentName provider) {
    if (validWidgetProviderComponentName) {
      bindAppWidgetId(appWidgetId, provider);
      return allowedToBindWidgets;
    } else {
      throw new IllegalArgumentException("not an appwidget provider");
    }
  }

  /**
   * Triggers a reapplication of the most recent set of actions against the widget, which is what happens when the
   * phone is rotated. Does not attempt to simulate a change in screen geometry.
   *
   * @param appWidgetId the ID of the widget to be affected
   */
  public void reconstructWidgetViewAsIfPhoneWasRotated(int appWidgetId) {
    WidgetInfo widgetInfo = widgetInfos.get(appWidgetId);
    widgetInfo.view = createWidgetView(widgetInfo.layoutId);
    widgetInfo.lastRemoteViews.reapply(context, widgetInfo.view);
  }

  /**
   * Creates a widget by inflating its layout.
   *
   * @param appWidgetProviderClass the app widget provider class
   * @param widgetLayoutId         id of the layout to inflate
   * @return the ID of the new widget
   */
  public int createWidget(Class<? extends AppWidgetProvider> appWidgetProviderClass, int widgetLayoutId) {
    return createWidgets(appWidgetProviderClass, widgetLayoutId, 1)[0];
  }

  /**
   * Creates a bunch of widgets by inflating the same layout multiple times.
   *
   * @param appWidgetProviderClass the app widget provider class
   * @param widgetLayoutId         id of the layout to inflate
   * @param howManyToCreate        number of new widgets to create
   * @return the IDs of the new widgets
   */
  public int[] createWidgets(Class<? extends AppWidgetProvider> appWidgetProviderClass, int widgetLayoutId, int howManyToCreate) {
    AppWidgetProvider appWidgetProvider = ReflectionHelpers.callConstructor(appWidgetProviderClass);

    int[] newWidgetIds = new int[howManyToCreate];
    for (int i = 0; i < howManyToCreate; i++) {
      View widgetView = createWidgetView(widgetLayoutId);

      int myWidgetId = nextWidgetId++;
      widgetInfos.put(myWidgetId, new WidgetInfo(widgetView, widgetLayoutId, appWidgetProvider));
      newWidgetIds[i] = myWidgetId;
    }

    appWidgetProvider.onUpdate(context, realAppWidgetManager, newWidgetIds);
    return newWidgetIds;
  }

  private View createWidgetView(int widgetLayoutId) {
    return LayoutInflater.from(RuntimeEnvironment.application).inflate(widgetLayoutId, null);
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
   * Enables testing of widget behavior when all of the views are recreated on every
   * update. This is useful for ensuring that your widget will behave correctly even
   * if it is restarted by the OS between events.
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

    public WidgetInfo(View view, int layoutId, AppWidgetProvider appWidgetProvider) {
      this.view = view;
      this.layoutId = layoutId;
      this.appWidgetProvider = appWidgetProvider;
      String packageName = appWidgetProvider.getClass().getPackage().getName();
      String className = appWidgetProvider.getClass().getName();
      providerComponent = new ComponentName(packageName, className);
    }

    public WidgetInfo(ComponentName providerComponent) {
      this.providerComponent = providerComponent;
      this.appWidgetProvider = null;
    }
  }
}
