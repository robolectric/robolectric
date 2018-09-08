package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.content.res.AssetManager;
import android.content.res.AssetManager.AssetInputStream;
import java.io.InputStream;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.android.Asset;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowAssetInputStream.Picker;

@SuppressWarnings("UnusedDeclaration")
@Implements(value = AssetInputStream.class, shadowPicker = Picker.class)
public class ShadowArscAssetInputStream extends ShadowAssetInputStream {

  @RealObject
  private AssetInputStream realObject;

  private Asset asset;

  @Implementation(minSdk = P)
  protected void __constructor__(AssetManager assetManager, long assetNativePtr) {
    Shadow.invokeConstructor(AssetInputStream.class, realObject,
        from(AssetManager.class, assetManager),
        from(long.class, assetNativePtr));

    this.asset = ShadowArscAssetManager9.NATIVE_ASSET_REGISTRY.getNativeObject(assetNativePtr);
  }

  @Override
  InputStream getDelegate() {
    return realObject;
  }

  @Override
  boolean isNinePatch() {
    return asset != null && asset.isNinePatch();
  }
}
