package org.robolectric.shadows;

import android.content.Context;
import android.media.Ringtone;
import androidx.test.core.app.ApplicationProvider;
import java.util.concurrent.atomic.AtomicInteger;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Shadow for {@link Ringtone}. */
@Implements(Ringtone.class)
public class ShadowRingtone {
  /** Represents how many times {@link Ringtone#play()} is called. */
  private final AtomicInteger playCount = new AtomicInteger(0);

  /**
   * Creates an instance of {@link Ringtone} using reflection, and a {@link Context} object provided
   * by the caller.
   */
  public static Ringtone create() throws Exception {
    return ReflectionHelpers.callConstructor(
        Ringtone.class,
        /* context */
        ClassParameter.from(Context.class, ApplicationProvider.getApplicationContext()),
        /* allowRemote */
        ClassParameter.from(boolean.class, true));
  }

  @Implementation
  protected void play() {
    playCount.getAndIncrement();
  }

  /** Returns how many times {@link #play()} was called. */
  public int getPlayCount() {
    return playCount.get();
  }
}
