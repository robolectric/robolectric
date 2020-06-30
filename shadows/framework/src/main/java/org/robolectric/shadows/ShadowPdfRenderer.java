package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import com.google.common.base.Preconditions;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/** Shadow for {@link PdfRenderer}. */
@Implements(value = PdfRenderer.class, minSdk = LOLLIPOP)
public class ShadowPdfRenderer {
  @RealObject private PdfRenderer pdfRenderer;

  private int pageCount;
  private Exception exception;
  private boolean shouldScaleForPrinting;

  @Implementation
  protected int getPageCount() throws Exception {
    if (exception != null) {
      throw exception;
    }
    return pageCount;
  }

  @Implementation
  protected PdfRenderer.Page openPage(int index) throws Exception {
    if (exception != null) {
      throw exception;
    }
    Preconditions.checkArgument(index >= 0 && index < pageCount, "Page index out of bound");
    return pdfRenderer.new Page(index);
  }

  @Implementation
  protected boolean shouldScaleForPrinting() {
    return this.shouldScaleForPrinting;
  }

  public void setPageCount(int pageCount) {
    this.pageCount = pageCount;
  }

  public void setException(Exception exception) {
    this.exception = exception;
  }

  public void setShouldScaleForPrinting(boolean shouldScaleForPrinting) {
    this.shouldScaleForPrinting = shouldScaleForPrinting;
  }

  /** Shadow for {@link PdfRenderer.Page}. */
  @Implements(value = PdfRenderer.Page.class, minSdk = LOLLIPOP)
  public static class ShadowPage {
    private int height;
    private int width;
    private int index;

    @Implementation
    protected void __constructor__(int index) {
      this.index = index;
    }

    @Implementation
    protected int getHeight() throws Exception {
      return height;
    }

    @Implementation
    protected int getWidth() throws Exception {
      return width;
    }

    @Implementation
    protected int getIndex() throws Exception {
      return index;
    }

    @Implementation
    void render(Bitmap destination, Rect destClip, Matrix transform, int renderMode) {}

    public void setHeight(int height) {
      this.height = height;
    }

    public void setWidth(int width) {
      this.width = width;
    }

    public void setIndex(int index) {
      this.index = index;
    }
  }
}
