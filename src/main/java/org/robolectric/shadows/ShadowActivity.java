package org.robolectric.shadows;

import android.R;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import org.robolectric.AndroidManifest;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.bytecode.RobolectricInternals;
import org.robolectric.res.ResName;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.fest.reflect.core.Reflection.field;
import static org.robolectric.Robolectric.directlyOn;
import static org.robolectric.Robolectric.shadowOf;

@Implements(Activity.class)
public class ShadowActivity extends ShadowContextThemeWrapper {
  private static final Set<String> ALREADY_WARNED_ABOUT = new HashSet<String>();

  @RealObject
  protected Activity realActivity;

  private int resultCode;
  private Intent resultIntent;
  private Activity parent;
  private boolean finishWasCalled;

  private List<IntentForResult> startedActivitiesForResults = new ArrayList<IntentForResult>();

  private Map<Intent, Integer> intentRequestCodeMap = new HashMap<Intent, Integer>();
  private int requestedOrientation = -1;
  private View currentFocus;
  private Integer lastShownDialogId = null;
  private int pendingTransitionEnterAnimResId = -1;
  private int pendingTransitionExitAnimResId = -1;
  private Object lastNonConfigurationInstance;
  private Map<Integer, Dialog> dialogForId = new HashMap<Integer, Dialog>();
  private CharSequence title;
  private boolean onKeyUpWasCalled;
  private ArrayList<Cursor> managedCusors = new ArrayList<Cursor>();

  private int mDefaultKeyMode = Activity.DEFAULT_KEYS_DISABLE;
  private SpannableStringBuilder mDefaultKeySsb = null;
  private boolean destroyed = false;
  private int streamType = -1;
  private boolean mIsTaskRoot = true;
  
  public void __constructor__() {
    RobolectricInternals.getConstructor(Activity.class, realActivity, new Class[0]).invoke();
  }

  public void setApplication(Application application) {
    field("mApplication").ofType(Application.class).in(realActivity).set(application);
  }

  public boolean setThemeFromManifest() {
    ShadowApplication shadowApplication = shadowOf(realActivity.getApplication());
    AndroidManifest appManifest = shadowApplication.getAppManifest();
    if (appManifest == null) return false;

    String themeRef = appManifest.getThemeRef(realActivity.getClass());

    if (themeRef != null) {
      ResName style = ResName.qualifyResName(themeRef.replace("@", ""), appManifest.getPackageName(), "style");
      Integer themeRes = shadowApplication.getResourceLoader().getResourceIndex().getResourceId(style);
      if (themeRes == null)
        throw new Resources.NotFoundException("no such theme " + style.getFullyQualifiedName());
      realActivity.setTheme(themeRes);
      return true;
    }
    return false;
  }

  public void callOnCreate(Bundle bundle) {
    invokeReflectively("onCreate", Bundle.class, bundle);
  }

  public void callOnRestoreInstanceState(Bundle savedInstanceState) {
    invokeReflectively("onRestoreInstanceState", Bundle.class, savedInstanceState);
  }

  public void callOnPostCreate(android.os.Bundle savedInstanceState) {
    invokeReflectively("onPostCreate", Bundle.class, savedInstanceState);
  }

  public void callOnStart() {
    invokeReflectively("onStart");
  }

  public void callOnRestart() {
    invokeReflectively("onRestart");
  }

  public void callOnResume() {
    invokeReflectively("onResume");
  }

  public void callOnPostResume() {
    invokeReflectively("onPostResume");
  }

  public void callOnNewIntent(android.content.Intent intent) {
    invokeReflectively("onNewIntent", Intent.class, intent);
  }

  public void callOnSaveInstanceState(android.os.Bundle outState) {
    invokeReflectively("onSaveInstanceState", Bundle.class, outState);
  }

  public void callOnPause() {
    invokeReflectively("onPause");
  }

  public void callOnUserLeaveHint() {
    invokeReflectively("onUserLeaveHint");
  }

  public void callOnStop() {
    invokeReflectively("onStop");
  }

  public void callOnDestroy() {
    invokeReflectively("onDestroy");
  }

