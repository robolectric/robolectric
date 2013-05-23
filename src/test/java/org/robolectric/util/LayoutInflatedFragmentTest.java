package org.robolectric.util;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.robolectric.Robolectric.shadowOf;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.TestRunners;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

@RunWith(TestRunners.WithDefaults.class)
public class LayoutInflatedFragmentTest {
	
  @Test public void should_find_view_on_fragment() throws Exception {
    TestActivity activity = new TestActivity();
    shadowOf(activity).callOnCreate(null);
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
