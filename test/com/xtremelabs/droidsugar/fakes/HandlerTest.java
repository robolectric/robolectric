package com.xtremelabs.droidsugar.fakes;

import android.os.Handler;
import android.os.Looper;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import com.xtremelabs.droidsugar.util.Transcript;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.droidsugar.fakes.FakeHelper.newInstanceOf;

@RunWith(DroidSugarAndroidTestRunner.class)
public class HandlerTest {
    private Transcript transcript;

    @Before
    public void setUp() throws Exception {
        DroidSugarAndroidTestRunner.addProxy(Handler.class, FakeHandler.class);
        DroidSugarAndroidTestRunner.addProxy(Looper.class, FakeLooper.class);

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
        Handler handler1 = new Handler(Looper.getMainLooper());
        handler1.post(new Say("first thing"));

        Handler handler2 = new Handler(Looper.myLooper());
        handler2.post(new Say("second thing"));

        proxyFor(Looper.myLooper()).idle();

        transcript.assertEventsSoFar("second thing");
    }


    private FakeLooper proxyFor(Looper view) {
        return (FakeLooper) DroidSugarAndroidTestRunner.proxyFor(view);
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
