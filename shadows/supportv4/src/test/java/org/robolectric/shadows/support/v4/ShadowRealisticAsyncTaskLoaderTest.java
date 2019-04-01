package org.robolectric.shadows.support.v4;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;
import static org.robolectric.shadows.ShadowBaseLooper.shadowMainLooper;

import android.support.v4.content.AsyncTaskLoader;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.util.TestRunnerWithManifest;

@RunWith(TestRunnerWithManifest.class)
@LooperMode(PAUSED)
// TODO DO NOT SUBMIT
@Config(sdk = 28)
public class ShadowRealisticAsyncTaskLoaderTest {
  private final List<String> taskRecord = new ArrayList<>();

  @Test
  public void forceLoad_shouldEnqueueWorkOnSchedulers() {
    new TestLoader(42).forceLoad();
    assertThat(taskRecord).isEmpty();

    // Robolectric.flushBackgroundThreadScheduler();
    assertThat(taskRecord).containsExactly("loadInBackground");
    taskRecord.clear();

    shadowMainLooper().idle();
    assertThat(taskRecord).containsExactly("deliverResult 42");
  }

  @Test
  public void forceLoad_multipleLoads() {
    TestLoader testLoader = new TestLoader(42);
    testLoader.forceLoad();
    assertThat(taskRecord).isEmpty();

    // Robolectric.flushBackgroundThreadScheduler();
    assertThat(taskRecord).containsExactly("loadInBackground");
    taskRecord.clear();

    shadowMainLooper().idle();
    assertThat(taskRecord).containsExactly("deliverResult 42");

    testLoader.setData(43);
    taskRecord.clear();
    testLoader.forceLoad();

    // Robolectric.flushBackgroundThreadScheduler();
    assertThat(taskRecord).containsExactly("loadInBackground");
    taskRecord.clear();

    shadowMainLooper().idle();
    assertThat(taskRecord).containsExactly("deliverResult 43");
  }

  public class TestLoader extends AsyncTaskLoader<Integer> {
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
