package org.robolectric.internal;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.Config;
import org.robolectric.res.FileFsFile;
import org.robolectric.res.FsFile;

@RunWith(JUnit4.class)
public class MavenManifestFactoryTest {

  private Config.Builder configBuilder;
  private MyMavenManifestFactory myMavenManifestFactory;

  @Before
  public void setUp() throws Exception {
    configBuilder = Config.Builder.defaults().setManifest("DifferentManifest.xml");
    myMavenManifestFactory = new MyMavenManifestFactory();
  }

  @Test public void identify() throws Exception {
    ManifestIdentifier manifestIdentifier = myMavenManifestFactory.identify(configBuilder.build());
    assertThat(manifestIdentifier.getManifestFile())
        .isEqualTo(FileFsFile.from(":fakefs:path/to/DifferentManifest.xml"));
    assertThat(manifestIdentifier.getResDir())
        .isEqualTo(FileFsFile.from(":fakefs:path/to/res"));
  }

  @Test public void withDotSlashManifest_identify() throws Exception {
    configBuilder.setManifest("./DifferentManifest.xml");

    ManifestIdentifier manifestIdentifier = myMavenManifestFactory.identify(configBuilder.build());
    assertThat(manifestIdentifier.getManifestFile())
        .isEqualTo(FileFsFile.from(":fakefs:path/to/DifferentManifest.xml"));
    assertThat(manifestIdentifier.getResDir())
        .isEqualTo(FileFsFile.from(":fakefs:path/to/res"));
  }

  @Test public void withDotDotSlashManifest_identify() throws Exception {
    configBuilder.setManifest("../DifferentManifest.xml");

    ManifestIdentifier manifestIdentifier = myMavenManifestFactory.identify(configBuilder.build());
    assertThat(manifestIdentifier.getManifestFile())
        .isEqualTo(FileFsFile.from(":fakefs:path/to/../DifferentManifest.xml"));
    assertThat(manifestIdentifier.getResDir())
        .isEqualTo(FileFsFile.from(":fakefs:path/to/../res"));
  }

  private static class MyMavenManifestFactory extends MavenManifestFactory {
    @Override
    FsFile getBaseDir() {
      return FileFsFile.from(":fakefs:path/to");
    }
  }
}