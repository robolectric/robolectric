package org.robolectric.util;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ManifestFactory;
import org.robolectric.internal.ManifestIdentifier;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;

public class TestRunnerWithManifest extends RobolectricTestRunner {
  public TestRunnerWithManifest(Class<?> testClass) throws InitializationError {
    super(testClass);
  }

  private static FsFile resourceFile(String... pathParts) {
    return Fs.newFile(resourcesBaseDirFile()).join(pathParts);
  }

  private static File resourcesBaseDirFile() {
    // Try to locate the manifest file as a classpath resource.
    final String resourceName = "/src/test/resources/AndroidManifest.xml";
    final URL resourceUrl = TestRunnerWithManifest.class.getResource(resourceName);
    if (resourceUrl != null && "file".equals(resourceUrl.getProtocol())) {
      // Construct a path to the manifest file relative to the current working directory.
      final URI workingDirectory = URI.create(System.getProperty("user.dir"));
      final URI absolutePath = URI.create(resourceUrl.getPath());
      final URI relativePath = workingDirectory.relativize(absolutePath);
      return new File(relativePath.toString()).getParentFile();
    }

    // Return a path relative to the current working directory.
    return Util.file("src", "test", "resources");
  }


  @Override
  protected ManifestFactory getManifestFactory(Config config) {
    return c -> new ManifestIdentifier(
        "org.robolectric",
        resourceFile("AndroidManifest.xml"),
        resourceFile("res"),
        resourceFile("assets"),
        Collections.emptyList()
    );
  }
}
