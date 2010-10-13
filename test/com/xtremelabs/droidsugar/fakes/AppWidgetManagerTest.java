package com.xtremelabs.droidsugar.fakes;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import com.xtremelabs.droidsugar.ProxyDelegatingHandler;
import com.xtremelabs.droidsugar.R;
import com.xtremelabs.droidsugar.res.ResourceLoader;
import com.xtremelabs.droidsugar.util.FakeHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.*;

@RunWith(DroidSugarAndroidTestRunner.class)
public class AppWidgetManagerTest {
    private AppWidgetManager appWidgetManager;
    private FakeAppWidgetManager fakeAppWidgetManager;

    @Before
    public void setUp() throws Exception {
        DroidSugarAndroidTestRunner.addGenericProxies();
        FakeHelper.application = new Application();
        FakeContextWrapper.resourceLoader = new ResourceLoader(R.class, new File("test/res"));
        appWidgetManager = AppWidgetManager.getInstance(FakeHelper.application);
        fakeAppWidgetManager = proxyFor(appWidgetManager);
    }

    @Test
    public void getInstance_shouldReturnSameInstance() throws Exception {
        assertNotNull(appWidgetManager);
        assertSame(AppWidgetManager.getInstance(FakeHelper.application), appWidgetManager);
        assertSame(AppWidgetManager.getInstance(new ContextWrapper(FakeHelper.application)), appWidgetManager);
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

    private FakeAppWidgetManager proxyFor(AppWidgetManager instance) {
        return (FakeAppWidgetManager) ProxyDelegatingHandler.getInstance().proxyFor(instance);
    }

    public static class SpanishTestAppWidgetProvider extends AppWidgetProvider {
        @Override public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
            remoteViews.setTextViewText(R.id.subtitle, "Hola");
            appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        }
    }
}
