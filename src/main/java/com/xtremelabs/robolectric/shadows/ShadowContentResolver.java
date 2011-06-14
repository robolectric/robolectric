package com.xtremelabs.robolectric.shadows;

import java.io.ByteArrayInputStream;

import android.content.ContentResolver;
import android.net.Uri;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ContentResolver.class)
public class ShadowContentResolver {
    private byte[] bytes;

    public void setStreamData(byte[] bytes) {
        this.bytes = bytes;
    }
    
    @Implementation
    public final InputStream openInputStream(final Uri uri) {
        if (bytes == null) {
            return new InputStream() {
                @Override public int read() throws IOException {
                    throw new UnsupportedOperationException();
                }
    
                @Override public String toString() {
                    return "stream for " + uri;
                }
            };
            
        } else {
          return new ByteArrayInputStream(bytes);
        }
    }
}
