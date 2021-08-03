package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.L;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Looper.getMainLooper;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowAppWidgetManagerTest {
  private Context context;
  private AppWidgetManager appWidgetManager;
  private ShadowAppWidgetManager shadowAppWidgetManager;

  @Before
  public void setUp() throws Exception {
    context = ApplicationProvider.getApplicationContext();
    appWidgetManager = AppWidgetManager.getInstance(context);
    shadowAppWidgetManager = shadowOf(appWidgetManager);
  }

  @Test
  public void createWidget_shouldInflateViewAndAssignId() {
    int widgetId =
        shadowAppWidgetManager.createWidget(
            SpanishTestAppWidgetProvider.class, R.layout.remote_views);
    View widgetView = shadowAppWidgetManager.getViewFor(widgetId);

    assertEquals("Hola", ((TextView) widgetView.findViewById(R.id.subtitle)).getText().toString());
  }

  @Test
  public void getViewFor_shouldReturnSameViewEveryTimeForGivenWidgetId() {
    int widgetId =
        shadowAppWidgetManager.createWidget(
            SpanishTestAppWidgetProvider.class, R.layout.remote_views);
    View widgetView = shadowAppWidgetManager.getViewFor(widgetId);

    assertNotNull(widgetView);
    assertSame(widgetView, shadowAppWidgetManager.getViewFor(widgetId));
  }

  @Test
  public void createWidget_shouldAllowForMultipleInstancesOfWidgets() {
    int widgetId =
        shadowAppWidgetManager.createWidget(
            SpanishTestAppWidgetProvider.class, R.layout.remote_views);
    View widgetView = shadowAppWidgetManager.getViewFor(widgetId);

    assertNotSame(
        widgetId,
        shadowAppWidgetManager.createWidget(
            SpanishTestAppWidgetProvider.class, R.layout.remote_views));
    assertNotSame(
        widgetView,
        shadowAppWidgetManager.getViewFor(
            shadowAppWidgetManager.createWidget(
                SpanishTestAppWidgetProvider.class, R.layout.remote_views)));
  }

  @Test
  public void shouldReplaceLayoutIfAndOnlyIfLayoutIdIsDifferent() {
    int widgetId =
        shadowAppWidgetManager.createWidget(
            SpanishTestAppWidgetProvider.class, R.layout.remote_views);
    View originalWidgetView = shadowAppWidgetManager.getViewFor(widgetId);
    assertViewId(R.id.remote_views_root, originalWidgetView);

    appWidgetManager.updateAppWidget(
        widgetId,
        new RemoteViews(
            ApplicationProvider.getApplicationContext().getPackageName(), R.layout.remote_views));
    assertSame(originalWidgetView, shadowAppWidgetManager.getViewFor(widgetId));

    appWidgetManager.updateAppWidget(
        widgetId,
        new RemoteViews(
            ApplicationProvider.getApplicationContext().getPackageName(),
            R.layout.remote_views_alt));
    assertNotSame(originalWidgetView, shadowAppWidgetManager.getViewFor(widgetId));

    View altWidgetView = shadowAppWidgetManager.getViewFor(widgetId);
    assertViewId(R.id.remote_views_alt_root, altWidgetView);
  }

  @Test
  public void getAppWidgetIds() {
    int expectedWidgetId =
        shadowAppWidgetManager.createWidget(
            SpanishTestAppWidgetProvider.class, R.layout.remote_views);

    int[] appWidgetIds =
        appWidgetManager.getAppWidgetIds(
            new ComponentName(
                ApplicationProvider.getApplicationContext(),
                SpanishTestAppWidgetProvider.class.getName()));

    assertEquals(1, appWidgetIds.length);
    assertEquals(expectedWidgetId, appWidgetIds[0]);
  }

  @Test
  public void getAppWidgetInfo_shouldReturnSpecifiedAppWidgetInfo() {
    AppWidgetProviderInfo expectedWidgetInfo = new AppWidgetProviderInfo();
    shadowAppWidgetManager.addBoundWidget(26, expectedWidgetInfo);

    assertEquals(expectedWidgetInfo, appWidgetManager.getAppWidgetInfo(26));
    assertNull(appWidgetManager.getAppWidgetInfo(27));
  }

  @Test
  public void bindAppWidgetIdIfAllowed_shouldReturnThePresetBoolean() {
    shadowAppWidgetManager.setAllowedToBindAppWidgets(false);
    assertFalse(shadowAppWidgetManager.bindAppWidgetIdIfAllowed(12345, new ComponentName("", "")));
    shadowAppWidgetManager.setAllowedToBindAppWidgets(true);
    assertTrue(shadowAppWidgetManager.bindAppWidgetIdIfAllowed(12345, new ComponentName("", "")));
  }

  @Test
  public void bindAppWidgetIdIfAllowed_shouldRecordTheBinding() {
    ComponentName provider = new ComponentName("A", "B");
    appWidgetManager.bindAppWidgetIdIfAllowed(789, provider);
    assertArrayEquals(new int[] {789}, appWidgetManager.getAppWidgetIds(provider));
  }

  @Test
  public void bindAppWidgetIdIfAllowed_shouldSetEmptyOptionsBundleIfNotProvided() {
    ComponentName provider = new ComponentName("A", "B");
    appWidgetManager.bindAppWidgetIdIfAllowed(789, provider);
    assertEquals(0, appWidgetManager.getAppWidgetOptions(789).size());
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void bindAppWidgetIdIfAllowed_shouldSetOptionsBundle() {
    ComponentName provider = new ComponentName("A", "B");
    Bundle options = new Bundle();
    options.putString("key", "value");
    appWidgetManager.bindAppWidgetIdIfAllowed(789, provider, options);
    assertEquals("value", appWidgetManager.getAppWidgetOptions(789).getString("key"));
  }

  @Test
  public void bindAppWidgetId_shouldRecordAppWidgetInfo() {
    ComponentName provider = new ComponentName("abc", "123");
    AppWidgetProviderInfo providerInfo = new AppWidgetProviderInfo();
    providerInfo.provider = provider;
    shadowAppWidgetManager.addInstalledProvider(providerInfo);

    appWidgetManager.bindAppWidgetIdIfAllowed(90210, provider);

    assertSame(providerInfo, appWidgetManager.getAppWidgetInfo(90210));
  }

  @Test(expected = IllegalArgumentException.class)
  public void bindAppWidgetIdIfAllowed_shouldThrowIllegalArgumentExceptionWhenPrompted() {
    shadowAppWidgetManager.setValidWidgetProviderComponentName(false);
    shadowAppWidgetManager.bindAppWidgetIdIfAllowed(12345, new ComponentName("", ""));
  }

  @Test
  public void getInstalledProviders_returnsWidgetList() {
    AppWidgetProviderInfo info1 = new AppWidgetProviderInfo();
    info1.label = "abc";
    AppWidgetProviderInfo info2 = new AppWidgetProviderInfo();
    info2.label = "def";
    shadowAppWidgetManager.addInstalledProvider(info1);
    shadowAppWidgetManager.addInstalledProvider(info2);
    List<AppWidgetProviderInfo> installedProviders = appWidgetManager.getInstalledProviders();
    assertEquals(2, installedProviders.size());
    assertEquals(info1, installedProviders.get(0));
    assertEquals(info2, installedProviders.get(1));
  }

  @Test
  public void removeInstalledProviders_returnsWidgetList() {
    AppWidgetProviderInfo info1 = new AppWidgetProviderInfo();
    info1.label = "abc";
    AppWidgetProviderInfo info2 = new AppWidgetProviderInfo();
    info2.label = "def";
    shadowAppWidgetManager.addInstalledProvider(info1);
    shadowAppWidgetManager.addInstalledProvider(info2);
    List<AppWidgetProviderInfo> installedProviders = appWidgetManager.getInstalledProviders();
    assertEquals(2, installedProviders.size());
    assertEquals(info1, installedProviders.get(0));
    assertEquals(info2, installedProviders.get(1));
    assertTrue(shadowAppWidgetManager.removeInstalledProvider(info1));
    installedProviders = appWidgetManager.getInstalledProviders();
    assertEquals(1, installedProviders.size());
    assertEquals(info2, installedProviders.get(0));
  }

  @Test
  public void tryRemoveNotInstalledProviders_noChange() {
    AppWidgetProviderInfo info1 = new AppWidgetProviderInfo();
    info1.label = "abc";
    AppWidgetProviderInfo info2 = new AppWidgetProviderInfo();
    info2.label = "def";
    AppWidgetProviderInfo info3 = new AppWidgetProviderInfo();
    info2.label = "efa";
    shadowAppWidgetManager.addInstalledProvider(info1);
    shadowAppWidgetManager.addInstalledProvider(info2);
    List<AppWidgetProviderInfo> installedProviders = appWidgetManager.getInstalledProviders();
    assertEquals(2, installedProviders.size());
    assertEquals(info1, installedProviders.get(0));
    assertEquals(info2, installedProviders.get(1));
    assertFalse(shadowAppWidgetManager.removeInstalledProvider(info3));
    installedProviders = appWidgetManager.getInstalledProviders();
    assertEquals(2, installedProviders.size());
    assertEquals(info1, installedProviders.get(0));
    assertEquals(info2, installedProviders.get(1));
  }

  @Test
  @Config(minSdk = L)
  public void getInstalledProvidersForProfile_returnsWidgetList() {
    UserHandle userHandle = UserHandle.CURRENT;
    assertTrue(appWidgetManager.getInstalledProvidersForProfile(userHandle).isEmpty());

    AppWidgetProviderInfo info1 = new AppWidgetProviderInfo();
    info1.label = "abc";
    AppWidgetProviderInfo info2 = new AppWidgetProviderInfo();
    info2.label = "def";
    shadowAppWidgetManager.addInstalledProvidersForProfile(userHandle, info1);
    shadowAppWidgetManager.addInstalledProvidersForProfile(userHandle, info2);
    List<AppWidgetProviderInfo> installedProvidersForProfile =
        appWidgetManager.getInstalledProvidersForProfile(userHandle);
    assertEquals(2, installedProvidersForProfile.size());
    assertTrue(installedProvidersForProfile.contains(info1));
    assertTrue(installedProvidersForProfile.contains(info2));
  }

  @Test
  @Config(minSdk = O)
  public void getInstalledProvidersForPackage_returnsWidgetList() {
    UserHandle userHandle = UserHandle.CURRENT;
    String packageName = "com.google.fakeapp";

    assertTrue(appWidgetManager.getInstalledProvidersForPackage(packageName, userHandle).isEmpty());

    AppWidgetProviderInfo info1 = new AppWidgetProviderInfo();
    info1.label = "abc";
    info1.provider = new ComponentName(packageName, "123");
    AppWidgetProviderInfo info2 = new AppWidgetProviderInfo();
    info2.label = "def";
    info2.provider = new ComponentName(packageName, "456");
    shadowAppWidgetManager.addInstalledProvidersForProfile(userHandle, info1);
    shadowAppWidgetManager.addInstalledProvidersForProfile(userHandle, info2);
    List<AppWidgetProviderInfo> installedProvidersForProfile =
        appWidgetManager.getInstalledProvidersForPackage(packageName, userHandle);

    assertEquals(2, installedProvidersForProfile.size());
    assertTrue(installedProvidersForProfile.contains(info1));
    assertTrue(installedProvidersForProfile.contains(info2));
  }

  @Test
  public void updateAppWidget_landscapeAndPortrait() {
    ComponentName provider = new ComponentName(context, SpanishTestAppWidgetProvider.class);
    appWidgetManager.bindAppWidgetIdIfAllowed(789, provider);

    RemoteViews landscape = new RemoteViews(provider.getPackageName(), R.layout.remote_views);
    RemoteViews portrait = new RemoteViews(provider.getPackageName(), R.layout.remote_views_alt);
    RemoteViews combined = new RemoteViews(landscape, portrait);
    appWidgetManager.updateAppWidget(789, combined);

    assertViewId(R.id.remote_views_alt_root, shadowAppWidgetManager.getViewFor(789));
  }

  @Test
  public void updateAppWidget_landscapeAndPortrait_canReapplySameView() {
    ComponentName provider = new ComponentName(context, SpanishTestAppWidgetProvider.class);
    appWidgetManager.bindAppWidgetIdIfAllowed(789, provider);

    RemoteViews landscape = new RemoteViews(provider.getPackageName(), R.layout.remote_views);
    RemoteViews portrait = new RemoteViews(provider.getPackageName(), R.layout.remote_views_alt);
    RemoteViews combined = new RemoteViews(landscape, portrait);
    appWidgetManager.updateAppWidget(789, combined);
    View originalView = shadowAppWidgetManager.getViewFor(789);

    RemoteViews landscape2 = new RemoteViews(provider.getPackageName(), R.layout.remote_views);
    RemoteViews portrait2 = new RemoteViews(provider.getPackageName(), R.layout.remote_views_alt);
    RemoteViews combined2 = new RemoteViews(landscape2, portrait2);
    appWidgetManager.updateAppWidget(789, combined2);

    // A bug around reapplying RemoteViews with landscape and portrait layouts was fixed in API 25.
    if (VERSION.SDK_INT >= 25) {
      assertSame(originalView, shadowAppWidgetManager.getViewFor(789));
    } else {
      assertNotSame(originalView, shadowAppWidgetManager.getViewFor(789));
    }
  }

  @Test
  public void updateAppWidget_landscapeAndPortrait_doesntReapplyDifferntViews() {
    ComponentName provider = new ComponentName(context, SpanishTestAppWidgetProvider.class);
    appWidgetManager.bindAppWidgetIdIfAllowed(789, provider);

    RemoteViews landscape = new RemoteViews(provider.getPackageName(), R.layout.remote_views);
    RemoteViews portrait = new RemoteViews(provider.getPackageName(), R.layout.remote_views_alt);
    RemoteViews combined = new RemoteViews(landscape, portrait);
    appWidgetManager.updateAppWidget(789, combined);
    View originalView = shadowAppWidgetManager.getViewFor(789);

    RemoteViews landscape2 = new RemoteViews(provider.getPackageName(), R.layout.remote_views_alt);
    RemoteViews portrait2 = new RemoteViews(provider.getPackageName(), R.layout.remote_views);
    RemoteViews combined2 = new RemoteViews(landscape2, portrait2);
    appWidgetManager.updateAppWidget(789, combined2);

    assertNotSame(originalView, shadowAppWidgetManager.getViewFor(789));
  }

  @Test
  public void updateAppWidget_invalidViewsInLayout_shouldThrow() {
    ComponentName provider = new ComponentName(context, SpanishTestAppWidgetProvider.class);
    appWidgetManager.bindAppWidgetIdIfAllowed(789, provider);

    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.remote_views_bad);

    assertThrows(RuntimeException.class, () -> appWidgetManager.updateAppWidget(789, remoteViews));
  }

  @Test
  public void updateAppWidgetOptions_shouldSetOptionsBundle() {
    ComponentName provider = new ComponentName("A", "B");
    appWidgetManager.bindAppWidgetIdIfAllowed(789, provider);
    Bundle options = new Bundle();
    options.putString("key", "value");

    appWidgetManager.updateAppWidgetOptions(789, options);

    assertEquals("value", appWidgetManager.getAppWidgetOptions(789).getString("key"));
  }

  @Test
  public void updateAppWidgetOptions_shouldMergeOptionsBundleIfAlreadyExists() {
    ComponentName provider = new ComponentName("A", "B");
    appWidgetManager.bindAppWidgetIdIfAllowed(789, provider);
    Bundle options = new Bundle();
    options.putString("key", "value");
    Bundle newOptions = new Bundle();
    options.putString("key2", "value2");

    appWidgetManager.updateAppWidgetOptions(789, options);
    appWidgetManager.updateAppWidgetOptions(789, newOptions);

    Bundle retrievedOptions = appWidgetManager.getAppWidgetOptions(789);
    assertEquals(2, retrievedOptions.size());
    assertEquals("value", retrievedOptions.getString("key"));
    assertEquals("value2", retrievedOptions.getString("key2"));
  }

  @Test
  public void updateAppWidgetOptions_triggersOnAppWidgetOptionsUpdated() {
    int widgetId =
        shadowAppWidgetManager.createWidget(
            SpanishTestAppWidgetProvider.class, R.layout.remote_views);

    appWidgetManager.updateAppWidgetOptions(widgetId, new Bundle());
    View widgetView = shadowAppWidgetManager.getViewFor(widgetId);

    assertEquals(
        "Actualizar", ((TextView) widgetView.findViewById(R.id.subtitle)).getText().toString());
  }

  @Test
  @Config(minSdk = O)
  public void isRequestPinAppWidgetSupported_shouldReturnThePresetBoolean() {
    shadowAppWidgetManager.setRequestPinAppWidgetSupported(false);
    assertFalse(shadowAppWidgetManager.isRequestPinAppWidgetSupported());
    shadowAppWidgetManager.setRequestPinAppWidgetSupported(true);
    assertTrue(shadowAppWidgetManager.isRequestPinAppWidgetSupported());
  }

  @SuppressWarnings("PendingIntentMutability")
  @Test
  @Config(minSdk = O)
  public void
      requestPinAppWidget_isRequestPinAppWidgetSupportedFalse_shouldNotBindAndReturnFalse() {
    shadowAppWidgetManager.setRequestPinAppWidgetSupported(false);

    String intentAction = "some_action";
    PendingIntent testSuccessIntent =
        PendingIntent.getBroadcast(
            ApplicationProvider.getApplicationContext(), 0, new Intent(intentAction), 0);

    AtomicBoolean successCallbackCalled = new AtomicBoolean(false);
    ApplicationProvider.getApplicationContext()
        .registerReceiver(
            new BroadcastReceiver() {
              @Override
              public void onReceive(Context context, Intent intent) {
                successCallbackCalled.set(true);
              }
            },
            new IntentFilter(intentAction));

    assertFalse(
        shadowAppWidgetManager.requestPinAppWidget(
            new ComponentName("A", "B"), null, testSuccessIntent));
    assertFalse(successCallbackCalled.get());
  }

  @Test
  @Config(minSdk = O)
  public void requestPinAppWidget_isRequestPinAppWidgetSupportedTrue_shouldBindWidget() {
    shadowAppWidgetManager.setRequestPinAppWidgetSupported(true);

    ComponentName provider = new ComponentName("A", "B");

    shadowAppWidgetManager.requestPinAppWidget(provider, null, null);
    shadowOf(getMainLooper()).idle();

    assertEquals(1, shadowAppWidgetManager.getAppWidgetIds(provider).length);
  }

  @SuppressWarnings("PendingIntentMutability")
  @Test
  @Config(minSdk = O)
  public void
      requestPinAppWidget_isRequestPinAppWidgetSupportedTrue_shouldExecuteCallbackWithOriginalIntentAndAppWidgetIdExtra() {
    shadowAppWidgetManager.setRequestPinAppWidgetSupported(true);

    String intentAction = "some_action";
    Intent originalIntent = new Intent(intentAction);
    originalIntent.setPackage(ApplicationProvider.getApplicationContext().getPackageName());
    originalIntent.setComponent(
        new ComponentName(
            ApplicationProvider.getApplicationContext(), ShadowAppWidgetManagerTest.class));
    originalIntent.putExtra("some_extra", "my_value");

    PendingIntent testSuccessIntent =
        PendingIntent.getBroadcast(
            ApplicationProvider.getApplicationContext(), 0, originalIntent, 0);

    AtomicReference<Intent> callbackIntent = new AtomicReference<>();
    ApplicationProvider.getApplicationContext()
        .registerReceiver(
            new BroadcastReceiver() {
              @Override
              public void onReceive(Context context, Intent intent) {
                callbackIntent.set(intent);
              }
            },
            new IntentFilter(intentAction));

    shadowAppWidgetManager.requestPinAppWidget(
        new ComponentName("A", "B"), null, testSuccessIntent);
    shadowOf(getMainLooper()).idle();

    assertNotNull(callbackIntent);

    // Original intent fields still exist.
    assertEquals("my_value", callbackIntent.get().getStringExtra("some_extra"));
    assertEquals(intentAction, callbackIntent.get().getAction());

    // Additionally, the newly created appwidget id is added to the extras.
    assertEquals(1, callbackIntent.get().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1));
  }

  @SuppressWarnings("PendingIntentMutability")
  @Test
  @Config(minSdk = O)
  public void requestPinAppWidget_isRequestPinAppWidgetSupportedTrue_shouldUseUniqueWidgetIds() {
    shadowAppWidgetManager.setRequestPinAppWidgetSupported(true);

    String intentAction = "some_action";
    PendingIntent testSuccessIntent =
        PendingIntent.getBroadcast(
            ApplicationProvider.getApplicationContext(), 0, new Intent(intentAction), 0);

    AtomicInteger callbackAppWidgetId = new AtomicInteger();
    ApplicationProvider.getApplicationContext()
        .registerReceiver(
            new BroadcastReceiver() {
              @Override
              public void onReceive(Context context, Intent intent) {
                callbackAppWidgetId.set(
                    intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1));
              }
            },
            new IntentFilter(intentAction));

    // Create first widget.
    shadowAppWidgetManager.requestPinAppWidget(
        new ComponentName("A", "B"), null, testSuccessIntent);
    shadowOf(getMainLooper()).idle();
    assertEquals(1, callbackAppWidgetId.get());

    // Create a second widget. It should have a different ID than the first.
    shadowAppWidgetManager.requestPinAppWidget(
        new ComponentName("C", "D"), null, testSuccessIntent);
    shadowOf(getMainLooper()).idle();
    assertEquals(2, callbackAppWidgetId.get());
  }

  /**
   * Asserts that the id of {@code view} matches {@code id}. Asserts on the string name, which
   * provides a more useful error message than the int value.
   */
  private void assertViewId(int id, View view) {
    assertEquals(getResourceName(id), getResourceName(view.getId()));
  }

  private String getResourceName(int id) {
    try {
      return context.getResources().getResourceName(id);
    } catch (NotFoundException e) {
      return String.valueOf(id);
    }
  }

  public static class SpanishTestAppWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
      RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.remote_views);
      remoteViews.setTextViewText(R.id.subtitle, "Hola");
      appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    @Override
    public void onAppWidgetOptionsChanged(
        Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
      RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.remote_views);
      remoteViews.setTextViewText(R.id.subtitle, "Actualizar");
      appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }
  }
}
