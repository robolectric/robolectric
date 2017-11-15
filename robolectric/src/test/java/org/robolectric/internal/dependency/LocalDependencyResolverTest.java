package org.robolectric.internal.dependency;

import android.os.Build;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(JUnit4.class)
public class LocalDependencyResolverTest {

  private Properties depsProp;
  private LocalDependencyResolver localDependencyResolver;
  private File baseDir;

  @Before
  public void setUp() {
    depsProp = new Properties();
    baseDir = new File("/tmp");
    localDependencyResolver = new LocalDependencyResolver(new DependencyProperties(depsProp));
  }

  @Test
  public void apiDoesNotExist() {
    try {
      localDependencyResolver.getLocalArtifactUrl(99);
      fail();
    } catch (NullPointerException e) {
      // expected
    }
  }

  @Test
  public void getArtifact() throws IOException {
    File localFile = File.createTempFile( "artifact", ".jar", baseDir);
    depsProp.setProperty(Integer.toString(JELLY_BEAN_MR2), localFile.getAbsolutePath());
    URL url = localDependencyResolver.getLocalArtifactUrl(JELLY_BEAN_MR2);
    assertThat(url.getFile()).isEqualTo(localFile.getAbsolutePath());
  }

  @Test
  public void fileDoesNotExist() throws IOException {
    depsProp.setProperty(Integer.toString(JELLY_BEAN_MR2), "does_not_exist");
    try {
      localDependencyResolver.getLocalArtifactUrl(JELLY_BEAN_MR2);
      fail();
    } catch (RuntimeException e) {
      // expected
    }
  }

  @Test
  public void fileNotReadable() throws IOException {
    File localFile = File.createTempFile( "artifact", ".jar", baseDir);
    localFile.setReadable(false);
    depsProp.setProperty(Integer.toString(JELLY_BEAN_MR2), localFile.getName());
    try {
      localDependencyResolver.getLocalArtifactUrl(JELLY_BEAN_MR2);
      fail();
    } catch (RuntimeException e) {
      // expected
    }
  }
}
