package org.robolectric;

import android.accounts.AccountManager;
import android.app.*;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.CursorWrapper;
import android.database.MergeCursor;
import android.database.sqlite.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.*;
import android.preference.*;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.ClipboardManager;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.text.method.PasswordTransformationMethod;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.*;
import android.view.animation.*;
import android.view.inputmethod.InputMethodManager;
import android.webkit.*;
import android.widget.*;
import org.robolectric.bytecode.IgnorableClassNotFoundException;
import org.robolectric.bytecode.RobolectricInternals;
import org.robolectric.bytecode.ShadowWrangler;
import org.robolectric.internal.Implements;
import org.robolectric.res.ResourceLoader;
import org.robolectric.shadows.*;
import org.robolectric.tester.org.apache.http.FakeHttpLayer;
import org.robolectric.tester.org.apache.http.HttpRequestInfo;
import org.robolectric.tester.org.apache.http.RequestMatcher;
import org.robolectric.util.Scheduler;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultRequestDirector;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Robolectric {
    public static Application application;
    public static final int DEFAULT_SDK_VERSION = 16;
    private static Set<String> unloadableClassNames = new HashSet<String>();

    public static <T> T newInstanceOf(Class<T> clazz) {
        return RobolectricInternals.newInstanceOf(clazz);
    }

    public static Object newInstanceOf(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (clazz != null) {
                return newInstanceOf(clazz);
            }
        } catch (ClassNotFoundException e) {
        }
        return null;
    }

    public static <T> T newInstance(Class<T> clazz, Class[] parameterTypes, Object[] params) {
        return RobolectricInternals.newInstance(clazz, parameterTypes, params);
    }

    public static void bindShadowClass(Class<?> shadowClass) {
        Implements realClass = shadowClass.getAnnotation(Implements.class);
        if (realClass == null) {
            throw new IllegalArgumentException(shadowClass + " is not annotated with @Implements");
        }

        try {
            getShadowWrangler().bindShadowClass(realClass.value(), shadowClass);
        } catch (TypeNotPresentException typeLoadingException) {
            String unloadableClassName = shadowClass.getSimpleName();
            if (isIgnorableClassLoadingException(typeLoadingException)) {
                //this allows users of the robolectric.jar file to use the non-Google APIs version of the api
                if (unloadableClassNames.add(unloadableClassName)) {
                    System.out.println("Warning: an error occurred while binding shadow class: " + unloadableClassName);
                }
            } else {
                throw typeLoadingException;
            }
        }
    }

    // todo: make private
    public static ShadowWrangler getShadowWrangler() {
        return ((ShadowWrangler) RobolectricInternals.getClassHandler());
    }

    private static boolean isIgnorableClassLoadingException(Throwable typeLoadingException) {
        if (typeLoadingException != null) {
            // instanceof doesn't work here. Are we in different classloaders?
            if (typeLoadingException.getClass().getName().equals(IgnorableClassNotFoundException.class.getName())) {
                return true;
            }

            if (typeLoadingException instanceof NoClassDefFoundError
                    || typeLoadingException instanceof ClassNotFoundException
                    || typeLoadingException instanceof TypeNotPresentException) {
                return isIgnorableClassLoadingException(typeLoadingException.getCause());
            }
        }
        return false;
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
        getShadowWrangler().logMissingInvokedShadowMethods();
    }

    public static List<Class<?>> getDefaultShadowClasses() {
        return Arrays.asList(
                ShadowAbsListView.class,
                ShadowAbsoluteLayout.class,
                ShadowAbsoluteLayout.ShadowLayoutParams.class,
                ShadowAbsSeekBar.class,
                ShadowAbsSpinner.class,
                ShadowAbstractCursor.class,
                ShadowAccount.class,
                ShadowAccountManager.class,
                ShadowActivity.class,
                ShadowActivityInfo.class,
                ShadowActivityGroup.class,
                ShadowActivityManager.class,
                ShadowAdapterView.class,
                ShadowAddress.class,
                ShadowAlarmManager.class,
                ShadowAlertDialog.class,
                ShadowAlertDialog.ShadowBuilder.class,
                ShadowAlphaAnimation.class,
                ShadowAndroidHttpClient.class,
                ShadowAnimation.class,
                ShadowAnimator.class,
                ShadowAnimationDrawable.class,
                ShadowAnimationSet.class,
                ShadowAnimationUtils.class,
                ShadowApplication.class,
                ShadowAppWidgetManager.class,
                ShadowArrayAdapter.class,
                ShadowAssetManager.class,
                ShadowAsyncTask.class,
                ShadowAudioManager.class,
                ShadowBaseAdapter.class,
                ShadowBase64.class,
                ShadowBinder.class,
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
                ShadowCheckedTextView.class,
                ShadowClipboardManager.class,
                ShadowColor.class,
                ShadowColorDrawable.class,
                ShadowColorMatrix.class,
                ShadowColorMatrixColorFilter.class,
                ShadowColorStateList.class,
                ShadowComponentName.class,
                ShadowCompoundButton.class,
                ShadowConfiguration.class,
                ShadowConnectivityManager.class,
                ShadowContentProvider.class,
                ShadowContentProviderOperation.class,
                ShadowContentProviderOperationBuilder.class,
                ShadowContentProviderResult.class,
                ShadowContentResolver.class,
                ShadowContentUris.class,
                ShadowContentValues.class,
                ShadowContext.class,
                ShadowContextWrapper.class,
                ShadowContextThemeWrapper.class,
                ShadowCookieManager.class,
                ShadowCookieSyncManager.class,
                ShadowCornerPathEffect.class,
                ShadowCountDownTimer.class,
                ShadowCriteria.class,
                ShadowCursorAdapter.class,
                ShadowCursorLoader.class,
                ShadowCursorWrapper.class,
                ShadowDatabaseUtils.class,
                ShadowDateFormat.class,
                ShadowDefaultRequestDirector.class,
                ShadowDisplay.class,
                ShadowDrawable.class,
                ShadowDialog.class,
                ShadowDialogFragment.class,
                ShadowDialogPreference.class,
                ShadowDownloadManager.class,
                ShadowDownloadManager.ShadowRequest.class,
                ShadowDownloadManager.ShadowQuery.class,
                ShadowEditText.class,
                ShadowEditTextPreference.class,
                ShadowEnvironment.class,
                ShadowExpandableListView.class,
                ShadowFilter.class,
                ShadowFloatMath.class,
                ShadowFragment.class,
                ShadowFragmentActivity.class,
                ShadowFragmentPagerAdapter.class,
                ShadowFrameLayout.class,
                ShadowGallery.class,
                ShadowGeocoder.class,
                //ShadowGeoPoint.class,
                ShadowGestureDetector.class,
                ShadowGridView.class,
                ShadowHandler.class,
                ShadowHandlerThread.class,
                ShadowHtml.class,
                ShadowImageButton.class,
                ShadowImageView.class,
                ShadowInputMethodManager.class,
                ShadowInputDevice.class,
                ShadowInputEvent.class,
                ShadowIntent.class,
                ShadowIntentFilter.class,
                ShadowIntentFilterAuthorityEntry.class,
                //ShadowItemizedOverlay.class,
                ShadowLayoutAnimationController.class,
                ShadowJsPromptResult.class,
                ShadowJsResult.class,
                ShadowKeyEvent.class,
                ShadowKeyguardManager.class,
                ShadowKeyGuardLock.class,
                ShadowLayerDrawable.class,
                ShadowLayoutInflater.class,
                ShadowLayoutParams.class,
                ShadowLinearGradient.class,
                ShadowLinearLayout.class,
                ShadowLinkMovementMethod.class,
                ShadowListActivity.class,
                ShadowListPreference.class,
                ShadowListView.class,
                ShadowLocalActivityManager.class,
                ShadowLocalBroadcastManager.class,
                ShadowLocation.class,
                ShadowLocationManager.class,
                ShadowLog.class,
                ShadowLooper.class,
                //ShadowMapController.class,
                //ShadowMapActivity.class,
                //ShadowMapView.class,
                ShadowMarginLayoutParams.class,
                ShadowMatrix.class,
                ShadowMatrixCursor.class,
                ShadowMeasureSpec.class,
                ShadowMediaPlayer.class,
                ShadowMediaRecorder.class,
                ShadowMediaStore.ShadowImages.ShadowMedia.class,
                ShadowMenuInflater.class,
                ShadowMergeCursor.class,
                ShadowMessage.class,
                ShadowMessenger.class,
                ShadowMimeTypeMap.class,
                ShadowMotionEvent.class,
                ShadowNotification.class,
                ShadowNdefMessage.class,
                ShadowNdefRecord.class,
                ShadowNfcAdapter.class,
                ShadowNotificationManager.class,
                ShadowNetworkInfo.class,
                //ShadowOverlayItem.class,
                ShadowObjectAnimator.class,
                ShadowPagerAdapter.class,
                ShadowPaint.class,
                ShadowPair.class,
                ShadowParcel.class,
                ShadowPasswordTransformationMethod.class,
                ShadowPath.class,
                ShadowPendingIntent.class,
                ShadowPeriodicSync.class,
                ShadowPhoneNumberUtils.class,
                ShadowPoint.class,
                ShadowPointF.class,
                ShadowPopupWindow.class,
                ShadowPowerManager.class,
                ShadowPowerManager.ShadowWakeLock.class,
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
                ShadowRectF.class,
                ShadowRelativeLayout.class,
                ShadowRelativeLayoutParams.class,
                ShadowResolveInfo.class,
                ShadowRemoteCallbackList.class,
                ShadowRemoteViews.class,
                ShadowResultReceiver.class,
                ShadowResourceCursorAdapter.class,
                ShadowResources.class,
                ShadowNotFoundException.class,
                ShadowResources.ShadowTheme.class,
                ShadowScaleGestureDetector.class,
                ShadowScanResult.class,
                ShadowScrollView.class,
                ShadowScroller.class,
                ShadowSeekBar.class,
                ShadowSensorManager.class,
                ShadowService.class,
                ShadowSettings.class,
                ShadowSettings.ShadowSecure.class,
                ShadowSettings.ShadowSystem.class,
                ShadowSimpleCursorAdapter.class,
                ShadowShapeDrawable.class,
                ShadowSmsManager.class,
                ShadowSpannableString.class,
                ShadowSpannableStringBuilder.class,
                ShadowSpannedString.class,
                ShadowSparseArray.class,
                ShadowSparseBooleanArray.class,
                ShadowSparseIntArray.class,
                ShadowSpinner.class,
                ShadowSyncResult.class,
                ShadowSyncResult.ShadowSyncStats.class,
                ShadowSQLiteProgram.class,
                ShadowSQLiteCloseable.class,
                ShadowSQLiteDatabase.class,
                ShadowSQLiteCursor.class,
                ShadowSQLiteOpenHelper.class,
                ShadowSQLiteStatement.class,
                ShadowSQLiteQueryBuilder.class,
                ShadowSslErrorHandler.class,
                ShadowStateListDrawable.class,
                ShadowStatFs.class,
                ShadowSurfaceView.class,
                ShadowTabActivity.class,
                ShadowTabHost.class,
                ShadowTabSpec.class,
                ShadowTelephonyManager.class,
                ShadowTextPaint.class,
                ShadowTextUtils.class,
                ShadowTextView.class,
                ShadowTime.class,
                ShadowToast.class,
                ShadowTouchDelegate.class,
                ShadowTranslateAnimation.class,
                ShadowTypedArray.class,
                ShadowTypedValue.class,
                ShadowTypeface.class,
                ShadowUriMatcher.class,
                ShadowURLSpan.class,
                ShadowValueAnimator.class,
                ShadowVibrator.class,
                ShadowVideoView.class,
                ShadowView.class,
                ShadowViewAnimator.class,
                ShadowViewConfiguration.class,
                ShadowViewGroup.class,
                ShadowViewFlipper.class,
                ShadowViewMeasureSpec.class,
                ShadowViewPager.class,
                ShadowViewStub.class,
                ShadowViewTreeObserver.class,
                ShadowWebView.class,
                ShadowWifiConfiguration.class,
                ShadowWifiInfo.class,
                ShadowWifiManager.class,
                ShadowWifiManager.ShadowWifiLock.class,
                ShadowWindow.class,
                ShadowZoomButtonsController.class
        );
    }

    public static void resetStaticState() {
        getShadowWrangler().silence();
        Robolectric.application = null;
        ShadowBitmapFactory.reset();
        ShadowDrawable.reset();
//        ShadowMediaStore.reset();
        ShadowLog.reset();
        ShadowContext.clearFilesAndCache();
        ShadowLooper.resetThreadLoopers();
        ShadowDialog.reset();
        ShadowContentResolver.reset();
        ShadowLocalBroadcastManager.reset();
        ShadowMimeTypeMap.reset();
        ShadowPowerManager.reset();
        ShadowStatFs.reset();
    }

    public static <T> T directlyOn(T shadowedObject) {
        return RobolectricInternals.directlyOn(shadowedObject);
    }

    public static <T> T directlyOn(T shadowedObject, Class<T> clazz) {
        return RobolectricInternals.directlyOn(shadowedObject, clazz);
    }

    public static ShadowAbsListView shadowOf(AbsListView instance) {
        return (ShadowAbsListView) shadowOf_(instance);
    }

    public static ShadowAbsSeekBar shadowOf(AbsSeekBar instance) {
        return (ShadowAbsSeekBar) shadowOf_(instance);
    }

    public static ShadowAccountManager shadowOf(AccountManager instance) {
        return (ShadowAccountManager) shadowOf_(instance);
    }
    
    public static ShadowActivity shadowOf(Activity instance) {
        return (ShadowActivity) shadowOf_(instance);
    }

    public static ShadowActivityGroup shadowOf(ActivityGroup instance) {
        return (ShadowActivityGroup) shadowOf_(instance);
    }

    public static ShadowActivityManager shadowOf(ActivityManager instance) {
        return (ShadowActivityManager) shadowOf_(instance);
    }

    public static ShadowAdapterView shadowOf(AdapterView instance) {
        return (ShadowAdapterView) shadowOf_(instance);
    }

    public static ShadowAddress shadowOf(Address instance) {
        return (ShadowAddress) shadowOf_(instance);
    }

    public static ShadowAlarmManager shadowOf(AlarmManager instance) {
        return (ShadowAlarmManager) Robolectric.shadowOf_(instance);
    }

    public static ShadowAlertDialog shadowOf(AlertDialog instance) {
        return (ShadowAlertDialog) shadowOf_(instance);
    }

    public static ShadowAlphaAnimation shadowOf(AlphaAnimation instance) {
        return (ShadowAlphaAnimation) shadowOf_(instance);
    }

    public static ShadowAnimation shadowOf(Animation instance) {
        return (ShadowAnimation) shadowOf_(instance);
    }

    public static ShadowLayoutAnimationController shadowOf(LayoutAnimationController instance) {
        return (ShadowLayoutAnimationController) shadowOf_(instance);
    }

    public static ShadowAnimationDrawable shadowOf(AnimationDrawable instance) {
        return (ShadowAnimationDrawable) shadowOf_(instance);
    }

    public static ShadowAnimationSet shadowOf(AnimationSet instance) {
        return (ShadowAnimationSet) shadowOf_(instance);
    }
    
    public static ShadowAnimationUtils shadowOf(AnimationUtils instance) {
        return (ShadowAnimationUtils) shadowOf_(instance);
    }

    public static ShadowApplication shadowOf(Application instance) {
        return (ShadowApplication) shadowOf_(instance);
    }

    public static ShadowAppWidgetManager shadowOf(AppWidgetManager instance) {
        return (ShadowAppWidgetManager) shadowOf_(instance);
    }

    public static ShadowArrayAdapter shadowOf(ArrayAdapter instance) {
        return (ShadowArrayAdapter) shadowOf_(instance);
    }

    public static ShadowAssetManager shadowOf(AssetManager instance) {
        return (ShadowAssetManager) Robolectric.shadowOf_(instance);
    }
    
    @SuppressWarnings("rawtypes")
    public static ShadowAsyncTask shadowOf(AsyncTask instance){
        return (ShadowAsyncTask) Robolectric.shadowOf_(instance);
    }

    public static ShadowAudioManager shadowOf(AudioManager instance) {
        return (ShadowAudioManager) shadowOf_(instance);
    }

    public static ShadowBaseAdapter shadowOf(BaseAdapter other) {
        return (ShadowBaseAdapter) Robolectric.shadowOf_(other);
    }

    public static ShadowBitmap shadowOf(Bitmap other) {
        return (ShadowBitmap) Robolectric.shadowOf_(other);
    }

    public static ShadowBitmapDrawable shadowOf(BitmapDrawable instance) {
        return (ShadowBitmapDrawable) shadowOf_(instance);
    }

    public static ShadowBluetoothAdapter shadowOf(BluetoothAdapter other) {
        return (ShadowBluetoothAdapter) Robolectric.shadowOf_(other);
    }

    public static ShadowBluetoothDevice shadowOf(BluetoothDevice other) {
        return (ShadowBluetoothDevice) Robolectric.shadowOf_(other);
    }

    public static ShadowBundle shadowOf(Bundle instance) {
        return (ShadowBundle) shadowOf_(instance);
    }

    public static ShadowCamera shadowOf(Camera instance) {
        return (ShadowCamera) shadowOf_(instance);
    }

    public static ShadowCameraParameters shadowOf(Camera.Parameters instance) {
        return (ShadowCameraParameters) shadowOf_(instance);
    }

    public static ShadowCameraSize shadowOf(Camera.Size instance) {
        return (ShadowCameraSize) shadowOf_(instance);
    }

    public static ShadowCanvas shadowOf(Canvas instance) {
        return (ShadowCanvas) shadowOf_(instance);
    }

    public static ShadowCheckedTextView shadowOf(CheckedTextView instance) {
        return (ShadowCheckedTextView) shadowOf_(instance);
    }

    public static ShadowClipboardManager shadowOf(ClipboardManager instance) {
        return (ShadowClipboardManager) shadowOf_(instance);
    }

    public static ShadowColor shadowOf(Color instance) {
        return (ShadowColor) shadowOf_(instance);
    }

    public static ShadowColorDrawable shadowOf(ColorDrawable instance) {
        return (ShadowColorDrawable) shadowOf_(instance);
    }

    public static ShadowColorMatrix shadowOf(ColorMatrix instance) {
        return (ShadowColorMatrix) shadowOf_(instance);
    }

    public static ShadowConfiguration shadowOf(Configuration instance) {
        return (ShadowConfiguration) Robolectric.shadowOf_(instance);
    }

    public static ShadowConnectivityManager shadowOf(ConnectivityManager instance) {
        return (ShadowConnectivityManager) shadowOf_(instance);
    }

    public static ShadowCookieManager shadowOf(CookieManager instance) {
        return (ShadowCookieManager) shadowOf_(instance);
    }

    public static ShadowContentResolver shadowOf(ContentResolver instance) {
        return (ShadowContentResolver) shadowOf_(instance);
    }

    public static ShadowContentProviderOperation shadowOf(ContentProviderOperation instance) {
        return (ShadowContentProviderOperation) shadowOf_(instance);
    }

    public static ShadowContentProviderOperationBuilder shadowOf(ContentProviderOperation.Builder instance) {
        return (ShadowContentProviderOperationBuilder) shadowOf_(instance);
    }

    public static ShadowContentProviderResult shadowOf(ContentProviderResult instance) {
        return (ShadowContentProviderResult) shadowOf_(instance);
    }

    public static ShadowCookieSyncManager shadowOf(CookieSyncManager instance) {
        return (ShadowCookieSyncManager) shadowOf_(instance);
    }

    public static ShadowContext shadowOf(Context instance) {
        return (ShadowContext) shadowOf_(instance);
    }

    public static ShadowContentValues shadowOf(ContentValues other) {
        return (ShadowContentValues) Robolectric.shadowOf_(other);
    }

    public static ShadowContextWrapper shadowOf(ContextWrapper instance) {
        return (ShadowContextWrapper) shadowOf_(instance);
    }

    public static ShadowCornerPathEffect shadowOf(CornerPathEffect instance) {
        return (ShadowCornerPathEffect) Robolectric.shadowOf_(instance);
    }

    public static ShadowCountDownTimer shadowOf(CountDownTimer instance) {
        return (ShadowCountDownTimer) Robolectric.shadowOf_(instance);
    }

    public static ShadowCursorAdapter shadowOf(CursorAdapter instance) {
        return (ShadowCursorAdapter) shadowOf_(instance);
    }

    public static ShadowCursorLoader shadowOf(CursorLoader instance) {
        return (ShadowCursorLoader) shadowOf_(instance);
    }

    public static ShadowCursorWrapper shadowOf(CursorWrapper instance) {
        return (ShadowCursorWrapper) shadowOf_(instance);
    }

    public static ShadowDateFormat shadowOf(DateFormat instance) {
        return (ShadowDateFormat) shadowOf_(instance);
    }

    public static ShadowDefaultRequestDirector shadowOf(DefaultRequestDirector instance) {
        return (ShadowDefaultRequestDirector) shadowOf_(instance);
    }

    public static ShadowDialog shadowOf(Dialog instance) {
        return (ShadowDialog) shadowOf_(instance);
    }

    public static ShadowDialogFragment shadowOf(DialogFragment instance) {
        return (ShadowDialogFragment) shadowOf_(instance);
    }

    public static ShadowDialogPreference shadowOf(DialogPreference instance) {
        return (ShadowDialogPreference) shadowOf_(instance);
    }

    public static ShadowEditTextPreference shadowOf(EditTextPreference instance) {
    	return (ShadowEditTextPreference) shadowOf_(instance);
    }

    public static ShadowDrawable shadowOf(Drawable instance) {
        return (ShadowDrawable) shadowOf_(instance);
    }

    public static ShadowDisplay shadowOf(Display instance) {
        return (ShadowDisplay) shadowOf_(instance);
    }

    public static ShadowExpandableListView shadowOf(ExpandableListView instance) {
        return (ShadowExpandableListView) shadowOf_(instance);
    }

    public static ShadowLocation shadowOf(Location instance) {
        return (ShadowLocation) shadowOf_(instance);
    }

    public static ShadowFilter shadowOf(Filter instance) {
        return (ShadowFilter) shadowOf_(instance);
    }

    public static ShadowFragment shadowOf(Fragment instance) {
        return (ShadowFragment) shadowOf_(instance);
    }

    public static ShadowFragmentActivity shadowOf(FragmentActivity instance) {
        return (ShadowFragmentActivity) shadowOf_(instance);
    }

    public static ShadowFragmentPagerAdapter shadowOf(FragmentPagerAdapter instance) {
        return (ShadowFragmentPagerAdapter) shadowOf_(instance);
    }

    public static ShadowFrameLayout shadowOf(FrameLayout instance) {
        return (ShadowFrameLayout) shadowOf_(instance);
    }

    public static ShadowGallery shadowOf(Gallery instance) {
        return (ShadowGallery) shadowOf_(instance);
    }

    public static ShadowGeocoder shadowOf(Geocoder instance) {
        return (ShadowGeocoder) shadowOf_(instance);
    }

    public static ShadowGestureDetector shadowOf(GestureDetector instance) {
        return (ShadowGestureDetector) shadowOf_(instance);
    }

    public static ShadowGridView shadowOf(GridView instance) {
        return (ShadowGridView) shadowOf_(instance);
    }

    public static ShadowHandler shadowOf(Handler instance) {
        return (ShadowHandler) shadowOf_(instance);
    }

    public static ShadowHandlerThread shadowOf(HandlerThread instance) {
        return (ShadowHandlerThread) shadowOf_(instance);
    }

    public static ShadowImageView shadowOf(ImageView instance) {
        return (ShadowImageView) shadowOf_(instance);
    }

    public static ShadowInputMethodManager shadowOf(InputMethodManager instance) {
        return (ShadowInputMethodManager) shadowOf_(instance);
    }

    public static ShadowInputDevice shadowOf(InputDevice instance) {
        return (ShadowInputDevice) shadowOf_(instance);
    }

    public static ShadowIntent shadowOf(Intent instance) {
        return (ShadowIntent) shadowOf_(instance);
    }

    public static ShadowJsPromptResult shadowOf(JsPromptResult instance) {
        return (ShadowJsPromptResult) shadowOf_(instance);
    }

    public static ShadowJsResult shadowOf(JsResult instance) {
        return (ShadowJsResult) shadowOf_(instance);
    }

    public static ShadowKeyEvent shadowOf(KeyEvent instance) {
        return (ShadowKeyEvent) shadowOf_(instance);
    }

    public static ShadowKeyguardManager shadowOf(KeyguardManager instance) {
        return (ShadowKeyguardManager) shadowOf_(instance);
    }

    public static ShadowKeyGuardLock shadowOf(KeyguardManager.KeyguardLock instance) {
        return (ShadowKeyGuardLock) shadowOf_(instance);
    }

    public static ShadowLayerDrawable shadowOf(LayerDrawable instance) {
        return (ShadowLayerDrawable) shadowOf_(instance);
    }

    public static ShadowLayoutInflater shadowOf(LayoutInflater instance) {
        return (ShadowLayoutInflater) shadowOf_(instance);
    }

    public static ShadowLinearGradient shadowOf(LinearGradient instance) {
        return (ShadowLinearGradient) shadowOf_(instance);
    }

    public static ShadowListActivity shadowOf(ListActivity instance) {
        return (ShadowListActivity) shadowOf_(instance);
    }

    public static ShadowListPreference shadowOf(ListPreference instance) {
        return (ShadowListPreference) shadowOf_(instance);
    }

    public static ShadowListView shadowOf(ListView instance) {
        return (ShadowListView) shadowOf_(instance);
    }

    public static ShadowLocationManager shadowOf(LocationManager instance) {
        return (ShadowLocationManager) shadowOf_(instance);
    }

    public static ShadowLooper shadowOf(Looper instance) {
        return (ShadowLooper) shadowOf_(instance);
    }

    public static ShadowMatrix shadowOf(Matrix other) {
        return (ShadowMatrix) Robolectric.shadowOf_(other);
    }

    public static ShadowMediaPlayer shadowOf(MediaPlayer instance) {
        return (ShadowMediaPlayer) shadowOf_(instance);
    }

    public static ShadowMediaRecorder shadowOf(MediaRecorder instance) {
        return (ShadowMediaRecorder) shadowOf_(instance);
    }

    public static ShadowMenuInflater shadowOf(MenuInflater instance) {
        return (ShadowMenuInflater) shadowOf_(instance);
    }

    public static ShadowMergeCursor shadowOf(MergeCursor instance) {
        return (ShadowMergeCursor) shadowOf_(instance);
    }
    
    public static ShadowMessage shadowOf(Message instance) {
        return (ShadowMessage) shadowOf_(instance);
    }

    public static ShadowMimeTypeMap shadowOf(MimeTypeMap instance) {
        return (ShadowMimeTypeMap) shadowOf_(instance);
    }

    public static ShadowMotionEvent shadowOf(MotionEvent other) {
        return (ShadowMotionEvent) Robolectric.shadowOf_(other);
    }

    public static ShadowNetworkInfo shadowOf(NetworkInfo instance) {
        return (ShadowNetworkInfo) shadowOf_(instance);
    }

    public static ShadowNotification shadowOf(Notification other) {
        return (ShadowNotification) Robolectric.shadowOf_(other);
    }

    public static ShadowNotificationManager shadowOf(NotificationManager other) {
        return (ShadowNotificationManager) Robolectric.shadowOf_(other);
    }

    public static ShadowPagerAdapter shadowOf(PagerAdapter instance) {
        return (ShadowPagerAdapter) shadowOf_(instance);
    }

    public static ShadowPaint shadowOf(Paint instance) {
        return (ShadowPaint) shadowOf_(instance);
    }

    public static ShadowParcel shadowOf(Parcel instance) {
        return (ShadowParcel) shadowOf_(instance);
    }

    public static ShadowPasswordTransformationMethod shadowOf(PasswordTransformationMethod instance) {
        return (ShadowPasswordTransformationMethod) shadowOf_(instance);
    }

    public static ShadowPath shadowOf(Path instance) {
        return (ShadowPath) shadowOf_(instance);
    }

    public static ShadowPendingIntent shadowOf(PendingIntent instance) {
        return (ShadowPendingIntent) shadowOf_(instance);
    }

    public static ShadowPhoneNumberUtils shadowOf(PhoneNumberUtils instance) {
        return (ShadowPhoneNumberUtils) shadowOf_(instance);
    }

    public static ShadowPopupWindow shadowOf(PopupWindow instance) {
        return (ShadowPopupWindow) shadowOf_(instance);
    }

    public static ShadowPowerManager shadowOf(PowerManager instance) {
        return (ShadowPowerManager) shadowOf_(instance);
    }

    public static ShadowPreference shadowOf(Preference instance) {
        return (ShadowPreference) shadowOf_(instance);
    }

    public static ShadowPreferenceActivity shadowOf(PreferenceActivity instance) {
        return (ShadowPreferenceActivity) shadowOf_(instance);
    }

    public static ShadowPreferenceCategory shadowOf(PreferenceCategory instance) {
        return (ShadowPreferenceCategory) shadowOf_(instance);
    }

    public static ShadowPreferenceGroup shadowOf(PreferenceGroup instance) {
        return (ShadowPreferenceGroup) shadowOf_(instance);
    }

    public static ShadowPreferenceScreen shadowOf(PreferenceScreen instance) {
        return (ShadowPreferenceScreen) shadowOf_(instance);
    }

    public static ShadowProgressBar shadowOf(ProgressBar instance) {
        return (ShadowProgressBar) shadowOf_(instance);
    }

    public static ShadowProgressDialog shadowOf(ProgressDialog instance) {
        return (ShadowProgressDialog) shadowOf_(instance);
    }

    public static ShadowRect shadowOf(Rect instance) {
        return (ShadowRect) shadowOf_(instance);
    }

    public static ShadowRatingBar shadowOf(RatingBar instance) {
        return (ShadowRatingBar) shadowOf_(instance);
    }

    public static ShadowRemoteViews shadowOf(RemoteViews instance) {
        return (ShadowRemoteViews) shadowOf_(instance);
    }

    public static ShadowResolveInfo shadowOf(ResolveInfo instance) {
        return (ShadowResolveInfo) shadowOf_(instance);
    }

    public static ShadowResourceCursorAdapter shadowOf(ResourceCursorAdapter instance) {
        return (ShadowResourceCursorAdapter) shadowOf_(instance);
    }

    public static ShadowResources shadowOf(Resources instance) {
        return (ShadowResources) shadowOf_(instance);
    }

    public static ShadowResultReceiver shadowOf(ResultReceiver instance) {
        return (ShadowResultReceiver) shadowOf_(instance);
    }

    public static ShadowScaleGestureDetector shadowOf(ScaleGestureDetector instance) {
        return (ShadowScaleGestureDetector) shadowOf_(instance);
    }

    public static ShadowScanResult shadowOf(ScanResult instance) {
        return (ShadowScanResult) shadowOf_(instance);
    }

    public static ShadowScroller shadowOf(Scroller instance) {
        return (ShadowScroller) shadowOf_(instance);
    }

    public static ShadowScrollView shadowOf(ScrollView instance) {
        return (ShadowScrollView) shadowOf_(instance);
    }

    public static ShadowSeekBar shadowOf(SeekBar instance) {
        return (ShadowSeekBar) shadowOf_(instance);
    }

    public static ShadowSensorManager shadowOf(SensorManager instance) {
        return (ShadowSensorManager) shadowOf_(instance);
    }

    public static ShadowService shadowOf(Service instance) {
        return (ShadowService) shadowOf_(instance);
    }

    public static ShadowShapeDrawable shadowOf(ShapeDrawable instance) {
        return (ShadowShapeDrawable) shadowOf_(instance);
    }

    public static ShadowSimpleCursorAdapter shadowOf(SimpleCursorAdapter instance) {
        return (ShadowSimpleCursorAdapter) shadowOf_(instance);
    }

    public static ShadowSmsManager shadowOf(SmsManager instance) {
        return (ShadowSmsManager) shadowOf_(instance);
    }

    public static <E> ShadowSparseArray<E> shadowOf(SparseArray<E> other) {
        //noinspection unchecked
        return (ShadowSparseArray<E>) Robolectric.shadowOf_(other);
    }

    public static ShadowSparseBooleanArray shadowOf(SparseBooleanArray other) {
        return (ShadowSparseBooleanArray) Robolectric.shadowOf_(other);
    }
    
    public static ShadowSparseIntArray shadowOf(SparseIntArray other){
    	return (ShadowSparseIntArray) Robolectric.shadowOf_( other );
    }

    public static ShadowSQLiteCursor shadowOf(SQLiteCursor other) {
        return (ShadowSQLiteCursor) Robolectric.shadowOf_(other);
    }

    public static ShadowSQLiteDatabase shadowOf(SQLiteDatabase other) {
        return (ShadowSQLiteDatabase) Robolectric.shadowOf_(other);
    }

    public static ShadowSQLiteOpenHelper shadowOf(SQLiteOpenHelper other) {
        return (ShadowSQLiteOpenHelper) Robolectric.shadowOf_(other);
    }

    public static ShadowSQLiteProgram shadowOf(SQLiteProgram other) {
        return (ShadowSQLiteProgram) Robolectric.shadowOf_(other);
    }

    public static ShadowSQLiteQueryBuilder shadowOf(SQLiteQueryBuilder other) {
        return (ShadowSQLiteQueryBuilder) Robolectric.shadowOf_(other);
    }

    public static ShadowSQLiteStatement shadowOf(SQLiteStatement other) {
        return (ShadowSQLiteStatement) Robolectric.shadowOf_(other);
    }

    public static ShadowSslErrorHandler shadowOf(SslErrorHandler instance) {
        return (ShadowSslErrorHandler) shadowOf_(instance);
    }

    public static ShadowStateListDrawable shadowOf(StateListDrawable instance) {
        return (ShadowStateListDrawable) shadowOf_(instance);
    }

    public static ShadowTabHost shadowOf(TabHost instance) {
        return (ShadowTabHost) shadowOf_(instance);
    }

    public static ShadowTabSpec shadowOf(TabHost.TabSpec instance) {
        return (ShadowTabSpec) shadowOf_(instance);
    }

    public static ShadowTelephonyManager shadowOf(TelephonyManager instance) {
        return (ShadowTelephonyManager) shadowOf_(instance);
    }

    public static ShadowTextPaint shadowOf(TextPaint instance) {
        return (ShadowTextPaint) shadowOf_(instance);
    }

    public static ShadowTextView shadowOf(TextView instance) {
        return (ShadowTextView) shadowOf_(instance);
    }

    public static ShadowResources.ShadowTheme shadowOf(Resources.Theme instance) {
        return (ShadowResources.ShadowTheme) shadowOf_(instance);
    }

    public static ShadowToast shadowOf(Toast instance) {
        return (ShadowToast) shadowOf_(instance);
    }

    public static ShadowTouchDelegate shadowOf( TouchDelegate instance ){
    	return (ShadowTouchDelegate) shadowOf_(instance);
    }
    
    public static ShadowTranslateAnimation shadowOf(TranslateAnimation instance) {
        return (ShadowTranslateAnimation) shadowOf_(instance);
    }

    public static ShadowTypedArray shadowOf(TypedArray instance) {
        return (ShadowTypedArray) shadowOf_(instance);
    }

    public static ShadowTypeface shadowOf(Typeface instance) {
        return (ShadowTypeface) shadowOf_(instance);
    }

    public static ShadowUriMatcher shadowOf(UriMatcher instance) {
        return (ShadowUriMatcher) shadowOf_(instance);
    }

    public static ShadowView shadowOf(View instance) {
        return (ShadowView) shadowOf_(instance);
    }

    public static ShadowViewAnimator shadowOf(ViewAnimator instance) {
        return (ShadowViewAnimator) shadowOf_(instance);
    }

    public static ShadowViewConfiguration shadowOf(ViewConfiguration instance) {
        return (ShadowViewConfiguration) shadowOf_(instance);
    }

    public static ShadowViewFlipper shadowOf(ViewFlipper instance) {
        return (ShadowViewFlipper) shadowOf_(instance);
    }

    public static ShadowViewPager shadowOf(ViewPager instance) {
        return (ShadowViewPager) shadowOf_(instance);
    }

    public static ShadowViewTreeObserver shadowOf(ViewTreeObserver instance) {
        return (ShadowViewTreeObserver) shadowOf_(instance);
    }

    public static ShadowViewGroup shadowOf(ViewGroup instance) {
        return (ShadowViewGroup) shadowOf_(instance);
    }

    public static ShadowVibrator shadowOf(Vibrator instance) {
        return (ShadowVibrator) shadowOf_(instance);
    }

    public static ShadowVideoView shadowOf(VideoView instance) {
        return (ShadowVideoView) shadowOf_(instance);
    }

    public static ShadowWebView shadowOf(WebView instance) {
        return (ShadowWebView) shadowOf_(instance);
    }

    public static ShadowWifiConfiguration shadowOf(WifiConfiguration instance) {
        return (ShadowWifiConfiguration) shadowOf_(instance);
    }

    public static ShadowWifiInfo shadowOf(WifiInfo instance) {
        return (ShadowWifiInfo) shadowOf_(instance);
    }

    public static ShadowWifiManager shadowOf(WifiManager instance) {
        return (ShadowWifiManager) shadowOf_(instance);
    }

    public static ShadowZoomButtonsController shadowOf(ZoomButtonsController instance) {
        return (ShadowZoomButtonsController) shadowOf_(instance);
    }

    @SuppressWarnings({"unchecked"})
    public static <P, R> P shadowOf_(R instance) {
        return (P) getShadowWrangler().shadowOf(instance);
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
     * @param headers      optional headers for the request
     */
    public static void addPendingHttpResponse(int statusCode, String responseBody, Header... headers) {
        getFakeHttpLayer().addPendingHttpResponse(statusCode, responseBody, headers);
    }

    /**
     * Sets up an HTTP response to be returned by calls to Apache's {@code HttpClient} implementers.
     *
     * @param statusCode   the status code of the response
     * @param responseBody the body of the response
     * @param contentType  the contentType of the response
     * @deprecated use {@link #addPendingHttpResponse(int, String, Header...)} instead
     */
    public static void addPendingHttpResponseWithContentType(int statusCode, String responseBody, Header contentType) {
        getFakeHttpLayer().addPendingHttpResponse(statusCode, responseBody, contentType);
    }

    /**
     * Sets up an HTTP response to be returned by calls to Apache's {@code HttpClient} implementers.
     *
     * @param httpResponse the response
     */
    public static void addPendingHttpResponse(HttpResponse httpResponse) {
        getFakeHttpLayer().addPendingHttpResponse(httpResponse);
    }

    /**
     * Sets up an HTTP response to be returned by calls to Apache's {@code HttpClient} implementers.
     *
     * @param httpResponseGenerator an HttpResponseGenerator that will provide responses
     */
    public static void addPendingHttpResponse(HttpResponseGenerator httpResponseGenerator) {
        getFakeHttpLayer().addPendingHttpResponse(httpResponseGenerator);
    }

    /**
     * Accessor to obtain HTTP requests made during the current test in the order in which they were made.
     *
     * @param index index of the request to retrieve.
     * @return the requested request.
     */
    public static HttpRequest getSentHttpRequest(int index) {
        return getFakeHttpLayer().getSentHttpRequestInfo(index).getHttpRequest();
    }

    public static HttpRequest getLatestSentHttpRequest() {
        return ShadowDefaultRequestDirector.getLatestSentHttpRequest();
    }

    /**
     * Accessor to find out if HTTP requests were made during the current test.
     *
     * @return whether a request was made.
     */
    public static boolean httpRequestWasMade() {
        return getShadowApplication().getFakeHttpLayer().hasRequestInfos();
    }

    public static boolean httpRequestWasMade(String uri) {
        return getShadowApplication().getFakeHttpLayer().hasRequestMatchingRule(new FakeHttpLayer.UriRequestMatcher(uri));
    }

    /**
     * Accessor to obtain metadata for an HTTP request made during the current test in the order in which they were made.
     *
     * @param index index of the request to retrieve.
     * @return the requested request metadata.
     */
    public static HttpRequestInfo getSentHttpRequestInfo(int index) {
        return getFakeHttpLayer().getSentHttpRequestInfo(index);
    }

    /**
     * Accessor to obtain HTTP requests made during the current test in the order in which they were made.
     *
     * @return the requested request or null if there are none.
     */
    public static HttpRequest getNextSentHttpRequest() {
        HttpRequestInfo httpRequestInfo = getFakeHttpLayer().getNextSentHttpRequestInfo();
        return httpRequestInfo == null ? null : httpRequestInfo.getHttpRequest();
    }

    /**
     * Accessor to obtain metadata for an HTTP request made during the current test in the order in which they were made.
     *
     * @return the requested request metadata or null if there are none.
     */
    public static HttpRequestInfo getNextSentHttpRequestInfo() {
        return getFakeHttpLayer().getNextSentHttpRequestInfo();
    }

    /**
     * Adds an HTTP response rule. The response will be returned when the rule is matched.
     *
     * @param method   method to match.
     * @param uri      uri to match.
     * @param response response to return when a match is found.
     */
    public static void addHttpResponseRule(String method, String uri, HttpResponse response) {
        getFakeHttpLayer().addHttpResponseRule(method, uri, response);
    }

    /**
     * Adds an HTTP response rule with a default method of GET. The response will be returned when the rule is matched.
     *
     * @param uri      uri to match.
     * @param response response to return when a match is found.
     */
    public static void addHttpResponseRule(String uri, HttpResponse response) {
        getFakeHttpLayer().addHttpResponseRule(uri, response);
    }

    /**
     * Adds an HTTP response rule. The response will be returned when the rule is matched.
     *
     * @param uri      uri to match.
     * @param response response to return when a match is found.
     */
    public static void addHttpResponseRule(String uri, String response) {
        getFakeHttpLayer().addHttpResponseRule(uri, response);
    }

    /**
     * Adds an HTTP response rule. The response will be returned when the rule is matched.
     *
     * @param requestMatcher custom {@code RequestMatcher}.
     * @param response       response to return when a match is found.
     */
    public static void addHttpResponseRule(RequestMatcher requestMatcher, HttpResponse response) {
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
    public static void addHttpResponseRule(RequestMatcher requestMatcher, List<? extends HttpResponse> responses) {
        getFakeHttpLayer().addHttpResponseRule(requestMatcher, responses);
    }

    public static FakeHttpLayer getFakeHttpLayer() {
        return getShadowApplication().getFakeHttpLayer();
    }

    public static void setDefaultHttpResponse(int statusCode, String responseBody) {
        getFakeHttpLayer().setDefaultHttpResponse(statusCode, responseBody);
    }

    public static void setDefaultHttpResponse(HttpResponse defaultHttpResponse) {
        getFakeHttpLayer().setDefaultHttpResponse(defaultHttpResponse);
    }

    public static void clearHttpResponseRules() {
        getFakeHttpLayer().clearHttpResponseRules();
    }

    public static void clearPendingHttpResponses() {
        getFakeHttpLayer().clearPendingHttpResponses();
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

    public static void idleMainLooper(long interval) {
        ShadowLooper.idleMainLooper(interval);
    }

    public static Scheduler getUiThreadScheduler() {
        return shadowOf(Looper.getMainLooper()).getScheduler();
    }

    public static Scheduler getBackgroundScheduler() {
        return getShadowApplication().getBackgroundScheduler();
    }

    public static ShadowApplication getShadowApplication() {
        return Robolectric.application == null ? null : shadowOf(Robolectric.application);
    }

    public static void setDisplayMetricsDensity(float densityMultiplier) {
        shadowOf(getShadowApplication().getResources()).setDensity(densityMultiplier);
    }

    public static void setDefaultDisplay(Display display) {
        shadowOf(getShadowApplication().getResources()).setDisplay(display);
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

    /**
     * Returns a textual representation of the appearance of the object.
     *
     * @param view the view to visualize
     */
    public static String visualize(View view) {
        Canvas canvas = new Canvas();
        view.draw(canvas);
        return shadowOf(canvas).getDescription();
    }

    /**
     * Returns a textual representation of the appearance of the object.
     *
     * @param canvas the canvas to visualize
     */
    public static String visualize(Canvas canvas) {
        return shadowOf(canvas).getDescription();
    }

    /**
     * Returns a textual representation of the appearance of the object.
     *
     * @param bitmap the bitmap to visualize
     */
    public static String visualize(Bitmap bitmap) {
        return shadowOf(bitmap).getDescription();
    }

    /**
     * Emits an xml-like representation of the view to System.out.
     *
     * @param view the view to dump
     */
    @SuppressWarnings("UnusedDeclaration")
    public static void dump(View view) {
        shadowOf(view).dump();
    }

    /**
     * Returns the text contained within this view.
     *
     * @param view the view to scan for text
     */
    @SuppressWarnings("UnusedDeclaration")
    public static String innerText(View view) {
        return shadowOf(view).innerText();
    }

    public static ResourceLoader getResourceLoader(Context context) {
        return shadowOf(context.getApplicationContext()).getResourceLoader();
    }

    /**
     * Reflection helper methods.
     */
    public static class Reflection {
        public static <T> T newInstanceOf(Class<T> clazz) {
            return Robolectric.newInstanceOf(clazz);
        }

        public static Object newInstanceOf(String className) {
            return Robolectric.newInstanceOf(className);
        }

        public static void setFinalStaticField(Class classWhichContainsField, String fieldName, Object newValue) {
            try {
                Field field = classWhichContainsField.getDeclaredField(fieldName);
                setFinalStaticField(field, newValue);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } 
        }

        public static Object setFinalStaticField(Field field, Object newValue) {
        	Object oldValue = null;
        	
            try {
            	field.setAccessible(true);

                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

               oldValue = field.get(null);
               field.set(null, newValue);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            
            return oldValue;
        }
    }

    // marker for shadow classes when the implementation class is unlinkable
    public interface Anything {
    }
}
