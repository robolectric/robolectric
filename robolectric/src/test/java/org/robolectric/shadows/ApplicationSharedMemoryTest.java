package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.android.internal.os.ApplicationSharedMemory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(minSdk = BAKLAVA)
@RunWith(AndroidJUnit4.class)
public final class ApplicationSharedMemoryTest {
  @Test
  public void getInstance_doesNotThrow() {
    assertThat(ApplicationSharedMemory.getInstance()).isNotNull();
    assertThat(ApplicationSharedMemory.getInstance().isMapped()).isTrue();
  }
}
