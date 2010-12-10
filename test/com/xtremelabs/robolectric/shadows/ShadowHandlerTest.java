package com.xtremelabs.robolectric.shadows;

import android.os.Handler;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)

public class ShadowHandlerTest {
    TestRunnable scratchRunnable = new TestRunnable();

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

    static class TestRunnable implements Runnable {
        public boolean wasRun = false;

        @Override public void run() {
            wasRun = true;
        }
    }
}