  private void invokeReflectively(String methodName, Class<?> argClass, Object arg) {
    try {
      Method method = Activity.class.getDeclaredMethod(methodName, argClass);
      method.setAccessible(true);
      method.invoke(realActivity, arg);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private void invokeReflectively(String methodName) {
    try {
      Method method = Activity.class.getDeclaredMethod(methodName);
      method.setAccessible(true);
      method.invoke(realActivity);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @Implementation
  public final Application getApplication() {
    return Robolectric.application;
  }

  @Override
  @Implementation
  public final Application getApplicationContext() {
    return getApplication();
  }

  //@Override
  //@Implementation
  //public Object getSystemService(String name) {
  //  return getApplicationContext().getSystemService(name);
  //}

  @Implementation
  public ComponentName getCallingActivity() {
    return null;
  }

  @Implementation
  public void setDefaultKeyMode(int keyMode) {
    mDefaultKeyMode = keyMode;

    // Some modes use a SpannableStringBuilder to track & dispatch input events
    // This list must remain in sync with the switch in onKeyDown()
    switch (mDefaultKeyMode) {
      case Activity.DEFAULT_KEYS_DISABLE:
      case Activity.DEFAULT_KEYS_SHORTCUT:
        mDefaultKeySsb = null;      // not used in these modes
        break;
      case Activity.DEFAULT_KEYS_DIALER:
      case Activity.DEFAULT_KEYS_SEARCH_LOCAL:
      case Activity.DEFAULT_KEYS_SEARCH_GLOBAL:
        mDefaultKeySsb = new SpannableStringBuilder();
        Selection.setSelection(mDefaultKeySsb, 0);
        break;
      default:
        throw new IllegalArgumentException();
    }
  }

  public int getDefaultKeymode() {
    return mDefaultKeyMode;
  }

  @Implementation(i18nSafe = false)
  public void setTitle(CharSequence title) {
    this.title = title;
  }

  @Implementation
  public void setTitle(int titleId) {
    this.title = this.getResources().getString(titleId);
  }

  @Implementation
  public CharSequence getTitle() {
    return title;
  }

  @Implementation
  public final void setResult(int resultCode) {
    this.resultCode = resultCode;
  }

  @Implementation
  public final void setResult(int resultCode, Intent data) {
    this.resultCode = resultCode;
    this.resultIntent = data;
  }

  @Implementation
  public LayoutInflater getLayoutInflater() {
    return LayoutInflater.from(realActivity);
  }

  @Implementation
  public MenuInflater getMenuInflater() {
    return new MenuInflater(realActivity);
  }

  /**
   * Checks to ensure that the{@code contentView} has been set
   *
   * @param id ID of the view to find
   * @return the view
   * @throws RuntimeException if the {@code contentView} has not been called first
   */
  @Implementation
  public View findViewById(int id) {
    return getWindow().findViewById(id);
  }

  @Implementation
  public final Activity getParent() {
    return parent;
  }

  /**
   * Allow setting of Parent fragmentActivity (for unit testing purposes only)
   *
   * @param parent Parent fragmentActivity to set on this fragmentActivity
   */
  public void setParent(Activity parent) {
    this.parent = parent;
  }

  @Implementation
  public void onBackPressed() {
    finish();
  }

  @Implementation
  public void finish() {
    finishWasCalled = true;
  }

  public void resetIsFinishing() {
    finishWasCalled = false;
  }

  /**
   * @return whether {@link #finish()} was called
   */
  @Implementation
  public boolean isFinishing() {
    return finishWasCalled;
  }

  /**
   * Constructs a new Window (a {@link com.android.internal.policy.impl.PhoneWindow}) if no window has previously been
   * set.
   *
   * @return the window associated with this Activity
   */
  @Implementation
  public Window getWindow()  {
    Window window = directlyOn(realActivity, Activity.class).getWindow();

    if (window == null) {
      try {
        window = ShadowWindow.create(realActivity);
        setWindow(window);
      } catch (Exception e) {
        throw new RuntimeException("Window creation failed!", e);
      }
    }

    return window;
  }

  public void setWindow(Window window) {
    field("mWindow").ofType(Window.class).in(realActivity).set(window);
  }

  @Implementation
  public void runOnUiThread(Runnable action) {
    Robolectric.getUiThreadScheduler().post(action);
  }

  /**
   * Checks to see if {@code BroadcastListener}s are still registered.
   *
   * @throws RuntimeException if any listeners are still registered
   * @see #assertNoBroadcastListenersRegistered()
   */
  @Implementation
  public void onDestroy() {
    assertNoBroadcastListenersRegistered();
    this.destroyed = true;
  }

  public boolean isDestroyed() {
    return destroyed;
  }

  @Implementation
  public WindowManager getWindowManager() {
    return (WindowManager) Robolectric.application.getSystemService(Context.WINDOW_SERVICE);
  }

  @Implementation
  public void setRequestedOrientation(int requestedOrientation) {
    if (getParent() != null) {
      getParent().setRequestedOrientation(requestedOrientation);
    } else {
      this.requestedOrientation = requestedOrientation;
    }
  }

  @Implementation
  public int getRequestedOrientation() {
    if (getParent() != null) {
      return getParent().getRequestedOrientation();
    } else {
      return this.requestedOrientation;
    }
  }

  @Implementation
  public int getTaskId() {
    return 0;
  }

  @Implementation
  public SharedPreferences getPreferences(int mode) {
    return ShadowPreferenceManager.getDefaultSharedPreferences(getApplicationContext());
  }

  /**
   * Checks the {@code ApplicationContext} to see if {@code BroadcastListener}s are still registered.
   *
   * @throws RuntimeException if any listeners are still registered
   * @see ShadowApplication#assertNoBroadcastListenersRegistered(android.content.Context, String)
   */
  public void assertNoBroadcastListenersRegistered() {
    shadowOf(getApplicationContext()).assertNoBroadcastListenersRegistered(realActivity, "Activity");
  }

  /**
   * Non-Android accessor.
   *
   * @return the {@code contentView} set by one of the {@code setContentView()} methods
   */
  public View getContentView() {
    return ((ViewGroup) getWindow().findViewById(R.id.content)).getChildAt(0);
  }

  /**
   * Non-Android accessor.
   *
   * @return the {@code resultCode} set by one of the {@code setResult()} methods
   */
  public int getResultCode() {
    return resultCode;
  }

  /**
   * Non-Android accessor.
   *
   * @return the {@code Intent} set by {@link #setResult(int, android.content.Intent)}
   */
  public Intent getResultIntent() {
    return resultIntent;
  }

  /**
   * Non-Android accessor consumes and returns the next {@code Intent} on the
   * started activities for results stack.
   *
   * @return the next started {@code Intent} for an activity, wrapped in
   *         an {@link ShadowActivity.IntentForResult} object
   */
  public IntentForResult getNextStartedActivityForResult() {
    if (startedActivitiesForResults.isEmpty()) {
      return null;
    } else {
      return startedActivitiesForResults.remove(0);
    }
  }

  /**
   * Non-Android accessor returns the most recent {@code Intent} started by
   * {@link #startActivityForResult(android.content.Intent, int)} without
   * consuming it.
   *
   * @return the most recently started {@code Intent}, wrapped in
   *         an {@link ShadowActivity.IntentForResult} object
   */
  public IntentForResult peekNextStartedActivityForResult() {
    if (startedActivitiesForResults.isEmpty()) {
      return null;
    } else {
      return startedActivitiesForResults.get(0);
    }
  }

  @Implementation
  public Object getLastNonConfigurationInstance() {
    return lastNonConfigurationInstance;
  }

  public void setLastNonConfigurationInstance(Object lastNonConfigurationInstance) {
    this.lastNonConfigurationInstance = lastNonConfigurationInstance;
  }

  /**
   * Non-Android accessor Sets the {@code View} for this {@code Activity}
   *
   * @param view
   */
  public void setCurrentFocus(View view) {
    currentFocus = view;
  }

  @Implementation
  public View getCurrentFocus() {
    return currentFocus;
  }

  @Implementation
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    onKeyUpWasCalled = true;
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      onBackPressed();
      return true;
    }
    return false;
  }

  public boolean onKeyUpWasCalled() {
    return onKeyUpWasCalled;
  }

  public void resetKeyUpWasCalled() {
    onKeyUpWasCalled = false;
  }

  public void performLayout() {
    shadowOf(getWindow()).performLayout();
  }

  public int getPendingTransitionEnterAnimationResourceId() {
    return pendingTransitionEnterAnimResId;
  }

  public int getPendingTransitionExitAnimationResourceId() {
    return pendingTransitionExitAnimResId;
  }

  /**
   * Container object to hold an Intent, together with the requestCode used
   * in a call to {@code Activity#startActivityForResult(Intent, int)}
   */
  public class IntentForResult {
    public Intent intent;
    public int requestCode;

    public IntentForResult(Intent intent, int requestCode) {
      this.intent = intent;
      this.requestCode = requestCode;
    }
  }

  @Implementation
  public void startActivity(Intent intent) {
    startActivityForResult(intent, -1);
  }

  @Implementation
  public void startActivityForResult(Intent intent, int requestCode) {
    intentRequestCodeMap.put(intent, requestCode);
    startedActivitiesForResults.add(new IntentForResult(intent, requestCode));
    getApplicationContext().startActivity(intent);
  }

  public void receiveResult(Intent requestIntent, int resultCode, Intent resultIntent) {
    Integer requestCode = intentRequestCodeMap.get(requestIntent);
    if (requestCode == null) {
      throw new RuntimeException("No intent matches " + requestIntent + " among " + intentRequestCodeMap.keySet());
    }

    final ActivityInvoker invoker = new ActivityInvoker();
    invoker.call("onActivityResult", Integer.TYPE, Integer.TYPE, Intent.class)
        .with(requestCode, resultCode, resultIntent);
  }

  @Implementation
  public final void showDialog(int id) {
    showDialog(id, null);
  }

  @Implementation
  public final void dismissDialog(int id) {
    final Dialog dialog = dialogForId.get(id);
    if (dialog == null) {
      throw new IllegalArgumentException();
    }

    dialog.dismiss();
  }

  @Implementation
  public final void removeDialog(int id) {
    dialogForId.remove(id);
  }

  @Implementation
  public final boolean showDialog(int id, Bundle bundle) {
    Dialog dialog = null;
    this.lastShownDialogId = id;

    dialog = dialogForId.get(id);

    if (dialog == null) {
      final ActivityInvoker invoker = new ActivityInvoker();
      dialog = (Dialog) invoker.call("onCreateDialog", Integer.TYPE).with(id);

      if (bundle == null) {
        invoker.call("onPrepareDialog", Integer.TYPE, Dialog.class)
            .with(id, dialog);
      } else {
        invoker.call("onPrepareDialog", Integer.TYPE, Dialog.class, Bundle.class)
            .with(id, dialog, bundle);
      }

      dialogForId.put(id, dialog);
    }

    dialog.show();

    return true;
  }

  public void setIsTaskRoot(boolean isRoot) {
    mIsTaskRoot = isRoot;
  }

  @Implementation
  public final boolean isTaskRoot() {
    return mIsTaskRoot;
  }

  /**
   * Non-Android accessor
   *
   * @return the dialog resource id passed into
   *         {@code Activity#showDialog(int, Bundle)} or {@code Activity#showDialog(int)}
   */
  public Integer getLastShownDialogId() {
    return lastShownDialogId;
  }

  public boolean hasCancelledPendingTransitions() {
    return pendingTransitionEnterAnimResId == 0 && pendingTransitionExitAnimResId == 0;
  }

  @Implementation
  public void overridePendingTransition(int enterAnim, int exitAnim) {
    pendingTransitionEnterAnimResId = enterAnim;
    pendingTransitionExitAnimResId = exitAnim;
  }

  public Dialog getDialogById(int dialogId) {
    return dialogForId.get(dialogId);
  }

  @Implementation
  public void recreate() {
    Bundle outState = new Bundle();
    final ActivityInvoker invoker = new ActivityInvoker();

    invoker.call("onSaveInstanceState", Bundle.class).with(outState);
    invoker.call("onPause").withNothing();
    invoker.call("onStop").withNothing();

    Object nonConfigInstance = invoker.call("onRetainNonConfigurationInstance").withNothing();
    setLastNonConfigurationInstance(nonConfigInstance);

    invoker.call("onDestroy").withNothing();
    invoker.call("onCreate", Bundle.class).with(outState);
    invoker.call("onStart").withNothing();
    invoker.call("onRestoreInstanceState", Bundle.class).with(outState);
    invoker.call("onResume").withNothing();
  }

  public void pauseAndThenResume() {
    final ActivityInvoker invoker = new ActivityInvoker();

    invoker.call("onPause").withNothing();
    invoker.call("onStop").withNothing();
    invoker.call("onRestart").withNothing();
    invoker.call("onStart").withNothing();
    invoker.call("onResume").withNothing();
  }

  @Implementation
  public void onSaveInstanceState(Bundle outState) {
  }

  @Implementation
  public void startManagingCursor(Cursor c) {
    managedCusors.add(c);
  }

  @Implementation
  public void stopManagingCursor(Cursor c) {
    managedCusors.remove(c);
  }

  public List<Cursor> getManagedCursors() {
    return managedCusors;
  }

  @Implementation
  public final void setVolumeControlStream(int streamType) {
    this.streamType = streamType;
  }

  @Implementation
  public final int getVolumeControlStream() {
    return streamType;
  }

  private final class ActivityInvoker {
    private Method method;

    public ActivityInvoker call(final String methodName, final Class... argumentClasses) {
      try {
        method = Activity.class.getDeclaredMethod(methodName, argumentClasses);
        method.setAccessible(true);
        return this;
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }

    public Object withNothing() {
      return with();
    }

    public Object with(final Object... parameters) {
      try {
        return method.invoke(realActivity, parameters);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (IllegalArgumentException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
