package org.robolectric.shadows;

import android.graphics.Bitmap.CompressFormat;
import android.graphics.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

public class ImageUtil {
  private static boolean initialized;

  /** Stops ImageIO from creating temp files when reading images from input stream. */
  private static void init() {
    if (!initialized) {
      ImageIO.setUseCache(false);
      initialized = true;
    }
  }

  public static Point getImageSizeFromStream(InputStream is) {
    init();

    try {
      ImageInputStream imageStream = ImageIO.createImageInputStream(is);
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

  /** Callback to receive information about a bitmap. */
  public interface BitmapDataCallback {
    void receiveBitmapData(Point size, int[] pixels);
  }

  public static void readBitmapFromStream(InputStream is, BitmapDataCallback callback) {
    init();

    try {
      // BufferedImage image = ImageIO.read(is);
      ImageInputStream imageStream = ImageIO.createImageInputStream(is);
      Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);
      if (!readers.hasNext()) {
        return;
      }

      ImageReader reader = readers.next();
      try {
        reader.setInput(imageStream);
        BufferedImage image = reader.read(0);
        int width = image.getWidth();
        int height = image.getHeight();
        callback.receiveBitmapData(
            new Point(width, height), image.getRGB(0, 0, width, height, null, 0, width));
      } finally {
        reader.dispose();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean writeToStream(
      int[] pixels,
      int width,
      int height,
      CompressFormat format,
      int quality,
      OutputStream stream) {
    if ((quality < 0) || (quality > 100)) {
      throw new IllegalArgumentException("Quality out of bounds!");
    }

    try {
      ImageWriter writer = null;
      Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(getFormatName(format));
      if (iter.hasNext()) {
        writer = (ImageWriter) iter.next();
      }
      try (ImageOutputStream ios = ImageIO.createImageOutputStream(stream)) {
        writer.setOutput(ios);
        ImageWriteParam iwparam = new JPEGImageWriteParam(Locale.getDefault());
        iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        iwparam.setCompressionQuality((quality / 100f));
        BufferedImage bufferedImage =
            new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        bufferedImage.setRGB(0, 0, width, height, pixels, 0, width);
        writer.write(null, new IIOImage(bufferedImage, null, null), iwparam);
        ios.flush();
        writer.dispose();
      }

    } catch (IOException e) {
      return false;
    }

    return true;
  }

  private static String getFormatName(CompressFormat compressFormat) {
    switch (compressFormat) {
      case JPEG:
      case WEBP:
      case WEBP_LOSSY:
      case WEBP_LOSSLESS:
        return "jpg";
      case PNG:
        return "png";
    }
    throw new UnsupportedOperationException("Cannot convert format: " + compressFormat);
  }
}
