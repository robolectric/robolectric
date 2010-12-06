package com.xtremelabs.robolectric;

import android.app.*;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.*;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.xtremelabs.robolectric.shadows.*;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.Scheduler;
import com.xtremelabs.robolectric.view.TestSharedPreferences;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public class Robolectric {
    public static Application application;
    public static Scheduler backgroundScheduler;
    public static Scheduler uiThreadScheduler;

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
        if (realClass == null) {
            throw new IllegalArgumentException(shadowClass + " is not annotated with @Implements");
        }

        try {
            bindShadowClass(realClass.value(), shadowClass);
        } catch (TypeNotPresentException ignored) {
            //this allows users of the robolectric.jar file to use the non-Google APIs version of the api
        }
    }

    public static void bindDefaultShadowClasses() {
        bindShadowClasses(getDefaultShadowClasses());
    }

    public static void bindShadowClasses(List<Class<?>> shadowClasses) {
        for (Class<?> shadowClass : shadowClasses) {
            bindShadowClass(shadowClass);
        }
    }

    /**
     * Invoke this utility method in tests to reveal which Android api classes and methods are being invoked
     * for which there are no shadows or shadow methods. This helps expose which methods are being invoked
     * either by a third party library or application code which need new shadow methods to be written. Generates
     * output for the current test only.
     */
    public static void logMissingInvokedShadowMethods() {
        ShadowWrangler.getInstance().logMissingInvokedShadowMethods();
    }

    /**
     * Calls {@code performClick()} on a {@code View} after ensuring that it and its ancestors are visible and that it
     * is enabled.
     *
     * @param view the view to click on
     * @return true if {@code View.OnClickListener}s were found and fired, false otherwise.
     * @throws RuntimeException if the preconditions are not met.
     */
    public static boolean clickOn(View view) {
        return shadowOf(view).checkedPerformClick();
    }

    public static List<Class<?>> getDefaultShadowClasses() {
        return Arrays.asList(
                ShadowAbsoluteLayout.class,
                ShadowAbsSpinner.class,
                ShadowAbstractCursor.class,
                ShadowActivity.class,
                ShadowAdapterView.class,
                ShadowAddress.class,
                ShadowAlertDialog.class,
                ShadowAlertDialog.ShadowBuilder.class,
                ShadowApplication.class,
                ShadowAppWidgetManager.class,
                ShadowAsyncTask.class,
                ShadowAudioManager.class,
                ShadowBaseAdapter.class,
                ShadowBitmap.class,
                ShadowBitmapDrawable.class,
                ShadowBitmapFactory.class,
                ShadowBluetoothAdapter.class,
                ShadowBluetoothDevice.class,
                ShadowBundle.class,
                ShadowCanvas.class,
                ShadowCompoundButton.class,
                ShadowComponentName.class,
                ShadowConnectivityManager.class,
                ShadowContentValues.class,
                ShadowContext.class,
                ShadowContextWrapper.class,
                ShadowContextThemeWrapper.class,
                ShadowDisplay.class,
                ShadowDrawable.class,
                ShadowDialog.class,
                ShadowEditText.class,
                ShadowGeocoder.class,
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
                ShadowMediaStore.ShadowImages.ShadowMedia.class,
                ShadowMotionEvent.class,
                ShadowNetworkInfo.class,
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
                ShadowSQLiteDatabase.class,
                ShadowSQLiteCursor.class,
                ShadowSQLiteOpenHelper.class,
                ShadowSQLiteQueryBuilder.class,
                ShadowSurfaceView.class,
                ShadowTextUtils.class,
                ShadowTextView.class,
                ShadowToast.class,
                ShadowTypedValue.class,
                ShadowView.class,
                ShadowViewGroup.class,
                ShadowWebView.class,
                ShadowWifiManager.class,
                ShadowZoomButtonsController.class
        );
    }

    public static void resetStaticState() {
        ShadowWrangler.getInstance().silence();
        Robolectric.application = new Application();
        Robolectric.backgroundScheduler = new Scheduler();
        Robolectric.uiThreadScheduler = new Scheduler();
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

    public static ShadowNetworkInfo shadowOf(NetworkInfo instance) {
        return (ShadowNetworkInfo) shadowOf_(instance);
    }

    public static ShadowConnectivityManager shadowOf(ConnectivityManager instance) {
        return (ShadowConnectivityManager) shadowOf_(instance);
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

    public static ShadowApplication shadowOf(Application instance) {
        return (ShadowApplication) shadowOf_(instance);
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

    public static ShadowWebView shadowOf(WebView instance) {
        return (ShadowWebView) shadowOf_(instance);
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
        return (ShadowLayoutInflater) shadowOf_(instance);
    }

    public static ShadowDisplay shadowOf(Display instance) {
        return (ShadowDisplay) shadowOf_(instance);
    }

    public static ShadowAudioManager shadowOf(AudioManager instance) {
        return (ShadowAudioManager) shadowOf_(instance);
    }

    public static ShadowGeocoder shadowOf(Geocoder instance) {
        return (ShadowGeocoder) shadowOf_(instance);
    }

    public static ShadowSQLiteDatabase shadowOf(SQLiteDatabase other) {
        return (ShadowSQLiteDatabase) Robolectric.shadowOf_(other);
    }
    
    public static ShadowSQLiteCursor shadowOf(SQLiteCursor other) {
        return (ShadowSQLiteCursor) Robolectric.shadowOf_(other);
    }

    public static ShadowSQLiteOpenHelper shadowOf(SQLiteOpenHelper other) {
        return (ShadowSQLiteOpenHelper) Robolectric.shadowOf_(other);
    }
    
    public static ShadowSQLiteQueryBuilder shadowOf(SQLiteQueryBuilder other) {
        return (ShadowSQLiteQueryBuilder) Robolectric.shadowOf_(other);
    }
    
    public static ShadowContentValues shadowOf(ContentValues other) {
        return (ShadowContentValues) Robolectric.shadowOf_(other);
    }

    public static ShadowBitmap shadowOf(Bitmap other) {
        return (ShadowBitmap) Robolectric.shadowOf_(other);
    }


    public static ShadowBluetoothAdapter shadowOf(BluetoothAdapter other) {
        return (ShadowBluetoothAdapter) Robolectric.shadowOf_(other);
    }


    public static ShadowBluetoothDevice shadowOf(BluetoothDevice other) {
        return (ShadowBluetoothDevice) Robolectric.shadowOf_(other);
    }

    @SuppressWarnings({"unchecked"})
    public static <P, R> P shadowOf_(R instance) {
        return (P) ShadowWrangler.getInstance().shadowOf(instance);
    }

    public static void runBackgroundTasks() {
        backgroundScheduler.tick(0);
    }

    public static void runUiThreadTasks() {
        uiThreadScheduler.tick(0);
    }
}
