package org.robolectric.res.android;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class CppApkAssetsTest {

  @Test
  public void forEachFile_returnsFalseIfInitializedTrivially() {
    boolean runningResult = new CppApkAssets().ForEachFile("a/robo", (string, type) -> {});
    assertThat(runningResult).isFalse();
  }
}
