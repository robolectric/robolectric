package org.robolectric;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.res.Fs;
import org.robolectric.res.ResourcePath;
import org.robolectric.test.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.pm.ApplicationInfo.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.robolectric.util.TestUtil.*;

@RunWith(TestRunners.WithDefaults.class)
public class AndroidManifestTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void shouldReadBroadcastReceivers() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestWithReceivers.xml");

    assertEquals(7, config.getReceiverCount());

    assertEquals("org.robolectric.AndroidManifestTest.ConfigTestReceiver", config.getReceiverClassName(0));
    assertEquals("org.robolectric.ACTION1", config.getReceiverIntentFilterActions(0).get(0));

    assertEquals("org.robolectric.AndroidManifestTest.ConfigTestReceiver", config.getReceiverClassName(1));
    assertEquals("org.robolectric.ACTION2", config.getReceiverIntentFilterActions(1).get(0));

    assertEquals("org.robolectric.tester.ConfigTestReceiver", config.getReceiverClassName(2));
    assertEquals("org.robolectric.ACTION_SUPERSET_PACKAGE", config.getReceiverIntentFilterActions(2).get(0));

    assertEquals("org.robolectric.ConfigTestReceiver", config.getReceiverClassName(3));
    assertEquals("org.robolectric.ACTION_SUBSET_PACKAGE", config.getReceiverIntentFilterActions(3).get(0));

    assertEquals("org.robolectric.DotConfigTestReceiver", config.getReceiverClassName(4));
    assertEquals("org.robolectric.ACTION_DOT_PACKAGE", config.getReceiverIntentFilterActions(4).get(0));

    assertEquals("org.robolectric.test.ConfigTestReceiver", config.getReceiverClassName(5));
    assertEquals("org.robolectric.ACTION_DOT_SUBPACKAGE", config.getReceiverIntentFilterActions(5).get(0));

    assertEquals("com.foo.Receiver", config.getReceiverClassName(6));
    assertEquals("org.robolectric.ACTION_DIFFERENT_PACKAGE", config.getReceiverIntentFilterActions(6).get(0));
  }

  @Test
  public void shouldReadTargetSdkVersionFromAndroidManifestOrDefaultToMin() throws Exception {
    assertEquals(42, newConfigWith("android:targetSdkVersion=\"42\" android:minSdkVersion=\"7\"").getTargetSdkVersion());
    assertEquals(7, newConfigWith("android:minSdkVersion=\"7\"").getTargetSdkVersion());
    assertEquals(1, newConfigWith("").getTargetSdkVersion());
  }

  @Test
  public void shouldReadMinSdkVersionFromAndroidManifestOrDefaultToOne() throws Exception {
    assertEquals(17, newConfigWith("android:minSdkVersion=\"17\"").getMinSdkVersion());
    assertEquals(1, newConfigWith("").getMinSdkVersion());
  }

  @Test
  public void shouldReadProcessFromAndroidManifest() throws Exception {
    assertEquals("robolectricprocess", newConfig("TestAndroidManifestWithProcess.xml").getProcessName());
  }

  @Test
  public void shouldReturnPackageNameWhenNoProcessIsSpecifiedInTheManifest() {
    assertEquals("org.robolectric", newConfig("TestAndroidManifestWithNoProcess.xml").getProcessName());
  }

  @Test public void shouldLoadAllResourcesForLibraries() {
    AndroidManifest appManifest = new AndroidManifest(resourceFile("TestAndroidManifest.xml"), resourceFile("res"));

    // This intentionally loads from the non standard resources/project.properties
    List<String> resourcePaths = stringify(appManifest.getIncludedResourcePaths());
    assertEquals(asList(
        joinPath(".", "src", "test", "resources", "res"),
        joinPath(".", "src", "test", "resources", "lib1", "res"),
        joinPath(".", "src", "test", "resources", "lib1", "..", "lib3", "res"),
        joinPath(".", "src", "test", "resources", "lib2", "res")),
        resourcePaths);
  }

  /////////////////////////////

  public AndroidManifest newConfigWith(String usesSdkAttrs) throws IOException {
    File f = temporaryFolder.newFile("whatever.xml",
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
            "          package=\"org.robolectric\">\n" +
            "    <uses-sdk " + usesSdkAttrs + "/>\n" +
            "</manifest>\n");
    return new AndroidManifest(Fs.newFile(f), null, null);
  }

  private List<String> stringify(List<ResourcePath> resourcePaths) {
    List<String> resourcePathBases = new ArrayList<String>();
    for (ResourcePath resourcePath : resourcePaths) {
      resourcePathBases.add(resourcePath.resourceBase.toString());
    }
    return resourcePathBases;
  }

  @Test
  public void shouldReadFlagsFromAndroidManifest() throws Exception {
    AndroidManifest config = newConfig("TestAndroidManifestWithFlags.xml");
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_ALLOW_BACKUP));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_ALLOW_CLEAR_USER_DATA));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_ALLOW_TASK_REPARENTING));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_DEBUGGABLE));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_HAS_CODE));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_KILL_AFTER_RESTORE));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_PERSISTENT));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_RESIZEABLE_FOR_SCREENS));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_RESTORE_ANY_VERSION));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_SUPPORTS_LARGE_SCREENS));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_SUPPORTS_NORMAL_SCREENS));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_SUPPORTS_SCREEN_DENSITIES));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_SUPPORTS_SMALL_SCREENS));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_TEST_ONLY));
    assertTrue(hasFlag(config.getApplicationFlags(), FLAG_VM_SAFE_MODE));
  }

  private boolean hasFlag(final int flags, final int flag) {
    return (flags & flag) != 0;
  }

  public static class ConfigTestReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
    }
  }
}
