package org.robolectric.shadows;

import android.media.SoundPool;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

import android.media.AudioAttributes;

import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.internal.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

@Implements(SoundPool.class)
public class ShadowSoundPool {
  @RealObject
  SoundPool realObject;

  @Implementation(minSdk = M)
  public void __constructor__(int maxStreams, AudioAttributes attributes) {
    if (getApiLevel() >= M) {
      ReflectionHelpers.setField(realObject, "mLock", new Object());
    } else {
      invokeConstructor(SoundPool.class, realObject,
          from(int.class, maxStreams),
          from(AudioAttributes.class, attributes));
    }
  }
}
