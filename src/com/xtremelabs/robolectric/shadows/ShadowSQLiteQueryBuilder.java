package com.xtremelabs.robolectric.shadows;

import android.database.sqlite.SQLiteQueryBuilder;

import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

/**
 * Shadow for {@code SQLiteQueryBuilder}.
 *
 */
@Implements(SQLiteQueryBuilder.class)
public class ShadowSQLiteQueryBuilder {
	
	@Implementation
	public static String buildQueryString (boolean distinct, String tables, 
			String[] columns, String where, String groupBy, String having,
			String orderBy, String limit) {
		StringBuilder sb = new StringBuilder();
		
		sb.append( "SELECT " );
		
		if ( distinct ) {
			sb.append("DISTINCT ");
		}
		
		if ( columns != null ) {
			for ( int i = 0; i < columns.length; i++ ) {
				sb.append( columns[i] );
				if ( i != columns.length -1 ) {
					sb.append(",");
				}
				sb.append( " " );
			}
		} else {
			sb.append("* ");
		}
		
		sb.append( "FROM " );
		sb.append( tables );
		
		if ( where != null ) {
			sb.append( " WHERE " );
			sb.append( where );
		}
		
		if ( groupBy != null ) {
			sb.append( " GROUP BY " );
			sb.append( groupBy );
		}
		
		if ( having != null ) {
			sb.append( " HAVING " );
			sb.append( having );
		}
		
		if ( orderBy != null ) {
			sb.append( " ORDER BY " );
			sb.append( orderBy );
		}
		
		if ( limit != null ) {
			sb.append( " LIMIT " );
			sb.append( limit );
		}
		
		sb.append(";");
		return sb.toString();
	}

}
