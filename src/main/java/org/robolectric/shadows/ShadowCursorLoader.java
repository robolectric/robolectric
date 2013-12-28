package org.robolectric.shadows;

import android.database.Cursor;
import org.robolectric.annotation.Implements;
import android.support.v4.content.CursorLoader;

@Implements(CursorLoader.class)
public class ShadowCursorLoader extends ShadowAsyncTaskLoader<Cursor> {
}
