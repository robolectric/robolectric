package com.xtremelabs.robolectric.shadows;

import android.os.AsyncTask;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.Join;
import com.xtremelabs.robolectric.util.Transcript;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class AsyncTaskTest {
    private Transcript transcript;

    @Before public void setUp() throws Exception {
        transcript = new Transcript();
        Robolectric.backgroundScheduler.pause();
        Robolectric.uiThreadScheduler.pause();
    }

    @Test
    public void testNormalFlow() throws Exception {
        AsyncTask<String, String, String> asyncTask = new MyAsyncTask();

        asyncTask.execute("a", "b");
        transcript.assertEventsSoFar("onPreExecute");

        Robolectric.runBackgroundTasks();
        transcript.assertEventsSoFar("doInBackground a, b");

        Robolectric.runUiThreadTasks();
        transcript.assertEventsSoFar("onPostExecute c");
    }

    @Test
    public void testCancelBeforeBackground() throws Exception {
        AsyncTask<String, String, String> asyncTask = new MyAsyncTask();

        asyncTask.execute("a", "b");
        transcript.assertEventsSoFar("onPreExecute");

        assertTrue(asyncTask.cancel(false));

        Robolectric.runBackgroundTasks();
        transcript.assertNoEventsSoFar();

        Robolectric.runUiThreadTasks();
        transcript.assertNoEventsSoFar();
    }

    @Test
    public void testCancelBeforePostExecute() throws Exception {
        AsyncTask<String, String, String> asyncTask = new MyAsyncTask();

        asyncTask.execute("a", "b");
        transcript.assertEventsSoFar("onPreExecute");

        Robolectric.runBackgroundTasks();
        transcript.assertEventsSoFar("doInBackground a, b");

        assertFalse(asyncTask.cancel(false));

        Robolectric.runUiThreadTasks();
        transcript.assertEventsSoFar("onPostExecute c");
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
            super.onProgressUpdate(values);
        }

        @Override protected void onPostExecute(String s) {
            transcript.add("onPostExecute " + s);
            super.onPostExecute(s);
        }

        @Override protected void onCancelled() {
            super.onCancelled();
        }
    }
}
