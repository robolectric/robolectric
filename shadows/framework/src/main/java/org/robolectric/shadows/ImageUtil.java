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
        BufferedImage bufferedImage =
            new BufferedImage(
                realBitmap.getWidth(),
                realBitmap.getHeight(),
                getBufferedImageType(realBitmap.getConfig(), needAlphaChannel(format)));
        for (int x = 0; x < realBitmap.getWidth(); x++) {
          for (int y = 0; y < realBitmap.getHeight(); y++) {
            bufferedImage.setRGB(x, y, realBitmap.getPixel(x, y));
          }
        }
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
}
