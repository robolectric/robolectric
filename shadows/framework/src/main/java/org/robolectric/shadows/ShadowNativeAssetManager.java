package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.res.AssetManager;
import android.content.res.AssetManager.AssetInputStream;
import android.content.res.XmlBlock;
import android.util.TypedValue;
import dalvik.system.VMRuntime;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.Direct.DirectFormat;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@Implements(
    value = AssetManager.class,
    minSdk = VANILLA_ICE_CREAM,
    callNativeMethodsByDefault = true,
    shadowPicker = ShadowAssetManager.Picker.class)
public class ShadowNativeAssetManager extends ShadowAssetManager {

  @ReflectorObject private AssetManagerReflector assetManagerReflector;

  @Override
  Collection<Path> getAllAssetDirs() {
    throw new UnsupportedOperationException();
  }

  @Override
  long getNativePtr() {
    throw new UnsupportedOperationException();
  }

  @Implementation
  protected InputStream open(String fileName, int accessMode) {
    // intercept real method to handle nine patch for LEGACY graphics mode
    InputStream is = assetManagerReflector.open(fileName, accessMode);
    setNinePatch(fileName, is);
    return is;
  }

  private static void setNinePatch(String fileName, InputStream is) {
    if (is instanceof AssetInputStream) {
      ShadowNativeAssetInputStream snais = Shadow.extract(is);
      // this is a dubious assumption, but this is the logic with BINARY resources mode:
      // assume nine patch based on file name
      if (fileName != null && fileName.toLowerCase(Locale.ENGLISH).endsWith(".9.png")) {
        snais.setNinePatch(true);
      }
    }
  }

  @Implementation
  protected InputStream openNonAsset(int cookie, String fileName, int accessMode) {
    // intercept real method to handle nine patch for LEGACY graphics mode
    InputStream is = assetManagerReflector.openNonAsset(cookie, fileName, accessMode);
    setNinePatch(fileName, is);
    return is;
  }

  /**
   * Use a similar implementation as applyStyle$ravenwood as workaround for allocating pinned
   * (non-movable) array objects.
   */
  @Implementation
  protected void applyStyle(
      long themePtr,
      int defStyleAttr,
      int defStyleRes,
      XmlBlock.Parser parser,
      int[] inAttrs,
      long outValuesAddress,
      long outIndicesAddress) {
    Objects.requireNonNull(inAttrs, "inAttrs");
    PerfStatsCollector.getInstance()
        .measure(
            "native applyStyle",
            () -> {
              ShadowVMRuntime shadowVmRuntime = Shadow.extract(VMRuntime.getRuntime());
              int[] outValues = (int[]) shadowVmRuntime.getObjectForAddress(outValuesAddress);
              int[] outIndices = (int[]) shadowVmRuntime.getObjectForAddress(outIndicesAddress);
              synchronized (this) {
                // Need to synchronize on AssetManager because we will be accessing
                // the native implementation of AssetManager.
                assetManagerReflector.ensureValidLocked();
                long xmlParserPtr =
                    parser != null
                        ? reflector(XmlBlockParserReflector.class, parser).getParseState()
                        : 0;

                reflector(AssetManagerDirectReflector.class)
                    .nativeApplyStyleWithArray(
                        assetManagerReflector.getObject(),
                        themePtr,
                        defStyleAttr,
                        defStyleRes,
                        xmlParserPtr,
                        inAttrs,
                        outValues,
                        outIndices);
              }
            });
  }

  @Implementation
  protected static int nativeGetResourceValue(
      long ptr, int resid, short density, TypedValue typed_value, boolean resolve_references) {
    return PerfStatsCollector.getInstance()
        .measure(
            "native nativeGetResourceValue",
            () ->
                reflector(AssetManagerDirectReflector.class)
                    .nativeGetResourceValue(ptr, resid, density, typed_value, resolve_references));
  }

  @ForType(XmlBlock.Parser.class)
  interface XmlBlockParserReflector {
    @Accessor("mParseState")
    long getParseState();
  }

  @ForType(AssetManager.class)
  interface AssetManagerDirectReflector {
    @Direct(format = DirectFormat.NATIVE)
    @Static
    int nativeGetResourceValue(
        long ptr, int resid, short density, TypedValue typedValue, boolean resolveReferences);

    @Direct(format = DirectFormat.NATIVE)
    @Static
    void nativeApplyStyleWithArray(
        long ptr,
        long themePtr,
        int defStyleAttr,
        int defStyleRes,
        long xmlParserPtr,
        int[] inAttrs,
        int[] outValues,
        int[] outIndices);
  }
}
