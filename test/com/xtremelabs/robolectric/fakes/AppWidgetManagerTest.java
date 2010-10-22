package com.xtremelabs.robolectric.fakes;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.xtremelabs.robolectric.DogfoodRobolectricTestRunner;
import com.xtremelabs.robolectric.ProxyDelegatingHandler;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.res.ResourceLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.*;

@RunWith(DogfoodRobolectricTestRunner.class)
public class AppWidgetManagerTest {
    private AppWidgetManager appWidgetManager;
    private ShadowAppWidgetManager fakeAppWidgetManager;

    @Before
    public void setUp() throws Exception {
        DogfoodRobolectricTestRunner.addGenericProxies();
        Robolectric.application = ShadowApplication.bind(new Application(), new ResourceLoader(R.class, new File("test/res")));
        appWidgetManager = AppWidgetManager.getInstance(Robolectric.application);
        fakeAppWidgetManager = shadowFor(appWidgetManager);
    }

    @Test
    public void getInstance_shouldReturnSameInstance() throws Exception {
        assertNotNull(appWidgetManager);
        assertSame(AppWidgetManager.getInstance(Robolectric.application), appWidgetManager);
        assertSame(AppWidgetManager.getInstance(new ContextWrapper(Robolectric.application)), appWidgetManager);
    }

    @Test
    public void createWidget_shouldInflateViewAndAssignId() throws Exception {
        int widgetId = fakeAppWidgetManager.createWidget(SpanishTestAppWidgetProvider.class, R.layout.main);
        View widgetView = fakeAppWidgetManager.getViewFor(widgetId);

        assertEquals("Hola", ((TextView) widgetView.findViewById(R.id.subtitle)).getText());
    }

    @Test
    public void getViewFor_shouldReturnSameViewEveryTimeForGivenWidgetId() throws Exception {
        int widgetId = fakeAppWidgetManager.createWidget(SpanishTestAppWidgetProvider.class, R.layout.main);
        View widgetView = fakeAppWidgetManager.getViewFor(widgetId);

        assertNotNull(widgetView);
        assertSame(widgetView, fakeAppWidgetManager.getViewFor(widgetId));
    }

    @Test
    public void createWidget_shouldAllowForMultipleInstancesOfWidgets() throws Exception {
        int widgetId = fakeAppWidgetManager.createWidget(SpanishTestAppWidgetProvider.class, R.layout.main);
        View widgetView = fakeAppWidgetManager.getViewFor(widgetId);

        assertNotSame(widgetId,
                fakeAppWidgetManager.createWidget(SpanishTestAppWidgetProvider.class, R.layout.main));
        assertNotSame(widgetView,
                fakeAppWidgetManager.getViewFor(fakeAppWidgetManager.createWidget(SpanishTestAppWidgetProvider.class, R.layout.main)));
    }

    @Test
    public void shouldReplaceLayoutIfAndOnlyIfLayoutIdIsDifferent() throws Exception {
        int widgetId = fakeAppWidgetManager.createWidget(SpanishTestAppWidgetProvider.class, R.layout.main);
        View originalWidgetView = fakeAppWidgetManager.getViewFor(widgetId);
        assertContains("Main Layout", originalWidgetView);

        appWidgetManager.updateAppWidget(widgetId, new RemoteViews("whatevs", R.layout.main));
        assertSame(originalWidgetView, fakeAppWidgetManager.getViewFor(widgetId));

        appWidgetManager.updateAppWidget(widgetId, new RemoteViews("whatevs", R.layout.media));
        assertNotSame(originalWidgetView, fakeAppWidgetManager.getViewFor(widgetId));

        View mediaWidgetView = fakeAppWidgetManager.getViewFor(widgetId);
        assertContains("Media Layout", mediaWidgetView);
    }

    private void assertContains(String expectedText, View view) {
        String actualText = shadowFor(view).innerText();
        assertTrue("Expected <" + actualText + "> to contain <" + expectedText + ">", actualText.contains(expectedText));
    }

    private ShadowView shadowFor(View instance) {
        return (ShadowView) ProxyDelegatingHandler.getInstance().shadowFor(instance);
    }

    private ShadowAppWidgetManager shadowFor(AppWidgetManager instance) {
        return (ShadowAppWidgetManager) ProxyDelegatingHandler.getInstance().shadowFor(instance);
    }

    public static class SpanishTestAppWidgetProvider extends AppWidgetProvider {
        @Override public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
            remoteViews.setTextViewText(R.id.subtitle, "Hola");
            appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        }
    }
}
