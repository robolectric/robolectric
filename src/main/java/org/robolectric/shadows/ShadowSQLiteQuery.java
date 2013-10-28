package org.robolectric.shadows;

import android.database.sqlite.SQLiteQuery;
import org.robolectric.annotation.Implements;

@Implements(value = SQLiteQuery.class, inheritImplementationMethods = true)
public class ShadowSQLiteQuery extends ShadowSQLiteProgram {

}
