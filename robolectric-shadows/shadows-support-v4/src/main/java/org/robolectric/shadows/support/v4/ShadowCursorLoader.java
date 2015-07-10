package org.robolectric.shadows.support.v4;

import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.support.v4.content.CursorLoader}.
 */
@Implements(CursorLoader.class)
public class ShadowCursorLoader extends ShadowAsyncTaskLoader<Cursor> {
}
