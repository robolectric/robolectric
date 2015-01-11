package org.robolectric;

import android.app.Activity;
import android.app.Service;
import android.os.Looper;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultRequestDirector;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.internal.bytecode.RobolectricInternals;
import org.robolectric.res.ResourceLoader;
import org.robolectric.util.ActivityController;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Scheduler;
import org.robolectric.util.ServiceController;
import org.robolectric.util.ShadowProvider;

//
// Imports for shims
//
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.*;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.database.CursorWrapper;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
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
import android.net.http.HttpResponseCache;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
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
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.content.ClipboardManager;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.view.*;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.*;
import android.view.inputmethod.InputMethodManager;
import android.webkit.*;
import android.widget.*;
import org.robolectric.shadows.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ServiceLoader;

public class Robolectric {
  private static final ShadowsAdapter shadowsAdapter = instantiateShadowsAdapter();

  public static void reset() {
    RuntimeEnvironment.application = null;
    Robolectric.application = null;
    RuntimeEnvironment.setRobolectricPackageManager(null);
    RuntimeEnvironment.setActivityThread(null);

    for (ShadowProvider provider : ServiceLoader.load(ShadowProvider.class)) {
      provider.reset();
    }
  }

  public static ShadowsAdapter getShadowsAdapter() {
    return shadowsAdapter;
  }

  public static <T extends Service> ServiceController<T> buildService(Class<T> serviceClass) {
    return ServiceController.of(shadowsAdapter, serviceClass);
  }

  public static <T extends Service> T setupService(Class<T> serviceClass) {
    return ServiceController.of(shadowsAdapter, serviceClass).attach().create().get();
  }

  public static <T extends Activity> ActivityController<T> buildActivity(Class<T> activityClass) {
    return ActivityController.of(shadowsAdapter, activityClass);
  }

  public static <T extends Activity> T setupActivity(Class<T> activityClass) {
    return ActivityController.of(shadowsAdapter, activityClass).setup().get();
  }

  private static ShadowsAdapter instantiateShadowsAdapter() {
    ShadowsAdapter result = null;
    for (ShadowsAdapter adapter : ServiceLoader.load(ShadowsAdapter.class)) {
      if (result == null) {
        result = adapter;
      } else {
        throw new RuntimeException("Multiple " + ShadowsAdapter.class.getCanonicalName() + "s found.  Robolectric has loaded multiple core shadow modules for some reason.");
      }
    }
    if (result == null) {
      throw new RuntimeException("No shadows modules found containing a " + ShadowsAdapter.class.getCanonicalName());
    } else {
      return result;
    }
  }

  ///
  /// Below here is contained shim methods as an interim to migration off of.
  ///

  
        /**
      * Runs any background tasks previously queued by {@link android.os.AsyncTask#execute(Object[])}.
      * <p/>
      * <p/>
      * Note: calling this method does not pause or unpause the scheduler.
      */
        public static void runBackgroundTasks() {
        getBackgroundScheduler().advanceBy(0);
      }
  
        public static Scheduler getBackgroundScheduler() {
        return ShadowApplication.getInstance().getBackgroundScheduler();
      }

        public static void setDisplayMetricsDensity(float densityMultiplier) {
        Shadows.shadowOf(getShadowApplication().getResources()).setDensity(densityMultiplier);
      }
  
        public static void setDefaultDisplay(Display display) {
        Shadows.shadowOf(getShadowApplication().getResources()).setDisplay(display);
      }

  
        /**
      * Returns a textual representation of the appearance of the object.
      *
      * @param view the view to visualize
      */
        public static String visualize(View view) {
        Canvas canvas = new Canvas();
        view.draw(canvas);
        return Shadows.shadowOf(canvas).getDescription();
      }
  
        /**
      * Returns a textual representation of the appearance of the object.
      *
      * @param canvas the canvas to visualize
      */
        public static String visualize(Canvas canvas) {
        return Shadows.shadowOf(canvas).getDescription();
      }
  
