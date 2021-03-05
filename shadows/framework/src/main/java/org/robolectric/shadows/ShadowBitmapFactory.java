package org.robolectric.shadows;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.shadows.ImageUtil.getImageSizeFromStream;

import android.content.res.AssetManager.AssetInputStream;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.util.TypedValue;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.Join;
import org.robolectric.util.Logger;
import org.robolectric.util.NamedStream;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BitmapFactory.class)
public class ShadowBitmapFactory {
  private static Map<String, Point> widthAndHeightMap = new HashMap<>();

  @Implementation
  protected static Bitmap decodeResourceStream(
      Resources res, TypedValue value, InputStream is, Rect pad, BitmapFactory.Options opts) {
    Bitmap bitmap = directlyOn(BitmapFactory.class, "decodeResourceStream",
        ClassParameter.from(Resources.class, res),
        ClassParameter.from(TypedValue.class, value),
        ClassParameter.from(InputStream.class, is),
        ClassParameter.from(Rect.class, pad),
        ClassParameter.from(BitmapFactory.Options.class, opts));

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

    Point imageSizeFromStream = getImageSizeFromStream(is);

    Bitmap bitmap = create("resource:" + res.getResourceName(id), options, imageSizeFromStream);
    ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
    shadowBitmap.createdFromResId = id;
    return bitmap;
  }

  @Implementation
  protected static Bitmap decodeFile(String pathName) {
    return decodeFile(pathName, null);
  }

  @Implementation
  protected static Bitmap decodeFile(String pathName, BitmapFactory.Options options) {
    // If a real file is used, attempt to get the image size from that file.
    Point imageSizeFromStream = null;
    if (pathName != null && new File(pathName).exists()) {
      try (FileInputStream fileInputStream = new FileInputStream(pathName);
          BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream); ) {
        imageSizeFromStream = getImageSizeFromStream(bufferedInputStream);
      } catch (IOException e) {
        Logger.warn("Error getting size of bitmap file", e);
      }
    }
    Bitmap bitmap = create("file:" + pathName, options, imageSizeFromStream);
    ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
    shadowBitmap.createdFromPath = pathName;
    return bitmap;
  }

  @SuppressWarnings("ObjectToString")
  @Implementation
  protected static Bitmap decodeFileDescriptor(
      FileDescriptor fd, Rect outPadding, BitmapFactory.Options opts) {
    Point imageSizeFromStream = null;
    // If a real FileDescriptor is used, attempt to get the image size.
    if (fd != null && fd.valid()) {
      try (FileInputStream fileInputStream = new FileInputStream(fd);
          BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream); ) {
        imageSizeFromStream = getImageSizeFromStream(bufferedInputStream);
      } catch (IOException e) {
        Logger.warn("Error getting size of bitmap file", e);
      }
    }
    Bitmap bitmap = create("fd:" + fd, outPadding, opts, imageSizeFromStream);
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

    String name = (is instanceof NamedStream)
        ? is.toString().replace("stream for ", "")
        : null;
    Point imageSize = (is instanceof NamedStream) ? null : getImageSizeFromStream(is);
    Bitmap bitmap = create(name, outPadding, opts, imageSize);
    ReflectionHelpers.callInstanceMethod(bitmap, "setNinePatchChunk",
            ClassParameter.from(byte[].class, ninePatchChunk));
    ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
    shadowBitmap.createdFromStream = is;

    try {
      if (is != null && opts != null) {
        is.reset();
        opts.outMimeType = URLConnection.guessContentTypeFromStream(is);
      }
    } catch (IOException e) {
      // ignore
    }
    return bitmap;
  }

  @Implementation
  protected static Bitmap decodeByteArray(byte[] data, int offset, int length) {
    Bitmap bitmap = decodeByteArray(data, offset, length, new BitmapFactory.Options());
    ShadowBitmap shadowBitmap = Shadow.extract(bitmap);
    shadowBitmap.createdFromBytes = data;
    return bitmap;
  }

  @Implementation
  protected static Bitmap decodeByteArray(
      byte[] data, int offset, int length, BitmapFactory.Options opts) {
    String desc = new String(data, UTF_8);

    if (offset != 0 || length != data.length) {
      desc += " bytes " + offset + ".." + length;
    }

    Point imageSize = getImageSizeFromStream(new ByteArrayInputStream(data, offset, length));
    return create(desc, opts, imageSize);
  }

  static Bitmap create(String name) {
    return create(name, null);
  }

  public static Bitmap create(String name, BitmapFactory.Options options) {
    return create(name, options, null);
  }

  public static Bitmap create(final String name, final BitmapFactory.Options options, final Point widthAndHeight) {
    return create(name, null, options, widthAndHeight);
  }

  private static Bitmap create(
      final String name,
      final Rect outPadding,
      final BitmapFactory.Options options,
      final Point widthAndHeight) {
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

    Point p = new Point(selectWidthAndHeight(name, widthAndHeight));
    if (options != null && options.inSampleSize > 1) {
      p.x = p.x / options.inSampleSize;
      p.y = p.y / options.inSampleSize;

      p.x = p.x == 0 ? 1 : p.x;
      p.y = p.y == 0 ? 1 : p.y;
    }

    shadowBitmap.setWidth(p.x);
    shadowBitmap.setHeight(p.y);
    shadowBitmap.setPixels(new int[p.x * p.y], 0, 0, 0, 0, p.x, p.y);
    if (options != null) {
      options.outWidth = p.x;
      options.outHeight = p.y;
    }

    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.KITKAT) {
      ReflectionHelpers.callStaticMethod(
          BitmapFactory.class,
          "setDensityFromOptions",
          ClassParameter.from(Bitmap.class, bitmap),
          ClassParameter.from(BitmapFactory.Options.class, options));
    } else {
      bitmap = ReflectionHelpers.callStaticMethod(
          BitmapFactory.class,
          "finishDecode",
          ClassParameter.from(Bitmap.class, bitmap),
          ClassParameter.from(Rect.class, outPadding),
          ClassParameter.from(BitmapFactory.Options.class, options));
    }
    return bitmap;
  }

  public static void provideWidthAndHeightHints(Uri uri, int width, int height) {
    widthAndHeightMap.put(uri.toString(), new Point(width, height));
  }

  public static void provideWidthAndHeightHints(int resourceId, int width, int height) {
    widthAndHeightMap.put(
        "resource:"
            + RuntimeEnvironment.getApplication().getResources().getResourceName(resourceId),
        new Point(width, height));
  }

  public static void provideWidthAndHeightHints(String file, int width, int height) {
    widthAndHeightMap.put("file:" + file, new Point(width, height));
  }

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
  }

  private static Point selectWidthAndHeight(final String name, final Point widthAndHeight) {
    final Point widthAndHeightFromMap = widthAndHeightMap.get(name);

    if (widthAndHeightFromMap != null) {
      return widthAndHeightFromMap;
    }

    if (widthAndHeight != null) {
      return widthAndHeight;
    }

    return new Point(100, 100);
  }
}
