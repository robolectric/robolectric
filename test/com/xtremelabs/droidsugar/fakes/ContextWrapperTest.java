package com.xtremelabs.droidsugar.fakes;

import android.app.Activity;
import android.content.*;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import com.xtremelabs.droidsugar.util.Transcript;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DroidSugarAndroidTestRunner.class)
public class ContextWrapperTest {
    public Transcript transcript;

    @Before public void setUp() throws Exception {
        DroidSugarAndroidTestRunner.addGenericProxies();

        transcript = new Transcript();
    }

    @Test
    public void registerReceiver_shouldRegisterForAllIntentFilterActions() throws Exception {
        IntentFilter intentFilter = new IntentFilter("foo");
        intentFilter.addAction("baz");

        ContextWrapper contextWrapper = new ContextWrapper(new Activity());
        contextWrapper.registerReceiver(new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                transcript.add("notified of " + intent.getAction());
            }
        }, intentFilter);

        contextWrapper.sendBroadcast(new Intent("foo"));
        transcript.assertEventsSoFar("notified of foo");

        contextWrapper.sendBroadcast(new Intent("womp"));
        transcript.assertNoEventsSoFar();

        contextWrapper.sendBroadcast(new Intent("baz"));
        transcript.assertEventsSoFar("notified of baz");
    }
}
