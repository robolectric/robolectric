package com.xtremelabs.robolectric.shadows;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteProgram;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

@Implements(SQLiteProgram.class)
public abstract class ShadowSQLiteProgram {
	@RealObject	SQLiteProgram realSQLiteProgram;
	protected SQLiteDatabase mDatabase;
	Connection connection;
	PreparedStatement actualDBstatement;
	public void init(SQLiteDatabase db, String sql) {
	 mDatabase = db;
	 connection = Robolectric.shadowOf(db).getConnection();

	 try {
			actualDBstatement = connection.prepareStatement(sql,
					Statement.RETURN_GENERATED_KEYS);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
    /**
     * Bind a NULL value to this statement. The value remains bound until
     * {@link #clearBindings} is called.
     *
     * @param index The 1-based index to the parameter to bind null to
     */
	@Implementation
    public void bindNull(int index) {
		checkDatabaseIsOpen();
		try {
			// SQLite ignores typecode
			// typecode is also ignored in H2 when using the two parameter setNUll()
			actualDBstatement.setNull(index,java.sql.Types.NULL); 
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
    }

    /**
     * Bind a long value to this statement. The value remains bound until
     * {@link #clearBindings} is called.
     *
     * @param index The 1-based index to the parameter to bind
     * @param value The value to bind
     */
    @Implementation
    public void bindLong(int index, long value) {
    	checkDatabaseIsOpen();

    	try {
			actualDBstatement.setLong(index,value);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
    }
    
    private void checkDatabaseIsOpen() {
    	if (!mDatabase.isOpen()) {
            throw new IllegalStateException("database " + mDatabase.getPath() + " already closed");
        }
    }

    public PreparedStatement getStatement() {
    	return actualDBstatement;
    }
    
    /**
     * Bind a double value to this statement. The value remains bound until
     * {@link #clearBindings} is called.
     *
     * @param index The 1-based index to the parameter to bind
     * @param value The value to bind
     */
    @Implementation
    public void bindDouble(int index, double value) {
    	checkDatabaseIsOpen();
    	try {
			actualDBstatement.setDouble(index,value);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
    }

    /**
     * Bind a String value to this statement. The value remains bound until
     * {@link #clearBindings} is called.
     *
     * @param index The 1-based index to the parameter to bind
     * @param value The value to bind
     */
    @Implementation
    public void bindString(int index, String value) {
        if (value == null) {
            throw new IllegalArgumentException("the bind value at index " + index + " is null");
        }
        checkDatabaseIsOpen();
        try {
			actualDBstatement.setString(index,value);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
    }

    /**
     * Bind a byte array value to this statement. The value remains bound until
     * {@link #clearBindings} is called.
     *
     * @param index The 1-based index to the parameter to bind
     * @param value The value to bind
     */
    @Implementation
    public void bindBlob(int index, byte[] value) {
        if (value == null) {
            throw new IllegalArgumentException("the bind value at index " + index + " is null");
        }
        checkDatabaseIsOpen();
        try {
			actualDBstatement.setBytes(index,value);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

    }

    /**
     * Clears all existing bindings. Unset bindings are treated as NULL.
     */
    @Implementation
    public void clearBindings() {
    	checkDatabaseIsOpen();
    	
    	try {
			actualDBstatement.clearParameters();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
    }
}
