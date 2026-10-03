package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static android.os.Looper.getMainLooper;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
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
import android.app.DirectAction;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.WindowConfiguration;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.LocusId;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Rect;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Rational;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SearchView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.LooperMode.Mode;
import org.robolectric.fakes.RoboSplashScreen;
import org.robolectric.junit.rules.SetSystemPropertyRule;
import org.robolectric.shadows.ShadowActivity.IntentForResult;
import org.robolectric.shadows.ShadowActivity.IntentSenderRequest;
import org.robolectric.util.ReflectionHelpers;

/** Test of ShadowActivity. */
@RunWith(AndroidJUnit4.class)
@SuppressWarnings("RobolectricSystemContext") // preexisting when check was enabled
public class ShadowActivityTest {
  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  private Activity activity;

  @Test
  public void shouldUseApplicationLabelFromManifestAsTitleForActivity() {
    activity = Robolectric.setupActivity(LabelTestActivity1.class);
    assertThat(activity.getTitle()).isNotNull();
    assertThat(activity.getTitle().toString()).isEqualTo(activity.getString(R.string.app_name));
  }

  @Test
  public void shouldUseActivityLabelFromManifestAsTitleForActivity() {
    activity = Robolectric.setupActivity(LabelTestActivity2.class);
    assertThat(activity.getTitle()).isNotNull();
    assertThat(activity.getTitle().toString())
        .isEqualTo(activity.getString(R.string.activity_name));
  }

  @Test
  public void shouldUseActivityLabelFromManifestAsTitleForActivityWithShortName() {
    activity = Robolectric.setupActivity(LabelTestActivity3.class);
    assertThat(activity.getTitle()).isNotNull();
    assertThat(activity.getTitle().toString())
        .isEqualTo(activity.getString(R.string.activity_name));
  }

