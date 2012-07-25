package com.xtremelabs.robolectric.shadows;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.util.Join;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Shadow for {@code SQLiteQueryBuilder}.
 */
@Implements(SQLiteQueryBuilder.class)
public class ShadowSQLiteQueryBuilder {

    private static final String TAG = "SQLiteQueryBuilder";
    private static final Pattern sLimitPattern =
            Pattern.compile("\\s*\\d+\\s*(,\\s*\\d+\\s*)?");

    private Map<String, String> mProjectionMap = null;
    private String mTables = "";
    private StringBuilder mWhereClause = null;  // lazily created
    private boolean mDistinct;
    private SQLiteDatabase.CursorFactory mFactory;
    private boolean mStrict;

    public ShadowSQLiteQueryBuilder() {
        mDistinct = false;
        mFactory = null;
    }

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

    @Implementation
    public void setDistinct(boolean distinct) {
        mDistinct = distinct;
    }

    @Implementation
    public String getTables() {
        return mTables;
    }

    @Implementation
    public void setTables(String inTables) {
        mTables = inTables;
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
    public void appendWhereEscapeString(String inWhere) {
        if (mWhereClause == null) {
            mWhereClause = new StringBuilder(inWhere.length() + 16);
        }
        if (mWhereClause.length() == 0) {
            mWhereClause.append('(');
        }
        DatabaseUtils.appendEscapedSQLString(mWhereClause, inWhere);
    }

    @Implementation
    public void setProjectionMap(Map<String, String> columnMap) {
        mProjectionMap = columnMap;
    }

    @Implementation
    public void setCursorFactory(SQLiteDatabase.CursorFactory factory) {
        mFactory = factory;
    }

    //@Implementation
    public void setStrict(boolean flag) {
        mStrict = flag;
    }

    private static void appendClause(StringBuilder s, String name, String clause) {
        if (!TextUtils.isEmpty(clause)) {
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

    @Implementation
    public Cursor query(SQLiteDatabase db, String[] projectionIn,
                        String selection, String[] selectionArgs, String groupBy,
                        String having, String sortOrder) {
        return query(db, projectionIn, selection, selectionArgs, groupBy, having, sortOrder,
                null /* limit */);
    }

    @Implementation
    public Cursor query(SQLiteDatabase db, String[] projectionIn,
                        String selection, String[] selectionArgs, String groupBy,
                        String having, String sortOrder, String limit) {
        if (mTables == null) {
            return null;
        }

        String sql = buildQuery(
                projectionIn, selection, groupBy, having,
                sortOrder, limit);

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Performing query: " + sql);
        }
        return db.rawQueryWithFactory(
                mFactory, sql, selectionArgs,
                SQLiteDatabase.findEditTable(mTables)); // will throw if query is invalid
    }

    //@Implementation
    public String buildQuery(
            String[] projectionIn, String selection, String groupBy,
            String having, String sortOrder, String limit) {
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

        return buildQueryString(
                mDistinct, mTables, projection, where.toString(),
                groupBy, having, sortOrder, limit);
    }

    @Implementation
    public String buildQuery(
            String[] projectionIn, String selection, String[] selectionArgs,
            String groupBy, String having, String sortOrder, String limit) {
        return buildQuery(projectionIn, selection, groupBy, having, sortOrder, limit);
    }

    //@Implementation
    public String buildUnionSubQuery(
            String typeDiscriminatorColumn,
            String[] unionColumns,
            Set<String> columnsPresentInTable,
            int computedColumnsOffset,
            String typeDiscriminatorValue,
            String selection,
            String groupBy,
            String having) {
        int unionColumnsCount = unionColumns.length;
        String[] projectionIn = new String[unionColumnsCount];

        for (int i = 0; i < unionColumnsCount; i++) {
            String unionColumn = unionColumns[i];

            if (unionColumn.equals(typeDiscriminatorColumn)) {
                projectionIn[i] = "'" + typeDiscriminatorValue + "' AS "
                        + typeDiscriminatorColumn;
            } else if (i <= computedColumnsOffset
                    || columnsPresentInTable.contains(unionColumn)) {
                projectionIn[i] = unionColumn;
            } else {
                projectionIn[i] = "NULL AS " + unionColumn;
            }
        }
        return buildQuery(
                projectionIn, selection, groupBy, having,
                null /* sortOrder */,
                null /* limit */);
    }

    @Implementation
    public String buildUnionSubQuery(
            String typeDiscriminatorColumn,
            String[] unionColumns,
            Set<String> columnsPresentInTable,
            int computedColumnsOffset,
            String typeDiscriminatorValue,
            String selection,
            String[] selectionArgs,
            String groupBy,
            String having) {
        return buildUnionSubQuery(
                typeDiscriminatorColumn, unionColumns, columnsPresentInTable,
                computedColumnsOffset, typeDiscriminatorValue, selection,
                groupBy, having);
    }

    @Implementation
    public String buildUnionQuery(String[] subQueries, String sortOrder, String limit) {
        StringBuilder query = new StringBuilder(128);
        int subQueryCount = subQueries.length;
        String unionOperator = mDistinct ? " UNION " : " UNION ALL ";

        for (int i = 0; i < subQueryCount; i++) {
            if (i > 0) {
                query.append(unionOperator);
            }
            query.append(subQueries[i]);
        }
        appendClause(query, " ORDER BY ", sortOrder);
        appendClause(query, " LIMIT ", limit);
        return query.toString();
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

                    if (!mStrict &&
                            ( userColumn.contains(" AS ") || userColumn.contains(" as "))) {
                        /* A column alias already exist */
                        projection[i] = userColumn;
                        continue;
                    }

                    throw new IllegalArgumentException("Invalid column "
                            + projectionIn[i]);
                }
                return projection;
            } else {
                return projectionIn;
            }
        } else if (mProjectionMap != null) {
            // Return all columns in projection map.
            Set<Map.Entry<String, String>> entrySet = mProjectionMap.entrySet();
            String[] projection = new String[entrySet.size()];
            Iterator<Map.Entry<String, String>> entryIter = entrySet.iterator();
            int i = 0;

            while (entryIter.hasNext()) {
                Map.Entry<String, String> entry = entryIter.next();

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
}
