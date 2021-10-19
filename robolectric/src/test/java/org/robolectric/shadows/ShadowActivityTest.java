package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Looper.getMainLooper;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Robolectric.setupActivity;
import static org.robolectric.RuntimeEnvironment.getApplication;
import static org.robolectric.RuntimeEnvironment.systemContext;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Application;
import android.app.Dialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SearchView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.LooperMode.Mode;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowActivity.IntentSenderRequest;
import org.robolectric.util.TestRunnable;

/** Test of ShadowActivity. */
@RunWith(AndroidJUnit4.class)
@SuppressWarnings("RobolectricSystemContext") // preexisting when check was enabled
public class ShadowActivityTest {
  private Activity activity;

  @Test
  public void shouldUseApplicationLabelFromManifestAsTitleForActivity() throws Exception {
    activity = Robolectric.setupActivity(LabelTestActivity1.class);
    assertThat(activity.getTitle()).isNotNull();
    assertThat(activity.getTitle().toString()).isEqualTo(activity.getString(R.string.app_name));
  }

  @Test
  public void shouldUseActivityLabelFromManifestAsTitleForActivity() throws Exception {
    activity = Robolectric.setupActivity(LabelTestActivity2.class);
    assertThat(activity.getTitle()).isNotNull();
    assertThat(activity.getTitle().toString())
        .isEqualTo(activity.getString(R.string.activity_name));
  }

  @Test
  public void shouldUseActivityLabelFromManifestAsTitleForActivityWithShortName() throws Exception {
    activity = Robolectric.setupActivity(LabelTestActivity3.class);
    assertThat(activity.getTitle()).isNotNull();
    assertThat(activity.getTitle().toString())
        .isEqualTo(activity.getString(R.string.activity_name));
  }

  @Test
  public void createActivity_noDisplayFinished_shouldFinishActivity() {
    ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class);
    controller.get().setTheme(android.R.style.Theme_NoDisplay);
    controller.create();
    controller.get().finish();
    controller.start().visible().resume();

