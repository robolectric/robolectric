package com.xtremelabs.robolectric.shadows;

import android.database.sqlite.SQLiteClosable;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadow for  {@code SQLiteCloseable}.  Though {@code SQLiteCloseable} is an abstract
 * class, a shadow is necessary. Its methods that are overridden in subclasses
 * require this shadow in order to be properly instrumented.
 */
@Implements(SQLiteClosable.class)
public class ShadowSQLiteCloseable {

	@Implementation
	public void close() {
	}
	
}
