package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S_V2;

import android.database.sqlite.SQLiteConnection;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.SQLiteConnectionNatives;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.versioning.AndroidVersions.T;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link SQLiteConnection} that is backed by native code */
@Implements(
    className = "android.database.sqlite.SQLiteConnection",
    isInAndroidSdk = false,
    callNativeMethodsByDefault = true)
public class ShadowNativeSQLiteConnection extends ShadowSQLiteConnection {
  @Implementation(maxSdk = O)
  protected static Number nativeOpen(
      String path, int openFlags, String label, boolean enableTrace, boolean enableProfile) {
    return nativeOpen(path, openFlags, label, enableTrace, enableProfile, 0, 0);
  }

  @Implementation(minSdk = O_MR1, maxSdk = U.SDK_INT)
  protected static long nativeOpen(
      String path,
      int openFlags,
      String label,
      boolean enableTrace,
      boolean enableProfile,
      int lookasideSlotSize,
      int lookasideSlotCount) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () ->
                SQLiteConnectionNatives.nativeOpen(
                    path,
                    openFlags,
                    label,
                    enableTrace,
                    enableProfile,
                    lookasideSlotSize,
                    lookasideSlotCount));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nativeClose(long connectionPtr) {
    PerfStatsCollector.getInstance()
        .measure("androidsqlite", () -> SQLiteConnectionNatives.nativeClose(connectionPtr));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nativePrepareStatement(long connectionPtr, String sql) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativePrepareStatement(connectionPtr, sql));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nativeFinalizeStatement(long connectionPtr, long statementPtr) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeFinalizeStatement(connectionPtr, statementPtr));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static int nativeGetParameterCount(final long connectionPtr, final long statementPtr) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeGetParameterCount(connectionPtr, statementPtr));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static boolean nativeIsReadOnly(final long connectionPtr, final long statementPtr) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeIsReadOnly(connectionPtr, statementPtr));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static String nativeExecuteForString(
      final long connectionPtr, final long statementPtr) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeExecuteForString(connectionPtr, statementPtr));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nativeRegisterLocalizedCollators(long connectionPtr, String locale) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeRegisterLocalizedCollators(connectionPtr, locale));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nativeExecuteForLong(final long connectionPtr, final long statementPtr) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeExecuteForLong(connectionPtr, statementPtr));
  }

  @Implementation(maxSdk = S_V2)
  protected static void nativeExecute(final long connectionPtr, final long statementPtr) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeExecute(connectionPtr, statementPtr, false));
  }

  @Implementation(minSdk = T.SDK_INT, maxSdk = U.SDK_INT)
  protected static void nativeExecute(
      final long connectionPtr, final long statementPtr, boolean isPragmaStmt) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeExecute(connectionPtr, statementPtr, isPragmaStmt));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static int nativeExecuteForChangedRowCount(
      final long connectionPtr, final long statementPtr) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () ->
                SQLiteConnectionNatives.nativeExecuteForChangedRowCount(
                    connectionPtr, statementPtr));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static int nativeGetColumnCount(final long connectionPtr, final long statementPtr) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeGetColumnCount(connectionPtr, statementPtr));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static String nativeGetColumnName(
      final long connectionPtr, final long statementPtr, final int index) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeGetColumnName(connectionPtr, statementPtr, index));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nativeBindNull(
      final long connectionPtr, final long statementPtr, final int index) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeBindNull(connectionPtr, statementPtr, index));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nativeBindLong(
      final long connectionPtr, final long statementPtr, final int index, final long value) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () ->
                SQLiteConnectionNatives.nativeBindLong(connectionPtr, statementPtr, index, value));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nativeBindDouble(
      final long connectionPtr, final long statementPtr, final int index, final double value) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () ->
                SQLiteConnectionNatives.nativeBindDouble(
                    connectionPtr, statementPtr, index, value));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nativeBindString(
      final long connectionPtr, final long statementPtr, final int index, final String value) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () ->
                SQLiteConnectionNatives.nativeBindString(
                    connectionPtr, statementPtr, index, value));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nativeBindBlob(
      final long connectionPtr, final long statementPtr, final int index, final byte[] value) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () ->
                SQLiteConnectionNatives.nativeBindBlob(connectionPtr, statementPtr, index, value));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nativeResetStatementAndClearBindings(
      final long connectionPtr, final long statementPtr) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () ->
                SQLiteConnectionNatives.nativeResetStatementAndClearBindings(
                    connectionPtr, statementPtr));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nativeExecuteForLastInsertedRowId(
      final long connectionPtr, final long statementPtr) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () ->
                SQLiteConnectionNatives.nativeExecuteForLastInsertedRowId(
                    connectionPtr, statementPtr));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static long nativeExecuteForCursorWindow(
      final long connectionPtr,
      final long statementPtr,
      final long windowPtr,
      final int startPos,
      final int requiredPos,
      final boolean countAllRows) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () ->
                SQLiteConnectionNatives.nativeExecuteForCursorWindow(
                    connectionPtr, statementPtr, windowPtr, startPos, requiredPos, countAllRows));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static int nativeExecuteForBlobFileDescriptor(
      final long connectionPtr, final long statementPtr) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () ->
                SQLiteConnectionNatives.nativeExecuteForBlobFileDescriptor(
                    connectionPtr, statementPtr));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nativeCancel(long connectionPtr) {
    PerfStatsCollector.getInstance()
        .measure("androidsqlite", () -> SQLiteConnectionNatives.nativeCancel(connectionPtr));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static void nativeResetCancel(long connectionPtr, boolean cancelable) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeResetCancel(connectionPtr, cancelable));
  }

  @Implementation(minSdk = R, maxSdk = U.SDK_INT)
  @SuppressWarnings("AndroidJdkLibsChecker")
  protected static void nativeRegisterCustomScalarFunction(
      long connectionPtr, String name, UnaryOperator<String> function) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () ->
                SQLiteConnectionNatives.nativeRegisterCustomScalarFunction(
                    connectionPtr, name, function));
  }

  @Implementation(minSdk = R, maxSdk = U.SDK_INT)
  @SuppressWarnings("AndroidJdkLibsChecker")
  protected static void nativeRegisterCustomAggregateFunction(
      long connectionPtr, String name, BinaryOperator<String> function) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () ->
                SQLiteConnectionNatives.nativeRegisterCustomAggregateFunction(
                    connectionPtr, name, function));
  }

  @Implementation(maxSdk = U.SDK_INT)
  protected static int nativeGetDbLookaside(long connectionPtr) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite", () -> SQLiteConnectionNatives.nativeGetDbLookaside(connectionPtr));
  }
}