    activity = controller.get();
    assertThat(activity.isFinishing()).isTrue();
  }

  @Config(minSdk = M)
  @Test
  public void createActivity_noDisplayNotFinished_shouldThrowIllegalStateException() {
    try {
      ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class);
      controller.get().setTheme(android.R.style.Theme_NoDisplay);
      controller.setup();

      // For apps targeting above Lollipop MR1, an exception "Activity <activity> did not call
      // finish() prior to onResume() completing" will be thrown
      fail("IllegalStateException should be thrown");
    } catch (IllegalStateException e) {
      // pass
    }
  }

  public static final class LabelTestActivity1 extends Activity {}

  public static final class LabelTestActivity2 extends Activity {}

  public static final class LabelTestActivity3 extends Activity {}

  @Test
  public void
      shouldNotComplainIfActivityIsDestroyedWhileAnotherActivityHasRegisteredBroadcastReceivers()
          throws Exception {
    ActivityController<DialogCreatingActivity> controller =
        Robolectric.buildActivity(DialogCreatingActivity.class);
    activity = controller.get();

    DialogLifeCycleActivity activity2 = Robolectric.setupActivity(DialogLifeCycleActivity.class);
    activity2.registerReceiver(new AppWidgetProvider(), new IntentFilter());

    controller.destroy();
  }

  @Test
  public void shouldNotRegisterNullBroadcastReceiver() {
    ActivityController<DialogCreatingActivity> controller =
        Robolectric.buildActivity(DialogCreatingActivity.class);
    activity = controller.get();
    activity.registerReceiver(null, new IntentFilter());

    controller.destroy();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void shouldReportDestroyedStatus() {
    ActivityController<DialogCreatingActivity> controller =
        Robolectric.buildActivity(DialogCreatingActivity.class);
    activity = controller.get();

    controller.destroy();
    assertThat(activity.isDestroyed()).isTrue();
  }

  @Test
  public void startActivity_shouldDelegateToStartActivityForResult() {

    TranscriptActivity activity = Robolectric.setupActivity(TranscriptActivity.class);

    activity.startActivity(new Intent().setType("image/*"));

    shadowOf(activity)
        .receiveResult(
            new Intent().setType("image/*"),
            Activity.RESULT_OK,
            new Intent().setData(Uri.parse("content:foo")));
    assertThat(activity.transcript)
        .containsExactly(
            "onActivityResult called with requestCode -1, resultCode -1, intent data content:foo");
  }

  public static class TranscriptActivity extends Activity {
    final List<String> transcript = new ArrayList<>();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      transcript.add(
          "onActivityResult called with requestCode "
              + requestCode
              + ", resultCode "
              + resultCode
              + ", intent data "
              + data.getData());
    }
  }

  @Test
  public void startActivities_shouldStartAllActivities() {
    activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);

    final Intent view = new Intent(Intent.ACTION_VIEW);
    final Intent pick = new Intent(Intent.ACTION_PICK);
    activity.startActivities(new Intent[] {view, pick});

    assertThat(shadowOf(activity).getNextStartedActivity()).isEqualTo(pick);
    assertThat(shadowOf(activity).getNextStartedActivity()).isEqualTo(view);
  }

  @Test
  public void startActivities_withBundle_shouldStartAllActivities() {
    activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);

    final Intent view = new Intent(Intent.ACTION_VIEW);
    final Intent pick = new Intent(Intent.ACTION_PICK);
    activity.startActivities(new Intent[] {view, pick}, new Bundle());

    assertThat(shadowOf(activity).getNextStartedActivity()).isEqualTo(pick);
    assertThat(shadowOf(activity).getNextStartedActivity()).isEqualTo(view);
  }

  @Test
  public void startActivityForResultAndReceiveResult_shouldSendResponsesBackToActivity()
      throws Exception {
    TranscriptActivity activity = Robolectric.setupActivity(TranscriptActivity.class);
    activity.startActivityForResult(new Intent().setType("audio/*"), 123);
    activity.startActivityForResult(new Intent().setType("image/*"), 456);

    shadowOf(activity)
        .receiveResult(
            new Intent().setType("image/*"),
            Activity.RESULT_OK,
            new Intent().setData(Uri.parse("content:foo")));
    assertThat(activity.transcript)
        .containsExactly(
            "onActivityResult called with requestCode 456, resultCode -1, intent data content:foo");
  }

  @Test
  public void startActivityForResultAndReceiveResult_whenNoIntentMatches_shouldThrowException()
      throws Exception {
    ThrowOnResultActivity activity = Robolectric.buildActivity(ThrowOnResultActivity.class).get();
    activity.startActivityForResult(new Intent().setType("audio/*"), 123);
    activity.startActivityForResult(new Intent().setType("image/*"), 456);

    Intent requestIntent = new Intent().setType("video/*");
    try {
      shadowOf(activity)
          .receiveResult(
              requestIntent, Activity.RESULT_OK, new Intent().setData(Uri.parse("content:foo")));
      fail();
    } catch (Exception e) {
      assertThat(e.getMessage()).startsWith("No intent matches " + requestIntent);
    }
  }

  public static class ThrowOnResultActivity extends Activity {
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      throw new IllegalStateException("should not be called");
    }
  }

  @Test
  public void shouldSupportStartActivityForResult() throws Exception {
    activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);
    Intent intent = new Intent().setClass(activity, DialogLifeCycleActivity.class);
    assertThat(shadowOf(activity).getNextStartedActivity()).isNull();

    activity.startActivityForResult(intent, 142);

    Intent startedIntent = shadowOf(activity).getNextStartedActivity();
    assertThat(startedIntent).isNotNull();
    assertThat(startedIntent).isSameInstanceAs(intent);
  }

  @Test
  public void shouldSupportGetStartedActivitiesForResult() throws Exception {
    activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);
    Intent intent = new Intent().setClass(activity, DialogLifeCycleActivity.class);

    activity.startActivityForResult(intent, 142);

    ShadowActivity.IntentForResult intentForResult =
        shadowOf(activity).getNextStartedActivityForResult();
    assertThat(intentForResult).isNotNull();
    assertThat(shadowOf(activity).getNextStartedActivityForResult()).isNull();
    assertThat(intentForResult.intent).isNotNull();
    assertThat(intentForResult.intent).isSameInstanceAs(intent);
    assertThat(intentForResult.requestCode).isEqualTo(142);
  }

  @Test
  public void shouldSupportPeekStartedActivitiesForResult() throws Exception {
    activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);
    Intent intent = new Intent().setClass(activity, DialogLifeCycleActivity.class);

    activity.startActivityForResult(intent, 142);

    ShadowActivity.IntentForResult intentForResult =
        shadowOf(activity).peekNextStartedActivityForResult();
    assertThat(intentForResult).isNotNull();
    assertThat(shadowOf(activity).peekNextStartedActivityForResult())
        .isSameInstanceAs(intentForResult);
    assertThat(intentForResult.intent).isNotNull();
    assertThat(intentForResult.intent).isSameInstanceAs(intent);
    assertThat(intentForResult.requestCode).isEqualTo(142);
  }

  @Test
  public void onContentChangedShouldBeCalledAfterContentViewIsSet() throws RuntimeException {
    final List<String> transcript = new ArrayList<>();
    ActivityWithContentChangedTranscript customActivity =
        Robolectric.setupActivity(ActivityWithContentChangedTranscript.class);
    customActivity.setTranscript(transcript);
    customActivity.setContentView(R.layout.main);
    assertThat(transcript).containsExactly("onContentChanged was called; title is \"Main Layout\"");
  }

  @Test
  public void shouldRetrievePackageNameFromTheManifest() throws Exception {
    assertThat(Robolectric.setupActivity(Activity.class).getPackageName())
        .isEqualTo(ApplicationProvider.getApplicationContext().getPackageName());
  }

  @Test
  public void shouldRunUiTasksImmediatelyByDefault() throws Exception {
    TestRunnable runnable = new TestRunnable();
    activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);
    activity.runOnUiThread(runnable);
    assertTrue(runnable.wasRun);
  }

  @Test
  @LooperMode(LEGACY)
  public void shouldQueueUiTasksWhenUiThreadIsPaused() throws Exception {
    shadowOf(getMainLooper()).pause();

    activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);
    TestRunnable runnable = new TestRunnable();
    activity.runOnUiThread(runnable);
    assertFalse(runnable.wasRun);

    shadowOf(getMainLooper()).idle();
    assertTrue(runnable.wasRun);
  }

  /**
   * The legacy behavior spec-ed in {@link #shouldQueueUiTasksWhenUiThreadIsPaused()} is actually
   * incorrect. The {@link Activity#runOnUiThread} will execute posted tasks inline.
   */
  @Test
  @LooperMode(Mode.PAUSED)
  public void shouldExecutePostedUiTasksInRealisticLooper() throws Exception {
    activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);
    TestRunnable runnable = new TestRunnable();
    activity.runOnUiThread(runnable);
    assertTrue(runnable.wasRun);
  }

  @Test
  public void showDialog_shouldCreatePrepareAndShowDialog() {
    final DialogLifeCycleActivity activity =
        Robolectric.setupActivity(DialogLifeCycleActivity.class);
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
    final DialogLifeCycleActivity activity =
        Robolectric.setupActivity(DialogLifeCycleActivity.class);
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
  public void showDialog_shouldReturnFalseIfDialogDoesNotExist() {
    final DialogLifeCycleActivity activity =
        Robolectric.setupActivity(DialogLifeCycleActivity.class);
    boolean dialogCreated = activity.showDialog(97, new Bundle());

    assertThat(dialogCreated).isFalse();
    assertThat(activity.createdDialog).isTrue();
    assertThat(activity.preparedDialogWithBundle).isFalse();
  }

  @Test
  public void showDialog_shouldReuseDialogs() {
    final DialogCreatingActivity activity = Robolectric.setupActivity(DialogCreatingActivity.class);
    activity.showDialog(1);
    Dialog firstDialog = ShadowDialog.getLatestDialog();
    activity.showDialog(1);

    Dialog secondDialog = ShadowDialog.getLatestDialog();
    assertSame("dialogs should be the same instance", firstDialog, secondDialog);
  }

  @Test
  public void showDialog_shouldShowDialog() throws Exception {
    final DialogCreatingActivity activity = Robolectric.setupActivity(DialogCreatingActivity.class);
    activity.showDialog(1);
    Dialog dialog = ShadowDialog.getLatestDialog();
    assertTrue(dialog.isShowing());
  }

  @Test
  public void dismissDialog_shouldDismissPreviouslyShownDialog() throws Exception {
    final DialogCreatingActivity activity = Robolectric.setupActivity(DialogCreatingActivity.class);
    activity.showDialog(1);
    activity.dismissDialog(1);
    Dialog dialog = ShadowDialog.getLatestDialog();
    assertFalse(dialog.isShowing());
  }

  @Test
  public void dismissDialog_shouldThrowExceptionIfDialogWasNotPreviouslyShown() throws Exception {
    final DialogCreatingActivity activity = Robolectric.setupActivity(DialogCreatingActivity.class);
    try {
      activity.dismissDialog(1);
    } catch (Throwable expected) {
      assertThat(expected).isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Test
  public void removeDialog_shouldCreateDialogAgain() {
    final DialogCreatingActivity activity = Robolectric.setupActivity(DialogCreatingActivity.class);
    activity.showDialog(1);
    Dialog firstDialog = ShadowDialog.getLatestDialog();

    activity.removeDialog(1);
    assertNull(shadowOf(activity).getDialogById(1));

    activity.showDialog(1);
    Dialog secondDialog = ShadowDialog.getLatestDialog();

    assertNotSame("dialogs should not be the same instance", firstDialog, secondDialog);
  }

  @Test
  public void shouldCallOnCreateDialogFromShowDialog() {
    ActivityWithOnCreateDialog activity =
        Robolectric.setupActivity(ActivityWithOnCreateDialog.class);
    activity.showDialog(123);
    assertTrue(activity.onCreateDialogWasCalled);
    assertThat(ShadowDialog.getLatestDialog()).isNotNull();
  }

  @Test
  public void shouldCallFinishInOnBackPressed() {
    Activity activity = new Activity();
    activity.onBackPressed();

    assertTrue(activity.isFinishing());
  }

  @Test
  @Config(minSdk = JELLY_BEAN)
  public void shouldCallFinishOnFinishAffinity() {
    Activity activity = new Activity();
    activity.finishAffinity();

    assertTrue(activity.isFinishing());
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void shouldCallFinishOnFinishAndRemoveTask() {
    Activity activity = new Activity();
    activity.finishAndRemoveTask();

    assertTrue(activity.isFinishing());
  }

  @Test
  public void shouldCallFinishOnFinish() {
    Activity activity = new Activity();
    activity.finish();

    assertTrue(activity.isFinishing());
  }

  @Test
  public void shouldSupportCurrentFocus() {
    activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);

    assertNull(activity.getCurrentFocus());
    View view = new View(activity);
    shadowOf(activity).setCurrentFocus(view);
    assertEquals(view, activity.getCurrentFocus());
  }

  @Test
  public void shouldSetOrientation() {
    activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);
    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    assertThat(activity.getRequestedOrientation())
        .isEqualTo(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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

    for (int mode : modes) {
      activity.setDefaultKeyMode(mode);
      assertWithMessage("Unexpected key mode")
          .that(shadowOf(activity).getDefaultKeymode())
          .isEqualTo(mode);
    }
  }

  @Test // unclear what the correct behavior should be here...
  public void shouldPopulateWindowDecorViewWithMergeLayoutContents() throws Exception {
    Activity activity = Robolectric.buildActivity(Activity.class).create().get();
    activity.setContentView(R.layout.toplevel_merge);

    View contentView = activity.findViewById(android.R.id.content);
    assertThat(((ViewGroup) contentView).getChildCount()).isEqualTo(2);
  }

  @Test
  public void setContentView_shouldReplaceOldContentView() throws Exception {
    View view1 = new View(getApplication());
    view1.setId(R.id.burritos);
    View view2 = new View(getApplication());
    view2.setId(R.id.button);

    Activity activity = buildActivity(Activity.class).create().get();
    activity.setContentView(view1);
    assertSame(view1, activity.findViewById(R.id.burritos));

    activity.setContentView(view2);
    assertNull(activity.findViewById(R.id.burritos));
    assertSame(view2, activity.findViewById(R.id.button));
  }

  @Test
  public void onKeyUp_callsOnBackPressedWhichFinishesTheActivity() throws Exception {
    OnBackPressedActivity activity = buildActivity(OnBackPressedActivity.class).setup().get();
    boolean downConsumed =
        activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
    boolean upConsumed =
        activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));

    assertTrue(downConsumed);
    assertTrue(upConsumed);
    assertTrue(activity.onBackPressedCalled);
    assertTrue(activity.isFinishing());
  }

  @Test
  public void shouldGiveSharedPreferences() throws Exception {
    Activity activity = Robolectric.setupActivity(Activity.class);
    SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
    assertNotNull(preferences);
    preferences.edit().putString("foo", "bar").commit();
    assertThat(activity.getPreferences(Context.MODE_PRIVATE).getString("foo", null))
        .isEqualTo("bar");
  }

  @Test
  public void shouldFindContentViewContainerWithChild() throws Exception {
    Activity activity = buildActivity(Activity.class).create().get();
    View contentView = new View(activity);
    activity.setContentView(contentView);

    FrameLayout contentViewContainer = (FrameLayout) activity.findViewById(android.R.id.content);
    assertThat(contentViewContainer.getChildAt(0)).isSameInstanceAs(contentView);
  }

  @Test
  public void shouldFindContentViewContainerWithoutChild() throws Exception {
    Activity activity = buildActivity(Activity.class).create().get();

    FrameLayout contentViewContainer = (FrameLayout) activity.findViewById(android.R.id.content);
    assertThat(contentViewContainer.getId()).isEqualTo(android.R.id.content);
  }

  @Test
  public void recreateGoesThroughFullLifeCycle() throws Exception {
    ActivityController<TestActivity> activityController =
        buildActivity(TestActivity.class).create();
    TestActivity oldActivity = activityController.get();

    // Recreate should create new instance.
    activityController.recreate();

    assertThat(activityController.get()).isNotSameInstanceAs(oldActivity);

    assertThat(oldActivity.transcript)
        .containsExactly(
            "onCreate",
            "onStart",
            "onPostCreate",
            "onResume",
            "onPause",
            "onStop",
            "onSaveInstanceState",
            "onRetainNonConfigurationInstance",
            "onDestroy")
        .inOrder();
    assertThat(activityController.get().transcript)
        .containsExactly(
            "onCreate", "onStart", "onRestoreInstanceState", "onPostCreate", "onResume")
        .inOrder();
  }

  @Test
  public void recreateBringsBackTheOriginalLifeCycleStateAfterRecreate_resumed() throws Exception {
    ActivityController<TestActivity> activityController = buildActivity(TestActivity.class).setup();
    TestActivity oldActivity = activityController.get();

    // Recreate the paused activity.
    activityController.recreate();

    assertThat(activityController.get()).isNotSameInstanceAs(oldActivity);

    assertThat(oldActivity.transcript)
        .containsExactly(
            "onCreate",
            "onStart",
            "onPostCreate",
            "onResume",
            "onPause",
            "onStop",
            "onSaveInstanceState",
            "onRetainNonConfigurationInstance",
            "onDestroy")
        .inOrder();
    assertThat(activityController.get().transcript)
        .containsExactly(
            "onCreate", "onStart", "onRestoreInstanceState", "onPostCreate", "onResume")
        .inOrder();
  }

  @Test
  public void recreateBringsBackTheOriginalLifeCycleStateAfterRecreate_paused() throws Exception {
    ActivityController<TestActivity> activityController = buildActivity(TestActivity.class).setup();
    TestActivity oldActivity = activityController.get();

    // Recreate the paused activity.
    activityController.pause();
    activityController.recreate();

    assertThat(activityController.get()).isNotSameInstanceAs(oldActivity);

    assertThat(oldActivity.transcript)
        .containsExactly(
            "onCreate",
            "onStart",
            "onPostCreate",
            "onResume",
            "onPause",
            "onStop",
            "onSaveInstanceState",
            "onRetainNonConfigurationInstance",
            "onDestroy")
        .inOrder();
    assertThat(activityController.get().transcript)
        .containsExactly(
            "onCreate", "onStart", "onRestoreInstanceState", "onPostCreate", "onResume", "onPause")
        .inOrder();
  }

  @Test
  public void recreateBringsBackTheOriginalLifeCycleStateAfterRecreate_stopped() throws Exception {
    ActivityController<TestActivity> activityController = buildActivity(TestActivity.class).setup();
    TestActivity oldActivity = activityController.get();

    // Recreate the stopped activity.
    activityController.pause().stop();
    activityController.recreate();

    assertThat(activityController.get()).isNotSameInstanceAs(oldActivity);

    assertThat(oldActivity.transcript)
        .containsExactly(
            "onCreate",
            "onStart",
            "onPostCreate",
            "onResume",
            "onPause",
            "onStop",
            "onSaveInstanceState",
            "onRetainNonConfigurationInstance",
            "onDestroy")
        .inOrder();
    assertThat(activityController.get().transcript)
        .containsExactly(
            "onCreate",
            "onStart",
            "onRestoreInstanceState",
            "onPostCreate",
            "onResume",
            "onPause",
            "onStop")
        .inOrder();
  }

  @Test
  public void startAndStopManagingCursorTracksCursors() throws Exception {
    TestActivity activity = new TestActivity();

    assertThat(shadowOf(activity).getManagedCursors()).isNotNull();
    assertThat(shadowOf(activity).getManagedCursors()).isEmpty();

    Cursor c = Shadow.newInstanceOf(SQLiteCursor.class);
    activity.startManagingCursor(c);

    assertThat(shadowOf(activity).getManagedCursors()).isNotNull();
    assertThat(shadowOf(activity).getManagedCursors()).hasSize(1);
    assertThat(shadowOf(activity).getManagedCursors().get(0)).isSameInstanceAs(c);

    activity.stopManagingCursor(c);

    assertThat(shadowOf(activity).getManagedCursors()).isNotNull();
    assertThat(shadowOf(activity).getManagedCursors()).isEmpty();
  }

  @Test
  public void setVolumeControlStream_setsTheSpecifiedStreamType() {
    TestActivity activity = new TestActivity();
    activity.setVolumeControlStream(AudioManager.STREAM_ALARM);
    assertThat(activity.getVolumeControlStream()).isEqualTo(AudioManager.STREAM_ALARM);
  }

  @Test
  public void decorViewSizeEqualToDisplaySize() {
    Activity activity = buildActivity(Activity.class).create().visible().get();
    View decorView = activity.getWindow().getDecorView();
    assertThat(decorView).isNotEqualTo(null);
    ViewRootImpl root = decorView.getViewRootImpl();
    assertThat(root).isNotEqualTo(null);
    assertThat(decorView.getWidth()).isNotEqualTo(0);
    assertThat(decorView.getHeight()).isNotEqualTo(0);
    Display display = ShadowDisplay.getDefaultDisplay();
    assertThat(decorView.getWidth()).isEqualTo(display.getWidth());
    assertThat(decorView.getHeight()).isEqualTo(display.getHeight());
  }

  @Test
  @Config(minSdk = M)
  public void requestsPermissions() {
    TestActivity activity = Robolectric.setupActivity(TestActivity.class);
    activity.requestPermissions(new String[] {Manifest.permission.CAMERA}, 1007);
  }

  private static class TestActivity extends Activity {
    List<String> transcript = new ArrayList<>();

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
      return 5;
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
  public void getAndSetParentActivity_shouldWorkForTestingPurposes() throws Exception {
    Activity parentActivity = new Activity();
    Activity activity = new Activity();
    shadowOf(activity).setParent(parentActivity);
    assertSame(parentActivity, activity.getParent());
  }

  @Test
  public void getAndSetRequestedOrientation_shouldRemember() throws Exception {
    Activity activity = new Activity();
    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, activity.getRequestedOrientation());
  }

  @Test
  public void getAndSetRequestedOrientation_shouldDelegateToParentIfPresent() throws Exception {
    Activity parentActivity = new Activity();
    Activity activity = new Activity();
    shadowOf(activity).setParent(parentActivity);
    parentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, activity.getRequestedOrientation());
    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
    assertEquals(
        ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
        parentActivity.getRequestedOrientation());
  }

  @Test
  public void shouldSupportIsTaskRoot() throws Exception {
    Activity activity = Robolectric.setupActivity(Activity.class);
    assertTrue(
        activity.isTaskRoot()); // as implemented, Activities are considered task roots by default

    shadowOf(activity).setIsTaskRoot(false);
    assertFalse(activity.isTaskRoot());
  }

  @Test
  @Config(minSdk = N)
  public void shouldSupportIsInMultiWindowMode() throws Exception {
    Activity activity = Robolectric.setupActivity(Activity.class);

    assertThat(activity.isInMultiWindowMode())
        .isFalse(); // Activity is not in multi window mode by default.
    shadowOf(activity).setInMultiWindowMode(true);

    assertThat(activity.isInMultiWindowMode()).isTrue();
  }

  @Test
  public void getPendingTransitionEnterAnimationResourceId_should() throws Exception {
    Activity activity = Robolectric.setupActivity(Activity.class);
    activity.overridePendingTransition(15, 2);
    assertThat(shadowOf(activity).getPendingTransitionEnterAnimationResourceId()).isEqualTo(15);
  }

  @Test
  public void getPendingTransitionExitAnimationResourceId_should() throws Exception {
    Activity activity = Robolectric.setupActivity(Activity.class);
    activity.overridePendingTransition(15, 2);
    assertThat(shadowOf(activity).getPendingTransitionExitAnimationResourceId()).isEqualTo(2);
  }

  @Test
  public void getActionBar_shouldWorkIfActivityHasAnAppropriateTheme() throws Exception {
    ActionBarThemedActivity myActivity =
        Robolectric.buildActivity(ActionBarThemedActivity.class).create().get();
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
  public void canGetOptionsMenu() throws Exception {
    Activity activity = buildActivity(OptionsMenuActivity.class).create().visible().get();
    Menu optionsMenu = shadowOf(activity).getOptionsMenu();
    assertThat(optionsMenu).isNotNull();
    assertThat(optionsMenu.getItem(0).getTitle().toString()).isEqualTo("Algebraic!");
  }

  @Test
  public void canGetOptionsMenuWithActionMenu() throws Exception {
    ActionMenuActivity activity = buildActivity(ActionMenuActivity.class).create().visible().get();

    SearchView searchView = activity.mSearchView;
    // This blows up when ShadowPopupMenu existed.
    searchView.setIconifiedByDefault(false);
  }

  @Test
  public void canStartActivityFromFragment() {
    final Activity activity = Robolectric.setupActivity(Activity.class);

    Intent intent = new Intent(Intent.ACTION_VIEW);
    activity.startActivityFromFragment(new Fragment(), intent, 4);

    ShadowActivity.IntentForResult intentForResult =
        shadowOf(activity).getNextStartedActivityForResult();
    assertThat(intentForResult.intent).isSameInstanceAs(intent);
    assertThat(intentForResult.requestCode).isEqualTo(4);
  }

  @Test
  public void canStartActivityFromFragment_withBundle() {
    final Activity activity = buildActivity(Activity.class).create().get();

    Bundle options = new Bundle();
    Intent intent = new Intent(Intent.ACTION_VIEW);
    activity.startActivityFromFragment(new Fragment(), intent, 5, options);

    ShadowActivity.IntentForResult intentForResult =
        shadowOf(activity).getNextStartedActivityForResult();
    assertThat(intentForResult.intent).isSameInstanceAs(intent);
    assertThat(intentForResult.options).isSameInstanceAs(options);
    assertThat(intentForResult.requestCode).isEqualTo(5);
  }

  @Test
  public void shouldUseAnimationOverride() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    Intent intent = new Intent(activity, OptionsMenuActivity.class);

    Bundle animationBundle =
        ActivityOptions.makeCustomAnimation(activity, R.anim.test_anim_1, R.anim.test_anim_1)
            .toBundle();
    activity.startActivity(intent, animationBundle);
    assertThat(shadowOf(activity).getNextStartedActivityForResult().options)
        .isSameInstanceAs(animationBundle);
  }

  @Test
  public void shouldCallActivityLifecycleCallbacks() {
    final List<String> transcript = new ArrayList<>();
    final ActivityController<Activity> controller = buildActivity(Activity.class);
    Application applicationContext = ApplicationProvider.getApplicationContext();
    applicationContext.registerActivityLifecycleCallbacks(
        new ActivityLifecycleCallbacks(transcript));

    controller.create();
    assertThat(transcript).containsExactly("onActivityCreated");
    transcript.clear();

    controller.start();
    assertThat(transcript).containsExactly("onActivityStarted");
    transcript.clear();

    controller.resume();
    assertThat(transcript).containsExactly("onActivityResumed");
    transcript.clear();

    controller.saveInstanceState(new Bundle());
    assertThat(transcript).containsExactly("onActivitySaveInstanceState");
    transcript.clear();

    controller.pause();
    assertThat(transcript).containsExactly("onActivityPaused");
    transcript.clear();

    controller.stop();
    assertThat(transcript).containsExactly("onActivityStopped");
    transcript.clear();

    controller.destroy();
    assertThat(transcript).containsExactly("onActivityDestroyed");
  }

  /** Activity for testing */
  public static class ChildActivity extends Activity {}

  /** Activity for testing */
  public static class ParentActivity extends Activity {}

  @Test
  public void getParentActivityIntent() {
    Activity activity = setupActivity(ChildActivity.class);

    assertThat(activity.getParentActivityIntent().getComponent().getClassName())
        .isEqualTo(ParentActivity.class.getName());
  }

  @Test
  public void getCallingActivity_defaultsToNull() {
    Activity activity = Robolectric.setupActivity(Activity.class);

    assertNull(activity.getCallingActivity());
  }

  @Test
  public void getCallingActivity_returnsSetValue() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    ComponentName componentName = new ComponentName("com.example.package", "SomeActivity");

    shadowOf(activity).setCallingActivity(componentName);

    assertEquals(componentName, activity.getCallingActivity());
  }

  @Test
  public void getCallingActivity_returnsValueSetInPackage() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    String packageName = "com.example.package";

    shadowOf(activity).setCallingPackage(packageName);

    assertEquals(packageName, activity.getCallingActivity().getPackageName());
  }

  @Test
  public void getCallingActivity_notOverwrittenByPackage() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    ComponentName componentName = new ComponentName("com.example.package", "SomeActivity");

    shadowOf(activity).setCallingActivity(componentName);
    shadowOf(activity).setCallingPackage(componentName.getPackageName());

    assertEquals(componentName, activity.getCallingActivity());
  }

  @Test
  public void getCallingPackage_defaultsToNull() {
    Activity activity = Robolectric.setupActivity(Activity.class);

    assertNull(activity.getCallingPackage());
  }

  @Test
  public void getCallingPackage_returnsSetValue() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    String packageName = "com.example.package";

    shadowOf(activity).setCallingPackage(packageName);

    assertEquals(packageName, activity.getCallingPackage());
  }

  @Test
  public void getCallingPackage_returnsValueSetInActivity() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    ComponentName componentName = new ComponentName("com.example.package", "SomeActivity");

    shadowOf(activity).setCallingActivity(componentName);

    assertEquals(componentName.getPackageName(), activity.getCallingPackage());
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void lockTask() {
    Activity activity = Robolectric.setupActivity(Activity.class);

    assertThat(shadowOf(activity).isLockTask()).isFalse();

    activity.startLockTask();
    assertThat(shadowOf(activity).isLockTask()).isTrue();

    activity.stopLockTask();
    assertThat(shadowOf(activity).isLockTask()).isFalse();
  }

  @Test
  @Config(minSdk = M)
  public void getPermission_shouldReturnRequestedPermissions() {
    // GIVEN
    String[] permission = {Manifest.permission.CAMERA};
    int requestCode = 1007;
    Activity activity = Robolectric.setupActivity(Activity.class);

    // WHEN
    activity.requestPermissions(permission, requestCode);

    // THEN
    ShadowActivity.PermissionsRequest request = shadowOf(activity).getLastRequestedPermission();
    assertThat(request.requestCode).isEqualTo(requestCode);
    assertThat(request.requestedPermissions).isEqualTo(permission);
  }

  @Test
  public void getLastIntentSenderRequest() throws IntentSender.SendIntentException {
    Activity activity = Robolectric.setupActivity(Activity.class);
    int requestCode = 108;
    Intent intent = new Intent("action");
    Intent fillInIntent = new Intent();
    PendingIntent pendingIntent = PendingIntent.getActivity(systemContext, requestCode, intent, 0);

    Bundle options = new Bundle();
    int flagsMask = 1;
    int flagsValues = 2;
    int extraFlags = 3;
    IntentSender intentSender = pendingIntent.getIntentSender();
    activity.startIntentSenderForResult(
        intentSender, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags, options);

    IntentSenderRequest lastIntentSenderRequest = shadowOf(activity).getLastIntentSenderRequest();
    assertThat(lastIntentSenderRequest.intentSender).isEqualTo(intentSender);
    assertThat(lastIntentSenderRequest.fillInIntent).isEqualTo(fillInIntent);
    assertThat(lastIntentSenderRequest.requestCode).isEqualTo(requestCode);
    assertThat(lastIntentSenderRequest.flagsMask).isEqualTo(flagsMask);
    assertThat(lastIntentSenderRequest.flagsValues).isEqualTo(flagsValues);
    assertThat(lastIntentSenderRequest.extraFlags).isEqualTo(extraFlags);
    assertThat(lastIntentSenderRequest.options).isEqualTo(options);
  }

  @Test
  public void startIntentSenderForResult_throwsException() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    shadowOf(activity).setThrowIntentSenderException(true);
    IntentSender intentSender =
        PendingIntent.getActivity(systemContext, 0, new Intent("action"), 0).getIntentSender();

    try {
      activity.startIntentSenderForResult(intentSender, 0, null, 0, 0, 0);
      fail("An IntentSender.SendIntentException should have been thrown");
    } catch (IntentSender.SendIntentException e) {
      // NOP
    }
  }

  @Test
  @Config(minSdk = KITKAT)
  public void reportFullyDrawn_reported() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    activity.reportFullyDrawn();
    assertThat(shadowOf(activity).getReportFullyDrawn()).isTrue();
  }

  @Test
  @Config(minSdk = N)
  public void enterPip() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    assertThat(activity.isInPictureInPictureMode()).isFalse();
    activity.enterPictureInPictureMode();
    assertThat(activity.isInPictureInPictureMode()).isTrue();
    activity.moveTaskToBack(false);
    assertThat(activity.isInPictureInPictureMode()).isFalse();
  }

  @Test
  @Config(minSdk = O)
  public void enterPipWithParams() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    assertThat(activity.isInPictureInPictureMode()).isFalse();
    activity.enterPictureInPictureMode(new PictureInPictureParams.Builder().build());
    assertThat(activity.isInPictureInPictureMode()).isTrue();
  }

  @Test
  @Config(minSdk = N)
  public void initializeVoiceInteractor_succeeds() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    shadowOf(activity).initializeVoiceInteractor();
    assertThat(activity.getVoiceInteractor()).isNotNull();
  }

  @Test
  @Config(minSdk = O)
  public void buildActivity_noOptionsBundle_launchesOnDefaultDisplay() {
    Activity activity = Robolectric.buildActivity(Activity.class, null).setup().get();

    assertThat(activity.getWindowManager().getDefaultDisplay().getDisplayId())
        .isEqualTo(Display.DEFAULT_DISPLAY);
  }

  @Test
  @Config(minSdk = O)
  public void buildActivity_optionBundleWithNoDisplaySet_launchesOnDefaultDisplay() {
    Activity activity =
        Robolectric.buildActivity(Activity.class, null, ActivityOptions.makeBasic().toBundle())
            .setup()
            .get();

    assertThat(activity.getWindowManager().getDefaultDisplay().getDisplayId())
        .isEqualTo(Display.DEFAULT_DISPLAY);
  }

  @Test
  @Config(minSdk = O)
  public void buildActivity_optionBundleWithDefaultDisplaySet_launchesOnDefaultDisplay() {
    Activity activity =
        Robolectric.buildActivity(
                Activity.class,
                null,
                ActivityOptions.makeBasic().setLaunchDisplayId(Display.DEFAULT_DISPLAY).toBundle())
            .setup()
            .get();

    assertThat(activity.getWindowManager().getDefaultDisplay().getDisplayId())
        .isEqualTo(Display.DEFAULT_DISPLAY);
  }

  @Test
  @Config(minSdk = O)
  public void buildActivity_optionBundleWithValidNonDefaultDisplaySet_launchesOnSpecifiedDisplay() {
    int displayId = ShadowDisplayManager.addDisplay("");

    Activity activity =
        Robolectric.buildActivity(
                Activity.class,
                null,
                ActivityOptions.makeBasic().setLaunchDisplayId(displayId).toBundle())
            .setup()
            .get();

    assertThat(activity.getWindowManager().getDefaultDisplay().getDisplayId())
        .isNotEqualTo(Display.DEFAULT_DISPLAY);
    assertThat(activity.getWindowManager().getDefaultDisplay().getDisplayId()).isEqualTo(displayId);
  }

  @Test
  @Config(minSdk = O)
  public void buildActivity_optionBundleWithInvalidNonDefaultDisplaySet_launchesOnDefaultDisplay() {
    Activity activity =
        Robolectric.buildActivity(
                Activity.class,
                null,
                ActivityOptions.makeBasic().setLaunchDisplayId(123).toBundle())
            .setup()
            .get();

    assertThat(activity.getWindowManager().getDefaultDisplay().getDisplayId())
        .isEqualTo(Display.DEFAULT_DISPLAY);
  }

  /////////////////////////////

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
      // Requesting the action bar causes it to be properly initialized when the Activity becomes
      // visible
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

  private static class ActionMenuActivity extends Activity {
    SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
      setContentView(new FrameLayout(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.action_menu, menu);

      MenuItem searchMenuItem = menu.findItem(R.id.action_search);
      mSearchView = (SearchView) searchMenuItem.getActionView();
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
    private List<String> transcript;

    @Override
    public void onContentChanged() {
      transcript.add(
          "onContentChanged was called; title is \""
              + shadowOf((View) findViewById(R.id.title)).innerText()
              + "\"");
    }

    private void setTranscript(List<String> transcript) {
      this.transcript = transcript;
    }
  }

  private static class OnBackPressedActivity extends Activity {
    public boolean onBackPressedCalled = false;

    @Override
    public void onBackPressed() {
      onBackPressedCalled = true;
      super.onBackPressed();
    }
  }

  private static class ActivityLifecycleCallbacks
      implements Application.ActivityLifecycleCallbacks {
    private final List<String> transcript;

    public ActivityLifecycleCallbacks(List<String> transcript) {
      this.transcript = transcript;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
      transcript.add("onActivityCreated");
    }

    @Override
    public void onActivityStarted(Activity activity) {
      transcript.add("onActivityStarted");
    }

    @Override
    public void onActivityResumed(Activity activity) {
      transcript.add("onActivityResumed");
    }

    @Override
    public void onActivityPaused(Activity activity) {
      transcript.add("onActivityPaused");
    }

    @Override
    public void onActivityStopped(Activity activity) {
      transcript.add("onActivityStopped");
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
      transcript.add("onActivitySaveInstanceState");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
      transcript.add("onActivityDestroyed");
    }
  }

  /** Activity for testing */
  public static class TestActivityWithAnotherTheme
      extends org.robolectric.shadows.testing.TestActivity {}
}
