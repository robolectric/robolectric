package org.robolectric.integrationtests.mockito.experimental;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/** Tests the ability to mock final classes and methods with mockito-inline. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class MockitoSpyTest {

  /** Regression test for https://github.com/mockito/mockito/issues/2040 */
  @Test
  public void spyActivity_hasSameBaseContext() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    Activity spyActivity = spy(activity);
    assertThat(activity.getBaseContext()).isEqualTo(spyActivity.getBaseContext());
  }

  @Test
  public void spyContext_canSpyGetSystemService() {
    Context context = spy(RuntimeEnvironment.getApplication());
    InputMethodManager expected = mock(InputMethodManager.class);
    when(context.getSystemService(Context.INPUT_METHOD_SERVICE)).thenReturn(expected);
    InputMethodManager actual =
        (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    assertThat(actual).isSameInstanceAs(expected);
  }
}
