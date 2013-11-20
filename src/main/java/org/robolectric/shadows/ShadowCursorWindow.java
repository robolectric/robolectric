package org.robolectric.shadows;

import android.database.Cursor;
import android.database.CursorWindow;
import com.almworks.sqlite4java.SQLiteConstants;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Implements(value = CursorWindow.class)
public class ShadowCursorWindow {
  private static final WindowData WINDOW_DATA = new WindowData();

  @Implementation
  public static int nativeCreate(String name, int cursorWindowSize) {
    return WINDOW_DATA.create(name, cursorWindowSize);
  }

  @Implementation
  public static void nativeDispose(int windowPtr) {
    WINDOW_DATA.close(windowPtr);
  }

  @Implementation
  public static byte[] nativeGetBlob(int windowPtr, int row, int column) {
    Value value = WINDOW_DATA.get(windowPtr).value(row, column);
    if (value.type == Cursor.FIELD_TYPE_BLOB) {
      return (byte[])value.value;
    } else {
      throw new android.database.sqlite.SQLiteException("Getting blob when column is non-blob. Row " + row + ", col " + column);
    }
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
    return WINDOW_DATA.get(windowPtr).allocRow();
  }

  @Implementation
  public static boolean nativeSetNumColumns(int windowPtr, int columnNum) {
    return WINDOW_DATA.get(windowPtr).setNumColumns(columnNum);
  }

  @Implementation
  public static String nativeGetName(int windowPtr) {
    return WINDOW_DATA.get(windowPtr).getName();
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
    private final List<Row> rows;
    private final String name;
    private int numColumns;

    public Data(String name, int cursorWindowSize) {
      this.name = name;
      this.rows = new ArrayList<Row>(cursorWindowSize);
    }

    public Value value(int rowN, int colN) {
      Row row = rows.get(rowN);
      if (row == null) {
        throw new IllegalArgumentException("Bad row number: " + rowN + ", count: " + rows.size());
      }
      return row.get(colN);
    }

    public int numRows() {
      return rows.size();
    }

    public boolean putValue(Value value, int rowN, int colN) {
      return rows.get(rowN).set(colN, value);
    }

    public void fillWith(SQLiteStatement stmt) throws SQLiteException {
      //Android caches results in the WindowedCursor to allow moveToPrevious() to function.
      //Robolectric will have to cache the results too. In the rows list.
      while (stmt.step()) {
        rows.add(fillRowValues(stmt));
      }
    }

    private static Row fillRowValues(SQLiteStatement stmt) throws SQLiteException {
      final int columnCount = stmt.columnCount();
      Row row = new Row(columnCount);
      for (int index = 0; index < columnCount; index++) {
        row.set(index, new Value(stmt.columnValue(index), stmt.columnType(index)));
      }
      return row;
    }

    public void clear() {
      rows.clear();
    }

    public boolean allocRow() {
      rows.add(new Row(numColumns));
      return true;
    }

    public boolean setNumColumns(int numColumns) {
      this.numColumns = numColumns;
      return true;
    }

    public String getName() {
      return name;
    }
  }

  private static class Row {
    private final List<Value> values;

    public Row(int length) {
      values = new ArrayList<Value>(length);
      for (int i=0; i<length; i++) {
        values.add(new Value(null, Cursor.FIELD_TYPE_NULL));
      }
    }

    public Value get(int n) {
      return values.get(n);
    }

    public boolean set(int colN, Value value) {
      values.set(colN, value);
      return true;
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
      Data data = get(ptr);
      data.fillWith(stmt);
      return data.numRows();
    }

    public void close(final int ptr) {
      Data removed = dataMap.remove(ptr);
      if (removed == null) {
        throw new IllegalArgumentException("Bad cursor window pointer " + ptr + ". Valid pointers: " + dataMap.keySet());
      }
    }

    public void clear(final int ptr) {
      get(ptr).clear();
    }

    public int create(String name, int cursorWindowSize) {
      int ptr = windowPtrCounter.incrementAndGet();
      dataMap.put(ptr, new Data(name, cursorWindowSize));
      return ptr;
    }

  }

  /* TODO:
  private static native int nativeCreateFromParcel(Parcel parcel);
  private static native void nativeWriteToParcel(int windowPtr, Parcel parcel);
  private static native void nativeFreeLastRow(int windowPtr);
  private static native void nativeCopyStringToBuffer(int windowPtr, int row, int column,
                                                      CharArrayBuffer buffer);
   */
}
