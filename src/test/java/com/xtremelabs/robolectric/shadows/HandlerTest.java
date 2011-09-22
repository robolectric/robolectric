package com.xtremelabs.robolectric.shadows;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.TestRunnable;
import com.xtremelabs.robolectric.util.Transcript;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

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
    public void testRemoveCallbacks() throws Exception {
        Handler handler = new Handler();
        ShadowLooper shadowLooper = shadowOf(handler.getLooper());
        shadowLooper.pause();
        handler.post(scratchRunnable);
        handler.removeCallbacks(scratchRunnable);

        shadowLooper.unPause();

        assertThat(scratchRunnable.wasRun, equalTo(false));
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

    @Test
    public void sendEmptyMessageHandler() {

        final Handler handler = new Handler(new Handler.Callback() {

            @Override
            public boolean handleMessage(Message message) {
                throw new UnsupportedOperationException("Method not implemented");
            }

        });
        handler.sendEmptyMessage(0);
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
