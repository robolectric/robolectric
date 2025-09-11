package org.robolectric.shadows.httpclient;

import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;

/**
 * @deprecated Apache HTTP client is deprecated in Android. Please migrate to an other solution
 */
@Deprecated
public class StatusLineStub implements StatusLine {
  @Override
  public ProtocolVersion getProtocolVersion() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getStatusCode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getReasonPhrase() {
    throw new UnsupportedOperationException();
  }
}
