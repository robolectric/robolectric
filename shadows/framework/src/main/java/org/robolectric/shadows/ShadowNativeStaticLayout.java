package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Paint;
import android.text.StaticLayout;
import android.text.TextPaint;
import java.nio.ByteBuffer;
import java.util.Locale;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.LineBreakerNatives;
import org.robolectric.nativeruntime.MeasuredTextBuilderNatives;
import org.robolectric.nativeruntime.MeasuredTextNatives;
import org.robolectric.nativeruntime.NativeAllocationRegistryNatives;
import org.robolectric.res.android.NativeObjRegistry;
import org.robolectric.shadows.ShadowNativeStaticLayout.Picker;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/**
 * Shadow for {@link StaticLayout} that is backed by native code for Android O-P. In Android Q, the
 * native methods relate to text layout were heavily refactored and moved to MeasuredText and
 * LineBreaker.
 */
@Implements(
    value = StaticLayout.class,
    minSdk = O,
    maxSdk = P,
    looseSignatures = true,
    shadowPicker = Picker.class)
public class ShadowNativeStaticLayout {

  // Only used for the O/O_MR1 adapter logic.
  static final NativeObjRegistry<NativeStaticLayoutSetup> nativeObjectRegistry =
      new NativeObjRegistry<>(NativeStaticLayoutSetup.class);

