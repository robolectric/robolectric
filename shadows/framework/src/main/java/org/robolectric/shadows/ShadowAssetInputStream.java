package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.shadows.ShadowAssetManager.useLegacy;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.content.res.AssetManager;
import android.content.res.AssetManager.AssetInputStream;
import android.os.Build;
import java.io.IOException;
import java.io.InputStream;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.android.Asset;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadow.api.ShadowPicker;

@SuppressWarnings("UnusedDeclaration")
abstract public class ShadowAssetInputStream {

  public static class Picker extends ResourceModeShadowPicker<ShadowAssetInputStream> {

    public Picker() {
      super(ShadowLegacyAssetInputStream.class, ShadowArscAssetInputStream.class,
          ShadowArscAssetInputStream.class);
    }
  }

  abstract InputStream getDelegate();

  abstract boolean isNinePatch();

}
