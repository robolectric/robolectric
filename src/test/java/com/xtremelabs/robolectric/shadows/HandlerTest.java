package com.xtremelabs.robolectric.shadows;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.TestRunnable;
import com.xtremelabs.robolectric.util.Transcript;

@RunWith(WithTestDefaultsRunner.class)
public class HandlerTest {
    private Transcript transcript;
    TestRunnable scratchRunnable = new TestRunnable();

    @Before
    public void setUp() throws Exception {
        transcript = new Transcript();
    }

    @Test
    public void testInsertsRunnablesBasedOnLooper() throws Exception {
        Looper looper = newInstanceOf(Looper.class);

        Handler handler1 = new Handler(looper);
        handler1.post(new Say("first thing"));

        Handler handler2 = new Handler(looper);
        handler2.post(new Say("second thing"));

        shadowOf(looper).idle();

        transcript.assertEventsSoFar("first thing", "second thing");
    }

    @Test
    public void testDefaultConstructorUsesDefaultLooper() throws Exception {
        Handler handler1 = new Handler();
        handler1.post(new Say("first thing"));

        Handler handler2 = new Handler(Looper.myLooper());
        handler2.post(new Say("second thing"));

        shadowOf(Looper.myLooper()).idle();

        transcript.assertEventsSoFar("first thing", "second thing");
    }
    
    @Test
    public void testCallbackConstructorUsesCallback() throws Exception {
        String event1 = "first thing";
        String event2 = "second thing";
        
        Handler handler1 = new Handler(new SayCallback(event1));
        handler1.sendEmptyMessage(0);
        Handler handler2 = new Handler(new SayCallback(event2));
        handler2.sendEmptyMessage(0);
        shadowOf(Looper.myLooper()).idle();

        transcript.assertEventsSoFar(event1, event2);
    }

    @Test
    public void testDifferentLoopersGetDifferentQueues() throws Exception {
        Looper looper1 = Robolectric.newInstanceOf(Looper.class);
        Robolectric.pauseLooper(looper1);

        Looper looper2 = Robolectric.newInstanceOf(Looper.class);
        Robolectric.pauseLooper(looper2);

        Handler handler1 = new Handler(looper1);
        handler1.post(new Say("first thing"));

        Handler handler2 = new Handler(looper2);
        handler2.post(new Say("second thing"));

        shadowOf(looper2).idle();

        transcript.assertEventsSoFar("second thing");
    }

    @Test
    public void testPostAndIdleMainLooper() throws Exception {
        new Handler().post(scratchRunnable);
        ShadowHandler.idleMainLooper();
        assertThat(scratchRunnable.wasRun, equalTo(true));
    }

    @Test
    public void postDelayedThenIdleMainLooper_shouldNotRunRunnable() throws Exception {
        new Handler().postDelayed(scratchRunnable, 1);
        ShadowHandler.idleMainLooper();
        assertThat(scratchRunnable.wasRun, equalTo(false));
    }

    @Test
    public void testPostDelayedThenRunMainLooperOneTask() throws Exception {
        new Handler().postDelayed(scratchRunnable, 1);
        ShadowHandler.runMainLooperOneTask();
        assertThat(scratchRunnable.wasRun, equalTo(true));
    }

    @Test
    public void testPostDelayedThenRunMainLooperToNextTask_shouldRunOneTask() throws Exception {
        new Handler().postDelayed(scratchRunnable, 1);
        ShadowHandler.runMainLooperToNextTask();
        assertThat(scratchRunnable.wasRun, equalTo(true));
    }

    @Test
    public void testPostDelayedTwiceThenRunMainLooperToNextTask_shouldRunMultipleTasks() throws Exception {
        TestRunnable task1 = new TestRunnable();
        TestRunnable task2 = new TestRunnable();

        new Handler().postDelayed(task1, 1);
        new Handler().postDelayed(task2, 1);

        ShadowHandler.runMainLooperToNextTask();
        assertThat(task1.wasRun, equalTo(true));
        assertThat(task2.wasRun, equalTo(true));
    }

    @Test
    public void testPostDelayedTwiceThenRunMainLooperOneTask_shouldRunOnlyOneTask() throws Exception {
        TestRunnable task1 = new TestRunnable();
        TestRunnable task2 = new TestRunnable();

        new Handler().postDelayed(task1, 1);
        new Handler().postDelayed(task2, 1);

        ShadowHandler.runMainLooperOneTask();
        assertThat(task1.wasRun, equalTo(true));
        assertThat(task2.wasRun, equalTo(false));
    }

    @Test
    public void testPostDelayedMultipleThenRunMainLooperOneTask_shouldRunMultipleTask() throws Exception {
        TestRunnable task1 = new TestRunnable();
        TestRunnable task2 = new TestRunnable();
        TestRunnable task3 = new TestRunnable();

        new Handler().postDelayed(task1, 1);
        new Handler().postDelayed(task2, 10);
        new Handler().postDelayed(task3, 100);

        ShadowHandler.runMainLooperToEndOfTasks();
        assertThat(task1.wasRun, equalTo(true));
        assertThat(task2.wasRun, equalTo(true));
        assertThat(task3.wasRun, equalTo(true));
    }

    private class Say implements Runnable {
        private final String event;

        public Say(final String event) {
            this.event = event;
        }

        @Override
        public void run() {
            transcript.add(event);
        }
    }
    
    private class SayCallback implements Handler.Callback {
        private final String event;

        public SayCallback(final String event) {
            this.event = event;
        }

        @Override
        public boolean handleMessage(final Message arg0) {
            transcript.add(event);
            return true;
        }
    }
}
