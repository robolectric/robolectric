package com.xtremelabs.robolectric.shadows;

import android.content.ContentResolver;
import android.net.Uri;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import java.io.ByteArrayInputStream;
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

            if (uri != null && ContentResolver.SCHEME_ANDROID_RESOURCE.equals(uri.getScheme())) {
                String path = uri.getPath();
                // check that path is a numerical resource id
                if (path != null && path.matches("/[0-9]+")) {
                    int resourceId = Integer.parseInt(path.substring(1));
                    return Robolectric.application.getResources().openRawResource(resourceId);
                }
            }

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

        } else {
            return new ByteArrayInputStream(bytes);
        }
    }
}
