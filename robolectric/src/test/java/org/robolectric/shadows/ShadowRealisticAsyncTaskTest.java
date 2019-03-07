package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.robolectric.shadows.ShadowBaseLooper.shadowMainLooper;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Looper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.util.Join;

@RunWith(AndroidJUnit4.class)
public class ShadowRealisticAsyncTaskTest {
  private List<String> transcript;

  @Before
  public void setUp() throws Exception {
    transcript = new ArrayList<>();
  }

  @Test
  public void testNormalFlow() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();

    asyncTask.execute("a", "b");

    ShadowRealisticAsyncTask.idle();
    assertThat(transcript).containsExactly("onPreExecute", "doInBackground a, b");
    transcript.clear();
    assertEquals("Result should get stored in the AsyncTask", "c", asyncTask.get(100, TimeUnit.MILLISECONDS));

    shadowMainLooper().idle();
    assertThat(transcript).containsExactly("onPostExecute c");
  }

  @Test
  public void testCancelBeforeBackground() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();

    asyncTask.execute("a", "b");
    assertThat(transcript).containsExactly("onPreExecute");
    transcript.clear();

    assertTrue(asyncTask.cancel(true));
    assertTrue(asyncTask.isCancelled());

    ShadowRealisticAsyncTask.idle();
    assertThat(transcript).isEmpty();

    shadowMainLooper().idle();
    assertThat(transcript).containsExactly("onCancelled null", "onCancelled");
  }

  @Test
  public void testCancelBeforePostExecute() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();

    asyncTask.execute("a", "b");

    ShadowRealisticAsyncTask.idle();
    assertThat(transcript).containsExactly("onPreExecute", "doInBackground a, b");

    transcript.clear();
    assertEquals("Result should get stored in the AsyncTask", "c", asyncTask.get(100, TimeUnit.MILLISECONDS));

    assertFalse(asyncTask.cancel(true));
    assertTrue(asyncTask.isCancelled());

    shadowMainLooper().idle();
    assertThat(transcript).containsExactly("onCancelled c", "onCancelled");
  }

  @Test
  public void progressUpdatesAreQueuedUntilBackgroundThreadFinishes() throws Exception {
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

    ShadowRealisticAsyncTask.idle();
    transcript.clear();
    assertThat(transcript).isEmpty();
    assertEquals("Result should get stored in the AsyncTask", "done", asyncTask.get(100, TimeUnit.MILLISECONDS));

    shadowMainLooper().idle();
    assertThat(transcript).containsExactly(
        "onProgressUpdate 33%",
        "onProgressUpdate 66%",
        "onProgressUpdate 99%",
        "onPostExecute done"
    );
  }

  @Test
  public void shouldGetStatusForAsyncTask() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();
    assertThat(asyncTask.getStatus()).isEqualTo(AsyncTask.Status.PENDING);
    asyncTask.execute("a");
    ShadowRealisticAsyncTask.idle();
    assertThat(asyncTask.getStatus()).isEqualTo(Status.RUNNING);
    shadowMainLooper().idle();
    assertThat(asyncTask.getStatus()).isEqualTo(Status.FINISHED);
  }

  @Test
  public void onPostExecute_doesNotSwallowExceptions() throws Exception {
    AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
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
      asyncTask.execute();
      ShadowRealisticAsyncTask.idle();
      shadowMainLooper().idle();
      fail("Task swallowed onPostExecute() exception!");
    } catch (RuntimeException e) {
      assertThat(e.getMessage()).isEqualTo("Don't swallow me!");
    }
  }

  @Test
  public void executeOnExecutor_usesPassedExecutor() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();

    assertThat(asyncTask.getStatus()).isEqualTo(AsyncTask.Status.PENDING);

    asyncTask.executeOnExecutor(new ImmediateExecutor(), "a", "b");

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
    AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {
        boolean isMainLooper = Looper.getMainLooper().getThread() == Thread.currentThread();
        transcript.add("doInBackground on main looper " + Boolean.toString(isMainLooper));
        return null;
      }
    };
    asyncTask.execute();
    ShadowRealisticAsyncTask.idle();
    assertThat(transcript).containsExactly("doInBackground on main looper false");
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

  public static class ImmediateExecutor implements Executor {
    @Override
    public void execute(Runnable command) {
      command.run();
    }
  }
}
