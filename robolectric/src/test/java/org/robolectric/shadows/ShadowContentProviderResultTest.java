package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.content.ContentProviderResult;
import android.net.Uri;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
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