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

        final String windowsPath = "E:\\Test\\Path With Spaces\\MyFile.jar";
        final URL windowsUrl = Util.url(windowsPath);
        assertThat(windowsUrl.toString()).isEqualTo("file:/" + windowsPath.replace('\\', '/'));

        final String unixPath = "/opt/test/myfile.jar";
        final URL unixUrl = Util.url(unixPath);
        assertThat(unixUrl.toString()).isEqualTo("file:" + unixPath);

    }
}
