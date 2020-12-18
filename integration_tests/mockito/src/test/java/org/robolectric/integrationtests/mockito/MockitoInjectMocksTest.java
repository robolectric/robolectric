package org.robolectric.integrationtests.mockito;

import android.app.Activity;
import android.widget.TextView;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class MockitoInjectMocksTest {
  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  TextView textView;

  @InjectMocks
  Activity activity = Robolectric.setupActivity(Activity.class);

  @Test
  public void testInjection() {
    activity.finish();
  }
}