  @Implementation(minSdk = P, maxSdk = P)
  protected static long nInit(
      int breakStrategy,
      int hyphenationFrequency,
      boolean isJustified,
      int[] indents,
      int[] leftPaddings,
      int[] rightPaddings) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return LineBreakerNatives.nInit(breakStrategy, hyphenationFrequency, isJustified, indents);
  }

  @Implementation(minSdk = P, maxSdk = P)
  protected static void nFinish(long nativePtr) {
    LineBreakerNatives.nFinishP(nativePtr);
  }

  /**
   * This has to use looseSignatures due to {@code recycle} param with non-public type {@code
   * android.text.StaticLayout$LineBreaks}.
   */
  @Implementation(minSdk = P, maxSdk = P)
  protected static int nComputeLineBreaks(
      Object nativePtr,
      Object text,
      Object measuredTextPtr,
      Object length,
      Object firstWidth,
      Object firstWidthLineCount,
      Object restWidth,
      Object variableTabStopsObject,
      Object defaultTabStop,
      Object indentsOffset,
      Object recycle,
      Object recycleLength,
      Object recycleBreaks,
      Object recycleWidths,
      Object recycleAscents,
      Object recycleDescents,
      Object recycleFlags,
      Object charWidths) {

    return LineBreakerNatives.nComputeLineBreaksP(
        (long) nativePtr,
        (char[]) text,
        (long) measuredTextPtr,
        (int) length,
        (float) firstWidth,
        (int) firstWidthLineCount,
        (float) restWidth,
        intsToFloat((int[]) variableTabStopsObject),
        ((Number) defaultTabStop).floatValue(),
        (int) indentsOffset,
        recycle,
        (int) recycleLength,
        (int[]) recycleBreaks,
        (float[]) recycleWidths,
        (float[]) recycleAscents,
        (float[]) recycleDescents,
        (int[]) recycleFlags,
        (float[]) charWidths);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static long nNewBuilder() {
    return nativeObjectRegistry.register(new NativeStaticLayoutSetup());
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static void nFreeBuilder(long nativePtr) {
    NativeStaticLayoutSetup setup = nativeObjectRegistry.getNativeObject(nativePtr);

    NativeAllocationRegistryNatives.applyFreeFunction(
        LineBreakerNatives.nGetReleaseResultFunc(), setup.lineBreakerResultPtr);

    nativeObjectRegistry.unregister(nativePtr);
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static void nFinishBuilder(long nativePtr) {
    // No-op
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static long nLoadHyphenator(ByteBuffer buf, int offset, int minPrefix, int minSuffix) {
    // nLoadHyphenator is not supported
    return 0;
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static void nSetLocale(long nativePtr, String locale, long nativeHyphenator) {
    NativeStaticLayoutSetup setup = nativeObjectRegistry.getNativeObject(nativePtr);
    setup.localePaint.setTextLocale(Locale.forLanguageTag(locale));
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static void nSetIndents(long nativePtr, int[] indents) {
    NativeStaticLayoutSetup setup = nativeObjectRegistry.getNativeObject(nativePtr);
    setup.indents = indents;
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static void nSetupParagraph(
      long nativePtr,
      char[] text,
      int length,
      float firstWidth,
      int firstWidthLineCount,
      float restWidth,
      int[] variableTabStops,
      int defaultTabStop,
      int breakStrategy,
      int hyphenationFrequency,
      boolean isJustified) {
    NativeStaticLayoutSetup setup = nativeObjectRegistry.getNativeObject(nativePtr);
    setup.text = text;
    setup.length = length;
    setup.firstWidth = firstWidth;
    setup.firstWidthLineCount = firstWidthLineCount;
    setup.restWidth = restWidth;
    setup.variableTabStops = variableTabStops;
    setup.defaultTabStop = defaultTabStop;
    setup.breakStrategy = breakStrategy;
    setup.hyphenationFrequency = hyphenationFrequency;
    setup.isJustified = isJustified;
    setup.measuredTextBuilderPtr = MeasuredTextBuilderNatives.nInitBuilder();
  }

  @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static float nAddStyleRun(
      long nativePtr, long nativePaint, long nativeTypeface, int start, int end, boolean isRtl) {
    NativeStaticLayoutSetup setup = nativeObjectRegistry.getNativeObject(nativePtr);

    MeasuredTextBuilderNatives.nAddStyleRun(
        setup.measuredTextBuilderPtr, nativePaint, start, end, isRtl);
    return 0f;
  }

  @Implementation
  protected static void nAddMeasuredRun(long nativePtr, int start, int end, float[] widths) {
    NativeStaticLayoutSetup setup = nativeObjectRegistry.getNativeObject(nativePtr);
    MeasuredTextBuilderNatives.nAddStyleRun(
        setup.measuredTextBuilderPtr, setup.localePaint.getNativeInstance(), start, end, false);
  }

  @Implementation
  protected static void nAddReplacementRun(long nativePtr, int start, int end, float width) {
    NativeStaticLayoutSetup setup = nativeObjectRegistry.getNativeObject(nativePtr);
    MeasuredTextBuilderNatives.nAddReplacementRun(
        setup.measuredTextBuilderPtr, setup.localePaint.getNativeInstance(), start, end, width);
  }

  @Implementation
  protected static void nGetWidths(long nativePtr, float[] widths) {
    // Returns the width of each char in the text.
    NativeStaticLayoutSetup setup = nativeObjectRegistry.getNativeObject(nativePtr);
    setup.measuredTextPtr =
        MeasuredTextBuilderNatives.nBuildMeasuredText(
            setup.measuredTextBuilderPtr, 0, setup.text, false, false);
    for (int i = 0; i < setup.text.length; i++) {
      widths[i] = MeasuredTextNatives.nGetCharWidthAt(setup.measuredTextPtr, i);
    }
    MeasuredTextBuilderNatives.nFreeBuilder(setup.measuredTextBuilderPtr);
  }

  /**
   * This has to use looseSignatures due to {@code recycle} param with non-public type {@code
   * android.text.StaticLayout$LineBreaks}.
   */
  @Implementation
  protected static int nComputeLineBreaks(
      Object /*long*/ nativePtr,
      Object /*LineBreaks*/ recycle,
      Object /*int[]*/ recycleBreaksObject,
      Object /*float[]*/ recycleWidthsObject,
      Object /*int[]*/ recycleFlagsObject,
      Object /*int*/ recycleLength) {

    int[] recycleBreaks = (int[]) recycleBreaksObject;
    float[] recycleWidths = (float[]) recycleWidthsObject;
    int[] recycleFlags = (int[]) recycleFlagsObject;

    NativeStaticLayoutSetup setup = nativeObjectRegistry.getNativeObject((long) nativePtr);

    long lineBreakerBuilderPtr =
        LineBreakerNatives.nInit(
            setup.breakStrategy, setup.hyphenationFrequency, setup.isJustified, setup.indents);

    setup.lineBreakerResultPtr =
        LineBreakerNatives.nComputeLineBreaks(
            lineBreakerBuilderPtr,
            setup.text,
            setup.measuredTextPtr,
            setup.length,
            setup.firstWidth,
            setup.firstWidthLineCount,
            setup.restWidth,
            intsToFloat(setup.variableTabStops),
            (float) setup.defaultTabStop,
            0);

    int lineCount = LineBreakerNatives.nGetLineCount(setup.lineBreakerResultPtr);

    if (lineCount > recycleBreaks.length) {
      // resize the recycle objects
      recycleBreaks = new int[lineCount];
      recycleWidths = new float[lineCount];
      recycleFlags = new int[lineCount];
      reflector(LineBreaksReflector.class, recycle).setBreaks(recycleBreaks);
      reflector(LineBreaksReflector.class, recycle).setWidths(recycleWidths);
      reflector(LineBreaksReflector.class, recycle).setFlags(recycleFlags);
    }

    for (int i = 0; i < lineCount; i++) {
      recycleBreaks[i] = LineBreakerNatives.nGetLineBreakOffset(setup.lineBreakerResultPtr, i);
      recycleWidths[i] = LineBreakerNatives.nGetLineWidth(setup.lineBreakerResultPtr, i);
      recycleFlags[i] = LineBreakerNatives.nGetLineFlag(setup.lineBreakerResultPtr, i);
    }

    // Release the pointers used for the builder, the result pointer is the only relevant pointer
    // now.
    NativeAllocationRegistryNatives.applyFreeFunction(
        LineBreakerNatives.nGetReleaseFunc(), lineBreakerBuilderPtr);

    NativeAllocationRegistryNatives.applyFreeFunction(
        MeasuredTextNatives.nGetReleaseFunc(), setup.measuredTextPtr);

    return lineCount;
  }

  static final class NativeStaticLayoutSetup {

    char[] text;
    int length;
    float firstWidth;
    int firstWidthLineCount;
    float restWidth;
    int[] variableTabStops;
    int defaultTabStop;
    int breakStrategy;
    int hyphenationFrequency;
    boolean isJustified;
    int[] indents;
    Paint localePaint = new TextPaint(); // TODO(hoisie): use `mPaint` from StaticLayout.Builder
    long measuredTextBuilderPtr;
    long measuredTextPtr;
    long lineBreakerResultPtr;
  }

  private static float[] intsToFloat(int[] intArray) {
    if (intArray == null) {
      return null;
    }
    float[] floatArray = new float[intArray.length];

    for (int i = 0; i < floatArray.length; i++) {
      floatArray[i] = intArray[i];
    }
    return floatArray;
  }

  @ForType(className = "android.text.StaticLayout$LineBreaks")
  interface LineBreaksReflector {
    @Accessor("breaks")
    int[] getBreaks();

    @Accessor("breaks")
    void setBreaks(int[] breaks);

    @Accessor("widths")
    float[] getWidths();

    @Accessor("widths")
    void setWidths(float[] widths);

    @Accessor("flags")
    int[] getFlags();

    @Accessor("flags")
    void setFlags(int[] flags);
  }

  /** Shadow picker for {@link StaticLayout}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowStaticLayout.class, ShadowNativeStaticLayout.class);
    }
  }
}
