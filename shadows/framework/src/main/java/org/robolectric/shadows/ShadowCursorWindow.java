package org.robolectric.shadows;

import android.database.CursorWindow;
import java.util.concurrent.atomic.AtomicBoolean;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/**
 * The base shadow class for {@link CursorWindow}.
 *
 * <p>The actual shadow class for {@link CursorWindow} will be selected during runtime by the
 * Picker.
 */
@Implements(value = CursorWindow.class, shadowPicker = ShadowCursorWindow.Picker.class)
public class ShadowCursorWindow {

  @ReflectorObject CursorWindowReflector cursorWindowReflector;

  private final AtomicBoolean disposed = new AtomicBoolean();

  @Implementation
  protected void dispose() {
    // On the JVM there may be two concurrent finalizer threads running if 'System.runFinalization'
    // is called. Because CursorWindow.dispose is not thread safe, we can work around it
    // by manually making it thread safe.
    if (disposed.compareAndSet(false, true)) {
      cursorWindowReflector.dispose();
    }
  }

  /** Shadow {@link Picker} for {@link ShadowCursorWindow} */
  public static class Picker extends SQLiteShadowPicker<ShadowCursorWindow> {
    public Picker() {
      super(ShadowLegacyCursorWindow.class, ShadowNativeCursorWindow.class);
    }
  }

  @ForType(CursorWindow.class)
  interface CursorWindowReflector {
    @Direct
    void dispose();
  }
}
