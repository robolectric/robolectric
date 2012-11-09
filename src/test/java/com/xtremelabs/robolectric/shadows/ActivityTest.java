package com.xtremelabs.robolectric.shadows;

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
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.xtremelabs.robolectric.ApplicationResolver;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.shadows.testing.OnMethodTestActivity;
import com.xtremelabs.robolectric.util.TestRunnable;
import com.xtremelabs.robolectric.util.Transcript;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static com.xtremelabs.robolectric.util.TestUtil.assertInstanceOf;
import static com.xtremelabs.robolectric.util.TestUtil.newConfig;
import static org.hamcrest.CoreMatchers.*;
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
    public void shouldNotRegisterNullBroadcastReceiver() {
        DialogLifeCycleActivity activity = new DialogLifeCycleActivity();
        activity.registerReceiver(null, new IntentFilter());

        activity.onDestroy();
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
        final DialogLifeCycleActivity activity = new DialogLifeCycleActivity();
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
        final DialogCreatingActivity activity = new DialogCreatingActivity();

        activity.showDialog(1);

        Dialog firstDialog = ShadowDialog.getLatestDialog();

        activity.showDialog(1);

        final Dialog secondDialog = ShadowDialog.getLatestDialog();

        assertSame("dialogs should be the same instance", firstDialog, secondDialog);
    }

    @Test
    public void showDialog_shouldShowDialog() throws Exception {
        final DialogCreatingActivity activity = new DialogCreatingActivity();
        activity.showDialog(1);
        Dialog dialog = ShadowDialog.getLatestDialog();
        assertTrue(dialog.isShowing());
    }

    @Test
    public void dismissDialog_shouldDismissPreviouslyShownDialog() throws Exception {
        final DialogCreatingActivity activity = new DialogCreatingActivity();
        activity.showDialog(1);
        activity.dismissDialog(1);
        Dialog dialog = ShadowDialog.getLatestDialog();
        assertFalse(dialog.isShowing());
    }

    @Test(expected = IllegalArgumentException.class)
    public void dismissDialog_shouldThrowExceptionIfDialogWasNotPreviouslyShown() throws Exception {
        final DialogCreatingActivity activity = new DialogCreatingActivity();
        activity.dismissDialog(1);
    }

    @Test
    public void removeDialog_shouldCreateDialogAgain() {
        final DialogCreatingActivity activity = new DialogCreatingActivity();

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
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        assertThat(activity.getRequestedOrientation(), equalTo(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
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
    	
    	for (int mode: modes) {
    		activity.setDefaultKeyMode(mode);
    		assertThat("Unexpected key mode",
    				shadow.getDefaultKeymode(),
    				equalTo(mode));
    	}
    }

    @Test
    public void shouldSetContentViewWithFrameLayoutAsParent() throws Exception {
        Activity activity = new Activity();
        activity.setContentView(R.layout.toplevel_merge);

        View contentView = shadowOf(activity).getContentView();
        assertInstanceOf(FrameLayout.class, contentView);
        assertThat(((FrameLayout) contentView).getChildCount(), equalTo(2));
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
        assertThat(activity.getPreferences(Context.MODE_PRIVATE).getString("foo", null), equalTo("bar"));
    }

    @Test
    public void shouldFindContentViewContainer() throws Exception {
        Activity activity = new Activity();
        View contentView = new View(activity);
        activity.setContentView(contentView);

        FrameLayout contentViewContainer = (FrameLayout) activity.findViewById(android.R.id.content);
        assertThat(contentViewContainer.getChildAt(0), is(contentView));
    }

    @Test
      public void createGoesThroughFullLifeCycle() throws Exception {
        TestActivity activity = new TestActivity();

        shadowOf(activity).create();

        activity.transcript.assertEventsSoFar(
                "onCreate",
                "onStart",
                "onPostCreate",
                "onResume"
        );
    }


    @Test
    public void recreateGoesThroughFullLifeCycle() throws Exception {
        TestActivity activity = new TestActivity();

        ShadowActivity shadow = shadowOf(activity);
        shadow.recreate();

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
        
        assertThat( shadow.getManagedCursors(), notNullValue() );
        assertThat( shadow.getManagedCursors().size(), equalTo(0) );  
        
        Cursor c = Robolectric.newInstanceOf(SQLiteCursor.class);
        activity.startManagingCursor(c);

        assertThat( shadow.getManagedCursors(), notNullValue() );
        assertThat( shadow.getManagedCursors().size(), equalTo(1) );
        assertThat( shadow.getManagedCursors().get(0), sameInstance(c) );

        activity.stopManagingCursor(c);
        
        assertThat( shadow.getManagedCursors(), notNullValue() );
        assertThat( shadow.getManagedCursors().size(), equalTo(0) );
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

            if( isRecreating ) {
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
        Activity activity = new Activity() {};
        shadowOf(activity).callOnStart();
    }

    private static class MyActivity extends Activity {
        @Override protected void onDestroy() {
            super.onDestroy();
        }
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
            return new Dialog(null);
        }
    }
}
