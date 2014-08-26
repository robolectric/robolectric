package org.robolectric.shadows;

import android.net.Uri;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class UriTest {
  @Test
  public void shouldParseUris() throws Exception {
    Uri testUri = Uri.parse("http://someplace.com:8080/a/path?param=value&another_param=another_value#top");

    assertThat(testUri.getQuery()).isEqualTo("param=value&another_param=another_value");
    assertThat(testUri.getPort()).isEqualTo(8080);
    assertThat(testUri.getAuthority()).isEqualTo("someplace.com:8080");
    assertThat(testUri.getHost()).isEqualTo("someplace.com");
    assertThat(testUri.getFragment()).isEqualTo("top");
    assertThat(testUri.getPath()).isEqualTo("/a/path");
    assertThat(testUri.getScheme()).isEqualTo("http");
  }

  @Test public void getQueryParameter_shouldWork() throws Exception {
    Uri testUri = Uri.parse("http://someplace.com:8080/a/path?param=value&another_param=another_value#top");
    assertThat(testUri.getQueryParameter("param")).isEqualTo("value");
  }
}
