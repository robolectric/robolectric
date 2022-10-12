package org.robolectric.shadows;

import static java.lang.Math.round;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.robolectric.shadows.ImageUtil.getImageFromStream;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.res.AssetManager.AssetInputStream;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.util.TypedValue;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ImageUtil.RobolectricBufferedImage;
import org.robolectric.util.Join;
import org.robolectric.util.Logger;
import org.robolectric.util.NamedStream;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BitmapFactory.class)
public class ShadowBitmapFactory {
  private static Map<String, Point> widthAndHeightMap = new HashMap<>();

  // Determines whether BitmapFactory.decode methods should allow invalid bitmap data and always
  // return a Bitmap object. Currently defaults to true to preserve legacy behavior. A
  // forthcoming release will switch the default to false, which is consistent with real Android.
  private static boolean allowInvalidImageData = true;

  @Implementation
  protected static Bitmap decodeResourceStream(
      Resources res, TypedValue value, InputStream is, Rect pad, BitmapFactory.Options opts) {
    Bitmap bitmap =
        reflector(BitmapFactoryReflector.class).decodeResourceStream(res, value, is, pad, opts);

    if (value != null && value.string != null && value.string.toString().contains(".9.")) {
      // todo: better support for nine-patches
      ReflectionHelpers.callInstanceMethod(
          bitmap, "setNinePatchChunk", ClassParameter.from(byte[].class, new byte[0]));
    }
    return bitmap;
  }

  @Implementation
  protected static Bitmap decodeResource(Resources res, int id, BitmapFactory.Options options) {
    if (id == 0) {
      return null;
    }

    final TypedValue value = new TypedValue();
    InputStream is = res.openRawResource(id, value);

    String resourceName = res.getResourceName(id);
    RobolectricBufferedImage image = getImageFromStream(resourceName, is);
    if (!allowInvalidImageData && image == null) {
      if (options != null) {
        options.outWidth = -1;
        options.outHeight = -1;
      }
      return null;
    }
    Bitmap bitmap = create("resource:" + resourceName, options, image);
    ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
    shadowBitmap.createdFromResId = id;
    return bitmap;
  }

  @Implementation
  protected static Bitmap decodeFile(String pathName) {
    return decodeFile(pathName, null);
  }

  @SuppressWarnings("Var")
  @Implementation
  protected static Bitmap decodeFile(String pathName, BitmapFactory.Options options) {
    // If a real file is used, attempt to get the image size from that file.
    RobolectricBufferedImage image = null;
    if (pathName != null && new File(pathName).exists()) {
      try (FileInputStream fileInputStream = new FileInputStream(pathName);
          BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
        image = getImageFromStream(pathName, bufferedInputStream);
      } catch (IOException e) {
        Logger.warn("Error getting size of bitmap file", e);
      }
    }
    if (!allowInvalidImageData && image == null) {
      if (options != null) {
        options.outWidth = -1;
        options.outHeight = -1;
      }
      return null;
    }
    Bitmap bitmap = create("file:" + pathName, options, image);
    ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
    shadowBitmap.createdFromPath = pathName;
    return bitmap;
  }

