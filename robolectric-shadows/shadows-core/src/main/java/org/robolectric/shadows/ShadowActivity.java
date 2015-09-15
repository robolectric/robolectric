package org.robolectric.shadows;

import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.internal.Shadow.directlyOn;
import static org.robolectric.internal.Shadow.invokeConstructor;

import android.R;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.ResName;
import org.robolectric.util.ReflectionHelpers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shadow for {@link android.app.Activity}.
 */
@Implements(Activity.class)
public class ShadowActivity extends ShadowContextThemeWrapper {

  @RealObject
  protected Activity realActivity;

  private int resultCode;
  private Intent resultIntent;
  private Activity parent;
  private boolean finishWasCalled;
  private List<IntentForResult> startedActivitiesForResults = new ArrayList<>();
  private Map<Intent.FilterComparison, Integer> intentRequestCodeMap = new HashMap<>();
  private int requestedOrientation = -1;
  private View currentFocus;
  private Integer lastShownDialogId = null;
  private int pendingTransitionEnterAnimResId = -1;
  private int pendingTransitionExitAnimResId = -1;
  private Object lastNonConfigurationInstance;
  private Map<Integer, Dialog> dialogForId = new HashMap<>();
  private ArrayList<Cursor> managedCursors = new ArrayList<>();
  private int mDefaultKeyMode = Activity.DEFAULT_KEYS_DISABLE;
  private SpannableStringBuilder mDefaultKeySsb = null;
  private int streamType = -1;
  private boolean mIsTaskRoot = true;
  private Menu optionsMenu;
  private Application testApplication;
  private ComponentName callingActivity;

  public void __constructor__() {
    invokeConstructor(Activity.class, realActivity);
  }

  public void setApplication(Application application) {
    ReflectionHelpers.setField(realActivity, "mApplication", application);
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

  public void setTestApplication(Application testApplication) {
    this.testApplication = testApplication;
  }

  public Application getTestApplication() {
    return testApplication;
  }

  @Implementation
  public final Application getApplication() {
    return testApplication != null ? testApplication : RuntimeEnvironment.application;
  }

  @Override
  @Implementation
  public final Application getApplicationContext() {
    return getApplication();
  }

  public void setCallingActivity(ComponentName activityName) {
    callingActivity = activityName;
  }

  @Implementation
  public ComponentName getCallingActivity() {
    return callingActivity;
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
  @HiddenApi @Implementation
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
    ReflectionHelpers.setField(realActivity, "mWindow", window);
  }

  @Implementation
  public void runOnUiThread(Runnable action) {
    ShadowApplication.getInstance().getForegroundThreadScheduler().post(action);
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
   * Non-Android accessor.
   *
   * @param view View to focus.
   */
  public void setCurrentFocus(View view) {
    currentFocus = view;
  }

  @Implementation
  public View getCurrentFocus() {
    return currentFocus;
  }

  public int getPendingTransitionEnterAnimationResourceId() {
    return pendingTransitionEnterAnimResId;
  }

  public int getPendingTransitionExitAnimationResourceId() {
    return pendingTransitionExitAnimResId;
  }

  @Implementation
  public boolean onCreateOptionsMenu(Menu menu) {
    optionsMenu = menu;
    return directlyOn(realActivity, Activity.class).onCreateOptionsMenu(menu);
  }

  /**
   * Return the options menu.
   *
   * @return  Options menu.
   */
  public Menu getOptionsMenu() {
    return optionsMenu;
  }

  /**
   * Perform a click on a menu item.
   *
   * @param menuItemResId Menu item resource ID.
   * @return True if the click was handled, false otherwise.
   */
  public boolean clickMenuItem(int menuItemResId) {
    if (optionsMenu == null) {
      throw new RuntimeException(
          "Activity does not have an options menu! Did you forget to call " +
          "super.onCreateOptionsMenu(menu) in " + realActivity.getClass().getName() + "?");
    }

    final RoboMenuItem item = new RoboMenuItem(menuItemResId);
    return realActivity.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, item);
  }

  /**
   * Container object to hold an Intent, together with the requestCode used
   * in a call to {@code Activity#startActivityForResult(Intent, int)}
   */
  public class IntentForResult {
    public Intent intent;
    public int requestCode;
    public Bundle options;

    public IntentForResult(Intent intent, int requestCode) {
      this.intent = intent;
      this.requestCode = requestCode;
      this.options = null;
    }

    public IntentForResult(Intent intent, int requestCode, Bundle options) {
      this.intent = intent;
      this.requestCode = requestCode;
      this.options = options;
    }
  }

  @Implementation
  public void startActivityForResult(Intent intent, int requestCode) {
    intentRequestCodeMap.put(new Intent.FilterComparison(intent), requestCode);
    startedActivitiesForResults.add(new IntentForResult(intent, requestCode));
    getApplicationContext().startActivity(intent);
  }

  @Implementation
  public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
    intentRequestCodeMap.put(new Intent.FilterComparison(intent), requestCode);
    startedActivitiesForResults.add(new IntentForResult(intent, requestCode, options));
    getApplicationContext().startActivity(intent);
  }

  public void receiveResult(Intent requestIntent, int resultCode, Intent resultIntent) {
    Integer requestCode = intentRequestCodeMap.get(new Intent.FilterComparison(requestIntent));
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
    this.lastShownDialogId = id;
    Dialog dialog = dialogForId.get(id);

    if (dialog == null) {
      final ActivityInvoker invoker = new ActivityInvoker();
      dialog = (Dialog) invoker.call("onCreateDialog", Integer.TYPE).with(id);
      if (dialog == null) {
        return false;
      }
      if (bundle == null) {
        invoker.call("onPrepareDialog", Integer.TYPE, Dialog.class).with(id, dialog);
      } else {
        invoker.call("onPrepareDialog", Integer.TYPE, Dialog.class, Bundle.class).with(id, dialog, bundle);
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
  public void startManagingCursor(Cursor c) {
    managedCursors.add(c);
  }

  @Implementation
  public void stopManagingCursor(Cursor c) {
    managedCursors.remove(c);
  }

  public List<Cursor> getManagedCursors() {
    return managedCursors;
  }

  @Implementation
  public final void setVolumeControlStream(int streamType) {
    this.streamType = streamType;
  }

  @Implementation
  public final int getVolumeControlStream() {
    return streamType;
  }

  @Implementation
  public void startActivityFromFragment(Fragment fragment, Intent intent, int requestCode) {
    startActivityForResult(intent, requestCode);
  }

  @Implementation
  public void startActivityFromFragment(Fragment fragment, Intent intent, int requestCode, Bundle options) {
    startActivityForResult(intent, requestCode, options);
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
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
