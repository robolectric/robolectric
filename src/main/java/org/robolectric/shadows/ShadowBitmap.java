package org.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.ResName;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Bitmap.class)
public class ShadowBitmap {
  @RealObject private Bitmap realBitmap;

  int createdFromResId = -1;
  String createdFromPath;
  InputStream createdFromStream;
  byte[] createdFromBytes;
  private Bitmap createdFromBitmap;
  private int createdFromX = -1;
  private int createdFromY = -1;
  private int createdFromWidth = -1;
  private int createdFromHeight = -1;
  private Matrix createdFromMatrix;
  private boolean createdFromFilter;

  private int width;
  private int height;
  private Bitmap.Config config;
  private boolean mutable;
  private String description = "";
  private boolean recycled = false;

  /**
   * Reference to original Bitmap from which this Bitmap was created. {@code null} if this Bitmap
   * was not copied from another instance.
   */
  public Bitmap getCreatedFromBitmap() {
    return createdFromBitmap;
  }

  /**
   * Resource ID from which this Bitmap was created. {@code 0} if this Bitmap was not created
   * from a resource.
   */
  public int getCreatedFromResId() {
    return createdFromResId;
  }

  /**
   * Path from which this Bitmap was created. {@code null} if this Bitmap was not create from a
   * path.
   */
  public String getCreatedFromPath() {
    return createdFromPath;
  }

  /**
   * {@link InputStream} from which this Bitmap was created. {@code null} if this Bitmap was not
   * created from a stream.
   */
  public InputStream getCreatedFromStream() {
    return createdFromStream;
  }

  /**
   * Bytes from which this Bitmap was created. {@code null} if this Bitmap was not created from
   * bytes.
   */
  public byte[] getCreatedFromBytes() {
    return createdFromBytes;
  }

  /** Horizontal offset within {@link #getCreatedFromBitmap()} of this Bitmap's content, or -1. */
  public int getCreatedFromX() {
    return createdFromX;
  }
  /** Vertical offset within {@link #getCreatedFromBitmap()} of this Bitmap's content, or -1. */
  public int getCreatedFromY() {
    return createdFromY;
  }

  /**
   * Width from {@link #getCreatedFromX()} within {@link #getCreatedFromBitmap()} of this Bitmap's
   * content, or -1.
   */
  public int getCreatedFromWidth() {
    return createdFromWidth;
  }

  /**
   * Height from {@link #getCreatedFromX()} within {@link #getCreatedFromBitmap()} of this Bitmap's
   * content, or -1.
   */
  public int getCreatedFromHeight() {
    return createdFromHeight;
  }

  /** Matrix from which this Bitmap's content was transformed, or {@code null}. */
  public Matrix getCreatedFromMatrix() {
    return createdFromMatrix;
  }

  /** {@code true} if this Bitmap was created with filtering. */
  public boolean getCreatedFromFilter() {
    return createdFromFilter;
  }

