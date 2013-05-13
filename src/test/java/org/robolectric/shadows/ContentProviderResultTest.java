package org.robolectric.shadows;

import android.content.ContentProviderResult;
import android.net.Uri;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ContentProviderResultTest {
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