package org.robolectric.util;

import static com.google.common.truth.Truth.assertThat;

import java.net.URL;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-10-04
 */
@RunWith(JUnit4.class)
public class UtilTest {
  @Test
  public void urlShouldReturnCorrectURLForSensibleOSes() throws Exception {
    final String unixPath = "/opt/test/myfile.jar";
    assertThat(Util.url(unixPath)).isEqualTo(new URL("file://" + unixPath));
  }

  @Test
  public void urlShouldReturnCorrectURLForWindowsLocal() throws Exception {
    // from https://blogs.msdn.microsoft.com/ie/2006/12/06/file-uris-in-windows/ sort of, but not
    final String windowsPath = "C:\\Documents and Settings\\davris\\FileSchemeURIs.doc";
    assertThat(Util.url(windowsPath))
        .isEqualTo(new URL("file:C:/Documents%20and%20Settings/davris/FileSchemeURIs.doc"));
  }

  @Test
  public void urlShouldReturnCorrectURLForWindowsUnc() throws Exception {
    // from https://blogs.msdn.microsoft.com/ie/2006/12/06/file-uris-in-windows/
    final String windowsUncPath = "\\\\laptop\\My Documents\\FileSchemeURIs.doc";
    assertThat(Util.url(windowsUncPath))
        .isEqualTo(new URL("file://laptop/My%20Documents/FileSchemeURIs.doc"));
  }
}
