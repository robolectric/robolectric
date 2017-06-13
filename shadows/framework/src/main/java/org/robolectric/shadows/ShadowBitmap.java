package org.robolectric.shadows;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Parcel;
import android.util.DisplayMetrics;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Bitmap.class)
public class ShadowBitmap {
  /** Number of bytes used internally to represent each pixel (in the {@link #colors} array) */
  private static final int INTERNAL_BYTES_PER_PIXEL = 4;

  @RealObject
  private Bitmap realBitmap;

  int createdFromResId = -1;
  String createdFromPath;
  InputStream createdFromStream;
  FileDescriptor createdFromFileDescriptor;
  byte[] createdFromBytes;
  private Bitmap createdFromBitmap;
  private int createdFromX = -1;
  private int createdFromY = -1;
  private int createdFromWidth = -1;
  private int createdFromHeight = -1;
  private int[] createdFromColors;
  private Matrix createdFromMatrix;
  private boolean createdFromFilter;
  private boolean hasAlpha;

  private int width;
  private int height;
  private int density;
  private int[] colors;
  private Bitmap.Config config;
  private boolean mutable;
  private String description = "";
  private boolean recycled = false;
  private boolean hasMipMap;

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
      stream.write((description + " compressed as " + format + " with quality " + quality).getBytes(UTF_8));
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
  public static Bitmap createBitmap(DisplayMetrics displayMetrics, int width, int height, Bitmap.Config config, boolean hasAlpha) {
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
    shadowBitmap.setPixels(new int[shadowBitmap.getHeight() * shadowBitmap.getWidth()], 0, shadowBitmap.getWidth(), 0, 0, shadowBitmap.getWidth(), shadowBitmap.getHeight());
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
    shadowBitmap.setPixels(new int[shadowBitmap.getHeight() * shadowBitmap.getWidth()], 0, 0, 0, 0, shadowBitmap.getWidth(), shadowBitmap.getHeight());
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
  public void setPixels(int[] pixels, int offset, int stride,
                        int x, int y, int width, int height) {
    this.colors = pixels;
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
    shadowBitmap.appendDescription(" at (" + x + "," + y + ")");
    shadowBitmap.appendDescription(" with width " + width + " and height " + height);
    if (matrix != null) {
      shadowBitmap.appendDescription(" using matrix " + shadowOf(matrix).getDescription());

      // Adjust width and height by using the matrix.
      RectF mappedRect = new RectF();
      matrix.mapRect(mappedRect, new RectF(0, 0, width, height));
      width = Math.round(mappedRect.width());
      height = Math.round(mappedRect.height());
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

  /**
   * Note that this method will return a RuntimeException unless:
   * - {@code pixels} has the same length as the number of pixels of the bitmap.
   * - {@code x = 0}
   * - {@code y = 0}
   * - {@code width} and {@code height} height match the current bitmap's dimensions.
   */
  @Implementation
  public void getPixels(int[] pixels, int offset, int stride, int x, int y, int width, int height) {
    if (x != 0 ||
        y != 0 ||
        width != getWidth() ||
        height != getHeight() ||
        pixels.length != colors.length) {
      throw new RuntimeException("Not implemented.");
    }
    System.arraycopy(colors, 0, pixels, 0, colors.length);
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
  public final boolean hasAlpha() {
    return hasAlpha;
  }

  @Implementation
  public void setHasAlpha(boolean hasAlpha) {
    this.hasAlpha = hasAlpha;
  }

  @Implementation
  public final boolean hasMipMap() {
    return hasMipMap;
  }

  @Implementation
  public final void setHasMipMap(boolean hasMipMap) {
    this.hasMipMap = hasMipMap;
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

  @Implementation
  public int getGenerationId() {
    return 0;
  }

  @Implementation
  public Bitmap createAshmemBitmap() {
    return realBitmap;
  }

  @Implementation
  public void eraseColor(int c) {

  }

  @Implementation
  public void writeToParcel(Parcel p, int flags) {
    p.writeInt(width);
    p.writeInt(height);
    p.writeSerializable(config);
    p.writeIntArray(colors);
  }

  @Implementation
  public static Bitmap nativeCreateFromParcel(Parcel p) {
    int parceledWidth = p.readInt();
    int parceledHeight = p.readInt();
    Bitmap.Config parceledConfig = (Bitmap.Config) p.readSerializable();

    int[] parceledColors = new int[parceledHeight * parceledWidth];
    p.readIntArray(parceledColors);

    return createBitmap(parceledColors, parceledWidth, parceledHeight, parceledConfig);
  }

  @Implementation
  public void copyPixelsFromBuffer(Buffer dst) {
    if (isRecycled()) {
      throw new IllegalStateException("Can't call copyPixelsFromBuffer() on a recycled bitmap");
    }

    // See the related comment in #copyPixelsToBuffer(Buffer).
    if (getBytesPerPixel(config) != INTERNAL_BYTES_PER_PIXEL) {
      throw new RuntimeException("Not implemented: only Bitmaps with " + INTERNAL_BYTES_PER_PIXEL
              + " bytes per pixel are supported");
    }
    if (!(dst instanceof ByteBuffer)) {
      throw new RuntimeException("Not implemented: unsupported Buffer subclass");
    }

    ByteBuffer byteBuffer = (ByteBuffer) dst;
    if (byteBuffer.remaining() < colors.length * INTERNAL_BYTES_PER_PIXEL) {
      throw new RuntimeException("Buffer not large enough for pixels");
    }

    for (int i = 0; i < colors.length; i++) {
      colors[i] = byteBuffer.getInt();
    }
  }

  @Implementation
  public void copyPixelsToBuffer(Buffer dst) {
    // Ensure that the Bitmap uses 4 bytes per pixel, since we always use 4 bytes per pixels
    // internally. Clients of this API probably expect that the buffer size must be >=
    // getByteCount(), but if we don't enforce this restriction then for RGB_4444 and other
    // configs that value would be smaller then the buffer size we actually need.
    if (getBytesPerPixel(config) != INTERNAL_BYTES_PER_PIXEL) {
      throw new RuntimeException("Not implemented: only Bitmaps with " + INTERNAL_BYTES_PER_PIXEL
              + " bytes per pixel are supported");
    }

    if (!(dst instanceof ByteBuffer)) {
      throw new RuntimeException("Not implemented: unsupported Buffer subclass");
    }

    ByteBuffer byteBuffer = (ByteBuffer) dst;
    for (int color : colors) {
      byteBuffer.putInt(color);
    }
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

  public void setCreatedFromResId(int resId, String description) {
    this.createdFromResId = resId;
    appendDescription(" for resource:" + description);
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
