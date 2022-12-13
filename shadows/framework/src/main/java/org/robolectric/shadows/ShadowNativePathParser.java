package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.util.PathParser;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.PathParserNatives;
import org.robolectric.shadows.ShadowNativePathParser.Picker;

/** Shadow for {@link PathParser} that is backed by native code */
@Implements(
    value = PathParser.class,
    minSdk = O,
    shadowPicker = Picker.class,
    isInAndroidSdk = false)
public class ShadowNativePathParser {

  static {
    DefaultNativeRuntimeLoader.injectAndLoad();
  }

  @Implementation(minSdk = O)
  protected static void nParseStringForPath(long pathPtr, String pathString, int stringLength) {
    PathParserNatives.nParseStringForPath(pathPtr, pathString, stringLength);
  }

  @Implementation(minSdk = O)
  protected static long nCreatePathDataFromString(String pathString, int stringLength) {
    return PathParserNatives.nCreatePathDataFromString(pathString, stringLength);
  }

  @Implementation(minSdk = O)
  protected static void nCreatePathFromPathData(long outPathPtr, long pathData) {
    PathParserNatives.nCreatePathFromPathData(outPathPtr, pathData);
  }

  @Implementation(minSdk = O)
  protected static long nCreateEmptyPathData() {
    return PathParserNatives.nCreateEmptyPathData();
  }

  @Implementation(minSdk = O)
  protected static long nCreatePathData(long nativePtr) {
    return PathParserNatives.nCreatePathData(nativePtr);
  }

  @Implementation(minSdk = O)
  protected static boolean nInterpolatePathData(
      long outDataPtr, long fromDataPtr, long toDataPtr, float fraction) {
    return PathParserNatives.nInterpolatePathData(outDataPtr, fromDataPtr, toDataPtr, fraction);
  }

  @Implementation(minSdk = O)
  protected static void nFinalize(long nativePtr) {
    PathParserNatives.nFinalize(nativePtr);
  }

  @Implementation(minSdk = O)
  protected static boolean nCanMorph(long fromDataPtr, long toDataPtr) {
    return PathParserNatives.nCanMorph(fromDataPtr, toDataPtr);
  }

  @Implementation(minSdk = O)
  protected static void nSetPathData(long outDataPtr, long fromDataPtr) {
    PathParserNatives.nSetPathData(outDataPtr, fromDataPtr);
  }

  /** Shadow picker for {@link PathParser}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowPathParser.class, ShadowNativePathParser.class);
    }
  }
}
