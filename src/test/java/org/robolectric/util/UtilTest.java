package org.robolectric.util;

import org.junit.Test;

import java.net.URL;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Vyacheslav Mayorov
 * @since 2013-10-04
 */
public class UtilTest {
  @Test
  public void urlShouldReturnCorrectURL() throws Exception {
    final String uncPath = "\\\\hostname\\path\\to\\file.jar";
    assertThat(Util.url(uncPath)).isEqualTo(new URL("file:////hostname/path/to/file.jar"));

    final String windowsPath = "E:\\Test\\Path With Spaces\\MyFile.jar";
    assertThat(Util.url(windowsPath)).isEqualTo(new URL("file:/" + windowsPath));

    final String unixPath = "/opt/test/myfile.jar";
    assertThat(Util.url(unixPath)).isEqualTo(new URL("file://" + unixPath));

  }
}
