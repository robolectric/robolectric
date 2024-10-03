package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.res.AssetManager;
import android.content.res.XmlBlock;
import dalvik.system.VMRuntime;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
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
    ShadowVMRuntime shadowVmRuntime = Shadow.extract(VMRuntime.getRuntime());
    int[] outValues = (int[]) shadowVmRuntime.getObjectForAddress(outValuesAddress);
    int[] outIndices = (int[]) shadowVmRuntime.getObjectForAddress(outIndicesAddress);
    synchronized (this) {
      // Need to synchronize on AssetManager because we will be accessing
      // the native implementation of AssetManager.
      assetManagerReflector.ensureValidLocked();
      long xmlParserPtr =
          parser != null ? reflector(XmlBlockParserReflector.class, parser).getParseState() : 0;
      ReflectionHelpers.callStaticMethod(
          AssetManager.class,
          Shadow.directNativeMethodName(AssetManager.class.getName(), "nativeApplyStyleWithArray"),
          ClassParameter.from(long.class, assetManagerReflector.getObject()),
          ClassParameter.from(long.class, themePtr),
          ClassParameter.from(int.class, defStyleAttr),
          ClassParameter.from(int.class, defStyleRes),
          ClassParameter.from(long.class, xmlParserPtr),
          ClassParameter.from(int[].class, inAttrs),
          ClassParameter.from(int[].class, outValues),
          ClassParameter.from(int[].class, outIndices));
    }
  }

  @ForType(AssetManager.class)
  interface AssetManagerReflector {
    @Accessor("mObject")
    long getObject();

    void ensureValidLocked();
  }

  @ForType(XmlBlock.Parser.class)
  interface XmlBlockParserReflector {
    @Accessor("mParseState")
    long getParseState();
  }
}
