package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for ShadowMediaScannerConnection */
@RunWith(AndroidJUnit4.class)
public class ShadowMediaScannerConnectionTest {
  private static final String[] paths = {"a", "b"};
  private static final String[] mimeTypes = {"c", "d"};

  @Test
  public void scanFile_validParameters_shouldContainsSamePaths() {
    ShadowMediaScannerConnection.scanFile(null, paths, mimeTypes, null);

    assertThat(ShadowMediaScannerConnection.getSavedPaths()).containsExactlyElementsIn(paths);
    assertThat(ShadowMediaScannerConnection.getSavedMimeTypes())
        .containsExactlyElementsIn(mimeTypes);
  }

  @Test
  public void scanFile_nullParameters_shouldContainsSamePaths() {
    int pathsSize = ShadowMediaScannerConnection.getSavedPaths().size();
    int mimeTypesSize = ShadowMediaScannerConnection.getSavedMimeTypes().size();

    ShadowMediaScannerConnection.scanFile(null, null, null, null);

    assertThat(ShadowMediaScannerConnection.getSavedPaths()).hasSize(pathsSize);
    assertThat(ShadowMediaScannerConnection.getSavedMimeTypes()).hasSize(mimeTypesSize);
  }
}
