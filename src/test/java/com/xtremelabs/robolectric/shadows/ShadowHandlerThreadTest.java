package com.xtremelabs.robolectric.shadows;

import android.os.HandlerThread;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ShadowHandlerThreadTest {
    @Test
    public void getLooper_returnsLooper() {
        HandlerThread handlerThread = new HandlerThread("test");
        handlerThread.start();
        assertThat(handlerThread.getLooper(), notNullValue());
    }
}
