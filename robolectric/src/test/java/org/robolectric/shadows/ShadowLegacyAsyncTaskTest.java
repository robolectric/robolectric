package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;

import android.os.AsyncTask;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.LooperMode;
import org.robolectric.util.Join;

/**
 * Unit tests for {@link ShadowLegacyAsyncTask}.
 */
@RunWith(AndroidJUnit4.class)
@LooperMode(LEGACY)
public class ShadowLegacyAsyncTaskTest {
  private List<String> transcript;

  @Before
  public void setUp() throws Exception {
    transcript = new ArrayList<>();
    Robolectric.getBackgroundThreadScheduler().pause();
    Robolectric.getForegroundThreadScheduler().pause();
  }

  @Test
  public void testNormalFlow() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();

    asyncTask.execute("a", "b");
    assertThat(transcript).containsExactly("onPreExecute");
    transcript.clear();

    ShadowApplication.runBackgroundTasks();
    assertThat(transcript).containsExactly("doInBackground a, b");
    transcript.clear();
    assertEquals(
        "Result should get stored in the AsyncTask",
        "c",
        asyncTask.get(100, TimeUnit.MILLISECONDS));

    ShadowLooper.runUiThreadTasks();
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

    ShadowApplication.runBackgroundTasks();
    assertThat(transcript).isEmpty();

    ShadowLooper.runUiThreadTasks();
    assertThat(transcript).containsExactly("onCancelled null", "onCancelled");
  }

  @Test
  public void testCancelBeforePostExecute() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();

    asyncTask.execute("a", "b");
    assertThat(transcript).containsExactly("onPreExecute");
    transcript.clear();

    ShadowApplication.runBackgroundTasks();
    assertThat(transcript).containsExactly("doInBackground a, b");
    transcript.clear();
    assertEquals(
        "Result should get stored in the AsyncTask",
        "c",
        asyncTask.get(100, TimeUnit.MILLISECONDS));

    assertFalse(asyncTask.cancel(true));
    assertFalse(asyncTask.isCancelled());

    ShadowLooper.runUiThreadTasks();
    assertThat(transcript).containsExactly("onPostExecute c");
  }

  @Test
  public void progressUpdatesAreQueuedUntilBackgroundThreadFinishes() throws Exception {
    AsyncTask<String, String, String> asyncTask =
        new MyAsyncTask() {
          @Override
          protected String doInBackground(String... strings) {
            publishProgress("33%");
            publishProgress("66%");
            publishProgress("99%");
            return "done";
          }
        };

    asyncTask.execute("a", "b");
    assertThat(transcript).containsExactly("onPreExecute");
    transcript.clear();

    ShadowApplication.runBackgroundTasks();
    assertThat(transcript).isEmpty();
    assertEquals(
        "Result should get stored in the AsyncTask",
        "done",
        asyncTask.get(100, TimeUnit.MILLISECONDS));

    ShadowLooper.runUiThreadTasks();
    assertThat(transcript)
        .containsExactly(
            "onProgressUpdate 33%",
            "onProgressUpdate 66%", "onProgressUpdate 99%", "onPostExecute done");
  }

  @Test
  public void executeReturnsAsyncTask() throws Exception {
    Robolectric.getBackgroundThreadScheduler().unPause();
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();
    assertThat(asyncTask.execute("a", "b").get()).isEqualTo("c");
  }

  @Test
  public void shouldGetStatusForAsyncTask() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();
    assertThat(asyncTask.getStatus()).isEqualTo(AsyncTask.Status.PENDING);
    asyncTask.execute("a");
    assertThat(asyncTask.getStatus()).isEqualTo(AsyncTask.Status.RUNNING);
    Robolectric.getBackgroundThreadScheduler().unPause();
    assertThat(asyncTask.getStatus()).isEqualTo(AsyncTask.Status.FINISHED);
  }

  @Test
  public void onPostExecute_doesNotSwallowExceptions() {
    Robolectric.getBackgroundThreadScheduler().unPause();
    Robolectric.getForegroundThreadScheduler().unPause();

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
      asyncTask.execute();
      fail("Task swallowed onPostExecute() exception!");
    } catch (RuntimeException e) {
      assertThat(e.getCause().getMessage()).isEqualTo("Don't swallow me!");
    }
  }

  @Test
  public void executeOnExecutor_usesPassedExecutor() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();

    assertThat(asyncTask.getStatus()).isEqualTo(AsyncTask.Status.PENDING);

    asyncTask.executeOnExecutor(MoreExecutors.directExecutor(), "a", "b");

    assertThat(asyncTask.getStatus()).isEqualTo(AsyncTask.Status.FINISHED);
    assertThat(transcript).containsExactly("onPreExecute", "doInBackground a, b");
    transcript.clear();
    assertEquals("Result should get stored in the AsyncTask", "c", asyncTask.get());

    ShadowLooper.runUiThreadTasks();
    assertThat(transcript).containsExactly("onPostExecute c");
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
}
