package com.xtremelabs.robolectric.shadows;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ContentResolver.class)
public class ShadowContentResolver {
    private int nextDatabaseIdForInserts;

    private Cursor cursor;
    private List<Uri> deletedUris = new ArrayList<Uri>();

    @Implementation
    public final InputStream openInputStream(final Uri uri) {
        return new InputStream() {
            @Override public int read() throws IOException {
                throw new UnsupportedOperationException();
            }

            @Override public String toString() {
                return "stream for " + uri;
            }
        };
    }

    @Implementation
    public final Uri insert(Uri url, ContentValues values) {
        return Uri.parse(url.toString() + "/" + nextDatabaseIdForInserts++);
    }

    @Implementation
    public final Cursor query(Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        return cursor;
    }

    @Implementation
    public final int delete(Uri url, String where, String[] selectionArgs) {
        deletedUris.add(url);
        return 1;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    public void setNextDatabaseIdForInserts(int nextId) {
        nextDatabaseIdForInserts = nextId;
    }

    public List<Uri> getDeletedUris() {
        return deletedUris;
    }
}
