package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.appwidget.AppWidgetProvider;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import com.xtremelabs.robolectric.ApplicationResolver;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.shadows.testing.OnMethodTestActivity;
import com.xtremelabs.robolectric.util.TestRunnable;
import com.xtremelabs.robolectric.util.Transcript;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static com.xtremelabs.robolectric.util.TestUtil.newConfig;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class ActivityTest {

    @Test(expected = IllegalStateException.class)
    public void shouldComplainIfActivityIsDestroyedWithRegisteredBroadcastReceivers() throws Exception {
        MyActivity activity = new MyActivity();
        activity.registerReceiver(new AppWidgetProvider(), new IntentFilter());
        activity.onDestroy();
    }

    @Test
    public void shouldNotComplainIfActivityIsDestroyedWhileAnotherActivityHasRegisteredBroadcastReceivers() throws Exception {
        MyActivity activity = new MyActivity();

        MyActivity activity2 = new MyActivity();
        activity2.registerReceiver(new AppWidgetProvider(), new IntentFilter());

        activity.onDestroy(); // should not throw exception
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
        MyActivity activity = new MyActivity();
        ShadowActivity shadowActivity = Robolectric.shadowOf(activity);
        Intent intent = new Intent().setClass(activity, MyActivity.class);
        assertThat(shadowActivity.getNextStartedActivity(), nullValue());

        activity.startActivityForResult(intent, 142);

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertThat(startedIntent, notNullValue());
        assertThat(startedIntent, sameInstance(intent));
    }

    @Test
    public void shouldSupportGetStartedActitivitesForResult() throws Exception {
        MyActivity activity = new MyActivity();
        ShadowActivity shadowActivity = Robolectric.shadowOf(activity);
        Intent intent = new Intent().setClass(activity, MyActivity.class);

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
        MyActivity activity = new MyActivity();
        ShadowActivity shadowActivity = Robolectric.shadowOf(activity);
        Intent intent = new Intent().setClass(activity, MyActivity.class);

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
        assertEquals("com.wacka.wa", new Activity().getPackageName());
    }

    @Test
    public void shouldRunUiTasksImmediatelyByDefault() throws Exception {
        TestRunnable runnable = new TestRunnable();
        MyActivity activity = new MyActivity();
        activity.runOnUiThread(runnable);
        assertTrue(runnable.wasRun);
    }

    @Test
    public void shouldQueueUiTasksWhenUiThreadIsPaused() throws Exception {
        Robolectric.pauseMainLooper();

        MyActivity activity = new MyActivity();
        TestRunnable runnable = new TestRunnable();
        activity.runOnUiThread(runnable);
        assertFalse(runnable.wasRun);

        Robolectric.unPauseMainLooper();
        assertTrue(runnable.wasRun);
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

    @Test
    public void getAndSetParentActivity_shouldWorkForTestingPurposes() throws Exception {
        Activity parentActivity = new Activity(){};
        Activity activity = new Activity(){};
        shadowOf(activity).setParent(parentActivity);
        assertSame(parentActivity, activity.getParent());
    }

    @Test
    public void getAndSetRequestedOrientation_shouldRemember() throws Exception {
        Activity activity = new Activity(){};
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, activity.getRequestedOrientation());
    }

    @Test
    public void getAndSetRequestedOrientation_shouldDelegateToParentIfPresent() throws Exception {
        Activity parentActivity = new Activity(){};
        Activity activity = new Activity(){};
        shadowOf(activity).setParent(parentActivity);
        parentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, activity.getRequestedOrientation());
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, parentActivity.getRequestedOrientation());
    }

    private static class MyActivity extends Activity {
        @Override protected void onDestroy() {
            super.onDestroy();
        }
    }

}
