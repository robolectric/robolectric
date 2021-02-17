package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.net.Uri;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowUriTest {
  @Test
  public void shouldParseUris() {
    Uri testUri =
        Uri.parse("http://someplace.com:8080/a/path?param=value&another_param=another_value#top");

    assertThat(testUri.getQuery()).isEqualTo("param=value&another_param=another_value");
    assertThat(testUri.getPort()).isEqualTo(8080);
    assertThat(testUri.getAuthority()).isEqualTo("someplace.com:8080");
    assertThat(testUri.getHost()).isEqualTo("someplace.com");
    assertThat(testUri.getFragment()).isEqualTo("top");
    assertThat(testUri.getPath()).isEqualTo("/a/path");
    assertThat(testUri.getScheme()).isEqualTo("http");
  }

  @Test
  public void getQueryParameter_shouldWork() {
    Uri testUri =
        Uri.parse("http://someplace.com:8080/a/path?param=value&another_param=another_value#top");
    assertThat(testUri.getQueryParameter("param")).isEqualTo("value");
  }

  // Captures a known issue in Android Q+ issue where Uri.EMPTY may not be initialized properly if
  // Uri.Builder is used before Uri.<clinit> runs.
  @Test
  public void testEmpty_initializerOrder() {
    new Uri.Builder().scheme("http").path("path").build();
    assertThat(Uri.EMPTY.toString()).isEmpty();
  }
}
