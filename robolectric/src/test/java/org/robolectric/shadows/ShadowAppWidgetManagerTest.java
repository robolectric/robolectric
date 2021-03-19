package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.L;
import static android.os.Build.VERSION_CODES.O;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.os.UserHandle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowAppWidgetManagerTest {
  private AppWidgetManager appWidgetManager;
  private ShadowAppWidgetManager shadowAppWidgetManager;

  @Before
  public void setUp() throws Exception {
    appWidgetManager = AppWidgetManager.getInstance(ApplicationProvider.getApplicationContext());
    shadowAppWidgetManager = shadowOf(appWidgetManager);
  }

  @Test
  public void createWidget_shouldInflateViewAndAssignId() {
    int widgetId = shadowAppWidgetManager.createWidget(SpanishTestAppWidgetProvider.class, R.layout.main);
    View widgetView = shadowAppWidgetManager.getViewFor(widgetId);

    assertEquals("Hola", ((TextView) widgetView.findViewById(R.id.subtitle)).getText().toString());
  }

  @Test
  public void getViewFor_shouldReturnSameViewEveryTimeForGivenWidgetId() {
    int widgetId = shadowAppWidgetManager.createWidget(SpanishTestAppWidgetProvider.class, R.layout.main);
    View widgetView = shadowAppWidgetManager.getViewFor(widgetId);

    assertNotNull(widgetView);
    assertSame(widgetView, shadowAppWidgetManager.getViewFor(widgetId));
  }

  @Test
  public void createWidget_shouldAllowForMultipleInstancesOfWidgets() {
    int widgetId = shadowAppWidgetManager.createWidget(SpanishTestAppWidgetProvider.class, R.layout.main);
    View widgetView = shadowAppWidgetManager.getViewFor(widgetId);

    assertNotSame(widgetId,
        shadowAppWidgetManager.createWidget(SpanishTestAppWidgetProvider.class, R.layout.main));
    assertNotSame(widgetView,
        shadowAppWidgetManager.getViewFor(shadowAppWidgetManager.createWidget(SpanishTestAppWidgetProvider.class, R.layout.main)));
  }

  @Test
  public void shouldReplaceLayoutIfAndOnlyIfLayoutIdIsDifferent() {
    int widgetId = shadowAppWidgetManager.createWidget(SpanishTestAppWidgetProvider.class, R.layout.main);
    View originalWidgetView = shadowAppWidgetManager.getViewFor(widgetId);
    assertContains("Main Layout", originalWidgetView);

    appWidgetManager.updateAppWidget(
        widgetId,
        new RemoteViews(
            ApplicationProvider.getApplicationContext().getPackageName(), R.layout.main));
    assertSame(originalWidgetView, shadowAppWidgetManager.getViewFor(widgetId));

    appWidgetManager.updateAppWidget(
        widgetId,
        new RemoteViews(
            ApplicationProvider.getApplicationContext().getPackageName(), R.layout.media));
    assertNotSame(originalWidgetView, shadowAppWidgetManager.getViewFor(widgetId));

    View mediaWidgetView = shadowAppWidgetManager.getViewFor(widgetId);
    assertContains("Media Layout", mediaWidgetView);
  }

  @Test
  public void getAppWidgetIds() {
    int expectedWidgetId = shadowAppWidgetManager.createWidget(SpanishTestAppWidgetProvider.class, R.layout.main);

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
    assertArrayEquals(new int[]{789}, appWidgetManager.getAppWidgetIds(provider));
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

  private void assertContains(String expectedText, View view) {
    String actualText = shadowOf(view).innerText();
    assertTrue(
        "Expected <" + actualText + "> to contain <" + expectedText + ">",
        actualText.contains(expectedText));
  }

  public static class SpanishTestAppWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
      RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
      remoteViews.setTextViewText(R.id.subtitle, "Hola");
      appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }
  }
}
