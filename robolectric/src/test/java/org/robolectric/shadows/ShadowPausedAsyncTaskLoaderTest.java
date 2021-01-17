package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static com.google.common.truth.Truth.assertThat;

import android.content.AsyncTaskLoader;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.android.util.concurrent.PausedExecutorService;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Tests for {@link ShadowPausedAsyncTaskLoader}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = KITKAT)
public class ShadowPausedAsyncTaskLoaderTest {
  private final List<String> taskRecord = new ArrayList<>();
  private TestLoader testLoader;
  private PausedExecutorService pausedBackgroundExecutor;

  @Before
  public void setUp() {
    pausedBackgroundExecutor = new PausedExecutorService();
    testLoader = new TestLoader(42);
    ShadowPausedAsyncTaskLoader<Integer> shadowLoader = Shadow.extract(testLoader);
    shadowLoader.setExecutor(pausedBackgroundExecutor);
  }

  @Test
  public void forceLoad_enqueuesWork() {
    testLoader.forceLoad();
    assertThat(taskRecord).isEmpty();

    pausedBackgroundExecutor.runAll();
    assertThat(taskRecord).containsExactly("loadInBackground");
    taskRecord.clear();

    ShadowLooper.idleMainLooper();
    assertThat(taskRecord).containsExactly("deliverResult 42");
  }

  @Test
  public void forceLoad_multipleLoads() {
    testLoader.forceLoad();
    assertThat(taskRecord).isEmpty();

    pausedBackgroundExecutor.runAll();
    assertThat(taskRecord).containsExactly("loadInBackground");
    taskRecord.clear();

    ShadowLooper.idleMainLooper();
    assertThat(taskRecord).containsExactly("deliverResult 42");

    testLoader.setData(43);
    taskRecord.clear();
    testLoader.forceLoad();

    pausedBackgroundExecutor.runAll();
    assertThat(taskRecord).containsExactly("loadInBackground");
    taskRecord.clear();

    ShadowLooper.idleMainLooper();
    assertThat(taskRecord).containsExactly("deliverResult 43");
  }

  class TestLoader extends AsyncTaskLoader<Integer> {
    private Integer data;

    public TestLoader(Integer data) {
      super(ApplicationProvider.getApplicationContext());
      this.data = data;
    }

    @Override
    public Integer loadInBackground() {
      taskRecord.add("loadInBackground");
      return data;
    }

    @Override
    public void deliverResult(Integer data) {
      taskRecord.add("deliverResult " + data);
    }

    public void setData(int newData) {
      this.data = newData;
    }
  }
}
