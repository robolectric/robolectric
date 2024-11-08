package org.robolectric.integrationtests.agp;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TestActivityTest {

  @Test
  public void testActivitiesCanBeDeclaredInADependencyLibrary() {
    ActivityScenario.launch(TestActivity.class);
  }
}
