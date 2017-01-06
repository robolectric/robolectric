package org.robolectric;

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

@RunWith(RobolectricTestRunner.class)
public class MockitoInjectMocksTest {
  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  TextView textView;

  @InjectMocks
  Activity activity = Robolectric.setupActivity(FragmentActivity.class);

  @Test
  public void test() {
    activity.finish();
  }
}
