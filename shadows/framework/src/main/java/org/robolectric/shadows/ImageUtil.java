package org.robolectric.shadows;

import android.graphics.Point;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

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
}
