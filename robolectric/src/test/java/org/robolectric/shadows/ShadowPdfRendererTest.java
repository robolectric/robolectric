package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowPdfRenderer.ShadowPage;

/** Test for {@link ShadowPdfRenderer}. */
@RunWith(AndroidJUnit4.class)
public class ShadowPdfRendererTest {

  private PdfRenderer pdfRenderer;
  private ShadowPdfRenderer shadowPdfRenderer;

  @Before
  public void setUp() throws Exception {
    File f = new File(ApplicationProvider.getApplicationContext().getFilesDir(), "test.pdf");
    ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, -1);
    pdfRenderer = new PdfRenderer(pfd);
    shadowPdfRenderer = Shadows.shadowOf(pdfRenderer);
  }

  @After
  public void tearDown() throws Exception {
    pdfRenderer.close();
  }

  @Test
  public void getPageCount_defaultPageZero() {
    assertThat(pdfRenderer.getPageCount()).isEqualTo(0);
  }

  @Test
  public void getPageCount_setPageCountViaShadow_returnPresetPageCount() {
    shadowPdfRenderer.setPageCount(10);

    assertThat(pdfRenderer.getPageCount()).isEqualTo(10);
  }

  // We need to use @Test(expected = Exception) here since assertThrows is not available in the
  // public version of junit.
  @SuppressWarnings("TestExceptionChecker")
  @Test(expected = FileNotFoundException.class)
  public void getPageCount_setExceptionViaShadow_throwException() {
    shadowPdfRenderer.setException(new FileNotFoundException("pdf file not found"));

    pdfRenderer.getPageCount();
  }

  @Test
  public void shouldScaleForPrinting_defaultToFalse() {
    assertThat(pdfRenderer.shouldScaleForPrinting()).isFalse();
  }

  @Test
  public void shouldScaleForPrinting_setTrueViaShadow_returnTrue() {
    shadowPdfRenderer.setShouldScaleForPrinting(true);

    assertThat(pdfRenderer.shouldScaleForPrinting()).isTrue();
  }

  // We need to use @Test(expected = Exception) here since assertThrows is not available in the
  // public version of junit.
  @SuppressWarnings("TestExceptionChecker")
  @Test(expected = IOException.class)
  public void shouldScaleForPrinting_setExceptionViaShadow_throwException() {
    shadowPdfRenderer.setException(new IOException("open failed"));

    pdfRenderer.shouldScaleForPrinting();
  }

  // We need to use @Test(expected = Exception) here since assertThrows is not available in the
  // public version of junit.
  @SuppressWarnings("TestExceptionChecker")
  @Test(expected = RuntimeException.class)
  public void openPage_setExceptionViaShadow_throwException() {
    shadowPdfRenderer.setException(new RuntimeException("Native crash"));

    pdfRenderer.openPage(0);
  }

  // We need to use @Test(expected = Exception) here since assertThrows is not available in the
  // public version of junit.
  @SuppressWarnings("TestExceptionChecker")
  @Test(expected = IllegalArgumentException.class)
  public void openPage_pageIndexOutOfBound_throwIllegalArgumentException() {
    shadowPdfRenderer.setPageCount(10);

    pdfRenderer.openPage(10);
  }

  @Test
  public void openPage_openSuccessfully() throws Exception {
    shadowPdfRenderer.setPageCount(10);

    PdfRenderer.Page page = shadowPdfRenderer.openPage(1);
    ShadowPage shadowPage = Shadows.shadowOf(page);

    assertThat(shadowPage.getWidth()).isEqualTo(0);
    assertThat(shadowPage.getHeight()).isEqualTo(0);
    assertThat(shadowPage.getIndex()).isEqualTo(1);
  }
}
