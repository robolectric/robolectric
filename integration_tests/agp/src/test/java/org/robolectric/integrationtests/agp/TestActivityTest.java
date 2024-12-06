package org.robolectric.integrationtests.agp;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.integrationtests.agp.testsupport.TestActivity;

/**
 * Test asserting that test-only activities can be declared in a dependency project's manifest as a
 * workaround for the fact that Android Gradle Plugin doesn't merge the test manifest (as of 3.4).
 *
 * <p>When http://issuetracker.google.com/issues/127986458 is fixed, we can collapse {@code
 * :integration_tests:agp:testsupport} back into {@code :integration_tests:agp}.
 */
@RunWith(AndroidJUnit4.class)
public class TestActivityTest {

  @Test
  public void testActivitiesCanBeDeclaredInADependencyLibrary() throws Exception {
    ActivityScenario.launch(TestActivity.class);
  }
}
