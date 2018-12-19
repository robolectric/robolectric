package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.os.Build;
import android.os.Parcel;
import android.os.SharedMemory;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.nio.ByteBuffer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ShadowSharedMemory}. */
@RunWith(AndroidJUnit4.class)
public class ShadowSharedMemoryTest {

  @Test
  @Config(minSdk = Build.VERSION_CODES.O_MR1)
  public void getSize_shouldReturnSizeAtCreation() throws Exception {
    try (SharedMemory sharedMemory = SharedMemory.create("foo", 4)) {
      assertThat(sharedMemory.getSize()).isEqualTo(4);
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O_MR1)
  public void mapReadWrite_shouldReflectWrites() throws Exception {
    try (SharedMemory sharedMemory = SharedMemory.create("foo", 4)) {
      ByteBuffer fooBuf = sharedMemory.mapReadWrite();
      fooBuf.putInt(1234);
      fooBuf.flip();
      assertThat(fooBuf.getInt()).isEqualTo(1234);
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O_MR1)
  public void mapReadWrite_shouldReflectWritesAcrossMappings() throws Exception {
    try (SharedMemory sharedMemory = SharedMemory.create("foo", 4)) {
      ByteBuffer fooBuf = sharedMemory.mapReadWrite();
      ByteBuffer barBuf = sharedMemory.mapReadOnly();

      fooBuf.putInt(1234);
      assertThat(barBuf.getInt()).isEqualTo(1234);
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O_MR1)
  public void mapReadWrite_shouldPersistWritesAcrossUnmap() throws Exception {
    try (SharedMemory sharedMemory = SharedMemory.create("foo", 4)) {
      ByteBuffer fooBuf = sharedMemory.mapReadWrite();
      fooBuf.putInt(1234);
      SharedMemory.unmap(fooBuf);

      ByteBuffer barBuf = sharedMemory.mapReadOnly();
      assertThat(barBuf.getInt()).isEqualTo(1234);
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O_MR1)
  public void mapReadWrite_shouldThrowAfterClose() throws Exception {
    SharedMemory sharedMemory = SharedMemory.create("foo", 4);
    sharedMemory.close();
    // Uncomment when robolectric actually implements android.system.Os#close():
    // try {
    //   sharedMemory.mapReadWrite();
    //   fail();
    // } catch (IllegalStateException expected) {
    // }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O_MR1)
  public void create_shouldIgnoreDebugNameForIdentity() throws Exception {
    try (SharedMemory fooMem = SharedMemory.create("same-name", 4);
        SharedMemory barMem = SharedMemory.create("same-name", 4)) {
      ByteBuffer fooBuf = fooMem.mapReadWrite();
      ByteBuffer barBuf = barMem.mapReadWrite();

      fooBuf.putInt(1234);
      barBuf.putInt(5678);

      fooBuf.flip();
      assertThat(fooBuf.getInt()).isEqualTo(1234);
      barBuf.flip();
      assertThat(barBuf.getInt()).isEqualTo(5678);
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O_MR1)
  public void unmap_shouldRejectUnknownByteBuffer() {
    try {
      SharedMemory.unmap(ByteBuffer.allocate(4));
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O_MR1)
  public void unmap_shouldTolerateDoubleUnmap() throws Exception {
    try (SharedMemory sharedMemory = SharedMemory.create("foo", 4)) {
      ByteBuffer fooBuf = sharedMemory.mapReadWrite();
      fooBuf.putInt(1234);
      SharedMemory.unmap(fooBuf);
      SharedMemory.unmap(fooBuf);
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O_MR1)
  public void writeToParcel_shouldNotCrash() throws Exception {
    try (SharedMemory sharedMemory = SharedMemory.create("foo", 4)) {
      ByteBuffer fooBuf = sharedMemory.mapReadWrite();
      fooBuf.putInt(1234);

      Parcel parcel = Parcel.obtain();
      parcel.writeParcelable(sharedMemory, 0);
    }
  }
}
