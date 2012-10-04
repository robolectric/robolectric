package com.xtremelabs.robolectric.shadows;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadow for {@code SQLiteQueryBuilder}.
 */
@Implements(SQLiteQueryBuilder.class)
public class ShadowSQLiteQueryBuilder {
  private static final Pattern sLimitPattern = Pattern.compile("\\s*\\d+\\s*(,\\s*\\d+\\s*)?");
  
  private boolean mStrict;
  private String mTables = "";
  private StringBuilder mWhereClause = null; // lazily created
  private Map<String, String> mProjectionMap = null;
  private boolean mDistinct;

  @Implementation
  public void setTables(String inTables) {
    mTables = inTables;
  }

  @Implementation
  public void setStrict(boolean flag) {
    mStrict = flag;
  }

  @Implementation
  public void setDistinct(boolean distinct) {
    mDistinct = distinct;
  }

  @Implementation
  public void appendWhere(CharSequence inWhere) {
    if (mWhereClause == null) {
      mWhereClause = new StringBuilder(inWhere.length() + 16);
    }
    if (mWhereClause.length() == 0) {
      mWhereClause.append('(');
    }
    mWhereClause.append(inWhere);
  }

  @Implementation
  public void setProjectionMap(Map<String, String> columnMap) {
    mProjectionMap = columnMap;
  }

  @Implementation
  public String buildQuery(String[] projectionIn, String selection, String groupBy, String having, String sortOrder, String limit) {
    String[] projection = computeProjection(projectionIn);

    StringBuilder where = new StringBuilder();
    boolean hasBaseWhereClause = mWhereClause != null && mWhereClause.length() > 0;

    if (hasBaseWhereClause) {
      where.append(mWhereClause.toString());
      where.append(')');
    }

    // Tack on the user's selection, if present.
    if (selection != null && selection.length() > 0) {
      if (hasBaseWhereClause) {
        where.append(" AND ");
      }

      where.append('(');
      where.append(selection);
      where.append(')');
    }

    return buildQueryString(mDistinct, mTables, projection, where.toString(), groupBy, having, sortOrder, limit);
  }

  @Implementation
  public static String buildQueryString(boolean distinct, String tables, String[] columns, String where, String groupBy, String having, String orderBy,
      String limit) {
    if (isEmpty(groupBy) && !isEmpty(having)) {
      throw new IllegalArgumentException("HAVING clauses are only permitted when using a GROUP BY clause");
    }
    if (!isEmpty(limit) && !sLimitPattern.matcher(limit).matches()) {
      throw new IllegalArgumentException("invalid LIMIT clauses:" + limit);
    }

    StringBuilder query = new StringBuilder(120);

    query.append("SELECT ");
    if (distinct) {
      query.append("DISTINCT ");
    }
    if (columns != null && columns.length != 0) {
      appendColumns(query, columns);
    } else {
      query.append("* ");
    }
    query.append("FROM ");
    query.append(tables);
    appendClause(query, " WHERE ", where);
    appendClause(query, " GROUP BY ", groupBy);
    appendClause(query, " HAVING ", having);
    appendClause(query, " ORDER BY ", orderBy);
    appendClause(query, " LIMIT ", limit);

    return query.toString();
  }

  private static void appendClause(StringBuilder s, String name, String clause) {
    if (!isEmpty(clause)) {
      s.append(name);
      s.append(clause);
    }
  }

  @Implementation
  public static void appendColumns(StringBuilder s, String[] columns) {
    int n = columns.length;

    for (int i = 0; i < n; i++) {
      String column = columns[i];

      if (column != null) {
        if (i > 0) {
          s.append(", ");
        }
        s.append(column);
      }
    }
    s.append(' ');
  }

  private String[] computeProjection(String[] projectionIn) {
    if (projectionIn != null && projectionIn.length > 0) {
      if (mProjectionMap != null) {
        String[] projection = new String[projectionIn.length];
        int length = projectionIn.length;

        for (int i = 0; i < length; i++) {
          String userColumn = projectionIn[i];
          String column = mProjectionMap.get(userColumn);

          if (column != null) {
            projection[i] = column;
            continue;
          }

          if (!mStrict && (userColumn.contains(" AS ") || userColumn.contains(" as "))) {
            /* A column alias already exist */
            projection[i] = userColumn;
            continue;
          }

          throw new IllegalArgumentException("Invalid column " + projectionIn[i]);
        }
        return projection;
      } else {
        return projectionIn;
      }
    } else if (mProjectionMap != null) {
      // Return all columns in projection map.
      Set<Entry<String, String>> entrySet = mProjectionMap.entrySet();
      String[] projection = new String[entrySet.size()];
      Iterator<Entry<String, String>> entryIter = entrySet.iterator();
      int i = 0;

      while (entryIter.hasNext()) {
        Entry<String, String> entry = entryIter.next();

        // Don't include the _count column when people ask for no projection.
        if (entry.getKey().equals(BaseColumns._COUNT)) {
          continue;
        }
        projection[i++] = entry.getValue();
      }
      return projection;
    }
    return null;
  }

  private static boolean isEmpty(String s) {
    return s == null || s.length() == 0;
  }

}
