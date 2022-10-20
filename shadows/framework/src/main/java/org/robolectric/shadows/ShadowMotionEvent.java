package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.P;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_ORIENTATION;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_PRESSURE;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_SIZE;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_TOOL_MAJOR;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_TOOL_MINOR;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_TOUCH_MAJOR;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_TOUCH_MINOR;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_X;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_Y;

import android.graphics.Matrix;
import android.os.Parcel;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.res.android.NativeObjRegistry;
import org.robolectric.util.ReflectionHelpers;

/**
 * Shadow of MotionEvent.
 *
 * <p>The Android framework stores motion events in a pool of native objects. All motion event data
 * is stored natively, and accessed via a series of static native methods following the pattern
 * nativeGetXXXX(mNativePtr, ...)
 *
 * <p>This shadow mirrors this design, but has java equivalents of each native object. Most of the
 * contents of this class were transliterated from oreo-mr1 (SDK 27)
 * frameworks/base/core/jni/android_view_MotionEvent.cpp
 *
 * @see <a
 *     href="https://android.googlesource.com/platform/frameworks/base/+/oreo-mr1-release/core/jni/android_view_MotionEvent.cpp">core/jni/android_view_MotionEvent.cpp</a>
 *     <p>Tests should not reference this class directly. MotionEvents should be created via one of
 *     the MotionEvent.obtain methods or via MotionEventBuilder.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(MotionEvent.class)
public class ShadowMotionEvent extends ShadowInputEvent {

  private static NativeObjRegistry<NativeInput.MotionEvent> nativeMotionEventRegistry =
      new NativeObjRegistry<>(NativeInput.MotionEvent.class);

  private static final int HISTORY_CURRENT = -0x80000000;

  @RealObject private MotionEvent realMotionEvent;

  @Resetter
  public static void reset() {
    // rely on MotionEvent finalizer to clear native object instead of calling
    // nativeMotionEventRegistry.clear();
    ReflectionHelpers.setStaticField(MotionEvent.class, "gRecyclerTop", null);
    ReflectionHelpers.setStaticField(MotionEvent.class, "gSharedTempPointerCoords", null);
    ReflectionHelpers.setStaticField(MotionEvent.class, "gSharedTempPointerProperties", null);
    ReflectionHelpers.setStaticField(MotionEvent.class, "gRecyclerUsed", 0);
    ReflectionHelpers.setStaticField(MotionEvent.class, "gSharedTempPointerIndexMap", null);
  }

  private static void validatePointerCount(int pointerCount) {
    checkState(pointerCount >= 1, "pointerCount must be at least 1");
  }

  private static void validatePointerPropertiesArray(
      PointerProperties[] pointerPropertiesObjArray, int pointerCount) {
    checkNotNull(pointerPropertiesObjArray, "pointerProperties array must not be null");
    checkState(
        pointerPropertiesObjArray.length >= pointerCount,
        "pointerProperties array must be large enough to hold all pointers");
  }

  private static void validatePointerCoordsObjArray(
      PointerCoords[] pointerCoordsObjArray, int pointerCount) {
    checkNotNull(pointerCoordsObjArray, "pointerCoords array must not be null");
    checkState(
        pointerCoordsObjArray.length >= pointerCount,
        "pointerCoords array must be large enough to hold all pointers");
  }

  private static void validatePointerIndex(int pointerIndex, int pointerCount) {
    checkState(pointerIndex >= 0 && pointerIndex < pointerCount, "pointerIndex out of range");
  }

  private static void validateHistoryPos(int historyPos, int historySize) {
    checkState(historyPos >= 0 && historyPos < historySize, "historyPos out of range");
  }

  private static void validatePointerCoords(PointerCoords pointerCoordsObj) {
    checkNotNull(pointerCoordsObj, "pointerCoords must not be null");
  }

  private static void validatePointerProperties(PointerProperties pointerPropertiesObj) {
    checkNotNull(pointerPropertiesObj, "pointerProperties must not be null");
  }

  private static NativeInput.PointerCoords pointerCoordsToNative(
      PointerCoords pointerCoordsObj, float xOffset, float yOffset) {
    NativeInput.PointerCoords outRawPointerCoords = new NativeInput.PointerCoords();
    outRawPointerCoords.clear();
    outRawPointerCoords.setAxisValue(AMOTION_EVENT_AXIS_X, pointerCoordsObj.x - xOffset);
    outRawPointerCoords.setAxisValue(AMOTION_EVENT_AXIS_Y, pointerCoordsObj.y - yOffset);
    outRawPointerCoords.setAxisValue(AMOTION_EVENT_AXIS_PRESSURE, pointerCoordsObj.pressure);
    outRawPointerCoords.setAxisValue(AMOTION_EVENT_AXIS_SIZE, pointerCoordsObj.size);
    outRawPointerCoords.setAxisValue(AMOTION_EVENT_AXIS_TOUCH_MAJOR, pointerCoordsObj.touchMajor);
    outRawPointerCoords.setAxisValue(AMOTION_EVENT_AXIS_TOUCH_MINOR, pointerCoordsObj.touchMinor);
    outRawPointerCoords.setAxisValue(AMOTION_EVENT_AXIS_TOOL_MAJOR, pointerCoordsObj.toolMajor);
    outRawPointerCoords.setAxisValue(AMOTION_EVENT_AXIS_TOOL_MINOR, pointerCoordsObj.toolMinor);
    outRawPointerCoords.setAxisValue(AMOTION_EVENT_AXIS_ORIENTATION, pointerCoordsObj.orientation);
    long packedAxisBits = ReflectionHelpers.getField(pointerCoordsObj, "mPackedAxisBits");
    NativeBitSet64 bits = new NativeBitSet64(packedAxisBits);
    if (!bits.isEmpty()) {
      float[] valuesArray = ReflectionHelpers.getField(pointerCoordsObj, "mPackedAxisValues");
      if (valuesArray != null) {
        int index = 0;
        do {
          int axis = bits.clearFirstMarkedBit();
          outRawPointerCoords.setAxisValue(axis, valuesArray[index++]);
        } while (!bits.isEmpty());
      }
    }
    return outRawPointerCoords;
  }

  private static float[] obtainPackedAxisValuesArray(
      int minSize, PointerCoords outPointerCoordsObj) {
    float[] outValuesArray = ReflectionHelpers.getField(outPointerCoordsObj, "mPackedAxisValues");
    if (outValuesArray != null) {
      int size = outValuesArray.length;
      if (minSize <= size) {
        return outValuesArray;
      }
    }
    int size = 8;
    while (size < minSize) {
      size *= 2;
    }
    outValuesArray = new float[size];
    ReflectionHelpers.setField(outPointerCoordsObj, "mPackedAxisValues", outValuesArray);
    return outValuesArray;
  }

  private static void pointerCoordsFromNative(
      NativeInput.PointerCoords rawPointerCoords,
      float xOffset,
      float yOffset,
      PointerCoords outPointerCoordsObj) {
    outPointerCoordsObj.x = rawPointerCoords.getAxisValue(AMOTION_EVENT_AXIS_X) + xOffset;
    outPointerCoordsObj.y = rawPointerCoords.getAxisValue(AMOTION_EVENT_AXIS_Y) + yOffset;
    outPointerCoordsObj.pressure = rawPointerCoords.getAxisValue(AMOTION_EVENT_AXIS_PRESSURE);
    outPointerCoordsObj.size = rawPointerCoords.getAxisValue(AMOTION_EVENT_AXIS_SIZE);
    outPointerCoordsObj.touchMajor = rawPointerCoords.getAxisValue(AMOTION_EVENT_AXIS_TOUCH_MAJOR);
    outPointerCoordsObj.touchMinor = rawPointerCoords.getAxisValue(AMOTION_EVENT_AXIS_TOUCH_MINOR);
    outPointerCoordsObj.toolMajor = rawPointerCoords.getAxisValue(AMOTION_EVENT_AXIS_TOOL_MAJOR);
    outPointerCoordsObj.toolMinor = rawPointerCoords.getAxisValue(AMOTION_EVENT_AXIS_TOOL_MINOR);
    outPointerCoordsObj.orientation = rawPointerCoords.getAxisValue(AMOTION_EVENT_AXIS_ORIENTATION);
    long outBits = 0;
    NativeBitSet64 bits = new NativeBitSet64(rawPointerCoords.getBits());
    bits.clearBit(AMOTION_EVENT_AXIS_X);
    bits.clearBit(AMOTION_EVENT_AXIS_Y);
    bits.clearBit(AMOTION_EVENT_AXIS_PRESSURE);
    bits.clearBit(AMOTION_EVENT_AXIS_SIZE);
    bits.clearBit(AMOTION_EVENT_AXIS_TOUCH_MAJOR);
    bits.clearBit(AMOTION_EVENT_AXIS_TOUCH_MINOR);
    bits.clearBit(AMOTION_EVENT_AXIS_TOOL_MAJOR);
    bits.clearBit(AMOTION_EVENT_AXIS_TOOL_MINOR);
    bits.clearBit(AMOTION_EVENT_AXIS_ORIENTATION);
    if (!bits.isEmpty()) {
      int packedAxesCount = bits.count();
      float[] outValuesArray = obtainPackedAxisValuesArray(packedAxesCount, outPointerCoordsObj);
      float[] outValues = outValuesArray;
      int index = 0;
      do {
        int axis = bits.clearFirstMarkedBit();
        outBits |= NativeBitSet64.valueForBit(axis);
        outValues[index++] = rawPointerCoords.getAxisValue(axis);
      } while (!bits.isEmpty());
    }
    ReflectionHelpers.setField(outPointerCoordsObj, "mPackedAxisBits", outBits);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static int nativeInitialize(
      int nativePtr,
      int deviceId,
      int source,
      int action,
      int flags,
      int edgeFlags,
      int metaState,
      int buttonState,
      float xOffset,
      float yOffset,
      float xPrecision,
      float yPrecision,
      long downTimeNanos,
      long eventTimeNanos,
      int pointerCount,
      PointerProperties[] pointerIds,
      PointerCoords[] pointerCoords) {
    return (int)
        nativeInitialize(
            (long) nativePtr,
            deviceId,
            source,
            action,
            flags,
            edgeFlags,
            metaState,
            buttonState,
            xOffset,
            yOffset,
            xPrecision,
            yPrecision,
            downTimeNanos,
            eventTimeNanos,
            pointerCount,
            pointerIds,
            pointerCoords);
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = P)
  @HiddenApi
  protected static long nativeInitialize(
      long nativePtr,
      int deviceId,
      int source,
      int action,
      int flags,
      int edgeFlags,
      int metaState,
      int buttonState,
      float xOffset,
      float yOffset,
      float xPrecision,
      float yPrecision,
      long downTimeNanos,
      long eventTimeNanos,
      int pointerCount,
      PointerProperties[] pointerPropertiesObjArray,
      PointerCoords[] pointerCoordsObjArray) {

    validatePointerCount(pointerCount);
    validatePointerPropertiesArray(pointerPropertiesObjArray, pointerCount);
    validatePointerCoordsObjArray(pointerCoordsObjArray, pointerCount);

    NativeInput.MotionEvent event;
    if (nativePtr > 0) {
      event = nativeMotionEventRegistry.getNativeObject(nativePtr);
    } else {
      event = new NativeInput.MotionEvent();
      nativePtr = nativeMotionEventRegistry.register(event);
    }

    NativeInput.PointerCoords[] rawPointerCoords = new NativeInput.PointerCoords[pointerCount];
    for (int i = 0; i < pointerCount; i++) {
      PointerCoords pointerCoordsObj = pointerCoordsObjArray[i];
      checkNotNull(pointerCoordsObj);
      rawPointerCoords[i] = pointerCoordsToNative(pointerCoordsObj, xOffset, yOffset);
    }

    event.initialize(
        deviceId,
        source,
        action,
        0,
        flags,
        edgeFlags,
        metaState,
        buttonState,
        xOffset,
        yOffset,
        xPrecision,
        yPrecision,
        downTimeNanos,
        eventTimeNanos,
        pointerCount,
        pointerPropertiesObjArray,
        rawPointerCoords);
    return nativePtr;
  }

  // TODO(brettchabot): properly handle displayId
  @Implementation(minSdk = android.os.Build.VERSION_CODES.Q)
  @HiddenApi
  protected static long nativeInitialize(
      long nativePtr,
      int deviceId,
      int source,
      int displayId,
      int action,
      int flags,
      int edgeFlags,
      int metaState,
      int buttonState,
      int classification,
      float xOffset,
      float yOffset,
      float xPrecision,
      float yPrecision,
      long downTimeNanos,
      long eventTimeNanos,
      int pointerCount,
      PointerProperties[] pointerIds,
      PointerCoords[] pointerCoords) {
        return
        nativeInitialize(
            nativePtr,
            deviceId,
            source,
            action,
            flags,
            edgeFlags,
            metaState,
            buttonState,
            xOffset,
            yOffset,
            xPrecision,
            yPrecision,
            downTimeNanos,
            eventTimeNanos,
            pointerCount,
            pointerIds,
            pointerCoords);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static void nativeDispose(int nativePtr) {
    nativeDispose((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static void nativeDispose(long nativePtr) {
    nativeMotionEventRegistry.unregister(nativePtr);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static void nativeAddBatch(
      int nativePtr, long eventTimeNanos, PointerCoords[] pointerCoordsObjArray, int metaState) {
    nativeAddBatch((long) nativePtr, eventTimeNanos, pointerCoordsObjArray, metaState);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static void nativeAddBatch(
      long nativePtr, long eventTimeNanos, PointerCoords[] pointerCoordsObjArray, int metaState) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    int pointerCount = event.getPointerCount();
    validatePointerCoordsObjArray(pointerCoordsObjArray, pointerCount);
    NativeInput.PointerCoords[] rawPointerCoords = new NativeInput.PointerCoords[pointerCount];
    for (int i = 0; i < pointerCount; i++) {
      PointerCoords pointerCoordsObj = pointerCoordsObjArray[i];
      checkNotNull(pointerCoordsObj);
      rawPointerCoords[i] =
          pointerCoordsToNative(pointerCoordsObj, event.getXOffset(), event.getYOffset());
    }
    event.addSample(eventTimeNanos, rawPointerCoords);
    event.setMetaState(event.getMetaState() | metaState);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static void nativeGetPointerCoords(
      int nativePtr, int pointerIndex, int historyPos, PointerCoords outPointerCoordsObj) {
    nativeGetPointerCoords((long) nativePtr, pointerIndex, historyPos, outPointerCoordsObj);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static void nativeGetPointerCoords(
      long nativePtr, int pointerIndex, int historyPos, PointerCoords outPointerCoordsObj) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    int pointerCount = event.getPointerCount();
    validatePointerIndex(pointerIndex, pointerCount);
    validatePointerCoords(outPointerCoordsObj);

    NativeInput.PointerCoords rawPointerCoords;
    if (historyPos == HISTORY_CURRENT) {
      rawPointerCoords = event.getRawPointerCoords(pointerIndex);
    } else {
      int historySize = event.getHistorySize();
      validateHistoryPos(historyPos, historySize);
      rawPointerCoords = event.getHistoricalRawPointerCoords(pointerIndex, historyPos);
    }
    pointerCoordsFromNative(
        rawPointerCoords, event.getXOffset(), event.getYOffset(), outPointerCoordsObj);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static void nativeGetPointerProperties(
      int nativePtr, int pointerIndex, PointerProperties outPointerPropertiesObj) {
    nativeGetPointerProperties((long) nativePtr, pointerIndex, outPointerPropertiesObj);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static void nativeGetPointerProperties(
      long nativePtr, int pointerIndex, PointerProperties outPointerPropertiesObj) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    int pointerCount = event.getPointerCount();
    validatePointerIndex(pointerIndex, pointerCount);
    validatePointerProperties(outPointerPropertiesObj);

    PointerProperties pointerProperties = event.getPointerProperties(pointerIndex);
    // pointerPropertiesFromNative(env, pointerProperties, outPointerPropertiesObj);
    outPointerPropertiesObj.copyFrom(pointerProperties);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static int nativeReadFromParcel(int nativePtr, Parcel parcelObj) {
    return (int) nativeReadFromParcel((long) nativePtr, parcelObj);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static long nativeReadFromParcel(long nativePtr, Parcel parcelObj) {
    NativeInput.MotionEvent event;
    if (nativePtr == 0) {
      event = new NativeInput.MotionEvent();
      nativePtr = nativeMotionEventRegistry.register(event);
    } else {
      event = nativeMotionEventRegistry.getNativeObject(nativePtr);
    }
    boolean status = event.readFromParcel(parcelObj);
    if (!status) {
      if (nativePtr > 0) {
        nativeMotionEventRegistry.unregister(nativePtr);
      }
      throw new RuntimeException("Failed to read MotionEvent parcel.");
    }
    return nativePtr;
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static void nativeWriteToParcel(int nativePtr, Parcel parcel) {
    nativeWriteToParcel((long) nativePtr, parcel);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static void nativeWriteToParcel(long nativePtr, Parcel parcel) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    if (!event.writeToParcel(parcel)) {
      throw new RuntimeException("Failed to write MotionEvent parcel.");
    }
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static String nativeAxisToString(int axis) {
    // The native code just mirrors the AXIS_* constants defined in MotionEvent.java.
    // Look up the field value by reflection to future proof this method
    for (Field field : MotionEvent.class.getDeclaredFields()) {
      int modifiers = field.getModifiers();
      try {
        if (Modifier.isStatic(modifiers)
            && Modifier.isPublic(modifiers)
            && field.getName().startsWith("AXIS_")
            && field.getInt(null) == axis) {
          // return the field name stripping off the "AXIS_" prefix
          return field.getName().substring(5);
        }
      } catch (IllegalAccessException e) {
        // ignore
      }
    }
    return null;
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static int nativeAxisFromString(String label) {
    // The native code just mirrors the AXIS_* constants defined in MotionEvent.java. Look up
    // the field value by reflection
    try {
      Field constantField = MotionEvent.class.getDeclaredField("AXIS_" + label);
      return constantField.getInt(null);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      return 0;
    }
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static int nativeGetPointerId(int nativePtr, int pointerIndex) {
    return nativeGetPointerId((long) nativePtr, pointerIndex);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static int nativeGetPointerId(long nativePtr, int pointerIndex) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    int pointerCount = event.getPointerCount();
    validatePointerIndex(pointerIndex, pointerCount);
    return event.getPointerId(pointerIndex);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static int nativeGetToolType(int nativePtr, int pointerIndex) {
    return nativeGetToolType((long) nativePtr, pointerIndex);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static int nativeGetToolType(long nativePtr, int pointerIndex) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    int pointerCount = event.getPointerCount();
    validatePointerIndex(pointerIndex, pointerCount);
    return event.getToolType(pointerIndex);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static long nativeGetEventTimeNanos(int nativePtr, int historyPos) {
    return nativeGetEventTimeNanos((long) nativePtr, historyPos);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static long nativeGetEventTimeNanos(long nativePtr, int historyPos) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    if (historyPos == HISTORY_CURRENT) {
      return event.getEventTime();
    } else {
      int historySize = event.getHistorySize();
      validateHistoryPos(historyPos, historySize);
      return event.getHistoricalEventTime(historyPos);
    }
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static float nativeGetRawAxisValue(
      int nativePtr, int axis, int pointerIndex, int historyPos) {
    return nativeGetRawAxisValue((long) nativePtr, axis, pointerIndex, historyPos);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static float nativeGetRawAxisValue(
      long nativePtr, int axis, int pointerIndex, int historyPos) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    int pointerCount = event.getPointerCount();
    validatePointerIndex(pointerIndex, pointerCount);

    if (historyPos == HISTORY_CURRENT) {
      return event.getRawAxisValue(axis, pointerIndex);
    } else {
      int historySize = event.getHistorySize();
      validateHistoryPos(historyPos, historySize);
      return event.getHistoricalRawAxisValue(axis, pointerIndex, historyPos);
    }
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static float nativeGetAxisValue(
      int nativePtr, int axis, int pointerIndex, int historyPos) {
    return nativeGetAxisValue((long) nativePtr, axis, pointerIndex, historyPos);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static float nativeGetAxisValue(
      long nativePtr, int axis, int pointerIndex, int historyPos) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    int pointerCount = event.getPointerCount();
    validatePointerIndex(pointerIndex, pointerCount);

    if (historyPos == HISTORY_CURRENT) {
      return event.getAxisValue(axis, pointerIndex);
    } else {
      int historySize = event.getHistorySize();
      validateHistoryPos(historyPos, historySize);
      return event.getHistoricalAxisValue(axis, pointerIndex, historyPos);
    }
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static int nativeCopy(int destNativePtr, int sourceNativePtr, boolean keepHistory) {
    return (int) nativeCopy((long) destNativePtr, (long) sourceNativePtr, keepHistory);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static long nativeCopy(long destNativePtr, long sourceNativePtr, boolean keepHistory) {
    NativeInput.MotionEvent destEvent = nativeMotionEventRegistry.peekNativeObject(destNativePtr);
    if (destEvent == null) {
      destEvent = new NativeInput.MotionEvent();
      destNativePtr = nativeMotionEventRegistry.register(destEvent);
    }
    NativeInput.MotionEvent sourceEvent = getNativeMotionEvent(sourceNativePtr);
    destEvent.copyFrom(sourceEvent, keepHistory);
    return destNativePtr;
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static int nativeGetDeviceId(int nativePtr) {
    return nativeGetDeviceId((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static int nativeGetDeviceId(long nativePtr) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    return event.getDeviceId();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static int nativeGetSource(int nativePtr) {
    return nativeGetSource((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static int nativeGetSource(long nativePtr) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    return event.getSource();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static int nativeSetSource(int nativePtr, int source) {
    nativeSetSource((long) nativePtr, source);
    return 0;
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  @SuppressWarnings("robolectric.ShadowReturnTypeMismatch")
  protected static void nativeSetSource(long nativePtr, int source) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    event.setSource(source);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static int nativeGetAction(int nativePtr) {
    return nativeGetAction((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static int nativeGetAction(long nativePtr) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    return event.getAction();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static void nativeSetAction(int nativePtr, int action) {
    nativeSetAction((long) nativePtr, action);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static void nativeSetAction(long nativePtr, int action) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    event.setAction(action);
  }

  @Implementation(minSdk = M)
  @HiddenApi
  protected static int nativeGetActionButton(long nativePtr) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    return event.getActionButton();
  }

  @Implementation(minSdk = M)
  @HiddenApi
  protected static void nativeSetActionButton(long nativePtr, int button) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    event.setActionButton(button);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static boolean nativeIsTouchEvent(int nativePtr) {
    return nativeIsTouchEvent((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static boolean nativeIsTouchEvent(long nativePtr) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    return event.isTouchEvent();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static int nativeGetFlags(int nativePtr) {
    return nativeGetFlags((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static int nativeGetFlags(long nativePtr) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    return event.getFlags();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static void nativeSetFlags(int nativePtr, int flags) {
    nativeSetFlags((long) nativePtr, flags);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static void nativeSetFlags(long nativePtr, int flags) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    event.setFlags(flags);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static int nativeGetEdgeFlags(int nativePtr) {
    return nativeGetEdgeFlags((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static int nativeGetEdgeFlags(long nativePtr) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    return event.getEdgeFlags();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static void nativeSetEdgeFlags(int nativePtr, int edgeFlags) {
    nativeSetEdgeFlags((long) nativePtr, edgeFlags);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static void nativeSetEdgeFlags(long nativePtr, int edgeFlags) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    event.setEdgeFlags(edgeFlags);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static int nativeGetMetaState(int nativePtr) {
    return nativeGetMetaState((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static int nativeGetMetaState(long nativePtr) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    return event.getMetaState();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static int nativeGetButtonState(int nativePtr) {
    return nativeGetButtonState((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static int nativeGetButtonState(long nativePtr) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    return event.getButtonState();
  }

  @Implementation(minSdk = M)
  @HiddenApi
  protected static void nativeSetButtonState(long nativePtr, int buttonState) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    event.setButtonState(buttonState);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static void nativeOffsetLocation(int nativePtr, float deltaX, float deltaY) {
    nativeOffsetLocation((long) nativePtr, deltaX, deltaY);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static void nativeOffsetLocation(long nativePtr, float deltaX, float deltaY) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    event.offsetLocation(deltaX, deltaY);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static float nativeGetXOffset(int nativePtr) {
    return nativeGetXOffset((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static float nativeGetXOffset(long nativePtr) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    return event.getXOffset();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static float nativeGetYOffset(int nativePtr) {
    return nativeGetYOffset((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static float nativeGetYOffset(long nativePtr) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    return event.getYOffset();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static float nativeGetXPrecision(int nativePtr) {
    return nativeGetXPrecision((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static float nativeGetXPrecision(long nativePtr) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    return event.getXPrecision();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static float nativeGetYPrecision(int nativePtr) {
    return nativeGetYPrecision((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static float nativeGetYPrecision(long nativePtr) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    return event.getYPrecision();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static long nativeGetDownTimeNanos(int nativePtr) {
    return nativeGetDownTimeNanos((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static long nativeGetDownTimeNanos(long nativePtr) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    return event.getDownTime();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static void nativeSetDownTimeNanos(int nativePtr, long downTimeNanos) {
    nativeSetDownTimeNanos((long) nativePtr, downTimeNanos);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static void nativeSetDownTimeNanos(long nativePtr, long downTimeNanos) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    event.setDownTime(downTimeNanos);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static int nativeGetPointerCount(int nativePtr) {
    return nativeGetPointerCount((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static int nativeGetPointerCount(long nativePtr) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    return event.getPointerCount();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static int nativeFindPointerIndex(int nativePtr, int pointerId) {
    return nativeFindPointerIndex((long) nativePtr, pointerId);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static int nativeFindPointerIndex(long nativePtr, int pointerId) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    return event.findPointerIndex(pointerId);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static int nativeGetHistorySize(int nativePtr) {
    return nativeGetHistorySize((long) nativePtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static int nativeGetHistorySize(long nativePtr) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    return event.getHistorySize();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  @HiddenApi
  protected static void nativeScale(int nativePtr, float scale) {
    nativeScale((long) nativePtr, scale);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected static void nativeScale(long nativePtr, float scale) {
    NativeInput.MotionEvent event = getNativeMotionEvent(nativePtr);
    event.scale(scale);
  }

  private static NativeInput.MotionEvent getNativeMotionEvent(long nativePtr) {
    // check that MotionEvent was initialized properly. This can occur if MotionEvent was mocked
    checkState(
        nativePtr > 0,
        "MotionEvent has not been initialized. "
            + "Ensure MotionEvent.obtain was used to create it, instead of creating it directly "
            + "or via a Mocking framework");

    return nativeMotionEventRegistry.getNativeObject(nativePtr);
  }

  @Implementation
  protected final void transform(Matrix matrix) {
    checkNotNull(matrix);
    NativeInput.MotionEvent event = getNativeMotionEvent();

    float[] m = new float[9];
    matrix.getValues(m);
    event.transform(m);
  }

  private NativeInput.MotionEvent getNativeMotionEvent() {
    long nativePtr;
    if (RuntimeEnvironment.getApiLevel() <= KITKAT_WATCH) {
      Integer nativePtrInt = ReflectionHelpers.getField(realMotionEvent, "mNativePtr");
      nativePtr = nativePtrInt.longValue();
    } else {
      nativePtr = ReflectionHelpers.getField(realMotionEvent, "mNativePtr");
    }
    return nativeMotionEventRegistry.getNativeObject(nativePtr);
  }

  // Testing API methods

  /**
   * @deprecated use {@link MotionEvent#obtain} or {@link
   *     androidx.test.core.view.MotionEventBuilder} to create a MotionEvent with desired data.
   */
  @Deprecated
  public MotionEvent setPointer2(float pointer1X, float pointer1Y) {
    NativeInput.MotionEvent event = getNativeMotionEvent();
    List<NativeInput.PointerCoords> pointerCoords = event.getSamplePointerCoords();
    List<PointerProperties> pointerProperties = event.getPointerProperties();
    ensureTwoPointers(pointerCoords, pointerProperties);

    pointerCoords.get(1).setAxisValue(AMOTION_EVENT_AXIS_X, pointer1X);
    pointerCoords.get(1).setAxisValue(AMOTION_EVENT_AXIS_Y, pointer1Y);
    return realMotionEvent;
  }

  private static void ensureTwoPointers(
      List<NativeInput.PointerCoords> pointerCoords, List<PointerProperties> pointerProperties) {
    if (pointerCoords.size() < 2) {
      pointerCoords.add(new NativeInput.PointerCoords());
    }
    if (pointerProperties.size() < 2) {
      pointerProperties.add(new PointerProperties());
    }
  }

  /**
   * @deprecated use {@link MotionEvent#obtain} or {@link
   *     androidx.test.core.view.MotionEventBuilder#setPointerAction(int, int)} to create a
   *     MotionEvent with desired data.
   */
  @Deprecated
  public void setPointerIndex(int pointerIndex) {
    NativeInput.MotionEvent event = getNativeMotionEvent();
    // pointer index is stored in upper two bytes of action
    event.setAction(
        event.getAction() | ((pointerIndex & 0xff) << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
  }

  /**
   * @deprecated use {@link MotionEvent#obtain} or {@link MotionEventBuilder} to create a
   *     MotionEvent with desired data
   */
  @Deprecated
  public void setPointerIds(int index0PointerId, int index1PointerId) {
    NativeInput.MotionEvent event = getNativeMotionEvent();
    List<NativeInput.PointerCoords> pointerCoords = event.getSamplePointerCoords();
    List<PointerProperties> pointerProperties = event.getPointerProperties();
    ensureTwoPointers(pointerCoords, pointerProperties);

    pointerProperties.get(0).id = index0PointerId;
    pointerProperties.get(1).id = index1PointerId;
  }
}
