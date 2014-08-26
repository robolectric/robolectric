package org.robolectric.shadows;

import android.app.Activity;
import android.os.Bundle;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.junit.Assert.assertNotNull;

@RunWith(TestRunners.WithDefaults.class)
public class LocalActivityManagerTest {
  @Ignore("deprecated in Android; use fragments instead!")
  @Test
  public void testInflation() throws Exception {
    OuterActivity activity = Robolectric.buildActivity(OuterActivity.class).create().get();
    // Make sure the container lookup works.
    assertNotNull(activity.findViewById(R.id.lam_container));
    // And make sure the view inside the LocalActivityManager-created view works too.
    assertNotNull(activity.findViewById(R.id.lam_inner_contents));
  }

  public static class OuterActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.lam_outer);
    }
  }
}
