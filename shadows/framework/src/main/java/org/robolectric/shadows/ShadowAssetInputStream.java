package org.robolectric.shadows;

import android.content.res.AssetManager.AssetInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

@SuppressWarnings("UnusedDeclaration")
@Implements(AssetInputStream.class)
public class ShadowAssetInputStream {

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
    if (RuntimeEnvironment.useLegacyResources()) {
      return delegate.read();
    } else {
      return Shadow.directlyOn(realObject, AssetInputStream.class).read();
    }
  }

  @Implementation
  protected int read(byte[] b) throws IOException {
    if (RuntimeEnvironment.useLegacyResources()) {
      return delegate.read(b);
    } else {
      return Shadow.directlyOn(realObject, AssetInputStream.class).read(b);
    }
  }

  @Implementation
  protected int read(byte[] b, int off, int len) throws IOException {
    if (RuntimeEnvironment.useLegacyResources()) {
      return delegate.read(b, off, len);
    } else {
      return Shadow.directlyOn(realObject, AssetInputStream.class).read(b, off, len);
    }
  }

  @Implementation
  protected long skip(long n) throws IOException {
    if (RuntimeEnvironment.useLegacyResources()) {
      return delegate.skip(n);
    } else {
      return Shadow.directlyOn(realObject, AssetInputStream.class).skip(n);
    }
  }

  @Implementation
  protected int available() throws IOException {
    if (RuntimeEnvironment.useLegacyResources()) {
      return delegate.available();
    } else {
      return Shadow.directlyOn(realObject, AssetInputStream.class).available();
    }
  }

  @Implementation
  protected void close() throws IOException {
    if (RuntimeEnvironment.useLegacyResources()) {
      delegate.close();
    } else {
      Shadow.directlyOn(realObject, AssetInputStream.class).close();
    }
  }

  @Implementation
  protected void mark(int readlimit) {
    if (RuntimeEnvironment.useLegacyResources()) {
      delegate.mark(readlimit);
    } else {
      Shadow.directlyOn(realObject, AssetInputStream.class).mark(readlimit);
    }
  }

  @Implementation
  protected void reset() throws IOException {
    if (RuntimeEnvironment.useLegacyResources()) {
      delegate.reset();
    } else {
      Shadow.directlyOn(realObject, AssetInputStream.class).reset();
    }
  }

  @Implementation
  protected boolean markSupported() {
    if (RuntimeEnvironment.useLegacyResources()) {
      return delegate.markSupported();
    } else {
      return Shadow.directlyOn(realObject, AssetInputStream.class).markSupported();
    }
  }
}
