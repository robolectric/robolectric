package org.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import java.io.InputStream;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowBitmap.Picker;

/** Base class for {@link Bitmap} shadows. */
@Implements(value = Bitmap.class, shadowPicker = Picker.class)
public abstract class ShadowBitmap {

  /**
   * Returns a textual representation of the appearance of the object.
   *
   * @param bitmap the bitmap to visualize
   * @return Textual representation of the appearance of the object.
   */
  public static String visualize(Bitmap bitmap) {
    ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
    return shadowBitmap.getDescription();
  }

  /**
   * Reference to original Bitmap from which this Bitmap was created. {@code null} if this Bitmap
   * was not copied from another instance.
   *
   * @return Original Bitmap from which this Bitmap was created.
   */
  public abstract Bitmap getCreatedFromBitmap();

  /**
   * Resource ID from which this Bitmap was created. {@code 0} if this Bitmap was not created from a
   * resource.
   *
   * @return Resource ID from which this Bitmap was created.
   */
  public abstract int getCreatedFromResId();

  /**
   * Path from which this Bitmap was created. {@code null} if this Bitmap was not create from a
   * path.
   *
   * @return Path from which this Bitmap was created.
   */
  public abstract String getCreatedFromPath();

  /**
   * {@link InputStream} from which this Bitmap was created. {@code null} if this Bitmap was not
   * created from a stream.
   *
   * @return InputStream from which this Bitmap was created.
   */
  public abstract InputStream getCreatedFromStream();

  /**
   * Bytes from which this Bitmap was created. {@code null} if this Bitmap was not created from
   * bytes.
   *
   * @return Bytes from which this Bitmap was created.
   */
  public abstract byte[] getCreatedFromBytes();

  /**
   * Horizontal offset within {@link #getCreatedFromBitmap()} of this Bitmap's content, or -1.
   *
   * @return Horizontal offset within {@link #getCreatedFromBitmap()}.
   */
  public abstract int getCreatedFromX();

  /**
   * Vertical offset within {@link #getCreatedFromBitmap()} of this Bitmap's content, or -1.
   *
   * @return Vertical offset within {@link #getCreatedFromBitmap()} of this Bitmap's content, or -1.
   */
  public abstract int getCreatedFromY();

  /**
   * Width from {@link #getCreatedFromX()} within {@link #getCreatedFromBitmap()} of this Bitmap's
   * content, or -1.
   *
   * @return Width from {@link #getCreatedFromX()} within {@link #getCreatedFromBitmap()} of this
   *     Bitmap's content, or -1.
   */
  public abstract int getCreatedFromWidth();

  /**
   * Height from {@link #getCreatedFromX()} within {@link #getCreatedFromBitmap()} of this Bitmap's
   * content, or -1.
   *
   * @return Height from {@link #getCreatedFromX()} within {@link #getCreatedFromBitmap()} of this
   *     Bitmap's content, or -1.
   */
  public abstract int getCreatedFromHeight();

  /**
   * Color array from which this Bitmap was created. {@code null} if this Bitmap was not created
   * from a color array.
   *
   * @return Color array from which this Bitmap was created.
   */
  public abstract int[] getCreatedFromColors();

  /**
   * Matrix from which this Bitmap's content was transformed, or {@code null}.
   *
   * @return Matrix from which this Bitmap's content was transformed, or {@code null}.
   */
  public abstract Matrix getCreatedFromMatrix();

  /**
   * {@code true} if this Bitmap was created with filtering.
   *
   * @return {@code true} if this Bitmap was created with filtering.
   */
  public abstract boolean getCreatedFromFilter();

  public abstract void setMutable(boolean mutable);

  public abstract void appendDescription(String s);

  public abstract String getDescription();

  public abstract void setDescription(String s);

  /** Shadow picker for {@link Bitmap}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowLegacyBitmap.class, ShadowNativeBitmap.class);
    }
  }
}
