package org.robolectric.shadows;

import android.media.AudioDeviceInfo;
import android.os.Build.VERSION_CODES;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Base class for {@link AudioDeviceInfo} shadows. */
@Implements(value = AudioDeviceInfo.class, minSdk = VERSION_CODES.M)
public final class ShadowAudioDeviceInfo {
  private int type;
  private String address;
  private CharSequence productName;
  private Role role = Role.UNKNOWN;

  /** Overrides the default implementation to always return zero. */
  @Implementation
  public int getId() {
    return 0;
  }

  public void setType(int type) {
    this.type = type;
  }

  /**
   * @return the type set by {@link #setType(int)}.
   */
  @Implementation
  public int getType() {
    return type;
  }

  public void setProductName(CharSequence productName) {
    this.productName = productName;
  }

  /**
   * @return the product name set by {@link #setProductName(CharSequence)}.
   */
  @Implementation
  public CharSequence getProductName() {
    return productName;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * @return the String set by {@link #setAddress(String)}.
   */
  @Implementation
  public String getAddress() {
    return address;
  }

  /**
   * Marks this AudioDevice as a source device. Consequentially, makes {@link #isSink()} return
   * false.
   */
  public void setIsSource() {
    role = Role.SOURCE;
  }

  /**
   * Marks this AudioDevice as a sink device. Consequentially, makes {@link #isSource()} return
   * false.
   */
  public void setIsSink() {
    role = Role.SINK;
  }

  /**
   * @return true when {@link #setIsSource()} was called.
   */
  @Implementation
  public boolean isSource() {
    return role.equals(Role.SOURCE);
  }

  /**
   * @return true when {@link #setIsSink()} was called.
   */
  @Implementation
  public boolean isSink() {
    return role.equals(Role.SINK);
  }

  enum Role {
    SOURCE,
    SINK,
    UNKNOWN
  }
}
