package com.xtremelabs.droidsugar.view;

import android.app.AlarmManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.LocationManager;
import android.test.mock.MockContentResolver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

@SuppressWarnings({"UnusedDeclaration"})
public class FakeContextWrapper {
    public static ResourceLoader resourceLoader;

    protected static Context contextForInflation = new ContextWrapper(null);
    public List<Intent> startedServices = new ArrayList<Intent>();
    private LocationManager locationManager;

    public Resources getResources() {
        return new Resources(null, null, null);
    }

    public Context getApplicationContext() {
        return new ContextWrapper(null);
    }

    public ContentResolver getContentResolver() {
        return new MockContentResolver();
    }

    public Object getSystemService(String name) {
        if (name.equals(Context.LAYOUT_INFLATER_SERVICE)) {
            return getFakeLayoutInflater();
        } else if (name.equals(Context.ALARM_SERVICE)) {
            return mock(AlarmManager.class);
        } else if (name.equals(Context.LOCATION_SERVICE)) {
            if (locationManager == null) {
                locationManager = mock(LocationManager.class);
            }
            return locationManager;
        }
        return null;
    }

    public FakeLayoutInflater getFakeLayoutInflater() {
        return new FakeLayoutInflater(resourceLoader.viewLoader);
    }

    public ComponentName startService(Intent service) {
        startedServices.add(service);
        return new ComponentName("some.service.package", "SomeServiceName");
    }

    public SharedPreferences getSharedPreferences(String name, int mode) {
        return new TestSharedPreferences(name, mode);
    }

    public static class FakeLayoutInflater extends LayoutInflater {

        private final ViewLoader viewLoader;

        public FakeLayoutInflater(ViewLoader viewLoader) {
            super(null);
            this.viewLoader = viewLoader;
        }

        @Override
        public View inflate(int resource, ViewGroup root, boolean attachToRoot) {
            View view = viewLoader.inflateView(contextForInflation, resource);
            if (root != null && attachToRoot) {
                root.addView(view);
            }
            return view;
        }

        @Override
        public View inflate(int resource, ViewGroup root) {
            return inflate(resource, root, true);
        }

        @Override
        public LayoutInflater cloneInContext(Context context) {
            return this;
        }
    }
}
