package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;

import android.content.AsyncTaskLoader;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.LooperMode;

/**
 * Unit tests for {@link ShadowLegacyAsyncTaskLoader}.
 */
@RunWith(AndroidJUnit4.class)
@LooperMode(LEGACY)
public class ShadowLegacyAsyncTaskLoaderTest {
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

  class TestLoader extends AsyncTaskLoader<Integer> {
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
