package org.robolectric.util;

import java.io.File;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.manifest.AndroidManifest;
import org.junit.runners.model.InitializationError;

public class TestRunnerWithManifest extends RobolectricTestRunner {
  public TestRunnerWithManifest(Class<?> testClass) throws InitializationError {
    super(testClass);
  }

  @Override
  protected AndroidManifest createAppManifest(FsFile manifestFile, FsFile resDir, FsFile assetsDir, String packageName) {
    return new AndroidManifest(resourceFile("AndroidManifest.xml"), resourceFile("res"), resourceFile("assets"));
  }

  private static FsFile resourceFile(String... pathParts) {
    return Fs.newFile(resourcesBaseDirFile()).join(pathParts);
  }

  private static File resourcesBaseDirFile() {
    File testDir = Util.file("src", "test", "resources");
    return hasTestManifest(testDir) ? testDir : Util.file("shadows-appcompat-v7", "src", "test", "resources");
  }

  private static boolean hasTestManifest(File testDir) {
    return new File(testDir, "AndroidManifest.xml").isFile();
  }
}
