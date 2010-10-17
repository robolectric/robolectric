package com.xtremelabs.robolectric;

import android.app.Application;
import com.xtremelabs.robolectric.fakes.*;
import com.xtremelabs.robolectric.view.TestSharedPreferences;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public class Robolectric {
    public static Application application;

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
                FakeAddress.class,
                FakeAlertDialog.class,
                FakeAlertDialog.FakeBuilder.class,
                FakeApplication.class,
                FakeAppWidgetManager.class,
                FakeBaseAdapter.class,
                FakeBitmapDrawable.class,
                FakeCanvas.class,
                FakeCompoundButton.class,
                FakeComponentName.class,
                FakeContext.class,
                FakeContextWrapper.class,
                FakeDrawable.class,
                FakeDialog.class,
                FakeEditText.class,
                FakeGeoPoint.class,
                FakeHandler.class,
                FakeImageView.class,
                FakeIntent.class,
                FakeIntentFilter.class,
                FakeItemizedOverlay.class,
                FakeLayoutInflater.class,
                FakeLayoutParams.class,
                FakeListActivity.class,
                FakeListView.class,
                FakeLocation.class,
                FakeLocationManager.class,
                FakeLooper.class,
                FakeMapController.class,
                FakeMapActivity.class,
                FakeMapView.class,
                FakeMotionEvent.class,
                FakeOverlayItem.class,
                FakePaint.class,
                FakePath.class,
                FakePendingIntent.class,
                FakePoint.class,
                FakeRect.class,
                FakeRemoteViews.class,
                FakeResources.class,
                FakeService.class,
                FakeSettings.class,
                FakeSettings.FakeSecure.class,
                FakeSettings.FakeSystem.class,
                FakeSpannableStringBuilder.class,
                FakeTextUtils.class,
                FakeTextView.class,
                FakeToast.class,
                FakeTypedValue.class,
                FakeView.class,
                FakeViewGroup.class,
                FakeWifiManager.class,
                FakeZoomButtonsController.class
                );
    }

    public static void resetStaticState() {
        Robolectric.application = new Application();
        TestSharedPreferences.reset();
        FakeToast.reset();
        FakeAlertDialog.reset();
        FakeDialog.reset();
        FakeLooper.resetAll();
    }
}
