package com.xtremelabs.robolectric.bytecode;

import com.xtremelabs.robolectric.TestRunners;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.Instrument;
import com.xtremelabs.robolectric.internal.RealObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;

import static com.xtremelabs.robolectric.Robolectric.*;
import static org.junit.Assert.assertSame;

@RunWith(TestRunners.WithoutDefaults.class)
public class ThreadSafetyTest {
    @Test
    public void shadowCreationShouldBeThreadsafe() throws Exception {
        bindShadowClass(InstrumentedThreadShadow.class);
        Field field = InstrumentedThread.class.getDeclaredField("shadowFromOtherThread");
        field.setAccessible(true);

        for (int i = 0; i < 100; i++) { // :-(
            InstrumentedThread instrumentedThread = new InstrumentedThread();
            instrumentedThread.start();
            Object shadowFromThisThread = shadowOf_(instrumentedThread);

            instrumentedThread.join();
            Object shadowFromOtherThread = field.get(instrumentedThread);
            assertSame(shadowFromThisThread, shadowFromOtherThread);
        }
    }

    @Instrument
    public static class InstrumentedThread extends Thread {
        InstrumentedThreadShadow shadowFromOtherThread;

        @Override
        public void run() {
            shadowFromOtherThread = shadowOf_(this);
        }
    }

    @Implements(InstrumentedThread.class)
    public static class InstrumentedThreadShadow {
        @RealObject InstrumentedThread realObject;
        @Implementation
        public void run() {
            directlyOn(realObject).run();
        }
    }
}
