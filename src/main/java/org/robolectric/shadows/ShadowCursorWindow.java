package org.robolectric.shadows;

import android.database.Cursor;
import android.database.CursorWindow;
import com.almworks.sqlite4java.SQLiteConstants;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Implements(value = CursorWindow.class)
public class ShadowCursorWindow {
  private static final WindowData WINDOW_DATA = new WindowData();

  @Implementation
  public static int nativeCreate(String name, int cursorWindowSize) {
    return WINDOW_DATA.create();
  }

  @Implementation
  public static void nativeDispose(int windowPtr) {
    WINDOW_DATA.close(windowPtr);
  }

  @Implementation
  public static byte[] nativeGetBlob(int windowPtr, int row, int column) {
    Data data = WINDOW_DATA.get(windowPtr);
    return (byte[])data.value(row, column).value;
  }

  @Implementation
  public static String nativeGetString(int windowPtr, int row, int column) {
    Value val = WINDOW_DATA.get(windowPtr).value(row, column);
    if (val.type == Cursor.FIELD_TYPE_BLOB) {
      throw new android.database.sqlite.SQLiteException("Getting string when column is blob. Row " + row + ", col " + column);
    }
    Object value = val.value;
    return value == null ? null : String.valueOf(value);
  }

  @Implementation
  public static long nativeGetLong(int windowPtr, int row, int column) {
    return nativeGetNumber(windowPtr, row, column).longValue();
  }

  @Implementation
  public static double nativeGetDouble(int windowPtr, int row, int column) {
    return nativeGetNumber(windowPtr, row, column).doubleValue();
  }

  @Implementation
  public static int nativeGetType(int windowPtr, int row, int column) {
    Value val = WINDOW_DATA.get(windowPtr).value(row, column);
    return val.value == null ? Cursor.FIELD_TYPE_NULL : val.type;
  }

  @Implementation
  public static void nativeClear(int windowPtr) {
    WINDOW_DATA.clear(windowPtr);
  }

  @Implementation
  public static int nativeGetNumRows(int windowPtr) {
    return WINDOW_DATA.get(windowPtr).numRows();
  }

  @Implementation
  public static boolean nativePutBlob(int windowPtr, byte[] value, int row, int column) {
    return WINDOW_DATA.get(windowPtr).putValue(new Value(value, Cursor.FIELD_TYPE_BLOB), row, column);
  }

  @Implementation
  public static boolean nativePutString(int windowPtr, String value, int row, int column) {
    return WINDOW_DATA.get(windowPtr).putValue(new Value(value, Cursor.FIELD_TYPE_STRING), row, column);
  }

  @Implementation
  public static boolean nativePutLong(int windowPtr, long value, int row, int column) {
    return WINDOW_DATA.get(windowPtr).putValue(new Value(value, Cursor.FIELD_TYPE_INTEGER), row, column);
  }

  @Implementation
  public static boolean nativePutDouble(int windowPtr, double value, int row, int column) {
    return WINDOW_DATA.get(windowPtr).putValue(new Value(value, Cursor.FIELD_TYPE_FLOAT), row, column);
  }

  @Implementation
  public static boolean nativePutNull(int windowPtr, int row, int column) {
    return WINDOW_DATA.get(windowPtr).putValue(new Value(null, Cursor.FIELD_TYPE_NULL), row, column);
  }

  @Implementation
  public static boolean nativeAllocRow(int windowPtr) {
    return true;
  }

  public static void reset() {
    WINDOW_DATA.reset();
  }

  protected static int setData(int windowPtr, SQLiteStatement stmt) throws SQLiteException {
    return WINDOW_DATA.setData(windowPtr, stmt);
  }

