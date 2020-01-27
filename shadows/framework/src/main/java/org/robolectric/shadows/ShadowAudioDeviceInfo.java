package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;

import android.media.AudioDeviceInfo;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

/** Shadow of {@link AudioDeviceInfo}. */
@Implements(value = AudioDeviceInfo.class, minSdk = M)
public class ShadowAudioDeviceInfo {

  private int type = AudioDeviceInfo.TYPE_UNKNOWN;
  private boolean isSink = false;
  private String address = "";

  /**
   * Creates new instance of {@link AudioDeviceInfo}, because its constructor is hidden.
   *
   * @return The Network instance.
   */
  public static AudioDeviceInfo newInstance() {
    return Shadow.newInstanceOf(AudioDeviceInfo.class);
  }

  @Implementation
  public boolean isSource() {
    return !isSink;
  }

  @Implementation
  public int getType() {
    return type;
  }

  @Implementation
  public boolean isSink() {
    return isSink;
  }

  @Implementation
  public String getAddress() {
    return address;
  }

  public void setIsSink(boolean isSink) {
    this.isSink = isSink;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public void setType(int type) {
    this.type = type;
  }
}
