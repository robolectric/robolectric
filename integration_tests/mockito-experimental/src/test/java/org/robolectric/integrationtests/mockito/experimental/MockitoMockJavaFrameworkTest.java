package org.robolectric.integrationtests.mockito.experimental;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Tests the ability to mock Java framework methods with mockito-inline. */
@RunWith(RobolectricTestRunner.class)
public class MockitoMockJavaFrameworkTest {
  @Test
  public void file_getAbsolutePath_isMockable() throws Exception {
    File file = mock(File.class);
    doReturn("absolute/path").when(file).getAbsolutePath();
    assertThat(file.getAbsolutePath()).isEqualTo("absolute/path");
  }

  @Test
  public void cipher_getIV_isMockable() {
    Cipher cipher = mock(Cipher.class);
    doReturn("fake".getBytes(StandardCharsets.UTF_8)).when(cipher).getIV();
    assertThat(cipher.getIV()).isEqualTo("fake".getBytes(StandardCharsets.UTF_8));
  }
}
