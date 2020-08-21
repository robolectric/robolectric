package org.robolectric.integration_tests.mockito_experimental;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class MockitoMockJavaFrameworkTest {
  /**
   * Mocking Java classes with mockito-inline is currently broken.
   *
   * @see <a href="https://github.com/robolectric/robolectric/issues/5522">Issue 5522</a>
   */
  @Test
  @Ignore("Enable when Mockito 3.5.4 is released")
  public void file_getAbsolutePath_isMockable() throws Exception {
    File file = mock(File.class);
    doReturn("absolute/path").when(file).getAbsolutePath();
    assertThat(file.getAbsolutePath()).isEqualTo("absolute/path");
  }
}
