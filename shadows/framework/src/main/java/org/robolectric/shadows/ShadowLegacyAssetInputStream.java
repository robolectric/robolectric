package org.robolectric.shadows;

import android.content.res.AssetManager.AssetInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowAssetInputStream.Picker;

@SuppressWarnings("UnusedDeclaration")
@Implements(value = AssetInputStream.class, shadowPicker = Picker.class)
public class ShadowLegacyAssetInputStream extends ShadowAssetInputStream {

  @RealObject
  private AssetInputStream realObject;

  private InputStream delegate;
  private boolean ninePatch;

  @Override
  InputStream getDelegate() {
    return delegate;
  }

  void setDelegate(InputStream delegate) {
    this.delegate = delegate;
  }

  @Override
  boolean isNinePatch() {
    return ninePatch;
  }

  void setNinePatch(boolean ninePatch) {
    this.ninePatch = ninePatch;
  }

  @Implementation
  protected int read() throws IOException {
    return stream().read();
  }

  @Implementation
  protected int read(byte[] b) throws IOException {
    return stream().read(b);
  }

  @Implementation
  protected int read(byte[] b, int off, int len) throws IOException {
    return stream().read(b, off, len);
  }

  @Implementation
  protected long skip(long n) throws IOException {
    return stream().skip(n);
  }

  @Implementation
  protected int available() throws IOException {
    return stream().available();
  }

  @Implementation
  protected void close() throws IOException {
    stream().close();
  }

  @Implementation
  protected void mark(int readlimit) {
    stream().mark(readlimit);
  }

  @Implementation
  protected void reset() throws IOException {
    stream().reset();
  }

  @Implementation
  protected boolean markSupported() {
    return stream().markSupported();
  }

  private InputStream stream() {
    return delegate == null ? Shadow.directlyOn(realObject, AssetInputStream.class) : delegate;
  }
}