        /**
      * Returns a textual representation of the appearance of the object.
      *
      * @param bitmap the bitmap to visualize
      */
        public static String visualize(Bitmap bitmap) {
        return Shadows.shadowOf(bitmap).getDescription();
      }
  
        /**
      * Emits an xmllike representation of the view to System.out.
      *
      * @param view the view to dump
      */
        @SuppressWarnings("UnusedDeclaration")
    public static void dump(View view) {
        org.robolectric.Shadows.shadowOf(view).dump();
      }
  
        /**
      * Returns the text contained within this view.
      *
      * @param view the view to scan for text
      */
        @SuppressWarnings("UnusedDeclaration")
    public static String innerText(View view) {
        return Shadows.shadowOf(view).innerText();
      }
  
        public static ResourceLoader getResourceLoader() {
        return ShadowApplication.getInstance().getResourceLoader();
      }
  

    /**
        * Set to true if you'd like Robolectric to strictly simulate the real Android behavior when
        * calling {@link Context#startActivity(android.content.Intent)}. Real Android throws a
        * {@link android.content.ActivityNotFoundException} if given
        * an {@link Intent} that is not known to the {@link android.content.pm.PackageManager}
        *
        * By default, this behavior is off (false).
        *
        * @param checkActivities
        */
      public static void checkActivities(boolean checkActivities) {
          Shadows.shadowOf(RuntimeEnvironment.application).checkActivities(checkActivities);
        }



  /**
   * @deprecated Use {@link org.robolectric.shadows.ShadowLooper#getUiThreadScheduler()} and
   * {org.robolectric.util.Scheduler#runUiThreadTasks()} instead
   */
  @Deprecated
  public static void runUiThreadTasks() {
    getUiThreadScheduler().advanceBy(0);
  }

  /**
   * @deprecated Use {@link org.robolectric.shadows.ShadowLooper#getUiThreadScheduler()} and
   * {org.robolectric.util.Scheduler#advanceToLastPostedRunnable()} instead
   */
  @Deprecated
  public static void runUiThreadTasksIncludingDelayedTasks() {
    getUiThreadScheduler().advanceToLastPostedRunnable();
  }

  /**
   * @deprecated Use {@link org.robolectric.shadows.ShadowLooper#pauseLooper(android.os.Looper)}  instead
   */
  @Deprecated
  public static void pauseLooper(Looper looper) {
    ShadowLooper.pauseLooper(looper);
  }

  /**
   * @deprecated Use {@link org.robolectric.shadows.ShadowLooper#unPauseLooper(android.os.Looper)} instead
   */
  @Deprecated
  public static void unPauseLooper(Looper looper) {
    ShadowLooper.unPauseLooper(looper);
  }

  /**
   * @deprecated Use {@link org.robolectric.shadows.ShadowLooper#pauseMainLooper()} instead
   */
  @Deprecated
  public static void pauseMainLooper() {
    ShadowLooper.pauseMainLooper();
  }

  /**
   * @deprecated Use {@link org.robolectric.shadows.ShadowLooper#unPauseMainLooper()} instead
   */
  @Deprecated
  public static void unPauseMainLooper() {
    ShadowLooper.unPauseMainLooper();
  }

  /**
   * @deprecated Use {@link org.robolectric.shadows.ShadowLooper#idleMainLooper(long)} instead
   */
  @Deprecated
  public static void idleMainLooper(long interval) {
    ShadowLooper.idleMainLooper(interval);
  }

  /**
   * @deprecated Use {@link org.robolectric.shadows.ShadowLooper#idleMainLooperConstantly(boolean)} instead
   */
  @Deprecated
  public static void idleMainLooperConstantly(boolean shouldIdleConstantly) {
    ShadowLooper.idleMainLooperConstantly(shouldIdleConstantly);
  }

  /**
   * @deprecated Use {@link org.robolectric.shadows.ShadowLooper#getUiThreadScheduler()} instead
   */
  @Deprecated
  public static Scheduler getUiThreadScheduler() {
    return ShadowLooper.getUiThreadScheduler();
  }
  
