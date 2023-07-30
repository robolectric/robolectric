package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.media.AudioDeviceInfo;
import android.os.Build.VERSION_CODES;
import android.util.SparseIntArray;
import androidx.annotation.RequiresApi;
import java.util.Optional;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Builder for {@link AudioDeviceInfo}. */
@RequiresApi(VERSION_CODES.M)
public class AudioDeviceInfoBuilder {

  private int type;

  private AudioDeviceInfoBuilder() {}

  public static AudioDeviceInfoBuilder newBuilder() {
    return new AudioDeviceInfoBuilder();
  }

  /**
   * Sets the device type.
   *
   * @param type The device type. The possible values are the constants defined as <a
   *     href="https://cs.android.com/android/platform/superproject/+/master:frameworks/base/media/java/android/media/AudioDeviceInfo.java?q=AudioDeviceType">AudioDeviceInfo.AudioDeviceType</a>
   */
  public AudioDeviceInfoBuilder setType(int type) {
    this.type = type;
    return this;
  }

  public AudioDeviceInfo build() {
    Object port = Shadow.newInstanceOf("android.media.AudioDevicePort");
    ReflectionHelpers.setField(port, "mType", externalToInternalType(type));

    return ReflectionHelpers.callConstructor(
        AudioDeviceInfo.class, ClassParameter.from(port.getClass(), port));
  }

  /** Accessor interface for {@link AudioDeviceInfo}'s internals. */
  @ForType(AudioDeviceInfo.class)
  interface AudioDeviceInfoReflector {

    @Static
    @Accessor("EXT_TO_INT_DEVICE_MAPPING")
    SparseIntArray getExtToIntDeviceMapping();
  }

  private static int externalToInternalType(int externalType) {
    return Optional.ofNullable(
            reflector(AudioDeviceInfoReflector.class).getExtToIntDeviceMapping().get(externalType))
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "External type "
                        + externalType
                        + " does not have a mapping to an internal type defined."));
  }
}
