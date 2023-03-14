package org.robolectric.shadows;

import android.annotation.RequiresPermission;
import android.hardware.soundtrigger.SoundTrigger;
import android.os.Build.VERSION_CODES;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** A Shadow SoundTriggerManager in Android O+. */
@Implements(
    className = "android.media.soundtrigger.SoundTriggerManager",
    minSdk = VERSION_CODES.N,
    isInAndroidSdk = false)
public final class ShadowSoundTriggerManager {
  private SoundTrigger.ModuleProperties moduleProperties = null;

  /**
   * Set {@code SoundTrigger.ModuleProperties}, value will returned for the following {@code
   * getModuleProperties} call.
   */
  public void setModuleProperties(@Nullable SoundTrigger.ModuleProperties moduleProperties) {
    this.moduleProperties = moduleProperties;
  }

  @RequiresPermission(android.Manifest.permission.MANAGE_SOUND_TRIGGER)
  @Implementation(minSdk = VERSION_CODES.R)
  protected SoundTrigger.ModuleProperties getModuleProperties() {
    if (RuntimeEnvironment.getApiLevel() == VERSION_CODES.R && moduleProperties == null) {
      throw new NullPointerException(
          "Throw NullPointException in Android R when moduleProperties is null.");
    }
    return moduleProperties;
  }
}
