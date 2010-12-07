package com.xtremelabs.robolectric.shadows;

import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;

public class StatusLineStub implements StatusLine {
    @Override public ProtocolVersion getProtocolVersion() {
        throw new UnsupportedOperationException();
    }

    @Override public int getStatusCode() {
        throw new UnsupportedOperationException();
    }

    @Override public String getReasonPhrase() {
        throw new UnsupportedOperationException();
    }
}
