package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.content.res.AssetManager.AssetInputStream;
import java.io.InputStream;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.android.Asset;
import org.robolectric.res.android.Registries;
import org.robolectric.shadows.ShadowAssetInputStream.Picker;
import org.robolectric.util.ReflectionHelpers;

@SuppressWarnings("UnusedDeclaration")
@Implements(value = AssetInputStream.class, shadowPicker = Picker.class)
public class ShadowArscAssetInputStream extends ShadowAssetInputStream {

  @RealObject
  private AssetInputStream realObject;

  @Override
  InputStream getDelegate() {
    return realObject;
  }

  private Asset getAsset() {
    int apiLevel = RuntimeEnvironment.getApiLevel();
    long assetPtr;
    if (apiLevel >= LOLLIPOP) {
      assetPtr = ReflectionHelpers.callInstanceMethod(realObject, "getNativeAsset");
    } else {
      assetPtr =
          ((Integer) ReflectionHelpers.callInstanceMethod(realObject, "getAssetInt")).longValue();
    }
    return Registries.NATIVE_ASSET_REGISTRY.getNativeObject(assetPtr);
  }

  @Override
  boolean isNinePatch() {
    Asset asset = getAsset();
    return asset != null && asset.isNinePatch();
  }
}
