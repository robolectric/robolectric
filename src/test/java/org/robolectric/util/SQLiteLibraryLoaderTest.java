package org.robolectric.util;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import java.io.File;
import java.io.FileOutputStream;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class SQLiteLibraryLoaderTest {

  @Before
  public void deleteExtractedLibrary() {
    SQLiteLibraryLoader.getNativeLibraryPath().delete();
    SQLiteLibraryLoader.mustReload();
  }

  @Test
  public void shouldExtractNativeLibrary() {
    File extractedPath = SQLiteLibraryLoader.getNativeLibraryPath();
    assertThat(extractedPath).doesNotExist();
    SQLiteLibraryLoader.load();
    assertThat(extractedPath).exists();
  }

  @Test
  public void shouldNotRewriteExistingLibraryIfThereAreNoChanges() throws Exception{
    SQLiteLibraryLoader.load();
    File extractedPath = SQLiteLibraryLoader.getNativeLibraryPath();
    assertThat(extractedPath).exists();

    final long resetTime = 1234L;
    assertThat(extractedPath.setLastModified(resetTime)).describedAs("Cannot reset modification date").isTrue();
    // actual time may be truncated to seconds
    long time = extractedPath.lastModified();
    assertThat(time).isLessThanOrEqualTo(resetTime);

    SQLiteLibraryLoader.mustReload();
    SQLiteLibraryLoader.load();
    extractedPath = SQLiteLibraryLoader.getNativeLibraryPath();
    assertThat(extractedPath.lastModified()).isEqualTo(time);
  }

  @Test
  public void shouldRewriteExistingLibraryIfThereAreChanges() throws Exception {
    IOUtils.copy(IOUtils.toInputStream("changed"), new FileOutputStream(SQLiteLibraryLoader.getNativeLibraryPath()));
    long firstSize = SQLiteLibraryLoader.getNativeLibraryPath().length();

    SQLiteLibraryLoader.load();
    File extractedPath = SQLiteLibraryLoader.getNativeLibraryPath();
    assertThat(extractedPath).exists();
    assertThat(extractedPath.length()).isGreaterThan(firstSize);
  }

}
