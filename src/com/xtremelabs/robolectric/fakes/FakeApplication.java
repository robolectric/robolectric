package com.xtremelabs.robolectric.fakes;

import android.app.AlarmManager;
import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.test.mock.MockContentResolver;
import android.view.LayoutInflater;
import com.xtremelabs.robolectric.ProxyDelegatingHandler;
import com.xtremelabs.robolectric.res.ResourceLoader;
import com.xtremelabs.robolectric.util.FakeHelper;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Application.class)
public class FakeApplication extends FakeContextWrapper {
    public static Application bind(Application application, ResourceLoader resourceLoader) {
        FakeApplication fakeApplication = (FakeApplication) ProxyDelegatingHandler.getInstance().proxyFor(application);
        if (fakeApplication.resourceLoader != null) throw new RuntimeException("ResourceLoader already set!");
        fakeApplication.resourceLoader = resourceLoader;
        return application;
    }

    private Application realApplication;
    private ResourceLoader resourceLoader;
    private MockContentResolver contentResolver = new MockContentResolver();
    private LocationManager locationManager;
    private WifiManager wifiManager;
    private List<Intent> startedActivities = new ArrayList<Intent>();
    private List<Intent> startedServices = new ArrayList<Intent>();

    // these are managed by the AppSingletonizier... kinda gross, sorry [xw]
    public LayoutInflater layoutInflater;
    public AppWidgetManager appWidgetManager;

    public FakeApplication(Application realApplication) {
        super(realApplication);
        this.realApplication = realApplication;
    }

    @Implementation
    public Context getApplicationContext() {
        return realApplication;
    }

    @Implementation
    public Resources getResources() {
        return FakeResources.bind(new Resources(null, null, null), resourceLoader);
    }

    @Implementation
    @Override public ContentResolver getContentResolver() {
        return contentResolver;
    }

    @Implementation
    @Override public Object getSystemService(String name) {
        if (name.equals(Context.LAYOUT_INFLATER_SERVICE)) {
            return LayoutInflater.from(realApplication);
        } else if (name.equals(Context.ALARM_SERVICE)) {
            return mock(AlarmManager.class);
        } else if (name.equals(Context.LOCATION_SERVICE)) {
            if (locationManager == null) {
                locationManager = mock(LocationManager.class);
            }
            return locationManager;
        } else if (name.equals(Context.WIFI_SERVICE)) {
            if (wifiManager == null) {
                wifiManager = FakeHelper.newInstanceOf(WifiManager.class);
            }
            return wifiManager;
        }
        return null;
    }

    @Implementation
    @Override public void startActivity(Intent intent) {
        startedActivities.add(intent);
    }

    @Implementation
    @Override public ComponentName startService(Intent intent) {
        startedServices.add(intent);
        return new ComponentName("some.service.package", "SomeServiceName-FIXME");
    }

    @Override public Intent getNextStartedActivity() {
        if (startedActivities.isEmpty()) {
            return null;
        } else {
            return startedActivities.remove(0);
        }
    }

    public Intent peekNextStartedActivity() {
        if (startedActivities.isEmpty()) {
            return null;
        } else {
            return startedActivities.get(0);
        }
    }

    @Override public Intent getNextStartedService() {
        if (startedServices.isEmpty()) {
            return null;
        } else {
            return startedServices.remove(0);
        }
    }

    public Intent peekNextStartedService() {
        if (startedServices.isEmpty()) {
            return null;
        } else {
            return startedServices.get(0);
        }
    }

    ResourceLoader getResourceLoader() {
        return resourceLoader;
    }
}
