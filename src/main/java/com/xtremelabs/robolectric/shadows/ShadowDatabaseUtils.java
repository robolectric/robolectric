package com.xtremelabs.robolectric.shadows;

import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteProgram;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(DatabaseUtils.class)
public class ShadowDatabaseUtils {

    @Implementation
    public static void bindObjectToProgram(SQLiteProgram prog, int index,
                                           Object value) {
        if (value == null) {
            prog.bindNull(index);
        } else if (value instanceof Double || value instanceof Float) {
            prog.bindDouble(index, ((Number) value).doubleValue());
        } else if (value instanceof Number) {
            prog.bindLong(index, ((Number) value).longValue());
        } else if (value instanceof Boolean) {
            Boolean bool = (Boolean) value;
            if (bool) {
                prog.bindLong(index, 1);
            } else {
                prog.bindLong(index, 0);
            }
        } else if (value instanceof byte[]) {
            prog.bindBlob(index, (byte[]) value);
        } else {
            prog.bindString(index, value.toString());
        }
    }
    
    @Implementation
    public static String sqlEscapeString( String value ) {
		StringBuilder builder = new StringBuilder();	
		
		// SQLite quoting conventions are used.
		value = value.replaceAll( "'", "''" );  
		builder.append( "'" ).append( value ).append( "'" );	
		
		return builder.toString();
	}
}
