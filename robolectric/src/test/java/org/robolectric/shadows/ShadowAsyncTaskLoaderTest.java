package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.AsyncTaskLoader;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

@RunWith(AndroidJUnit4.class)
public class ShadowAsyncTaskLoaderTest {
  private final List<String> transcript = new ArrayList<>();

  @Before
  public void setUp() {
    Robolectric.getForegroundThreadScheduler().pause();
    Robolectric.getBackgroundThreadScheduler().pause();
  }

  @Test
  public void forceLoad_shouldEnqueueWorkOnSchedulers() {
    new TestLoader(42).forceLoad();
    assertThat(transcript).isEmpty();

    Robolectric.flushBackgroundThreadScheduler();
    assertThat(transcript).containsExactly("loadInBackground");
    transcript.clear();

    Robolectric.flushForegroundThreadScheduler();
    assertThat(transcript).containsExactly("deliverResult 42");
  }

  @Test
  public void forceLoad_multipleLoads() {
    TestLoader testLoader = new TestLoader(42);
    testLoader.forceLoad();
    assertThat(transcript).isEmpty();

    Robolectric.flushBackgroundThreadScheduler();
    assertThat(transcript).containsExactly("loadInBackground");
    transcript.clear();

    Robolectric.flushForegroundThreadScheduler();
    assertThat(transcript).containsExactly("deliverResult 42");

    testLoader.setData(43);
    transcript.clear();
    testLoader.forceLoad();

    Robolectric.flushBackgroundThreadScheduler();
    assertThat(transcript).containsExactly("loadInBackground");
    transcript.clear();

    Robolectric.flushForegroundThreadScheduler();
    assertThat(transcript).containsExactly("deliverResult 43");
  }

  public class TestLoader extends AsyncTaskLoader<Integer> {
    private Integer data;

    public TestLoader(Integer data) {
      super(ApplicationProvider.getApplicationContext());
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

    public void setData(int newData) {
      this.data = newData;
    }
  }
}
