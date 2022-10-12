package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S_V2;

import android.database.sqlite.SQLiteConnection;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.SQLiteConnectionNatives;
import org.robolectric.util.PerfStatsCollector;

/** Shadow for {@link SQLiteConnection} that is backed by native code */
@Implements(className = "android.database.sqlite.SQLiteConnection", isInAndroidSdk = false)
public class ShadowNativeSQLiteConnection extends ShadowSQLiteConnection {
  @Implementation(maxSdk = O)
  protected static Number nativeOpen(
      String path, int openFlags, String label, boolean enableTrace, boolean enableProfile) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> {
              long result =
                  SQLiteConnectionNatives.nativeOpen(
                      path, openFlags, label, enableTrace, enableProfile, 0, 0);
              if (RuntimeEnvironment.getApiLevel() < LOLLIPOP) {
                return PreLPointers.register(result);
              }
              return result;
            });
  }

  @Implementation(minSdk = O_MR1)
  protected static long nativeOpen(
      String path,
      int openFlags,
      String label,
      boolean enableTrace,
      boolean enableProfile,
      int lookasideSlotSize,
      int lookasideSlotCount) {
    return nativeOpen(path, openFlags, label, enableTrace, enableProfile).longValue();
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static void nativeClose(int connectionPtr) {
    nativeClose(PreLPointers.get(connectionPtr));
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeClose(long connectionPtr) {
    PerfStatsCollector.getInstance()
        .measure("androidsqlite", () -> SQLiteConnectionNatives.nativeClose(connectionPtr));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static int nativePrepareStatement(int connectionPtr, String sql) {
    long statementPtr = nativePrepareStatement(PreLPointers.get(connectionPtr), sql);
    return PreLPointers.register(statementPtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static long nativePrepareStatement(long connectionPtr, String sql) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativePrepareStatement(connectionPtr, sql));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static void nativeFinalizeStatement(int connectionPtr, int statementPtr) {
    nativeFinalizeStatement(PreLPointers.get(connectionPtr), PreLPointers.get(statementPtr));
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeFinalizeStatement(long connectionPtr, long statementPtr) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeFinalizeStatement(connectionPtr, statementPtr));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static int nativeGetParameterCount(int connectionPtr, int statementPtr) {
    return nativeGetParameterCount(PreLPointers.get(connectionPtr), PreLPointers.get(statementPtr));
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static int nativeGetParameterCount(final long connectionPtr, final long statementPtr) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeGetParameterCount(connectionPtr, statementPtr));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static boolean nativeIsReadOnly(int connectionPtr, int statementPtr) {
    return nativeIsReadOnly(PreLPointers.get(connectionPtr), PreLPointers.get(statementPtr));
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static boolean nativeIsReadOnly(final long connectionPtr, final long statementPtr) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeIsReadOnly(connectionPtr, statementPtr));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static String nativeExecuteForString(int connectionPtr, int statementPtr) {
    return nativeExecuteForString(PreLPointers.get(connectionPtr), PreLPointers.get(statementPtr));
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static String nativeExecuteForString(
      final long connectionPtr, final long statementPtr) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeExecuteForString(connectionPtr, statementPtr));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static void nativeRegisterLocalizedCollators(int connectionPtr, String locale) {
    nativeRegisterLocalizedCollators(PreLPointers.get(connectionPtr), locale);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeRegisterLocalizedCollators(long connectionPtr, String locale) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeRegisterLocalizedCollators(connectionPtr, locale));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static long nativeExecuteForLong(int connectionPtr, int statementPtr) {
    return nativeExecuteForLong(PreLPointers.get(connectionPtr), PreLPointers.get(statementPtr));
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static long nativeExecuteForLong(final long connectionPtr, final long statementPtr) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeExecuteForLong(connectionPtr, statementPtr));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static void nativeExecute(int connectionPtr, int statementPtr) {
    nativeExecute(PreLPointers.get(connectionPtr), PreLPointers.get(statementPtr));
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = S_V2)
  protected static void nativeExecute(final long connectionPtr, final long statementPtr) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeExecute(connectionPtr, statementPtr, false));
  }

  @Implementation(minSdk = 33)
  protected static void nativeExecute(
      final long connectionPtr, final long statementPtr, boolean isPragmaStmt) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeExecute(connectionPtr, statementPtr, isPragmaStmt));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static int nativeExecuteForChangedRowCount(int connectionPtr, int statementPtr) {
    return nativeExecuteForChangedRowCount(
        PreLPointers.get(connectionPtr), PreLPointers.get(statementPtr));
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static int nativeExecuteForChangedRowCount(
      final long connectionPtr, final long statementPtr) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () ->
                SQLiteConnectionNatives.nativeExecuteForChangedRowCount(
                    connectionPtr, statementPtr));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static int nativeGetColumnCount(int connectionPtr, int statementPtr) {
    return nativeGetColumnCount(PreLPointers.get(connectionPtr), PreLPointers.get(statementPtr));
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static int nativeGetColumnCount(final long connectionPtr, final long statementPtr) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeGetColumnCount(connectionPtr, statementPtr));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static String nativeGetColumnName(int connectionPtr, int statementPtr, int index) {
    return nativeGetColumnName(
        PreLPointers.get(connectionPtr), PreLPointers.get(statementPtr), index);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static String nativeGetColumnName(
      final long connectionPtr, final long statementPtr, final int index) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeGetColumnName(connectionPtr, statementPtr, index));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static void nativeBindNull(int connectionPtr, int statementPtr, int index) {
    nativeBindNull(PreLPointers.get(connectionPtr), PreLPointers.get(statementPtr), index);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeBindNull(
      final long connectionPtr, final long statementPtr, final int index) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeBindNull(connectionPtr, statementPtr, index));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static void nativeBindLong(int connectionPtr, int statementPtr, int index, long value) {
    nativeBindLong(PreLPointers.get(connectionPtr), PreLPointers.get(statementPtr), index, value);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeBindLong(
      final long connectionPtr, final long statementPtr, final int index, final long value) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () ->
                SQLiteConnectionNatives.nativeBindLong(connectionPtr, statementPtr, index, value));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static void nativeBindDouble(
      int connectionPtr, int statementPtr, int index, double value) {
    nativeBindDouble(PreLPointers.get(connectionPtr), PreLPointers.get(statementPtr), index, value);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeBindDouble(
      final long connectionPtr, final long statementPtr, final int index, final double value) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () ->
                SQLiteConnectionNatives.nativeBindDouble(
                    connectionPtr, statementPtr, index, value));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static void nativeBindString(
      int connectionPtr, int statementPtr, int index, String value) {
    nativeBindString(PreLPointers.get(connectionPtr), PreLPointers.get(statementPtr), index, value);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeBindString(
      final long connectionPtr, final long statementPtr, final int index, final String value) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () ->
                SQLiteConnectionNatives.nativeBindString(
                    connectionPtr, statementPtr, index, value));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static void nativeBindBlob(
      int connectionPtr, int statementPtr, int index, byte[] value) {
    nativeBindBlob(PreLPointers.get(connectionPtr), PreLPointers.get(statementPtr), index, value);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeBindBlob(
      final long connectionPtr, final long statementPtr, final int index, final byte[] value) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () ->
                SQLiteConnectionNatives.nativeBindBlob(connectionPtr, statementPtr, index, value));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static void nativeResetStatementAndClearBindings(int connectionPtr, int statementPtr) {
    nativeResetStatementAndClearBindings(
        PreLPointers.get(connectionPtr), PreLPointers.get(statementPtr));
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeResetStatementAndClearBindings(
      final long connectionPtr, final long statementPtr) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () ->
                SQLiteConnectionNatives.nativeResetStatementAndClearBindings(
                    connectionPtr, statementPtr));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static long nativeExecuteForLastInsertedRowId(int connectionPtr, int statementPtr) {
    return nativeExecuteForLastInsertedRowId(
        PreLPointers.get(connectionPtr), PreLPointers.get(statementPtr));
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static long nativeExecuteForLastInsertedRowId(
      final long connectionPtr, final long statementPtr) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () ->
                SQLiteConnectionNatives.nativeExecuteForLastInsertedRowId(
                    connectionPtr, statementPtr));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static long nativeExecuteForCursorWindow(
      int connectionPtr,
      int statementPtr,
      int windowPtr,
      int startPos,
      int requiredPos,
      boolean countAllRows) {
    return nativeExecuteForCursorWindow(
        PreLPointers.get(connectionPtr),
        PreLPointers.get(statementPtr),
        PreLPointers.get(windowPtr),
        startPos,
        requiredPos,
        countAllRows);
  }

  @Implementation(minSdk = LOLLIPOP)
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

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static int nativeExecuteForBlobFileDescriptor(int connectionPtr, int statementPtr) {
    return nativeExecuteForBlobFileDescriptor(
        PreLPointers.get(connectionPtr), PreLPointers.get(statementPtr));
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static int nativeExecuteForBlobFileDescriptor(
      final long connectionPtr, final long statementPtr) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () ->
                SQLiteConnectionNatives.nativeExecuteForBlobFileDescriptor(
                    connectionPtr, statementPtr));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static void nativeCancel(int connectionPtr) {
    nativeCancel(PreLPointers.get(connectionPtr));
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeCancel(long connectionPtr) {
    PerfStatsCollector.getInstance()
        .measure("androidsqlite", () -> SQLiteConnectionNatives.nativeCancel(connectionPtr));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static void nativeResetCancel(int connectionPtr, boolean cancelable) {
    nativeResetCancel(PreLPointers.get(connectionPtr), cancelable);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static void nativeResetCancel(long connectionPtr, boolean cancelable) {
    PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite",
            () -> SQLiteConnectionNatives.nativeResetCancel(connectionPtr, cancelable));
  }

  @Implementation(minSdk = R)
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

  @Implementation(minSdk = R)
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

  @Implementation(maxSdk = KITKAT_WATCH)
  protected static int nativeGetDbLookaside(int connectionPtr) {
    return nativeGetDbLookaside(PreLPointers.get(connectionPtr));
  }

  @Implementation(minSdk = LOLLIPOP)
  protected static int nativeGetDbLookaside(long connectionPtr) {
    return PerfStatsCollector.getInstance()
        .measure(
            "androidsqlite", () -> SQLiteConnectionNatives.nativeGetDbLookaside(connectionPtr));
  }
}
