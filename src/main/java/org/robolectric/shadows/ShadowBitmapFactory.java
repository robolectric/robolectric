package org.robolectric.shadows;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.util.TypedValue;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.NamedStream;
import org.robolectric.util.Join;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static org.fest.reflect.core.Reflection.method;
import static org.robolectric.Robolectric.directlyOn;
import static org.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BitmapFactory.class)
public class ShadowBitmapFactory {
  private static Map<String, Point> widthAndHeightMap = new HashMap<String, Point>();

  @Implementation
  public static Bitmap decodeResourceStream(Resources res, TypedValue value,
                        InputStream is, Rect pad, BitmapFactory.Options opts) {
    Bitmap bitmap = (Bitmap) directlyOn(BitmapFactory.class, "decodeResourceStream",
        Resources.class, TypedValue.class,
        InputStream.class, Rect.class, BitmapFactory.Options.class)
        .invoke(res, value, is, pad, opts);

    if (value != null && value.string != null && value.string.toString().contains(".9.")) {
      // todo: better support for nine-patches
      method("setNinePatchChunk").withParameterTypes(byte[].class).in(bitmap).invoke(new byte[0]);
    }
    return bitmap;
  }

  @Implementation
  public static Bitmap decodeResource(Resources res, int id, BitmapFactory.Options options) {
    Bitmap bitmap = create("resource:" + getResourceName(id), options);
    shadowOf(bitmap).createdFromResId = id;
    return bitmap;
  }

  @Implementation
  public static Bitmap decodeResource(Resources res, int id) {
    return decodeResource(res, id, null);
  }

  private static String getResourceName(int id) {
    return shadowOf(Robolectric.application).getResourceLoader().getNameForId(id);
  }

  @Implementation
  public static Bitmap decodeFile(String pathName) {
    return decodeFile(pathName, null);
  }

  @Implementation
  public static Bitmap decodeFile(String pathName, BitmapFactory.Options options) {
    Bitmap bitmap = create("file:" + pathName, options);
    ShadowBitmap shadowBitmap = shadowOf(bitmap);
    shadowBitmap.createdFromPath = pathName;
    return bitmap;
  }

  @Implementation
  public static Bitmap decodeStream(InputStream is) {
    return decodeStream(is, null, null);
  }

  @Implementation
  public static Bitmap decodeStream(InputStream is, Rect outPadding, BitmapFactory.Options opts) {
    Bitmap bitmap = create(is instanceof NamedStream ? is.toString().replace("stream for ", "") : null, opts);
    ShadowBitmap shadowBitmap = shadowOf(bitmap);
    shadowBitmap.createdFromStream = is;
    return bitmap;
  }

  @Implementation
  public static Bitmap decodeByteArray(byte[] data, int offset, int length) {
    Bitmap bitmap = decodeByteArray(data, offset, length, new BitmapFactory.Options());
    ShadowBitmap shadowBitmap = shadowOf(bitmap);
    shadowBitmap.createdFromBytes = data;
    return bitmap;
  }

  @Implementation
  public static Bitmap decodeByteArray(byte[] data, int offset, int length, BitmapFactory.Options opts) {
    String desc = new String(data);
    if (!Charset.forName("US-ASCII").newEncoder().canEncode(desc)) {
      Checksum checksumEngine = new CRC32();
      checksumEngine.update(data, 0, data.length);

      desc = "byte array, checksum: " + checksumEngine.getValue();
    }

    if (offset != 0 || length != data.length) {
      desc += " bytes " + offset + ".." + length;
    }
    return create(desc, opts);
  }

  static Bitmap create(String name) {
    return create(name, null);
  }

  public static Bitmap create(String name, BitmapFactory.Options options) {
    Bitmap bitmap = Robolectric.newInstanceOf(Bitmap.class);
    ShadowBitmap shadowBitmap = shadowOf(bitmap);
    shadowBitmap.appendDescription(name == null ? "Bitmap" : "Bitmap for " + name);

    String optionsString = stringify(options);
    if (!optionsString.isEmpty()) {
      shadowBitmap.appendDescription(" with options ");
      shadowBitmap.appendDescription(optionsString);
    }

    Point widthAndHeight = widthAndHeightMap.get(name);
    if (widthAndHeight == null) {
      widthAndHeight = new Point(100, 100);
    }

    Point p = new Point(widthAndHeight);
    if (options != null && options.inSampleSize > 1) {
      p.x = p.x / options.inSampleSize;
      p.y = p.y / options.inSampleSize;

      p.x = p.x == 0 ? 1 : p.x;
      p.y = p.y == 0 ? 1 : p.y;
    }

    shadowBitmap.setWidth(p.x);
    shadowBitmap.setHeight(p.y);
    if (options != null) {
      options.outWidth = p.x;
      options.outHeight = p.y;
    }
    return bitmap;
  }

  public static void provideWidthAndHeightHints(Uri uri, int width, int height) {
    widthAndHeightMap.put(uri.toString(), new Point(width, height));
  }

  public static void provideWidthAndHeightHints(int resourceId, int width, int height) {
    widthAndHeightMap.put("resource:" + getResourceName(resourceId), new Point(width, height));
  }

  public static void provideWidthAndHeightHints(String file, int width, int height) {
    widthAndHeightMap.put("file:" + file, new Point(width, height));
  }

  private static String stringify(BitmapFactory.Options options) {
    if (options == null) return "";
    List<String> opts = new ArrayList<String>();

    if (options.inJustDecodeBounds) opts.add("inJustDecodeBounds");
    if (options.inSampleSize > 1) opts.add("inSampleSize=" + options.inSampleSize);

    return Join.join(", ", opts);
  }

  public static void reset() {
    widthAndHeightMap.clear();
  }
}
