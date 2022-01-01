package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;

import android.content.res.AssetManager;
import android.graphics.fonts.Font;
import android.graphics.fonts.FontStyle;
import androidx.annotation.RequiresApi;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.res.android.ApkAssetsCookie;
import org.robolectric.res.android.Asset;
import org.robolectric.res.android.Asset.AccessMode;
import org.robolectric.res.android.CppAssetManager2;
import org.robolectric.res.android.Registries;

/** Shadow for {@link android.graphics.fonts.Font.Builder} */
@Implements(value = Font.Builder.class, minSdk = Q)
@RequiresApi(api = Q)
public class ShadowFontBuilder {
  private static final Map<Long, FontInternal> resettableByteBuffers = new HashMap<>();

  @Resetter
  public static void reset() {
    for (Map.Entry<Long, FontInternal> entry : resettableByteBuffers.entrySet()) {
      FontInternal fontInternal = entry.getValue();
      if (fontInternal == null || fontInternal.buffer == null) {
        continue;
      }
      ByteBuffer buffer = fontInternal.buffer;
      buffer.rewind();
      buffer.clear();
    }
    resettableByteBuffers.clear();
  }

  static ByteBuffer getBuffer(long ptr) {
    FontInternal fontInternal = resettableByteBuffers.get(ptr);
    if (fontInternal == null) {
      return ByteBuffer.allocate(0);
    }
    return fontInternal.buffer;
  }

  static int getPackedStyle(long ptr) {
    FontInternal fontInternal = resettableByteBuffers.get(ptr);
    if (fontInternal == null) {
      return FontStyle.FONT_WEIGHT_NORMAL;
    }
    return pack(fontInternal.weight, fontInternal.italic);
  }

  private static int pack(int weight, boolean italic) {
    // See android.graphics.fonts.FontUtils#pack
    return weight | (italic ? 0x10000 : 0);
  }

  // transliterated from frameworks/base/core/jni/android/graphics/fonts/Font.cpp

  @Implementation(maxSdk = Q)
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

  @Implementation(maxSdk = Q)
  protected static ByteBuffer nGetAssetBuffer(long nativeAsset) {
    // Asset* asset = toAsset(nativeAsset);
    Asset asset = Registries.NATIVE_ASSET_REGISTRY.getNativeObject(nativeAsset);
    //return env->NewDirectByteBuffer(const_cast<void*>(asset->getBuffer(false)), asset->getLength());
    return ByteBuffer.wrap(asset.getBuffer(false));
  }

  @Implementation(maxSdk = Q)
  protected static long nGetReleaseNativeAssetFunc() {
    // return reinterpret_cast<jlong>(&releaseAsset);
    // TODO: implement
    return 0;
  }

  /** Re-implement to avoid call to DirectByteBuffer#array, which is not supported on JDK */
  @Implementation(minSdk = R)
  protected static ByteBuffer createBuffer(
      AssetManager am, String path, boolean isAsset, int cookie) throws IOException {
    Preconditions.checkNotNull(am, "assetManager can not be null");
    Preconditions.checkNotNull(path, "path can not be null");

    try (InputStream assetStream =
        isAsset
            ? am.open(path, AssetManager.ACCESS_BUFFER)
            : am.openNonAsset(cookie, path, AssetManager.ACCESS_BUFFER)) {

      int capacity = assetStream.available();
      ByteBuffer buffer = ByteBuffer.allocate(capacity);
      buffer.order(ByteOrder.nativeOrder());
      assetStream.read(buffer.array(), buffer.arrayOffset(), assetStream.available());

      if (assetStream.read() != -1) {
        throw new IOException("Unable to access full contents of " + path);
      }

      return buffer;
    }
  }

  @Implementation(minSdk = S)
  protected static long nBuild(
      long builderPtr,
      ByteBuffer buffer,
      String filePath,
      String localeList,
      int weight,
      boolean italic,
      int ttcIndex) {
    Preconditions.checkNotNull(buffer);
    Preconditions.checkNotNull(filePath);
    Preconditions.checkNotNull(localeList);

    buffer.rewind();
    // If users use one ttf file for different style, for example one ttf for bold and normal,
    // the buffer's hasCode is the same. We can use packed style as addition to calculate identical
    // pointer address for native object.
    long ptr = ((long) buffer.hashCode()) * 32 + pack(weight, italic);
    FontInternal fontInternal = new FontInternal();
    fontInternal.buffer = buffer;
    fontInternal.filePath = filePath;
    fontInternal.localeList = localeList;
    fontInternal.weight = weight;
    fontInternal.italic = italic;
    fontInternal.ttcIndex = ttcIndex;
    resettableByteBuffers.put(ptr, fontInternal);
    return ptr;
  }

  private static class FontInternal {
    ByteBuffer buffer;
    String filePath;
    String localeList;
    int weight;
    boolean italic;
    int ttcIndex;
  }
}
