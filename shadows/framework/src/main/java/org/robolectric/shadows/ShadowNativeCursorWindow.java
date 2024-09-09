package org.robolectric.shadows;

import static org.robolectric.RuntimeEnvironment.getApiLevel;

import android.database.CharArrayBuffer;
import android.database.CursorWindow;
import com.google.common.base.Preconditions;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.CursorWindowNatives;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link CursorWindow} that is backed by native code */
@Implements(value = CursorWindow.class, isInAndroidSdk = false, callNativeMethodsByDefault = true)
public class ShadowNativeCursorWindow extends ShadowCursorWindow {

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nativeCreate(String name, int cursorWindowSize) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return CursorWindowNatives.nativeCreate(name, cursorWindowSize);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nativeDispose(long windowPtr) {
    CursorWindowNatives.nativeDispose(windowPtr);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static String nativeGetName(long windowPtr) {
    return CursorWindowNatives.nativeGetName(windowPtr);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static byte[] nativeGetBlob(long windowPtr, int row, int column) {
    return CursorWindowNatives.nativeGetBlob(windowPtr, row, column);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static String nativeGetString(long windowPtr, int row, int column) {
    return CursorWindowNatives.nativeGetString(windowPtr, row, column);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nativeCopyStringToBuffer(
      long windowPtr, int row, int column, CharArrayBuffer buffer) {
    CursorWindowNatives.nativeCopyStringToBuffer(windowPtr, row, column, buffer);
  }

  @Implementation
  protected static boolean nativePutBlob(long windowPtr, byte[] value, int row, int column) {
    // Real Android will crash in native code if putBlob is called with a null value.
    Preconditions.checkNotNull(value);
    if (getApiLevel() <= U.SDK_INT) {
      return CursorWindowNatives.nativePutBlob(windowPtr, value, row, column);
    } else {
      // directly call the real native method, renamed by during instrumentation
      return ReflectionHelpers.callStaticMethod(
          CursorWindow.class,
          Shadow.directNativeMethodName(CursorWindow.class.getName(), "nativePutBlob"),
          ClassParameter.from(long.class, windowPtr),
          ClassParameter.from(byte[].class, value),
          ClassParameter.from(int.class, row),
          ClassParameter.from(int.class, column));
    }
  }

  @Implementation
  protected static boolean nativePutString(long windowPtr, String value, int row, int column) {
    // Real Android will crash in native code if putString is called with a null value.
    Preconditions.checkNotNull(value);
    if (getApiLevel() <= U.SDK_INT) {
      return CursorWindowNatives.nativePutString(windowPtr, value, row, column);
    } else {
      // directly call the real native method, renamed by during instrumentation
      return ReflectionHelpers.callStaticMethod(
          CursorWindow.class,
          Shadow.directNativeMethodName(CursorWindow.class.getName(), "nativePutString"),
          ClassParameter.from(long.class, windowPtr),
          ClassParameter.from(String.class, value),
          ClassParameter.from(int.class, row),
          ClassParameter.from(int.class, column));
    }
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nativeClear(long windowPtr) {
    CursorWindowNatives.nativeClear(windowPtr);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static int nativeGetNumRows(long windowPtr) {
    return CursorWindowNatives.nativeGetNumRows(windowPtr);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static boolean nativeSetNumColumns(long windowPtr, int columnNum) {
    return CursorWindowNatives.nativeSetNumColumns(windowPtr, columnNum);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static boolean nativeAllocRow(long windowPtr) {
    return CursorWindowNatives.nativeAllocRow(windowPtr);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nativeFreeLastRow(long windowPtr) {
    CursorWindowNatives.nativeFreeLastRow(windowPtr);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static int nativeGetType(long windowPtr, int row, int column) {
    return CursorWindowNatives.nativeGetType(windowPtr, row, column);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nativeGetLong(long windowPtr, int row, int column) {
    return CursorWindowNatives.nativeGetLong(windowPtr, row, column);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static double nativeGetDouble(long windowPtr, int row, int column) {
    return CursorWindowNatives.nativeGetDouble(windowPtr, row, column);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static boolean nativePutLong(long windowPtr, long value, int row, int column) {
    return CursorWindowNatives.nativePutLong(windowPtr, value, row, column);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static boolean nativePutDouble(long windowPtr, double value, int row, int column) {
    return CursorWindowNatives.nativePutDouble(windowPtr, value, row, column);
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static boolean nativePutNull(long windowPtr, int row, int column) {
    return CursorWindowNatives.nativePutNull(windowPtr, row, column);
  }
}
