package com.xtremelabs.droidsugar.fakes;

import android.app.AlarmManager;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.test.mock.MockContentResolver;
import com.xtremelabs.droidsugar.util.FakeHelper;
import com.xtremelabs.droidsugar.util.Implements;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Application.class)
public class FakeApplication extends ContextWrapper {
    private MockContentResolver contentResolver = new MockContentResolver();
    private LocationManager locationManager;
    private WifiManager wifiManager;
    public List<Intent> startedIntents = new ArrayList<Intent>();

    public FakeApplication(Application base) {
        super(base);
    }

    @Override public ContentResolver getContentResolver() {
        return contentResolver;
    }

    @Override public Object getSystemService(String name) {
        if (name.equals(Context.LAYOUT_INFLATER_SERVICE)) {
            return getFakeLayoutInflater();
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

    public FakeContextWrapper.FakeLayoutInflater getFakeLayoutInflater() {
        return new FakeContextWrapper.FakeLayoutInflater(FakeContextWrapper.resourceLoader.viewLoader);
    }

    @Override public void startActivity(Intent intent) {
        startedIntents.add(intent);
    }

    public Intent getNextStartedIntent() {
        if (startedIntents.isEmpty()) {
            return null;
        } else {
            return startedIntents.remove(0);
        }
    }
}
