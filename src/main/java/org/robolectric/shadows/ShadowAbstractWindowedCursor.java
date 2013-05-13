package org.robolectric.shadows;

import android.database.AbstractWindowedCursor;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Simulates an Android Cursor object, by wrapping a JDBC ResultSet.
 */
@Implements(value = AbstractWindowedCursor.class, inheritImplementationMethods = true)
public class ShadowAbstractWindowedCursor extends ShadowAbstractCursor {

  private ResultSet resultSet;


  /**
   * Stores the column names so they are retrievable after the resultSet has closed
   */
  private void cacheColumnNames(ResultSet rs) {
    try {
      ResultSetMetaData metaData = rs.getMetaData();
      int columnCount = metaData.getColumnCount();
      columnNameArray = new String[columnCount];
      for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
        String cName = metaData.getColumnName(columnIndex).toLowerCase();
        this.columnNames.put(cName, columnIndex-1);
        this.columnNameArray[columnIndex-1]=cName;
      }
    } catch (SQLException e) {
      throw new RuntimeException("SQL exception in cacheColumnNames", e);
    }
  }




  private Integer getColIndex(String columnName) {
    if (columnName == null) {
      return -1;
    }

    Integer i  = this.columnNames.get(columnName.toLowerCase());
    if (i==null) return -1;
    return i;
  }

  @Implementation
  public byte[] getBlob(int columnIndex) {
    checkPosition();
    return (byte[]) this.currentRow.get(getColumnNames()[columnIndex]);
  }

  @Implementation
  public String getString(int columnIndex) {
    checkPosition();
    Object value = this.currentRow.get(getColumnNames()[columnIndex]);
    if (value == null) {
      return null;
    } else if (value instanceof Clob) {
      try {
        return ((Clob) value).getSubString(1, (int)((Clob) value).length());
      } catch (SQLException x) {
        throw new RuntimeException(x);
      }
    } else if (value instanceof byte[]) {
      throw new SQLiteException("getString called on a blob field");
    } else {
      return value.toString();
    }
  }

  @Implementation
  public short getShort(int columnIndex) {
    checkPosition();
    Object o =this.currentRow.get(getColumnNames()[columnIndex]);
    if (o==null) return 0;
    return new Short(o.toString());
  }

  @Implementation
  public int getInt(int columnIndex) {
    checkPosition();
    Object o =this.currentRow.get(getColumnNames()[columnIndex]);
    if (o==null) return 0;
    return new Integer(o.toString());
  }

  @Implementation
  public long getLong(int columnIndex) {
    checkPosition();
    Object o =this.currentRow.get(getColumnNames()[columnIndex]);
    if (o==null) return 0;
    return new Long(o.toString());
  }

  @Implementation
  public float getFloat(int columnIndex) {
    checkPosition();
    Object o =this.currentRow.get(getColumnNames()[columnIndex]);
    if (o==null) return 0;
    return new Float(o.toString());

  }

  @Implementation
  public double getDouble(int columnIndex) {
    checkPosition();
    Object o =this.currentRow.get(getColumnNames()[columnIndex]);
    if (o==null) return 0;
    return new Double(o.toString());
  }

  @Implementation
  public int getType(int columnIndex) {
    checkPosition();
    Object o = this.currentRow.get(getColumnNames()[columnIndex]);
    if (o == null) {
      return Cursor.FIELD_TYPE_NULL;
    } else if (o instanceof Double || o instanceof Float) {
      return Cursor.FIELD_TYPE_FLOAT;
    } else if (o instanceof Number) {
      return Cursor.FIELD_TYPE_INTEGER;
    } else if (o instanceof byte[]) {
      return Cursor.FIELD_TYPE_BLOB;
    } else {
      return Cursor.FIELD_TYPE_STRING;
    }
  }

  @Implementation
  public void checkPosition() {
  }

  @Implementation
  public boolean isNull(int columnIndex) {
    Object o = this.currentRow.get(getColumnNames()[columnIndex]);
    return o == null;
  }

  @Implementation
  @Override
  public int getCount() {
    return super.getCount();
  }
}
