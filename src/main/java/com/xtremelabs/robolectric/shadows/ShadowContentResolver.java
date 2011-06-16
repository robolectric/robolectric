package com.xtremelabs.robolectric.shadows;

import android.content.ContentResolver;
import android.net.Uri;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ContentResolver.class)
public class ShadowContentResolver {

    @Implementation
    public final InputStream openInputStream(final Uri uri) {

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

    }
}
