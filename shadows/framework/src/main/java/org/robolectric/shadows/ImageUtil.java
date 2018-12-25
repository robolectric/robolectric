package org.robolectric.shadows;

import android.graphics.Bitmap;
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

  public static Point getImageSizeFromStream(InputStream is) {
    if (!initialized) {
      // Stops ImageIO from creating temp files when reading images
      // from input stream.
      ImageIO.setUseCache(false);
      initialized = true;
    }

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

  public static boolean writeToStream(
      Bitmap realBitmap, CompressFormat format, int quality, OutputStream stream) {
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
        writer.write(
            null,
            new IIOImage(
                new BufferedImage(
                    realBitmap.getWidth(), realBitmap.getHeight(), BufferedImage.TYPE_BYTE_BINARY),
                null,
                null),
            iwparam);
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
        return "jpg";
      case PNG:
        return "png";
    }
    throw new UnsupportedOperationException("Cannot convert format: " + compressFormat);
  }
}
