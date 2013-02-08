package com.xtremelabs.robolectric.shadows;

import android.os.DropBoxManager;
import android.util.Log;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;
import tv.ouya.util.Encodings;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Implements(DropBoxManager.class)
public class ShadowDropBoxManager implements Closeable {
    @RealObject private DropBoxManager realDropBoxManager;

    private static DropBoxManager.Entry nextEntry;

    public DropBoxManager.Entry getNextEntry(String tag, long time) {
        return nextEntry;
    }

    @Implementation
    public void setNextEntry(DropBoxManager.Entry nextEntry) {
        this.nextEntry = nextEntry;
    }

    @Override
    @Implementation
    public void close() throws IOException {
    }

    @Implements(DropBoxManager.Entry.class)
    public static class ShadowEntry {
        @RealObject private DropBoxManager.Entry realEntry;
        private static final String TAG = ShadowEntry.class.getSimpleName();

        private long time;
        private int flags;
        private byte[] data;

        @Implementation
        public int getFlags() {
            return flags;
        }

        public ShadowEntry setFlags(int flags) {
            this.flags = flags;
            return this;
        }

        @Implementation
        public long getTimeMillis() {
            return time;
        }

        public ShadowEntry setTimeMillis(long time) {
            this.time = time;
            return this;
        }

        @Implementation
        public String getText(int maxBytes) throws UnsupportedEncodingException {
            return new String(data, 0, Math.min(maxBytes, data.length), Encodings.UTF_8);
        }

        public ShadowEntry setText(String text) {
            try {
                this.data = text.getBytes(Encodings.UTF_8);
            }
            catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Caught exception: " + e.getClass().getSimpleName(), e);
            }
            return this;
        }
    }
}
