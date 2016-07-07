package org.robolectric.shadows;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.Transcript;
import android.content.AsyncTaskLoader;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowAsyncTaskLoaderTest {
  private final Transcript transcript = new Transcript();

  @Before
  public void setUp() {
    Robolectric.getForegroundThreadScheduler().pause();
    Robolectric.getBackgroundThreadScheduler().pause();
  }

  @Test
  public void forceLoad_shouldEnqueueWorkOnSchedulers() {
    new TestLoader(42).forceLoad();
    transcript.assertNoEventsSoFar();

    Robolectric.flushBackgroundThreadScheduler();
    transcript.assertEventsSoFar("loadInBackground");

    Robolectric.flushForegroundThreadScheduler();
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
