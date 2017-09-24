package org.robolectric.shadows;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowAppWidgetManagerTest {
  private AppWidgetManager appWidgetManager;
  private ShadowAppWidgetManager shadowAppWidgetManager;

  @Before
  public void setUp() throws Exception {
    appWidgetManager = AppWidgetManager.getInstance(RuntimeEnvironment.application);
    shadowAppWidgetManager = shadowOf(appWidgetManager);
  }

  @Test
  public void getInstance_shouldReturnSameInstance() throws Exception {
    assertNotNull(appWidgetManager);
    assertSame(AppWidgetManager.getInstance(RuntimeEnvironment.application), appWidgetManager);
    assertSame(AppWidgetManager.getInstance(new ContextWrapper(RuntimeEnvironment.application)), appWidgetManager);
  }

  @Test
  public void createWidget_shouldInflateViewAndAssignId() throws Exception {
    int widgetId = shadowAppWidgetManager.createWidget(SpanishTestAppWidgetProvider.class, R.layout.main);
    View widgetView = shadowAppWidgetManager.getViewFor(widgetId);

    assertEquals("Hola", ((TextView) widgetView.findViewById(R.id.subtitle)).getText());
  }

  @Test
  public void getViewFor_shouldReturnSameViewEveryTimeForGivenWidgetId() throws Exception {
    int widgetId = shadowAppWidgetManager.createWidget(SpanishTestAppWidgetProvider.class, R.layout.main);
    View widgetView = shadowAppWidgetManager.getViewFor(widgetId);

    assertNotNull(widgetView);
    assertSame(widgetView, shadowAppWidgetManager.getViewFor(widgetId));
  }

  @Test
  public void createWidget_shouldAllowForMultipleInstancesOfWidgets() throws Exception {
    int widgetId = shadowAppWidgetManager.createWidget(SpanishTestAppWidgetProvider.class, R.layout.main);
    View widgetView = shadowAppWidgetManager.getViewFor(widgetId);

    assertNotSame(widgetId,
        shadowAppWidgetManager.createWidget(SpanishTestAppWidgetProvider.class, R.layout.main));
    assertNotSame(widgetView,
        shadowAppWidgetManager.getViewFor(shadowAppWidgetManager.createWidget(SpanishTestAppWidgetProvider.class, R.layout.main)));
  }

  @Test
  public void shouldReplaceLayoutIfAndOnlyIfLayoutIdIsDifferent() throws Exception {
    int widgetId = shadowAppWidgetManager.createWidget(SpanishTestAppWidgetProvider.class, R.layout.main);
    View originalWidgetView = shadowAppWidgetManager.getViewFor(widgetId);
    assertContains("Main Layout", originalWidgetView);

    appWidgetManager.updateAppWidget(widgetId, new RemoteViews(RuntimeEnvironment.application.getPackageName(), R.layout.main));
    assertSame(originalWidgetView, shadowAppWidgetManager.getViewFor(widgetId));

    appWidgetManager.updateAppWidget(widgetId, new RemoteViews(RuntimeEnvironment.application.getPackageName(), R.layout.media));
    assertNotSame(originalWidgetView, shadowAppWidgetManager.getViewFor(widgetId));

    View mediaWidgetView = shadowAppWidgetManager.getViewFor(widgetId);
    assertContains("Media Layout", mediaWidgetView);
  }

  @Test
  public void getAppWidgetIds() {
    int expectedWidgetId = shadowAppWidgetManager.createWidget(SpanishTestAppWidgetProvider.class, R.layout.main);

    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
        new ComponentName(
            SpanishTestAppWidgetProvider.class.getPackage().getName(),
            SpanishTestAppWidgetProvider.class.getName()));

    assertEquals(1, appWidgetIds.length);
    assertEquals(expectedWidgetId, appWidgetIds[0]);
  }

  @Test
  public void getAppWidgetInfo_shouldReturnSpecifiedAppWidgetInfo() throws Exception {
    AppWidgetProviderInfo expectedWidgetInfo = new AppWidgetProviderInfo();
    shadowAppWidgetManager.addBoundWidget(26, expectedWidgetInfo);

    assertEquals(expectedWidgetInfo, appWidgetManager.getAppWidgetInfo(26));
    assertEquals(null, appWidgetManager.getAppWidgetInfo(27));
  }

  @Test
  public void bindAppWidgetIdIfAllowed_shouldReturnThePresetBoolean() throws Exception {
    shadowAppWidgetManager.setAllowedToBindAppWidgets(false);
    assertEquals(shadowAppWidgetManager.bindAppWidgetIdIfAllowed(12345, new ComponentName("", "")), false);
    shadowAppWidgetManager.setAllowedToBindAppWidgets(true);
    assertEquals(shadowAppWidgetManager.bindAppWidgetIdIfAllowed(12345, new ComponentName("", "")), true);
  }

  @Test
  public void bindAppWidgetIdIfAllowed_shouldRecordTheBinding() throws Exception {
    ComponentName provider = new ComponentName("A", "B");
    appWidgetManager.bindAppWidgetIdIfAllowed(789, provider);
    assertArrayEquals(new int[]{789}, appWidgetManager.getAppWidgetIds(provider));
  }

  @Test
  public void bindAppWidgetId_shouldRecordAppWidgetInfo() throws Exception {
    ComponentName provider = new ComponentName("abc", "123");
    AppWidgetProviderInfo providerInfo = new AppWidgetProviderInfo();
    providerInfo.provider = provider;
    shadowAppWidgetManager.addInstalledProvider(providerInfo);

    appWidgetManager.bindAppWidgetIdIfAllowed(90210, provider);

    assertSame(providerInfo, appWidgetManager.getAppWidgetInfo(90210));
  }

  @Test(expected = IllegalArgumentException.class)
  public void bindAppWidgetIdIfAllowed_shouldThrowIllegalArgumentExceptionWhenPrompted() throws Exception {
    shadowAppWidgetManager.setValidWidgetProviderComponentName(false);
    shadowAppWidgetManager.bindAppWidgetIdIfAllowed(12345, new ComponentName("", ""));
  }

  @Test
  public void getInstalledProviders_returnsWidgetList() throws Exception {
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

  private void assertContains(String expectedText, View view) {
    String actualText = shadowOf(view).innerText();
    assertTrue("Expected <" + actualText + "> to contain <" + expectedText + ">", actualText.contains(expectedText));
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
