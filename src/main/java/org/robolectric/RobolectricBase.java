package org.robolectric;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.robolectric.shadows.ShadowAbsListView;
import org.robolectric.shadows.ShadowAbsSeekBar;
import org.robolectric.shadows.ShadowAbsSpinner;
import org.robolectric.shadows.ShadowAbsoluteLayout;
import org.robolectric.shadows.ShadowAbstractCursor;
import org.robolectric.shadows.ShadowAbstractWindowedCursor;
import org.robolectric.shadows.ShadowAccessibilityManager;
import org.robolectric.shadows.ShadowAccountManager;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowActivityGroup;
import org.robolectric.shadows.ShadowActivityManager;
import org.robolectric.shadows.ShadowActivityThread;
import org.robolectric.shadows.ShadowAdapterView;
import org.robolectric.shadows.ShadowAddress;
import org.robolectric.shadows.ShadowAlarmManager;
import org.robolectric.shadows.ShadowAlertController;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowAlphaAnimation;
import org.robolectric.shadows.ShadowAndroidBidi;
import org.robolectric.shadows.ShadowAndroidHttpClient;
import org.robolectric.shadows.ShadowAnimation;
import org.robolectric.shadows.ShadowAnimationUtils;
import org.robolectric.shadows.ShadowAnimator;
import org.robolectric.shadows.ShadowAppWidgetHost;
import org.robolectric.shadows.ShadowAppWidgetHostView;
import org.robolectric.shadows.ShadowAppWidgetManager;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowArrayAdapter;
import org.robolectric.shadows.ShadowAssetManager;
import org.robolectric.shadows.ShadowAsyncTask;
import org.robolectric.shadows.ShadowAudioManager;
import org.robolectric.shadows.ShadowBaseAdapter;
import org.robolectric.shadows.ShadowBinder;
import org.robolectric.shadows.ShadowBitmap;
import org.robolectric.shadows.ShadowBitmapDrawable;
import org.robolectric.shadows.ShadowBitmapFactory;
import org.robolectric.shadows.ShadowBitmapShader;
import org.robolectric.shadows.ShadowBluetoothAdapter;
import org.robolectric.shadows.ShadowBluetoothDevice;
import org.robolectric.shadows.ShadowBundle;
import org.robolectric.shadows.ShadowCamera;
import org.robolectric.shadows.ShadowCanvas;
import org.robolectric.shadows.ShadowChoreographer;
import org.robolectric.shadows.ShadowClipboardManager;
import org.robolectric.shadows.ShadowColor;
import org.robolectric.shadows.ShadowColorMatrix;
import org.robolectric.shadows.ShadowColorMatrixColorFilter;
import org.robolectric.shadows.ShadowColorStateList;
import org.robolectric.shadows.ShadowConfiguration;
import org.robolectric.shadows.ShadowConnectivityManager;
import org.robolectric.shadows.ShadowContentObserver;
import org.robolectric.shadows.ShadowContentProvider;
import org.robolectric.shadows.ShadowContentProviderOperation;
import org.robolectric.shadows.ShadowContentProviderResult;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.shadows.ShadowContentUris;
import org.robolectric.shadows.ShadowContext;
import org.robolectric.shadows.ShadowContextImpl;
import org.robolectric.shadows.ShadowContextThemeWrapper;
import org.robolectric.shadows.ShadowContextWrapper;
import org.robolectric.shadows.ShadowCookieManager;
import org.robolectric.shadows.ShadowCookieSyncManager;
import org.robolectric.shadows.ShadowCornerPathEffect;
import org.robolectric.shadows.ShadowCountDownTimer;
import org.robolectric.shadows.ShadowCriteria;
import org.robolectric.shadows.ShadowCursorAdapter;
import org.robolectric.shadows.ShadowCursorLoader;
import org.robolectric.shadows.ShadowCursorWrapper;
import org.robolectric.shadows.ShadowDashPathEffect;
import org.robolectric.shadows.ShadowDatabaseUtils;
import org.robolectric.shadows.ShadowDateFormat;
import org.robolectric.shadows.ShadowDatePickerDialog;
import org.robolectric.shadows.ShadowDebug;
import org.robolectric.shadows.ShadowDefaultRequestDirector;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowDialogPreference;
import org.robolectric.shadows.ShadowDisplay;
import org.robolectric.shadows.ShadowDownloadManager;
import org.robolectric.shadows.ShadowDrawable;
import org.robolectric.shadows.ShadowEdgeEffect;
import org.robolectric.shadows.ShadowEditTextPreference;
import org.robolectric.shadows.ShadowEmojiFactory;
import org.robolectric.shadows.ShadowEnvironment;
import org.robolectric.shadows.ShadowExifInterface;
import org.robolectric.shadows.ShadowExpandableListView;
import org.robolectric.shadows.ShadowFilter;
import org.robolectric.shadows.ShadowFloatMath;
import org.robolectric.shadows.ShadowFrameLayout;
import org.robolectric.shadows.ShadowGeoPoint;
import org.robolectric.shadows.ShadowGeocoder;
import org.robolectric.shadows.ShadowGestureDetector;
import org.robolectric.shadows.ShadowHandler;
import org.robolectric.shadows.ShadowHandlerThread;
import org.robolectric.shadows.ShadowImageView;
import org.robolectric.shadows.ShadowInputDevice;
import org.robolectric.shadows.ShadowInputEvent;
import org.robolectric.shadows.ShadowInputMethodManager;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowIntentFilter;
import org.robolectric.shadows.ShadowIntentSender;
import org.robolectric.shadows.ShadowIntentService;
import org.robolectric.shadows.ShadowItemizedOverlay;
import org.robolectric.shadows.ShadowJsPromptResult;
import org.robolectric.shadows.ShadowJsResult;
import org.robolectric.shadows.ShadowKeyCharacterMap;
import org.robolectric.shadows.ShadowKeyEvent;
import org.robolectric.shadows.ShadowKeyguardManager;
import org.robolectric.shadows.ShadowLayoutAnimationController;
import org.robolectric.shadows.ShadowLinearGradient;
import org.robolectric.shadows.ShadowLinearLayout;
import org.robolectric.shadows.ShadowLinkMovementMethod;
import org.robolectric.shadows.ShadowListPreference;
import org.robolectric.shadows.ShadowListView;
import org.robolectric.shadows.ShadowLocalActivityManager;
import org.robolectric.shadows.ShadowLocalBroadcastManager;
import org.robolectric.shadows.ShadowLocation;
import org.robolectric.shadows.ShadowLocationManager;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowMapActivity;
import org.robolectric.shadows.ShadowMapController;
import org.robolectric.shadows.ShadowMapView;
import org.robolectric.shadows.ShadowMatrix;
import org.robolectric.shadows.ShadowMatrixCursor;
import org.robolectric.shadows.ShadowMediaPlayer;
import org.robolectric.shadows.ShadowMediaRecorder;
import org.robolectric.shadows.ShadowMediaScannerConnection;
import org.robolectric.shadows.ShadowMediaStore;
import org.robolectric.shadows.ShadowMenuInflater;
import org.robolectric.shadows.ShadowMergeCursor;
import org.robolectric.shadows.ShadowMessenger;
import org.robolectric.shadows.ShadowMimeTypeMap;
import org.robolectric.shadows.ShadowMockPackageManager;
import org.robolectric.shadows.ShadowMotionEvent;
import org.robolectric.shadows.ShadowNdefMessage;
import org.robolectric.shadows.ShadowNdefRecord;
import org.robolectric.shadows.ShadowNetworkInfo;
import org.robolectric.shadows.ShadowNfcAdapter;
import org.robolectric.shadows.ShadowNinePatch;
import org.robolectric.shadows.ShadowNotification;
import org.robolectric.shadows.ShadowNotificationManager;
import org.robolectric.shadows.ShadowNumberPicker;
import org.robolectric.shadows.ShadowObjectAnimator;
import org.robolectric.shadows.ShadowOverlayItem;
import org.robolectric.shadows.ShadowPaint;
import org.robolectric.shadows.ShadowParcel;
import org.robolectric.shadows.ShadowPath;
import org.robolectric.shadows.ShadowPendingIntent;
import org.robolectric.shadows.ShadowPeriodicSync;
import org.robolectric.shadows.ShadowPhoneWindow;
import org.robolectric.shadows.ShadowPopupWindow;
import org.robolectric.shadows.ShadowPorterDuffXfermode;
import org.robolectric.shadows.ShadowPowerManager;
import org.robolectric.shadows.ShadowPreference;
import org.robolectric.shadows.ShadowPreferenceActivity;
import org.robolectric.shadows.ShadowPreferenceCategory;
import org.robolectric.shadows.ShadowPreferenceGroup;
import org.robolectric.shadows.ShadowPreferenceManager;
import org.robolectric.shadows.ShadowPreferenceScreen;
import org.robolectric.shadows.ShadowProgressBar;
import org.robolectric.shadows.ShadowProgressDialog;
import org.robolectric.shadows.ShadowRegion;
import org.robolectric.shadows.ShadowRelativeLayout;
import org.robolectric.shadows.ShadowRemoteCallbackList;
import org.robolectric.shadows.ShadowRemoteViews;
import org.robolectric.shadows.ShadowResolveInfo;
import org.robolectric.shadows.ShadowResourceCursorAdapter;
import org.robolectric.shadows.ShadowResources;
import org.robolectric.shadows.ShadowResultReceiver;
import org.robolectric.shadows.ShadowSQLiteClosable;
import org.robolectric.shadows.ShadowSQLiteCursor;
import org.robolectric.shadows.ShadowSQLiteDatabase;
import org.robolectric.shadows.ShadowSQLiteOpenHelper;
import org.robolectric.shadows.ShadowSQLiteProgram;
import org.robolectric.shadows.ShadowSQLiteQueryBuilder;
import org.robolectric.shadows.ShadowSQLiteStatement;
import org.robolectric.shadows.ShadowScaleGestureDetector;
import org.robolectric.shadows.ShadowScanResult;
import org.robolectric.shadows.ShadowScrollView;
import org.robolectric.shadows.ShadowScroller;
import org.robolectric.shadows.ShadowSeekBar;
import org.robolectric.shadows.ShadowSensorEvent;
import org.robolectric.shadows.ShadowSensorManager;
import org.robolectric.shadows.ShadowService;
import org.robolectric.shadows.ShadowServiceManager;
import org.robolectric.shadows.ShadowSettings;
import org.robolectric.shadows.ShadowSimpleCursorAdapter;
import org.robolectric.shadows.ShadowSmsManager;
import org.robolectric.shadows.ShadowSpannableStringBuilder;
import org.robolectric.shadows.ShadowSpellChecker;
import org.robolectric.shadows.ShadowSslErrorHandler;
import org.robolectric.shadows.ShadowStatFs;
import org.robolectric.shadows.ShadowStateListDrawable;
import org.robolectric.shadows.ShadowSurface;
import org.robolectric.shadows.ShadowSurfaceView;
import org.robolectric.shadows.ShadowSyncResult;
import org.robolectric.shadows.ShadowSyncStats;
import org.robolectric.shadows.ShadowSystemClock;
import org.robolectric.shadows.ShadowSystemProperties;
import org.robolectric.shadows.ShadowTabActivity;
import org.robolectric.shadows.ShadowTabHost;
import org.robolectric.shadows.ShadowTabWidget;
import org.robolectric.shadows.ShadowTelephonyManager;
import org.robolectric.shadows.ShadowTextPaint;
import org.robolectric.shadows.ShadowTextView;
import org.robolectric.shadows.ShadowTime;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.shadows.ShadowTouchDelegate;
import org.robolectric.shadows.ShadowTranslateAnimation;
import org.robolectric.shadows.ShadowTypedArray;
import org.robolectric.shadows.ShadowTypeface;
import org.robolectric.shadows.ShadowURLSpan;
import org.robolectric.shadows.ShadowUri;
import org.robolectric.shadows.ShadowUriMatcher;
import org.robolectric.shadows.ShadowValueAnimator;
import org.robolectric.shadows.ShadowVideoView;
import org.robolectric.shadows.ShadowView;
import org.robolectric.shadows.ShadowViewAnimator;
import org.robolectric.shadows.ShadowViewConfiguration;
import org.robolectric.shadows.ShadowViewGroup;
import org.robolectric.shadows.ShadowViewRootImpl;
import org.robolectric.shadows.ShadowViewTreeObserver;
import org.robolectric.shadows.ShadowWebSyncManager;
import org.robolectric.shadows.ShadowWebView;
import org.robolectric.shadows.ShadowWebViewDatabase;
import org.robolectric.shadows.ShadowWifiConfiguration;
import org.robolectric.shadows.ShadowWifiInfo;
import org.robolectric.shadows.ShadowWifiManager;
import org.robolectric.shadows.ShadowWindow;
import org.robolectric.shadows.ShadowWindowManager;
import org.robolectric.shadows.ShadowWindowManagerImpl;
import org.robolectric.shadows.ShadowZoomButtonsController;

