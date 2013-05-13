package org.robolectric.shadows;

import android.os.AsyncTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.util.Join;
import org.robolectric.util.Transcript;

import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;

@RunWith(TestRunners.WithDefaults.class)
public class AsyncTaskTest {
  private Transcript transcript;

  @Before public void setUp() throws Exception {
    transcript = new Transcript();
    Robolectric.getBackgroundScheduler().pause();
    Robolectric.getUiThreadScheduler().pause();
  }

  @Test
  public void testNormalFlow() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();

    asyncTask.execute("a", "b");
    transcript.assertEventsSoFar("onPreExecute");

    Robolectric.runBackgroundTasks();
    transcript.assertEventsSoFar("doInBackground a, b");
    assertEquals("Result should get stored in the AsyncTask", "c", asyncTask.get(100, TimeUnit.MILLISECONDS));

    Robolectric.runUiThreadTasks();
    transcript.assertEventsSoFar("onPostExecute c");
  }

  @Test
  public void testCancelBeforeBackground() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();

    asyncTask.execute("a", "b");
    transcript.assertEventsSoFar("onPreExecute");

    assertTrue(asyncTask.cancel(true));
    assertTrue(asyncTask.isCancelled());

    Robolectric.runBackgroundTasks();
    transcript.assertNoEventsSoFar();

    Robolectric.runUiThreadTasks();
    transcript.assertEventsSoFar("onCancelled");
  }

  @Test
  public void testCancelBeforePostExecute() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();

    asyncTask.execute("a", "b");
    transcript.assertEventsSoFar("onPreExecute");

    Robolectric.runBackgroundTasks();
    transcript.assertEventsSoFar("doInBackground a, b");
    assertEquals("Result should get stored in the AsyncTask", "c", asyncTask.get(100, TimeUnit.MILLISECONDS));

    assertFalse(asyncTask.cancel(true));
    assertFalse(asyncTask.isCancelled());

    Robolectric.runUiThreadTasks();
    transcript.assertEventsSoFar("onPostExecute c");
  }

  @Test
  public void progressUpdatesAreQueuedUntilBackgroundThreadFinishes() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask() {
      @Override protected String doInBackground(String... strings) {
        publishProgress("33%");
        publishProgress("66%");
        publishProgress("99%");
        return "done";
      }
    };

    asyncTask.execute("a", "b");
    transcript.assertEventsSoFar("onPreExecute");

    Robolectric.runBackgroundTasks();
    transcript.assertNoEventsSoFar();
    assertEquals("Result should get stored in the AsyncTask", "done", asyncTask.get(100, TimeUnit.MILLISECONDS));

    Robolectric.runUiThreadTasks();
    transcript.assertEventsSoFar(
        "onProgressUpdate 33%",
        "onProgressUpdate 66%",
        "onProgressUpdate 99%",
        "onPostExecute done");
  }

  @Test
  public void executeReturnsAsyncTask() throws Exception {
    Robolectric.getBackgroundScheduler().unPause();
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();
    assertThat(asyncTask.execute("a", "b").get()).isEqualTo("c");
  }

  @Test
  public void shouldGetStatusForAsyncTask() throws Exception {
    AsyncTask<String, String, String> asyncTask = new MyAsyncTask();
    assertThat(asyncTask.getStatus()).isEqualTo(AsyncTask.Status.PENDING);
    asyncTask.execute("a");
    assertThat(asyncTask.getStatus()).isEqualTo(AsyncTask.Status.RUNNING);
    Robolectric.getBackgroundScheduler().unPause();
    assertThat(asyncTask.getStatus()).isEqualTo(AsyncTask.Status.FINISHED);
  }

  private class MyAsyncTask extends AsyncTask<String, String, String> {
    @Override protected void onPreExecute() {
      transcript.add("onPreExecute");
    }

    @Override protected String doInBackground(String... strings) {
      transcript.add("doInBackground " + Join.join(", ", (Object[]) strings));
      return "c";
    }

    @Override protected void onProgressUpdate(String... values) {
      transcript.add("onProgressUpdate " + Join.join(", ", (Object[]) values));
    }

    @Override protected void onPostExecute(String s) {
      transcript.add("onPostExecute " + s);
    }

    @Override protected void onCancelled() {
      transcript.add("onCancelled");
    }
  }
}
