package com.xtremelabs.robolectric.shadows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteProgram;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(SQLiteProgram.class)
public abstract class ShadowSQLiteProgram {
	@RealObject	SQLiteProgram realSQLiteProgram;
	protected SQLiteDatabase mDatabase;
	Connection connection;
	//Map<Integer,Object> parameterMap = new HashMap<Integer,Object>();
	PreparedStatement actualDBstatement;
	public void init(SQLiteDatabase db, String sql) {
	 mDatabase = db;
	 connection = Robolectric.shadowOf(db).getConnection();
       // db.acquireReference();
      //  db.addSQLiteClosable(this);
     //   this.nHandle = db.mNativeHandle;
	 
	 try {
			actualDBstatement = connection.prepareStatement(sql,
					Statement.RETURN_GENERATED_KEYS);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	 
        compile(sql, false);
	}
	
	@Implementation
	public void compile(String sql, boolean forceCompilation) {
        // Only compile if we don't have a valid statement already or the caller has
        // explicitly requested a recompile. 
       // if (nStatement == 0 || forceCompilation) {
       //   Robolectric.shadowOf(mDatabase).lock();
           // try {
                // Note that the native_compile() takes care of destroying any previously
                // existing programs before it compiles.
            //    acquireReference();                
            //    native_compile(sql);
           // } finally {
           //     releaseReference();
          //      Robolectric.shadowOf(mDatabase).unlock();
         //   }        
      //  }
    } 
	

    /**
     * Bind a NULL value to this statement. The value remains bound until
     * {@link #clearBindings} is called.
     *
     * @param index The 1-based index to the parameter to bind null to
     */
	@Implementation
    public void bindNull(int index) {
	//	parameterMap.put(index, null);
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
    //	parameterMap.put(index, value);
    	try {
			actualDBstatement.setLong(index,value);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
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
    	//parameterMap.put(index, value);
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
      //  parameterMap.put(index, value);
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
      //  parameterMap.put(index, value);
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
       //     parameterMap = new HashMap<Integer,Object>();
    	try {
			actualDBstatement.clearParameters();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
    }
}
