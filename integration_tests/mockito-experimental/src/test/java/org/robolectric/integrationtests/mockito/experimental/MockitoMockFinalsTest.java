package org.robolectric.integrationtests.mockito.experimental;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.text.Layout;
import android.widget.TextView;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;

/** Tests the ability to mock final classes and methods with mockito-inline. */
@RunWith(RobolectricTestRunner.class)
public class MockitoMockFinalsTest {
  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock TextView textView;

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

  static final class User {
    final int getId() {
      return -1;
    }
  }
}