public class RobolectricBase {
  static final List<Class<?>> DEFAULT_SHADOW_CLASSES = Collections.unmodifiableList(Arrays.asList(
      ShadowAbsListView.class,
      ShadowAbsoluteLayout.class,
      ShadowAbsoluteLayout.ShadowLayoutParams.class,
      ShadowAbsSeekBar.class,
      ShadowAbsSpinner.class,
      ShadowAbstractCursor.class,
      ShadowAbstractWindowedCursor.class,
      ShadowAccessibilityManager.class,
      ShadowAccountManager.class,
      ShadowActivity.class,
      ShadowActivityGroup.class,
      ShadowActivityManager.class,
      ShadowActivityThread.class,
      ShadowAdapterView.class,
      ShadowAddress.class,
      ShadowAlarmManager.class,
      ShadowAlertController.class,
      ShadowAlertDialog.class,
      ShadowAlertDialog.ShadowBuilder.class,
      ShadowAlphaAnimation.class,
      ShadowAndroidBidi.class,
      ShadowAndroidHttpClient.class,
      ShadowAnimation.class,
      ShadowAnimationUtils.class,
      ShadowAnimator.class,
      ShadowApplication.class,
      ShadowAppWidgetHost.class,
      ShadowAppWidgetHostView.class,
      ShadowAppWidgetManager.class,
      ShadowArrayAdapter.class,
      ShadowAssetManager.class,
      ShadowAsyncTask.class,
      ShadowAudioManager.class,
      ShadowBaseAdapter.class,
      ShadowBinder.class,
      ShadowBitmap.class,
      ShadowBitmapDrawable.class,
      ShadowBitmapFactory.class,
      ShadowBitmapShader.class,
      ShadowBluetoothAdapter.class,
      ShadowBluetoothDevice.class,
      ShadowBundle.class,
      ShadowCamera.class,
      ShadowCamera.ShadowParameters.class,
      ShadowCamera.ShadowSize.class,
      ShadowCanvas.class,
      ShadowChoreographer.class,
      ShadowClipboardManager.class,
      ShadowColor.class,
      ShadowColorMatrix.class,
      ShadowColorMatrixColorFilter.class,
      ShadowColorStateList.class,
      ShadowConfiguration.class,
      ShadowConnectivityManager.class,
      ShadowContentObserver.class,
      ShadowContentProvider.class,
      ShadowContentProviderOperation.class,
      ShadowContentProviderResult.class,
      ShadowContentResolver.class,
      ShadowContentUris.class,
      ShadowContext.class,
      ShadowContextImpl.class,
      ShadowContextImpl.ShadowServiceFetcher.class,
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
      ShadowDashPathEffect.class,
      ShadowDatabaseUtils.class,
      ShadowDateFormat.class,
      ShadowDatePickerDialog.class,
      ShadowDebug.class,
      ShadowDefaultRequestDirector.class,
      ShadowDisplay.class,
      ShadowDrawable.class,
      ShadowDialog.class,
      ShadowDialogPreference.class,
      ShadowDownloadManager.class,
      ShadowDownloadManager.ShadowRequest.class,
      ShadowDownloadManager.ShadowQuery.class,
      ShadowEdgeEffect.class,
      ShadowEditTextPreference.class,
      ShadowEmojiFactory.class,
      ShadowEnvironment.class,
      ShadowExifInterface.class,
      ShadowExpandableListView.class,
      ShadowFilter.class,
      ShadowFloatMath.class,
      ShadowFrameLayout.class,
      ShadowGeocoder.class,
      ShadowGeoPoint.class,
      ShadowGestureDetector.class,
      ShadowHandler.class,
      ShadowHandlerThread.class,
      ShadowImageView.class,
      ShadowInputMethodManager.class,
      ShadowInputDevice.class,
      ShadowInputEvent.class,
      ShadowIntent.class,
      ShadowIntentService.class,
      ShadowIntentFilter.class,
      ShadowIntentFilter.ShadowAuthorityEntry.class,
      ShadowIntentSender.class,
      ShadowItemizedOverlay.class,
      ShadowLayoutAnimationController.class,
      ShadowJsPromptResult.class,
      ShadowJsResult.class,
      ShadowKeyCharacterMap.class,
      ShadowKeyEvent.class,
      ShadowKeyguardManager.class,
      ShadowKeyguardManager.ShadowKeyguardLock.class,
      ShadowViewGroup.ShadowLayoutParams.class,
      ShadowLinearGradient.class,
      ShadowLinearLayout.class,
      ShadowLinkMovementMethod.class,
      ShadowListPreference.class,
      ShadowListView.class,
      ShadowLocalActivityManager.class,
      ShadowLocalBroadcastManager.class,
      ShadowLocation.class,
      ShadowLocationManager.class,
      ShadowLog.class,
      ShadowLooper.class,
      ShadowMapController.class,
      ShadowMapActivity.class,
      ShadowMapView.class,
      ShadowViewGroup.ShadowMarginLayoutParams.class,
      ShadowMatrix.class,
      ShadowMatrixCursor.class,
      ShadowMediaPlayer.class,
      ShadowMediaRecorder.class,
      ShadowMediaScannerConnection.class,
      ShadowMediaStore.ShadowImages.ShadowMedia.class,
      ShadowMenuInflater.class,
      ShadowMergeCursor.class,
      ShadowMessenger.class,
      ShadowMimeTypeMap.class,
      ShadowMockPackageManager.class,
      ShadowMotionEvent.class,
      ShadowNotification.class,
      ShadowNdefMessage.class,
      ShadowNdefRecord.class,
      ShadowNfcAdapter.class,
      ShadowNotification.ShadowBuilder.class,
      ShadowNotificationManager.class,
      ShadowNetworkInfo.class,
      ShadowNinePatch.class,
      ShadowNumberPicker.class,
      ShadowOverlayItem.class,
      ShadowObjectAnimator.class,
      ShadowPaint.class,
      ShadowParcel.class,
      ShadowPath.class,
      ShadowPendingIntent.class,
      ShadowPeriodicSync.class,
      ShadowPhoneWindow.class,
      ShadowPopupWindow.class,
      ShadowPorterDuffXfermode.class,
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
      ShadowRegion.class,
      ShadowRelativeLayout.class,
      ShadowRelativeLayout.ShadowLayoutParams.class,
      ShadowResolveInfo.class,
      ShadowRemoteCallbackList.class,
      ShadowRemoteViews.class,
      ShadowResultReceiver.class,
      ShadowResourceCursorAdapter.class,
      ShadowResources.class,
      ShadowResources.ShadowNotFoundException.class,
      ShadowResources.ShadowTheme.class,
      ShadowScaleGestureDetector.class,
      ShadowScanResult.class,
      ShadowScrollView.class,
      ShadowScroller.class,
      ShadowSeekBar.class,
      ShadowSensorEvent.class,
      ShadowSensorManager.class,
      ShadowService.class,
      ShadowServiceManager.class,
      ShadowSettings.class,
      ShadowSettings.ShadowSecure.class,
      ShadowSettings.ShadowSystem.class,
      ShadowSimpleCursorAdapter.class,
      ShadowSmsManager.class,
      ShadowSpannableStringBuilder.class,
      ShadowSpellChecker.class,
      ShadowSyncResult.class,
      ShadowSyncStats.class,
      ShadowSQLiteProgram.class,
      ShadowSQLiteClosable.class,
      ShadowSQLiteDatabase.class,
      ShadowSQLiteCursor.class,
      ShadowSQLiteOpenHelper.class,
      ShadowSQLiteStatement.class,
      ShadowSQLiteQueryBuilder.class,
      ShadowSslErrorHandler.class,
      ShadowStateListDrawable.class,
      ShadowStatFs.class,
      ShadowSurface.class,
      ShadowSurfaceView.class,
      ShadowSystemClock.class,
      ShadowSystemProperties.class,
      ShadowTabActivity.class,
      ShadowTabHost.class,
      ShadowTabHost.ShadowTabSpec.class,
      ShadowTabWidget.class,
      ShadowTelephonyManager.class,
      ShadowTextPaint.class,
      ShadowTextView.class,
      ShadowTime.class,
      ShadowToast.class,
      ShadowTouchDelegate.class,
      ShadowTranslateAnimation.class,
      ShadowTypedArray.class,
      ShadowTypeface.class,
      ShadowUriMatcher.class,
      ShadowUri.class,
      ShadowURLSpan.class,
      ShadowValueAnimator.class,
      ShadowVideoView.class,
      ShadowView.class,
      ShadowViewAnimator.class,
      ShadowViewConfiguration.class,
      ShadowViewGroup.class,
      ShadowViewRootImpl.class,
      ShadowViewTreeObserver.class,
      ShadowWebView.class,
      ShadowWebViewDatabase.class,
      ShadowWebSyncManager.class,
      ShadowWifiConfiguration.class,
      ShadowWifiInfo.class,
      ShadowWifiManager.class,
      ShadowWifiManager.ShadowWifiLock.class,
      ShadowWindow.class,
      ShadowWindowManager.class,
      ShadowWindowManagerImpl.class,
      ShadowZoomButtonsController.class
  ));
}
