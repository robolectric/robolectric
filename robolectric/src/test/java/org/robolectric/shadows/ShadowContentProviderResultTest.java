package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.ContentProviderResult;
import android.net.Uri;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowContentProviderResultTest {
  @Test
  public void count() {
    ContentProviderResult result = new ContentProviderResult(5);
    assertThat(result.count).isEqualTo(5);
  }

  @Test
  public void uri() {
    Uri uri = Uri.parse("content://org.robolectric");
    ContentProviderResult result = new ContentProviderResult(uri);
    assertThat(result.uri).isEqualTo(uri);
  }
}