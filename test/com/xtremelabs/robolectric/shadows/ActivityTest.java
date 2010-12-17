package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.appwidget.AppWidgetProvider;
import android.content.IntentFilter;
import com.xtremelabs.robolectric.ApplicationResolver;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.TestRunnable;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

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
    public void shouldRetrievePackageNameFromTheManifest() throws Exception {
        Robolectric.application = new ApplicationResolver("test" + File.separator + "TestAndroidManifestWithPackageName.xml").resolveApplication();
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

    private static class MyActivity extends Activity {
        @Override protected void onDestroy() {
            super.onDestroy();
        }
    }
}
