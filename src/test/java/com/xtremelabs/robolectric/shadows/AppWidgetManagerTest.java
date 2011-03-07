package com.xtremelabs.robolectric.shadows;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class AppWidgetManagerTest {
    private AppWidgetManager appWidgetManager;
    private ShadowAppWidgetManager shadowAppWidgetManager;

    @Before
    public void setUp() throws Exception {
        appWidgetManager = AppWidgetManager.getInstance(Robolectric.application);
        shadowAppWidgetManager = shadowOf(appWidgetManager);
    }

    @Test
    public void getInstance_shouldReturnSameInstance() throws Exception {
        assertNotNull(appWidgetManager);
        assertSame(AppWidgetManager.getInstance(Robolectric.application), appWidgetManager);
        assertSame(AppWidgetManager.getInstance(new ContextWrapper(Robolectric.application)), appWidgetManager);
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

        appWidgetManager.updateAppWidget(widgetId, new RemoteViews("whatevs", R.layout.main));
        assertSame(originalWidgetView, shadowAppWidgetManager.getViewFor(widgetId));

        appWidgetManager.updateAppWidget(widgetId, new RemoteViews("whatevs", R.layout.media));
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

    private void assertContains(String expectedText, View view) {
        String actualText = shadowOf(view).innerText();
        assertTrue("Expected <" + actualText + "> to contain <" + expectedText + ">", actualText.contains(expectedText));
    }

    public static class SpanishTestAppWidgetProvider extends AppWidgetProvider {
        @Override public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
            remoteViews.setTextViewText(R.id.subtitle, "Hola");
            appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        }
    }
}
