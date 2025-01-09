package org.robolectric.internal;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.Config;

@RunWith(JUnit4.class)
public class MavenManifestFactoryTest {

  private Config.Builder configBuilder;
  private MyMavenManifestFactory myMavenManifestFactory;

  @Before
  public void setUp() throws Exception {
    configBuilder = Config.Builder.defaults().setManifest("DifferentManifest.xml");
    myMavenManifestFactory = new MyMavenManifestFactory();
  }

  @Test
  public void identify() {
    ManifestIdentifier manifestIdentifier = myMavenManifestFactory.identify(configBuilder.build());
    assertThat(manifestIdentifier.getManifestFile())
        .isEqualTo(Paths.get("_fakefs_path").resolve("to").resolve("DifferentManifest.xml"));
    assertThat(manifestIdentifier.getResDir()).isEqualTo(Paths.get("_fakefs_path/to/res"));
  }

  @Test
  public void withDotSlashManifest_identify() {
    configBuilder.setManifest("./DifferentManifest.xml");

    ManifestIdentifier manifestIdentifier = myMavenManifestFactory.identify(configBuilder.build());
    assertThat(manifestIdentifier.getManifestFile().normalize())
        .isEqualTo(Paths.get("_fakefs_path/to/DifferentManifest.xml"));
    assertThat(manifestIdentifier.getResDir().normalize())
        .isEqualTo(Paths.get("_fakefs_path/to/res"));
  }

  @Test
  public void withDotDotSlashManifest_identify() {
    configBuilder.setManifest("../DifferentManifest.xml");

    ManifestIdentifier manifestIdentifier = myMavenManifestFactory.identify(configBuilder.build());
    assertThat(manifestIdentifier.getManifestFile())
        .isEqualTo(Paths.get("_fakefs_path/to/../DifferentManifest.xml"));
    assertThat(manifestIdentifier.getResDir()).isEqualTo(Paths.get("_fakefs_path/to/../res"));
  }

  private static class MyMavenManifestFactory extends MavenManifestFactory {
    @Override
    Path getBaseDir() {
      return Paths.get("_fakefs_path").resolve("to");
    }
  }
}
