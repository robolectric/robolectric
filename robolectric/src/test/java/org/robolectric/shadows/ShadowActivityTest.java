package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static org.assertj.core.api.Assertions.assertThat;
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
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Application;
import android.app.Dialog;
import android.app.Fragment;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
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
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.TestRunnable;

@RunWith(RobolectricTestRunner.class)
public class ShadowActivityTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
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
    assertThat(activity.getTitle().toString()).isEqualTo(activity.getString(R.string.activity_name));
  }

  @Test
  public void shouldUseActivityLabelFromManifestAsTitleForActivityWithShortName() throws Exception {
    activity = Robolectric.setupActivity(LabelTestActivity3.class);
    assertThat(activity.getTitle()).isNotNull();
    assertThat(activity.getTitle().toString()).isEqualTo(activity.getString(R.string.activity_name));
  }

  public static final class LabelTestActivity1 extends Activity {}
  public static final class LabelTestActivity2 extends Activity {}
  public static final class LabelTestActivity3 extends Activity {}

  @Test
  public void shouldNotComplainIfActivityIsDestroyedWhileAnotherActivityHasRegisteredBroadcastReceivers() throws Exception {
    ActivityController<DialogCreatingActivity> controller = Robolectric.buildActivity(DialogCreatingActivity.class);
    activity = controller.get();

    DialogLifeCycleActivity activity2 = Robolectric.setupActivity(DialogLifeCycleActivity.class);
    activity2.registerReceiver(new AppWidgetProvider(), new IntentFilter());

    controller.destroy();
  }

  @Test
  public void shouldNotRegisterNullBroadcastReceiver() {
    ActivityController<DialogCreatingActivity> controller = Robolectric.buildActivity(DialogCreatingActivity.class);
    activity = controller.get();
    activity.registerReceiver(null, new IntentFilter());

    controller.destroy();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void shouldReportDestroyedStatus() {
    ActivityController<DialogCreatingActivity> controller = Robolectric.buildActivity(DialogCreatingActivity.class);
    activity = controller.get();

    controller.destroy();
    assertThat(activity.isDestroyed()).isTrue();
  }

  @Test
  public void startActivity_shouldDelegateToStartActivityForResult() {
    final List<String> transcript = new ArrayList<>();
    Activity activity = new Activity() {
      @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        transcript.add("onActivityResult called with requestCode " + requestCode + ", resultCode " + resultCode + ", intent data " + data.getData());
      }
    };
    activity.startActivity(new Intent().setType("image/*"));

    shadowOf(activity).receiveResult(new Intent().setType("image/*"), Activity.RESULT_OK,
        new Intent().setData(Uri.parse("content:foo")));
    assertThat(transcript).containsExactly("onActivityResult called with requestCode -1, resultCode -1, intent data content:foo");
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
  public void startActivityForResultAndReceiveResult_shouldSendResponsesBackToActivity() throws Exception {
    final List<String> transcript = new ArrayList<>();
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
    assertThat(transcript).containsExactly("onActivityResult called with requestCode 456, resultCode -1, intent data content:foo");
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
    activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);
    ShadowActivity shadowActivity = shadowOf(activity);
    Intent intent = new Intent().setClass(activity, DialogLifeCycleActivity.class);
    assertThat(shadowActivity.getNextStartedActivity()).isNull();

    activity.startActivityForResult(intent, 142);

    Intent startedIntent = shadowActivity.getNextStartedActivity();
    assertThat(startedIntent).isNotNull();
    assertThat(startedIntent).isSameAs(intent);
  }

  @Test
  public void shouldSupportGetStartedActivitiesForResult() throws Exception {
    activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);
    ShadowActivity shadowActivity = shadowOf(activity);
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
  public void shouldSupportPeekStartedActivitiesForResult() throws Exception {
    activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);
    ShadowActivity shadowActivity = shadowOf(activity);
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
    final List<String> transcript = new ArrayList<>();
    ActivityWithContentChangedTranscript customActivity = Robolectric.setupActivity(ActivityWithContentChangedTranscript.class);
    customActivity.setTranscript(transcript);
    customActivity.setContentView(R.layout.main);
    assertThat(transcript).containsExactly("onContentChanged was called; title is \"Main Layout\"");
  }

  @Test
  public void shouldRetrievePackageNameFromTheManifest() throws Exception {
    assertThat(Robolectric.setupActivity(Activity.class).getPackageName()).isEqualTo(RuntimeEnvironment.application.getPackageName());
  }

  @Test
  public void shouldRunUiTasksImmediatelyByDefault() throws Exception {
    TestRunnable runnable = new TestRunnable();
    activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);
    activity.runOnUiThread(runnable);
    assertTrue(runnable.wasRun);
  }

  @Test
  public void shouldQueueUiTasksWhenUiThreadIsPaused() throws Exception {
    ShadowLooper.pauseMainLooper();

    activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);
    TestRunnable runnable = new TestRunnable();
    activity.runOnUiThread(runnable);
    assertFalse(runnable.wasRun);

    ShadowLooper.unPauseMainLooper();
    assertTrue(runnable.wasRun);
  }

  @Test
  public void showDialog_shouldCreatePrepareAndShowDialog() {
    final DialogLifeCycleActivity activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);
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
    final DialogLifeCycleActivity activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);
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
    final DialogLifeCycleActivity activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);
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

  @Test(expected = IllegalArgumentException.class)
  public void dismissDialog_shouldThrowExceptionIfDialogWasNotPreviouslyShown() throws Exception {
    final DialogCreatingActivity activity = Robolectric.setupActivity(DialogCreatingActivity.class);
    activity.dismissDialog(1);
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
    ActivityWithOnCreateDialog activity = Robolectric.setupActivity(ActivityWithOnCreateDialog.class);
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
  @Config(minSdk = JELLY_BEAN)
  public void shouldCallFinishOnFinishAffinity() {
    Activity activity = new Activity();
    activity.finishAffinity();

    ShadowActivity shadowActivity = shadowOf(activity);
    assertTrue(shadowActivity.isFinishing());
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void shouldCallFinishOnFinishAndRemoveTask() {
    Activity activity = new Activity();
    activity.finishAndRemoveTask();

    ShadowActivity shadowActivity = shadowOf(activity);
    assertTrue(shadowActivity.isFinishing());
  }

  @Test
  public void shouldCallFinishOnFinish() {
    Activity activity = new Activity();
    activity.finish();

    ShadowActivity shadowActivity = shadowOf(activity);
    assertTrue(shadowActivity.isFinishing());
  }

  @Test
  public void shouldSupportCurrentFocus() {
    activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);
    ShadowActivity shadow = shadowOf(activity);

    assertNull(shadow.getCurrentFocus());
    View view = new View(activity);
    shadow.setCurrentFocus(view);
    assertEquals(view, shadow.getCurrentFocus());
  }

  @Test
  public void shouldSetOrientation() {
    activity = Robolectric.setupActivity(DialogLifeCycleActivity.class);
    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    assertThat(activity.getRequestedOrientation()).isEqualTo(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
      assertThat(shadow.getDefaultKeymode()).as("Unexpected key mode").isEqualTo(mode);
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
    TestActivity activity = buildActivity(TestActivity.class).get();
    activity.recreate();

    assertThat(activity.transcript).containsExactly(
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
  public void startAndStopManagingCursorTracksCursors() throws Exception {
    TestActivity activity = new TestActivity();

    ShadowActivity shadow = shadowOf(activity);

    assertThat(shadow.getManagedCursors()).isNotNull();
    assertThat(shadow.getManagedCursors().size()).isEqualTo(0);

    Cursor c = Shadow.newInstanceOf(SQLiteCursor.class);
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

  @Test
  public void decorViewSizeEqualToDisplaySize() {
    Activity activity = buildActivity(Activity.class).create().visible().get();
    View decorView = activity.getWindow().getDecorView();
    assertThat(decorView).isNotEqualTo(null);
    ViewRootImpl root = decorView.getViewRootImpl();
    assertThat(root).isNotEqualTo(null);
    assertThat(decorView.getWidth()).isNotEqualTo(0);
    assertThat(decorView.getHeight()).isNotEqualTo(0);
    Display display = Shadow.newInstanceOf(Display.class);
    ShadowDisplay shadowDisplay = Shadows.shadowOf(display);
    assertThat(decorView.getWidth()).isEqualTo(shadowDisplay.getWidth());
    assertThat(decorView.getHeight()).isEqualTo(shadowDisplay.getHeight());
  }

  @Test
  @Config(minSdk = M)
  public void requestsPermissions() {
    TestActivity activity = new TestActivity();
    activity.requestPermissions(new String[0], -1);
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
    Activity activity = Robolectric.setupActivity(Activity.class);
    assertTrue(activity.isTaskRoot()); // as implemented, Activities are considered task roots by default

    shadowOf(activity).setIsTaskRoot(false);
    assertFalse(activity.isTaskRoot());
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

  @Test public void shouldGetAttributeFromThemeSetOnActivity() throws Exception {
    ShadowThemeTest.TestActivity activity = setupActivity(ShadowThemeTest.TestActivityWithAnotherTheme.class);
    TypedArray a = activity.obtainStyledAttributes(R.styleable.AnotherTheme);

    assertThat(a.hasValue(R.styleable.AnotherTheme_animalStyle)).isTrue();
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
  public void canGetOptionsMenu() throws Exception {
    Activity activity = buildActivity(OptionsMenuActivity.class).create().visible().get();
    Menu optionsMenu = shadowOf(activity).getOptionsMenu();
    assertThat(optionsMenu).isNotNull();
    assertThat(optionsMenu.getItem(0).getTitle()).isEqualTo("Algebraic!");
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
    final Activity activity = buildActivity(Activity.class).create().get();

    Intent intent = new Intent(Intent.ACTION_VIEW);
    activity.startActivityFromFragment(new Fragment(), intent, 4);

    ShadowActivity.IntentForResult intentForResult = shadowOf(activity).getNextStartedActivityForResult();
    assertThat(intentForResult.intent).isSameAs(intent);
    assertThat(intentForResult.requestCode).isEqualTo(4);
  }

  @Test
  public void canStartActivityFromFragment_withBundle() {
    final Activity activity = buildActivity(Activity.class).create().get();

    Bundle options = new Bundle();
    Intent intent = new Intent(Intent.ACTION_VIEW);
    activity.startActivityFromFragment(new Fragment(), intent, 5, options);

    ShadowActivity.IntentForResult intentForResult = shadowOf(activity).getNextStartedActivityForResult();
    assertThat(intentForResult.intent).isSameAs(intent);
    assertThat(intentForResult.options).isSameAs(options);
    assertThat(intentForResult.requestCode).isEqualTo(5);
  }

  @Test
  public void shouldUseAnimationOverride() {
    Activity activity = buildActivity(Activity.class).create().get();
    Intent intent = new Intent(activity, OptionsMenuActivity.class);

    Bundle animationBundle = ActivityOptions.makeCustomAnimation(activity, R.anim.test_anim_1, R.anim.test_anim_1).toBundle();
    activity.startActivity(intent, animationBundle);
    assertThat(shadowOf(activity).getNextStartedActivityForResult().options).isSameAs(animationBundle);
  }

  @Test
  public void shouldCallActivityLifecycleCallbacks() {
    final List<String> transcript = new ArrayList<>();
    final ActivityController<Activity> controller = buildActivity(Activity.class);
    RuntimeEnvironment.application.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks(transcript));

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

  public static class ChildActivity extends Activity { }

  public static class ParentActivity extends Activity { }

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

    ShadowActivity shadowActivity = shadowOf(activity);
    shadowActivity.setCallingActivity(componentName);

    assertEquals(componentName, activity.getCallingActivity());
  }

  /////////////////////////////

  public AndroidManifest newConfigWith(String contents) throws IOException {
    return newConfigWith("org.robolectric", contents);
  }

  private AndroidManifest newConfigWith(String packageName, String contents) throws IOException {
    String fileContents = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
        "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
        "          package=\"" + packageName + "\">\n" +
        "    " + contents + "\n" +
        "</manifest>\n";
    File f = temporaryFolder.newFile("whatever.xml");
    Files.write(fileContents, f, Charsets.UTF_8);
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

  private static class ActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
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
}