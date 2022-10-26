package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Integer.max;
import static java.lang.Integer.min;

import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Parcel;
import android.util.DisplayMetrics;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = Bitmap.class, isInAndroidSdk = false)
public class ShadowLegacyBitmap extends ShadowBitmap {
  /** Number of bytes used internally to represent each pixel */
  private static final int INTERNAL_BYTES_PER_PIXEL = 4;

  int createdFromResId = -1;
  String createdFromPath;
  InputStream createdFromStream;
  FileDescriptor createdFromFileDescriptor;
  byte[] createdFromBytes;
  @RealObject private Bitmap realBitmap;
  private Bitmap createdFromBitmap;
  private Bitmap scaledFromBitmap;
  private int createdFromX = -1;
  private int createdFromY = -1;
  private int createdFromWidth = -1;
  private int createdFromHeight = -1;
  private int[] createdFromColors;
  private Matrix createdFromMatrix;
  private boolean createdFromFilter;

  private int width;
  private int height;
  private BufferedImage bufferedImage;
  private Bitmap.Config config;
  private boolean mutable = true;
  private String description = "";
  private boolean recycled = false;
  private boolean hasMipMap;
  private boolean requestPremultiplied = true;
  private boolean hasAlpha;
  private ColorSpace colorSpace;

  @Implementation
  protected static Bitmap createBitmap(int width, int height, Bitmap.Config config) {
    return createBitmap((DisplayMetrics) null, width, height, config);
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected static Bitmap createBitmap(
      DisplayMetrics displayMetrics, int width, int height, Bitmap.Config config) {
    return createBitmap(displayMetrics, width, height, config, true);
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected static Bitmap createBitmap(
      DisplayMetrics displayMetrics,
      int width,
      int height,
      Bitmap.Config config,
      boolean hasAlpha) {
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException("width and height must be > 0");
    }
    checkNotNull(config);
    Bitmap scaledBitmap = ReflectionHelpers.callConstructor(Bitmap.class);
    ShadowLegacyBitmap shadowBitmap = Shadow.extract(scaledBitmap);
    shadowBitmap.setDescription("Bitmap (" + width + " x " + height + ")");

    shadowBitmap.width = width;
    shadowBitmap.height = height;
    shadowBitmap.config = config;
    shadowBitmap.hasAlpha = hasAlpha;
    shadowBitmap.setMutable(true);
    if (displayMetrics != null) {
      scaledBitmap.setDensity(displayMetrics.densityDpi);
    }
    shadowBitmap.bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    if (RuntimeEnvironment.getApiLevel() >= O) {
      shadowBitmap.colorSpace = ColorSpace.get(ColorSpace.Named.SRGB);
    }
    return scaledBitmap;
  }

  @Implementation(minSdk = O)
  protected static Bitmap createBitmap(
      int width, int height, Bitmap.Config config, boolean hasAlpha, ColorSpace colorSpace) {
    checkArgument(colorSpace != null || config == Bitmap.Config.ALPHA_8);
    Bitmap bitmap = createBitmap(null, width, height, config, hasAlpha);
    ShadowLegacyBitmap shadowBitmap = Shadow.extract(bitmap);
    shadowBitmap.colorSpace = colorSpace;
    return bitmap;
  }

  @Implementation
  protected static Bitmap createBitmap(
      Bitmap src, int x, int y, int width, int height, Matrix matrix, boolean filter) {
    if (x == 0
        && y == 0
        && width == src.getWidth()
        && height == src.getHeight()
        && (matrix == null || matrix.isIdentity())) {
      return src; // Return the original.
    }

    if (x + width > src.getWidth()) {
      throw new IllegalArgumentException("x + width must be <= bitmap.width()");
    }
    if (y + height > src.getHeight()) {
      throw new IllegalArgumentException("y + height must be <= bitmap.height()");
    }

    Bitmap newBitmap = ReflectionHelpers.callConstructor(Bitmap.class);
    ShadowLegacyBitmap shadowNewBitmap = Shadow.extract(newBitmap);

    ShadowLegacyBitmap shadowSrcBitmap = Shadow.extract(src);
    shadowNewBitmap.appendDescription(shadowSrcBitmap.getDescription());
    shadowNewBitmap.appendDescription(" at (" + x + "," + y + ")");
    shadowNewBitmap.appendDescription(" with width " + width + " and height " + height);

    shadowNewBitmap.createdFromBitmap = src;
    shadowNewBitmap.createdFromX = x;
    shadowNewBitmap.createdFromY = y;
    shadowNewBitmap.createdFromWidth = width;
    shadowNewBitmap.createdFromHeight = height;
    shadowNewBitmap.createdFromMatrix = matrix;
    shadowNewBitmap.createdFromFilter = filter;
    shadowNewBitmap.config = src.getConfig();
    if (matrix != null) {
      ShadowMatrix shadowMatrix = Shadow.extract(matrix);
      shadowNewBitmap.appendDescription(" using matrix " + shadowMatrix.getDescription());

      // Adjust width and height by using the matrix.
      RectF mappedRect = new RectF();
      matrix.mapRect(mappedRect, new RectF(0, 0, width, height));
      width = Math.round(mappedRect.width());
      height = Math.round(mappedRect.height());
    }
    if (filter) {
      shadowNewBitmap.appendDescription(" with filter");
    }

    // updated if matrix is non-null
    shadowNewBitmap.width = width;
    shadowNewBitmap.height = height;
    shadowNewBitmap.setMutable(true);
    newBitmap.setDensity(src.getDensity());
    if ((matrix == null || matrix.isIdentity()) && shadowSrcBitmap.bufferedImage != null) {
      // Only simple cases are supported for setting image data to the new Bitmap.
      shadowNewBitmap.bufferedImage =
          shadowSrcBitmap.bufferedImage.getSubimage(x, y, width, height);
    }
    if (RuntimeEnvironment.getApiLevel() >= O) {
      shadowNewBitmap.colorSpace = shadowSrcBitmap.colorSpace;
    }
    return newBitmap;
  }

  @Implementation
  protected static Bitmap createBitmap(
      int[] colors, int offset, int stride, int width, int height, Bitmap.Config config) {
    return createBitmap(null, colors, offset, stride, width, height, config);
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected static Bitmap createBitmap(
      DisplayMetrics displayMetrics,
      int[] colors,
      int offset,
      int stride,
      int width,
      int height,
      Bitmap.Config config) {
    if (width <= 0) {
      throw new IllegalArgumentException("width must be > 0");
    }
    if (height <= 0) {
      throw new IllegalArgumentException("height must be > 0");
    }
    if (Math.abs(stride) < width) {
      throw new IllegalArgumentException("abs(stride) must be >= width");
    }
    checkNotNull(config);
    int lastScanline = offset + (height - 1) * stride;
    int length = colors.length;
    if (offset < 0
        || (offset + width > length)
        || lastScanline < 0
        || (lastScanline + width > length)) {
      throw new ArrayIndexOutOfBoundsException();
    }

    BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    bufferedImage.setRGB(0, 0, width, height, colors, offset, stride);
    Bitmap bitmap = createBitmap(bufferedImage, width, height, config);
    ShadowLegacyBitmap shadowBitmap = Shadow.extract(bitmap);
    shadowBitmap.setMutable(false);
    shadowBitmap.createdFromColors = colors;
    if (displayMetrics != null) {
      bitmap.setDensity(displayMetrics.densityDpi);
    }
    if (RuntimeEnvironment.getApiLevel() >= O) {
      shadowBitmap.colorSpace = ColorSpace.get(ColorSpace.Named.SRGB);
    }
    return bitmap;
  }

  private static Bitmap createBitmap(
      BufferedImage bufferedImage, int width, int height, Bitmap.Config config) {
    Bitmap newBitmap = Bitmap.createBitmap(width, height, config);
    ShadowLegacyBitmap shadowBitmap = Shadow.extract(newBitmap);
    shadowBitmap.bufferedImage = bufferedImage;
    return newBitmap;
  }

  @Implementation
  protected static Bitmap createScaledBitmap(
      Bitmap src, int dstWidth, int dstHeight, boolean filter) {
    if (dstWidth == src.getWidth() && dstHeight == src.getHeight() && !filter) {
      return src; // Return the original.
    }
    if (dstWidth <= 0 || dstHeight <= 0) {
      throw new IllegalArgumentException("width and height must be > 0");
    }
    Bitmap scaledBitmap = ReflectionHelpers.callConstructor(Bitmap.class);
    ShadowLegacyBitmap shadowBitmap = Shadow.extract(scaledBitmap);

    ShadowLegacyBitmap shadowSrcBitmap = Shadow.extract(src);
    shadowBitmap.appendDescription(shadowSrcBitmap.getDescription());
    shadowBitmap.appendDescription(" scaled to " + dstWidth + " x " + dstHeight);
    if (filter) {
      shadowBitmap.appendDescription(" with filter " + filter);
    }

    shadowBitmap.createdFromBitmap = src;
    shadowBitmap.scaledFromBitmap = src;
    shadowBitmap.createdFromFilter = filter;
    shadowBitmap.width = dstWidth;
    shadowBitmap.height = dstHeight;
    shadowBitmap.config = src.getConfig();
    shadowBitmap.mutable = true;
    if (!ImageUtil.scaledBitmap(src, scaledBitmap, filter)) {
      shadowBitmap.bufferedImage =
          new BufferedImage(dstWidth, dstHeight, BufferedImage.TYPE_INT_ARGB);
      shadowBitmap.setPixelsInternal(
          new int[shadowBitmap.getHeight() * shadowBitmap.getWidth()],
          0,
          0,
          0,
          0,
          shadowBitmap.getWidth(),
          shadowBitmap.getHeight());
    }
    if (RuntimeEnvironment.getApiLevel() >= O) {
      shadowBitmap.colorSpace = shadowSrcBitmap.colorSpace;
    }
    return scaledBitmap;
  }

  @Implementation
  protected static Bitmap nativeCreateFromParcel(Parcel p) {
    int parceledWidth = p.readInt();
    int parceledHeight = p.readInt();
    Bitmap.Config parceledConfig = (Bitmap.Config) p.readSerializable();

    int[] parceledColors = new int[parceledHeight * parceledWidth];
    p.readIntArray(parceledColors);

    return createBitmap(
        parceledColors, 0, parceledWidth, parceledWidth, parceledHeight, parceledConfig);
  }

  static int getBytesPerPixel(Bitmap.Config config) {
    if (config == null) {
      throw new NullPointerException("Bitmap config was null.");
    }
    switch (config) {
      case RGBA_F16:
        return 8;
      case ARGB_8888:
      case HARDWARE:
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

  /**
   * Reference to original Bitmap from which this Bitmap was created. {@code null} if this Bitmap
   * was not copied from another instance.
   *
   * @return Original Bitmap from which this Bitmap was created.
   */
  @Override
  public Bitmap getCreatedFromBitmap() {
    return createdFromBitmap;
  }

  /**
   * Resource ID from which this Bitmap was created. {@code 0} if this Bitmap was not created from a
   * resource.
   *
   * @return Resource ID from which this Bitmap was created.
   */
  @Override
  public int getCreatedFromResId() {
    return createdFromResId;
  }

  /**
   * Path from which this Bitmap was created. {@code null} if this Bitmap was not create from a
   * path.
   *
   * @return Path from which this Bitmap was created.
   */
  @Override
  public String getCreatedFromPath() {
    return createdFromPath;
  }

  /**
   * {@link InputStream} from which this Bitmap was created. {@code null} if this Bitmap was not
   * created from a stream.
   *
   * @return InputStream from which this Bitmap was created.
   */
  @Override
  public InputStream getCreatedFromStream() {
    return createdFromStream;
  }

  /**
   * Bytes from which this Bitmap was created. {@code null} if this Bitmap was not created from
   * bytes.
   *
   * @return Bytes from which this Bitmap was created.
   */
  @Override
  public byte[] getCreatedFromBytes() {
    return createdFromBytes;
  }

  /**
   * Horizontal offset within {@link #getCreatedFromBitmap()} of this Bitmap's content, or -1.
   *
   * @return Horizontal offset within {@link #getCreatedFromBitmap()}.
   */
  @Override
  public int getCreatedFromX() {
    return createdFromX;
  }

  /**
   * Vertical offset within {@link #getCreatedFromBitmap()} of this Bitmap's content, or -1.
   *
   * @return Vertical offset within {@link #getCreatedFromBitmap()} of this Bitmap's content, or -1.
   */
  @Override
  public int getCreatedFromY() {
    return createdFromY;
  }

  /**
   * Width from {@link #getCreatedFromX()} within {@link #getCreatedFromBitmap()} of this Bitmap's
   * content, or -1.
   *
   * @return Width from {@link #getCreatedFromX()} within {@link #getCreatedFromBitmap()} of this
   *     Bitmap's content, or -1.
   */
  @Override
  public int getCreatedFromWidth() {
    return createdFromWidth;
  }

  /**
   * Height from {@link #getCreatedFromX()} within {@link #getCreatedFromBitmap()} of this Bitmap's
   * content, or -1.
   *
   * @return Height from {@link #getCreatedFromX()} within {@link #getCreatedFromBitmap()} of this
   *     Bitmap's content, or -1.
   */
  @Override
  public int getCreatedFromHeight() {
    return createdFromHeight;
  }

  /**
   * Color array from which this Bitmap was created. {@code null} if this Bitmap was not created
   * from a color array.
   *
   * @return Color array from which this Bitmap was created.
   */
  @Override
  public int[] getCreatedFromColors() {
    return createdFromColors;
  }

  /**
   * Matrix from which this Bitmap's content was transformed, or {@code null}.
   *
   * @return Matrix from which this Bitmap's content was transformed, or {@code null}.
   */
  @Override
  public Matrix getCreatedFromMatrix() {
    return createdFromMatrix;
  }

  /**
   * {@code true} if this Bitmap was created with filtering.
   *
   * @return {@code true} if this Bitmap was created with filtering.
   */
  @Override
  public boolean getCreatedFromFilter() {
    return createdFromFilter;
  }

  @Implementation(minSdk = S)
  protected Bitmap asShared() {
    setMutable(false);
    return realBitmap;
  }

  @Implementation
  protected boolean compress(Bitmap.CompressFormat format, int quality, OutputStream stream) {
    appendDescription(" compressed as " + format + " with quality " + quality);
    return ImageUtil.writeToStream(realBitmap, format, quality, stream);
  }

  @Implementation
  protected void setPixels(
      int[] pixels, int offset, int stride, int x, int y, int width, int height) {
    checkBitmapMutable();
    setPixelsInternal(pixels, offset, stride, x, y, width, height);
  }

  void setPixelsInternal(
      int[] pixels, int offset, int stride, int x, int y, int width, int height) {
    if (bufferedImage == null) {
      bufferedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
    }
    bufferedImage.setRGB(x, y, width, height, pixels, offset, stride);
  }

  @Implementation
  protected int getPixel(int x, int y) {
    internalCheckPixelAccess(x, y);
    if (bufferedImage != null) {
      // Note that getPixel() returns a non-premultiplied ARGB value; if
      // config is RGB_565, our return value will likely be more precise than
      // on a physical device, since it needs to map each color component from
      // 5 or 6 bits to 8 bits.
      return bufferedImage.getRGB(x, y);
    } else {
      return 0;
    }
  }

  @Implementation
  protected void setPixel(int x, int y, int color) {
    checkBitmapMutable();
    internalCheckPixelAccess(x, y);
    if (bufferedImage == null) {
      bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }
    bufferedImage.setRGB(x, y, color);
  }

  /**
   * Note that this method will return a RuntimeException unless: - {@code pixels} has the same
   * length as the number of pixels of the bitmap. - {@code x = 0} - {@code y = 0} - {@code width}
   * and {@code height} height match the current bitmap's dimensions.
   */
  @Implementation
  protected void getPixels(
      int[] pixels, int offset, int stride, int x, int y, int width, int height) {
    bufferedImage.getRGB(x, y, width, height, pixels, offset, stride);
  }

  @Implementation
  protected int getRowBytes() {
    return getBytesPerPixel(config) * getWidth();
  }

  @Implementation
  protected int getByteCount() {
    return getRowBytes() * getHeight();
  }

  @Implementation
  protected void recycle() {
    recycled = true;
  }

  @Implementation
  protected final boolean isRecycled() {
    return recycled;
  }

  @Implementation
  protected Bitmap copy(Bitmap.Config config, boolean isMutable) {
    Bitmap newBitmap = ReflectionHelpers.callConstructor(Bitmap.class);
    ShadowLegacyBitmap shadowBitmap = Shadow.extract(newBitmap);
    shadowBitmap.createdFromBitmap = realBitmap;
    shadowBitmap.config = config;
    shadowBitmap.mutable = isMutable;
    shadowBitmap.height = getHeight();
    shadowBitmap.width = getWidth();
    if (bufferedImage != null) {
      ColorModel cm = bufferedImage.getColorModel();
      WritableRaster raster =
          bufferedImage.copyData(bufferedImage.getRaster().createCompatibleWritableRaster());
      shadowBitmap.bufferedImage = new BufferedImage(cm, raster, false, null);
    }
    return newBitmap;
  }

  @Implementation(minSdk = KITKAT)
  protected final int getAllocationByteCount() {
    return getRowBytes() * getHeight();
  }

  @Implementation
  protected final Bitmap.Config getConfig() {
    return config;
  }

  @Implementation(minSdk = KITKAT)
  protected void setConfig(Bitmap.Config config) {
    this.config = config;
  }

  @Implementation
  protected final boolean isMutable() {
    return mutable;
  }

  @Override
  public void setMutable(boolean mutable) {
    this.mutable = mutable;
  }

  @Override
  public void appendDescription(String s) {
    description += s;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String s) {
    description = s;
  }

  @Implementation
  protected final boolean hasAlpha() {
    return hasAlpha && config != Bitmap.Config.RGB_565;
  }

  @Implementation
  protected void setHasAlpha(boolean hasAlpha) {
    this.hasAlpha = hasAlpha;
  }

  @Implementation
  protected Bitmap extractAlpha() {
    WritableRaster raster = bufferedImage.getAlphaRaster();
    BufferedImage alphaImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    alphaImage.getAlphaRaster().setRect(raster);
    return createBitmap(alphaImage, getWidth(), getHeight(), Bitmap.Config.ALPHA_8);
  }

  /**
   * This shadow implementation ignores the given paint and offsetXY and simply calls {@link
   * #extractAlpha()}.
   */
  @Implementation
  protected Bitmap extractAlpha(Paint paint, int[] offsetXY) {
    return extractAlpha();
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected final boolean hasMipMap() {
    return hasMipMap;
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected final void setHasMipMap(boolean hasMipMap) {
    this.hasMipMap = hasMipMap;
  }

  @Implementation
  protected int getWidth() {
    return width;
  }

  @Implementation(minSdk = KITKAT)
  protected void setWidth(int width) {
    this.width = width;
  }

  @Implementation
  protected int getHeight() {
    return height;
  }

  @Implementation(minSdk = KITKAT)
  protected void setHeight(int height) {
    this.height = height;
  }

  @Implementation
  protected int getGenerationId() {
    return 0;
  }

  @Implementation(minSdk = M)
  protected Bitmap createAshmemBitmap() {
    return realBitmap;
  }

  @Implementation
  protected void eraseColor(int color) {
    if (bufferedImage != null) {
      int[] pixels = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
      Arrays.fill(pixels, color);
    }
    setDescription(String.format("Bitmap (%d, %d)", width, height));
    if (color != 0) {
      appendDescription(String.format(" erased with 0x%08x", color));
    }
  }

  @Implementation
  protected void writeToParcel(Parcel p, int flags) {
    p.writeInt(width);
    p.writeInt(height);
    p.writeSerializable(config);
    int[] pixels = new int[width * height];
    getPixels(pixels, 0, width, 0, 0, width, height);
    p.writeIntArray(pixels);
  }

  @Implementation
  protected void copyPixelsFromBuffer(Buffer dst) {
    if (isRecycled()) {
      throw new IllegalStateException("Can't call copyPixelsFromBuffer() on a recycled bitmap");
    }

    // See the related comment in #copyPixelsToBuffer(Buffer).
    if (getBytesPerPixel(config) != INTERNAL_BYTES_PER_PIXEL) {
      throw new RuntimeException(
          "Not implemented: only Bitmaps with "
              + INTERNAL_BYTES_PER_PIXEL
              + " bytes per pixel are supported");
    }
    if (!(dst instanceof ByteBuffer) && !(dst instanceof IntBuffer)) {
      throw new RuntimeException("Not implemented: unsupported Buffer subclass");
    }

    ByteBuffer byteBuffer = null;
    IntBuffer intBuffer;
    if (dst instanceof IntBuffer) {
      intBuffer = (IntBuffer) dst;
    } else {
      byteBuffer = (ByteBuffer) dst;
      intBuffer = byteBuffer.asIntBuffer();
    }

    if (intBuffer.remaining() < (width * height)) {
      throw new RuntimeException("Buffer not large enough for pixels");
    }

    int[] colors = new int[width * height];
    intBuffer.get(colors);
    if (byteBuffer != null) {
      byteBuffer.position(byteBuffer.position() + intBuffer.position() * INTERNAL_BYTES_PER_PIXEL);
    }
    int[] pixels = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
    System.arraycopy(colors, 0, pixels, 0, pixels.length);
  }

  @Implementation
  protected void copyPixelsToBuffer(Buffer dst) {
    // Ensure that the Bitmap uses 4 bytes per pixel, since we always use 4 bytes per pixels
    // internally. Clients of this API probably expect that the buffer size must be >=
    // getByteCount(), but if we don't enforce this restriction then for RGB_4444 and other
    // configs that value would be smaller then the buffer size we actually need.
    if (getBytesPerPixel(config) != INTERNAL_BYTES_PER_PIXEL) {
      throw new RuntimeException(
          "Not implemented: only Bitmaps with "
              + INTERNAL_BYTES_PER_PIXEL
              + " bytes per pixel are supported");
    }

    if (!(dst instanceof ByteBuffer) && !(dst instanceof IntBuffer)) {
      throw new RuntimeException("Not implemented: unsupported Buffer subclass");
    }
    int[] pixels = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
    if (dst instanceof ByteBuffer) {
      IntBuffer intBuffer = ((ByteBuffer) dst).asIntBuffer();
      intBuffer.put(pixels);
      dst.position(intBuffer.position() * 4);
    } else if (dst instanceof IntBuffer) {
      ((IntBuffer) dst).put(pixels);
    }
  }

  @Implementation(minSdk = KITKAT)
  protected void reconfigure(int width, int height, Bitmap.Config config) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this.config == Bitmap.Config.HARDWARE) {
      throw new IllegalStateException("native-backed bitmaps may not be reconfigured");
    }

    // This should throw if the resulting allocation size is greater than the initial allocation
    // size of our Bitmap, but we don't keep track of that information reliably, so we're forced to
    // assume that our original dimensions and config are large enough to fit the new dimensions and
    // config
    this.width = width;
    this.height = height;
    this.config = config;
    bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
  }

  @Implementation(minSdk = KITKAT)
  protected boolean isPremultiplied() {
    return requestPremultiplied && hasAlpha();
  }

  @Implementation(minSdk = KITKAT)
  protected void setPremultiplied(boolean isPremultiplied) {
    this.requestPremultiplied = isPremultiplied;
  }

  @Implementation(minSdk = O)
  protected ColorSpace getColorSpace() {
    return colorSpace;
  }

  @Implementation(minSdk = Q)
  protected void setColorSpace(ColorSpace colorSpace) {
    this.colorSpace = checkNotNull(colorSpace);
  }

  @Implementation
  protected boolean sameAs(Bitmap other) {
    if (other == null) {
      return false;
    }
    ShadowLegacyBitmap shadowOtherBitmap = Shadow.extract(other);
    if (this.width != shadowOtherBitmap.width || this.height != shadowOtherBitmap.height) {
      return false;
    }
    if (this.config != shadowOtherBitmap.config) {
      return false;
    }

    if (bufferedImage == null && shadowOtherBitmap.bufferedImage != null) {
      return false;
    } else if (bufferedImage != null && shadowOtherBitmap.bufferedImage == null) {
      return false;
    } else if (bufferedImage != null && shadowOtherBitmap.bufferedImage != null) {
      int[] pixels = ((DataBufferInt) bufferedImage.getData().getDataBuffer()).getData();
      int[] otherPixels =
          ((DataBufferInt) shadowOtherBitmap.bufferedImage.getData().getDataBuffer()).getData();
      if (!Arrays.equals(pixels, otherPixels)) {
        return false;
      }
    }
    // When Bitmap.createScaledBitmap is called, the colors array is cleared, so we need a basic
    // way to detect if two scaled bitmaps are the same.
    if (scaledFromBitmap != null && shadowOtherBitmap.scaledFromBitmap != null) {
      return scaledFromBitmap.sameAs(shadowOtherBitmap.scaledFromBitmap);
    }
    return true;
  }

  void setCreatedFromResId(int resId, String description) {
    this.createdFromResId = resId;
    appendDescription(" for resource:" + description);
  }

  private void checkBitmapMutable() {
    if (isRecycled()) {
      throw new IllegalStateException("Can't call setPixel() on a recycled bitmap");
    } else if (!isMutable()) {
      throw new IllegalStateException("Bitmap is immutable");
    }
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

  void drawRect(Rect r, Paint paint) {
    if (bufferedImage == null) {
      return;
    }
    int[] pixels = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();

    Rect toDraw =
        new Rect(
            max(0, r.left), max(0, r.top), min(getWidth(), r.right), min(getHeight(), r.bottom));
    if (toDraw.left == 0 && toDraw.top == 0 && toDraw.right == getWidth()) {
      Arrays.fill(pixels, 0, getWidth() * toDraw.bottom, paint.getColor());
      return;
    }
    for (int y = toDraw.top; y < toDraw.bottom; y++) {
      Arrays.fill(
          pixels, y * getWidth() + toDraw.left, y * getWidth() + toDraw.right, paint.getColor());
    }
  }

  void drawRect(RectF r, Paint paint) {
    if (bufferedImage == null) {
      return;
    }

    Graphics2D graphics2D = bufferedImage.createGraphics();
    Rectangle2D r2d = new Rectangle2D.Float(r.left, r.top, r.right - r.left, r.bottom - r.top);
    graphics2D.setColor(new Color(paint.getColor()));
    graphics2D.draw(r2d);
    graphics2D.dispose();
  }

  void drawBitmap(Bitmap source, int left, int top) {
    ShadowLegacyBitmap shadowSource = Shadow.extract(source);
    if (bufferedImage == null || shadowSource.bufferedImage == null) {
      // pixel data not available, so there's nothing we can do
      return;
    }

    int[] pixels = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
    int[] sourcePixels =
        ((DataBufferInt) shadowSource.bufferedImage.getRaster().getDataBuffer()).getData();

    // fast path
    if (left == 0 && top == 0 && getWidth() == source.getWidth()) {
      int size = min(getWidth() * getHeight(), source.getWidth() * source.getHeight());
      System.arraycopy(sourcePixels, 0, pixels, 0, size);
      return;
    }
    // slower (row-by-row) path
    int startSourceY = max(0, -top);
    int startSourceX = max(0, -left);
    int startY = max(0, top);
    int startX = max(0, left);
    int endY = min(getHeight(), top + source.getHeight());
    int endX = min(getWidth(), left + source.getWidth());
    int lenY = endY - startY;
    int lenX = endX - startX;
    for (int y = 0; y < lenY; y++) {
      System.arraycopy(
          sourcePixels,
          (startSourceY + y) * source.getWidth() + startSourceX,
          pixels,
          (startY + y) * getWidth() + startX,
          lenX);
    }
  }

  BufferedImage getBufferedImage() {
    return bufferedImage;
  }

  void setBufferedImage(BufferedImage bufferedImage) {
    this.bufferedImage = bufferedImage;
  }
}
