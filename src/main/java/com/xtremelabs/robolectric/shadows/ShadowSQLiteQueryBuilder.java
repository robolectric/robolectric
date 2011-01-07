package com.xtremelabs.robolectric.shadows;

import android.database.sqlite.SQLiteQueryBuilder;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.util.Join;

/**
 * Shadow for {@code SQLiteQueryBuilder}.
 */
@Implements(SQLiteQueryBuilder.class)
public class ShadowSQLiteQueryBuilder {

    @Implementation
    public static String buildQueryString(boolean distinct, String tables,
                                          String[] columns, String where, String groupBy, String having,
                                          String orderBy, String limit) {

        StringBuilder sb = new StringBuilder("SELECT ");

        if (distinct) {
            sb.append("DISTINCT ");
        }

        if (columns != null) {
            sb.append(Join.join(", ", (Object[]) columns));
        } else {
            sb.append("*");
        }

        sb.append(" FROM ");
        sb.append(tables);

        conditionallyAppend(sb, " WHERE ", where);
        conditionallyAppend(sb, " GROUP BY ", groupBy);
        conditionallyAppend(sb, " HAVING ", having);
        conditionallyAppend(sb, " ORDER BY ", orderBy);
        conditionallyAppend(sb, " LIMIT ", limit);

        return sb.toString();
    }

    private static void conditionallyAppend(StringBuilder sb, String keyword, String value) {
        if (value != null) {
            sb.append(keyword);
            sb.append(value);
        }
    }

}