  @SuppressWarnings({"ObjectToString", "Var"})
  @Implementation
  protected static Bitmap decodeFileDescriptor(
      FileDescriptor fd, Rect outPadding, BitmapFactory.Options opts) {
    RobolectricBufferedImage image = null;
    // If a real FileDescriptor is used, attempt to get the image size.
    if (fd != null && fd.valid()) {
      try (FileInputStream fileInputStream = new FileInputStream(fd);
          BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream); ) {
        image = getImageFromStream(bufferedInputStream);
      } catch (IOException e) {
        Logger.warn("Error getting size of bitmap file", e);
      }
    }
    if (!allowInvalidImageData && image == null) {
      if (opts != null) {
        opts.outWidth = -1;
        opts.outHeight = -1;
      }
      return null;
    }
    Bitmap bitmap = create("fd:" + fd, null, outPadding, opts, null, image);
    ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
    shadowBitmap.createdFromFileDescriptor = fd;
    return bitmap;
  }

  @Implementation
  protected static Bitmap decodeStream(InputStream is) {
    return decodeStream(is, null, null);
  }

  @Implementation
  protected static Bitmap decodeStream(
      InputStream is, Rect outPadding, BitmapFactory.Options opts) {
    byte[] ninePatchChunk = null;

    if (is instanceof AssetInputStream) {
      ShadowAssetInputStream sais = Shadow.extract(is);
      if (sais.isNinePatch()) {
        ninePatchChunk = new byte[0];
      }
      if (sais.getDelegate() != null) {
        is = sais.getDelegate();
      }
    }

    try {
      if (is != null) {
        is.reset();
      }
    } catch (IOException e) {
      // ignore
    }

    boolean isNamedStream = is instanceof NamedStream;
    String name = isNamedStream ? is.toString().replace("stream for ", "") : null;
    RobolectricBufferedImage image = isNamedStream ? null : getImageFromStream(is);
    if (!allowInvalidImageData && image == null) {
      if (opts != null) {
        opts.outWidth = -1;
        opts.outHeight = -1;
      }
      return null;
    }
    Bitmap bitmap = create(name, null, outPadding, opts, null, image);
    ReflectionHelpers.callInstanceMethod(
        bitmap, "setNinePatchChunk", ClassParameter.from(byte[].class, ninePatchChunk));
    ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
    shadowBitmap.createdFromStream = is;

    if (image != null && opts != null) {
      opts.outMimeType = image.getMimeType();
    }
    return bitmap;
  }

  @Implementation
  protected static Bitmap decodeByteArray(byte[] data, int offset, int length) {
    return decodeByteArray(data, offset, length, new BitmapFactory.Options());
  }

  @Implementation
  protected static Bitmap decodeByteArray(
      byte[] data, int offset, int length, BitmapFactory.Options opts) {
    String desc = data.length + " bytes";

    if (offset != 0 || length != data.length) {
      desc += " " + offset + ".." + length;
    }

    ByteArrayInputStream is = new ByteArrayInputStream(data, offset, length);
    RobolectricBufferedImage image = getImageFromStream(is);
    if (!allowInvalidImageData && image == null) {
      if (opts != null) {
        opts.outWidth = -1;
        opts.outHeight = -1;
      }
      return null;
    }
    Bitmap bitmap = create(desc, data, null, opts, null, image);
    ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
    shadowBitmap.createdFromBytes = data;
    return bitmap;
  }

  static Bitmap create(
      final String name,
      final BitmapFactory.Options options,
      final RobolectricBufferedImage image) {
    return create(name, null, null, options, null, image);
  }

  private static Bitmap create(
      final String name,
      byte[] bytes,
      final Rect outPadding,
      final BitmapFactory.Options options,
      final Point widthAndHeightOverride,
      final RobolectricBufferedImage image) {
    Bitmap bitmap = Shadow.newInstanceOf(Bitmap.class);
    ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
    shadowBitmap.appendDescription(name == null ? "Bitmap" : "Bitmap for " + name);

    Bitmap.Config config;
    if (options != null && options.inPreferredConfig != null) {
      config = options.inPreferredConfig;
    } else {
      config = Bitmap.Config.ARGB_8888;
    }
    shadowBitmap.setConfig(config);

    String optionsString = stringify(options);
    if (!optionsString.isEmpty()) {
      shadowBitmap.appendDescription(" with options ");
      shadowBitmap.appendDescription(optionsString);
    }

    Point p = new Point(selectWidthAndHeight(name, bytes, widthAndHeightOverride, image));
    if (options != null && options.inSampleSize > 1) {
      p.x = p.x / options.inSampleSize;
      p.y = p.y / options.inSampleSize;

      p.x = p.x == 0 ? 1 : p.x;
      p.y = p.y == 0 ? 1 : p.y;
    }

    // Prior to KitKat the density scale will be applied by finishDecode below.
    float scale =
        RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.KITKAT
                && options != null
                && options.inScaled
                && options.inDensity != 0
                && options.inTargetDensity != 0
                && options.inDensity != options.inScreenDensity
            ? (float) options.inTargetDensity / options.inDensity
            : 1;
    int scaledWidth = round(p.x * scale);
    int scaledHeight = round(p.y * scale);

    shadowBitmap.setWidth(scaledWidth);
    shadowBitmap.setHeight(scaledHeight);
    if (image != null) {
      BufferedImage bufferedImage =
          new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
      // Copy the image as TYPE_INT_ARGB for fast comparison (sameAs).
      Graphics2D g = bufferedImage.createGraphics();
      g.drawImage(image.getBufferedImage(), 0, 0, null);
      g.dispose();
      shadowBitmap.setBufferedImage(bufferedImage);
    } else {
      shadowBitmap.setPixelsInternal(
          new int[scaledWidth * scaledHeight], 0, 0, 0, 0, scaledWidth, scaledHeight);
    }
    if (options != null) {
      options.outWidth = p.x;
      options.outHeight = p.y;
      shadowBitmap.setMutable(options.inMutable);
    }

    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.KITKAT) {
      ReflectionHelpers.callStaticMethod(
          BitmapFactory.class,
          "setDensityFromOptions",
          ClassParameter.from(Bitmap.class, bitmap),
          ClassParameter.from(BitmapFactory.Options.class, options));
    } else {
      bitmap =
          ReflectionHelpers.callStaticMethod(
              BitmapFactory.class,
              "finishDecode",
              ClassParameter.from(Bitmap.class, bitmap),
              ClassParameter.from(Rect.class, outPadding),
              ClassParameter.from(BitmapFactory.Options.class, options));
    }
    return bitmap;
  }

  /**
   * @deprecated Use any of the BitmapFactory.decode methods with real image data.
   */
  @Deprecated
  public static void provideWidthAndHeightHints(Uri uri, int width, int height) {
    widthAndHeightMap.put(uri.toString(), new Point(width, height));
  }

  /**
   * @deprecated Use any of the BitmapFactory.decode methods with real image data.
   */
  @Deprecated
  public static void provideWidthAndHeightHints(int resourceId, int width, int height) {
    widthAndHeightMap.put(
        "resource:"
            + RuntimeEnvironment.getApplication().getResources().getResourceName(resourceId),
        new Point(width, height));
  }

  /**
   * @deprecated Use any of the BitmapFactory.decode methods with real image data.
   */
  @Deprecated
  public static void provideWidthAndHeightHints(String file, int width, int height) {
    widthAndHeightMap.put("file:" + file, new Point(width, height));
  }

  /**
   * @deprecated Use any of the BitmapFactory.decode methods with real image data.
   */
  @Deprecated
  @SuppressWarnings("ObjectToString")
  public static void provideWidthAndHeightHints(FileDescriptor fd, int width, int height) {
    widthAndHeightMap.put("fd:" + fd, new Point(width, height));
  }

  private static String stringify(BitmapFactory.Options options) {
    if (options == null) return "";
    List<String> opts = new ArrayList<>();

    if (options.inJustDecodeBounds) opts.add("inJustDecodeBounds");
    if (options.inSampleSize > 1) opts.add("inSampleSize=" + options.inSampleSize);

    return Join.join(", ", opts);
  }

  @Resetter
  public static void reset() {
    widthAndHeightMap.clear();
    allowInvalidImageData = true;
  }

  private static Point selectWidthAndHeight(
      final String name,
      byte[] bytes,
      final Point widthAndHeightOverride,
      final RobolectricBufferedImage robolectricBufferedImage) {
    if (!widthAndHeightMap.isEmpty()) {
      String sizeKey = bytes == null ? name : new String(bytes, UTF_8);
      final Point widthAndHeightFromMap = widthAndHeightMap.get(sizeKey);
      if (widthAndHeightFromMap != null) {
        return widthAndHeightFromMap;
      }
    }
    if (robolectricBufferedImage != null) {
      return robolectricBufferedImage.getWidthAndHeight();
    }
    if (widthAndHeightOverride != null) {
      return widthAndHeightOverride;
    }
    return new Point(100, 100);
  }

  /**
   * Whether the BitmapFactory.decode methods, such as {@link
   * BitmapFactory#decodeStream(InputStream, Rect, Options)} should allow invalid image data and
   * always return Bitmap objects. If set to false, BitmapFactory.decode methods will be consistent
   * with real Android, and return null Bitmap values and set {@link BitmapFactory.Options#outWidth}
   * and {@link BitmapFactory.Options#outHeight} to -1.
   *
   * @param allowInvalidImageData whether invalid bitmap data is allowed and BitmapFactory should
   *     always return Bitmap objects.
   */
  public static void setAllowInvalidImageData(boolean allowInvalidImageData) {
    ShadowBitmapFactory.allowInvalidImageData = allowInvalidImageData;
  }

  @ForType(BitmapFactory.class)
  interface BitmapFactoryReflector {

    @Static
    @Direct
    Bitmap decodeResourceStream(
        Resources res, TypedValue value, InputStream is, Rect pad, BitmapFactory.Options opts);
  }
}
