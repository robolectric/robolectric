package com.xtremelabs.robolectric.shadows;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
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

@Implements(ContentResolver.class)
public class ShadowContentResolver {
    private int nextDatabaseIdForInserts;
    private int nextDatabaseIdForUpdates;

    private TestCursor cursor;
    private final List<InsertStatement> insertStatements = new ArrayList<InsertStatement>();
    private final List<UpdateStatement> updateStatements = new ArrayList<UpdateStatement>();
    private final List<DeleteStatement> deleteStatements = new ArrayList<DeleteStatement>();
    private List<NotifiedUri> notifiedUris = new ArrayList<NotifiedUri>();
    private HashMap<Uri, TestCursor> uriCursorMap = new HashMap<Uri, TestCursor>();

    public static class NotifiedUri {
        public final Uri uri;
        public final boolean syncToNetwork;
        public final ContentObserver observer;

        public NotifiedUri(Uri uri, ContentObserver observer, boolean syncToNetwork) {
            this.uri = uri;
            this.syncToNetwork = syncToNetwork;
            this.observer = observer;
        }
    }

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
        InsertStatement insertStatement = new InsertStatement(url, new ContentValues(values));
        insertStatements.add(insertStatement);
        return Uri.parse(url.toString() + "/" + nextDatabaseIdForInserts++);
    }
    
    @Implementation
    public int update(Uri uri, ContentValues values, String where, String[] selectionArgs) {
        UpdateStatement updateStatement = new UpdateStatement(uri, new ContentValues(values), where, selectionArgs);
        updateStatements.add(updateStatement);
        return nextDatabaseIdForUpdates++;
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

    @Implementation
    public void notifyChange(Uri uri, ContentObserver observer, boolean syncToNetwork) {
       notifiedUris.add(new NotifiedUri(uri, observer, syncToNetwork));
    }

    @Implementation
    public void notifyChange(Uri uri, ContentObserver observer) {
        notifyChange(uri, observer, false);
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
    
    public void setNextDatabaseIdForUpdates(int nextId) {
        nextDatabaseIdForUpdates = nextId;
    }

    public List<InsertStatement> getInsertStatements() {
        return insertStatements;
    }
    
    public List<UpdateStatement> getUpdateStatements() {
        return updateStatements;
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
    
    public List<NotifiedUri> getNotifiedUris() {
        return notifiedUris;
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
    
    public class InsertStatement {
        private final Uri uri;
        private final ContentValues contentValues;
        
        public InsertStatement(Uri uri, ContentValues contentValues) {
            this.uri = uri;
            this.contentValues = contentValues;
        }
        
        public Uri getUri() {
            return uri;
        }
        
        public ContentValues getContentValues() {
            return contentValues;
        }
    }
    
    public class UpdateStatement {
        private final Uri uri;
        private final ContentValues values;
        private final String where;
        private final String[] selectionArgs;
        
        public UpdateStatement(Uri uri, ContentValues values, String where, String[] selectionArgs) {
            this.uri = uri;
            this.values = values;
            this.where = where;
            this.selectionArgs = selectionArgs;
        }
        
        public Uri getUri() {
            return uri;
        }
        
        public ContentValues getContentValues() {
            return values;
        }
        
        public String getWhere() {
            return where;
        }
        
        public String[] getSelectionArgs() {
            return selectionArgs;
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
