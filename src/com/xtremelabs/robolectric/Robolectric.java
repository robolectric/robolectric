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
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.xtremelabs.robolectric.shadows.*;
import com.xtremelabs.robolectric.util.Implements;
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

    public static void bindShadowClass(Class<?> realClass, Class<?> shadowClass) {
        ShadowWrangler.getInstance().bindShadowClass(realClass, shadowClass);
    }

    public static void bindShadowClass(Class<?> shadowClass) {
        Implements realClass = shadowClass.getAnnotation(Implements.class);
        bindShadowClass(realClass.value(), shadowClass);
    }

    public static void bindDefaultShadowClasses() {
        bindShadowClasses(getDefaultShadowClasses());
    }

    public static void bindShadowClasses(List<Class<?>> shadowClasses) {
        for (Class<?> shadowClass : shadowClasses) {
            bindShadowClass(shadowClass);
        }
    }

    public static List<Class<?>> getDefaultShadowClasses() {
        return Arrays.asList(
                ShadowAbsSpinner.class,
                ShadowActivity.class,
                ShadowAdapterView.class,
                ShadowAddress.class,
                ShadowAlertDialog.class,
                ShadowAlertDialog.ShadowBuilder.class,
                ShadowApplication.class,
                ShadowAppWidgetManager.class,
                ShadowAudioManager.class,
                ShadowBaseAdapter.class,
                ShadowBitmapDrawable.class,
                ShadowBundle.class,
                ShadowCanvas.class,
                ShadowCompoundButton.class,
                ShadowComponentName.class,
                ShadowContext.class,
                ShadowContextWrapper.class,
                ShadowContextThemeWrapper.class,
                ShadowDisplay.class,
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
                ShadowPreferenceManager.class,
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

    public static <T> T directlyOn(T shadowedObject) {
        return RobolectricInternals.directlyOn(shadowedObject);
    }

    public static ShadowDrawable shadowOf(Drawable instance) {
        return (ShadowDrawable) shadowOf_(instance);
    }

    public static ShadowToast shadowOf(Toast instance) {
        return (ShadowToast) shadowOf_(instance);
    }

    public static ShadowBitmapDrawable shadowOf(BitmapDrawable instance) {
        return (ShadowBitmapDrawable) shadowOf_(instance);
    }

    public static ShadowZoomButtonsController shadowOf(ZoomButtonsController instance) {
        return (ShadowZoomButtonsController) shadowOf_(instance);
    }

    public static ShadowGeoPoint shadowOf(GeoPoint instance) {
        return (ShadowGeoPoint) shadowOf_(instance);
    }

    public static ShadowMapView shadowOf(MapView instance) {
        return (ShadowMapView) shadowOf_(instance);
    }

    public static ShadowMapController shadowOf(MapController instance) {
        return (ShadowMapController) shadowOf_(instance);
    }

    public static ShadowItemizedOverlay shadowOf(ItemizedOverlay instance) {
        return (ShadowItemizedOverlay) shadowOf_(instance);
    }

    public static ShadowListView shadowOf(ListView instance) {
        return (ShadowListView) shadowOf_(instance);
    }

    public static ShadowActivity shadowOf(Activity instance) {
        return (ShadowActivity) shadowOf_(instance);
    }

    public static ShadowContextWrapper shadowOf(ContextWrapper instance) {
        return (ShadowContextWrapper) shadowOf_(instance);
    }

    public static ShadowContextWrapper shadowOf(Context instance) {
        return (ShadowContextWrapper) shadowOf_(instance);
    }

    public static ShadowPaint shadowOf(Paint instance) {
        return (ShadowPaint) shadowOf_(instance);
    }

    public static ShadowPath shadowOf(Path instance) {
        return (ShadowPath) shadowOf_(instance);
    }

    public static ShadowListActivity shadowOf(ListActivity instance) {
        return (ShadowListActivity) shadowOf_(instance);
    }

    public static ShadowHandler shadowOf(Handler instance) {
        return (ShadowHandler) shadowOf_(instance);
    }

    public static ShadowIntent shadowOf(Intent instance) {
        return (ShadowIntent) shadowOf_(instance);
    }

    public static ShadowView shadowOf(View instance) {
        return (ShadowView) shadowOf_(instance);
    }

    public static ShadowViewGroup shadowOf(ViewGroup instance) {
        return (ShadowViewGroup) shadowOf_(instance);
    }

    public static ShadowAdapterView shadowOf(AdapterView instance) {
        return (ShadowAdapterView) shadowOf_(instance);
    }

    public static ShadowTextView shadowOf(TextView instance) {
        return (ShadowTextView) shadowOf_(instance);
    }

    public static ShadowImageView shadowOf(ImageView instance) {
        return (ShadowImageView) shadowOf_(instance);
    }

    public static ShadowRemoteViews shadowOf(RemoteViews instance) {
        return (ShadowRemoteViews) shadowOf_(instance);
    }

    public static ShadowDialog shadowOf(Dialog instance) {
        return (ShadowDialog) shadowOf_(instance);
    }

    public static ShadowAlertDialog shadowOf(AlertDialog instance) {
        return (ShadowAlertDialog) shadowOf_(instance);
    }

    public static ShadowLooper shadowOf(Looper instance) {
        return (ShadowLooper) shadowOf_(instance);
    }

    public static ShadowCanvas shadowOf(Canvas instance) {
        return (ShadowCanvas) shadowOf_(instance);
    }

    public static ShadowLocationManager shadowOf(LocationManager instance) {
        return (ShadowLocationManager) shadowOf_(instance);
    }

    public static ShadowAppWidgetManager shadowOf(AppWidgetManager instance) {
        return (ShadowAppWidgetManager) shadowOf_(instance);
    }

    public static ShadowResources shadowOf(Resources instance) {
        return (ShadowResources) shadowOf_(instance);
    }

    public static ShadowLayoutInflater shadowOf(LayoutInflater instance) {
        return ((ShadowLayoutInflater) shadowOf_(instance));
    }
    
	public static ShadowDisplay shadowOf(Display instance) {
		return ((ShadowDisplay) shadowOf_(instance));
	}
	
	public static ShadowAudioManager shadowOf(AudioManager instance) {
		return ((ShadowAudioManager) shadowOf_(instance));
	}

    @SuppressWarnings({"unchecked"})
    public static <P, R> P shadowOf_(R instance) {
        return (P) ShadowWrangler.getInstance().shadowOf(instance);
    }

}
