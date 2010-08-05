package com.xtremelabs.droidsugar.util;

import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsSpinner;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.xtremelabs.droidsugar.fakes.FakeAbsSpinner;
import com.xtremelabs.droidsugar.fakes.FakeActivity;
import com.xtremelabs.droidsugar.fakes.FakeAdapterView;
import com.xtremelabs.droidsugar.fakes.FakeContextWrapper;
import com.xtremelabs.droidsugar.fakes.FakeGeoPoint;
import com.xtremelabs.droidsugar.fakes.FakeHandler;
import com.xtremelabs.droidsugar.fakes.FakeImageView;
import com.xtremelabs.droidsugar.fakes.FakeIntent;
import com.xtremelabs.droidsugar.fakes.FakeItemizedOverlay;
import com.xtremelabs.droidsugar.fakes.FakeLayoutParams;
import com.xtremelabs.droidsugar.fakes.FakeListView;
import com.xtremelabs.droidsugar.fakes.FakeLooper;
import com.xtremelabs.droidsugar.fakes.FakeMapActivity;
import com.xtremelabs.droidsugar.fakes.FakeMapController;
import com.xtremelabs.droidsugar.fakes.FakeMapView;
import com.xtremelabs.droidsugar.fakes.FakeOverlayItem;
import com.xtremelabs.droidsugar.fakes.FakeTextView;
import com.xtremelabs.droidsugar.fakes.FakeView;
import com.xtremelabs.droidsugar.fakes.FakeViewGroup;

import java.util.HashMap;
import java.util.Map;

public class FakesEnvironment {
    public static Map<Class, Class> getGenericProxies() {
        HashMap<Class, Class> map = new HashMap<Class, Class>();

        map.put(AbsSpinner.class, FakeAbsSpinner.class);
        map.put(Activity.class, FakeActivity.class);
        map.put(AdapterView.class, FakeAdapterView.class);
        map.put(ContextWrapper.class, FakeContextWrapper.class);
        map.put(ImageView.class, FakeImageView.class);
        map.put(GeoPoint.class, FakeGeoPoint.class);
        map.put(Handler.class, FakeHandler.class);
        map.put(Intent.class, FakeIntent.class);
        map.put(ItemizedOverlay.class, FakeItemizedOverlay.class);
        map.put(ListView.class, FakeListView.class);
        map.put(Looper.class, FakeLooper.class);
        map.put(MapController.class, FakeMapController.class);
        map.put(MapActivity.class, FakeMapActivity.class);
        map.put(MapView.class, FakeMapView.class);
        map.put(OverlayItem.class, FakeOverlayItem.class);
        map.put(TextView.class, FakeTextView.class);
        map.put(View.class, FakeView.class);
        map.put(ViewGroup.class, FakeViewGroup.class);
        map.put(ViewGroup.LayoutParams.class, FakeLayoutParams.class);

        return map;
    }
}
