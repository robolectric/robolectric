package org.robolectric.shadows;

import static org.robolectric.shadows.ShadowArscAssetManager9.NATIVE_ASSET_REGISTRY;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.content.res.AssetManager;
import android.content.res.AssetManager.AssetInputStream;
import java.io.InputStream;
import org.robolectric.res.android.Asset;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

@SuppressWarnings("UnusedDeclaration")
public abstract class ShadowAssetInputStream {

  static AssetInputStream createAssetInputStream(InputStream delegateInputStream, Asset asset,
      AssetManager assetManager) {
    long nativeObjectId = NATIVE_ASSET_REGISTRY.getNativeObjectId(asset);

    AssetInputStream ais = ReflectionHelpers.callConstructor(AssetInputStream.class,
        from(AssetManager.class, assetManager),
        from(long.class, nativeObjectId));

    ShadowAssetInputStream sais = Shadow.extract(ais);
    if (sais instanceof ShadowLegacyAssetInputStream) {
      ShadowLegacyAssetInputStream slais = (ShadowLegacyAssetInputStream) sais;
      slais.setDelegate(delegateInputStream);
      slais.setNinePatch(asset.isNinePatch());
    }
    return ais;
  }

  public static class Picker extends ResourceModeShadowPicker<ShadowAssetInputStream> {

    public Picker() {
      super(ShadowLegacyAssetInputStream.class, ShadowArscAssetInputStream.class,
          ShadowArscAssetInputStream.class);
    }
  }

  abstract InputStream getDelegate();

  abstract boolean isNinePatch();

}