  // https://github.com/android/platform_frameworks_base/blob/master/core/jni/android_database_CursorWindow.cpp#L364
  private static Number nativeGetNumber(int windowPtr, int row, int column) {
    Value value = WINDOW_DATA.get(windowPtr).value(row, column);
    switch (value.type) {
      case Cursor.FIELD_TYPE_NULL:
      case SQLiteConstants.SQLITE_NULL:
        return 0;
      case Cursor.FIELD_TYPE_INTEGER:
      case Cursor.FIELD_TYPE_FLOAT:
        return (Number) value.value;
      case Cursor.FIELD_TYPE_STRING: {
        try {
          return Double.parseDouble((String) value.value);
        } catch (NumberFormatException e) {
          return 0;
        }
      }
      case Cursor.FIELD_TYPE_BLOB:
        throw new android.database.sqlite.SQLiteException("could not convert "+value);
      default:
        throw new android.database.sqlite.SQLiteException("unknown type: "+value.type);
    }
  }

  private static class Data {
    private String[] columnNameArray;
    private final ArrayList<Map<String, Value>> rows = new ArrayList<Map<String, Value>>();
    private final SQLiteStatement stmt;

    public Data(final SQLiteStatement stmt) throws SQLiteException {
      this.stmt = stmt;
      cacheColumnNames();
      fillRows();
    }

    public Value value(int rowN, int colN) {
      Map<String, Value> row = rows.get(rowN);
      if (row == null) {
        throw new IllegalArgumentException("Bad row number: " + rowN + ", count: " + rows.size());
      }
      String col = columnNameArray[colN];
      return row.get(col);
    }

    public int numRows() {
      return rows.size();
    }

    public boolean putValue(Value value, int rowN, int colN) {
      return true;
    }

    private Map<String, Value> fillRowValues(SQLiteStatement stmt) throws SQLiteException {
      Map<String, Value> row = new HashMap<String, Value>();
      int index = 0;
      for (String s : columnNameArray) {
        row.put(s, new Value(stmt.columnValue(index), stmt.columnType(index)));
        index++;
      }
      return row;
    }

    private void fillRows() throws SQLiteException {
      //Android caches results in the WindowedCursor to allow moveToPrevious() to function.
      //Robolectric will have to cache the results too. In the rows map.
      while (stmt.step()) {
        rows.add(fillRowValues(stmt));
      }
    }

    /**
     * Stores the column names so they are retrievable after the resultSet has closed
     */
    private void cacheColumnNames() throws SQLiteException {
      int columnCount = stmt.columnCount();
      columnNameArray = new String[columnCount];
      for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
        String cName = stmt.getColumnName(columnIndex).toLowerCase();
        this.columnNameArray[columnIndex] = cName;
      }
    }
  }

  private static class Value {
    final Object value;
    final int type;

    public Value(final Object value, final int type) {
      this.value = value;
      this.type = type;
    }
  }

  private static class WindowData {
    private final AtomicInteger windowPtrCounter = new AtomicInteger(0);
    private final Map<Integer, Data> dataMap = new ConcurrentHashMap<Integer, Data>();

    public Data get(int ptr) {
      Data data = dataMap.get(ptr);
      if (data == null) {
        throw new IllegalArgumentException("Invalid window pointer: " + ptr + "; current pointers: " + dataMap.keySet());
      }
      return data;
    }

    public int setData(final int ptr, final SQLiteStatement stmt) throws SQLiteException {
      if (dataMap.containsKey(ptr)) {
        throw new IllegalStateException("Already have data for window " + ptr);
      }
      Data data = new Data(stmt);
      dataMap.put(ptr, data);
      return data.numRows();
    }

    public void close(final int ptr) {
      dataMap.remove(ptr);
    }

    public void clear(final int ptr) {
      close(ptr);
    }

    public int create() {
      return windowPtrCounter.incrementAndGet();
    }

    public void reset() {
      windowPtrCounter.set(0);
      dataMap.clear();
    }
  }

  /* TODO:
  private static native int nativeCreateFromParcel(Parcel parcel);
  private static native void nativeWriteToParcel(int windowPtr, Parcel parcel);

  private static native boolean nativeSetNumColumns(int windowPtr, int columnNum);

  private static native void nativeFreeLastRow(int windowPtr);

  private static native void nativeCopyStringToBuffer(int windowPtr, int row, int column,
                                                      CharArrayBuffer buffer);

  private static native String nativeGetName(int windowPtr);
   */
}
