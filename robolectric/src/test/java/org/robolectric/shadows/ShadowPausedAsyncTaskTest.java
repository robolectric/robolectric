package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Looper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.android.util.concurrent.PausedExecutorService;
import org.robolectric.annotation.LooperMode;
import org.robolectric.util.Join;

/**
 * Unit tests for {@link ShadowPausedAsyncTask}.
 */
@RunWith(AndroidJUnit4.class)
@LooperMode(PAUSED)
public class ShadowPausedAsyncTaskTest {
  private List<String> transcript;

  @Before
  public void setUp() throws Exception {
    transcript = new ArrayList<>();
  }

  /** Test uses AsyncTask without overridding executor. */
  @Test
  public void testNormalFlow() throws Exception {
    AsyncTask<String, String, String> asyncTask = new RecordingAsyncTask();

    String result = asyncTask.execute("a", "b").get();
    assertThat(transcript).containsExactly("onPreExecute", "doInBackground a, b");
    assertThat(result).isEqualTo("c");
    transcript.clear();
    shadowMainLooper().idle();
    assertThat(transcript).containsExactly("onPostExecute c");
  }

  @Test
  public void testCancelBeforeBackground() {
    AsyncTask<String, String, String> asyncTask = new RecordingAsyncTask();

    // rely on AsyncTask being processed serially on a single background thread, and block
    // processing
    BlockingAsyncTask blockingAsyncTask = new BlockingAsyncTask();
    blockingAsyncTask.execute();

    asyncTask.execute("a", "b");
    assertThat(transcript).containsExactly("onPreExecute");
    transcript.clear();

    assertTrue(asyncTask.cancel(true));
    assertTrue(asyncTask.isCancelled());

    blockingAsyncTask.release();

    assertThat(transcript).isEmpty();

    shadowMainLooper().idle();
    assertThat(transcript).containsExactly("onCancelled null", "onCancelled");
  }

  @Test
  public void testCancelBeforePostExecute() throws Exception {
    AsyncTask<String, String, String> asyncTask = new RecordingAsyncTask();

    asyncTask.execute("a", "b").get();

    assertThat(transcript).containsExactly("onPreExecute", "doInBackground a, b");

    transcript.clear();
    assertEquals(
        "Result should get stored in the AsyncTask",
        "c",
        asyncTask.get(100, TimeUnit.MILLISECONDS));

    assertFalse(asyncTask.cancel(true));
    assertTrue(asyncTask.isCancelled());

    shadowMainLooper().idle();
    assertThat(transcript).containsExactly("onCancelled c", "onCancelled");
  }

  @Test
  public void progressUpdatesAreQueuedUntilBackgroundThreadFinishes() throws Exception {
    AsyncTask<String, String, String> asyncTask =
        new RecordingAsyncTask() {
          @Override
          protected String doInBackground(String... strings) {
            publishProgress("33%");
            publishProgress("66%");
            publishProgress("99%");
            return "done";
          }
        };

    asyncTask.execute("a", "b").get();

    transcript.clear();
    assertThat(transcript).isEmpty();
    assertEquals(
        "Result should get stored in the AsyncTask",
        "done",
        asyncTask.get(100, TimeUnit.MILLISECONDS));

    shadowMainLooper().idle();
    assertThat(transcript)
        .containsExactly(
            "onProgressUpdate 33%",
            "onProgressUpdate 66%", "onProgressUpdate 99%", "onPostExecute done");
  }

  @Test
  public void shouldGetStatusForAsyncTask() throws Exception {
    AsyncTask<String, String, String> asyncTask = new RecordingAsyncTask();
    assertThat(asyncTask.getStatus()).isEqualTo(AsyncTask.Status.PENDING);
    asyncTask.execute("a").get();

    assertThat(asyncTask.getStatus()).isEqualTo(Status.RUNNING);
    shadowMainLooper().idle();
    assertThat(asyncTask.getStatus()).isEqualTo(Status.FINISHED);
  }

  @Test
  public void onPostExecute_doesNotSwallowExceptions() throws Exception {
    AsyncTask<Void, Void, Void> asyncTask =
        new AsyncTask<Void, Void, Void>() {
          @Override
          protected Void doInBackground(Void... params) {
            return null;
          }

          @Override
          protected void onPostExecute(Void aVoid) {
            throw new RuntimeException("Don't swallow me!");
          }
        };

    try {
      asyncTask.execute().get();

      shadowMainLooper().idle();
      fail("Task swallowed onPostExecute() exception!");
    } catch (RuntimeException e) {
      assertThat(e.getMessage()).isEqualTo("Don't swallow me!");
    }
  }

  @Test
  public void executeOnExecutor_usesPassedExecutor() throws Exception {
    AsyncTask<String, String, String> asyncTask = new RecordingAsyncTask();

    assertThat(asyncTask.getStatus()).isEqualTo(AsyncTask.Status.PENDING);

    asyncTask.executeOnExecutor(MoreExecutors.directExecutor(), "a", "b");

    assertThat(asyncTask.getStatus()).isEqualTo(Status.RUNNING);
    assertThat(transcript).containsExactly("onPreExecute", "doInBackground a, b");
    transcript.clear();
    assertEquals("Result should get stored in the AsyncTask", "c", asyncTask.get());

    shadowMainLooper().idle();
    assertThat(transcript).containsExactly("onPostExecute c");
    assertThat(asyncTask.getStatus()).isEqualTo(Status.FINISHED);
  }

  @Test
  public void asyncTasksExecuteInBackground() throws ExecutionException, InterruptedException {
    AsyncTask<Void, Void, Void> asyncTask =
        new AsyncTask<Void, Void, Void>() {
          @Override
          protected Void doInBackground(Void... params) {
            boolean isMainLooper = Looper.getMainLooper().getThread() == Thread.currentThread();
            transcript.add("doInBackground on main looper " + Boolean.toString(isMainLooper));
            return null;
          }
        };
    asyncTask.execute().get();
    assertThat(transcript).containsExactly("doInBackground on main looper false");
  }

  @Test
  public void overrideExecutor() throws ExecutionException, InterruptedException {
    PausedExecutorService pausedExecutor = new PausedExecutorService();
    ShadowPausedAsyncTask.overrideExecutor(pausedExecutor);

    AsyncTask<String, String, String> asyncTask = new RecordingAsyncTask();

    asyncTask.execute("a", "b");
    assertThat(transcript).containsExactly("onPreExecute");
    transcript.clear();
    pausedExecutor.runAll();
    assertThat(transcript).containsExactly("doInBackground a, b");
    assertThat(asyncTask.get()).isEqualTo("c");
    transcript.clear();
    shadowMainLooper().idle();
    assertThat(transcript).containsExactly("onPostExecute c");
  }

  private class RecordingAsyncTask extends AsyncTask<String, String, String> {
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
    protected void onCancelled(String result) {
      transcript.add("onCancelled " + result);
      // super should call onCancelled() without arguments
      super.onCancelled(result);
    }

    @Override
    protected void onCancelled() {
      transcript.add("onCancelled");
    }
  }

  private static class BlockingAsyncTask extends AsyncTask<Void, Void, Void> {

    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    protected Void doInBackground(Void... voids) {
      try {
        latch.await();
      } catch (InterruptedException e) {
        // ignore
      }
      return null;
    }

    void release() {
      latch.countDown();
    }
  }
}