  /**
   * @deprecated Use {@link org.robolectric.RuntimeEnvironment#application} instead
   */
  @Deprecated
  public static Application application;

  /**
   * @deprecated Use {@link org.robolectric.shadows.ShadowApplication#getInstance()} instead
   */
  @Deprecated
  public static ShadowApplication getShadowApplication() {
    return ShadowApplication.getInstance();
  }

  /**
   * @deprecated Use {@link org.robolectric.shadows.ShadowView#clickOn(android.view.View)} instead
   */
  @Deprecated
  public static boolean clickOn(View view) {
    return ShadowView.clickOn(view);
  }

  public static <T> T newInstanceOf(Class<T> clazz) {
    return ReflectionHelpers.callConstructor(clazz);
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
    return ReflectionHelpers.callConstructor(clazz, ReflectionHelpers.ClassParameter.fromComponentLists(parameterTypes, params));
  }

//  public static <T> T directlyOn(T shadowedObject, Class<T> clazz) {
//    return ReflectionHelpers.callConstructor(clazz, ReflectionHelpers.ClassParameter.fromComponentLists(new Class[]{DirectObjectMarker.class, clazz}, new Object[]{DirectObjectMarker.INSTANCE, shadowedObject}));
//  }

  public static <R> R directlyOn(Object shadowedObject, String clazzName, String methodName, ReflectionHelpers.ClassParameter... paramValues) {
    try {
      Class<Object> aClass = (Class<Object>) shadowedObject.getClass().getClassLoader().loadClass(clazzName);
      return directlyOn(shadowedObject, aClass, methodName, paramValues);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static <R, T> R directlyOn(T shadowedObject, Class<T> clazz, String methodName, ReflectionHelpers.ClassParameter... paramValues) {
    //String directMethodName = RobolectricInternals.directMethodName(clazz.getName(), methodName);
    return ReflectionHelpers.callInstanceMethod(shadowedObject, methodName, paramValues);
  }

  public static <R, T> R directlyOn(Class<T> clazz, String methodName, ReflectionHelpers.ClassParameter... paramValues) {
//    String directMethodName = RobolectricInternals.directMethodName(clazz.getName(), methodName);
    return ReflectionHelpers.callStaticMethod(clazz, methodName, paramValues);
  }

//  public static <R> R invokeConstructor(Class<? extends R> clazz, R instance, ReflectionHelpers.StringParameter paramValue0, ReflectionHelpers.StringParameter... paramValues) {
//    ReflectionHelpers.ClassParameter[] classParamValues = new ReflectionHelpers.ClassParameter[paramValues.length + 1];
//    try {
//      Class<?> paramClass = clazz.getClassLoader().loadClass(paramValue0.className);
//      classParamValues[0] = new ReflectionHelpers.ClassParameter(paramClass, paramValue0.val);
//    } catch (ClassNotFoundException e) {
//      throw new RuntimeException(e);
//    }
//    for (int i = 0; i < paramValues.length; i++) {
//      try {
//        Class<?> paramClass = clazz.getClassLoader().loadClass(paramValues[i].className);
//        classParamValues[i + 1] = new ReflectionHelpers.ClassParameter(paramClass, paramValues[i].val);
//      } catch (ClassNotFoundException e) {
//        throw new RuntimeException(e);
//      }
//    }
//    return invokeConstructor(clazz, instance, classParamValues);
//  }

//  public static <R> R invokeConstructor(Class<? extends R> clazz, R instance, ReflectionHelpers.ClassParameter... paramValues) {
//    String directMethodName = RobolectricInternals.directMethodName(clazz.getName(), InstrumentingClassLoader.CONSTRUCTOR_METHOD_NAME);
//    return ReflectionHelpers.callInstanceMethod(instance, directMethodName, paramValues);
//  }

  /**
   * @deprecated Please use {@link org.robolectric.util.ReflectionHelpers} instead
   */
  @Deprecated
  public static class Reflection {
    public static <T> T newInstanceOf(Class<T> clazz) {
      return Robolectric.newInstanceOf(clazz);
    }

    public static Object newInstanceOf(String className) {
      return Robolectric.newInstanceOf(className);
    }

    public static void setFinalStaticField(Class classWhichContainsField, String fieldName, Object newValue) {
      ReflectionHelpers.setStaticField(classWhichContainsField, fieldName, newValue);
    }

    public static Object setFinalStaticField(Field field, Object newValue) {
      Object oldValue;

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

  public static ShadowAbsListView shadowOf(AbsListView instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowAbsSeekBar shadowOf(AbsSeekBar instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowAccessibilityManager shadowOf(AccessibilityManager instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowAccountManager shadowOf(AccountManager instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowActivity shadowOf(Activity instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowActivityGroup shadowOf(ActivityGroup instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowActivityManager shadowOf(ActivityManager instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowAdapterView shadowOf(AdapterView instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowAddress shadowOf(Address instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowAlarmManager shadowOf(AlarmManager instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowAlertDialog shadowOf(AlertDialog instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowAlphaAnimation shadowOf(AlphaAnimation instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowAnimation shadowOf(Animation instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowLayoutAnimationController shadowOf(LayoutAnimationController instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowAnimationUtils shadowOf(AnimationUtils instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowAnimator shadowOf(Animator instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowApplication shadowOf(Application instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowAppWidgetHost shadowOf(AppWidgetHost instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowAppWidgetHostView shadowOf(AppWidgetHostView instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowAppWidgetManager shadowOf(AppWidgetManager instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowArrayAdapter shadowOf(ArrayAdapter instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowAssetManager shadowOf(AssetManager instance) {
    return Shadows.shadowOf(instance);
  }

  @SuppressWarnings("rawtypes")
  public static ShadowAsyncTask shadowOf(AsyncTask instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowAudioManager shadowOf(AudioManager instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowBaseAdapter shadowOf(BaseAdapter other) {
    return Shadows.shadowOf(other);
  }

  public static ShadowBitmap shadowOf(Bitmap other) {
    return Shadows.shadowOf(other);
  }

  public static ShadowBitmapDrawable shadowOf(BitmapDrawable instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowBluetoothAdapter shadowOf(BluetoothAdapter other) {
    return Shadows.shadowOf(other);
  }

  public static ShadowBluetoothDevice shadowOf(BluetoothDevice other) {
    return Shadows.shadowOf(other);
  }

  public static ShadowBundle shadowOf(Bundle instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowCamera shadowOf(Camera instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowCamera.ShadowParameters shadowOf(Camera.Parameters instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowCamera.ShadowSize shadowOf(Camera.Size instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowCanvas shadowOf(Canvas instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowClipboardManager shadowOf(ClipboardManager instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowColor shadowOf(Color instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowColorMatrix shadowOf(ColorMatrix instance) {
    return (ShadowColorMatrix) Shadows.shadowOf(instance);
  }

  public static ShadowConfiguration shadowOf(Configuration instance) {
    return (ShadowConfiguration) Shadows.shadowOf(instance);
  }

  public static ShadowConnectivityManager shadowOf(ConnectivityManager instance) {
    return (ShadowConnectivityManager) Shadows.shadowOf(instance);
  }

  public static ShadowCookieManager shadowOf(CookieManager instance) {
    return (ShadowCookieManager) Shadows.shadowOf(instance);
  }

  public static ShadowContentObserver shadowOf(ContentObserver instance) {
    return (ShadowContentObserver) Shadows.shadowOf(instance);
  }

  public static ShadowContentResolver shadowOf(ContentResolver instance) {
    return (ShadowContentResolver) Shadows.shadowOf(instance);
  }

  public static ShadowContentProviderClient shadowOf(ContentProviderClient client) {
    return (ShadowContentProviderClient) Shadows.shadowOf(client);
  }

  public static ShadowContentProviderOperation shadowOf(ContentProviderOperation instance) {
    return (ShadowContentProviderOperation) Shadows.shadowOf(instance);
  }

  public static ShadowContentProviderResult shadowOf(ContentProviderResult instance) {
    return (ShadowContentProviderResult) Shadows.shadowOf(instance);
  }

  public static ShadowCookieSyncManager shadowOf(CookieSyncManager instance) {
    return (ShadowCookieSyncManager) Shadows.shadowOf(instance);
  }

  public static ShadowContext shadowOf(Context instance) {
    return (ShadowContext) Shadows.shadowOf(instance);
  }

  public static ShadowContextWrapper shadowOf(ContextWrapper instance) {
    return (ShadowContextWrapper) Shadows.shadowOf(instance);
  }

  public static ShadowCornerPathEffect shadowOf(CornerPathEffect instance) {
    return (ShadowCornerPathEffect) Shadows.shadowOf(instance);
  }

  public static ShadowCountDownTimer shadowOf(CountDownTimer instance) {
    return (ShadowCountDownTimer) Shadows.shadowOf(instance);
  }

  public static ShadowCursorAdapter shadowOf(CursorAdapter instance) {
    return (ShadowCursorAdapter) Shadows.shadowOf(instance);
  }

  public static ShadowCursorWrapper shadowOf(CursorWrapper instance) {
    return (ShadowCursorWrapper) Shadows.shadowOf(instance);
  }

  public static ShadowDateFormat shadowOf(DateFormat instance) {
    return (ShadowDateFormat) Shadows.shadowOf(instance);
  }

  public static ShadowDatePickerDialog shadowOf(DatePickerDialog instance) {
    return (ShadowDatePickerDialog) Shadows.shadowOf(instance);
  }
//
//  public static ShadowDefaultRequestDirector shadowOf(DefaultRequestDirector instance) {
//    return (ShadowDefaultRequestDirector) Shadows.shadowOf(instance);
//  }

  public static ShadowDialog shadowOf(Dialog instance) {
    return (ShadowDialog) Shadows.shadowOf(instance);
  }

//  public static ShadowDialogPreference shadowOf(DialogPreference instance) {
//    return (ShadowDialogPreference) Shadows.shadowOf(instance);
//  }

  public static ShadowDrawable shadowOf(Drawable instance) {
    return (ShadowDrawable) Shadows.shadowOf(instance);
  }

  public static ShadowDisplay shadowOf(Display instance) {
    return (ShadowDisplay) Shadows.shadowOf(instance);
  }

  public static ShadowExpandableListView shadowOf(ExpandableListView instance) {
    return (ShadowExpandableListView) Shadows.shadowOf(instance);
  }

//  public static ShadowLocalBroadcastManager shadowOf(LocalBroadcastManager instance) {
//    return (ShadowLocalBroadcastManager) Shadows.shadowOf(instance);
//  }

  public static ShadowLocation shadowOf(Location instance) {
    return (ShadowLocation) Shadows.shadowOf(instance);
  }

  public static ShadowFilter shadowOf(Filter instance) {
    return (ShadowFilter) Shadows.shadowOf(instance);
  }

  public static ShadowFrameLayout shadowOf(FrameLayout instance) {
    return (ShadowFrameLayout) Shadows.shadowOf(instance);
  }

//  public static ShadowGeocoder shadowOf(Geocoder instance) {
//    return (ShadowGeocoder) Shadows.shadowOf(instance);
//  }

  public static ShadowGestureDetector shadowOf(GestureDetector instance) {
    return (ShadowGestureDetector) Shadows.shadowOf(instance);
  }

  public static ShadowGradientDrawable shadowOf(GradientDrawable instance) {
    return (ShadowGradientDrawable) Shadows.shadowOf(instance);
  }

  public static ShadowHandler shadowOf(Handler instance) {
    return (ShadowHandler) Shadows.shadowOf(instance);
  }

  public static ShadowHandlerThread shadowOf(HandlerThread instance) {
    return (ShadowHandlerThread) Shadows.shadowOf(instance);
  }

  public static ShadowHttpResponseCache shadowOf(HttpResponseCache instance) {
    return (ShadowHttpResponseCache) Shadows.shadowOf(instance);
  }

  public static ShadowImageView shadowOf(ImageView instance) {
    return (ShadowImageView) Shadows.shadowOf(instance);
  }

  public static ShadowInputMethodManager shadowOf(InputMethodManager instance) {
    return (ShadowInputMethodManager) Shadows.shadowOf(instance);
  }

  public static ShadowInputDevice shadowOf(InputDevice instance) {
    return (ShadowInputDevice) Shadows.shadowOf(instance);
  }

  public static ShadowIntent shadowOf(Intent instance) {
    return (ShadowIntent) Shadows.shadowOf(instance);
  }

  public static ShadowIntentService shadowOf(IntentService instance) {
    return (ShadowIntentService) Shadows.shadowOf(instance);
  }

  public static ShadowJsPromptResult shadowOf(JsPromptResult instance) {
    return (ShadowJsPromptResult) Shadows.shadowOf(instance);
  }

  public static ShadowJsResult shadowOf(JsResult instance) {
    return (ShadowJsResult) Shadows.shadowOf(instance);
  }

//  public static ShadowKeyEvent shadowOf(KeyEvent instance) {
//    return (ShadowKeyEvent) Shadows.shadowOf(instance);
//  }

  public static ShadowKeyguardManager shadowOf(KeyguardManager instance) {
    return (ShadowKeyguardManager) Shadows.shadowOf(instance);
  }

  public static ShadowKeyguardManager.ShadowKeyguardLock shadowOf(KeyguardManager.KeyguardLock instance) {
    return (ShadowKeyguardManager.ShadowKeyguardLock) Shadows.shadowOf(instance);
  }

  public static ShadowLinearLayout shadowOf(LinearLayout instance) {
    return (ShadowLinearLayout) Shadows.shadowOf(instance);
  }

  public static ShadowLinearGradient shadowOf(LinearGradient instance) {
    return (ShadowLinearGradient) Shadows.shadowOf(instance);
  }
//
//  public static ShadowListPreference shadowOf(ListPreference instance) {
//    return (ShadowListPreference) Shadows.shadowOf(instance);
//  }

  public static ShadowListView shadowOf(ListView instance) {
    return (ShadowListView) Shadows.shadowOf(instance);
  }

  public static ShadowLocationManager shadowOf(LocationManager instance) {
    return (ShadowLocationManager) Shadows.shadowOf(instance);
  }

  public static ShadowLooper shadowOf(Looper instance) {
    return (ShadowLooper) Shadows.shadowOf(instance);
  }

  public static ShadowMatrix shadowOf(Matrix other) {
    return (ShadowMatrix) Shadows.shadowOf(other);
  }

  public static ShadowMediaPlayer shadowOf(MediaPlayer instance) {
    return (ShadowMediaPlayer) Shadows.shadowOf(instance);
  }


  public static ShadowMediaRecorder shadowOf(MediaRecorder instance) {
    return (ShadowMediaRecorder) Shadows.shadowOf(instance);
  }


//  public static ShadowMenuInflater shadowOf(MenuInflater instance) {
//    return (ShadowMenuInflater) Shadows.shadowOf(instance);
//  }


  public static ShadowMimeTypeMap shadowOf(MimeTypeMap instance) {
    return (ShadowMimeTypeMap) Shadows.shadowOf(instance);
  }


  public static ShadowMotionEvent shadowOf(MotionEvent other) {
    return (ShadowMotionEvent) Shadows.shadowOf(other);
  }


  public static ShadowNetworkInfo shadowOf(NetworkInfo instance) {
    return (ShadowNetworkInfo) Shadows.shadowOf(instance);
  }


  public static ShadowNotification shadowOf(Notification other) {
    return (ShadowNotification) Shadows.shadowOf(other);
  }


  public static ShadowNotification.ShadowBigTextStyle shadowOf(Notification.BigTextStyle other) {
    return (ShadowNotification.ShadowBigTextStyle) Shadows.shadowOf(other);
  }


  public static ShadowNotificationManager shadowOf(NotificationManager other) {
    return (ShadowNotificationManager) Shadows.shadowOf(other);
  }

  public static ShadowNumberPicker shadowOf(NumberPicker other) {
    return (ShadowNumberPicker) Shadows.shadowOf(other);
  }

  public static ShadowObjectAnimator shadowOf(ObjectAnimator instance) {
    return (ShadowObjectAnimator) Shadows.shadowOf(instance);
  }

  public static ShadowPaint shadowOf(Paint instance) {
    return (ShadowPaint) Shadows.shadowOf(instance);
  }

  public static ShadowParcel shadowOf(Parcel instance) {
    return (ShadowParcel) Shadows.shadowOf(instance);
  }

  public static ShadowPath shadowOf(Path instance) {
    return (ShadowPath) Shadows.shadowOf(instance);
  }

  public static ShadowPendingIntent shadowOf(PendingIntent instance) {
    return (ShadowPendingIntent) Shadows.shadowOf(instance);
  }

  public static ShadowPopupWindow shadowOf(PopupWindow instance) {
    return (ShadowPopupWindow) Shadows.shadowOf(instance);
  }

  public static ShadowPowerManager shadowOf(PowerManager instance) {
    return (ShadowPowerManager) Shadows.shadowOf(instance);
  }

  public static ShadowPowerManager.ShadowWakeLock shadowOf(PowerManager.WakeLock instance) {
    return (ShadowPowerManager.ShadowWakeLock) Shadows.shadowOf(instance);
  }

  public static ShadowPreference shadowOf(Preference instance) {
    return (ShadowPreference) Shadows.shadowOf(instance);
  }

  public static ShadowPreferenceActivity shadowOf(PreferenceActivity instance) {
    return (ShadowPreferenceActivity) Shadows.shadowOf(instance);
  }

//  public static ShadowPreferenceCategory shadowOf(PreferenceCategory instance) {
//    return (ShadowPreferenceCategory) Shadows.shadowOf(instance);
//  }
//
//  public static ShadowPreferenceGroup shadowOf(PreferenceGroup instance) {
//    return (ShadowPreferenceGroup) Shadows.shadowOf(instance);
//  }
//
//  public static ShadowPreferenceScreen shadowOf(PreferenceScreen instance) {
//    return (ShadowPreferenceScreen) Shadows.shadowOf(instance);
//  }

  public static ShadowProgressBar shadowOf(ProgressBar instance) {
    return (ShadowProgressBar) Shadows.shadowOf(instance);
  }

  public static ShadowProgressDialog shadowOf(ProgressDialog instance) {
    return (ShadowProgressDialog) Shadows.shadowOf(instance);
  }

  public static ShadowRemoteViews shadowOf(RemoteViews instance) {
    return (ShadowRemoteViews) Shadows.shadowOf(instance);
  }

  public static ShadowResolveInfo shadowOf(ResolveInfo instance) {
    return (ShadowResolveInfo) Shadows.shadowOf(instance);
  }

  public static ShadowResourceCursorAdapter shadowOf(ResourceCursorAdapter instance) {
    return (ShadowResourceCursorAdapter) Shadows.shadowOf(instance);
  }

  public static ShadowResources shadowOf(Resources instance) {
    return (ShadowResources) Shadows.shadowOf(instance);
  }

  public static ShadowResultReceiver shadowOf(ResultReceiver instance) {
    return (ShadowResultReceiver) Shadows.shadowOf(instance);
  }

  public static ShadowScaleGestureDetector shadowOf(ScaleGestureDetector instance) {
    return (ShadowScaleGestureDetector) Shadows.shadowOf(instance);
  }

  public static ShadowScanResult shadowOf(ScanResult instance) {
    return (ShadowScanResult) Shadows.shadowOf(instance);
  }

  public static ShadowScroller shadowOf(Scroller instance) {
    return (ShadowScroller) Shadows.shadowOf(instance);
  }

  public static ShadowScrollView shadowOf(ScrollView instance) {
    return (ShadowScrollView) Shadows.shadowOf(instance);
  }

  public static ShadowSeekBar shadowOf(SeekBar instance) {
    return (ShadowSeekBar) Shadows.shadowOf(instance);
  }

  public static ShadowSensorManager shadowOf(SensorManager instance) {
    return (ShadowSensorManager) Shadows.shadowOf(instance);
  }

  public static ShadowService shadowOf(Service instance) {
    return (ShadowService) Shadows.shadowOf(instance);
  }

  public static ShadowSimpleCursorAdapter shadowOf(SimpleCursorAdapter instance) {
    return (ShadowSimpleCursorAdapter) Shadows.shadowOf(instance);
  }

  public static ShadowSmsManager shadowOf(SmsManager instance) {
    return (ShadowSmsManager) Shadows.shadowOf(instance);
  }

  public static ShadowSslErrorHandler shadowOf(SslErrorHandler instance) {
    return (ShadowSslErrorHandler) Shadows.shadowOf(instance);
  }

  public static ShadowStateListDrawable shadowOf(StateListDrawable instance) {
    return (ShadowStateListDrawable) Shadows.shadowOf(instance);
  }

  public static ShadowTabHost shadowOf(TabHost instance) {
    return (ShadowTabHost) Shadows.shadowOf(instance);
  }

  public static ShadowTabHost.ShadowTabSpec shadowOf(TabHost.TabSpec instance) {
    return (ShadowTabHost.ShadowTabSpec) Shadows.shadowOf(instance);
  }

  public static ShadowTelephonyManager shadowOf(TelephonyManager instance) {
    return (ShadowTelephonyManager) Shadows.shadowOf(instance);
  }

  public static ShadowTextPaint shadowOf(TextPaint instance) {
    return (ShadowTextPaint) Shadows.shadowOf(instance);
  }

  public static ShadowTextToSpeech shadowOf(TextToSpeech instance) {
    return (ShadowTextToSpeech) Shadows.shadowOf(instance);
  }

  public static ShadowTextView shadowOf(TextView instance) {
    return (ShadowTextView) Shadows.shadowOf(instance);
  }

  public static ShadowResources.ShadowTheme shadowOf(Resources.Theme instance) {
    return (ShadowResources.ShadowTheme) Shadows.shadowOf(instance);
  }

  public static ShadowTimePickerDialog shadowOf(TimePickerDialog instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowToast shadowOf(Toast instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowTouchDelegate shadowOf(TouchDelegate instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowTranslateAnimation shadowOf(TranslateAnimation instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowTypedArray shadowOf(TypedArray instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowTypeface shadowOf(Typeface instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowView shadowOf(View instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowViewAnimator shadowOf(ViewAnimator instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowViewConfiguration shadowOf(ViewConfiguration instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowViewTreeObserver shadowOf(ViewTreeObserver instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowViewGroup shadowOf(ViewGroup instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowVideoView shadowOf(VideoView instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowWebView shadowOf(WebView instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowWebViewDatabase shadowOf(WebViewDatabase instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowWifiConfiguration shadowOf(WifiConfiguration instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowWifiInfo shadowOf(WifiInfo instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowWifiManager shadowOf(WifiManager instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowWindow shadowOf(Window instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowZoomButtonsController shadowOf(ZoomButtonsController instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowWindowManager shadowOf(WindowManager instance) {
    return Shadows.shadowOf(instance);
  }

//  public static ShadowDrawerLayout shadowOf(DrawerLayout instance) {
//    return Shadows.shadowOf(instance);
//  }

  public static ShadowPopupMenu shadowOf(PopupMenu instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowDownloadManager shadowOf(DownloadManager instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowDownloadManager.ShadowRequest shadowOf(DownloadManager.Request instance) {
    return Shadows.shadowOf(instance);
  }

  public static ShadowSurface shadowOf(Surface surface) {
    return Shadows.shadowOf(surface);
  }

  @SuppressWarnings({"unchecked"})
  public static <P, R> P shadowOf_(R instance) {
    return (P) ShadowExtractor.extract(instance);
  }
}
