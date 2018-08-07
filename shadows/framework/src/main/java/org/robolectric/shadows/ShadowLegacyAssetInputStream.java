package org.robolectric.shadows;

import android.content.res.AssetManager.AssetInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowAssetInputStream.Picker;

@SuppressWarnings("UnusedDeclaration")
@Implements(value = AssetInputStream.class, shadowPicker = Picker.class)
public class ShadowLegacyAssetInputStream extends ShadowAssetInputStream {

  @RealObject
  private AssetInputStream realObject;

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
