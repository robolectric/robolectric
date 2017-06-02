package org.robolectric.shadows;

import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

@RunWith(TestRunners.MultiApiSelfTest.class)
@Config(manifest = "TestAndroidManifest25.xml")
public class ShadowNotificationBuilder25Test extends ShadowNotificationBuilderTest {
  // run 'em all again with android:targetSdkVersion=25 - behavior of NotificationBuilder
  // varies based on version specified in Manifest rather than runtime framework version.
}
