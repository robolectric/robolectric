package com.xtremelabs.robolectric;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultRequestDirector;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.hardware.Camera;
import android.location.Geocoder;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.preference.DialogPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AbsSeekBar;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ExpandableListView;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RemoteViews;
import android.widget.ResourceCursorAdapter;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ZoomButtonsController;

import com.xtremelabs.robolectric.bytecode.RobolectricInternals;
import com.xtremelabs.robolectric.bytecode.ShadowWrangler;
import com.xtremelabs.robolectric.shadows.*;
import com.xtremelabs.robolectric.tester.org.apache.http.FakeHttpLayer;
import com.xtremelabs.robolectric.tester.org.apache.http.HttpRequestInfo;
import com.xtremelabs.robolectric.tester.org.apache.http.RequestMatcher;
import com.xtremelabs.robolectric.util.Scheduler;

@SuppressWarnings({"UnusedDeclaration"})
public class Robolectric {
    public static Application application;

    public static <T> T newInstanceOf(final Class<T> clazz) {
        return RobolectricInternals.newInstanceOf(clazz);
    }

    public static Object newInstanceOf(final String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (clazz != null) {
                return newInstanceOf(clazz);
            }
        } catch (ClassNotFoundException e) {
        }
        return null;
    }

    public static void bindShadowClass(final Class<?> shadowClass) {
        RobolectricInternals.bindShadowClass(shadowClass);
    }

    public static void bindDefaultShadowClasses() {
        bindShadowClasses(getDefaultShadowClasses());
    }

    public static void bindShadowClasses(final List<Class<?>> shadowClasses) {
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

    public static List<Class<?>> getDefaultShadowClasses() {
        return Arrays.asList(
                ShadowAbsListView.class,
                ShadowAbsoluteLayout.class,
                ShadowAbsSeekBar.class,
                ShadowActivityGroup.class,
                ShadowAbsSpinner.class,
                ShadowAbstractCursor.class,
                ShadowActivity.class,
                ShadowActivityManager.class,
                ShadowAdapterView.class,
                ShadowAddress.class,
                ShadowAlarmManager.class,
                ShadowAlertDialog.class,
                ShadowAlertDialog.ShadowBuilder.class,
                ShadowAnimation.class,
                ShadowAnimationUtils.class,
                ShadowApplication.class,
                ShadowAppWidgetManager.class,
                ShadowArrayAdapter.class,
                ShadowAssetManager.class,
                ShadowAsyncTask.class,
                ShadowAudioManager.class,
                ShadowBaseAdapter.class,
                ShadowSimpleAdapter.class,
                ShadowBitmap.class,
                ShadowBitmapDrawable.class,
                ShadowBitmapFactory.class,
                ShadowBluetoothAdapter.class,
                ShadowBluetoothDevice.class,
                ShadowBundle.class,
                ShadowButton.class,
                ShadowCamera.class,
                ShadowCameraParameters.class,
                ShadowCameraSize.class,
                ShadowCanvas.class,
                ShadowColorDrawable.class,
                ShadowColorMatrix.class,
                ShadowColorMatrixColorFilter.class,
                ShadowColorStateList.class,
                ShadowComponentName.class,
                ShadowCompoundButton.class,
                ShadowConfiguration.class,
                ShadowConnectivityManager.class,
                ShadowContentResolver.class,
                ShadowContentValues.class,
                ShadowContext.class,
                ShadowContextWrapper.class,
                ShadowContextThemeWrapper.class,
                ShadowCookieManager.class,
                ShadowCookieSyncManager.class,
                ShadowCountDownTimer.class,
                ShadowCursorAdapter.class,
                ShadowDateFormat.class,
                ShadowDefaultRequestDirector.class,
                ShadowDisplay.class,
                ShadowDrawable.class,
                ShadowDialog.class,
                ShadowDialogPreference.class,
                ShadowEditText.class,
                ShadowEnvironment.class,
                ShadowExpandableListView.class,
                ShadowFilter.class,
                ShadowFloatMath.class,
                ShadowFrameLayout.class,
                ShadowGeocoder.class,
                ShadowGeoPoint.class,
                ShadowGridView.class,
                ShadowHandler.class,
                ShadowHtml.class,
                ShadowImageView.class,
                ShadowInputMethodManager.class,
                ShadowIntent.class,
                ShadowIntent.class,
                ShadowIntentFilter.class,
                ShadowIntentFilterAuthorityEntry.class,
                ShadowItemizedOverlay.class,
                ShadowKeyEvent.class,
                ShadowKeyguardManager.class,
                ShadowLayoutInflater.class,
                ShadowLayoutParams.class,
                ShadowLinearLayout.class,
                ShadowListActivity.class,
                ShadowListPreference.class,
                ShadowListView.class,
                ShadowLocation.class,
                ShadowLocationManager.class,
                ShadowLooper.class,
                ShadowMapController.class,
                ShadowMapActivity.class,
                ShadowMapView.class,
                ShadowMatrix.class,
                ShadowMediaPlayer.class,
                ShadowMediaRecorder.class,
                ShadowMediaStore.ShadowImages.ShadowMedia.class,
                ShadowMenuInflater.class,
                ShadowMotionEvent.class,
                ShadowNotification.class,
                ShadowNdefMessage.class,
                ShadowNdefRecord.class,
                ShadowNfcAdapter.class,
                ShadowNotificationManager.class,
                ShadowNetworkInfo.class,
                ShadowOverlayItem.class,
                ShadowPaint.class,
                ShadowPair.class,
                ShadowParcel.class,
                ShadowPath.class,
                ShadowPendingIntent.class,
                ShadowPoint.class,
                ShadowPointF.class,
                ShadowPowerManager.class,
                ShadowPreference.class,
                ShadowPreferenceActivity.class,
                ShadowPreferenceCategory.class,
                ShadowPreferenceGroup.class,
                ShadowPreferenceManager.class,
                ShadowPreferenceScreen.class,
                ShadowProgressBar.class,
                ShadowProgressDialog.class,
                ShadowRadioButton.class,
                ShadowRadioGroup.class,
                ShadowRatingBar.class,
                ShadowRect.class,
                ShadowResolveInfo.class,
                ShadowRemoteViews.class,
                ShadowResultReceiver.class,
                ShadowResourceCursorAdapter.class,
                ShadowResources.class,
                ShadowResources.ShadowTheme.class,
                ShadowSeekBar.class,
                ShadowService.class,
                ShadowSettings.class,
                ShadowSettings.ShadowSecure.class,
                ShadowSettings.ShadowSystem.class,
                ShadowSimpleCursorAdapter.class,
                ShadowShapeDrawable.class,
                ShadowSpannableStringBuilder.class,
                ShadowSQLiteDatabase.class,
                ShadowSQLiteCursor.class,
                ShadowSQLiteOpenHelper.class,
                ShadowSQLiteQueryBuilder.class,
                ShadowSslErrorHandler.class,
                ShadowSurfaceView.class,
                ShadowTabActivity.class,
                ShadowTabHost.class,
                ShadowTabSpec.class,
                ShadowTelephonyManager.class,
                ShadowTextUtils.class,
                ShadowTextView.class,
                ShadowToast.class,
                ShadowTypedArray.class,
                ShadowTypedValue.class,
                ShadowURLSpan.class,
                ShadowVideoView.class,
                ShadowView.class,
                ShadowViewGroup.class,
                ShadowViewStub.class,
                ShadowWebSettings.class,
                ShadowWebView.class,
                ShadowWifiManager.class,
                ShadowWindow.class,
                ShadowZoomButtonsController.class
        );
    }

    public static void resetStaticState() {
        ShadowWrangler.getInstance().silence();
        Robolectric.application = new Application();
        ShadowBitmapFactory.reset();
        ShadowDrawable.reset();
        ShadowMediaStore.reset();
    }

    public static <T> T directlyOn(final T shadowedObject) {
        return RobolectricInternals.directlyOn(shadowedObject);
    }

    public static ShadowAbsListView shadowOf(final AbsListView instance) {
        return (ShadowAbsListView) shadowOf_(instance);
    }

    public static ShadowCursorAdapter shadowOf(final CursorAdapter instance) {
        return (ShadowCursorAdapter) shadowOf_(instance);
    }
    
    public static ShadowSimpleAdapter shadowOf(final SimpleAdapter instance) {
        return (ShadowSimpleAdapter) shadowOf_(instance);
    }

    public static ShadowDrawable shadowOf(final Drawable instance) {
        return (ShadowDrawable) shadowOf_(instance);
    }

    public static ShadowService shadowOf(final Service instance) {
        return (ShadowService) shadowOf_(instance);
    }

    public static ShadowToast shadowOf(final Toast instance) {
        return (ShadowToast) shadowOf_(instance);
    }

    public static ShadowNetworkInfo shadowOf(final NetworkInfo instance) {
        return (ShadowNetworkInfo) shadowOf_(instance);
    }

    public static ShadowContentResolver shadowOf(final ContentResolver instance) {
        return (ShadowContentResolver) shadowOf_(instance);
    }

    public static ShadowConnectivityManager shadowOf(final ConnectivityManager instance) {
        return (ShadowConnectivityManager) shadowOf_(instance);
    }

    public static ShadowWifiManager shadowOf(final WifiManager instance) {
        return (ShadowWifiManager) shadowOf_(instance);
    }

    public static ShadowBitmapDrawable shadowOf(final BitmapDrawable instance) {
        return (ShadowBitmapDrawable) shadowOf_(instance);
    }

    public static ShadowZoomButtonsController shadowOf(final ZoomButtonsController instance) {
        return (ShadowZoomButtonsController) shadowOf_(instance);
    }

    public static ShadowListView shadowOf(final ListView instance) {
        return (ShadowListView) shadowOf_(instance);
    }

    public static ShadowExpandableListView shadowOf(final ExpandableListView instance) {
        return (ShadowExpandableListView) shadowOf_(instance);
    }

    public static ShadowActivity shadowOf(final Activity instance) {
        return (ShadowActivity) shadowOf_(instance);
    }

    public static ShadowArrayAdapter shadowOf(final ArrayAdapter instance) {
        return (ShadowArrayAdapter) shadowOf_(instance);
    }

    public static ShadowFilter shadowOf(final Filter instance) {
        return (ShadowFilter) shadowOf_(instance);
    }

    public static ShadowContextWrapper shadowOf(final ContextWrapper instance) {
        return (ShadowContextWrapper) shadowOf_(instance);
    }

    public static ShadowApplication shadowOf(final Application instance) {
        return (ShadowApplication) shadowOf_(instance);
    }

    public static ShadowCookieManager shadowOf(final CookieManager instance) {
        return (ShadowCookieManager) shadowOf_(instance);
    }

    public static ShadowContext shadowOf(final Context instance) {
        return (ShadowContext) shadowOf_(instance);
    }

    public static ShadowCookieSyncManager shadowOf(final CookieSyncManager instance) {
        return (ShadowCookieSyncManager) shadowOf_(instance);
    }

    public static ShadowPaint shadowOf(final Paint instance) {
        return (ShadowPaint) shadowOf_(instance);
    }

    public static ShadowPath shadowOf(final Path instance) {
        return (ShadowPath) shadowOf_(instance);
    }

    public static ShadowPreference shadowOf(final Preference instance) {
        return (ShadowPreference) shadowOf_(instance);
    }

    public static ShadowPreferenceActivity shadowOf(final PreferenceActivity instance) {
        return (ShadowPreferenceActivity) shadowOf_(instance);
    }

    public static ShadowPreferenceCategory shadowOf(final PreferenceCategory instance) {
        return (ShadowPreferenceCategory) shadowOf_(instance);
    }

    public static ShadowPreferenceGroup shadowOf(final PreferenceGroup instance) {
        return (ShadowPreferenceGroup) shadowOf_(instance);
    }

    public static ShadowPreferenceScreen shadowOf(final PreferenceScreen instance) {
        return (ShadowPreferenceScreen) shadowOf_(instance);
    }

    public static ShadowProgressBar shadowOf(final ProgressBar instance) {
        return (ShadowProgressBar) shadowOf_(instance);
    }

    public static ShadowProgressDialog shadowOf(final ProgressDialog instance) {
        return (ShadowProgressDialog) shadowOf_(instance);
    }

    public static ShadowListActivity shadowOf(final ListActivity instance) {
        return (ShadowListActivity) shadowOf_(instance);
    }

    public static ShadowActivityGroup shadowOf(final ActivityGroup instance) {
        return (ShadowActivityGroup) shadowOf_(instance);
    }

    public static ShadowListPreference shadowOf(final ListPreference instance) {
        return (ShadowListPreference) shadowOf_(instance);
    }

    public static ShadowHandler shadowOf(final Handler instance) {
        return (ShadowHandler) shadowOf_(instance);
    }

    public static ShadowShapeDrawable shadowOf(final ShapeDrawable instance) {
        return (ShadowShapeDrawable) shadowOf_(instance);
    }

    public static ShadowSslErrorHandler shadowOf(final SslErrorHandler instance) {
        return (ShadowSslErrorHandler) shadowOf_(instance);
    }

    public static ShadowColorMatrix shadowOf(final ColorMatrix instance) {
        return (ShadowColorMatrix) shadowOf_(instance);
    }

    public static ShadowIntent shadowOf(final Intent instance) {
        return (ShadowIntent) shadowOf_(instance);
    }

    public static ShadowView shadowOf(final View instance) {
        return (ShadowView) shadowOf_(instance);
    }

    public static ShadowColorDrawable shadowOf(final ColorDrawable instance) {
        return (ShadowColorDrawable) shadowOf_(instance);
    }

    public static ShadowViewGroup shadowOf(final ViewGroup instance) {
        return (ShadowViewGroup) shadowOf_(instance);
    }

    public static ShadowWebSettings shadowOf(final WebSettings instance) {
        return (ShadowWebSettings) shadowOf_(instance);
    }

    public static ShadowWebView shadowOf(final WebView instance) {
        return (ShadowWebView) shadowOf_(instance);
    }

    public static ShadowAdapterView shadowOf(final AdapterView instance) {
        return (ShadowAdapterView) shadowOf_(instance);
    }

    public static ShadowTextView shadowOf(final TextView instance) {
        return (ShadowTextView) shadowOf_(instance);
    }

    public static ShadowImageView shadowOf(final ImageView instance) {
        return (ShadowImageView) shadowOf_(instance);
    }

    public static ShadowResolveInfo shadowOf(final ResolveInfo instance) {
        return (ShadowResolveInfo) shadowOf_(instance);
    }

    public static ShadowRemoteViews shadowOf(final RemoteViews instance) {
        return (ShadowRemoteViews) shadowOf_(instance);
    }

    public static ShadowDialog shadowOf(final Dialog instance) {
        return (ShadowDialog) shadowOf_(instance);
    }

    public static ShadowDialogPreference shadowOf(final DialogPreference instance) {
        return (ShadowDialogPreference) shadowOf_(instance);
    }

    public static ShadowDefaultRequestDirector shadowOf(final DefaultRequestDirector instance) {
        return (ShadowDefaultRequestDirector) shadowOf_(instance);
    }

    public static ShadowAlertDialog shadowOf(final AlertDialog instance) {
        return (ShadowAlertDialog) shadowOf_(instance);
    }

    public static ShadowLooper shadowOf(final Looper instance) {
        return (ShadowLooper) shadowOf_(instance);
    }

    public static ShadowCanvas shadowOf(final Canvas instance) {
        return (ShadowCanvas) shadowOf_(instance);
    }

    public static ShadowLocationManager shadowOf(final LocationManager instance) {
        return (ShadowLocationManager) shadowOf_(instance);
    }

    public static ShadowAppWidgetManager shadowOf(final AppWidgetManager instance) {
        return (ShadowAppWidgetManager) shadowOf_(instance);
    }

    public static ShadowResources shadowOf(final Resources instance) {
        return (ShadowResources) shadowOf_(instance);
    }

    public static ShadowResultReceiver shadowOf(final ResultReceiver instance) {
        return (ShadowResultReceiver) shadowOf_(instance);
    }

    public static ShadowLayoutInflater shadowOf(final LayoutInflater instance) {
        return (ShadowLayoutInflater) shadowOf_(instance);
    }

    public static ShadowMenuInflater shadowOf(final MenuInflater instance) {
        return (ShadowMenuInflater) shadowOf_(instance);
    }

    public static ShadowDisplay shadowOf(final Display instance) {
        return (ShadowDisplay) shadowOf_(instance);
    }

    public static ShadowAudioManager shadowOf(final AudioManager instance) {
        return (ShadowAudioManager) shadowOf_(instance);
    }

    public static ShadowGeocoder shadowOf(final Geocoder instance) {
        return (ShadowGeocoder) shadowOf_(instance);
    }

    public static ShadowSQLiteDatabase shadowOf(final SQLiteDatabase other) {
        return (ShadowSQLiteDatabase) Robolectric.shadowOf_(other);
    }

    public static ShadowSQLiteCursor shadowOf(final SQLiteCursor other) {
        return (ShadowSQLiteCursor) Robolectric.shadowOf_(other);
    }

    public static ShadowSQLiteOpenHelper shadowOf(final SQLiteOpenHelper other) {
        return (ShadowSQLiteOpenHelper) Robolectric.shadowOf_(other);
    }

    public static ShadowSQLiteQueryBuilder shadowOf(final SQLiteQueryBuilder other) {
        return (ShadowSQLiteQueryBuilder) Robolectric.shadowOf_(other);
    }

    public static ShadowContentValues shadowOf(final ContentValues other) {
        return (ShadowContentValues) Robolectric.shadowOf_(other);
    }

    public static ShadowCamera shadowOf(final Camera instance) {
        return (ShadowCamera) shadowOf_(instance);
    }

    public static ShadowCameraParameters shadowOf(final Camera.Parameters instance) {
        return (ShadowCameraParameters) shadowOf_(instance);
    }

    public static ShadowCameraSize shadowOf(final Camera.Size instance) {
        return (ShadowCameraSize) shadowOf_(instance);
    }

    public static ShadowMediaPlayer shadowOf(final MediaPlayer instance) {
        return (ShadowMediaPlayer) shadowOf_(instance);
    }

    public static ShadowMediaRecorder shadowOf(final MediaRecorder instance) {
        return (ShadowMediaRecorder) shadowOf_(instance);
    }

    public static ShadowAssetManager shadowOf(final AssetManager instance) {
        return (ShadowAssetManager) Robolectric.shadowOf_(instance);
    }

    public static ShadowAlarmManager shadowOf(final AlarmManager instance) {
        return (ShadowAlarmManager) Robolectric.shadowOf_(instance);
    }

    public static ShadowConfiguration shadowOf(final Configuration instance) {
        return (ShadowConfiguration) Robolectric.shadowOf_(instance);
    }

    public static ShadowCountDownTimer shadowOf(final CountDownTimer instance) {
        return (ShadowCountDownTimer) Robolectric.shadowOf_(instance);
    }

    public static ShadowBitmap shadowOf(final Bitmap other) {
        return (ShadowBitmap) Robolectric.shadowOf_(other);
    }

    public static ShadowBluetoothAdapter shadowOf(final BluetoothAdapter other) {
        return (ShadowBluetoothAdapter) Robolectric.shadowOf_(other);
    }

    public static ShadowBluetoothDevice shadowOf(final BluetoothDevice other) {
        return (ShadowBluetoothDevice) Robolectric.shadowOf_(other);
    }

    public static ShadowMatrix shadowOf(final Matrix other) {
        return (ShadowMatrix) Robolectric.shadowOf_(other);
    }

    public static ShadowMotionEvent shadowOf(final MotionEvent other) {
        return (ShadowMotionEvent) Robolectric.shadowOf_(other);
    }

    public static ShadowNotificationManager shadowOf(final NotificationManager other) {
        return (ShadowNotificationManager) Robolectric.shadowOf_(other);
    }

    public static ShadowNotification shadowOf(final Notification other) {
        return (ShadowNotification) Robolectric.shadowOf_(other);
    }

    public static ShadowAbsSeekBar shadowOf(final AbsSeekBar instance) {
        return (ShadowAbsSeekBar) shadowOf_(instance);
    }

    public static ShadowRatingBar shadowOf(final RatingBar instance) {
        return (ShadowRatingBar) shadowOf_(instance);
    }

    public static ShadowSeekBar shadowOf(final SeekBar instance) {
        return (ShadowSeekBar) shadowOf_(instance);
    }

    public static ShadowParcel shadowOf(final Parcel instance) {
        return (ShadowParcel) shadowOf_(instance);
    }

    public static ShadowAnimationUtils shadowOf(final AnimationUtils instance) {
        return (ShadowAnimationUtils) shadowOf_(instance);
    }

    public static ShadowGridView shadowOf(final GridView instance) {
        return (ShadowGridView) shadowOf_(instance);
    }

    public static ShadowTabHost shadowOf(final TabHost instance) {
        return (ShadowTabHost) shadowOf_(instance);
    }

    public static ShadowTabSpec shadowOf(final TabHost.TabSpec instance) {
        return (ShadowTabSpec) shadowOf_(instance);
    }

    public static ShadowFrameLayout shadowOf(final FrameLayout instance) {
        return (ShadowFrameLayout) shadowOf_(instance);
    }

    public static ShadowRect shadowOf(final Rect instance) {
        return (ShadowRect) shadowOf_(instance);
    }

    public static PendingIntent shadowOf(final PendingIntent instance) {
        return (PendingIntent) shadowOf_(instance);
    }

    public static ShadowDateFormat shadowOf(final DateFormat instance) {
        return (ShadowDateFormat) shadowOf_(instance);
    }

    public static ShadowResourceCursorAdapter shadowOf(final ResourceCursorAdapter instance) {
        return (ShadowResourceCursorAdapter) shadowOf_(instance);
    }

    public static ShadowSimpleCursorAdapter shadowOf(final SimpleCursorAdapter instance) {
        return (ShadowSimpleCursorAdapter) shadowOf_(instance);
    }

    public static ShadowPowerManager shadowOf(final PowerManager instance) {
        return (ShadowPowerManager) shadowOf_(instance);
    }

    public static ShadowKeyguardManager shadowOf(final KeyguardManager instance) {
        return (ShadowKeyguardManager) shadowOf_(instance);
    }

    public static ShadowInputMethodManager shadowOf(final InputMethodManager instance) {
        return (ShadowInputMethodManager) shadowOf_(instance);
    }

    public static ShadowAnimation shadowOf(final Animation instance) {
        return (ShadowAnimation) shadowOf_(instance);
    }

    public static ShadowVideoView shadowOf(final VideoView instance) {
        return (ShadowVideoView) shadowOf_(instance);
    }

    public static ShadowTelephonyManager shadowOf(final TelephonyManager instance) {
        return (ShadowTelephonyManager) shadowOf_(instance);
    }

    public static ShadowActivityManager shadowOf(final ActivityManager instance) {
        return (ShadowActivityManager) shadowOf_(instance);
    }

    @SuppressWarnings({"unchecked"})
    public static <P, R> P shadowOf_(final R instance) {
        return (P) ShadowWrangler.getInstance().shadowOf(instance);
    }

    /**
     * Runs any background tasks previously queued by {@link android.os.AsyncTask#execute(Object[])}.
     * <p/>
     * <p/>
     * Note: calling this method does not pause or un-pause the scheduler.
     */
    public static void runBackgroundTasks() {
        getBackgroundScheduler().advanceBy(0);
    }

    /**
     * Runs any immediately runnable tasks previously queued on the UI thread,
     * e.g. by {@link Activity#runOnUiThread(Runnable)} or {@link android.os.AsyncTask#onPostExecute(Object)}.
     * <p/>
     * <p/>
     * Note: calling this method does not pause or un-pause the scheduler.
     */
    public static void runUiThreadTasks() {
        getUiThreadScheduler().advanceBy(0);
    }

    public static void runUiThreadTasksIncludingDelayedTasks() {
        getUiThreadScheduler().advanceToLastPostedRunnable();
    }

    /**
     * Sets up an HTTP response to be returned by calls to Apache's {@code HttpClient} implementers.
     *
     * @param statusCode   the status code of the response
     * @param responseBody the body of the response
     */
    public static void addPendingHttpResponse(final int statusCode, final String responseBody) {
        getFakeHttpLayer().addPendingHttpResponse(statusCode, responseBody);
    }

    /**
     * Sets up an HTTP response to be returned by calls to Apache's {@code HttpClient} implementers.
     *
     * @param statusCode   the status code of the response
     * @param responseBody the body of the response
     * @param contentType  the contentType of the response
     */
    public static void addPendingHttpResponseWithContentType(final int statusCode, final String responseBody, final Header contentType) {
        getFakeHttpLayer().addPendingHttpResponseWithContentType(statusCode, responseBody, contentType);
    }

    /**
     * Sets up an HTTP response to be returned by calls to Apache's {@code HttpClient} implementers.
     *
     * @param httpResponse the response
     */
    public static void addPendingHttpResponse(final HttpResponse httpResponse) {
        getFakeHttpLayer().addPendingHttpResponse(httpResponse);
    }

    /**
     * Accessor to obtain HTTP requests made during the current test in the order in which they were made.
     *
     * @param index index of the request to retrieve.
     * @return the requested request.
     */
    public static HttpRequest getSentHttpRequest(final int index) {
        return ShadowDefaultRequestDirector.getSentHttpRequest(index);
    }

    /**
     * Accessor to find out if HTTP requests were made during the current test.
     *
     * @return whether a request was made.
     */
    public static boolean httpRequestWasMade() {
        return getShadowApplication().getFakeHttpLayer().hasRequestInfos();
    }

    public static boolean httpRequestWasMade(final String uri) {
        return getShadowApplication().getFakeHttpLayer().hasRequestMatchingRule(new FakeHttpLayer.UriRequestMatcher(uri));
    }

    /**
     * Accessor to obtain metadata for an HTTP request made during the current test in the order in which they were made.
     *
     * @param index index of the request to retrieve.
     * @return the requested request metadata.
     */
    public static HttpRequestInfo getSentHttpRequestInfo(final int index) {
        return ShadowDefaultRequestDirector.getSentHttpRequestInfo(index);
    }

    /**
     * Adds an HTTP response rule. The response will be returned when the rule is matched.
     *
     * @param method   method to match.
     * @param uri      uri to match.
     * @param response response to return when a match is found.
     */
    public static void addHttpResponseRule(final String method, final String uri, final HttpResponse response) {
        getFakeHttpLayer().addHttpResponseRule(method, uri, response);
    }

    /**
     * Adds an HTTP response rule with a default method of GET. The response will be returned when the rule is matched.
     *
     * @param uri      uri to match.
     * @param response response to return when a match is found.
     */
    public static void addHttpResponseRule(final String uri, final HttpResponse response) {
        getFakeHttpLayer().addHttpResponseRule(uri, response);
    }

    /**
     * Adds an HTTP response rule. The response will be returned when the rule is matched.
     *
     * @param uri      uri to match.
     * @param response response to return when a match is found.
     */
    public static void addHttpResponseRule(final String uri, final String response) {
        getFakeHttpLayer().addHttpResponseRule(uri, response);
    }

    /**
     * Adds an HTTP response rule. The response will be returned when the rule is matched.
     *
     * @param requestMatcher custom {@code RequestMatcher}.
     * @param response       response to return when a match is found.
     */
    public static void addHttpResponseRule(final RequestMatcher requestMatcher, final HttpResponse response) {
        getFakeHttpLayer().addHttpResponseRule(requestMatcher, response);
    }

    /**
     * Adds an HTTP response rule. For each time the rule is matched, responses will be shifted
     * off the list and returned. When all responses have been given and the rule is matched again,
     * an exception will be thrown.
     *
     * @param requestMatcher custom {@code RequestMatcher}.
     * @param responses      responses to return in order when a match is found.
     */
    public static void addHttpResponseRule(final RequestMatcher requestMatcher, final List<? extends HttpResponse> responses) {
        getFakeHttpLayer().addHttpResponseRule(requestMatcher, responses);
    }

    public static FakeHttpLayer getFakeHttpLayer() {
        return getShadowApplication().getFakeHttpLayer();
    }

    public static void setDefaultHttpResponse(final int statusCode, final String responseBody) {
        getFakeHttpLayer().setDefaultHttpResponse(statusCode, responseBody);
    }

    public static void setDefaultHttpResponse(final HttpResponse defaultHttpResponse) {
        getFakeHttpLayer().setDefaultHttpResponse(defaultHttpResponse);
    }

    public static void clearHttpResponseRules() {
        getFakeHttpLayer().clearHttpResponseRules();
    }

    public static void clearPendingHttpResponses() {
        getFakeHttpLayer().clearPendingHttpResponses();
    }

    public static void pauseLooper(final Looper looper) {
        ShadowLooper.pauseLooper(looper);
    }

    public static void unPauseLooper(final Looper looper) {
        ShadowLooper.unPauseLooper(looper);
    }

    public static void pauseMainLooper() {
        ShadowLooper.pauseMainLooper();
    }

    public static void unPauseMainLooper() {
        ShadowLooper.unPauseMainLooper();
    }

    public static void idleMainLooper(final int interval) {
        ShadowLooper.idleMainLooper(interval);
    }

    public static Scheduler getUiThreadScheduler() {
        return shadowOf(Looper.getMainLooper()).getScheduler();
    }

    public static Scheduler getBackgroundScheduler() {
        return getShadowApplication().getBackgroundScheduler();
    }

    public static ShadowApplication getShadowApplication() {
        return shadowOf(Robolectric.application);
    }

    public static void setDisplayMetricsDensity(final float densityMultiplier) {
        shadowOf(getShadowApplication().getResources()).setDensity(densityMultiplier);
    }

    /**
     * Calls {@code performClick()} on a {@code View} after ensuring that it and its ancestors are visible and that it
     * is enabled.
     *
     * @param view the view to click on
     * @return true if {@code View.OnClickListener}s were found and fired, false otherwise.
     * @throws RuntimeException if the preconditions are not met.
     */
    public static boolean clickOn(final View view) {
        return shadowOf(view).checkedPerformClick();
    }

    public static String visualize(final View view) {
        Canvas canvas = new Canvas();
        view.draw(canvas);
        return shadowOf(canvas).getDescription();
    }

    public static String visualize(final Canvas canvas) {
        return shadowOf(canvas).getDescription();
    }

    public static String visualize(final Bitmap bitmap) {
        return shadowOf(bitmap).getDescription();
    }

    /**
     * Reflection helper methods.
     */
    public static class Reflection {
        public static <T> T newInstanceOf(final Class<T> clazz) {
            return Robolectric.newInstanceOf(clazz);
        }

        public static Object newInstanceOf(final String className) {
            return Robolectric.newInstanceOf(className);
        }

        public static void setFinalStaticField(final Class classWhichContainsField, final String fieldName, final Object newValue) {
            try {
                Field field = classWhichContainsField.getField(fieldName);
                field.setAccessible(true);

                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

                field.set(null, newValue);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
