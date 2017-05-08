package org.robolectric.shadows;


import libcore.io.IoUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.test.TemporaryFolder;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiSelfTest.class)
public class ShadowIoUtilsTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void ioUtils() throws Exception {

    File fontFile = temporaryFolder.newFile("test_file.txt", "some contents");

    String contents = IoUtils.readFileAsString(fontFile.getAbsolutePath());
    assertThat(contents).isEqualTo("some contents");
  }
}
