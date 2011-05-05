package com.xtremelabs.robolectric.shadows;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ContentResolver.class)
public class ShadowContentResolver {
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
        return Uri.parse("content://foobar");
    }
}
