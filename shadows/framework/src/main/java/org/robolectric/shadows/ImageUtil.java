package org.robolectric.shadows;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static javax.imageio.ImageIO.createImageInputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

public class ImageUtil {
  private static final String FORMAT_NAME_JPEG = "jpg";
  private static final String FORMAT_NAME_PNG = "png";
  private static boolean initialized;

  public static Point getImageSizeFromStream(InputStream is) {
    if (!initialized) {
      // Stops ImageIO from creating temp files when reading images
      // from input stream.
      ImageIO.setUseCache(false);
      initialized = true;
    }

    try {
      ImageInputStream imageStream = createImageInputStream(is);
      Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);
      if (!readers.hasNext()) return null;

      ImageReader reader = readers.next();
      try {
        reader.setInput(imageStream);
        return new Point(reader.getWidth(0), reader.getHeight(0));
      } finally {
        reader.dispose();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static RobolectricBufferedImage getImageFromStream(InputStream is) {
    if (!initialized) {
      // Stops ImageIO from creating temp files when reading images
      // from input stream.
      ImageIO.setUseCache(false);
      initialized = true;
    }

    try {
      ImageInputStream imageStream = createImageInputStream(is);
      Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);
      if (!readers.hasNext()) return null;

      ImageReader reader = readers.next();
      try {
        reader.setInput(imageStream);
        String format = reader.getFormatName();
        int minIndex = reader.getMinIndex();
        BufferedImage image = reader.read(minIndex);
        RobolectricBufferedImage robolectricBufferedImage = new RobolectricBufferedImage();
        robolectricBufferedImage.bufferedImage = image;
        robolectricBufferedImage.mimeType = ("image/" + format).toLowerCase();
        return robolectricBufferedImage;
      } finally {
        reader.dispose();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean writeToStream(
      Bitmap realBitmap, CompressFormat format, int quality, OutputStream stream) {
    if ((quality < 0) || (quality > 100)) {
      throw new IllegalArgumentException("Quality out of bounds!");
    }

    try {
      ImageWriter writer = null;
      Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(getFormatName(format));
      if (iter.hasNext()) {
        writer = iter.next();
      }
      if (writer == null) {
        return false;
      }
      try (ImageOutputStream ios = ImageIO.createImageOutputStream(stream)) {
        writer.setOutput(ios);
        ImageWriteParam iwparam = writer.getDefaultWriteParam();
        iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwparam.setCompressionQuality((quality / 100f));
        int width = realBitmap.getWidth();
        int height = realBitmap.getHeight();
        BufferedImage bufferedImage =
            new BufferedImage(
                width,
                height,
                getBufferedImageType(realBitmap.getConfig(), needAlphaChannel(format)));
        int[] pixels = new int[width * height];
        realBitmap.getPixels(pixels, 0, 0, 0, 0, width, height);
        bufferedImage.setRGB(0, 0, width, height, pixels, 0, width);
        writer.write(null, new IIOImage(bufferedImage, null, null), iwparam);
        ios.flush();
        writer.dispose();
      }
    } catch (IOException ignore) {
      return false;
    }

    return true;
  }

  private static String getFormatName(CompressFormat compressFormat) {
    switch (compressFormat) {
      case JPEG:
        return FORMAT_NAME_JPEG;
      case WEBP:
      case WEBP_LOSSY:
      case WEBP_LOSSLESS:
      case PNG:
        return FORMAT_NAME_PNG;
    }
    throw new UnsupportedOperationException("Cannot convert format: " + compressFormat);
  }

  private static boolean needAlphaChannel(CompressFormat compressFormat) {
    return !FORMAT_NAME_JPEG.equals(getFormatName(compressFormat));
  }

  private static int getBufferedImageType(Bitmap.Config config, boolean needAlphaChannel) {
    if (config == null) {
      return needAlphaChannel ? TYPE_INT_ARGB : TYPE_INT_RGB;
    }
    switch (config) {
      case RGB_565:
        return BufferedImage.TYPE_USHORT_565_RGB;
      case RGBA_F16:
        return needAlphaChannel ? TYPE_INT_ARGB_PRE : TYPE_INT_RGB;
      case ALPHA_8:
      case ARGB_4444:
      case ARGB_8888:
      case HARDWARE:
      default:
        return needAlphaChannel ? TYPE_INT_ARGB : TYPE_INT_RGB;
    }
  }

  static class RobolectricBufferedImage {
    BufferedImage bufferedImage;
    String mimeType;
  }
}
