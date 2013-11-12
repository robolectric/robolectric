package org.robolectric.shadows;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.AndroidManifest;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.res.Fs;
import org.robolectric.shadows.testing.OnMethodTestActivity;
import org.robolectric.test.TemporaryFolder;
import org.robolectric.util.ActivityController;
import org.robolectric.util.TestRunnable;
import org.robolectric.util.Transcript;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ActivityTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Activity activity;

  @Test(expected = IllegalStateException.class)
  public void shouldComplainIfActivityIsDestroyedWithRegisteredBroadcastReceivers() throws Exception {
    activity = create(DialogLifeCycleActivity.class);
    activity.registerReceiver(new AppWidgetProvider(), new IntentFilter());
    destroy(activity);
  }

  @Test
  public void shouldNotComplainIfActivityIsDestroyedWhileAnotherActivityHasRegisteredBroadcastReceivers() throws Exception {
    activity = create(DialogLifeCycleActivity.class);

    DialogLifeCycleActivity activity2 = new DialogLifeCycleActivity();
    activity2.registerReceiver(new AppWidgetProvider(), new IntentFilter());

    destroy(activity); // should not throw exception
  }

  @Test
  public void shouldNotRegisterNullBroadcastReceiver() {
    activity = create(DialogLifeCycleActivity.class);
    activity.registerReceiver(null, new IntentFilter());

    destroy(activity);
  }

  @Test
  public void shouldReportDestroyedStatus() {
    activity = create(DialogLifeCycleActivity.class);
    destroy(activity);
    assertThat(shadowOf(activity).isDestroyed()).isTrue();
  }

  @Test
  public void startActivity_shouldDelegateToStartActivityForResult() {
    final Transcript transcript = new Transcript();
    Activity activity = new Activity() {
      @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        transcript.add("onActivityResult called with requestCode " + requestCode + ", resultCode " + resultCode + ", intent data " + data.getData());
      }
    };
    activity.startActivity(new Intent().setType("image/*"));

    shadowOf(activity).receiveResult(new Intent().setType("image/*"), Activity.RESULT_OK,
        new Intent().setData(Uri.parse("content:foo")));
    transcript.assertEventsSoFar("onActivityResult called with requestCode -1, resultCode -1, intent data content:foo");
  }

  @Test
  public void startActivityForResultAndReceiveResult_shouldSendResponsesBackToActivity() throws Exception {
    final Transcript transcript = new Transcript();
    Activity activity = new Activity() {
      @Override
      protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        transcript.add("onActivityResult called with requestCode " + requestCode + ", resultCode " + resultCode + ", intent data " + data.getData());
      }
    };
    activity.startActivityForResult(new Intent().setType("audio/*"), 123);
    activity.startActivityForResult(new Intent().setType("image/*"), 456);

    shadowOf(activity).receiveResult(new Intent().setType("image/*"), Activity.RESULT_OK,
        new Intent().setData(Uri.parse("content:foo")));
    transcript.assertEventsSoFar("onActivityResult called with requestCode 456, resultCode -1, intent data content:foo");
  }

  @Test
  public void startActivityForResultAndReceiveResult_whenNoIntentMatches_shouldThrowException() throws Exception {
    Activity activity = new Activity() {
      @Override
      protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        throw new IllegalStateException("should not be called");
      }
    };
    activity.startActivityForResult(new Intent().setType("audio/*"), 123);
    activity.startActivityForResult(new Intent().setType("image/*"), 456);

    Intent requestIntent = new Intent().setType("video/*");
    try {
      shadowOf(activity).receiveResult(requestIntent, Activity.RESULT_OK,
          new Intent().setData(Uri.parse("content:foo")));
      fail();
    } catch (Exception e) {
      assertThat(e.getMessage()).startsWith("No intent matches " + requestIntent);
    }
  }

  @Test
  public void shouldSupportStartActivityForResult() throws Exception {
    activity = create(DialogLifeCycleActivity.class);
    ShadowActivity shadowActivity = Robolectric.shadowOf(activity);
    Intent intent = new Intent().setClass(activity, DialogLifeCycleActivity.class);
    assertThat(shadowActivity.getNextStartedActivity()).isNull();

    activity.startActivityForResult(intent, 142);

    Intent startedIntent = shadowActivity.getNextStartedActivity();
    assertThat(startedIntent).isNotNull();
    assertThat(startedIntent).isSameAs(intent);
  }

  @Test
  public void shouldSupportGetStartedActitivitesForResult() throws Exception {
    activity = create(DialogLifeCycleActivity.class);
    ShadowActivity shadowActivity = Robolectric.shadowOf(activity);
    Intent intent = new Intent().setClass(activity, DialogLifeCycleActivity.class);

    activity.startActivityForResult(intent, 142);

    ShadowActivity.IntentForResult intentForResult = shadowActivity.getNextStartedActivityForResult();
    assertThat(intentForResult).isNotNull();
    assertThat(shadowActivity.getNextStartedActivityForResult()).isNull();
    assertThat(intentForResult.intent).isNotNull();
    assertThat(intentForResult.intent).isSameAs(intent);
    assertThat(intentForResult.requestCode).isEqualTo(142);
  }

  @Test
  public void shouldSupportPeekStartedActitivitesForResult() throws Exception {
    activity = create(DialogLifeCycleActivity.class);
    ShadowActivity shadowActivity = Robolectric.shadowOf(activity);
    Intent intent = new Intent().setClass(activity, DialogLifeCycleActivity.class);

    activity.startActivityForResult(intent, 142);

    ShadowActivity.IntentForResult intentForResult = shadowActivity.peekNextStartedActivityForResult();
    assertThat(intentForResult).isNotNull();
    assertThat(shadowActivity.peekNextStartedActivityForResult()).isSameAs(intentForResult);
    assertThat(intentForResult.intent).isNotNull();
    assertThat(intentForResult.intent).isSameAs(intent);
    assertThat(intentForResult.requestCode).isEqualTo(142);
  }

  @Test
  public void onContentChangedShouldBeCalledAfterContentViewIsSet() throws RuntimeException {
    final Transcript transcript = new Transcript();
    ActivityWithContentChangedTranscript customActivity = buildActivity(ActivityWithContentChangedTranscript.class).create().get();
    customActivity.setTranscript(transcript);
    customActivity.setContentView(R.layout.main);
    transcript.assertEventsSoFar("onContentChanged was called; title is \"Main Layout\"");
  }

  @Test
  public void shouldRetrievePackageNameFromTheManifest() throws Exception {
    AndroidManifest appManifest = newConfigWith("com.wacka.wa", "");
    Robolectric.application = new DefaultTestLifecycle().createApplication(null, appManifest);
    shadowOf(application).bind(appManifest, null);

    assertThat("com.wacka.wa").isEqualTo(new Activity().getPackageName());
  }

  @Test
  public void shouldRunUiTasksImmediatelyByDefault() throws Exception {
    TestRunnable runnable = new TestRunnable();
    activity = create(DialogLifeCycleActivity.class);
    activity.runOnUiThread(runnable);
    assertTrue(runnable.wasRun);
  }

  @Test
  public void shouldQueueUiTasksWhenUiThreadIsPaused() throws Exception {
    Robolectric.pauseMainLooper();

    activity = create(DialogLifeCycleActivity.class);
    TestRunnable runnable = new TestRunnable();
    activity.runOnUiThread(runnable);
    assertFalse(runnable.wasRun);

    Robolectric.unPauseMainLooper();
    assertTrue(runnable.wasRun);
  }

  @Test
  public void showDialog_shouldCreatePrepareAndShowDialog() {
    final DialogLifeCycleActivity activity = create(DialogLifeCycleActivity.class);
    final AtomicBoolean dialogWasShown = new AtomicBoolean(false);

    new Dialog(activity) {
      {
        activity.dialog = this;
      }

      @Override
      public void show() {
        dialogWasShown.set(true);
      }
    };

    activity.showDialog(1);

    assertTrue(activity.createdDialog);
    assertTrue(activity.preparedDialog);
    assertTrue(dialogWasShown.get());
  }

  @Test
  public void showDialog_shouldCreatePrepareAndShowDialogWithBundle() {
    final DialogLifeCycleActivity activity = create(DialogLifeCycleActivity.class);
    final AtomicBoolean dialogWasShown = new AtomicBoolean(false);

    new Dialog(activity) {
      {
        activity.dialog = this;
      }

      @Override
      public void show() {
        dialogWasShown.set(true);
      }
    };

    activity.showDialog(1, new Bundle());

    assertTrue(activity.createdDialog);
    assertTrue(activity.preparedDialogWithBundle);
    assertTrue(dialogWasShown.get());
  }

  @Test
  public void showDialog_shouldReuseDialogs() {
    final DialogCreatingActivity activity = create(DialogCreatingActivity.class);
    activity.showDialog(1);
    Dialog firstDialog = ShadowDialog.getLatestDialog();
    activity.showDialog(1);

    Dialog secondDialog = ShadowDialog.getLatestDialog();
    assertSame("dialogs should be the same instance", firstDialog, secondDialog);
  }

  @Test
  public void showDialog_shouldShowDialog() throws Exception {
    final DialogCreatingActivity activity = create(DialogCreatingActivity.class);
    activity.showDialog(1);
    Dialog dialog = ShadowDialog.getLatestDialog();
    assertTrue(dialog.isShowing());
  }

  @Test
  public void dismissDialog_shouldDismissPreviouslyShownDialog() throws Exception {
    final DialogCreatingActivity activity = create(DialogCreatingActivity.class);
    activity.showDialog(1);
    activity.dismissDialog(1);
    Dialog dialog = ShadowDialog.getLatestDialog();
    assertFalse(dialog.isShowing());
  }

  @Test(expected = IllegalArgumentException.class)
  public void dismissDialog_shouldThrowExceptionIfDialogWasNotPreviouslyShown() throws Exception {
    final DialogCreatingActivity activity = create(DialogCreatingActivity.class);
    activity.dismissDialog(1);
  }

  @Test
  public void removeDialog_shouldCreateDialogAgain() {
    final DialogCreatingActivity activity = create(DialogCreatingActivity.class);
    activity.showDialog(1);
    Dialog firstDialog = ShadowDialog.getLatestDialog();

    activity.removeDialog(1);
    assertNull(Robolectric.shadowOf(activity).getDialogById(1));

    activity.showDialog(1);
    Dialog secondDialog = ShadowDialog.getLatestDialog();

    assertNotSame("dialogs should not be the same instance", firstDialog, secondDialog);
  }

  @Test
  public void shouldCallOnCreateDialogFromShowDialog() {
    ActivityWithOnCreateDialog activity = create(ActivityWithOnCreateDialog.class);
    activity.showDialog(123);
    assertTrue(activity.onCreateDialogWasCalled);
    assertThat(ShadowDialog.getLatestDialog()).isNotNull();
  }

  @Test
  public void shouldCallFinishInOnBackPressed() {
    Activity activity = new Activity();
    activity.onBackPressed();

    ShadowActivity shadowActivity = shadowOf(activity);
    assertTrue(shadowActivity.isFinishing());
  }

  @Test
  public void shouldSupportCurrentFocus() {
    activity = create(DialogLifeCycleActivity.class);
    ShadowActivity shadow = shadowOf(activity);

    assertNull(shadow.getCurrentFocus());
    View view = new View(activity);
    shadow.setCurrentFocus(view);
    assertEquals(view, shadow.getCurrentFocus());
  }

  @Test
  public void shouldSetOrientation() {
    activity = create(DialogLifeCycleActivity.class);
    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    assertThat(activity.getRequestedOrientation()).isEqualTo(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
  }

  @Test
  public void retrieveIdOfResource() {
    Activity activity = new Activity();

    int id1 = R.string.hello;
    String string = activity.getString(id1);
    assertEquals("Hello", string);

    int id = activity.getResources().getIdentifier("hello", "string", "org.robolectric");
    assertThat(id).isEqualTo(R.string.hello);

    String hello = activity.getResources().getString(id);
    assertEquals("Hello", hello);
  }

  @Test
  public void retrieveIdOfNonExistingResource() {
    Activity activity = new Activity();

    int id = activity.getResources().getIdentifier("just_alot_of_crap", "string", "org.robolectric");
    assertThat(id).isEqualTo(0);
  }

  @Test
  public void setDefaultKeyMode_shouldSetKeyMode() {
    int[] modes = {
        Activity.DEFAULT_KEYS_DISABLE,
        Activity.DEFAULT_KEYS_SHORTCUT,
        Activity.DEFAULT_KEYS_DIALER,
        Activity.DEFAULT_KEYS_SEARCH_LOCAL,
        Activity.DEFAULT_KEYS_SEARCH_GLOBAL
    };
    Activity activity = new Activity();
    ShadowActivity shadow = shadowOf(activity);

    for (int mode : modes) {
      activity.setDefaultKeyMode(mode);
      assertThat(shadow.getDefaultKeymode()).isEqualTo(mode).as("Unexpected key mode");
    }
  }

  @Test // unclear what the correct behavior should be here...
  public void shouldPopulateWindowDecorViewWithMergeLayoutContents() throws Exception {
    Activity activity = Robolectric.buildActivity(Activity.class).create().get();
    activity.setContentView(R.layout.toplevel_merge);

    View contentView = activity.findViewById(android.R.id.content);
    assertThat(((ViewGroup) contentView).getChildCount()).isEqualTo(2);
  }

  @Test public void setContentView_shouldReplaceOldContentView() throws Exception {
    View view1 = new View(application);
    view1.setId(R.id.burritos);
    View view2 = new View(application);
    view2.setId(R.id.button);

    Activity activity = buildActivity(Activity.class).create().get();
    activity.setContentView(view1);
    assertSame(view1, activity.findViewById(R.id.burritos));

    activity.setContentView(view2);
    assertNull(activity.findViewById(R.id.burritos));
    assertSame(view2, activity.findViewById(R.id.button));
  }

  @Test
  public void onKeyUp_recordsThatItWasCalled() throws Exception {
    Activity activity = new Activity();
    boolean consumed = activity.onKeyUp(KeyEvent.KEYCODE_0, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_0));

    assertFalse(consumed);
    assertTrue(shadowOf(activity).onKeyUpWasCalled());

    shadowOf(activity).resetKeyUpWasCalled();
    assertFalse(shadowOf(activity).onKeyUpWasCalled());
  }

  @Test
  public void onKeyUp_callsOnBackPressedWhichFinishesTheActivity() throws Exception {
    Activity activity = new Activity();
    boolean consumed = activity.onKeyUp(KeyEvent.KEYCODE_BACK, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));

    assertTrue(consumed);
    assertTrue(shadowOf(activity).onKeyUpWasCalled());
    assertTrue(activity.isFinishing());
  }

  @Test
  public void shouldGiveSharedPreferences() throws Exception {
    Activity activity = new Activity();
    SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
    assertNotNull(preferences);
    preferences.edit().putString("foo", "bar").commit();
    assertThat(activity.getPreferences(Context.MODE_PRIVATE).getString("foo", null)).isEqualTo("bar");
  }

  @Test
  public void shouldFindContentViewContainerWithChild() throws Exception {
    Activity activity = buildActivity(Activity.class).create().get();
    View contentView = new View(activity);
    activity.setContentView(contentView);

    FrameLayout contentViewContainer = (FrameLayout) activity.findViewById(android.R.id.content);
    assertThat(contentViewContainer.getChildAt(0)).isSameAs(contentView);
  }

  @Test
  public void shouldFindContentViewContainerWithoutChild() throws Exception {
    Activity activity = buildActivity(Activity.class).create().get();

    FrameLayout contentViewContainer = (FrameLayout) activity.findViewById(android.R.id.content);
    assertThat(contentViewContainer.getId()).isEqualTo(android.R.id.content);
  }

  @Test
  public void recreateGoesThroughFullLifeCycle() throws Exception {
    TestActivity activity = buildActivity(TestActivity.class).attach().get();
    activity.recreate();

    activity.transcript.assertEventsSoFar(
        "onSaveInstanceState",
        "onPause",
        "onStop",
        "onRetainNonConfigurationInstance",
        "onDestroy",
        "onCreate",
        "onStart",
        "onRestoreInstanceState",
        "onResume"
    );

    Integer storedValue = (Integer) activity.getLastNonConfigurationInstance();
    assertEquals(5, storedValue.intValue());
  }

  @Test
  public void pauseAndThenResumeGoesThroughTheFullLifeCycle() throws Exception {
    TestActivity activity = new TestActivity();

    ShadowActivity shadow = shadowOf(activity);
    shadow.pauseAndThenResume();

    activity.transcript.assertEventsSoFar(
        "onPause",
        "onStop",
        "onRestart",
        "onStart",
        "onResume"
    );

  }

  @Test
  public void startAndStopManagingCursorTracksCursors() throws Exception {
    TestActivity activity = new TestActivity();

    ShadowActivity shadow = shadowOf(activity);

    assertThat(shadow.getManagedCursors()).isNotNull();
    assertThat(shadow.getManagedCursors().size()).isEqualTo(0);

    Cursor c = Robolectric.newInstanceOf(SQLiteCursor.class);
    activity.startManagingCursor(c);

    assertThat(shadow.getManagedCursors()).isNotNull();
    assertThat(shadow.getManagedCursors().size()).isEqualTo(1);
    assertThat(shadow.getManagedCursors().get(0)).isSameAs(c);

    activity.stopManagingCursor(c);

    assertThat(shadow.getManagedCursors()).isNotNull();
    assertThat(shadow.getManagedCursors().size()).isEqualTo(0);
  }

  @Test
  public void setVolumeControlStream_setsTheSpecifiedStreamType() {
    TestActivity activity = new TestActivity();
    activity.setVolumeControlStream(AudioManager.STREAM_ALARM);
    assertThat(activity.getVolumeControlStream()).isEqualTo(AudioManager.STREAM_ALARM);
  }

  private static class TestActivity extends Activity {
    Transcript transcript = new Transcript();

    private boolean isRecreating = false;

    @Override
    public void onSaveInstanceState(Bundle outState) {
      isRecreating = true;
      transcript.add("onSaveInstanceState");
      outState.putString("TestActivityKey", "TestActivityValue");
      super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
      transcript.add("onRestoreInstanceState");
      assertTrue(savedInstanceState.containsKey("TestActivityKey"));
      assertEquals("TestActivityValue", savedInstanceState.getString("TestActivityKey"));
      super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
      transcript.add("onRetainNonConfigurationInstance");
      return new Integer(5);
    }

    @Override
    public void onPause() {
      transcript.add("onPause");
      super.onPause();
    }

    @Override
    public void onDestroy() {
      transcript.add("onDestroy");
      super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      transcript.add("onCreate");

      if (isRecreating) {
        assertTrue(savedInstanceState.containsKey("TestActivityKey"));
        assertEquals("TestActivityValue", savedInstanceState.getString("TestActivityKey"));
      }

      super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
      transcript.add("onStart");
      super.onStart();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
      transcript.add("onPostCreate");
      super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onStop() {
      transcript.add("onStop");
      super.onStop();
    }

    @Override
    public void onRestart() {
      transcript.add("onRestart");
      super.onRestart();
    }

    @Override
    public void onResume() {
      transcript.add("onResume");
      super.onResume();
    }
  }

  @Test
  public void callOnXxxMethods_shouldCallProtectedVersions() throws Exception {
    final Transcript transcript = new Transcript();

    Activity activity = new OnMethodTestActivity(transcript);

    ShadowActivity shadowActivity = shadowOf(activity);

    Bundle bundle = new Bundle();
    bundle.putString("key", "value");
    shadowActivity.callOnCreate(bundle);
    transcript.assertEventsSoFar("onCreate was called with value");

    shadowActivity.callOnStart();
    transcript.assertEventsSoFar("onStart was called");

    shadowActivity.callOnRestoreInstanceState(null);
    transcript.assertEventsSoFar("onRestoreInstanceState was called");

    shadowActivity.callOnPostCreate(null);
    transcript.assertEventsSoFar("onPostCreate was called");

    shadowActivity.callOnRestart();
    transcript.assertEventsSoFar("onRestart was called");

    shadowActivity.callOnResume();
    transcript.assertEventsSoFar("onResume was called");

    shadowActivity.callOnPostResume();
    transcript.assertEventsSoFar("onPostResume was called");

    Intent intent = new Intent("some action");
    shadowActivity.callOnNewIntent(intent);
    transcript.assertEventsSoFar("onNewIntent was called with " + intent);

    shadowActivity.callOnSaveInstanceState(null);
    transcript.assertEventsSoFar("onSaveInstanceState was called");

    shadowActivity.callOnPause();
    transcript.assertEventsSoFar("onPause was called");

    shadowActivity.callOnUserLeaveHint();
    transcript.assertEventsSoFar("onUserLeaveHint was called");

    shadowActivity.callOnStop();
    transcript.assertEventsSoFar("onStop was called");

    shadowActivity.callOnDestroy();
    transcript.assertEventsSoFar("onDestroy was called");
  }

  @Test
  public void callOnXxxMethods_shouldWorkIfNotDeclaredOnConcreteClass() throws Exception {
    Activity activity = new Activity() {
    };
    shadowOf(activity).callOnStart();
  }

  @Test
  public void getAndSetParentActivity_shouldWorkForTestingPurposes() throws Exception {
    Activity parentActivity = new Activity() {
    };
    Activity activity = new Activity() {
    };
    shadowOf(activity).setParent(parentActivity);
    assertSame(parentActivity, activity.getParent());
  }

  @Test
  public void getAndSetRequestedOrientation_shouldRemember() throws Exception {
    Activity activity = new Activity() {
    };
    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, activity.getRequestedOrientation());
  }

  @Test
  public void getAndSetRequestedOrientation_shouldDelegateToParentIfPresent() throws Exception {
    Activity parentActivity = new Activity() {
    };
    Activity activity = new Activity() {
    };
    shadowOf(activity).setParent(parentActivity);
    parentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, activity.getRequestedOrientation());
    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
    assertEquals(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, parentActivity.getRequestedOrientation());
  }

  @Test
  public void shouldSupportIsTaskRoot() throws Exception {
    Activity activity = new Activity();
    assertTrue(activity.isTaskRoot()); // as implemented, Activities are considered task roots by default

    shadowOf(activity).setIsTaskRoot(false);
    assertFalse(activity.isTaskRoot());
  }

  @Test
  public void getPendingTransitionEnterAnimationResourceId_should() throws Exception {
    Activity activity = new Activity();
    activity.overridePendingTransition(15, 2);
    assertThat(shadowOf(activity).getPendingTransitionEnterAnimationResourceId()).isEqualTo(15);
  }

  @Test
  public void getPendingTransitionExitAnimationResourceId_should() throws Exception {
    Activity activity = new Activity();
    activity.overridePendingTransition(15, 2);
    assertThat(shadowOf(activity).getPendingTransitionExitAnimationResourceId()).isEqualTo(2);
  }

  @Test
  public void getActionBar_shouldWorkIfActivityHasAnAppropriateTheme() throws Exception {
    ActionBarThemedActivity myActivity = Robolectric.buildActivity(ActionBarThemedActivity.class).create().get();
    ActionBar actionBar = myActivity.getActionBar();
    assertThat(actionBar).isNotNull();
  }

  public static class ActionBarThemedActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setTheme(android.R.style.Theme_Holo_Light);
      setContentView(new LinearLayout(this));
    }
  }

  @Test
  public void shouldSetCustomTitle() {
    CustomTitleActivity activity = create(CustomTitleActivity.class);
    assertThat(activity.customTitleText).isNotNull();
    assertThat(activity.customTitleText.getText().toString()).isEqualTo(activity.getString(R.string.hello));
  }

  @Test @Config(emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
  public void canGetOptionsMenu() throws Exception {
    Activity activity = buildActivity(OptionsMenuActivity.class).create().visible().get();
    Menu optionsMenu = shadowOf(activity).getOptionsMenu();
    assertThat(optionsMenu).isNotNull();
    assertThat(optionsMenu.getItem(0).getTitle()).isEqualTo("Algebraic!");
  }

  /////////////////////////////

  private void destroy(Activity activity) {
    new ActivityController(activity).destroy();
  }

  private <T extends Activity> T create(Class<T> activityClass) {
    return Robolectric.buildActivity(activityClass).create().get();
  }

  public AndroidManifest newConfigWith(String contents) throws IOException {
    return newConfigWith("org.robolectric", contents);
  }

  private AndroidManifest newConfigWith(String packageName, String contents) throws IOException {
    File f = temporaryFolder.newFile("whatever.xml",
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
            "          package=\"" + packageName + "\">\n" +
            "    " + contents + "\n" +
            "</manifest>\n");
    return new AndroidManifest(Fs.newFile(f), null, null);
  }

  private static class DialogCreatingActivity extends Activity {
    @Override
    protected Dialog onCreateDialog(int id) {
      return new Dialog(this);
    }
  }

  private static class OptionsMenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      // Requesting the action bar causes it to be properly initialized when the Activity becomes visible
      getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
      setContentView(new FrameLayout(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      super.onCreateOptionsMenu(menu);
      menu.add("Algebraic!");
      return true;
    }
  }

  private static class DialogLifeCycleActivity extends Activity {
    public boolean createdDialog = false;
    public boolean preparedDialog = false;
    public boolean preparedDialogWithBundle = false;
    public Dialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(new FrameLayout(this));
    }

    @Override
    protected void onDestroy() {
      super.onDestroy();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
      createdDialog = true;
      return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
      preparedDialog = true;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {
      preparedDialogWithBundle = true;
    }
  }

  private static class ActivityWithOnCreateDialog extends Activity {
    boolean onCreateDialogWasCalled = false;

    @Override
    protected Dialog onCreateDialog(int id) {
      onCreateDialogWasCalled = true;
      return new Dialog(this);
    }
  }

  private static class ActivityWithContentChangedTranscript extends Activity {
    private Transcript transcript;

    @Override
    public void onContentChanged() {
      transcript.add("onContentChanged was called; title is \"" + shadowOf(findViewById(R.id.title)).innerText() + "\"");
    }

    private void setTranscript(Transcript transcript) {
      this.transcript = transcript;
    }
  }

  private static class CustomTitleActivity extends Activity {
    public TextView customTitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
      setContentView(R.layout.main);
      getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

      customTitleText = (TextView) findViewById(R.id.custom_title_text);
    }
  }
}
