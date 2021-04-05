package org.robolectric.shadows;

import static java.awt.image.AffineTransformOp.TYPE_BILINEAR;
import static java.awt.image.AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static javax.imageio.ImageIO.createImageInputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Point;
import com.google.auto.value.AutoValue;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
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
import org.robolectric.Shadows;
import org.robolectric.shadow.api.Shadow;

public class ImageUtil {
  private static final String FORMAT_NAME_JPEG = "jpg";
  private static final String FORMAT_NAME_PNG = "png";
  private static boolean initialized;

  static Point getImageSizeFromStream(InputStream is) {
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

  static RobolectricBufferedImage getImageFromStream(InputStream is) {
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
        return RobolectricBufferedImage.create(image, ("image/" + format).toLowerCase());
      } finally {
        reader.dispose();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static boolean scaledBitmap(Bitmap src, Bitmap dst, boolean filter) {
    if (src == null || dst == null) {
      return false;
    }
    int srcWidth = src.getWidth();
    int srcHeight = src.getHeight();
    if (srcWidth <= 0 || srcHeight <= 0) {
      return false;
    }
    BufferedImage before = ((ShadowBitmap) Shadow.extract(src)).getBufferedImage();
    if (before == null || before.getColorModel() == null) {
      return false;
    }
    int dstWidth = dst.getWidth();
    int dstHeight = dst.getHeight();
    int imageType = getBufferedImageType(src.getConfig(), before.getColorModel().hasAlpha());
    BufferedImage after = new BufferedImage(dstWidth, dstHeight, imageType);
    AffineTransform at = new AffineTransform();
    at.scale(dstWidth * 1.0f / srcWidth, dstHeight * 1.0f / srcHeight);
    // See Android's Bitmap#createScaledBitmap SDK doc
    int interpolationType = filter ? TYPE_BILINEAR : TYPE_NEAREST_NEIGHBOR;
    AffineTransformOp scaleOp = new AffineTransformOp(at, interpolationType);
    ShadowBitmap shadowBitmap = Shadow.extract(dst);
    shadowBitmap.setBufferedImage(scaleOp.filter(before, after));
    return true;
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
        boolean needAlphaChannel = needAlphaChannel(format);
        BufferedImage bufferedImage = Shadows.shadowOf(realBitmap).getBufferedImage();
        if (bufferedImage == null) {
          bufferedImage =
              new BufferedImage(
                  realBitmap.getWidth(), realBitmap.getHeight(), BufferedImage.TYPE_INT_ARGB);
        }
        int outputImageType = getBufferedImageType(realBitmap.getConfig(), needAlphaChannel);
        if (outputImageType != BufferedImage.TYPE_INT_ARGB) {
          // re-encode image data with a type that is compatible with the output format.
          BufferedImage outputBufferedImage = new BufferedImage(width, height, outputImageType);
          Graphics2D g = outputBufferedImage.createGraphics();
          g.drawImage(bufferedImage, 0, 0, null);
          g.dispose();
          bufferedImage = outputBufferedImage;
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

  @AutoValue
  abstract static class RobolectricBufferedImage {
    abstract BufferedImage getBufferedImage();

    abstract String getMimeType();

    public Point getWidthAndHeight() {
      return new Point(getBufferedImage().getWidth(), getBufferedImage().getHeight());
    }

    static RobolectricBufferedImage create(BufferedImage bufferedImage, String mimeType) {
      return new AutoValue_ImageUtil_RobolectricBufferedImage(bufferedImage, mimeType);
    }
  }
}
