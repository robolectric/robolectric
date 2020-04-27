package org.robolectric.shadows.support.v4;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import androidx.loader.content.AsyncTaskLoader;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.util.concurrent.PausedExecutorService;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadow.api.Shadow;

/** Unit tests for {@link ShadowPausedAsyncTaskLoader}. */
@RunWith(AndroidJUnit4.class)
@LooperMode(PAUSED)
public class ShadowPausedAsyncTaskLoaderTest {
  private final List<String> taskRecord = new ArrayList<>();
  private TestLoader testLoader;
  private PausedExecutorService pausedBackgroundExecutor;

  @Before
  public void setUp() {
    pausedBackgroundExecutor = new PausedExecutorService();
    testLoader = new TestLoader(42);
    ShadowPausedAsyncTaskLoader shadowLoader = Shadow.extract(testLoader);
    shadowLoader.setExecutor(pausedBackgroundExecutor);
  }

  @Test
  public void forceLoad_shouldEnqueueWork() {
    testLoader.forceLoad();
    assertThat(taskRecord).isEmpty();

    pausedBackgroundExecutor.runAll();
    assertThat(taskRecord).containsExactly("loadInBackground");
    taskRecord.clear();

    shadowMainLooper().idle();
    assertThat(taskRecord).containsExactly("deliverResult 42");
  }

  @Test
  public void forceLoad_multipleLoads() {
    testLoader.forceLoad();
    assertThat(taskRecord).isEmpty();

    pausedBackgroundExecutor.runAll();
    assertThat(taskRecord).containsExactly("loadInBackground");
    taskRecord.clear();

    shadowMainLooper().idle();
    assertThat(taskRecord).containsExactly("deliverResult 42");

    testLoader.setData(43);
    taskRecord.clear();
    testLoader.forceLoad();

    pausedBackgroundExecutor.runAll();
    assertThat(taskRecord).containsExactly("loadInBackground");
    taskRecord.clear();

    shadowMainLooper().idle();
    assertThat(taskRecord).containsExactly("deliverResult 43");
  }

  class TestLoader extends AsyncTaskLoader<Integer> {
    private Integer data;

    public TestLoader(Integer data) {
      super(RuntimeEnvironment.application);
      this.data = data;
    }

    @Override
    public Integer loadInBackground() {
      taskRecord.add("loadInBackground");
      return data;
    }

    @Override
    public void deliverResult(Integer data) {
      taskRecord.add("deliverResult " + data.toString());
    }

    public void setData(int newData) {
      this.data = newData;
    }
  }
}
