package org.robolectric.shadows;

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
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.versioning.AndroidVersions.V;

@Implements(
    value = AssetManager.class,
    minSdk = V.SDK_INT,
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
                ReflectionHelpers.callStaticMethod(
                    AssetManager.class,
                    Shadow.directNativeMethodName(
                        AssetManager.class.getName(), "nativeApplyStyleWithArray"),
                    ClassParameter.from(long.class, assetManagerReflector.getObject()),
                    ClassParameter.from(long.class, themePtr),
                    ClassParameter.from(int.class, defStyleAttr),
                    ClassParameter.from(int.class, defStyleRes),
                    ClassParameter.from(long.class, xmlParserPtr),
                    ClassParameter.from(int[].class, inAttrs),
                    ClassParameter.from(int[].class, outValues),
                    ClassParameter.from(int[].class, outIndices));
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
                ReflectionHelpers.callStaticMethod(
                    AssetManager.class,
                    Shadow.directNativeMethodName(
                        AssetManager.class.getName(), "nativeGetResourceValue"),
                    ClassParameter.from(long.class, ptr),
                    ClassParameter.from(int.class, resid),
                    ClassParameter.from(short.class, density),
                    ClassParameter.from(TypedValue.class, typed_value),
                    ClassParameter.from(boolean.class, resolve_references)));
  }

  @ForType(AssetManager.class)
  interface AssetManagerReflector {
    @Accessor("mObject")
    long getObject();

    void ensureValidLocked();

    @Direct
    InputStream open(String fileName, int accessMode);

    @Direct
    InputStream openNonAsset(int cookie, String fileName, int accessMode);
  }

  @ForType(XmlBlock.Parser.class)
  interface XmlBlockParserReflector {
    @Accessor("mParseState")
    long getParseState();
  }
}
