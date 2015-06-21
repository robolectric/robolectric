package org.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.DisplayMetrics;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.ResName;
import org.robolectric.util.ReflectionHelpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.robolectric.Shadows.shadowOf;

/**
 * Shadow for {@link android.graphics.Bitmap}.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Bitmap.class)
public class ShadowBitmap {
  @RealObject
  private Bitmap realBitmap;

  int createdFromResId = -1;
  String createdFromPath;
  InputStream createdFromStream;
  byte[] createdFromBytes;
  private Bitmap createdFromBitmap;
  private int createdFromX = -1;
  private int createdFromY = -1;
  private int createdFromWidth = -1;
  private int createdFromHeight = -1;
  private int[] createdFromColors;
  private Matrix createdFromMatrix;
  private boolean createdFromFilter;

  private int width;
  private int height;
  private int density;
  private int[] colors;
  private Bitmap.Config config;
  private boolean mutable;
  private String description = "";
  private boolean recycled = false;

  /**
   * Returns a textual representation of the appearance of the object.
   *
   * @param bitmap the bitmap to visualize
   * @return Textual representation of the appearance of the object.
   */
  public static String visualize(Bitmap bitmap) {
    return shadowOf(bitmap).getDescription();
  }

  /**
   * Reference to original Bitmap from which this Bitmap was created. {@code null} if this Bitmap
   * was not copied from another instance.
   *
   * @return Original Bitmap from which this Bitmap was created.
   */
  public Bitmap getCreatedFromBitmap() {
    return createdFromBitmap;
  }

  /**
   * Resource ID from which this Bitmap was created. {@code 0} if this Bitmap was not created
   * from a resource.
   *
   * @return Resource ID from which this Bitmap was created.
   */
  public int getCreatedFromResId() {
    return createdFromResId;
  }

  /**
   * Path from which this Bitmap was created. {@code null} if this Bitmap was not create from a
   * path.
   *
   * @return Path from which this Bitmap was created.
   */
  public String getCreatedFromPath() {
    return createdFromPath;
  }

  /**
   * {@link InputStream} from which this Bitmap was created. {@code null} if this Bitmap was not
   * created from a stream.
   *
   * @return InputStream from which this Bitmap was created.
   */
  public InputStream getCreatedFromStream() {
    return createdFromStream;
  }

  /**
   * Bytes from which this Bitmap was created. {@code null} if this Bitmap was not created from
   * bytes.
   *
   * @return Bytes from which this Bitmap was created.
   */
  public byte[] getCreatedFromBytes() {
    return createdFromBytes;
  }

  /**
   * Horizontal offset within {@link #getCreatedFromBitmap()} of this Bitmap's content, or -1.
   *
   * @return Horizontal offset within {@link #getCreatedFromBitmap()}.
   */
  public int getCreatedFromX() {
    return createdFromX;
  }

  /**
   * Vertical offset within {@link #getCreatedFromBitmap()} of this Bitmap's content, or -1.
   *
   * @return Vertical offset within {@link #getCreatedFromBitmap()} of this Bitmap's content, or -1.
   */
  public int getCreatedFromY() {
    return createdFromY;
  }

  /**
   * Width from {@link #getCreatedFromX()} within {@link #getCreatedFromBitmap()} of this Bitmap's
   * content, or -1.
   *
   * @return Width from {@link #getCreatedFromX()} within {@link #getCreatedFromBitmap()} of this Bitmap's
   * content, or -1.
   */
  public int getCreatedFromWidth() {
    return createdFromWidth;
  }

  /**
   * Height from {@link #getCreatedFromX()} within {@link #getCreatedFromBitmap()} of this Bitmap's
   * content, or -1.
   * @return Height from {@link #getCreatedFromX()} within {@link #getCreatedFromBitmap()} of this Bitmap's
   * content, or -1.
   */
  public int getCreatedFromHeight() {
    return createdFromHeight;
  }

  /**
   * Color array from which this Bitmap was created. {@code null} if this Bitmap was not created
   * from a color array.
   * @return Color array from which this Bitmap was created.
   */
  public int[] getCreatedFromColors() {
    return createdFromColors;
  }

  /**
   * Matrix from which this Bitmap's content was transformed, or {@code null}.
   * @return Matrix from which this Bitmap's content was transformed, or {@code null}.
   */
  public Matrix getCreatedFromMatrix() {
    return createdFromMatrix;
  }

  /**
   * {@code true} if this Bitmap was created with filtering.
   * @return {@code true} if this Bitmap was created with filtering.
   */
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
    return createBitmap((DisplayMetrics) null, width, height, config);
  }

  @Implementation
  public static Bitmap createBitmap(DisplayMetrics displayMetrics, int width, int height, Bitmap.Config config) {
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException("width and height must be > 0");
    }
    Bitmap scaledBitmap = ReflectionHelpers.callConstructor(Bitmap.class);
    ShadowBitmap shadowBitmap = shadowOf(scaledBitmap);
    shadowBitmap.setDescription("Bitmap (" + width + " x " + height + ")");

    shadowBitmap.width = width;
    shadowBitmap.height = height;
    shadowBitmap.config = config;
    shadowBitmap.setMutable(true);
    if (displayMetrics != null) {
      shadowBitmap.density = displayMetrics.densityDpi;
    }
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

    Bitmap scaledBitmap = ReflectionHelpers.callConstructor(Bitmap.class);
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

    Bitmap newBitmap = ReflectionHelpers.callConstructor(Bitmap.class);
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

    if (x + width > src.getWidth()) {
      throw new IllegalArgumentException("x + width must be <= bitmap.width()");
    }
    if (y + height > src.getHeight()) {
      throw new IllegalArgumentException("y + height must be <= bitmap.height()");
    }

    Bitmap newBitmap = ReflectionHelpers.callConstructor(Bitmap.class);
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
  public static Bitmap createBitmap(int[] colors, int width, int height, Bitmap.Config config) {
    if (colors.length != width * height) {
      throw new IllegalArgumentException("array length (" + colors.length + ") did not match width * height (" + (width * height) + ")");
    }

    Bitmap newBitmap = Bitmap.createBitmap(width, height, config);
    ShadowBitmap shadowBitmap = shadowOf(newBitmap);

    shadowBitmap.setMutable(false);
    shadowBitmap.createdFromColors = colors;
    shadowBitmap.colors = new int[colors.length];
    System.arraycopy(colors, 0, shadowBitmap.colors, 0, colors.length);
    return newBitmap;
  }

  @Implementation
  public int getPixel(int x, int y) {
    internalCheckPixelAccess(x, y);
    if (colors != null) {
      // Note that getPixel() returns a non-premultiplied ARGB value; if
      // config is RGB_565, our return value will likely be more precise than
      // on a physical device, since it needs to map each color component from
      // 5 or 6 bits to 8 bits.
      return colors[y * getWidth() + x];
    } else {
      return 0;
    }
  }

  @Implementation
  public void setPixel(int x, int y, int color) {
    if (isRecycled()) {
      throw new IllegalStateException("Can't call setPixel() on a recycled bitmap");
    } else if (!isMutable()) {
      throw new IllegalStateException("Bitmap is immutable");
    }
    internalCheckPixelAccess(x, y);
    if (colors == null) {
      colors = new int[getWidth() * getHeight()];
    }
    colors[y * getWidth() + x] = color;
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
    Bitmap newBitmap = ReflectionHelpers.callConstructor(Bitmap.class);
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

  @Implementation
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

  @Implementation
  public void setWidth(int width) {
    this.width = width;
  }

  @Implementation
  public int getWidth() {
    return width;
  }

  @Implementation
  public void setHeight(int height) {
    this.height = height;
  }

  @Implementation
  public int getHeight() {
    return height;
  }

  @Implementation
  public void setDensity(int density) {
    this.density = density;
  }

  @Implementation
  public int getDensity() {
    return density;
  }

  @Override
  public String toString() {
    return "Bitmap{description='" + description + '\'' + ", width=" + width + ", height=" + height + '}';
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

  private void internalCheckPixelAccess(int x, int y) {
    if (x < 0) {
      throw new IllegalArgumentException("x must be >= 0");
    }
    if (y < 0) {
      throw new IllegalArgumentException("y must be >= 0");
    }
    if (x >= getWidth()) {
      throw new IllegalArgumentException("x must be < bitmap.width()");
    }
    if (y >= getHeight()) {
      throw new IllegalArgumentException("y must be < bitmap.height()");
    }
  }
}
