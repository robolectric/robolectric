package org.robolectric.shadows;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteProgram;
import android.os.CancellationSignal;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

@Implements(value = SQLiteProgram.class)
public abstract class ShadowSQLiteProgram {

//  @RealObject
//  SQLiteProgram realSQLiteProgram;
//
//  protected SQLiteDatabase mDatabase;
//
//  Connection connection;
//
//  PreparedStatement actualDbStatement;
//
//  String sql;
//
//  @Implementation
//  public void __constructor__(SQLiteDatabase db, String sql, Object[] bindArgs,
//                CancellationSignal cancellationSignalForPrepare) {
//    this.sql = sql;
//    mDatabase = db;
//    connection = Robolectric.shadowOf(db).getConnection();
//
//    try {
//      actualDbStatement = connection.prepareStatement(sql,
//          Statement.RETURN_GENERATED_KEYS);
//    } catch (SQLException e) {
//      throw new SQLiteException("Cannot initialize program", e);
//    }
//  }
//
//  /**
//   * Bind a NULL value to this statement. The value remains bound until
//   * {@link #clearBindings} is called.
//   *
//   * @param index The 1-based index to the parameter to bind null to
//   */
//  @Implementation
//  public void bindNull(int index) {
//    checkDatabaseIsOpen();
//    try {
//      // SQLite ignores typecode
//      actualDbStatement.setNull(index, java.sql.Types.NULL);
//    } catch (SQLException e) {
//      throw new SQLiteException("Cannot bindNull", e);
//    }
//  }
//
//  /**
//   * Bind a long value to this statement. The value remains bound until
//   * {@link #clearBindings} is called.
//   *
//   * @param index The 1-based index to the parameter to bind
//   * @param value The value to bind
//   */
//  @Implementation
//  public void bindLong(int index, long value) {
//    checkDatabaseIsOpen();
//
//    try {
//      actualDbStatement.setLong(index, value);
//    } catch (SQLException e) {
//      throw new SQLiteException("Cannot bind long", e);
//    }
//  }
//
//  private void checkDatabaseIsOpen() {
//    if (!mDatabase.isOpen()) {
//      throw new IllegalStateException("database " + mDatabase.getPath() + " already closed");
//    }
//  }
//
//  public PreparedStatement getStatement() {
//    return actualDbStatement;
//  }
//
//  /**
//   * Bind a double value to this statement. The value remains bound until
//   * {@link #clearBindings} is called.
//   *
//   * @param index The 1-based index to the parameter to bind
//   * @param value The value to bind
//   */
//  @Implementation
//  public void bindDouble(int index, double value) {
//    checkDatabaseIsOpen();
//    try {
//      actualDbStatement.setDouble(index, value);
//    } catch (SQLException e) {
//      throw new SQLiteException("Cannot bind double", e);
//    }
//  }
//
//  /**
//   * Bind a String value to this statement. The value remains bound until
//   * {@link #clearBindings} is called.
//   *
//   * @param index The 1-based index to the parameter to bind
//   * @param value The value to bind
//   */
//  @Implementation
//  public void bindString(int index, String value) {
//    if (value == null) {
//      throw new IllegalArgumentException("the bind value at index " + index + " is null");
//    }
//    checkDatabaseIsOpen();
//    try {
//      actualDbStatement.setString(index, value);
//    } catch (SQLException e) {
//      throw new SQLiteException("Cannot bind string", e);
//    }
//  }
//
//  /**
//   * Bind a byte array value to this statement. The value remains bound until
//   * {@link #clearBindings} is called.
//   *
//   * @param index The 1-based index to the parameter to bind
//   * @param value The value to bind
//   */
//  @Implementation
//  public void bindBlob(int index, byte[] value) {
//    if (value == null) {
//      throw new IllegalArgumentException("the bind value at index " + index + " is null");
//    }
//    checkDatabaseIsOpen();
//    try {
//      actualDbStatement.setBytes(index, value);
//    } catch (SQLException e) {
//      throw new SQLiteException("Cannot bind blob", e);
//    }
//
//  }
//
//  /**
//   * Clears all existing bindings. Unset bindings are treated as NULL.
//   */
//  @Implementation
//  public void clearBindings() {
//    checkDatabaseIsOpen();
//
//    try {
//      actualDbStatement.clearParameters();
//    } catch (SQLException e) {
//      throw new RuntimeException(e);
//    }
//  }
}
