package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.view.InsetsSource;
import android.view.InsetsState;
import android.view.WindowInsets;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow of {@link InsetsState}. */
@Implements(value = InsetsState.class, minSdk = Q, isInAndroidSdk = false)
public class ShadowInsetsState {
  // These must align with the indexes declared in InsetsState in SDK up to 33
  static final int STATUS_BARS = 0;
  static final int NAVIGATION_BARS = 1;

  @RealObject private InsetsState realInsetsState;
  @ReflectorObject private InsetsStateReflector insetsStateReflector;

  InsetsSource getOrCreateSource(int id) {
    return RuntimeEnvironment.getApiLevel() < UPSIDE_DOWN_CAKE
        ? insetsStateReflector.getSource(id)
        : realInsetsState.getOrCreateSource(id, getType(id));
  }

  int getSourceSize() {
    if (RuntimeEnvironment.getApiLevel() >= UPSIDE_DOWN_CAKE) {
      return realInsetsState.sourceSize();
    } else if (RuntimeEnvironment.getApiLevel() >= R) {
      return reflector(InsetsStateReflector.class).getLastType() + 1;
    } else {
      return insetsStateReflector.getSourcesCount();
    }
  }

  private static int getType(int id) {
    switch (id) {
      case STATUS_BARS:
        return RuntimeEnvironment.getApiLevel() < Q
            ? reflector(WindowInsetsTypeReflector.class).topBar()
            : WindowInsets.Type.statusBars();
      case NAVIGATION_BARS:
        return RuntimeEnvironment.getApiLevel() < Q
            ? reflector(WindowInsetsTypeReflector.class).sideBars()
            : WindowInsets.Type.navigationBars();
      default:
        throw new IllegalArgumentException();
    }
  }

  @ForType(InsetsState.class)
  interface InsetsStateReflector {
    InsetsSource getSource(int type);

    @Accessor("ITYPE_IME")
    @Static
    int getImeType();

    @Accessor("LAST_TYPE")
    @Static
    int getLastType();

    int getSourcesCount();
  }

  @ForType(WindowInsets.Type.class)
  interface WindowInsetsTypeReflector {
    @Static
    int topBar();

    @Static
    int sideBars();
  }
}
