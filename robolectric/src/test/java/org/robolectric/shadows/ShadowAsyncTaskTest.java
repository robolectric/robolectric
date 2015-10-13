package org.robolectric.shadows;

import android.os.AsyncTask;
import android.os.Looper;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.util.Join;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Transcript;

import java.util.ArrayDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowAsyncTaskTest {
  private Transcript transcript;

  @Before
  public void setUp() throws Exception {
    transcript = new Transcript();
    Robolectric.getBackgroundThreadScheduler().pause();
    Robolectric.getForegroundThreadScheduler().pause();
  }

  @Test
  public void reset_shouldEmptyExecutorQueue() {
    ArrayDeque<Runnable> serialQueue = ReflectionHelpers.getField(AsyncTask.SERIAL_EXECUTOR, "mTasks");

    for (int i = 0; i < 5; i++) {
      AsyncTask<String, String, String> asyncTask = new MyAsyncTask();
      asyncTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "a", "b");
    }
    int queueSize;
    synchronized (AsyncTask.SERIAL_EXECUTOR) {
      queueSize = serialQueue.size();
      if (ReflectionHelpers.getField(AsyncTask.SERIAL_EXECUTOR, "mActive") != null) {
        queueSize++;
      }
    }
    assertThat(queueSize).as("beforeReset").isGreaterThan(0);
    ShadowAsyncTask.reset();
    assertThat(serialQueue).as("afterReset").isEmpty();
    assertThat(ReflectionHelpers.getField(AsyncTask.SERIAL_EXECUTOR, "mActive")).as("afterReset.active").isNull();
  }

  @Test
  public void whenAborted_beforeExecute_taskTerminates_beforeRunningAnything() {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();

    shadowOf(asyncTask).abort();
    assertThat(asyncTask.execute("a", "b")).as("execute").isSameAs(asyncTask);
    transcript.assertNoEventsSoFar();
    ShadowApplication.runBackgroundTasks();
    transcript.assertNoEventsSoFar();
    ShadowLooper.runUiThreadTasks();
    transcript.assertNoEventsSoFar();
  }

  @Test
  public void whenAborted_beforeBackground_taskTerminates_beforeRunningDoInBackground() {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();

    assertThat(asyncTask.execute("a", "b")).as("execute").isSameAs(asyncTask);
    transcript.assertEventsSoFar("onPreExecute");
    shadowOf(asyncTask).abort();
    ShadowApplication.runBackgroundTasks();
    transcript.assertNoEventsSoFar();
    ShadowLooper.runUiThreadTasks();
    transcript.assertNoEventsSoFar();
  }

  @Test
  public void whenAborted_beforeFinish_taskTerminates_beforeRunningPostTasks() {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();

    assertThat(asyncTask.execute("a", "b")).as("execute").isSameAs(asyncTask);
    transcript.assertEventsSoFar("onPreExecute");
    ShadowApplication.runBackgroundTasks();
    transcript.assertEventsSoFar("doInBackground a, b");
    shadowOf(asyncTask).abort();
    ShadowLooper.runUiThreadTasks();
    transcript.assertNoEventsSoFar();
  }

  @Test
  public void testNormalFlow() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();

    asyncTask.execute("a", "b");
    transcript.assertEventsSoFar("onPreExecute");
    assertThat(Robolectric.getBackgroundThreadScheduler().size()).isEqualTo(1);
    ShadowApplication.runBackgroundTasks();
    transcript.assertEventsSoFar("doInBackground a, b");
    assertEquals("Result should get stored in the AsyncTask", "c", asyncTask.get(100, TimeUnit.MILLISECONDS));

    ShadowLooper.runUiThreadTasks();
    transcript.assertEventsSoFar("onPostExecute c");
  }

  @Test
  public void whenCancelled_beforeExecute_shouldCancelSuccessfully() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();

    assertThat(asyncTask.cancel(true)).as("cancel").isTrue();
    assertThat(asyncTask.isCancelled()).as("isCancelled").isTrue();

    asyncTask.execute("a", "b");
    transcript.assertEventsSoFar("onPreExecute");

    ShadowApplication.runBackgroundTasks();

    transcript.assertNoEventsSoFar();

    ShadowLooper.runUiThreadTasks();
    transcript.assertEventsSoFar("onCancelled null");
  }

  @Test
  public void whenCancelled_beforeBackgroundTasks_shouldCancelSuccessfully() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();

    asyncTask.execute("a", "b");
    transcript.assertEventsSoFar("onPreExecute");

    assertThat(asyncTask.cancel(true)).as("cancel").isTrue();
    assertThat(asyncTask.isCancelled()).as("isCancelled").isTrue();

    ShadowApplication.runBackgroundTasks();

    transcript.assertNoEventsSoFar();

    ShadowLooper.runUiThreadTasks();
    transcript.assertEventsSoFar("onCancelled null");
  }

  @Test
  public void whenCancelled_beforeFinishCallback_shouldCallOnCancelled() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();

    asyncTask.execute("a", "b");
    transcript.assertEventsSoFar("onPreExecute");

    ShadowApplication.runBackgroundTasks();
    transcript.assertEventsSoFar("doInBackground a, b");
    assertThat(asyncTask.get(100, TimeUnit.MILLISECONDS)).as("Result should get stored in the AsyncTask").isEqualTo("c");

    assertThat(asyncTask.cancel(true)).as("cancel").isFalse();
    assertThat(asyncTask.isCancelled()).as("isCancelled").isTrue();

    ShadowLooper.runUiThreadTasks();
    transcript.assertEventsSoFar("onCancelled c");
  }

  @Test
  public void whenCancelled_afterFinishCallback_shouldCallOnPostExecute() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();

    asyncTask.execute("a", "b");
    transcript.assertEventsSoFar("onPreExecute");

    ShadowApplication.runBackgroundTasks();
    transcript.assertEventsSoFar("doInBackground a, b");
    assertThat(asyncTask.get(100, TimeUnit.MILLISECONDS)).as("Result should get stored in the AsyncTask").isEqualTo("c");

    // Ensures that the finish() callback has been invoked. The decision on whether to invoke
    // onPostExecute() or onCancelled() is made late; when the UI-thread callback is invoked. If
    // we cancel before the callback is processed, we'll still cause onCancelled() to run.
    ShadowLooper.runUiThreadTasks();
    transcript.assertEventsSoFar("onPostExecute c");

    assertThat(asyncTask.cancel(true)).as("cancel").isFalse();
    assertThat(asyncTask.isCancelled()).as("isCancelled").isTrue();
  }

  @Test
  public void progressUpdates_shouldBeQueued_untilBackgroundThreadFinishes() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask() {
      @Override
      protected String doInBackground(String... strings) {
        publishProgress("33%");
        publishProgress("66%");
        publishProgress("99%");
        return "done";
      }
    };

    asyncTask.execute("a", "b");
    transcript.assertEventsSoFar("onPreExecute");

    ShadowApplication.runBackgroundTasks();
    transcript.assertNoEventsSoFar();
    assertThat(asyncTask.get(100, TimeUnit.MILLISECONDS)).as("Result should get stored in the AsyncTask").isEqualTo("done");

    ShadowLooper.runUiThreadTasks();
    transcript.assertEventsSoFar(
        "onProgressUpdate 33%",
        "onProgressUpdate 66%",
        "onProgressUpdate 99%",
        "onPostExecute done");
  }

  @Test
  public void execute_returnsSelf() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();
    assertThat(asyncTask.execute("a", "b")).isSameAs(asyncTask);
  }

  @Test
  public void shouldGetStatusForAsyncTask() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();
    assertThat(asyncTask.getStatus()).as("before").isEqualTo(AsyncTask.Status.PENDING);
    asyncTask.execute("a");
    assertThat(asyncTask.getStatus()).as("during").isEqualTo(AsyncTask.Status.RUNNING);
    Robolectric.getBackgroundThreadScheduler().unPause();
    // Original implementation checked for status==FINISHED immediately after running background
    // tasks. This is not sufficient because the state is only changed after final UI-thread
    // callback is run.
    assertThat(asyncTask.getStatus()).as("after execute, before post").isEqualTo(AsyncTask.Status.RUNNING);
    Robolectric.getForegroundThreadScheduler().unPause();
    assertThat(asyncTask.getStatus()).as("after").isEqualTo(AsyncTask.Status.FINISHED);
  }

  @Test
  public void onPostExecute_doesNotSwallowExceptions() throws Exception {
    Robolectric.getBackgroundThreadScheduler().unPause();

    final RuntimeException re = new RuntimeException("Don't swallow me!");
    AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {
        return null;
      }

      @Override
      protected void onPostExecute(Void aVoid) {
        throw re;
      }
    };

    asyncTask.execute();

    try {
      Robolectric.flushForegroundThreadScheduler();
      Assertions.failBecauseExceptionWasNotThrown(RuntimeException.class);
    } catch (RuntimeException e) {
      assertThat(e).as("exception").isSameAs(re);
    }
  }

  @Test(timeout=5000)
  public void executeOnExecutor_withImmediateExecutor_doesntBlock() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();
    assertThat(asyncTask.getStatus()).isEqualTo(AsyncTask.Status.PENDING);

    asyncTask.executeOnExecutor(new ImmediateExecutor(), "a", "b");
    // Changed from FINISHED to RUNNING, because the transition to FINISHED
    // doesn't happen until the final UI task is run.
    assertThat(asyncTask.getStatus()).isEqualTo(AsyncTask.Status.RUNNING);

    transcript.assertEventsSoFar("onPreExecute", "doInBackground a, b");
    assertThat(asyncTask.get()).as("Result should get stored in the AsyncTask").isEqualTo("c");

    ShadowLooper.runUiThreadTasks();
    assertThat(asyncTask.getStatus()).isEqualTo(AsyncTask.Status.FINISHED);
    transcript.assertEventsSoFar("onPostExecute c");
  }

  @Test
  public void executeOnExecutor_withParallelExecutor_andMultipleTasks() {
    MyAsyncTask[] asyncTask = new MyAsyncTask[4];

    for (int i = 0; i < asyncTask.length; i++) {
      asyncTask[i] = new MyAsyncTask();
      assertThat(asyncTask[i].getStatus()).as("task " + i + ".getStatus() before").isEqualTo(AsyncTask.Status.PENDING);
      asyncTask[i].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "" + i);
      assertThat(asyncTask[i].getStatus()).as("task " + i + ".getStatus() after execute").isEqualTo(AsyncTask.Status.RUNNING);
    }
    transcript.assertEventsSoFar("onPreExecute", "onPreExecute", "onPreExecute", "onPreExecute");
    Robolectric.flushBackgroundThreadScheduler();
    transcript.assertEventsSoFar("doInBackground 0", "doInBackground 1", "doInBackground 2", "doInBackground 3");
    for (int i = 0; i < asyncTask.length; i++) {
      assertThat(asyncTask[i].getStatus()).as("task " + i + ".getStatus() after flush").isEqualTo(AsyncTask.Status.RUNNING);
    }
    Robolectric.flushForegroundThreadScheduler();
    transcript.assertEventsSoFar("onPostExecute c", "onPostExecute c", "onPostExecute c", "onPostExecute c");
    for (int i = 0; i < asyncTask.length; i++) {
      assertThat(asyncTask[i].getStatus()).as("task " + i + ".getStatus() after fg flush").isEqualTo(AsyncTask.Status.FINISHED);
    }
  }

  @Test
  public void doOnBackground_doesOnBackground() throws Exception {
    Robolectric.getBackgroundThreadScheduler().unPause();
    final AtomicBoolean mainThread = new AtomicBoolean(true);
    final AtomicReference<Looper> looper = new AtomicReference<>(Looper.getMainLooper());
    new AsyncTask<Void,Void,Void>() {
      @Override
      protected Void doInBackground(Void... voids) {
        mainThread.set(RuntimeEnvironment.isMainThread());
        looper.set(Looper.myLooper());
        return null;
      }
    }.execute().get();
    assertThat(mainThread.get()).as("mainThread").isFalse();
    assertThat(looper.get()).as("looper").isNull();
  }


  private class MyAsyncTask extends AsyncTask<String, String, String> {
    @Override
    protected void onPreExecute() {
      transcript.add("onPreExecute");
    }

    @Override
    protected String doInBackground(String... strings) {
      transcript.add("doInBackground " + Join.join(", ", (Object[]) strings));
      return "c";
    }

    @Override
    protected void onProgressUpdate(String... values) {
      transcript.add("onProgressUpdate " + Join.join(", ", (Object[]) values));
    }

    @Override
    protected void onPostExecute(String s) {
      transcript.add("onPostExecute " + s);
    }

    @Override
    protected void onCancelled(String s) {
      transcript.add("onCancelled " + s);
    }
  }

  public class ImmediateExecutor implements Executor {
    @Override
    public void execute(Runnable command) {
      command.run();
    }
  }
}
