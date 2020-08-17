package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.RuntimeEnvironment.getApiLevel;

import android.hardware.HardwareBuffer;
import android.os.Parcel;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.res.android.NativeObjRegistry;

/** Shadow of android.hardware.HardwareBuffer. */
@Implements(HardwareBuffer.class)
public class ShadowHardwareBuffer {
  private static final long INVALID_BUFFER_ID = 0;

  private static final long USAGE_FLAGS_O =
      HardwareBuffer.USAGE_CPU_READ_RARELY
          | HardwareBuffer.USAGE_CPU_READ_OFTEN
          | HardwareBuffer.USAGE_CPU_WRITE_RARELY
          | HardwareBuffer.USAGE_CPU_WRITE_OFTEN
          | HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE
          | HardwareBuffer.USAGE_GPU_COLOR_OUTPUT
          | HardwareBuffer.USAGE_PROTECTED_CONTENT
          | HardwareBuffer.USAGE_VIDEO_ENCODE
          | HardwareBuffer.USAGE_GPU_DATA_BUFFER
          | HardwareBuffer.USAGE_SENSOR_DIRECT_DATA;

  private static final long USAGE_FLAGS_P =
      HardwareBuffer.USAGE_GPU_CUBE_MAP | HardwareBuffer.USAGE_GPU_MIPMAP_COMPLETE;

  private static final long VALID_USAGE_FLAGS;

  private static class BufferState {
    public int width;
    public int height;
    public int layers;
    public int format;
    public long usage;
  }

  private static final NativeObjRegistry<BufferState> BUFFER_STATE_REGISTRY =
      new NativeObjRegistry<>(BufferState.class);

  static {
    long usageFlags = 0;

    if (getApiLevel() >= O) {
      usageFlags |= USAGE_FLAGS_O;
    }

    if (getApiLevel() >= P) {
      usageFlags |= USAGE_FLAGS_P;
    }

    VALID_USAGE_FLAGS = usageFlags;
  }

  @Implementation(minSdk = O)
  protected static long nCreateHardwareBuffer(
      int width, int height, int format, int layers, long usage) {
    if ((usage & ~VALID_USAGE_FLAGS) != 0) {
      return INVALID_BUFFER_ID;
    }

    BufferState bufferState = new BufferState();
    bufferState.width = width;
    bufferState.height = height;
    bufferState.format = format;
    bufferState.layers = layers;
    bufferState.usage = usage;
    return BUFFER_STATE_REGISTRY.register(bufferState);
  }

  @Implementation(minSdk = O)
  protected static void nWriteHardwareBufferToParcel(long nativeObject, Parcel dest) {
    BufferState bufferState = BUFFER_STATE_REGISTRY.getNativeObject(nativeObject);
    dest.writeInt(bufferState.width);
    dest.writeInt(bufferState.height);
    dest.writeInt(bufferState.format);
    dest.writeInt(bufferState.layers);
    dest.writeLong(bufferState.usage);
  }

  @Implementation(minSdk = O)
  protected static long nReadHardwareBufferFromParcel(Parcel in) {
    int width = in.readInt();
    int height = in.readInt();
    int format = in.readInt();
    int layers = in.readInt();
    long usage = in.readLong();
    return nCreateHardwareBuffer(width, height, format, layers, usage);
  }

  @Implementation(minSdk = O)
  protected static int nGetWidth(long nativeObject) {
    return BUFFER_STATE_REGISTRY.getNativeObject(nativeObject).width;
  }

  @Implementation(minSdk = O)
  protected static int nGetHeight(long nativeObject) {
    return BUFFER_STATE_REGISTRY.getNativeObject(nativeObject).height;
  }

  @Implementation(minSdk = O)
  protected static int nGetFormat(long nativeObject) {
    return BUFFER_STATE_REGISTRY.getNativeObject(nativeObject).format;
  }

  @Implementation(minSdk = O)
  protected static int nGetLayers(long nativeObject) {
    return BUFFER_STATE_REGISTRY.getNativeObject(nativeObject).layers;
  }

  @Implementation(minSdk = O)
  protected static long nGetUsage(long nativeObject) {
    return BUFFER_STATE_REGISTRY.getNativeObject(nativeObject).usage;
  }
}
