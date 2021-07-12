package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.res.AssetManager.AssetInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowAssetInputStream.Picker;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@SuppressWarnings("UnusedDeclaration")
@Implements(value = AssetInputStream.class, shadowPicker = Picker.class)
public class ShadowLegacyAssetInputStream extends ShadowAssetInputStream {

  @RealObject private AssetInputStream realObject;

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
    return delegate == null
        ? reflector(AssetInputStreamReflector.class, realObject).read()
        : delegate.read();
  }

  @Implementation
  protected int read(byte[] b) throws IOException {
    return delegate == null
        ? reflector(AssetInputStreamReflector.class, realObject).read(b)
        : delegate.read(b);
  }

  @Implementation
  protected int read(byte[] b, int off, int len) throws IOException {
    return delegate == null
        ? reflector(AssetInputStreamReflector.class, realObject).read(b, off, len)
        : delegate.read(b, off, len);
  }

  @Implementation
  protected long skip(long n) throws IOException {
    return delegate == null
        ? reflector(AssetInputStreamReflector.class, realObject).skip(n)
        : delegate.skip(n);
  }

  @Implementation
  protected int available() throws IOException {
    return delegate == null
        ? reflector(AssetInputStreamReflector.class, realObject).available()
        : delegate.available();
  }

  @Implementation
  protected void close() throws IOException {
    if (delegate == null) {
      reflector(AssetInputStreamReflector.class, realObject).close();
    } else {
      delegate.close();
    }
  }

  @Implementation
  protected void mark(int readlimit) {
    if (delegate == null) {
      reflector(AssetInputStreamReflector.class, realObject).mark(readlimit);
    } else {
      delegate.mark(readlimit);
    }
  }

  @Implementation
  protected void reset() throws IOException {
    if (delegate == null) {
      reflector(AssetInputStreamReflector.class, realObject).reset();
    } else {
      delegate.reset();
    }
  }

  @Implementation
  protected boolean markSupported() {
    return delegate == null
        ? reflector(AssetInputStreamReflector.class, realObject).markSupported()
        : delegate.markSupported();
  }

  @ForType(AssetInputStream.class)
  interface AssetInputStreamReflector {

    @Direct
    int read();

    @Direct
    int read(byte[] b);

    @Direct
    int read(byte[] b, int off, int len);

    @Direct
    long skip(long n);

    @Direct
    int available();

    @Direct
    void close();

    @Direct
    void mark(int readlimit);

    @Direct
    void reset();

    @Direct
    boolean markSupported();
  }
}
