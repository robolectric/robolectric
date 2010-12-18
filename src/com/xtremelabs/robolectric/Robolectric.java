package com.xtremelabs.robolectric;

import android.app.*;
import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.location.Geocoder;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
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
import com.xtremelabs.robolectric.content.TestSharedPreferences;
import com.xtremelabs.robolectric.shadows.*;
import com.xtremelabs.robolectric.util.HttpRequestData;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.Scheduler;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultRequestDirector;

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

    public static void bindShadowClass(Class<?> shadowClass) {
        Implements realClass = shadowClass.getAnnotation(Implements.class);
        if (realClass == null) {
            throw new IllegalArgumentException(shadowClass + " is not annotated with @Implements");
        }

        try {
            ShadowWrangler.getInstance().bindShadowClass(realClass.value(), shadowClass);
        } catch (TypeNotPresentException ignored) {
            //this allows users of the robolectric.jar file to use the non-Google APIs version of the api
            System.out.println("Warning: an error occurred while binding shadow class: " + shadowClass.getSimpleName());
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
                ShadowAssetManager.class,
                ShadowAsyncTask.class,
                ShadowAudioManager.class,
                ShadowBaseAdapter.class,
                ShadowBitmapDrawable.class,
                ShadowBundle.class,
                ShadowCamera.class,
                ShadowCameraParameters.class,
                ShadowCanvas.class,
                ShadowCompoundButton.class,
                ShadowComponentName.class,
                ShadowConnectivityManager.class,
                ShadowContentValues.class,
                ShadowContext.class,
                ShadowContextWrapper.class,
                ShadowContextThemeWrapper.class,
                ShadowDefaultRequestDirector.class,
                ShadowDisplay.class,
                ShadowDrawable.class,
                ShadowDialog.class,
                ShadowEditText.class,
                ShadowExpandableListView.class,
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
                ShadowMediaRecorder.class,
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
                ShadowTextUtils.class,
                ShadowTextView.class,
                ShadowToast.class,
                ShadowTypedValue.class,
                ShadowURLSpan.class,
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
        TestSharedPreferences.reset();
        ShadowToast.reset();
        ShadowAlertDialog.reset();
        ShadowDialog.reset();
        ShadowLooper.resetAll();
        ShadowAsyncTask.resetAll();
        ShadowDefaultRequestDirector.reset();
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

    public static ExpandableListView shadowOf(ExpandableListView instance) {
        return (ExpandableListView) shadowOf_(instance);
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

    public static ShadowDefaultRequestDirector shadowOf(DefaultRequestDirector instance) {
        return (ShadowDefaultRequestDirector) shadowOf_(instance);
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

    public static ShadowCamera shadowOf(Camera instance) {
        return (ShadowCamera) shadowOf_(instance);
    }

    public static ShadowCameraParameters shadowOf(Camera.Parameters instance) {
        return (ShadowCameraParameters) shadowOf_(instance);
    }

    public static ShadowMediaRecorder shadowOf(MediaRecorder instance) {
        return (ShadowMediaRecorder) shadowOf_(instance);
    }

    public static ShadowAssetManager shadowOf(AssetManager instance) {
        return (ShadowAssetManager) Robolectric.shadowOf_(instance);
    }

    @SuppressWarnings({"unchecked"})
    public static <P, R> P shadowOf_(R instance) {
        return (P) ShadowWrangler.getInstance().shadowOf(instance);
    }

    public static void runBackgroundTasks() {
        ShadowAsyncTask.getAsyncTaskScheduler().advanceBy(0);
    }

    public static void runUiThreadTasks() {
        getUiThreadScheduler().advanceBy(0);
    }

    /**
     * Sets up an HTTP response to be returned by calls to Apache's {@code }HttpClient} implementers.
     *
     * @param statusCode the status code of the response
     * @param responseBody the body of the response
     */
    public static void addPendingHttpResponse(int statusCode, String responseBody) {
        ShadowDefaultRequestDirector.addPendingResponse(statusCode, responseBody);
    }

    /**
     * Sets up an HTTP response to be returned by calls to Apache's {@code }HttpClient} implementers.
     *
     * @param httpResponse the response
     */
    public static void addPendingHttpResponse(HttpResponse httpResponse) {
        ShadowDefaultRequestDirector.addPendingResponse(httpResponse);
    }

    /**
     * Accessor to obtain requests made during the current test in the order in which they were made.
     *
     * @param index index of the request to retrieve.
     * @return the requested request.
     */
    public static HttpRequest getSentHttpRequest(int index) {
        return ShadowDefaultRequestDirector.getSentHttpRequest(index);
    }

    public static HttpRequestData getSentHttpRequestData(int index) {
        return ShadowDefaultRequestDirector.getSentHttpRequestData(index);
    }

    public static void pauseLooper(Looper looper) {
        ShadowLooper.pauseLooper(looper);
    }

    public static void unPauseLooper(Looper looper) {
        ShadowLooper.unPauseLooper(looper);
    }

    public static void pauseMainLooper() {
        ShadowLooper.pauseMainLooper();
    }

    public static void unPauseMainLooper() {
        ShadowLooper.unPauseMainLooper();
    }

    public static Scheduler getUiThreadScheduler() {
        return shadowOf(Looper.getMainLooper()).getScheduler();
    }
}