  @Implementation
  public boolean compress(Bitmap.CompressFormat format, int quality, OutputStream stream) {
    try {
      stream.write((description + " compressed as " + format + " with quality " + quality).getBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return true;
  }

  @Implementation
  public static Bitmap createBitmap(int width, int height, Bitmap.Config config) {
    Bitmap scaledBitmap = Robolectric.newInstanceOf(Bitmap.class);
    ShadowBitmap shadowBitmap = shadowOf(scaledBitmap);

    shadowBitmap.setDescription("Bitmap (" + width + " x " + height + ")");

    shadowBitmap.width = width;
    shadowBitmap.height = height;
    shadowBitmap.config = config;
    return scaledBitmap;
  }

  @Implementation
  public static Bitmap createBitmap(Bitmap src) {
    ShadowBitmap shadowBitmap = shadowOf(src);
    shadowBitmap.appendDescription(" created from Bitmap object");
    return src;
  }

  @Implementation
  public static Bitmap createScaledBitmap(Bitmap src, int dstWidth, int dstHeight, boolean filter) {
    if (dstWidth == src.getWidth() && dstHeight == src.getHeight() && !filter) {
      return src; // Return the original.
    }

    Bitmap scaledBitmap = Robolectric.newInstanceOf(Bitmap.class);
    ShadowBitmap shadowBitmap = shadowOf(scaledBitmap);

    shadowBitmap.appendDescription(shadowOf(src).getDescription());
    shadowBitmap.appendDescription(" scaled to " + dstWidth + " x " + dstHeight);
    if (filter) {
      shadowBitmap.appendDescription(" with filter " + filter);
    }

    shadowBitmap.createdFromBitmap = src;
    shadowBitmap.createdFromFilter = filter;
    shadowBitmap.width = dstWidth;
    shadowBitmap.height = dstHeight;
    return scaledBitmap;
  }

  @Implementation
  public static Bitmap createBitmap(Bitmap src, int x, int y, int width, int height) {
    if (x == 0 && y == 0 && width == src.getWidth() && height == src.getHeight()) {
      return src; // Return the original.
    }

    Bitmap newBitmap = Robolectric.newInstanceOf(Bitmap.class);
    ShadowBitmap shadowBitmap = shadowOf(newBitmap);

    shadowBitmap.appendDescription(shadowOf(src).getDescription());
    shadowBitmap.appendDescription(" at (" + x + "," + y);
    shadowBitmap.appendDescription(" with width " + width + " and height " + height);

    shadowBitmap.createdFromBitmap = src;
    shadowBitmap.createdFromX = x;
    shadowBitmap.createdFromY = y;
    shadowBitmap.createdFromWidth = width;
    shadowBitmap.createdFromHeight = height;
    shadowBitmap.width = width;
    shadowBitmap.height = height;
    return newBitmap;
  }

  @Implementation
  public static Bitmap createBitmap(Bitmap src, int x, int y, int width, int height, Matrix matrix, boolean filter) {
    if (x == 0 && y == 0 && width == src.getWidth() && height == src.getHeight() && (matrix == null || matrix.isIdentity())) {
      return src; // Return the original.
    }

    Bitmap newBitmap = Robolectric.newInstanceOf(Bitmap.class);
    ShadowBitmap shadowBitmap = shadowOf(newBitmap);

    shadowBitmap.appendDescription(shadowOf(src).getDescription());
    shadowBitmap.appendDescription(" at (" + x + "," + y);
    shadowBitmap.appendDescription(" with width " + width + " and height " + height);
    if (matrix != null) {
      shadowBitmap.appendDescription(" using matrix " + matrix);
    }
    if (filter) {
      shadowBitmap.appendDescription(" with filter");
    }

    shadowBitmap.createdFromBitmap = src;
    shadowBitmap.createdFromX = x;
    shadowBitmap.createdFromY = y;
    shadowBitmap.createdFromWidth = width;
    shadowBitmap.createdFromHeight = height;
    shadowBitmap.createdFromMatrix = matrix;
    shadowBitmap.createdFromFilter = filter;
    shadowBitmap.width = width;
    shadowBitmap.height = height;
    return newBitmap;
  }

  @Implementation
  public static Bitmap createBitmap(int[] ignored, int width, int height, Bitmap.Config config) {
    return Bitmap.createBitmap(width, height, config);
  }

  @Implementation
  public int getRowBytes() {
    return getBytesPerPixel(config) * getWidth();
  }

  @Implementation
  public int getByteCount() {
    return getRowBytes() * getHeight();
  }

  @Implementation
  public void recycle() {
    recycled = true;
  }

  @Implementation
  public final boolean isRecycled() {
    return recycled;
  }

  @Implementation
  public Bitmap copy(Bitmap.Config config, boolean isMutable) {
    Bitmap newBitmap = Robolectric.newInstanceOf(Bitmap.class);
    ShadowBitmap shadowBitmap = shadowOf(newBitmap);
    shadowBitmap.createdFromBitmap = realBitmap;
    shadowBitmap.config = config;
    shadowBitmap.mutable = isMutable;
    return newBitmap;
  }

  @Implementation
  public final Bitmap.Config getConfig() {
    return config;
  }

  public void setConfig(Bitmap.Config config) {
    this.config = config;
  }

  @Implementation
  public final boolean isMutable() {
    return mutable;
  }

  public void setMutable(boolean mutable) {
    this.mutable = mutable;
  }

  public void appendDescription(String s) {
    description += s;
  }

  public void setDescription(String s) {
    description = s;
  }

  public String getDescription() {
    return description;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  @Implementation
  public int getWidth() {
    return width;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  @Implementation
  public int getHeight() {
    return height;
  }

  @Override @Implementation
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Bitmap)) return false;

    ShadowBitmap that = shadowOf((Bitmap) o);

    if (height != that.height) return false;
    if (width != that.width) return false;
    if (description != null ? !description.equals(that.description) : that.description != null) return false;

    return true;
  }

  @Override @Implementation
  public int hashCode() {
    int result = width;
    result = 31 * result + height;
    result = 31 * result + (description != null ? description.hashCode() : 0);
    return result;
  }

  @Override @Implementation
  public String toString() {
    return "Bitmap{" +
        "description='" + description + '\'' +
        ", width=" + width +
        ", height=" + height +
        '}';
  }

  public Bitmap getRealBitmap() {
    return realBitmap;
  }

  public static int getBytesPerPixel(Bitmap.Config config) {
    if (config == null) {
      throw new NullPointerException("Bitmap config was null.");
    }
    switch (config) {
      case ARGB_8888:
        return 4;
      case RGB_565:
      case ARGB_4444:
        return 2;
      case ALPHA_8:
        return 1;
      default:
        throw new IllegalArgumentException("Unknown bitmap config: " + config);
    }
  }

  public void setCreatedFromResId(int resId, ResName resName) {
    this.createdFromResId = resId;
    appendDescription(" for resource:" + resName.getFullyQualifiedName());
  }
}
