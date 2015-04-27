package org.robolectric.shadows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.Transcript;
import org.robolectric.util.TestRunnerWithManifest;
import android.support.v4.content.AsyncTaskLoader;

@RunWith(TestRunnerWithManifest.class)
public class ShadowAsyncTaskLoaderTest {
  private final Transcript transcript = new Transcript();

  @Before
  public void setUp() {
    ShadowLooper.getUiThreadScheduler().pause();
    ShadowApplication.getInstance().getBackgroundScheduler().pause();
  }

  @Test
  public void forceLoad_shouldEnqueueWorkOnSchedulers() {
    new TestLoader(42).forceLoad();
    transcript.assertNoEventsSoFar();

    ShadowApplication.getInstance().getBackgroundScheduler().runOneTask();
    transcript.assertEventsSoFar("loadInBackground");

    ShadowLooper.getUiThreadScheduler().runOneTask();
    transcript.assertEventsSoFar("deliverResult 42");
  }

  public class TestLoader extends AsyncTaskLoader<Integer> {
    private final Integer data;

    public TestLoader(Integer data) {
      super(RuntimeEnvironment.application);
      this.data = data;
    }

    @Override
    public Integer loadInBackground() {
      transcript.add("loadInBackground");
      return data;
    }

    @Override
    public void deliverResult(Integer data) {
      transcript.add("deliverResult " + data.toString());
    }
  }
}
