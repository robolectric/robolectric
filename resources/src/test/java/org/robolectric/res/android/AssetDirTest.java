package org.robolectric.res.android;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class AssetDirTest {

  @Test
  public void getFileCount_returnsZeroIfInitializedTrivially() {
    assertThat(new AssetDir().getFileCount()).isEqualTo(0);
  }

  @Test
  public void getFileCount_returnsCorrectFileCount() {
    AssetDir.FileInfo fileInfo1 = new AssetDir.FileInfo(new String8("a/a.txt"));
    AssetDir.FileInfo fileInfo2 = new AssetDir.FileInfo(new String8("b/b.txt"));
    SortedVector<AssetDir.FileInfo> fileInfos = new SortedVector<>();
    fileInfos.add(fileInfo1);
    fileInfos.add(fileInfo2);
    AssetDir assetDir = new AssetDir();
    assetDir.setFileList(fileInfos);

    assertThat(assetDir.getFileCount()).isEqualTo(2);
  }
}
