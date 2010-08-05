package com.xtremelabs.droidsugar.util;

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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public class FakeHelper {
    public static <T> T newInstanceOf(Class<T> clazz) {
        try {
            Constructor<T> defaultConstructor = clazz.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            return defaultConstructor.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Class<?>> getGenericProxies() {
        return Arrays.asList(
                FakeAbsSpinner.class,
                FakeActivity.class,
                FakeAdapterView.class,
                FakeContextWrapper.class,
                FakeImageView.class,
                FakeGeoPoint.class,
                FakeHandler.class,
                FakeIntent.class,
                FakeItemizedOverlay.class,
                FakeListView.class,
                FakeLooper.class,
                FakeMapController.class,
                FakeMapActivity.class,
                FakeMapView.class,
                FakeOverlayItem.class,
                FakeTextView.class,
                FakeView.class,
                FakeViewGroup.class,
                FakeLayoutParams.class
        );
    }
}
