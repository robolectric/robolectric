package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.net.Uri;
import androidx.core.content.FileProvider;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowFileProvider} */
@RunWith(AndroidJUnit4.class)
@Config(shadows = {ShadowFileProvider.class})
public class ShadowFileProviderTest {

  @Test
  public void getUriForFile() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    File cacheDir = context.getCacheDir();
    File testFile = new File(cacheDir, "test");
    String authority = "foo";
    cacheDir.mkdirs();
    testFile.createNewFile();

    Uri uri = FileProvider.getUriForFile(context, authority, testFile);
    assertThat(uri.getScheme()).isEqualTo("content");
    assertThat(uri.getAuthority()).isEqualTo(authority);
    assertThat(uri.getPath()).isEqualTo(testFile.getAbsolutePath());
  }
}
