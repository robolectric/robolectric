package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.appwidget.AppWidgetProvider;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;

import com.xtremelabs.robolectric.ApplicationResolver;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.Scheduler;

import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;

import static org.junit.Assert.assertEquals;

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
    public void shouldSupportRunOnUiThread() {
        MyActivity activity = new MyActivity();
        Runnable runnable = new Runnable() { public void run() { } };
        ShadowLooper shadowLooper = shadowOf(Looper.myLooper());
        Scheduler scheduler = shadowLooper.getScheduler();
        
        shadowLooper.reset();
        assertThat( scheduler.enqueuedTaskCount(), equalTo( 0 ) );
        activity.runOnUiThread( runnable );
        assertThat( scheduler.enqueuedTaskCount(), equalTo( 1 ) );
    }

    private static class MyActivity extends Activity {
        @Override protected void onDestroy() {
            super.onDestroy();
        }
    }
}
