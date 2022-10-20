package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.database.CharArrayBuffer;
import android.database.CursorWindow;
import com.google.common.base.Preconditions;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.CursorWindowNatives;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;

/** Shadow for {@link CursorWindow} that is backed by native code */
@Implements(value = CursorWindow.class, isInAndroidSdk = false)
public class ShadowNativeCursorWindow extends ShadowCursorWindow {

  @Implementation
  protected static Number nativeCreate(String name, int cursorWindowSize) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    long result = CursorWindowNatives.nativeCreate(name, cursorWindowSize);
    if (RuntimeEnvironment.getApiLevel() < LOLLIPOP) {
      return PreLPointers.register(result);
    }
    return result;
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static void nativeDispose(int windowPtr) {
    nativeDispose(PreLPointers.get(windowPtr));
    PreLPointers.remove(windowPtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeDispose(long windowPtr) {
    CursorWindowNatives.nativeDispose(windowPtr);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static String nativeGetName(int windowPtr) {
    return nativeGetName(PreLPointers.get(windowPtr));
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static String nativeGetName(long windowPtr) {
    return CursorWindowNatives.nativeGetName(windowPtr);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static byte[] nativeGetBlob(int windowPtr, int row, int column) {
    return nativeGetBlob(PreLPointers.get(windowPtr), row, column);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static byte[] nativeGetBlob(long windowPtr, int row, int column) {
    return CursorWindowNatives.nativeGetBlob(windowPtr, row, column);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static String nativeGetString(int windowPtr, int row, int column) {
    return nativeGetString(PreLPointers.get(windowPtr), row, column);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static String nativeGetString(long windowPtr, int row, int column) {
    return CursorWindowNatives.nativeGetString(windowPtr, row, column);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeCopyStringToBuffer(
      long windowPtr, int row, int column, CharArrayBuffer buffer) {
    CursorWindowNatives.nativeCopyStringToBuffer(windowPtr, row, column, buffer);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static boolean nativePutBlob(int windowPtr, byte[] value, int row, int column) {
    return nativePutBlob(PreLPointers.get(windowPtr), value, row, column);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static boolean nativePutBlob(long windowPtr, byte[] value, int row, int column) {
    // Real Android will crash in native code if putBlob is called with a null value.
    Preconditions.checkNotNull(value);
    return CursorWindowNatives.nativePutBlob(windowPtr, value, row, column);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static boolean nativePutString(int windowPtr, String value, int row, int column) {
    return nativePutString(PreLPointers.get(windowPtr), value, row, column);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static boolean nativePutString(long windowPtr, String value, int row, int column) {
    // Real Android will crash in native code if putString is called with a null value.
    Preconditions.checkNotNull(value);
    return CursorWindowNatives.nativePutString(windowPtr, value, row, column);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static void nativeClear(int windowPtr) {
    nativeClear(PreLPointers.get(windowPtr));
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeClear(long windowPtr) {
    CursorWindowNatives.nativeClear(windowPtr);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static int nativeGetNumRows(int windowPtr) {
    return nativeGetNumRows(PreLPointers.get(windowPtr));
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static int nativeGetNumRows(long windowPtr) {
    return CursorWindowNatives.nativeGetNumRows(windowPtr);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static boolean nativeSetNumColumns(int windowPtr, int columnNum) {
    return nativeSetNumColumns(PreLPointers.get(windowPtr), columnNum);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static boolean nativeSetNumColumns(long windowPtr, int columnNum) {
    return CursorWindowNatives.nativeSetNumColumns(windowPtr, columnNum);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static boolean nativeAllocRow(int windowPtr) {
    return nativeAllocRow(PreLPointers.get(windowPtr));
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static boolean nativeAllocRow(long windowPtr) {
    return CursorWindowNatives.nativeAllocRow(windowPtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeFreeLastRow(long windowPtr) {
    CursorWindowNatives.nativeFreeLastRow(windowPtr);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static int nativeGetType(int windowPtr, int row, int column) {
    return nativeGetType(PreLPointers.get(windowPtr), row, column);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static int nativeGetType(long windowPtr, int row, int column) {
    return CursorWindowNatives.nativeGetType(windowPtr, row, column);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static long nativeGetLong(int windowPtr, int row, int column) {
    return nativeGetLong(PreLPointers.get(windowPtr), row, column);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static long nativeGetLong(long windowPtr, int row, int column) {
    return CursorWindowNatives.nativeGetLong(windowPtr, row, column);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static double nativeGetDouble(int windowPtr, int row, int column) {
    return nativeGetDouble(PreLPointers.get(windowPtr), row, column);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static double nativeGetDouble(long windowPtr, int row, int column) {
    return CursorWindowNatives.nativeGetDouble(windowPtr, row, column);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static boolean nativePutLong(int windowPtr, long value, int row, int column) {
    return nativePutLong(PreLPointers.get(windowPtr), value, row, column);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static boolean nativePutLong(long windowPtr, long value, int row, int column) {
    return CursorWindowNatives.nativePutLong(windowPtr, value, row, column);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static boolean nativePutDouble(int windowPtr, double value, int row, int column) {
    return nativePutDouble(PreLPointers.get(windowPtr), value, row, column);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static boolean nativePutDouble(long windowPtr, double value, int row, int column) {
    return CursorWindowNatives.nativePutDouble(windowPtr, value, row, column);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static boolean nativePutNull(int windowPtr, int row, int column) {
    return nativePutNull(PreLPointers.get(windowPtr), row, column);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static boolean nativePutNull(long windowPtr, int row, int column) {
    return CursorWindowNatives.nativePutNull(windowPtr, row, column);
  }
}
