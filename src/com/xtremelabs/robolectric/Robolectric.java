package com.xtremelabs.robolectric;

import android.app.*;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.xtremelabs.robolectric.shadows.*;
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
                ShadowAbsSpinner.class,
                ShadowActivity.class,
                ShadowAdapterView.class,
                ShadowAddress.class,
                ShadowAlertDialog.class,
                ShadowAlertDialog.ShadowBuilder.class,
                ShadowApplication.class,
                ShadowAppWidgetManager.class,
                ShadowBaseAdapter.class,
                ShadowBitmapDrawable.class,
                ShadowCanvas.class,
                ShadowCompoundButton.class,
                ShadowComponentName.class,
                ShadowContext.class,
                ShadowContextWrapper.class,
                ShadowDrawable.class,
                ShadowDialog.class,
                ShadowEditText.class,
                ShadowGeoPoint.class,
                ShadowHandler.class,
                ShadowImageView.class,
                ShadowIntent.class,
                ShadowIntentFilter.class,
                ShadowItemizedOverlay.class,
                ShadowLayoutInflater.class,
                ShadowLayoutParams.class,
                ShadowListActivity.class,
                ShadowListView.class,
                ShadowLocation.class,
                ShadowLocationManager.class,
                ShadowLooper.class,
                ShadowMapController.class,
                ShadowMapActivity.class,
                ShadowMapView.class,
                ShadowMotionEvent.class,
                ShadowOverlayItem.class,
                ShadowPaint.class,
                ShadowPath.class,
                ShadowPendingIntent.class,
                ShadowPoint.class,
                ShadowRect.class,
                ShadowRemoteViews.class,
                ShadowResources.class,
                ShadowService.class,
                ShadowSettings.class,
                ShadowSettings.ShadowSecure.class,
                ShadowSettings.ShadowSystem.class,
                ShadowSpannableStringBuilder.class,
                ShadowTextUtils.class,
                ShadowTextView.class,
                ShadowToast.class,
                ShadowTypedValue.class,
                ShadowView.class,
                ShadowViewGroup.class,
                ShadowWifiManager.class,
                ShadowZoomButtonsController.class
                );
    }

    public static void resetStaticState() {
        Robolectric.application = new Application();
        TestSharedPreferences.reset();
        ShadowToast.reset();
        ShadowAlertDialog.reset();
        ShadowDialog.reset();
        ShadowLooper.resetAll();
    }

    public static ShadowDrawable shadowFor(Drawable instance) {
        return (ShadowDrawable) shadowFor_(instance);
    }

    public static ShadowToast shadowFor(Toast instance) {
        return (ShadowToast) shadowFor_(instance);
    }

    public static ShadowBitmapDrawable shadowFor(BitmapDrawable instance) {
        return (ShadowBitmapDrawable) shadowFor_(instance);
    }

    public static ShadowZoomButtonsController shadowFor(ZoomButtonsController instance) {
        return (ShadowZoomButtonsController) shadowFor_(instance);
    }

    public static ShadowGeoPoint shadowFor(GeoPoint instance) {
        return (ShadowGeoPoint) shadowFor_(instance);
    }

    public static ShadowMapView shadowFor(MapView instance) {
        return (ShadowMapView) shadowFor_(instance);
    }

    public static ShadowMapController shadowFor(MapController instance) {
        return (ShadowMapController) shadowFor_(instance);
    }

    public static ShadowItemizedOverlay shadowFor(ItemizedOverlay instance) {
        return (ShadowItemizedOverlay) shadowFor_(instance);
    }

    public static ShadowListView shadowFor(ListView instance) {
        return (ShadowListView) shadowFor_(instance);
    }

    public static ShadowActivity shadowFor(Activity instance) {
        return (ShadowActivity) shadowFor_(instance);
    }

    public static ShadowContextWrapper shadowFor(ContextWrapper instance) {
        return (ShadowContextWrapper) shadowFor_(instance);
    }

    public static ShadowContextWrapper shadowFor(Context instance) {
        return (ShadowContextWrapper) shadowFor_(instance);
    }

    public static ShadowPaint shadowFor(Paint instance) {
        return (ShadowPaint) shadowFor_(instance);
    }

    public static ShadowPath shadowFor(Path instance) {
        return (ShadowPath) shadowFor_(instance);
    }

    public static ShadowListActivity shadowFor(ListActivity instance) {
        return (ShadowListActivity) shadowFor_(instance);
    }

    public static ShadowHandler shadowFor(Handler instance) {
        return (ShadowHandler) shadowFor_(instance);
    }

    public static ShadowIntent shadowFor(Intent instance) {
        return (ShadowIntent) shadowFor_(instance);
    }

    public static ShadowView shadowFor(View instance) {
        return (ShadowView) shadowFor_(instance);
    }

    public static ShadowViewGroup shadowFor(ViewGroup instance) {
        return (ShadowViewGroup) shadowFor_(instance);
    }

    public static ShadowAdapterView shadowFor(AdapterView instance) {
        return (ShadowAdapterView) shadowFor_(instance);
    }

    public static ShadowTextView shadowFor(TextView instance) {
        return (ShadowTextView) shadowFor_(instance);
    }

    public static ShadowImageView shadowFor(ImageView instance) {
        return (ShadowImageView) shadowFor_(instance);
    }

    public static ShadowRemoteViews shadowFor(RemoteViews instance) {
        return (ShadowRemoteViews) shadowFor_(instance);
    }

    public static ShadowDialog shadowFor(Dialog instance) {
        return (ShadowDialog) shadowFor_(instance);
    }

    public static ShadowAlertDialog shadowFor(AlertDialog instance) {
        return (ShadowAlertDialog) shadowFor_(instance);
    }

    public static ShadowLooper shadowFor(Looper instance) {
        return (ShadowLooper) shadowFor_(instance);
    }

    public static ShadowCanvas shadowFor(Canvas instance) {
        return (ShadowCanvas) shadowFor_(instance);
    }

    public static ShadowLocationManager shadowFor(LocationManager instance) {
        return (ShadowLocationManager) shadowFor_(instance);
    }

    public static ShadowAppWidgetManager shadowFor(AppWidgetManager instance) {
        return (ShadowAppWidgetManager) shadowFor_(instance);
    }

    public static ShadowResources shadowFor(Resources instance) {
        return (ShadowResources) shadowFor_(instance);
    }

    @SuppressWarnings({"unchecked"})
    public static <P, R> P shadowFor_(R instance) {
        return (P) ProxyDelegatingHandler.getInstance().shadowFor(instance);
    }

    public static void addProxy(Class<?> realClass, Class<?> handlerClass) {
        ProxyDelegatingHandler.getInstance().addProxyClass(realClass, handlerClass);
    }
}
