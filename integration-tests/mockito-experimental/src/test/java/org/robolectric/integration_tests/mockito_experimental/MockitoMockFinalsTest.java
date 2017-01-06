package org.robolectric.integration_tests.mockito_experimental;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
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
public class MockitoMockFinalsTest {
  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  TextView textView;

  @InjectMocks
  Activity activity = Robolectric.setupActivity(FragmentActivity.class);

  @Test
  public void testInjection() {
    activity.finish();
  }
}
