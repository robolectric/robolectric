package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.res.AssetManager.AssetInputStream;
import java.io.InputStream;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.android.Asset;
import org.robolectric.res.android.Registries;
import org.robolectric.shadows.ShadowAssetInputStream.Picker;
import org.robolectric.util.reflector.ForType;

@SuppressWarnings("UnusedDeclaration")
@Implements(value = AssetInputStream.class, shadowPicker = Picker.class)
public class ShadowArscAssetInputStream extends ShadowAssetInputStream {

  @RealObject private AssetInputStream realObject;

  @Override
  InputStream getDelegate() {
    return realObject;
  }

  private Asset getAsset() {
    long assetPtr = reflector(_AssetInputStream_.class, realObject).getNativeAsset();
    return Registries.NATIVE_ASSET_REGISTRY.getNativeObject(assetPtr);
  }

  @Override
  boolean isNinePatch() {
    Asset asset = getAsset();
    return asset != null && asset.isNinePatch();
  }

  /** Accessor interface for {@link AssetInputStream}'s internals. */
  @ForType(AssetInputStream.class)
  private interface _AssetInputStream_ {
    int getAssetInt();

    long getNativeAsset();
  }
}
