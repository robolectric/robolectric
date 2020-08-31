package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;

import android.content.res.AssetManager;
import android.graphics.fonts.Font;
import com.google.common.base.Preconditions;
import java.nio.ByteBuffer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.res.android.ApkAssetsCookie;
import org.robolectric.res.android.Asset;
import org.robolectric.res.android.Asset.AccessMode;
import org.robolectric.res.android.CppAssetManager2;
import org.robolectric.res.android.Registries;

/** Shadow for {@link android.graphics.fonts.Font.Builder} */
@Implements(value = Font.Builder.class, minSdk = Q)
public class ShadowFontBuilder {

  // transliterated from frameworks/base/core/jni/android/graphics/fonts/Font.cpp

  @Implementation
  protected static long nGetNativeAsset(
      AssetManager assetMgr, String path, boolean isAsset, int cookie) {
    // NPE_CHECK_RETURN_ZERO(env, assetMgr);
    Preconditions.checkNotNull(assetMgr);
    // NPE_CHECK_RETURN_ZERO(env, path);
    Preconditions.checkNotNull(path);

    // Guarded<AssetManager2>* mgr = AssetManagerForJavaObject(env, assetMgr);
    CppAssetManager2 mgr = ShadowArscAssetManager10.AssetManagerForJavaObject(assetMgr);
    //if (mgr == nullptr) {
    if (mgr == null) {
      return 0;
    }

    // ScopedUtfChars str(env, path);
    // if (str.c_str() == nullptr) {
    //   return 0;
    // }

    // std::unique_ptr<Asset> asset;
    Asset asset;
    {
      // ScopedLock<AssetManager2> locked_mgr(*mgr);
      if (isAsset) {
        // asset = locked_mgr->Open(str.c_str(), Asset::ACCESS_BUFFER);
        asset = mgr.Open(path, AccessMode.ACCESS_BUFFER);
      } else if (cookie > 0) {
        // Valid java cookies are 1-based, but AssetManager cookies are 0-based.
        // asset = locked_mgr->OpenNonAsset(str.c_str(), static_cast<ApkAssetsCookie>(cookie - 1), Asset::ACCESS_BUFFER);
        asset = mgr.OpenNonAsset(path, ApkAssetsCookie.forInt(cookie - 1), AccessMode.ACCESS_BUFFER);
      } else {
        // asset = locked_mgr->OpenNonAsset(str.c_str(), Asset::ACCESS_BUFFER);
        asset = mgr.OpenNonAsset(path, AccessMode.ACCESS_BUFFER);
      }
    }

    // return reinterpret_cast<jlong>(asset.release());
    return Registries.NATIVE_ASSET_REGISTRY.register(asset);
  }

  @Implementation
  protected static ByteBuffer nGetAssetBuffer(long nativeAsset) {
    // Asset* asset = toAsset(nativeAsset);
    Asset asset = Registries.NATIVE_ASSET_REGISTRY.getNativeObject(nativeAsset);
    //return env->NewDirectByteBuffer(const_cast<void*>(asset->getBuffer(false)), asset->getLength());
    return ByteBuffer.wrap(asset.getBuffer(false));
  }

  @Implementation
  protected static long nGetReleaseNativeAssetFunc() {
    // return reinterpret_cast<jlong>(&releaseAsset);
    // TODO: implement
    return 0;
  }
}
