package org.robolectric.res;

import org.junit.Test;
import org.robolectric.R;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

public class PackageResourceLoaderTest {
  @Test
  public void testBadPathShouldThrow() {
    try {
      final FsFile resDir = new FileFsFile(new File(File.separator + "bad path"));
      final ResourcePath resourcePath = new ResourcePath(R.class, R.class.getPackage().getName(), resDir, resDir);
      new PackageResourceLoader(resourcePath);
      fail("Constructor didn't throw exception!");
    } catch (IllegalArgumentException ex) {
      assertThat(ex.getMessage()).isEqualTo("Resource path must end in \"" + File.separator + "res\"");
    }
  }

  @Test
  public void testBadAndroidPathShouldThrow() {
    try {
      final FsFile resDir = new FileFsFile(new File("/bad path"));
      final ResourcePath resourcePath = new ResourcePath(android.R.class, "android", resDir, resDir);
      new PackageResourceLoader(resourcePath);
      fail("Constructor didn't throw exception!");
    } catch (IllegalArgumentException ex) {
      assertThat(ex.getMessage()).isEqualTo("Resource path must end in \"/res\"");
    }
  }
}
