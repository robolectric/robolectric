package org.robolectric.shadows;

    import static org.assertj.core.api.Assertions.assertThat;

    import android.os.Build;
    import android.os.SharedMemory;
    import org.junit.Test;
    import org.junit.runner.RunWith;
    import org.robolectric.RobolectricTestRunner;
    import org.robolectric.annotation.Config;

/**
 * Unit tests for {@link ShadowSharedMemory}.
 */
@RunWith(RobolectricTestRunner.class)
public class ShadowSharedMemoryTest {

  @Test
  @Config(minSdk = Build.VERSION_CODES.O_MR1)
  public void create() throws Exception {
    SharedMemory sharedMemory = SharedMemory.create("foo", 4);
    assertThat(sharedMemory).isNotNull();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O_MR1)
  public void size() throws Exception {
    SharedMemory sharedMemory = SharedMemory.create("foo", 4);
    assertThat(sharedMemory.getSize()).isEqualTo(4);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O_MR1)
  public void mapReadWrite() throws Exception {
    SharedMemory sharedMemory = SharedMemory.create("foo", 4);
    assertThat(sharedMemory.mapReadWrite()).isNotNull();
  }
}

