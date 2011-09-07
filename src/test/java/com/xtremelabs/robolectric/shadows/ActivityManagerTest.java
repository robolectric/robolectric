package com.xtremelabs.robolectric.shadows;

import android.app.ActivityManager;
import android.content.Context;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class ActivityManagerTest {
    @Test
    public void canGetMemoryInfoForOurProcess() {
        ActivityManager activityManager = (ActivityManager) Robolectric.application.getSystemService(Context.ACTIVITY_SERVICE);
        ShadowActivityManager shadowActivityManager = shadowOf(activityManager);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        memoryInfo.lowMemory = true;
        shadowActivityManager.setMemoryInfo(memoryInfo);
        ActivityManager.MemoryInfo fetchedMemoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(fetchedMemoryInfo);
        assertTrue(fetchedMemoryInfo.lowMemory);
    }
    
    @Test
    public void canGetMemoryInfoEvenWhenWeDidNotSetIt() {
        ActivityManager activityManager = (ActivityManager) Robolectric.application.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo fetchedMemoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(fetchedMemoryInfo);
        assertFalse(fetchedMemoryInfo.lowMemory);
    }
}
