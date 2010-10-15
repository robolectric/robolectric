package com.xtremelabs.robolectric.fakes;

import android.os.Handler;
import android.os.Looper;
import com.xtremelabs.robolectric.RobolectricAndroidTestRunner;
import com.xtremelabs.robolectric.util.FakeHelper;
import com.xtremelabs.robolectric.util.Transcript;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.util.FakeHelper.newInstanceOf;

@RunWith(RobolectricAndroidTestRunner.class)
public class HandlerTest {
    private Transcript transcript;

    @Before
    public void setUp() throws Exception {
        RobolectricAndroidTestRunner.addProxy(Handler.class, FakeHandler.class);
        RobolectricAndroidTestRunner.addProxy(Looper.class, FakeLooper.class);

        transcript = new Transcript();
    }

    @Test
    public void testInsertsRunnablesBasedOnLooper() throws Exception {
        Looper looper = newInstanceOf(Looper.class);

        Handler handler1 = new Handler(looper);
        handler1.post(new Say("first thing"));

        Handler handler2 = new Handler(looper);
        handler2.post(new Say("second thing"));

        proxyFor(looper).idle();

        transcript.assertEventsSoFar("first thing", "second thing");
    }

    @Test
    public void testDefaultConstructorUsesDefaultLooper() throws Exception {
        Handler handler1 = new Handler();
        handler1.post(new Say("first thing"));

        Handler handler2 = new Handler(Looper.myLooper());
        handler2.post(new Say("second thing"));

        proxyFor(Looper.myLooper()).idle();

        transcript.assertEventsSoFar("first thing", "second thing");
    }

    @Test
    public void testDifferentLoopersGetDifferentQueues() throws Exception {
        Looper looper1 = FakeHelper.newInstanceOf(Looper.class);
        Looper looper2 = FakeHelper.newInstanceOf(Looper.class);

        Handler handler1 = new Handler(looper1);
        handler1.post(new Say("first thing"));

        Handler handler2 = new Handler(looper2);
        handler2.post(new Say("second thing"));

        proxyFor(looper2).idle();

        transcript.assertEventsSoFar("second thing");
    }


    private FakeLooper proxyFor(Looper view) {
        return (FakeLooper) RobolectricAndroidTestRunner.proxyFor(view);
    }

    private class Say implements Runnable {
        private String event;

        public Say(String event) {
            this.event = event;
        }

        @Override
        public void run() {
            transcript.add(event);
        }
    }
}
