package org.robolectric.shadows;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_ORIENTATION;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_PRESSURE;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_SIZE;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_TOOL_MAJOR;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_TOOL_MINOR;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_TOUCH_MAJOR;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_TOUCH_MINOR;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_X;
import static org.robolectric.shadows.NativeAndroidInput.AMOTION_EVENT_AXIS_Y;

import android.os.Parcel;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.robolectric.Shadows;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.res.android.Ref;
import org.robolectric.util.ReflectionHelpers;

/**
 * Shadow of MotionEvent.
 *
 * Original Android implementation stores motion events in a pool of native objects. All motion
 * event data is stored natively, and accessed via a series of static native methods following the
 * pattern nativeGetXXXX(mNativePtr, ...)
 *
 * This shadow mirrors this design, but has java equivalents of each native object.
 * Most of the contents of this class were transliterated from frameworks/base/core/jni/android_view_MotionEvent.cpp
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(MotionEvent.class)
public class ShadowMotionEvent {

  private static NativeObjRegistry<NativeInput.MotionEvent> nativeObjRegistry = new NativeObjRegistry<>();

  @Resetter
  public static void reset() {
    nativeObjRegistry.clear();
  }

  @Implementation
  @HiddenApi
  protected static long nativeInitialize(long nativePtr,
      int deviceId, int source, int action, int flags, int edgeFlags,
      int metaState, int buttonState,
      float xOffset, float yOffset, float xPrecision, float yPrecision,
      long downTimeNanos, long eventTimeNanos,
      int pointerCount, PointerProperties[] pointerPropertiesObjArray, PointerCoords[] pointerCoordsObjArray) {

    if (!validatePointerCount(pointerCount)
        || !validatePointerPropertiesArray(pointerPropertiesObjArray, pointerCount)
        || !validatePointerCoordsObjArray(pointerCoordsObjArray, pointerCount)) {
      return 0;
    }

    NativeInput.MotionEvent event;
    if (nativePtr > 0) {
      event = nativeObjRegistry.getNativeObject(nativePtr);
    } else {
      event = new NativeInput.MotionEvent();
      nativePtr = nativeObjRegistry.getNativeObjectId(event);
    }

    NativeInput.PointerCoords[] rawPointerCoords = new NativeInput.PointerCoords[pointerCount];
    for (int i = 0; i < pointerCount; i++) {
      PointerCoords pointerCoordsObj = pointerCoordsObjArray[i];
      checkNotNull(pointerCoordsObj);
      rawPointerCoords[i] = pointerCoordsToNative(pointerCoordsObj, xOffset, yOffset);
    }

    event.initialize(deviceId, source, action, 0, flags, edgeFlags, metaState, buttonState,
        xOffset, yOffset, xPrecision, yPrecision,
        downTimeNanos, eventTimeNanos, pointerCount, pointerPropertiesObjArray, rawPointerCoords);
    return nativePtr;
  }


//   static struct {
//     jclass clazz;
//     jmethodID obtain;
//     jmethodID recycle;
//     jfieldID mNativePtr;
//   } gMotionEventClassInfo;
//   static struct {
//     jfieldID mPackedAxisBits;
//     jfieldID mPackedAxisValues;
//     jfieldID x;
//     jfieldID y;
//     jfieldID pressure;
//     jfieldID size;
//     jfieldID touchMajor;
//     jfieldID touchMinor;
//     jfieldID toolMajor;
//     jfieldID toolMinor;
//     jfieldID orientation;
//   } gPointerCoordsClassInfo;
//   static struct {
//     jfieldID id;
//     jfieldID toolType;
//   } gPointerPropertiesClassInfo;
// // ----------------------------------------------------------------------------
//   MotionEvent* getNativePtr(JNIEnv* env, jobject eventObj) {
//     if (!eventObj) {
//       return NULL;
//     }
//     return reinterpret_cast<MotionEvent*>(
//         env.GetLongField(eventObj, gMotionEventClassInfo.mNativePtr));
//   }
//   static void setNativePtr(JNIEnv* env, jobject eventObj,
//       MotionEvent* event) {
//     env.SetLongField(eventObj, gMotionEventClassInfo.mNativePtr,
//         reinterpret_cast<long>(event));
//   }
//   jobject obtainAsCopy(JNIEnv* env, const MotionEvent* event) {
//     jobject eventObj = env.CallStaticObjectMethod(gMotionEventClassInfo.clazz,
//         gMotionEventClassInfo.obtain);
//     if (env.ExceptionCheck() || !eventObj) {
//       ALOGE("An exception occurred while obtaining a motion event.");
//       LOGE_EX(env);
//       env.ExceptionClear();
//       return NULL;
//     }
//     MotionEvent* destEvent = getNativePtr(env, eventObj);
//     if (!destEvent) {
//       destEvent = new MotionEvent();
//       setNativePtr(env, eventObj, destEvent);
//     }
//     destEvent.copyFrom(event, true);
//     return eventObj;
//   }
//   status_t recycle(JNIEnv* env, jobject eventObj) {
//     env.CallVoidMethod(eventObj, gMotionEventClassInfo.recycle);
//     if (env.ExceptionCheck()) {
//       ALOGW("An exception occurred while recycling a motion event.");
//       LOGW_EX(env);
//       env.ExceptionClear();
//       return UNKNOWN_ERROR;
//     }
//     return OK;
//   }
// // ----------------------------------------------------------------------------

  private static final int HISTORY_CURRENT = -0x80000000;

  private static boolean validatePointerCount(int pointerCount) {
    checkState(pointerCount >= 1, "pointerCount must be at least 1");
    return true;
  }

  private static boolean validatePointerPropertiesArray(PointerProperties[] pointerPropertiesObjArray,
      int pointerCount) {
    checkNotNull(pointerPropertiesObjArray, "pointerProperties array must not be null");
    checkState(pointerPropertiesObjArray.length >= pointerCount,
          "pointerProperties array must be large enough to hold all pointers");
    return true;
  }

  private static boolean validatePointerCoordsObjArray(PointerCoords[] pointerCoordsObjArray,
      int pointerCount) {
    checkNotNull(pointerCoordsObjArray,
          "pointerCoords array must not be null");
    checkState(pointerCoordsObjArray.length >= pointerCount, "pointerCoords array must be large enough to hold all pointers");
    return true;
  }

//   static bool validatePointerIndex(JNIEnv* env, int pointerIndex, size_t pointerCount) {
//     if (pointerIndex < 0 || size_t(pointerIndex) >= pointerCount) {
//       jniThrowException(env, "java/lang/IllegalArgumentException",
//           "pointerIndex out of range");
//       return false;
//     }
//     return true;
//   }
//   static bool validateHistoryPos(JNIEnv* env, int historyPos, size_t historySize) {
//     if (historyPos < 0 || size_t(historyPos) >= historySize) {
//       jniThrowException(env, "java/lang/IllegalArgumentException",
//           "historyPos out of range");
//       return false;
//     }
//     return true;
//   }
//   static bool validatePointerCoords(JNIEnv* env, jobject pointerCoordsObj) {
//     if (!pointerCoordsObj) {
//       jniThrowException(env, "java/lang/IllegalArgumentException",
//           "pointerCoords must not be null");
//       return false;
//     }
//     return true;
//   }
//   static bool validatePointerProperties(JNIEnv* env, jobject pointerPropertiesObj) {
//     if (!pointerPropertiesObj) {
//       jniThrowException(env, "java/lang/IllegalArgumentException",
//           "pointerProperties must not be null");
//       return false;
//     }
//     return true;
//   }
  
  static NativeInput.PointerCoords pointerCoordsToNative(PointerCoords pointerCoordsObj,
      float xOffset, float yOffset) {
    NativeInput.PointerCoords outRawPointerCoords = new NativeInput.PointerCoords();
    outRawPointerCoords.clear();
    outRawPointerCoords.setAxisValue(AMOTION_EVENT_AXIS_X,
        pointerCoordsObj.x - xOffset);
    outRawPointerCoords.setAxisValue(AMOTION_EVENT_AXIS_Y,
        pointerCoordsObj.y - yOffset);
    outRawPointerCoords.setAxisValue(AMOTION_EVENT_AXIS_PRESSURE,
        pointerCoordsObj.pressure);
    outRawPointerCoords.setAxisValue(AMOTION_EVENT_AXIS_SIZE,
        pointerCoordsObj.size);
    outRawPointerCoords.setAxisValue(AMOTION_EVENT_AXIS_TOUCH_MAJOR,
        pointerCoordsObj.touchMajor);
    outRawPointerCoords.setAxisValue(AMOTION_EVENT_AXIS_TOUCH_MINOR,
        pointerCoordsObj.touchMinor);
    outRawPointerCoords.setAxisValue(AMOTION_EVENT_AXIS_TOOL_MAJOR,
        pointerCoordsObj.toolMajor);
    outRawPointerCoords.setAxisValue(AMOTION_EVENT_AXIS_TOOL_MINOR,
        pointerCoordsObj.toolMinor);
    outRawPointerCoords.setAxisValue(AMOTION_EVENT_AXIS_ORIENTATION,
        pointerCoordsObj.orientation);
    long packedAxisBits = ReflectionHelpers.getField(pointerCoordsObj, "mPackedAxisBits");
    NativeBitSet64 bits = new NativeBitSet64(packedAxisBits);
    if (!bits.isEmpty()) {
      float[] valuesArray = ReflectionHelpers.getField(pointerCoordsObj, "mPackedAxisValues");
      if (valuesArray != null) {
        //float* values = static_cast<float*>(
        //    env.GetPrimitiveArrayCritical(valuesArray, NULL));
        int index = 0;
        do {
          int axis = bits.clearFirstMarkedBit();
          outRawPointerCoords.setAxisValue(axis, valuesArray[index++]);
        } while (!bits.isEmpty());
        // env.ReleasePrimitiveArrayCritical(valuesArray, values, JNI_ABORT);
        // env.DeleteLocalRef(valuesArray);
      }
    }
    return outRawPointerCoords;
  }

//   static floatArray obtainPackedAxisValuesArray(JNIEnv* env, uint32_t minSize,
//       jobject outPointerCoordsObj) {
//     floatArray outValuesArray = floatArray(env.GetObjectField(outPointerCoordsObj,
//         gPointerCoordsClassInfo.mPackedAxisValues));
//     if (outValuesArray) {
//       uint32_t size = env.GetArrayLength(outValuesArray);
//       if (minSize <= size) {
//         return outValuesArray;
//       }
//       env.DeleteLocalRef(outValuesArray);
//     }
//     uint32_t size = 8;
//     while (size < minSize) {
//       size *= 2;
//     }
//     outValuesArray = env.NewFloatArray(size);
//     env.SetObjectField(outPointerCoordsObj,
//         gPointerCoordsClassInfo.mPackedAxisValues, outValuesArray);
//     return outValuesArray;
//   }
//   static void pointerCoordsFromNative(JNIEnv* env, const PointerCoords* rawPointerCoords,
//       float xOffset, float yOffset, jobject outPointerCoordsObj) {
//     env.SetFloatField(outPointerCoordsObj, gPointerCoordsClassInfo.x,
//         rawPointerCoords.getAxisValue(AMOTION_EVENT_AXIS_X) + xOffset);
//     env.SetFloatField(outPointerCoordsObj, gPointerCoordsClassInfo.y,
//         rawPointerCoords.getAxisValue(AMOTION_EVENT_AXIS_Y) + yOffset);
//     env.SetFloatField(outPointerCoordsObj, gPointerCoordsClassInfo.pressure,
//         rawPointerCoords.getAxisValue(AMOTION_EVENT_AXIS_PRESSURE));
//     env.SetFloatField(outPointerCoordsObj, gPointerCoordsClassInfo.size,
//         rawPointerCoords.getAxisValue(AMOTION_EVENT_AXIS_SIZE));
//     env.SetFloatField(outPointerCoordsObj, gPointerCoordsClassInfo.touchMajor,
//         rawPointerCoords.getAxisValue(AMOTION_EVENT_AXIS_TOUCH_MAJOR));
//     env.SetFloatField(outPointerCoordsObj, gPointerCoordsClassInfo.touchMinor,
//         rawPointerCoords.getAxisValue(AMOTION_EVENT_AXIS_TOUCH_MINOR));
//     env.SetFloatField(outPointerCoordsObj, gPointerCoordsClassInfo.toolMajor,
//         rawPointerCoords.getAxisValue(AMOTION_EVENT_AXIS_TOOL_MAJOR));
//     env.SetFloatField(outPointerCoordsObj, gPointerCoordsClassInfo.toolMinor,
//         rawPointerCoords.getAxisValue(AMOTION_EVENT_AXIS_TOOL_MINOR));
//     env.SetFloatField(outPointerCoordsObj, gPointerCoordsClassInfo.orientation,
//         rawPointerCoords.getAxisValue(AMOTION_EVENT_AXIS_ORIENTATION));
//     uint64_t outBits = 0;
//     BitSet64 bits = BitSet64(rawPointerCoords.bits);
//     bits.clearBit(AMOTION_EVENT_AXIS_X);
//     bits.clearBit(AMOTION_EVENT_AXIS_Y);
//     bits.clearBit(AMOTION_EVENT_AXIS_PRESSURE);
//     bits.clearBit(AMOTION_EVENT_AXIS_SIZE);
//     bits.clearBit(AMOTION_EVENT_AXIS_TOUCH_MAJOR);
//     bits.clearBit(AMOTION_EVENT_AXIS_TOUCH_MINOR);
//     bits.clearBit(AMOTION_EVENT_AXIS_TOOL_MAJOR);
//     bits.clearBit(AMOTION_EVENT_AXIS_TOOL_MINOR);
//     bits.clearBit(AMOTION_EVENT_AXIS_ORIENTATION);
//     if (!bits.isEmpty()) {
//       uint32_t packedAxesCount = bits.count();
//       floatArray outValuesArray = obtainPackedAxisValuesArray(env, packedAxesCount,
//           outPointerCoordsObj);
//       if (!outValuesArray) {
//         return; // OOM
//       }
//       float* outValues = static_cast<float*>(env.GetPrimitiveArrayCritical(
//           outValuesArray, NULL));
//       uint32_t index = 0;
//       do {
//         uint32_t axis = bits.clearFirstMarkedBit();
//         outBits |= BitSet64::valueForBit(axis);
//         outValues[index++] = rawPointerCoords.getAxisValue(axis);
//       } while (!bits.isEmpty());
//       env.ReleasePrimitiveArrayCritical(outValuesArray, outValues, 0);
//       env.DeleteLocalRef(outValuesArray);
//     }
//     env.SetLongField(outPointerCoordsObj, gPointerCoordsClassInfo.mPackedAxisBits, outBits);
//   }

//   // ----------------------------------------------------------------------------
//   static long nativeInitialize(JNIEnv* env, jclass clazz,
//       long nativePtr,
//       int deviceId, int source, int action, int flags, int edgeFlags,
//       int metaState, int buttonState,
//       float xOffset, float yOffset, float xPrecision, float yPrecision,
//       long downTimeNanos, long eventTimeNanos,
//       int pointerCount, jobjectArray pointerPropertiesObjArray,
//       jobjectArray pointerCoordsObjArray) {
//     if (!validatePointerCount(env, pointerCount)
//         || !validatePointerPropertiesArray(env, pointerPropertiesObjArray, pointerCount)
//         || !validatePointerCoordsObjArray(env, pointerCoordsObjArray, pointerCount)) {
//       return 0;
//     }
//     MotionEvent* event;
//     if (nativePtr) {
//       event = reinterpret_cast<MotionEvent*>(nativePtr);
//     } else {
//       event = new MotionEvent();
//     }
//     PointerProperties pointerProperties[pointerCount];
//     PointerCoords rawPointerCoords[pointerCount];
//     for (int i = 0; i < pointerCount; i++) {
//       jobject pointerPropertiesObj = env.GetObjectArrayElement(pointerPropertiesObjArray, i);
//       if (!pointerPropertiesObj) {
//             goto Error;
//       }
//       pointerPropertiesToNative(env, pointerPropertiesObj, &pointerProperties[i]);
//       env.DeleteLocalRef(pointerPropertiesObj);
//       jobject pointerCoordsObj = env.GetObjectArrayElement(pointerCoordsObjArray, i);
//       if (!pointerCoordsObj) {
//         jniThrowNullPointerException(env, "pointerCoords");
//             goto Error;
//       }
//       pointerCoordsToNative(env, pointerCoordsObj, xOffset, yOffset, &rawPointerCoords[i]);
//       env.DeleteLocalRef(pointerCoordsObj);
//     }
//     event.initialize(deviceId, source, action, 0, flags, edgeFlags, metaState, buttonState,
//         xOffset, yOffset, xPrecision, yPrecision,
//         downTimeNanos, eventTimeNanos, pointerCount, pointerProperties, rawPointerCoords);
//     return reinterpret_cast<long>(event);
//     Error:
//     if (!nativePtr) {
//       delete event;
//     }
//     return 0;
//   }
//   static void nativeDispose(JNIEnv* env, jclass clazz,
//       long nativePtr) {
//     NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
//     delete event;
//   }
//   static void nativeAddBatch(JNIEnv* env, jclass clazz,
//       long nativePtr, long eventTimeNanos, jobjectArray pointerCoordsObjArray,
//       int metaState) {
//     NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
//     size_t pointerCount = event.getPointerCount();
//     if (!validatePointerCoordsObjArray(env, pointerCoordsObjArray, pointerCount)) {
//       return;
//     }
//     PointerCoords rawPointerCoords[pointerCount];
//     for (size_t i = 0; i < pointerCount; i++) {
//       jobject pointerCoordsObj = env.GetObjectArrayElement(pointerCoordsObjArray, i);
//       if (!pointerCoordsObj) {
//         jniThrowNullPointerException(env, "pointerCoords");
//         return;
//       }
//       pointerCoordsToNative(env, pointerCoordsObj,
//           event.getXOffset(), event.getYOffset(), &rawPointerCoords[i]);
//       env.DeleteLocalRef(pointerCoordsObj);
//     }
//     event.addSample(eventTimeNanos, rawPointerCoords);
//     event.setMetaState(event.getMetaState() | metaState);
//   }
//   static void nativeGetPointerCoords(JNIEnv* env, jclass clazz,
//       long nativePtr, int pointerIndex, int historyPos, jobject outPointerCoordsObj) {
//     NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
//     size_t pointerCount = event.getPointerCount();
//     if (!validatePointerIndex(env, pointerIndex, pointerCount)
//         || !validatePointerCoords(env, outPointerCoordsObj)) {
//       return;
//     }
//     const PointerCoords* rawPointerCoords;
//     if (historyPos == HISTORY_CURRENT) {
//       rawPointerCoords = event.getRawPointerCoords(pointerIndex);
//     } else {
//       size_t historySize = event.getHistorySize();
//       if (!validateHistoryPos(env, historyPos, historySize)) {
//         return;
//       }
//       rawPointerCoords = event.getHistoricalRawPointerCoords(pointerIndex, historyPos);
//     }
//     pointerCoordsFromNative(env, rawPointerCoords, event.getXOffset(), event.getYOffset(),
//         outPointerCoordsObj);
//   }
//   static void nativeGetPointerProperties(JNIEnv* env, jclass clazz,
//       long nativePtr, int pointerIndex, jobject outPointerPropertiesObj) {
//     NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
//     size_t pointerCount = event.getPointerCount();
//     if (!validatePointerIndex(env, pointerIndex, pointerCount)
//         || !validatePointerProperties(env, outPointerPropertiesObj)) {
//       return;
//     }
//     const PointerProperties* pointerProperties = event.getPointerProperties(pointerIndex);
//     pointerPropertiesFromNative(env, pointerProperties, outPointerPropertiesObj);
//   }
//   static long nativeReadFromParcel(JNIEnv* env, jclass clazz,
//       long nativePtr, jobject parcelObj) {
//     NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
//     if (!event) {
//       event = new MotionEvent();
//     }
//     Parcel* parcel = parcelForJavaObject(env, parcelObj);
//     status_t status = event.readFromParcel(parcel);
//     if (status) {
//       if (!nativePtr) {
//         delete event;
//       }
//       jniThrowRuntimeException(env, "Failed to read MotionEvent parcel.");
//       return 0;
//     }
//     return reinterpret_cast<long>(event);
//   }
//   static void nativeWriteToParcel(JNIEnv* env, jclass clazz,
//       long nativePtr, jobject parcelObj) {
//     NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
//     Parcel* parcel = parcelForJavaObject(env, parcelObj);
//     status_t status = event.writeToParcel(parcel);
//     if (status) {
//       jniThrowRuntimeException(env, "Failed to write MotionEvent parcel.");
//     }
//   }
//   static jstring nativeAxisToString(JNIEnv* env, jclass clazz,
//       int axis) {
//     return env.NewStringUTF(MotionEvent::getLabel(static_cast<int32_t>(axis)));
//   }
//   static int nativeAxisFromString(JNIEnv* env, jclass clazz,
//       jstring label) {
//     ScopedUtfChars axisLabel(env, label);
//     return static_cast<int>(MotionEvent::getAxisFromLabel(axisLabel.c_str()));
//   }
//   // ---------------- @FastNative ----------------------------------
//   static int nativeGetPointerId(JNIEnv* env, jclass clazz,
//       long nativePtr, int pointerIndex) {
//     NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
//     size_t pointerCount = event.getPointerCount();
//     if (!validatePointerIndex(env, pointerIndex, pointerCount)) {
//       return -1;
//     }
//     return event.getPointerId(pointerIndex);
//   }
//   static int nativeGetToolType(JNIEnv* env, jclass clazz,
//       long nativePtr, int pointerIndex) {
//     NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
//     size_t pointerCount = event.getPointerCount();
//     if (!validatePointerIndex(env, pointerIndex, pointerCount)) {
//       return -1;
//     }
//     return event.getToolType(pointerIndex);
//   }
//   static long nativeGetEventTimeNanos(JNIEnv* env, jclass clazz,
//       long nativePtr, int historyPos) {
//     NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
//     if (historyPos == HISTORY_CURRENT) {
//       return event.getEventTime();
//     } else {
//       size_t historySize = event.getHistorySize();
//       if (!validateHistoryPos(env, historyPos, historySize)) {
//         return 0;
//       }
//       return event.getHistoricalEventTime(historyPos);
//     }
//   }
//   static float nativeGetRawAxisValue(JNIEnv* env, jclass clazz,
//       long nativePtr, int axis,
//       int pointerIndex, int historyPos) {
//     NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
//     size_t pointerCount = event.getPointerCount();
//     if (!validatePointerIndex(env, pointerIndex, pointerCount)) {
//       return 0;
//     }
//     if (historyPos == HISTORY_CURRENT) {
//       return event.getRawAxisValue(axis, pointerIndex);
//     } else {
//       size_t historySize = event.getHistorySize();
//       if (!validateHistoryPos(env, historyPos, historySize)) {
//         return 0;
//       }
//       return event.getHistoricalRawAxisValue(axis, pointerIndex, historyPos);
//     }
//   }
//   static float nativeGetAxisValue(JNIEnv* env, jclass clazz,
//       long nativePtr, int axis, int pointerIndex, int historyPos) {
//     NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
//     size_t pointerCount = event.getPointerCount();
//     if (!validatePointerIndex(env, pointerIndex, pointerCount)) {
//       return 0;
//     }
//     if (historyPos == HISTORY_CURRENT) {
//       return event.getAxisValue(axis, pointerIndex);
//     } else {
//       size_t historySize = event.getHistorySize();
//       if (!validateHistoryPos(env, historyPos, historySize)) {
//         return 0;
//       }
//       return event.getHistoricalAxisValue(axis, pointerIndex, historyPos);
//     }
//   }
//   // ----------------- @CriticalNative ------------------------------
//   @Implementation @HiddenApi static long nativeCopy(long destNativePtr, long sourceNativePtr,
//       boolean keepHistory) {
//     MotionEvent* destEvent = reinterpret_cast<MotionEvent*>(destNativePtr);
//     if (!destEvent) {
//       destEvent = new MotionEvent();
//     }
//     MotionEvent* sourceEvent = reinterpret_cast<MotionEvent*>(sourceNativePtr);
//     destEvent.copyFrom(sourceEvent, keepHistory);
//     return reinterpret_cast<long>(destEvent);
//   }
  @Implementation @HiddenApi static int nativeGetDeviceId(long nativePtr) {
    NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
    return event.getDeviceId();
  }
  @Implementation @HiddenApi static int nativeGetSource(long nativePtr) {
    NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
    return event.getSource();
  }
  @Implementation @HiddenApi static void nativeSetSource(long nativePtr, int source) {
    NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
    event.setSource(source);
  }
  @Implementation @HiddenApi static int nativeGetAction(long nativePtr) {
    NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
    return event.getAction();
  }
  @Implementation @HiddenApi static void nativeSetAction(long nativePtr, int action) {
    NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
    event.setAction(action);
  }
  @Implementation @HiddenApi static int nativeGetActionButton(long nativePtr) {
    NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
    return event.getActionButton();
  }
  @Implementation @HiddenApi static void nativeSetActionButton(long nativePtr, int button) {
    NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
    event.setActionButton(button);
  }
  @Implementation @HiddenApi static boolean nativeIsTouchEvent(long nativePtr) {
    NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
    return event.isTouchEvent();
  }
  @Implementation @HiddenApi static int nativeGetFlags(long nativePtr) {
    NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
    return event.getFlags();
  }
  @Implementation @HiddenApi static void nativeSetFlags(long nativePtr, int flags) {
    NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
    event.setFlags(flags);
  }
  @Implementation @HiddenApi static int nativeGetEdgeFlags(long nativePtr) {
    NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
    return event.getEdgeFlags();
  }
  @Implementation @HiddenApi static void nativeSetEdgeFlags(long nativePtr, int edgeFlags) {
    NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
    event.setEdgeFlags(edgeFlags);
  }
  @Implementation @HiddenApi static int nativeGetMetaState(long nativePtr) {
    NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
    return event.getMetaState();
  }
  @Implementation @HiddenApi static int nativeGetButtonState(long nativePtr) {
    NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
    return event.getButtonState();
  }
  @Implementation @HiddenApi static void nativeSetButtonState(long nativePtr, int buttonState) {
    NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
    event.setButtonState(buttonState);
  }
  @Implementation @HiddenApi static void nativeOffsetLocation(long nativePtr, float deltaX,
      float deltaY) {
    NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
    return event.offsetLocation(deltaX, deltaY);
  }
  @Implementation @HiddenApi static float nativeGetXOffset(long nativePtr) {
    NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
    return event.getXOffset();
  }
  @Implementation @HiddenApi static float nativeGetYOffset(long nativePtr) {
    NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
    return event.getYOffset();
  }
  @Implementation @HiddenApi static float nativeGetXPrecision(long nativePtr) {
    NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
    return event.getXPrecision();
  }
  @Implementation @HiddenApi static float nativeGetYPrecision(long nativePtr) {
    NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
    return event.getYPrecision();
  }
  @Implementation @HiddenApi protected static long nativeGetDownTimeNanos(long nativePtr) {
    NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
    return event.getDownTime();
  }
//   @Implementation @HiddenApi static void nativeSetDownTimeNanos(long nativePtr, long downTimeNanos) {
//     NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
//     event.setDownTime(downTimeNanos);
//   }
//   @Implementation @HiddenApi static int nativeGetPointerCount(long nativePtr) {
//     NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
//     return int(event.getPointerCount());
//   }
//   @Implementation @HiddenApi static int nativeFindPointerIndex(long nativePtr, int pointerId) {
//     NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
//     return int(event.findPointerIndex(pointerId));
//   }
//   @Implementation @HiddenApi static int nativeGetHistorySize(long nativePtr) {
//     NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
//     return int(event.getHistorySize());
//   }
//   @Implementation @HiddenApi static void nativeScale(long nativePtr, float scale) {
//     NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
//     event.scale(scale);
//   }
//   @Implementation @HiddenApi static void nativeTransform(long nativePtr, long matrixPtr) {
//     NativeInput.MotionEvent event = nativeObjRegistry.getNativeObject(nativePtr);
//     SkMatrix* matrix = reinterpret_cast<SkMatrix*>(matrixPtr);
//     static_assert(SkMatrix::kMScaleX == 0, "SkMatrix unexpected index");
//     static_assert(SkMatrix::kMSkewX == 1, "SkMatrix unexpected index");
//     static_assert(SkMatrix::kMTransX == 2, "SkMatrix unexpected index");
//     static_assert(SkMatrix::kMSkewY == 3, "SkMatrix unexpected index");
//     static_assert(SkMatrix::kMScaleY == 4, "SkMatrix unexpected index");
//     static_assert(SkMatrix::kMTransY == 5, "SkMatrix unexpected index");
//     static_assert(SkMatrix::kMPersp0 == 6, "SkMatrix unexpected index");
//     static_assert(SkMatrix::kMPersp1 == 7, "SkMatrix unexpected index");
//     static_assert(SkMatrix::kMPersp2 == 8, "SkMatrix unexpected index");
//     float m[9];
//     matrix.get9(m);
//     event.transform(m);
//   }
// // ----------------------------------------------------------------------------
//   static const JNINativeMethod gMotionEventMethods[] = {
//     /* name, signature, funcPtr */
//       { "nativeInitialize",
//           "(JIIIIIIIFFFFJJI[Landroid/view/MotionEvent$PointerProperties;"
//           "[Landroid/view/MotionEvent$PointerCoords;)J",
//           (void*)nativeInitialize },
//     { "nativeDispose",
//     "(J)V",
//     (void*)nativeDispose },
//     { "nativeAddBatch",
//     "(JJ[Landroid/view/MotionEvent$PointerCoords;I)V",
//     (void*)nativeAddBatch },
//     { "nativeReadFromParcel",
//     "(JLandroid/os/Parcel;)J",
//     (void*)nativeReadFromParcel },
//     { "nativeWriteToParcel",
//     "(JLandroid/os/Parcel;)V",
//     (void*)nativeWriteToParcel },
//     { "nativeAxisToString", "(I)Ljava/lang/String;",
//     (void*)nativeAxisToString },
//     { "nativeAxisFromString", "(Ljava/lang/String;)I",
//     (void*)nativeAxisFromString },
//     { "nativeGetPointerProperties",
//     "(JILandroid/view/MotionEvent$PointerProperties;)V",
//     (void*)nativeGetPointerProperties },
//     { "nativeGetPointerCoords",
//     "(JIILandroid/view/MotionEvent$PointerCoords;)V",
//     (void*)nativeGetPointerCoords },
//     // --------------- @FastNative ----------------------
//     { "nativeGetPointerId",
//     "(JI)I",
//     (void*)nativeGetPointerId },
//     { "nativeGetToolType",
//     "(JI)I",
//     (void*)nativeGetToolType },
//     { "nativeGetEventTimeNanos",
//     "(JI)J",
//     (void*)nativeGetEventTimeNanos },
//     { "nativeGetRawAxisValue",
//     "(JIII)F",
//     (void*)nativeGetRawAxisValue },
//     { "nativeGetAxisValue",
//     "(JIII)F",
//     (void*)nativeGetAxisValue },
//     // --------------- @CriticalNative ------------------
//     { "nativeCopy",
//     "(JJZ)J",
//     (void*)nativeCopy },
//     { "nativeGetDeviceId",
//     "(J)I",
//     (void*)nativeGetDeviceId },
//     { "nativeGetSource",
//     "(J)I",
//     (void*)nativeGetSource },
//     { "nativeSetSource",
//     "(JI)I",
//     (void*)nativeSetSource },
//     { "nativeGetAction",
//     "(J)I",
//     (void*)nativeGetAction },
//     { "nativeSetAction",
//     "(JI)V",
//     (void*)nativeSetAction },
//     { "nativeGetActionButton",
//     "(J)I",
//     (void*)nativeGetActionButton},
//     { "nativeSetActionButton",
//     "(JI)V",
//     (void*)nativeSetActionButton},
//     { "nativeIsTouchEvent",
//     "(J)Z",
//     (void*)nativeIsTouchEvent },
//     { "nativeGetFlags",
//     "(J)I",
//     (void*)nativeGetFlags },
//     { "nativeSetFlags",
//     "(JI)V",
//     (void*)nativeSetFlags },
//     { "nativeGetEdgeFlags",
//     "(J)I",
//     (void*)nativeGetEdgeFlags },
//     { "nativeSetEdgeFlags",
//     "(JI)V",
//     (void*)nativeSetEdgeFlags },
//     { "nativeGetMetaState",
//     "(J)I",
//     (void*)nativeGetMetaState },
//     { "nativeGetButtonState",
//     "(J)I",
//     (void*)nativeGetButtonState },
//     { "nativeSetButtonState",
//     "(JI)V",
//     (void*)nativeSetButtonState },
//     { "nativeOffsetLocation",
//     "(JFF)V",
//     (void*)nativeOffsetLocation },
//     { "nativeGetXOffset",
//     "(J)F",
//     (void*)nativeGetXOffset },
//     { "nativeGetYOffset",
//     "(J)F",
//     (void*)nativeGetYOffset },
//     { "nativeGetXPrecision",
//     "(J)F",
//     (void*)nativeGetXPrecision },
//     { "nativeGetYPrecision",
//     "(J)F",
//     (void*)nativeGetYPrecision },
//     { "nativeGetDownTimeNanos",
//     "(J)J",
//     (void*)nativeGetDownTimeNanos },
//     { "nativeSetDownTimeNanos",
//     "(JJ)V",
//     (void*)nativeSetDownTimeNanos },
//     { "nativeGetPointerCount",
//     "(J)I",
//     (void*)nativeGetPointerCount },
//     { "nativeFindPointerIndex",
//     "(JI)I",
//     (void*)nativeFindPointerIndex },
//     { "nativeGetHistorySize",
//     "(J)I",
//     (void*)nativeGetHistorySize },
//     { "nativeScale",
//     "(JF)V",
//     (void*)nativeScale },
//     { "nativeTransform",
//     "(JJ)V",
//     (void*)nativeTransform },
//     };
//     int register_android_view_MotionEvent(JNIEnv* env) {
//     int res = RegisterMethodsOrDie(env, "android/view/MotionEvent", gMotionEventMethods,
//     NELEM(gMotionEventMethods));
//     gMotionEventClassInfo.clazz = FindClassOrDie(env, "android/view/MotionEvent");
//     gMotionEventClassInfo.clazz = MakeGlobalRefOrDie(env, gMotionEventClassInfo.clazz);
//     gMotionEventClassInfo.obtain = GetStaticMethodIDOrDie(env, gMotionEventClassInfo.clazz,
//     "obtain", "()Landroid/view/MotionEvent;");
//     gMotionEventClassInfo.recycle = GetMethodIDOrDie(env, gMotionEventClassInfo.clazz,
//     "recycle", "()V");
//     gMotionEventClassInfo.mNativePtr = GetFieldIDOrDie(env, gMotionEventClassInfo.clazz,
//     "mNativePtr", "J");
//     jclass clazz = FindClassOrDie(env, "android/view/MotionEvent$PointerCoords");
//     gPointerCoordsClassInfo.mPackedAxisBits = GetFieldIDOrDie(env, clazz, "mPackedAxisBits", "J");
//     gPointerCoordsClassInfo.mPackedAxisValues = GetFieldIDOrDie(env, clazz, "mPackedAxisValues",
//     "[F");
//     gPointerCoordsClassInfo.x = GetFieldIDOrDie(env, clazz, "x", "F");
//     gPointerCoordsClassInfo.y = GetFieldIDOrDie(env, clazz, "y", "F");
//     gPointerCoordsClassInfo.pressure = GetFieldIDOrDie(env, clazz, "pressure", "F");
//     gPointerCoordsClassInfo.size = GetFieldIDOrDie(env, clazz, "size", "F");
//     gPointerCoordsClassInfo.touchMajor = GetFieldIDOrDie(env, clazz, "touchMajor", "F");
//     gPointerCoordsClassInfo.touchMinor = GetFieldIDOrDie(env, clazz, "touchMinor", "F");
//     gPointerCoordsClassInfo.toolMajor = GetFieldIDOrDie(env, clazz, "toolMajor", "F");
//     gPointerCoordsClassInfo.toolMinor = GetFieldIDOrDie(env, clazz, "toolMinor", "F");
//     gPointerCoordsClassInfo.orientation = GetFieldIDOrDie(env, clazz, "orientation", "F");
//     clazz = FindClassOrDie(env, "android/view/MotionEvent$PointerProperties");
//     gPointerPropertiesClassInfo.id = GetFieldIDOrDie(env, clazz, "id", "I");
//     gPointerPropertiesClassInfo.toolType = GetFieldIDOrDie(env, clazz, "toolType", "I");
//     return res;
//     }

  // Testing API methods

  public void setPointer2(float pointer1X, float pointer1Y) {
    throw new UnsupportedOperationException();
  }

  public void setPointerIndex(int i) {
    throw new UnsupportedOperationException();
  }

  public void setPointerIds(int i, int i1) {
    throw new UnsupportedOperationException();
  }

}
