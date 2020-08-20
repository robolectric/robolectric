package org.robolectric.integration_tests.mockito_experimental;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.text.Layout;
import android.widget.TextView;
import java.io.File;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class MockitoMockFinalsTest {
  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  TextView textView;

  @Test
  public void testInjection() {
    Layout layout = mock(Layout.class);
    when(textView.getLayout()).thenReturn(layout);
    assertThat(textView.getLayout()).isSameInstanceAs(layout);
  }

  @Test
  public void canMockUserId() {
    User user = mock(User.class);
    when(user.getId()).thenReturn(1);
    assertThat(user.getId()).isEqualTo(1);
  }


  final static class User {
    final int getId() {
      return -1;
    }
  }

  /**
   * Mocking Java classes with mockito-inline is currently broken.
   *
   * @see <a href="https://github.com/robolectric/robolectric/issues/5522">Issue 5522</a>
   */
  @Test
  @Ignore
  public void file_getAbsolutePath_isMockable() throws Exception {
    File file = mock(File.class);
    doReturn("absolute/path").when(file).getAbsolutePath();
    assertThat(file.getAbsolutePath()).isEqualTo("absolute/path");
  }
}
