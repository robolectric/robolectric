package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.pm.SharedLibraryInfo;
import android.os.Build.VERSION_CODES;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Tests for {@link SharedLibraryInfoBuilder}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.O)
public final class SharedLibraryInfoBuilderTest {

  @Test
  @Config(maxSdk = VERSION_CODES.P)
  public void build_beforeVersionQ() {

    SharedLibraryInfo sharedLibraryInfo =
        SharedLibraryInfoBuilder.newBuilder()
            .setName("trichromelibrary")
            .setVersion(0)
            .setType(SharedLibraryInfo.TYPE_STATIC)
            .build();

    assertThat(sharedLibraryInfo.getType()).isEqualTo(SharedLibraryInfo.TYPE_STATIC);
    assertThat(sharedLibraryInfo.getName()).isEqualTo("trichromelibrary");
    assertThat(sharedLibraryInfo.getVersion()).isEqualTo(0);
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q)
  public void build_versionQ() {

    SharedLibraryInfo sharedLibraryInfo =
        SharedLibraryInfoBuilder.newBuilder()
            .setName("com.google.android.trichromelibrary_535912833")
            .setVersion(535912833)
            .setType(SharedLibraryInfo.TYPE_STATIC)
            .build();

    assertThat(sharedLibraryInfo.getType()).isEqualTo(SharedLibraryInfo.TYPE_STATIC);
    assertThat(sharedLibraryInfo.getName())
        .isEqualTo("com.google.android.trichromelibrary_535912833");
    assertThat(sharedLibraryInfo.getVersion()).isEqualTo(535912833);
  }
}
