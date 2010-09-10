package com.xtremelabs.droidsugar.fakes;

import android.app.AlarmManager;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.LocationManager;
import android.test.mock.MockContentResolver;
import android.test.mock.MockPackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.xtremelabs.droidsugar.util.Implements;
import com.xtremelabs.droidsugar.util.ResourceLoader;
import com.xtremelabs.droidsugar.util.ViewLoader;
import com.xtremelabs.droidsugar.view.TestSharedPreferences;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ContextWrapper.class)
public class FakeContextWrapper {
    public static ResourceLoader resourceLoader;

    protected static Context contextForInflation = new ContextWrapper(null);
    public List<Intent> startedServices = new ArrayList<Intent>();
    private LocationManager locationManager;
    private MockPackageManager packageManager;

    public Resources getResources() {
        return new Resources(null, null, null);
    }

    public Context getApplicationContext() {
        return new ContextWrapper(null);
    }

    public ContentResolver getContentResolver() {
        return new MockContentResolver();
    }

    public PackageManager getPackageManager() {
        if(packageManager == null) {
            packageManager = new MockPackageManager() {
                public PackageInfo packageInfo;
                @Override
                public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
                    if(packageInfo == null) {
                        packageInfo = new PackageInfo();
                        packageInfo.versionName = "1.0";
                    }
                    return packageInfo;
                }
            };
        }
        return packageManager;
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
