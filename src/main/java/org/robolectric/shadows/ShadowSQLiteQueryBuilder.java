package org.robolectric.shadows;

import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.Join;

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

    if (columns != null && columns.length != 0) {
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

  static void conditionallyAppend(StringBuilder sb, String keyword, String value) {
    if (!TextUtils.isEmpty(value)) {
      sb.append(keyword);
      sb.append(value);
    }
  }

}
