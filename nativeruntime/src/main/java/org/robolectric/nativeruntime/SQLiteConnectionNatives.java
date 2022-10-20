/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.robolectric.nativeruntime;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/**
 * Native methods for SQLiteConnection JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-11.0.0_r1:frameworks/base/core/java/android/database/sqlite/SQLiteConnection.java
 */
public class SQLiteConnectionNatives {

  private SQLiteConnectionNatives() {}

  public static native long nativeOpen(
      String path,
      int openFlags,
      String label,
      boolean enableTrace,
      boolean enableProfile,
      int lookasideSlotSize,
      int lookasideSlotCount);

  public static native void nativeClose(long connectionPtr);

  public static native void nativeRegisterCustomScalarFunction(
      long connectionPtr, String name, UnaryOperator<String> function);

  public static native void nativeRegisterCustomAggregateFunction(
      long connectionPtr, String name, BinaryOperator<String> function);

  public static native void nativeRegisterLocalizedCollators(long connectionPtr, String locale);

  public static native long nativePrepareStatement(long connectionPtr, String sql);

  public static native void nativeFinalizeStatement(long connectionPtr, long statementPtr);

  public static native int nativeGetParameterCount(long connectionPtr, long statementPtr);

  public static native boolean nativeIsReadOnly(long connectionPtr, long statementPtr);

  public static native int nativeGetColumnCount(long connectionPtr, long statementPtr);

  public static native String nativeGetColumnName(long connectionPtr, long statementPtr, int index);

  public static native void nativeBindNull(long connectionPtr, long statementPtr, int index);

  public static native void nativeBindLong(
      long connectionPtr, long statementPtr, int index, long value);

  public static native void nativeBindDouble(
      long connectionPtr, long statementPtr, int index, double value);

  public static native void nativeBindString(
      long connectionPtr, long statementPtr, int index, String value);

  public static native void nativeBindBlob(
      long connectionPtr, long statementPtr, int index, byte[] value);

  public static native void nativeResetStatementAndClearBindings(
      long connectionPtr, long statementPtr);

  public static native void nativeExecute(
      long connectionPtr, long statementPtr, boolean isPragmaStmt);

  public static native long nativeExecuteForLong(long connectionPtr, long statementPtr);

  public static native String nativeExecuteForString(long connectionPtr, long statementPtr);

  public static native int nativeExecuteForBlobFileDescriptor(
      long connectionPtr, long statementPtr);

  public static native int nativeExecuteForChangedRowCount(long connectionPtr, long statementPtr);

  public static native long nativeExecuteForLastInsertedRowId(
      long connectionPtr, long statementPtr);

  public static native long nativeExecuteForCursorWindow(
      long connectionPtr,
      long statementPtr,
      long windowPtr,
      int startPos,
      int requiredPos,
      boolean countAllRows);

  public static native int nativeGetDbLookaside(long connectionPtr);

  public static native void nativeCancel(long connectionPtr);

  public static native void nativeResetCancel(long connectionPtr, boolean cancelable);
}
