package com.xtremelabs.robolectric.shadows;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.tester.android.database.TestCursor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({ "UnusedDeclaration" })
@Implements(ContentResolver.class)
public class ShadowContentResolver {
    private int nextDatabaseIdForInserts;

    private TestCursor cursor;
    private final List<DeleteStatement> deleteStatements = new ArrayList<DeleteStatement>();
    private HashMap<Uri, TestCursor> uriCursorMap = new HashMap<Uri, TestCursor>();

    @Implementation
    public final InputStream openInputStream(final Uri uri) {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                throw new UnsupportedOperationException();
            }

            @Override
            public String toString() {
                return "stream for " + uri;
            }
        };
    }

    @Implementation
    public final Uri insert(Uri url, ContentValues values) {
        return Uri.parse(url.toString() + "/" + nextDatabaseIdForInserts++);
    }

    @Implementation
    public final Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        TestCursor returnCursor = getCursor(uri);
        if (returnCursor == null) {
            return null;
        }

        returnCursor.setQuery(uri, projection, selection, selectionArgs,
                sortOrder);
        return returnCursor;
    }

    @Implementation
    public final int delete(Uri url, String where, String[] selectionArgs) {
        DeleteStatement deleteStatement = new DeleteStatement(url, where, selectionArgs);
        deleteStatements.add(deleteStatement);
        return 1;
    }

    public void setCursor(TestCursor cursor) {
        this.cursor = cursor;
    }

    public void setCursor(Uri uri, TestCursor cursorForUri) {
        this.uriCursorMap.put(uri, cursorForUri);
    }

    public void setNextDatabaseIdForInserts(int nextId) {
        nextDatabaseIdForInserts = nextId;
    }

    public List<Uri> getDeletedUris() {
        List<Uri> uris = new ArrayList<Uri>();
        for (DeleteStatement deleteStatement : deleteStatements) {
            uris.add(deleteStatement.getUri());
        }
        return uris;
    }

    public List<DeleteStatement> getDeleteStatements() {
        return deleteStatements;
    }

    private TestCursor getCursor(Uri uri) {
        if (uriCursorMap.get(uri) != null) {
            return uriCursorMap.get(uri);
        } else if (cursor != null) {
            return cursor;
        } else {
            return null;
        }
    }

    public class DeleteStatement {
        private final Uri uri;
        private final String where;
        private final String[] selectionArgs;

        public DeleteStatement(Uri uri, String where, String[] selectionArgs) {
            this.uri = uri;
            this.where = where;
            this.selectionArgs = selectionArgs;
        }

        public Uri getUri() {
            return uri;
        }

        public String getWhere() {
            return where;
        }

        public String[] getSelectionArgs() {
            return selectionArgs;
        }
    }
}
