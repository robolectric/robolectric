package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.Dialog;
import android.appwidget.AppWidgetProvider;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.xtremelabs.robolectric.ApplicationResolver;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.TestRunnable;
import com.xtremelabs.robolectric.util.Transcript;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static com.xtremelabs.robolectric.util.TestUtil.newConfig;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class ActivityTest {

    @Test(expected = IllegalStateException.class)
    public void shouldComplainIfActivityIsDestroyedWithRegisteredBroadcastReceivers() throws Exception {
        DialogLifeCycleActivity activity = new DialogLifeCycleActivity();
        activity.registerReceiver(new AppWidgetProvider(), new IntentFilter());
        activity.onDestroy();
    }

    @Test
    public void shouldNotComplainIfActivityIsDestroyedWhileAnotherActivityHasRegisteredBroadcastReceivers() throws Exception {
        DialogLifeCycleActivity activity = new DialogLifeCycleActivity();

        DialogLifeCycleActivity activity2 = new DialogLifeCycleActivity();
        activity2.registerReceiver(new AppWidgetProvider(), new IntentFilter());

        activity.onDestroy(); // should not throw exception
    }

    @Test
    public void startActivityForResultAndReceiveResult_shouldSendResponsesBackToActivity() throws Exception {
        final Transcript transcript = new Transcript();
        Activity activity = new Activity() {
            @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
            @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
            assertThat(e.getMessage(), startsWith("No intent matches " + requestIntent));
        }
    }

    @Test
    public void shouldSupportStartActivityForResult() throws Exception {
        DialogLifeCycleActivity activity = new DialogLifeCycleActivity();
        ShadowActivity shadowActivity = Robolectric.shadowOf(activity);
        Intent intent = new Intent().setClass(activity, DialogLifeCycleActivity.class);
        assertThat(shadowActivity.getNextStartedActivity(), nullValue());

        activity.startActivityForResult(intent, 142);

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent, notNullValue());
        assertThat(startedIntent, sameInstance(intent));
    }

    @Test
    public void shouldSupportGetStartedActitivitesForResult() throws Exception {
        DialogLifeCycleActivity activity = new DialogLifeCycleActivity();
        ShadowActivity shadowActivity = Robolectric.shadowOf(activity);
        Intent intent = new Intent().setClass(activity, DialogLifeCycleActivity.class);

        activity.startActivityForResult(intent, 142);

        ShadowActivity.IntentForResult intentForResult = shadowActivity.getNextStartedActivityForResult();
        assertThat(intentForResult, notNullValue());
        assertThat(shadowActivity.getNextStartedActivityForResult(), nullValue());
        assertThat(intentForResult.intent, notNullValue());
        assertThat(intentForResult.intent, sameInstance(intent));
        assertThat(intentForResult.requestCode, equalTo(142));
    }

    @Test
    public void shouldSupportPeekStartedActitivitesForResult() throws Exception {
        DialogLifeCycleActivity activity = new DialogLifeCycleActivity();
        ShadowActivity shadowActivity = Robolectric.shadowOf(activity);
        Intent intent = new Intent().setClass(activity, DialogLifeCycleActivity.class);

        activity.startActivityForResult(intent, 142);

        ShadowActivity.IntentForResult intentForResult = shadowActivity.peekNextStartedActivityForResult();
        assertThat(intentForResult, notNullValue());
        assertThat(shadowActivity.peekNextStartedActivityForResult(), sameInstance(intentForResult));
        assertThat(intentForResult.intent, notNullValue());
        assertThat(intentForResult.intent, sameInstance(intent));
        assertThat(intentForResult.requestCode, equalTo(142));
    }

    @Test
    public void onContentChangedShouldBeCalledAfterContentViewIsSet() throws RuntimeException {
        final Transcript transcript = new Transcript();
        Activity customActivity = new Activity() {
            @Override
            public void onContentChanged() {
                transcript.add("onContentChanged was called; title is \"" + shadowOf(findViewById(R.id.title)).innerText() + "\"");
            }
        };
        customActivity.setContentView(R.layout.main);
        transcript.assertEventsSoFar("onContentChanged was called; title is \"Main Layout\"");
    }

    @Test
    public void shouldRetrievePackageNameFromTheManifest() throws Exception {
        Robolectric.application = new ApplicationResolver(newConfig("TestAndroidManifestWithPackageName.xml")).resolveApplication();
        assertThat("com.wacka.wa", equalTo(new Activity().getPackageName()));
    }

    @Test
    public void shouldRunUiTasksImmediatelyByDefault() throws Exception {
        TestRunnable runnable = new TestRunnable();
        DialogLifeCycleActivity activity = new DialogLifeCycleActivity();
        activity.runOnUiThread(runnable);
        assertTrue(runnable.wasRun);
    }

    @Test
    public void shouldQueueUiTasksWhenUiThreadIsPaused() throws Exception {
        Robolectric.pauseMainLooper();

        DialogLifeCycleActivity activity = new DialogLifeCycleActivity();
        TestRunnable runnable = new TestRunnable();
        activity.runOnUiThread(runnable);
        assertFalse(runnable.wasRun);

        Robolectric.unPauseMainLooper();
        assertTrue(runnable.wasRun);
    }

    @Test
    public void showDialog_shouldCreatePrepareAndShowDialog() {
        final DialogLifeCycleActivity activity = new DialogLifeCycleActivity();
        final AtomicBoolean dialogWasShown = new AtomicBoolean(false);

        new Dialog(activity) {
            {  activity.dialog = this; }

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
        final DialogLifeCycleActivity activity = new DialogLifeCycleActivity();
        final AtomicBoolean dialogWasShown = new AtomicBoolean(false);

        new Dialog(activity) {
            {  activity.dialog = this; }

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
        final DialogCreatingActivity activity = new DialogCreatingActivity();

        activity.showDialog(1);

        Dialog firstDialog = ShadowDialog.getLatestDialog();

        activity.showDialog(1);

        final Dialog secondDialog = ShadowDialog.getLatestDialog();

        assertSame("dialogs should be the same instance", firstDialog, secondDialog);
    }


    @Test
    public void shouldCallOnCreateDialogFromShowDialog() {
        ActivityWithOnCreateDialog activity = new ActivityWithOnCreateDialog();
        activity.showDialog(123);
        assertTrue(activity.onCreateDialogWasCalled);
        assertThat(ShadowDialog.getLatestDialog(), CoreMatchers.<Object>notNullValue());
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
        DialogLifeCycleActivity activity = new DialogLifeCycleActivity();
        ShadowActivity shadow = shadowOf(activity);

        assertNull(shadow.getCurrentFocus());
        View view = new View(activity);
        shadow.setCurrentFocus(view);
        assertEquals(view, shadow.getCurrentFocus());
    }

        @Test
    public void shouldSetOrientation() {
        DialogLifeCycleActivity activity = new DialogLifeCycleActivity();
        activity.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
        assertThat( activity.getRequestedOrientation(), equalTo( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ) );
    }
    
    @Test
    public void retrieveIdOfResource() {
        Activity activity = new Activity();

        int id1 = R.string.hello;
        String string = activity.getString(id1);
        assertEquals("Hello", string);

        int id = activity.getResources().getIdentifier("hello", "string", "com.xtremelabs.robolectric");
        assertTrue(id > 0);
        
        String hello = activity.getResources().getString(id);
        assertEquals("Hello", hello);
    }

    @Test
    public void retrieveIdOfNonExistingResource() {
        Activity activity = new Activity();

        int id = activity.getResources().getIdentifier("just_alot_of_crap", "string", "com.xtremelabs.robolectric");
        assertTrue(id == 0);
    }

    private static class DialogCreatingActivity extends Activity {
        @Override
        protected Dialog onCreateDialog(int id) {
            return new Dialog(this);
        }
    }

    private static class DialogLifeCycleActivity extends Activity {
        public boolean createdDialog = false;
        public boolean preparedDialog = false;
        public boolean preparedDialogWithBundle = false;
        public Dialog dialog = null;

        @Override protected void onDestroy() {
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
            return new Dialog(null);
        }
    }
}
