/* Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.webkit;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Fork of CTS's MimeTypeMapTest */
@RunWith(AndroidJUnit4.class)
public class MimeTypeMapTest {

  private MimeTypeMap mimeTypeMap;

  @Before
  public void setUp() throws Exception {
    mimeTypeMap = MimeTypeMap.getSingleton();
  }

  @Test
  public void testGetFileExtensionFromUrl() {
    assertThat(MimeTypeMap.getFileExtensionFromUrl("http://localhost/index.html"))
        .isEqualTo("html");
    assertThat(MimeTypeMap.getFileExtensionFromUrl("http://host/x.html?x=y")).isEqualTo("html");
    assertThat(MimeTypeMap.getFileExtensionFromUrl("http://www.example.com/")).isEmpty();
    assertThat(MimeTypeMap.getFileExtensionFromUrl("https://example.com/foo")).isEmpty();
    assertThat(MimeTypeMap.getFileExtensionFromUrl(null)).isEmpty();
    assertThat(MimeTypeMap.getFileExtensionFromUrl("")).isEmpty();
    assertThat(MimeTypeMap.getFileExtensionFromUrl("http://abc/&%$.()*")).isEmpty();
  }

  @Test
  public void testHasMimeType() {
    assertThat(mimeTypeMap.hasMimeType("audio/mpeg")).isTrue();
    assertThat(mimeTypeMap.hasMimeType("text/plain")).isTrue();

    assertThat(mimeTypeMap.hasMimeType("some_random_string")).isFalse();

    assertThat(mimeTypeMap.hasMimeType("")).isFalse();
    assertThat(mimeTypeMap.hasMimeType(null)).isFalse();
  }

  @Test
  public void testGetMimeTypeFromExtension() {
    assertThat(mimeTypeMap.getMimeTypeFromExtension("mp3")).isEqualTo("audio/mpeg");
    assertThat(mimeTypeMap.getMimeTypeFromExtension("zip")).isEqualTo("application/zip");
    assertThat(mimeTypeMap.getMimeTypeFromExtension("jpg")).isEqualTo("image/jpeg");
    assertThat(mimeTypeMap.getMimeTypeFromExtension("jpeg")).isEqualTo("image/jpeg");
    assertThat(mimeTypeMap.getMimeTypeFromExtension("m4a")).isEqualTo("audio/mpeg");
    assertThat(mimeTypeMap.getMimeTypeFromExtension("gif")).isEqualTo("image/gif");
    assertThat(mimeTypeMap.getMimeTypeFromExtension("pdf")).isEqualTo("application/pdf");
    assertThat(mimeTypeMap.getMimeTypeFromExtension("apk"))
        .isEqualTo("application/vnd.android.package-archive");
    assertThat(mimeTypeMap.getMimeTypeFromExtension("3gp")).isEqualTo("video/3gpp");
    // Android <= 24 maps 3gpp to audio/3gpp
    assertThat(mimeTypeMap.getMimeTypeFromExtension("3gpp")).isAnyOf("video/3gpp", "audio/3gpp");

    assertThat(mimeTypeMap.getMimeTypeFromExtension("some_random_string")).isNull();

    assertThat(mimeTypeMap.getMimeTypeFromExtension(null)).isNull();
    assertThat(mimeTypeMap.getMimeTypeFromExtension("")).isNull();
  }

  @Test
  public void testHasExtension() {
    assertThat(mimeTypeMap.hasExtension("mp3")).isTrue();
    assertThat(mimeTypeMap.hasExtension("zip")).isTrue();

    assertThat(mimeTypeMap.hasExtension("some_random_string")).isFalse();

    assertThat(mimeTypeMap.hasExtension("")).isFalse();
    assertThat(mimeTypeMap.hasExtension(null)).isFalse();
  }

  @Test
  public void testGetExtensionFromMimeType() {
    assertThat(mimeTypeMap.getExtensionFromMimeType("audio/mpeg")).isEqualTo("mp3");
    assertThat(mimeTypeMap.getExtensionFromMimeType("image/png")).isEqualTo("png");
    assertThat(mimeTypeMap.getExtensionFromMimeType("application/zip")).isEqualTo("zip");
    assertThat(mimeTypeMap.getExtensionFromMimeType("video/mp4")).isEqualTo("mp4");
    assertThat(mimeTypeMap.getExtensionFromMimeType("image/jpeg")).isAnyOf("jpg", "jpeg");
    assertThat(mimeTypeMap.getExtensionFromMimeType("text/plain")).isEqualTo("txt");

    assertThat(mimeTypeMap.getExtensionFromMimeType("video/3gp")).isNull();
    assertThat(mimeTypeMap.getExtensionFromMimeType("some_random_string")).isNull();
    assertThat(mimeTypeMap.getExtensionFromMimeType("application/text")).isNull();

    assertThat(mimeTypeMap.getExtensionFromMimeType(null)).isNull();
    assertThat(mimeTypeMap.getExtensionFromMimeType("")).isNull();
  }

  @Test
  public void testGetSingleton() {
    MimeTypeMap firstMimeTypeMap = MimeTypeMap.getSingleton();
    MimeTypeMap secondMimeTypeMap = MimeTypeMap.getSingleton();

    assertThat(secondMimeTypeMap).isSameInstanceAs(firstMimeTypeMap);
  }
}
