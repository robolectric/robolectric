// BEGIN-INTERNAL
package org.robolectric.shadows;

import android.content.res.AssetManager.AssetInputStream;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("UnusedDeclaration")
@Implements(AssetInputStream.class)
public class ShadowAssetInputStream {

  private InputStream delegate;
  private boolean ninePatch;

  InputStream getDelegate() {
    return delegate;
  }

  void setDelegate(InputStream delegate) {
    this.delegate = delegate;
  }

  boolean isNinePatch() {
    return ninePatch;
  }

  void setNinePatch(boolean ninePatch) {
    this.ninePatch = ninePatch;
  }

  @Implementation
  protected int read() throws IOException {
    return delegate.read();
  }

  @Implementation
  protected int read(byte[] b) throws IOException {
    return delegate.read(b);
  }

  @Implementation
  protected int read(byte[] b, int off, int len) throws IOException {
    return delegate.read(b, off, len);
  }

  @Implementation
  protected long skip(long n) throws IOException {
    return delegate.skip(n);
  }

  @Implementation
  protected int available() throws IOException {
    return delegate.available();
  }

  @Implementation
  protected void close() throws IOException {
    delegate.close();
  }

  @Implementation
  protected void mark(int readlimit) {
    delegate.mark(readlimit);
  }

  @Implementation
  protected void reset() throws IOException {
    delegate.reset();
  }

  @Implementation
  protected boolean markSupported() {
    return delegate.markSupported();
  }
}
// END-INTERNAL
