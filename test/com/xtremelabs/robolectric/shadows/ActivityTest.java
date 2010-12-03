package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.Application;
import android.appwidget.AppWidgetProvider;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.Transcript;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(WithTestDefaultsRunner.class)
public class ActivityTest {
    @Before
    public void setUp() throws Exception {
        Robolectric.bindDefaultShadowClasses();

        Robolectric.application = new Application();
    }

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

    private static class MyActivity extends Activity {
        @Override protected void onDestroy() {
            super.onDestroy();
        }
    }
}
