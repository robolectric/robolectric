package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.GraphicsMode.Mode;
import org.robolectric.config.ConfigurationRegistry;
import org.robolectric.shadow.api.ShadowPicker;

/** A {@link ShadowPicker} that selects between shadows given the Graphics mode. */
public class GraphicsShadowPicker<T> implements ShadowPicker<T> {

  private final Class<? extends T> legacyShadowClass;
  private final Class<? extends T> nativeShadowClass;

  public GraphicsShadowPicker(
      Class<? extends T> legacyShadowClass, Class<? extends T> nativeShadowClass) {
    this.legacyShadowClass = legacyShadowClass;
    this.nativeShadowClass = nativeShadowClass;
  }

  @Override
  public Class<? extends T> pickShadowClass() {
    if (RuntimeEnvironment.getApiLevel() >= O
        && ConfigurationRegistry.get(GraphicsMode.Mode.class) == Mode.NATIVE) {
      return nativeShadowClass;
    } else {
      return legacyShadowClass;
    }
  }
}