  @Test
  public void createActivity_noDisplayFinished_shouldFinishActivity() {
    try (ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class)) {
      controller.get().setTheme(android.R.style.Theme_NoDisplay);
      controller.create();
      controller.get().finish();
      controller.start().visible().resume();

      activity = controller.get();
      assertThat(activity.isFinishing()).isTrue();
    }
  }

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

  @Test
  public void createRootActivity_moveTaskToBackNonRoot_shouldMoveTaskToBack() {
    try (ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class)) {
      activity = controller.get();
      controller.create();
      shadowOf(activity).setIsTaskRoot(true);

      boolean isTaskMovedToBack = activity.moveTaskToBack(/* nonRoot= */ true);

      assertThat(isTaskMovedToBack).isTrue();
      assertThat(shadowOf(activity).isTaskMovedToBack()).isTrue();
    }
  }

  @Test
  public void createNonRootActivity_moveTaskToBackNonRoot_shouldMoveTaskToBack() {
    try (ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class)) {
      activity = controller.get();
      controller.create();
      shadowOf(activity).setIsTaskRoot(false);

      boolean isTaskMovedToBack = activity.moveTaskToBack(/* nonRoot= */ true);

      assertThat(isTaskMovedToBack).isTrue();
      assertThat(shadowOf(activity).isTaskMovedToBack()).isTrue();
    }
  }

  @Test
  public void createNonRootActivity_moveTaskToBackRoot_shouldNotMoveTaskToBack() {
    try (ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class)) {
      activity = controller.get();
      controller.create();
      shadowOf(activity).setIsTaskRoot(false);

      boolean isTaskMovedToBack = activity.moveTaskToBack(/* nonRoot= */ false);

      assertThat(isTaskMovedToBack).isFalse();
      assertThat(shadowOf(activity).isTaskMovedToBack()).isFalse();
    }
  }

  @Test
  public void createRootActivity_moveTaskToBackRoot_shouldMoveTaskToBack() {
    try (ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class)) {
      activity = controller.get();
      controller.create();
      shadowOf(activity).setIsTaskRoot(true);

      boolean isTaskMovedToBack = activity.moveTaskToBack(/* nonRoot= */ false);

      assertThat(isTaskMovedToBack).isTrue();
      assertThat(shadowOf(activity).isTaskMovedToBack()).isTrue();
    }
  }

  @Test
  public void createNonRootActivity_moveTaskToBackNonRootThenRoot_moveTaskToBacksReturnTrue() {
    try (ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class)) {
      activity = controller.get();
      controller.create();
      shadowOf(activity).setIsTaskRoot(false);

      boolean isTaskMovedToBackNonRoot = activity.moveTaskToBack(/* nonRoot= */ true);
      boolean isTaskMovedToBackRoot = activity.moveTaskToBack(/* nonRoot= */ false);

      assertThat(isTaskMovedToBackNonRoot).isTrue();
      assertThat(isTaskMovedToBackRoot).isTrue();
    }
  }

  public static final class LabelTestActivity1 extends Activity {}

  public static final class LabelTestActivity2 extends Activity {}

  public static final class LabelTestActivity3 extends Activity {}

  @Test
  public void
      shouldNotComplainIfActivityIsDestroyedWhileAnotherActivityHasRegisteredBroadcastReceivers() {
    try (ActivityController<DialogCreatingActivity> controller =
        Robolectric.buildActivity(DialogCreatingActivity.class)) {
      activity = controller.get();

      DialogLifeCycleActivity activity2 = Robolectric.setupActivity(DialogLifeCycleActivity.class);
      activity2.registerReceiver(new AppWidgetProvider(), new IntentFilter());

      controller.destroy();
    }
  }

  @Test
  public void shouldNotRegisterNullBroadcastReceiver() {
    try (ActivityController<DialogCreatingActivity> controller =
        Robolectric.buildActivity(DialogCreatingActivity.class)) {
      activity = controller.get();
      activity.registerReceiver(null, new IntentFilter());

      controller.destroy();
    }
  }

  @Test
  public void shouldReportDestroyedStatus() {
    try (ActivityController<DialogCreatingActivity> controller =
        Robolectric.buildActivity(DialogCreatingActivity.class)) {
      activity = controller.get();

      controller.destroy();
      assertThat(activity.isDestroyed()).isTrue();
    }
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

  @Test
  public void startActivity_optionsRecordedInIntentForResult() {
    TranscriptActivity activity = Robolectric.setupActivity(TranscriptActivity.class);

    Bundle options = new Bundle();
    options.putString("key", "value");
    activity.startActivity(new Intent().setType("image/*"), options);

    IntentForResult intentForResult = shadowOf(activity).peekNextStartedActivityForResult();
    assertThat(intentForResult).isNotNull();
    assertThat(intentForResult.options).isNotNull();
    assertThat(intentForResult.options.getString("key")).isEqualTo("value");
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
  public void startActivityForResultAndReceiveResult_shouldSendResponsesBackToActivity() {
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
  public void startActivityForResultAndReceiveResult_whenNoIntentMatches_shouldThrowException() {
    Intent requestIntent = new Intent();
    try (ActivityController<ThrowOnResultActivity> controller =
        Robolectric.buildActivity(ThrowOnResultActivity.class)) {
      ThrowOnResultActivity activity = controller.get();
      activity.startActivityForResult(new Intent().setType("audio/*"), 123);
      activity.startActivityForResult(new Intent().setType("image/*"), 456);

      requestIntent.setType("video/*");
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
  public void shouldSupportStartActivityForResult() {
    activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);
    Intent intent = new Intent().setClass(activity, DialogLifeCycleActivity.class);
    assertThat(shadowOf(activity).getNextStartedActivity()).isNull();

    activity.startActivityForResult(intent, 142);

    Intent startedIntent = shadowOf(activity).getNextStartedActivity();
    assertThat(startedIntent).isNotNull();
    assertThat(startedIntent).isSameInstanceAs(intent);
  }

  @Test
  public void shouldSupportGetStartedActivitiesForResult() {
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
  public void shouldSupportPeekStartedActivitiesForResult() {
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
  public void shouldRetrievePackageNameFromTheManifest() {
    assertThat(Robolectric.setupActivity(Activity.class).getPackageName())
        .isEqualTo(ApplicationProvider.getApplicationContext().getPackageName());
  }

  @Test
  public void shouldRunUiTasksImmediatelyByDefault() {
    AtomicBoolean wasRun = new AtomicBoolean(false);
    Runnable runnable = () -> wasRun.set(true);
    activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);
    activity.runOnUiThread(runnable);
    assertThat(wasRun.get()).isTrue();
  }

  @Test
  @LooperMode(LEGACY)
  @Config(maxSdk = BAKLAVA)
  public void shouldQueueUiTasksWhenUiThreadIsPaused() {
    shadowOf(getMainLooper()).pause();

    activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);
    AtomicBoolean wasRun = new AtomicBoolean(false);
    Runnable runnable = () -> wasRun.set(true);
    activity.runOnUiThread(runnable);
    assertThat(wasRun.get()).isFalse();

    shadowOf(getMainLooper()).idle();
    assertThat(wasRun.get()).isTrue();
  }

  /**
   * The legacy behavior spec-ed in {@link #shouldQueueUiTasksWhenUiThreadIsPaused()} is actually
   * incorrect. The {@link Activity#runOnUiThread} will execute posted tasks inline.
   */
  @Test
  @LooperMode(Mode.PAUSED)
  public void shouldExecutePostedUiTasksInRealisticLooper() {
    activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);
    AtomicBoolean wasRun = new AtomicBoolean(false);
    Runnable runnable = () -> wasRun.set(true);
    activity.runOnUiThread(runnable);
    assertThat(wasRun.get()).isTrue();
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
  public void showDialog_shouldShowDialog() {
    final DialogCreatingActivity activity = Robolectric.setupActivity(DialogCreatingActivity.class);
    activity.showDialog(1);
    Dialog dialog = ShadowDialog.getLatestDialog();
    assertTrue(dialog.isShowing());
  }

  @Test
  public void dismissDialog_shouldDismissPreviouslyShownDialog() {
    final DialogCreatingActivity activity = Robolectric.setupActivity(DialogCreatingActivity.class);
    activity.showDialog(1);
    activity.dismissDialog(1);
    Dialog dialog = ShadowDialog.getLatestDialog();
    assertFalse(dialog.isShowing());
  }

  @Test
  public void dismissDialog_shouldThrowExceptionIfDialogWasNotPreviouslyShown() {
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
  public void shouldCallFinishOnFinishAffinity() {
    Activity activity = new Activity();
    activity.finishAffinity();

    assertTrue(activity.isFinishing());
  }

  @Test
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

  @Test
  @Config(minSdk = O_MR1)
  public void setShowWhenLocked_shouldSetShowWhenLocked() {
    try (ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class)) {
      activity = controller.create().get();
      ShadowActivity shadowActivity = shadowOf(activity);
      assertThat(shadowActivity.getShowWhenLocked()).isFalse();
      activity.setShowWhenLocked(true);
      assertThat(shadowActivity.getShowWhenLocked()).isTrue();
      activity.setShowWhenLocked(false);
      assertThat(shadowActivity.getShowWhenLocked()).isFalse();
    }
  }

  @Test
  @Config(minSdk = O_MR1)
  public void setTurnScreenOn_shouldSetTurnScreenOn() {
    try (ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class)) {
      activity = controller.create().get();
      ShadowActivity shadowActivity = shadowOf(activity);
      assertThat(shadowActivity.getTurnScreenOn()).isFalse();
      activity.setTurnScreenOn(true);
      assertThat(shadowActivity.getTurnScreenOn()).isTrue();
      activity.setTurnScreenOn(false);
      assertThat(shadowActivity.getTurnScreenOn()).isFalse();
    }
  }

  public static final class ShowWhenLockedActivity extends Activity {}

  public static final class DoNotShowWhenLockedActivity extends Activity {}

  @Test
  @Config(minSdk = O_MR1)
  public void createActivity_showWhenLockedEnabled_returnsTrueForShowWhenLocked() {
    try (ActivityController<ShowWhenLockedActivity> controller =
        Robolectric.buildActivity(ShowWhenLockedActivity.class)) {
      assertThat(shadowOf(controller.get()).getShowWhenLocked()).isTrue();
    }
  }

  @Test
  @Config(minSdk = O_MR1)
  public void createActivity_showWhenLockedDisabled_returnsFalseForShowWhenLocked() {
    try (ActivityController<DoNotShowWhenLockedActivity> controller =
        Robolectric.buildActivity(DoNotShowWhenLockedActivity.class)) {
      assertThat(shadowOf(controller.get()).getShowWhenLocked()).isFalse();
    }
  }

  @Test // unclear what the correct behavior should be here...
  public void shouldPopulateWindowDecorViewWithMergeLayoutContents() {
    try (ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class)) {
      Activity activity = controller.create().get();
      activity.setContentView(R.layout.toplevel_merge);

      View contentView = activity.findViewById(android.R.id.content);
      assertThat(((ViewGroup) contentView).getChildCount()).isEqualTo(2);
    }
  }

  @Test
  public void setContentView_shouldReplaceOldContentView() {
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
  public void onKeyUp_callsOnBackPressedWhichFinishesTheActivity() {
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
  public void shouldGiveSharedPreferences() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
    assertNotNull(preferences);
    preferences.edit().putString("foo", "bar").commit();
    assertThat(activity.getPreferences(Context.MODE_PRIVATE).getString("foo", null))
        .isEqualTo("bar");
  }

  @Test
  public void shouldFindContentViewContainerWithChild() {
    Activity activity = buildActivity(Activity.class).create().get();
    View contentView = new View(activity);
    activity.setContentView(contentView);

    FrameLayout contentViewContainer = (FrameLayout) activity.findViewById(android.R.id.content);
    assertThat(contentViewContainer.getChildAt(0)).isSameInstanceAs(contentView);
  }

  @Test
  public void shouldFindContentViewContainerWithoutChild() {
    Activity activity = buildActivity(Activity.class).create().get();

    FrameLayout contentViewContainer = (FrameLayout) activity.findViewById(android.R.id.content);
    assertThat(contentViewContainer.getId()).isEqualTo(android.R.id.content);
  }

  @Test
  public void recreateGoesThroughFullLifeCycle() {
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
  public void recreateBringsBackTheOriginalLifeCycleStateAfterRecreate_resumed() {
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
  public void recreateBringsBackTheOriginalLifeCycleStateAfterRecreate_paused() {
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
  public void recreateBringsBackTheOriginalLifeCycleStateAfterRecreate_stopped() {
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
  public void startAndStopManagingCursorTracksCursors() {
    TestActivity activity = new TestActivity();

    assertThat(shadowOf(activity).getManagedCursors()).isNotNull();
    assertThat(shadowOf(activity).getManagedCursors()).isEmpty();

    Cursor c = new MatrixCursor(new String[] {"a"});
    activity.startManagingCursor(c);

    assertThat(shadowOf(activity).getManagedCursors()).isNotNull();
    assertThat(shadowOf(activity).getManagedCursors()).hasSize(1);
    assertThat(shadowOf(activity).getManagedCursors().get(0)).isSameInstanceAs(c);

    activity.stopManagingCursor(c);

    assertThat(shadowOf(activity).getManagedCursors()).isNotNull();
    assertThat(shadowOf(activity).getManagedCursors()).isEmpty();
    c.close();
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
  @Config(minSdk = Config.OLDEST_SDK)
  public void requestsPermissions() {
    TestActivity activity = Robolectric.setupActivity(TestActivity.class);
    activity.requestPermissions(new String[] {Manifest.permission.CAMERA}, 1007);
  }

  private static class TestActivity extends Activity {
    List<String> transcript = new ArrayList<>();

    private boolean isRecreating = false;
    private boolean returnMalformedDirectAction = false;

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

    void setReturnMalformedDirectAction(boolean returnMalformedDirectAction) {
      this.returnMalformedDirectAction = returnMalformedDirectAction;
    }

    DirectAction getDirectActionForTesting() {
      Bundle extras = new Bundle();
      extras.putParcelable("componentName", this.getComponentName());
      return new DirectAction.Builder("testDirectAction")
          .setExtras(extras)
          .setLocusId(new LocusId("unused"))
          .build();
    }

    DirectAction getMalformedDirectAction() {
      return new DirectAction.Builder("malformedDirectAction").build();
    }

    @Override
    public void onGetDirectActions(
        @Nonnull CancellationSignal cancellationSignal,
        @Nonnull Consumer<List<DirectAction>> callback) {
      if (returnMalformedDirectAction) {
        callback.accept(Collections.singletonList(getMalformedDirectAction()));
      } else {
        callback.accept(Collections.singletonList(getDirectActionForTesting()));
      }
    }
  }

  @Test
  public void getAndSetParentActivity_shouldWorkForTestingPurposes() {
    Activity parentActivity = new Activity();
    Activity activity = new Activity();
    shadowOf(activity).setParent(parentActivity);
    assertSame(parentActivity, activity.getParent());
  }

  @Test
  public void getAndSetRequestedOrientation_shouldRemember() {
    Activity activity = new Activity();
    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, activity.getRequestedOrientation());
  }

  @Test
  public void getAndSetRequestedOrientation_shouldDelegateToParentIfPresent() {
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
  public void shouldSupportIsTaskRoot() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    assertTrue(
        activity.isTaskRoot()); // as implemented, Activities are considered task roots by default

    shadowOf(activity).setIsTaskRoot(false);
    assertFalse(activity.isTaskRoot());
  }

  @Test
  @Config(minSdk = N)
  public void shouldSupportIsInMultiWindowMode() {
    Activity activity = Robolectric.setupActivity(Activity.class);

    assertThat(activity.isInMultiWindowMode())
        .isFalse(); // Activity is not in multi window mode by default.
    shadowOf(activity).setInMultiWindowMode(true);

    assertThat(activity.isInMultiWindowMode()).isTrue();
  }

  @Test
  public void getPendingTransitionEnterAnimationResourceId_should() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    activity.overridePendingTransition(15, 2);
    assertThat(shadowOf(activity).getPendingTransitionEnterAnimationResourceId()).isEqualTo(15);
  }

  @Test
  public void getPendingTransitionExitAnimationResourceId_should() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    activity.overridePendingTransition(15, 2);
    assertThat(shadowOf(activity).getPendingTransitionExitAnimationResourceId()).isEqualTo(2);
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void getOverriddenActivityTransitionOpen_withoutBackgroundColor() {
    try (ActivityController<Activity> controller = buildActivity(Activity.class).setup()) {
      Activity activity = controller.get();
      activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, 15, 2);
      ShadowActivity.OverriddenActivityTransition overriddenActivityTransition =
          shadowOf(activity).getOverriddenActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN);

      assertThat(overriddenActivityTransition).isNotNull();
      assertThat(overriddenActivityTransition.enterAnim).isEqualTo(15);
      assertThat(overriddenActivityTransition.exitAnim).isEqualTo(2);
      assertThat(overriddenActivityTransition.backgroundColor).isEqualTo(Color.TRANSPARENT);
    }
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void getOverriddenActivityTransitionClose_withoutBackgroundColor() {
    try (ActivityController<Activity> controller = buildActivity(Activity.class).setup()) {
      Activity activity = controller.get();
      ShadowActivity shadowActivity = shadowOf(activity);
      activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 15, 2);
      ShadowActivity.OverriddenActivityTransition overriddenActivityTransition =
          shadowActivity.getOverriddenActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE);

      assertThat(overriddenActivityTransition).isNotNull();
      assertThat(overriddenActivityTransition.enterAnim).isEqualTo(15);
      assertThat(overriddenActivityTransition.exitAnim).isEqualTo(2);
      assertThat(overriddenActivityTransition.backgroundColor).isEqualTo(Color.TRANSPARENT);
    }
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void getOverriddenActivityTransitionOpen_withBackgroundColor() {
    try (ActivityController<Activity> controller = buildActivity(Activity.class).setup()) {
      Activity activity = controller.get();
      activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, 33, 12, Color.RED);
      ShadowActivity.OverriddenActivityTransition overriddenActivityTransition =
          shadowOf(activity).getOverriddenActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN);

      assertThat(overriddenActivityTransition).isNotNull();
      assertThat(overriddenActivityTransition.enterAnim).isEqualTo(33);
      assertThat(overriddenActivityTransition.exitAnim).isEqualTo(12);
      assertThat(overriddenActivityTransition.backgroundColor).isEqualTo(Color.RED);
    }
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void getOverriddenActivityTransitionClose_withBackgroundColor() {
    try (ActivityController<Activity> controller = buildActivity(Activity.class).setup()) {
      Activity activity = controller.get();
      ShadowActivity shadowActivity = shadowOf(activity);
      activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 33, 12, Color.RED);
      ShadowActivity.OverriddenActivityTransition overriddenActivityTransition =
          shadowActivity.getOverriddenActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE);

      assertThat(overriddenActivityTransition).isNotNull();
      assertThat(overriddenActivityTransition.enterAnim).isEqualTo(33);
      assertThat(overriddenActivityTransition.exitAnim).isEqualTo(12);
      assertThat(overriddenActivityTransition.backgroundColor).isEqualTo(Color.RED);
    }
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void getOverriddenActivityTransition_invalidType() {
    try (ActivityController<Activity> controller = buildActivity(Activity.class).setup()) {
      Activity activity = controller.get();
      activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 33, 12, Color.RED);
      ShadowActivity.OverriddenActivityTransition overriddenActivityTransition =
          shadowOf(activity).getOverriddenActivityTransition(-1);
      assertThat(overriddenActivityTransition).isNull();
    }
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void getOverriddenActivityTransition_beforeOverridingOrAfterClearing() {
    try (ActivityController<Activity> controller = buildActivity(Activity.class).setup()) {
      Activity activity = controller.get();
      ShadowActivity shadowActivity = shadowOf(activity);

      assertThat(shadowActivity.getOverriddenActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN))
          .isNull();

      assertThat(shadowActivity.getOverriddenActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE))
          .isNull();

      activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, 12, 33);
      activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 33, 12);
      activity.clearOverrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN);
      activity.clearOverrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE);

      assertThat(shadowActivity.getOverriddenActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN))
          .isNull();

      assertThat(shadowActivity.getOverriddenActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE))
          .isNull();
    }
  }

  @Test
  public void getActionBar_shouldWorkIfActivityHasAnAppropriateTheme() {
    try (ActivityController<ActionBarThemedActivity> controller =
        Robolectric.buildActivity(ActionBarThemedActivity.class)) {
      ActionBarThemedActivity myActivity = controller.create().get();
      ActionBar actionBar = myActivity.getActionBar();
      assertThat(actionBar).isNotNull();
    }
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
  public void canGetOptionsMenu() {
    Activity activity = buildActivity(OptionsMenuActivity.class).create().visible().get();
    Menu optionsMenu = shadowOf(activity).getOptionsMenu();
    assertThat(optionsMenu).isNotNull();
    assertThat(optionsMenu.getItem(0).getTitle().toString()).isEqualTo("Algebraic!");
  }

  @Test
  public void canGetOptionsMenuWithActionMenu() {
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
  public void lockTask() {
    Activity activity = Robolectric.setupActivity(Activity.class);

    assertThat(shadowOf(activity).isLockTask()).isFalse();

    activity.startLockTask();
    assertThat(shadowOf(activity).isLockTask()).isTrue();

    activity.stopLockTask();
    assertThat(shadowOf(activity).isLockTask()).isFalse();
  }

  @Test
  @Config(minSdk = Config.OLDEST_SDK)
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

    Intent intent = shadowOf(activity).getNextStartedActivity();
    assertThat(intent).isNotNull();
    assertThat(intent.getAction()).isEqualTo("android.content.pm.action.REQUEST_PERMISSIONS");
    assertThat(intent.getStringArrayExtra("android.content.pm.extra.REQUEST_PERMISSIONS_NAMES"))
        .isEqualTo(permission);
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
  public void getLastIntentSenderRequest_sendWithRequestCode()
      throws IntentSender.SendIntentException {
    TranscriptActivity activity = Robolectric.setupActivity(TranscriptActivity.class);
    int requestCode = 108;
    Intent intent = new Intent("action");
    Intent fillInIntent = new Intent();
    PendingIntent pendingIntent =
        PendingIntent.getActivity(getApplication(), requestCode, intent, 0);

    IntentSender intentSender = pendingIntent.getIntentSender();
    activity.startIntentSenderForResult(intentSender, requestCode, fillInIntent, 0, 0, 0, null);

    shadowOf(activity).receiveResult(intent, Activity.RESULT_OK, intent);
    assertThat(activity.transcript)
        .containsExactly(
            "onActivityResult called with requestCode 108, resultCode -1, intent data null");
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
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class, null)) {
      Activity activity = controller.setup().get();

      assertThat(activity.getWindowManager().getDefaultDisplay().getDisplayId())
          .isEqualTo(Display.DEFAULT_DISPLAY);
    }
  }

  @Test
  @Config(minSdk = O)
  public void buildActivity_optionBundleWithNoDisplaySet_launchesOnDefaultDisplay() {
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class, null, ActivityOptions.makeBasic().toBundle())) {
      Activity activity = controller.setup().get();

      assertThat(activity.getWindowManager().getDefaultDisplay().getDisplayId())
          .isEqualTo(Display.DEFAULT_DISPLAY);
    }
  }

  @Test
  @Config(minSdk = O)
  public void buildActivity_optionBundleWithDefaultDisplaySet_launchesOnDefaultDisplay() {
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(
            Activity.class,
            null,
            ActivityOptions.makeBasic().setLaunchDisplayId(Display.DEFAULT_DISPLAY).toBundle())) {
      Activity activity = controller.setup().get();
      assertThat(activity.getWindowManager().getDefaultDisplay().getDisplayId())
          .isEqualTo(Display.DEFAULT_DISPLAY);
    }
  }

  @Test
  @Config(minSdk = O)
  public void buildActivity_optionBundleWithValidNonDefaultDisplaySet_launchesOnSpecifiedDisplay() {
    int displayId = ShadowDisplayManager.addDisplay("");
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(
            Activity.class,
            null,
            ActivityOptions.makeBasic().setLaunchDisplayId(displayId).toBundle())) {
      Activity activity = controller.setup().get();
      assertThat(activity.getWindowManager().getDefaultDisplay().getDisplayId())
          .isNotEqualTo(Display.DEFAULT_DISPLAY);
      assertThat(activity.getWindowManager().getDefaultDisplay().getDisplayId())
          .isEqualTo(displayId);
    }
  }

  @Test
  @Config(minSdk = O)
  public void buildActivity_onNonDefaultDisplay_hasThatDisplaysConfiguration() {
    int displayId = ShadowDisplayManager.addDisplay("w960dp-h540dp-land-xhdpi");

    try (ActivityController<Activity> controller = buildActivityOnDisplay(displayId)) {
      Configuration configuration = controller.setup().get().getResources().getConfiguration();

      assertThat(configuration.screenWidthDp).isEqualTo(960);
      assertThat(configuration.screenHeightDp).isEqualTo(540);
      assertThat(configuration.smallestScreenWidthDp).isEqualTo(540);
      assertThat(configuration.orientation).isEqualTo(Configuration.ORIENTATION_LANDSCAPE);
      assertThat(configuration.densityDpi).isEqualTo(DisplayMetrics.DENSITY_XHIGH);
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.R)
  public void buildActivity_onNonDefaultDisplay_hasThatDisplaysWindowBounds() {
    int displayId = ShadowDisplayManager.addDisplay("w960dp-h540dp-land-xhdpi");

    try (ActivityController<Activity> controller = buildActivityOnDisplay(displayId)) {
      WindowManager windowManager = controller.setup().get().getWindowManager();

      assertThat(windowManager.getCurrentWindowMetrics().getBounds())
          .isEqualTo(new Rect(0, 0, 1920, 1080));
      assertThat(windowManager.getMaximumWindowMetrics().getBounds())
          .isEqualTo(new Rect(0, 0, 1920, 1080));
    }
  }

  @Test
  @Config(minSdk = O)
  public void configurationChange_onNonDefaultDisplay_keepsThatDisplaysSize() {
    int displayId = ShadowDisplayManager.addDisplay("w960dp-h540dp-land-xhdpi");

    try (ActivityController<Activity> controller = buildActivityOnDisplay(displayId).setup()) {
      RuntimeEnvironment.setQualifiers("+night");
      controller.configurationChange();

      Activity activity = controller.get();
      Configuration configuration = activity.getResources().getConfiguration();
      assertThat(activity.getWindowManager().getDefaultDisplay().getDisplayId())
          .isEqualTo(displayId);
      assertThat(configuration.screenWidthDp).isEqualTo(960);
      assertThat(configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK)
          .isEqualTo(Configuration.UI_MODE_NIGHT_YES);
    }
  }

  @Test
  @Config(minSdk = O)
  public void recreate_onNonDefaultDisplay_staysOnThatDisplay() {
    int displayId = ShadowDisplayManager.addDisplay("w960dp-h540dp-land-xhdpi");

    try (ActivityController<Activity> controller = buildActivityOnDisplay(displayId).setup()) {
      controller.recreate();

      Activity activity = controller.get();
      assertThat(activity.getWindowManager().getDefaultDisplay().getDisplayId())
          .isEqualTo(displayId);
      assertThat(activity.getResources().getConfiguration().screenWidthDp).isEqualTo(960);
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void buildActivity_withLaunchBounds_isInAWindowWithThoseBounds() {
    try (ActivityController<Activity> controller =
        buildActivityWithLaunchBounds(new Rect(100, 50, 600, 750)).setup()) {
      Activity activity = controller.get();

      Configuration configuration = activity.getResources().getConfiguration();
      assertThat(configuration.screenWidthDp).isEqualTo(500);
      assertThat(configuration.screenHeightDp).isEqualTo(700);
      assertThat(configuration.orientation).isEqualTo(Configuration.ORIENTATION_PORTRAIT);
      assertThat(activity.isInMultiWindowMode()).isTrue();
      assertThat(activity.getWindow().getDecorView().getWidth()).isEqualTo(500);
      assertThat(
              RuntimeEnvironment.getApplication().getResources().getConfiguration().screenWidthDp)
          .isEqualTo(1280);
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.R, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void buildActivity_withLaunchBounds_hasTheWindowsMetrics() {
    try (ActivityController<Activity> controller =
        buildActivityWithLaunchBounds(new Rect(100, 50, 600, 750)).setup()) {
      WindowManager windowManager = controller.get().getWindowManager();

      assertThat(windowManager.getCurrentWindowMetrics().getBounds())
          .isEqualTo(new Rect(100, 50, 600, 750));
      assertThat(windowManager.getMaximumWindowMetrics().getBounds())
          .isEqualTo(new Rect(0, 0, 1280, 800));
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void setWindowBounds_whenTheActivityHandlesTheChange_resizesItsWindow() {
    handleWindowChanges(WindowAwareActivity.class);
    ActivityController<WindowAwareActivity> windowController =
        buildActivityInWindow(WindowAwareActivity.class, new Rect(0, 0, 640, 800)).setup();
    WindowAwareActivity activity = windowController.get();

    shadowOf(windowController.get()).setWindowBounds(new Rect(0, 0, 400, 800));
    shadowOf(getMainLooper()).idle();

    assertThat(windowController.get()).isSameInstanceAs(activity);
    assertThat(activity.events).containsExactly("onConfigurationChanged w400dp");
    assertThat(activity.getResources().getDisplayMetrics().widthPixels).isEqualTo(400);
    assertThat(activity.getWindow().getDecorView().getWidth()).isEqualTo(400);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void setWindowBounds_whenTheActivityDoesNotHandleTheChange_recreatesItInTheNewWindow() {
    ActivityController<Activity> windowController =
        buildActivityInWindow(Activity.class, new Rect(0, 0, 640, 800)).setup();
    Activity activity = windowController.get();

    shadowOf(windowController.get()).setWindowBounds(new Rect(0, 0, 400, 800));

    Activity recreatedActivity = windowController.get();
    assertThat(recreatedActivity).isNotSameInstanceAs(activity);
    assertThat(recreatedActivity.getResources().getConfiguration().screenWidthDp).isEqualTo(400);
    assertThat(recreatedActivity.isInMultiWindowMode()).isTrue();
    assertThat(recreatedActivity.getWindow().getDecorView().getWidth()).isEqualTo(400);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void setWindowBounds_inAndOutOfAWindow_reportsTheMultiWindowModeChanges() {
    handleWindowChanges(WindowAwareActivity.class);
    ActivityController<WindowAwareActivity> windowController =
        buildActivityInWindow(WindowAwareActivity.class, new Rect(0, 0, 640, 800)).setup();
    WindowAwareActivity activity = windowController.get();

    shadowOf(windowController.get()).setWindowBounds(null);
    shadowOf(getMainLooper()).idle();

    assertThat(activity.isInMultiWindowMode()).isFalse();
    assertThat(activity.getResources().getDisplayMetrics().widthPixels).isEqualTo(1280);
    assertThat(activity.getWindow().getDecorView().getWidth()).isEqualTo(1280);

    shadowOf(windowController.get()).setWindowBounds(new Rect(0, 0, 400, 800));

    assertThat(activity.isInMultiWindowMode()).isTrue();
    assertThat(activity.events)
        .containsExactly(
            "onMultiWindowModeChanged false",
            "onConfigurationChanged w1280dp",
            "onMultiWindowModeChanged true",
            "onConfigurationChanged w400dp")
        .inOrder();
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void setWindowBounds_whenTheActivitySharesTheApplicationContext_recreatesItInTheWindow() {
    ActivityController<Activity> sharedController =
        Robolectric.buildActivity(Activity.class).setup();
    Activity activity = sharedController.get();

    shadowOf(sharedController.get()).setWindowBounds(new Rect(0, 0, 400, 800));

    Activity recreatedActivity = sharedController.get();
    assertThat(recreatedActivity).isNotSameInstanceAs(activity);
    assertThat(recreatedActivity.getResources().getConfiguration().screenWidthDp).isEqualTo(400);
    assertThat(recreatedActivity.isInMultiWindowMode()).isTrue();
    assertThat(
            ApplicationProvider.getApplicationContext()
                .getResources()
                .getConfiguration()
                .screenWidthDp)
        .isEqualTo(1280);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void
      setWindowBounds_whenTheActivitySharesTheApplicationContext_recreatesItEvenIfItHandlesIt() {
    handleWindowChanges(WindowAwareActivity.class);
    ActivityController<WindowAwareActivity> sharedController =
        Robolectric.buildActivity(WindowAwareActivity.class).setup();
    WindowAwareActivity activity = sharedController.get();

    shadowOf(activity).setWindowBounds(new Rect(0, 0, 400, 800));

    assertThat(sharedController.get()).isNotSameInstanceAs(activity);
    assertThat(sharedController.get().getResources().getConfiguration().screenWidthDp)
        .isEqualTo(400);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void enterSplitScreen_putsTheActivityInTheLeftHalfOfALandscapeDisplay() {
    ActivityController<Activity> splitController =
        Robolectric.buildActivity(Activity.class).setup();

    shadowOf(splitController.get()).enterSplitScreen();

    Activity activity = splitController.get();
    Configuration configuration = activity.getResources().getConfiguration();
    assertThat(configuration.windowConfiguration.getWindowingMode())
        .isEqualTo(splitScreenWindowingMode("WINDOWING_MODE_SPLIT_SCREEN_PRIMARY"));
    assertThat(configuration.windowConfiguration.getBounds()).isEqualTo(new Rect(0, 0, 635, 800));
    assertThat(configuration.screenWidthDp).isEqualTo(635);
    assertThat(activity.isInMultiWindowMode()).isTrue();
    assertThat(activity.getWindow().getDecorView().getWidth()).isEqualTo(635);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w800dp-h1280dp-port-mdpi")
  public void enterSplitScreen_nextToAnotherActivity_putsItInTheOtherHalf() {
    ActivityController<Activity> topController = Robolectric.buildActivity(Activity.class).setup();
    shadowOf(topController.get()).enterSplitScreen();
    ActivityController<Activity> splitController =
        Robolectric.buildActivity(Activity.class).setup();

    shadowOf(splitController.get()).enterSplitScreen();

    Configuration configuration = splitController.get().getResources().getConfiguration();
    assertThat(configuration.windowConfiguration.getWindowingMode())
        .isEqualTo(splitScreenWindowingMode("WINDOWING_MODE_SPLIT_SCREEN_SECONDARY"));
    assertThat(configuration.windowConfiguration.getBounds())
        .isEqualTo(new Rect(0, 645, 800, 1280));
    assertThat(configuration.screenHeightDp).isEqualTo(635);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void enterSplitScreen_thenRotating_keepsItsHalfOfTheDisplay() {
    ActivityController<Activity> splitController =
        Robolectric.buildActivity(Activity.class).setup();
    shadowOf(splitController.get()).enterSplitScreen();

    RuntimeEnvironment.setQualifiers("+port");
    splitController.configurationChange();

    Configuration configuration = splitController.get().getResources().getConfiguration();
    assertThat(configuration.windowConfiguration.getBounds()).isEqualTo(new Rect(0, 0, 800, 635));
    assertThat(splitController.get().isInMultiWindowMode()).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void setWindowBounds_null_leavesSplitScreen() {
    ActivityController<Activity> splitController =
        Robolectric.buildActivity(Activity.class).setup();
    shadowOf(splitController.get()).enterSplitScreen();

    shadowOf(splitController.get()).setWindowBounds(null);

    Activity activity = splitController.get();
    assertThat(activity.getResources().getConfiguration().screenWidthDp).isEqualTo(1280);
    assertThat(activity.isInMultiWindowMode()).isFalse();
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void startActivity_launchAdjacentFromSplitScreen_launchesInTheOtherHalf() {
    ActivityController<Activity> launcher = Robolectric.buildActivity(Activity.class).setup();
    shadowOf(launcher.get()).enterSplitScreen();

    launcher.get().startActivity(adjacentIntent());
    ActivityController<AdjacentActivity> adjacent =
        Robolectric.buildActivity(
                AdjacentActivity.class, shadowOf(launcher.get()).getNextStartedActivity())
            .setup();

    assertThat(windowBounds(adjacent.get())).isEqualTo(new Rect(645, 0, 1280, 800));
    assertThat(adjacent.get().isInMultiWindowMode()).isTrue();
    assertThat(windowBounds(launcher.get())).isEqualTo(new Rect(0, 0, 635, 800));
  }

  @Test
  @Config(minSdk = VERSION_CODES.S_V2, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void startActivity_launchAdjacentFromAFullscreenActivity_splitsTheScreen() {
    ActivityController<Activity> launcher = Robolectric.buildActivity(Activity.class).setup();

    launcher.get().startActivity(adjacentIntent());
    shadowOf(getMainLooper()).idle();
    ActivityController<AdjacentActivity> adjacent =
        Robolectric.buildActivity(
                AdjacentActivity.class, shadowOf(launcher.get()).getNextStartedActivity())
            .setup();

    assertThat(windowBounds(launcher.get())).isEqualTo(new Rect(0, 0, 635, 800));
    assertThat(launcher.get().isInMultiWindowMode()).isTrue();
    assertThat(windowBounds(adjacent.get())).isEqualTo(new Rect(645, 0, 1280, 800));
  }

  @Test
  @Config(
      minSdk = VERSION_CODES.P,
      maxSdk = VERSION_CODES.S,
      qualifiers = "w1280dp-h800dp-land-mdpi")
  public void startActivity_launchAdjacentFromAFullscreenActivityBeforeS_V2_launchesFullscreen() {
    ActivityController<Activity> launcher = Robolectric.buildActivity(Activity.class).setup();

    launcher.get().startActivity(adjacentIntent());
    shadowOf(getMainLooper()).idle();
    ActivityController<AdjacentActivity> adjacent =
        Robolectric.buildActivity(
                AdjacentActivity.class, shadowOf(launcher.get()).getNextStartedActivity())
            .setup();

    assertThat(launcher.get().isInMultiWindowMode()).isFalse();
    assertThat(adjacent.get().isInMultiWindowMode()).isFalse();
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void setSplitScreenDividerPosition_resizesBothHalves() {
    ActivityController<Activity> left = Robolectric.buildActivity(Activity.class).setup();
    shadowOf(left.get()).enterSplitScreen();
    ActivityController<Activity> right = Robolectric.buildActivity(Activity.class).setup();
    shadowOf(right.get()).enterSplitScreen();

    ShadowDisplayManager.setSplitScreenDividerPosition(Display.DEFAULT_DISPLAY, 0.3f);

    assertThat(windowBounds(left.get())).isEqualTo(new Rect(0, 0, 379, 800));
    assertThat(windowBounds(right.get())).isEqualTo(new Rect(389, 0, 1280, 800));
    assertThat(right.get().getResources().getConfiguration().screenWidthDp).isEqualTo(891);
    assertThat(right.get().getWindow().getDecorView().getWidth()).isEqualTo(891);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void setSplitScreenDividerPosition_withAnActivityHandlingIt_reportsItsNewSize() {
    handleWindowChanges(WindowAwareActivity.class);
    ActivityController<WindowAwareActivity> windowController =
        buildActivityInWindow(WindowAwareActivity.class, new Rect(0, 0, 640, 800)).setup();
    shadowOf(windowController.get()).enterSplitScreen();
    WindowAwareActivity activity = windowController.get();

    ShadowDisplayManager.setSplitScreenDividerPosition(Display.DEFAULT_DISPLAY, 0.7f);

    assertThat(windowController.get()).isSameInstanceAs(activity);
    assertThat(activity.events)
        .containsExactly("onConfigurationChanged w635dp", "onConfigurationChanged w891dp")
        .inOrder();
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void enterSplitScreen_afterTheDividerMoved_usesItsPosition() {
    ShadowDisplayManager.setSplitScreenDividerPosition(Display.DEFAULT_DISPLAY, 0.7f);
    ActivityController<Activity> splitController =
        Robolectric.buildActivity(Activity.class).setup();

    shadowOf(splitController.get()).enterSplitScreen();

    assertThat(windowBounds(splitController.get())).isEqualTo(new Rect(0, 0, 891, 800));
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void touch_inSplitScreen_goesToTheActivityItIsIn() {
    ActivityController<FocusAwareActivity> left = buildFocusAwareActivityInSplitScreen();
    ActivityController<FocusAwareActivity> right = buildFocusAwareActivityInSplitScreen();

    touch(100, 400);
    touch(745, 400);

    assertThat(left.get().touches).containsExactly("100,400");
    assertThat(right.get().touches).containsExactly("100,400");
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void touch_inSplitScreen_movesTheFocusToTheTouchedActivity() {
    ActivityController<FocusAwareActivity> left = buildFocusAwareActivityInSplitScreen();
    ActivityController<FocusAwareActivity> right = startFocusAwareActivityAdjacentTo(left.get());
    right.windowFocusChanged(true);
    left.get().events.clear();
    right.get().events.clear();

    touch(100, 400);

    assertThat(right.get().events)
        .containsExactly("onTopResumedActivityChanged false", "onWindowFocusChanged false")
        .inOrder();
    assertThat(left.get().events)
        .containsExactly(
            "onTopResumedActivityChanged true", "onWindowFocusChanged true", "onTouchEvent")
        .inOrder();
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void touch_inTheFocusedActivity_keepsTheFocus() {
    ActivityController<FocusAwareActivity> left = buildFocusAwareActivityInSplitScreen();
    ActivityController<FocusAwareActivity> right = startFocusAwareActivityAdjacentTo(left.get());
    right.windowFocusChanged(true);
    left.get().events.clear();
    right.get().events.clear();

    touch(745, 400);

    assertThat(left.get().events).isEmpty();
    assertThat(right.get().events).containsExactly("onTouchEvent");
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void startActivity_launchAdjacent_takesTheFocusFromTheLaunchingActivity() {
    ActivityController<FocusAwareActivity> launcher = buildFocusAwareActivityInSplitScreen();
    launcher.windowFocusChanged(true);
    launcher.get().events.clear();

    ActivityController<FocusAwareActivity> adjacent =
        startFocusAwareActivityAdjacentTo(launcher.get());

    assertThat(launcher.get().events)
        .containsExactly("onTopResumedActivityChanged false", "onWindowFocusChanged false")
        .inOrder();
    assertThat(adjacent.get().events).containsExactly("onTopResumedActivityChanged true");
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void launchBounds_takeTheFocusFromOtherActivities() {
    FocusAwareActivity fullscreenActivity =
        Robolectric.buildActivity(FocusAwareActivity.class).setup().get();
    fullscreenActivity.events.clear();

    buildActivityInWindow(FocusAwareActivity.class, new Rect(100, 100, 500, 400)).setup();

    assertThat(fullscreenActivity.events).containsExactly("onTopResumedActivityChanged false");
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void setSplitScreenDividerPosition_keepsTheTopResumedActivity() {
    ActivityController<FocusAwareActivity> left = buildFocusAwareActivityInSplitScreen();
    ActivityController<FocusAwareActivity> right = startFocusAwareActivityAdjacentTo(left.get());
    FocusAwareActivity leftActivity = left.get();

    ShadowDisplayManager.setSplitScreenDividerPosition(Display.DEFAULT_DISPLAY, 0.3f);

    assertThat(left.get()).isNotSameInstanceAs(leftActivity);
    assertThat(left.get().events).isEmpty();
    assertThat(right.get().events).containsExactly("onTopResumedActivityChanged true");
  }

  @Test
  public void setSplitScreenDividerPosition_outsideTheDisplay_throws() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ShadowDisplayManager.setSplitScreenDividerPosition(Display.DEFAULT_DISPLAY, 0f));
    assertThrows(
        IllegalArgumentException.class,
        () -> ShadowDisplayManager.setSplitScreenDividerPosition(Display.DEFAULT_DISPLAY, 1f));
  }

  @Test
  @Config(qualifiers = "w411dp-h891dp-port")
  public void setRequestedOrientation_rotatesTheDisplay() {
    ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class).setup();

    controller.get().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    shadowOf(getMainLooper()).idle();

    Activity activity = controller.get();
    Configuration configuration = activity.getResources().getConfiguration();
    assertThat(configuration.orientation).isEqualTo(Configuration.ORIENTATION_LANDSCAPE);
    assertThat(configuration.screenWidthDp).isEqualTo(891);
    assertThat(activity.getRequestedOrientation())
        .isEqualTo(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
  }

  @Test
  @Config(qualifiers = "w411dp-h891dp-port")
  public void buildActivity_declaringAnOrientation_launchesInIt() {
    declareOrientation(OrientationActivity.class, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    OrientationActivity.creations = 0;

    try (ActivityController<OrientationActivity> controller =
        Robolectric.buildActivity(OrientationActivity.class).setup()) {
      OrientationActivity activity = controller.get();

      assertThat(activity.getResources().getConfiguration().orientation)
          .isEqualTo(Configuration.ORIENTATION_LANDSCAPE);
      assertThat(activity.getRequestedOrientation())
          .isEqualTo(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      assertThat(OrientationActivity.creations).isEqualTo(1);
    }
  }

  @Test
  @Config(minSdk = BAKLAVA, qualifiers = "w1280dp-h800dp-land")
  public void setRequestedOrientation_onALargeScreen_isIgnoredForAnAppTargetingAndroid16() {
    getApplication().getApplicationInfo().targetSdkVersion = BAKLAVA;
    ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class).setup();

    controller.get().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    shadowOf(getMainLooper()).idle();

    Configuration configuration = controller.get().getResources().getConfiguration();
    assertThat(configuration.orientation).isEqualTo(Configuration.ORIENTATION_LANDSCAPE);
    assertThat(configuration.screenWidthDp).isEqualTo(1280);
  }

  @Test
  @Config(minSdk = S, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void setRequestedOrientation_whenTheDisplayIgnoresIt_letterboxesTheActivity()
      throws Exception {
    executeShellCommand("wm set-ignore-orientation-request true");
    ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class).setup();

    controller.get().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    shadowOf(getMainLooper()).idle();

    Activity activity = controller.get();
    Configuration configuration = activity.getResources().getConfiguration();
    assertThat(configuration.windowConfiguration.getBounds()).isEqualTo(new Rect(390, 0, 890, 800));
    assertThat(configuration.orientation).isEqualTo(Configuration.ORIENTATION_PORTRAIT);
    assertThat(configuration.screenWidthDp).isEqualTo(500);
    assertThat(activity.isInMultiWindowMode()).isFalse();
    assertThat(activity.getWindow().getDecorView().getWidth()).isEqualTo(500);
    assertThat(activity.getWindowManager().getMaximumWindowMetrics().getBounds())
        .isEqualTo(new Rect(390, 0, 890, 800));
    assertThat(getApplication().getResources().getConfiguration().screenWidthDp).isEqualTo(1280);
    assertThat(executeShellCommand("wm get-ignore-orientation-request"))
        .isEqualTo("ignoreOrientationRequest true for displayId=0\n");
  }

  @Test
  @Config(minSdk = S, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void setIgnoreOrientationRequest_false_rotatesTheDisplayForALetterboxedActivity()
      throws Exception {
    executeShellCommand("wm set-ignore-orientation-request true");
    ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class).setup();
    controller.get().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    shadowOf(getMainLooper()).idle();

    executeShellCommand("wm set-ignore-orientation-request false");

    Configuration configuration = controller.get().getResources().getConfiguration();
    assertThat(configuration.orientation).isEqualTo(Configuration.ORIENTATION_PORTRAIT);
    assertThat(configuration.screenWidthDp).isEqualTo(800);
    assertThat(controller.get().getWindow().getDecorView().getWidth()).isEqualTo(800);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w411dp-h891dp-port-mdpi")
  public void setRequestedOrientation_inAFreeformWindow_isIgnored() {
    ActivityController<Activity> controller =
        buildActivityInWindow(Activity.class, new Rect(0, 0, 300, 600)).setup();

    controller.get().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    shadowOf(getMainLooper()).idle();

    assertThat(getApplication().getResources().getConfiguration().orientation)
        .isEqualTo(Configuration.ORIENTATION_PORTRAIT);
    assertThat(controller.get().getResources().getConfiguration().screenWidthDp).isEqualTo(300);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void enterPictureInPictureMode_putsTheActivityInAPinnedWindow() {
    ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class).setup();

    controller.get().enterPictureInPictureMode();
    shadowOf(getMainLooper()).idle();

    Activity activity = controller.get();
    Configuration configuration = activity.getResources().getConfiguration();
    assertThat(configuration.windowConfiguration.getWindowingMode())
        .isEqualTo(WindowConfiguration.WINDOWING_MODE_PINNED);
    assertThat(configuration.windowConfiguration.getBounds())
        .isEqualTo(new Rect(937, 600, 1264, 784));
    assertThat(activity.isInPictureInPictureMode()).isTrue();
    assertThat(activity.isInMultiWindowMode()).isTrue();
    assertThat(activity.isResumed()).isFalse();
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void enterPictureInPictureMode_withAnAspectRatio_sizesTheWindowForIt() {
    ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class).setup();

    controller
        .get()
        .enterPictureInPictureMode(
            new PictureInPictureParams.Builder().setAspectRatio(new Rational(1, 1)).build());
    shadowOf(getMainLooper()).idle();

    Rect bounds =
        controller.get().getResources().getConfiguration().windowConfiguration.getBounds();
    assertThat(bounds.width()).isEqualTo(184);
    assertThat(bounds.height()).isEqualTo(184);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void enterPictureInPictureMode_whenTheActivityHandlesIt_reportsEnteringAndLeaving() {
    handleWindowChanges(PictureInPictureActivity.class);
    ActivityController<PictureInPictureActivity> controller =
        buildActivityInWindow(PictureInPictureActivity.class, new Rect(0, 0, 640, 800)).setup();
    PictureInPictureActivity activity = controller.get();
    shadowOf(activity).setWindowBounds(null);
    activity.events.clear();

    activity.enterPictureInPictureMode();
    shadowOf(getMainLooper()).idle();

    assertThat(controller.get()).isSameInstanceAs(activity);
    assertThat(activity.events)
        .containsExactly(
            "onPictureInPictureModeChanged true",
            "onMultiWindowModeChanged true",
            "onConfigurationChanged w327dp",
            "onPause")
        .inOrder();

    activity.events.clear();
    shadowOf(activity).setWindowBounds(null);

    assertThat(activity.isInPictureInPictureMode()).isFalse();
    assertThat(activity.events)
        .containsExactly(
            "onPictureInPictureModeChanged false",
            "onMultiWindowModeChanged false",
            "onConfigurationChanged w1280dp",
            "onResume")
        .inOrder();
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w800dp-h1280dp-port-mdpi")
  public void buildActivity_withLaunchBounds_placesItsWindowOnTheScreen() {
    ActivityController<Activity> controller =
        buildActivityInWindow(Activity.class, new Rect(100, 150, 600, 700)).setup();
    shadowOf(getMainLooper()).idle();

    int[] location = new int[2];
    controller.get().getWindow().getDecorView().getLocationOnScreen(location);
    assertThat(location).isEqualTo(new int[] {100, 150});
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU, qualifiers = "w800dp-h1280dp-port-mdpi")
  public void buildActivity_withLaunchBounds_hasTheCaptionBarOfAFreeformWindow() {
    ActivityController<Activity> controller =
        buildActivityInWindow(Activity.class, new Rect(100, 150, 600, 700)).setup();
    shadowOf(getMainLooper()).idle();

    assertThat(captionBarInsets(controller.get())).isEqualTo(Insets.of(0, 42, 0, 0));

    shadowOf(controller.get()).setWindowBounds(null);
    shadowOf(getMainLooper()).idle();

    assertThat(captionBarInsets(controller.get())).isEqualTo(Insets.NONE);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU, qualifiers = "w800dp-h1280dp-port-mdpi")
  public void enterSplitScreen_hasNoCaptionBar() {
    ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class).setup();

    shadowOf(controller.get()).enterSplitScreen();
    shadowOf(getMainLooper()).idle();

    assertThat(captionBarInsets(controller.get())).isEqualTo(Insets.NONE);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w411dp-h891dp-port-mdpi")
  public void buildActivity_withLaunchBounds_whenNotResizeable_fillsTheDisplay() {
    declareNotResizeable(NotResizeableActivity.class);

    try (ActivityController<NotResizeableActivity> controller =
        buildActivityInWindow(NotResizeableActivity.class, new Rect(0, 0, 300, 600)).setup()) {
      assertThat(controller.get().isInMultiWindowMode()).isFalse();
      assertThat(controller.get().getResources().getConfiguration().screenWidthDp).isEqualTo(411);
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w411dp-h891dp-port-mdpi")
  public void enterSplitScreen_whenNotResizeable_throws() {
    declareNotResizeable(NotResizeableActivity.class);
    ActivityController<NotResizeableActivity> controller =
        Robolectric.buildActivity(NotResizeableActivity.class).setup();

    assertThrows(IllegalStateException.class, () -> shadowOf(controller.get()).enterSplitScreen());
    assertThrows(
        IllegalStateException.class,
        () -> shadowOf(controller.get()).setWindowBounds(new Rect(0, 0, 300, 600)));
  }

  @Test
  @Config(minSdk = S, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void enterSplitScreen_whenNotResizeable_isAllowedOnALargeScreen() {
    declareNotResizeable(NotResizeableActivity.class);
    ActivityController<NotResizeableActivity> controller =
        Robolectric.buildActivity(NotResizeableActivity.class).setup();

    shadowOf(controller.get()).enterSplitScreen();

    assertThat(controller.get().isInMultiWindowMode()).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void buildActivity_withLaunchBounds_isAtLeastTheDefaultMinimalSize() {
    try (ActivityController<Activity> controller =
        buildActivityInWindow(Activity.class, new Rect(100, 100, 150, 150)).setup()) {
      assertThat(windowBounds(controller.get())).isEqualTo(new Rect(100, 100, 320, 320));
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, qualifiers = "w1280dp-h800dp-land-mdpi")
  public void buildActivity_withLaunchBounds_isAtLeastTheMinimalSizeTheActivityDeclares() {
    declareMinimalSize(MinimalSizeActivity.class, 400, 300);

    try (ActivityController<MinimalSizeActivity> controller =
        buildActivityInWindow(MinimalSizeActivity.class, new Rect(100, 100, 150, 150)).setup()) {
      assertThat(windowBounds(controller.get())).isEqualTo(new Rect(100, 100, 500, 400));
    }
  }

  @Test
  @Config(
      minSdk = S,
      maxSdk = VERSION_CODES.VANILLA_ICE_CREAM,
      qualifiers = "w411dp-h891dp-port-mdpi")
  public void enterSplitScreen_onASmallScreen_whenTheMinimalSizeDoesNotFit_throws() {
    declareMinimalSize(MinimalSizeActivity.class, 300, 500);
    ActivityController<MinimalSizeActivity> controller =
        Robolectric.buildActivity(MinimalSizeActivity.class).setup();

    assertThrows(IllegalStateException.class, () -> shadowOf(controller.get()).enterSplitScreen());
  }

  @Test
  @Config(minSdk = O)
  public void buildActivity_optionBundleWithInvalidNonDefaultDisplaySet_launchesOnDefaultDisplay() {
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(
            Activity.class, null, ActivityOptions.makeBasic().setLaunchDisplayId(123).toBundle())) {
      Activity activity = controller.setup().get();
      assertThat(activity.getWindowManager().getDefaultDisplay().getDisplayId())
          .isEqualTo(Display.DEFAULT_DISPLAY);
    }
  }

  @Test
  public void buildActivity_abstractActivityClass_throwsRuntimeException() {
    Throwable throwable =
        assertThrows(
            RuntimeException.class,
            () -> {
              Robolectric.buildActivity(AbstractTestActivity.class, null);
            });
    assertThat(throwable.getMessage())
        .isEqualTo("buildActivity must be called with non-abstract class");
  }

  @Test
  @Config(minSdk = Q)
  public void callOnGetDirectActions_succeeds() {
    try (ActivityController<TestActivity> controller =
        Robolectric.buildActivity(TestActivity.class)) {
      TestActivity testActivity = controller.setup().get();
      Consumer<List<DirectAction>> testConsumer =
          (directActions) -> {
            assertThat(directActions).hasSize(1);
            DirectAction action = directActions.get(0);
            assertThat(action.getId()).isEqualTo(testActivity.getDirectActionForTesting().getId());
            ComponentName componentName = action.getExtras().getParcelable("componentName");
            assertThat(componentName.compareTo(testActivity.getComponentName())).isEqualTo(0);
          };
      shadowOf(testActivity).callOnGetDirectActions(new CancellationSignal(), testConsumer);
    }
  }

  @Test
  @Config(minSdk = Q)
  public void callOnGetDirectActions_malformedDirectAction_fails() {
    try (ActivityController<TestActivity> controller =
        Robolectric.buildActivity(TestActivity.class)) {
      TestActivity testActivity = controller.setup().get();
      // malformed DirectAction has missing LocusId
      testActivity.setReturnMalformedDirectAction(true);
      assertThrows(
          NullPointerException.class,
          () ->
              shadowOf(testActivity)
                  .callOnGetDirectActions(new CancellationSignal(), (unused) -> {}));
    }
  }

  @Test
  @Config(minSdk = S)
  public void splashScreen_setThemeId_succeeds() {
    int splashScreenThemeId = 173;
    try (ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class)) {
      Activity activity = controller.setup().get();

      activity.getSplashScreen().setSplashScreenTheme(splashScreenThemeId);

      RoboSplashScreen roboSplashScreen = (RoboSplashScreen) activity.getSplashScreen();
      assertThat(roboSplashScreen.getSplashScreenTheme()).isEqualTo(splashScreenThemeId);
    }
  }

  @Test
  @Config(minSdk = S)
  public void splashScreen_instanceOfRoboSplashScreen_succeeds() {
    try (ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class)) {
      Activity activity = controller.setup().get();
      assertThat(activity.getSplashScreen()).isInstanceOf(RoboSplashScreen.class);
    }
  }

  @Test
  public void applicationWindow_hasCorrectWindowTokens() {
    try (ActivityController<TestActivity> controller =
        Robolectric.buildActivity(TestActivity.class)) {
      Activity activity = controller.setup().get();
      View activityView = activity.getWindow().getDecorView();
      WindowManager.LayoutParams activityLp =
          (WindowManager.LayoutParams) activityView.getLayoutParams();

      View windowView = new View(activity);
      WindowManager.LayoutParams windowViewLp = new WindowManager.LayoutParams();
      windowViewLp.type = WindowManager.LayoutParams.TYPE_APPLICATION;
      ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE))
          .addView(windowView, windowViewLp);
      ShadowLooper.idleMainLooper();

      assertThat(activityLp.token).isNotNull();
      assertThat(windowViewLp.token).isEqualTo(activityLp.token);
    }
  }

  @Test
  public void subWindow_hasCorrectWindowTokens() {
    try (ActivityController<TestActivity> controller =
        Robolectric.buildActivity(TestActivity.class)) {
      Activity activity = controller.setup().get();
      View activityView = activity.getWindow().getDecorView();
      WindowManager.LayoutParams activityLp =
          (WindowManager.LayoutParams) activityView.getLayoutParams();

      View windowView = new View(activity);
      WindowManager.LayoutParams windowViewLp = new WindowManager.LayoutParams();
      windowViewLp.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
      ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE))
          .addView(windowView, windowViewLp);
      ShadowLooper.idleMainLooper();

      assertThat(activityLp.token).isNotNull();
      assertThat(windowViewLp.token).isEqualTo(activityView.getWindowToken());
      assertThat(windowView.getApplicationWindowToken()).isEqualTo(activityView.getWindowToken());
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.R)
  public void getDisplay_succeeds() {
    setSystemPropertyRule.set("robolectric.createActivityContexts", "true");

    Activity activity = Robolectric.setupActivity(Activity.class);
    assertThat(activity.getDisplay()).isNotNull();
  }

  @Test
  @Config(minSdk = VERSION_CODES.R)
  public void setLocusContext_updatesLastLocusContextFields() {
    try (ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class)) {
      Activity activity = controller.setup().get();
      ShadowActivity shadowActivity = shadowOf(activity);

      assertThat(shadowActivity.getLastLocusContextId()).isNull();
      assertThat(shadowActivity.getLastLocusContextExtras()).isNull();

      activity.setLocusContext(new LocusId("test_locus_id"), /* bundle= */ null);
      assertThat(shadowActivity.getLastLocusContextId()).isEqualTo(new LocusId("test_locus_id"));
      assertThat(shadowActivity.getLastLocusContextExtras()).isNull();

      Bundle extras = new Bundle();
      extras.putString("test", "hello");
      activity.setLocusContext(new LocusId("test_locus_id_2"), extras);
      assertThat(shadowActivity.getLastLocusContextId()).isEqualTo(new LocusId("test_locus_id_2"));
      assertThat(shadowActivity.getLastLocusContextExtras()).isEqualTo(extras);

      activity.setLocusContext(/* locusId= */ null, /* bundle= */ null);
      assertThat(shadowActivity.getLastLocusContextId()).isNull();
      assertThat(shadowActivity.getLastLocusContextExtras()).isNull();
    }
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
    public void onActivityCreated(@Nonnull Activity activity, Bundle bundle) {
      transcript.add("onActivityCreated");
    }

    @Override
    public void onActivityStarted(@Nonnull Activity activity) {
      transcript.add("onActivityStarted");
    }

    @Override
    public void onActivityResumed(@Nonnull Activity activity) {
      transcript.add("onActivityResumed");
    }

    @Override
    public void onActivityPaused(@Nonnull Activity activity) {
      transcript.add("onActivityPaused");
    }

    @Override
    public void onActivityStopped(@Nonnull Activity activity) {
      transcript.add("onActivityStopped");
    }

    @Override
    public void onActivitySaveInstanceState(@Nonnull Activity activity, @Nonnull Bundle bundle) {
      transcript.add("onActivitySaveInstanceState");
    }

    @Override
    public void onActivityDestroyed(@Nonnull Activity activity) {
      transcript.add("onActivityDestroyed");
    }
  }

  /** Test Activity for abstract checking scenario. */
  abstract static class AbstractTestActivity extends Activity {}

  /** Activity for testing */
  public static class TestActivityWithAnotherTheme
      extends org.robolectric.shadows.testing.TestActivity {}

  private static <T extends Activity> ActivityController<T> buildActivityInWindow(
      Class<T> activityClass, Rect bounds) {
    return Robolectric.buildActivity(
        activityClass, null, ActivityOptions.makeBasic().setLaunchBounds(bounds).toBundle());
  }

  private static Rect windowBounds(Activity activity) {
    return activity.getResources().getConfiguration().windowConfiguration.getBounds();
  }

  private static Intent adjacentIntent() {
    return new Intent(ApplicationProvider.getApplicationContext(), AdjacentActivity.class)
        .addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_NEW_TASK);
  }

  /** Returns the windowing mode of the system's split screen, which moved to Shell in S_V2. */
  private static int splitScreenWindowingMode(String legacyWindowingMode) {
    return RuntimeEnvironment.getApiLevel() >= VERSION_CODES.S_V2
        ? WindowConfiguration.WINDOWING_MODE_MULTI_WINDOW
        : ReflectionHelpers.getStaticField(WindowConfiguration.class, legacyWindowingMode);
  }

  private static void handleWindowChanges(Class<? extends Activity> activityClass) {
    Context context = ApplicationProvider.getApplicationContext();
    ActivityInfo activityInfo = new ActivityInfo();
    activityInfo.name = activityClass.getName();
    activityInfo.packageName = context.getPackageName();
    activityInfo.configChanges =
        ActivityInfo.CONFIG_SCREEN_SIZE
            | ActivityInfo.CONFIG_SMALLEST_SCREEN_SIZE
            | ActivityInfo.CONFIG_SCREEN_LAYOUT
            | ActivityInfo.CONFIG_ORIENTATION;
    shadowOf(context.getPackageManager()).addOrUpdateActivity(activityInfo);
  }

  private static ActivityController<FocusAwareActivity> buildFocusAwareActivityInSplitScreen() {
    ActivityController<FocusAwareActivity> controller =
        Robolectric.buildActivity(FocusAwareActivity.class).setup();
    shadowOf(controller.get()).enterSplitScreen();
    controller.get().events.clear();
    return controller;
  }

  private static ActivityController<FocusAwareActivity> startFocusAwareActivityAdjacentTo(
      Activity activity) {
    activity.startActivity(adjacentIntent().setClass(activity, FocusAwareActivity.class));
    return Robolectric.buildActivity(
            FocusAwareActivity.class, shadowOf(activity).getNextStartedActivity())
        .setup();
  }

  /** Touches the screen at the given position, then lifts the pointer. */
  private static void touch(int x, int y) {
    long time = SystemClock.uptimeMillis();
    for (int action : new int[] {MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP}) {
      MotionEvent event = MotionEvent.obtain(time, time, action, x, y, /* metaState= */ 0);
      ShadowUiAutomation.injectInputEvent(event);
      event.recycle();
    }
  }

  /** Records the touches it gets and the changes to its focus it is told about. */
  public static class FocusAwareActivity extends Activity {
    final List<String> events = new ArrayList<>();
    final List<String> touches = new ArrayList<>();

    @Override
    public void onTopResumedActivityChanged(boolean isTopResumedActivity) {
      super.onTopResumedActivityChanged(isTopResumedActivity);
      events.add("onTopResumedActivityChanged " + isTopResumedActivity);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
      super.onWindowFocusChanged(hasFocus);
      events.add("onWindowFocusChanged " + hasFocus);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
      if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
        events.add("onTouchEvent");
        touches.add((int) event.getX() + "," + (int) event.getY());
      }
      return true;
    }
  }

  /** An activity started adjacent to another. */
  public static class AdjacentActivity extends Activity {}

  /** Records the changes to its window it is told about. */
  public static class WindowAwareActivity extends Activity {
    final List<String> events = new ArrayList<>();

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode, Configuration newConfig) {
      super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig);
      events.add("onMultiWindowModeChanged " + isInMultiWindowMode);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      events.add("onConfigurationChanged w" + newConfig.screenWidthDp + "dp");
    }
  }

  private static Insets captionBarInsets(Activity activity) {
    return activity
        .getWindow()
        .getDecorView()
        .getRootWindowInsets()
        .getInsets(WindowInsets.Type.captionBar());
  }

  private static void declareNotResizeable(Class<? extends Activity> activityClass) {
    ActivityInfo activityInfo = new ActivityInfo();
    activityInfo.name = activityClass.getName();
    activityInfo.packageName = getApplication().getPackageName();
    activityInfo.resizeMode = ActivityInfo.RESIZE_MODE_UNRESIZEABLE;
    shadowOf(getApplication().getPackageManager()).addOrUpdateActivity(activityInfo);
  }

  private static void declareMinimalSize(
      Class<? extends Activity> activityClass, int minWidth, int minHeight) {
    ActivityInfo activityInfo = new ActivityInfo();
    activityInfo.name = activityClass.getName();
    activityInfo.packageName = getApplication().getPackageName();
    activityInfo.windowLayout = new ActivityInfo.WindowLayout(0, 0, 0, 0, 0, minWidth, minHeight);
    shadowOf(getApplication().getPackageManager()).addOrUpdateActivity(activityInfo);
  }

  private static void declareOrientation(
      Class<? extends Activity> activityClass, int screenOrientation) {
    ActivityInfo activityInfo = new ActivityInfo();
    activityInfo.name = activityClass.getName();
    activityInfo.packageName = getApplication().getPackageName();
    activityInfo.screenOrientation = screenOrientation;
    shadowOf(getApplication().getPackageManager()).addOrUpdateActivity(activityInfo);
  }

  private static String executeShellCommand(String command) throws IOException {
    ParcelFileDescriptor output =
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(command);
    try (InputStream inputStream = new ParcelFileDescriptor.AutoCloseInputStream(output)) {
      return new String(ByteStreams.toByteArray(inputStream), UTF_8);
    }
  }

  /** Records entering and leaving picture-in-picture mode too. */
  public static class PictureInPictureActivity extends WindowAwareActivity {
    @Override
    public void onPictureInPictureModeChanged(
        boolean isInPictureInPictureMode, Configuration newConfig) {
      super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
      events.add("onPictureInPictureModeChanged " + isInPictureInPictureMode);
    }

    @Override
    protected void onPause() {
      super.onPause();
      events.add("onPause");
    }

    @Override
    protected void onResume() {
      super.onResume();
      events.add("onResume");
    }
  }

  /** An activity that isn't resizeable. */
  public static class NotResizeableActivity extends Activity {}

  /** An activity that declares its minimal size. */
  public static class MinimalSizeActivity extends Activity {}

  /** Counts how often it is created. */
  public static class OrientationActivity extends Activity {
    static int creations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      creations++;
    }
  }

  private static ActivityController<Activity> buildActivityWithLaunchBounds(Rect bounds) {
    return Robolectric.buildActivity(
        Activity.class, null, ActivityOptions.makeBasic().setLaunchBounds(bounds).toBundle());
  }

  private static ActivityController<Activity> buildActivityOnDisplay(int displayId) {
    return Robolectric.buildActivity(
        Activity.class, null, ActivityOptions.makeBasic().setLaunchDisplayId(displayId).toBundle());
  }
}
