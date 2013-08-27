package org.robolectric.util;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.TestRunners;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.robolectric.Robolectric.buildActivity;

@RunWith(TestRunners.WithDefaults.class)
public class LayoutInflatedFragmentTest {

  @Test public void shouldFindViewOnFragment() throws Exception {
    TestActivity activity = buildActivity(TestActivity.class).create().get();
    Fragment fragment = activity.getSupportFragmentManager().findFragmentById(R.id.fragment);
    assertThat(fragment, notNullValue());
    assertThat(fragment.getView(), notNullValue());
  }

  public static class TestActivity extends FragmentActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_fragment);
    }
  }
}
