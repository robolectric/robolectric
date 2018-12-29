package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.testing.TestContentProvider1;

@RunWith(AndroidJUnit4.class)
public class ShadowContentProviderClientTest {

  private static final String AUTHORITY = "org.robolectric";

  ContentProvider provider;
  ContentResolver contentResolver =
      (ApplicationProvider.getApplicationContext()).getContentResolver();

  @Before
  public void setUp() {
    provider = Robolectric.buildContentProvider(TestContentProvider1.class).create(AUTHORITY).get();
  }

  @Test
  public void acquireContentProviderClient_isStable() {
    ContentProviderClient client = contentResolver.acquireContentProviderClient(AUTHORITY);
    assertThat(shadowOf(client).isStable()).isTrue();
  }

  @Test
  public void acquireUnstableContentProviderClient_isUnstable() {
    ContentProviderClient client = contentResolver.acquireUnstableContentProviderClient(AUTHORITY);
    assertThat(shadowOf(client).isStable()).isFalse();
  }

  @Test
  public void release_shouldRelease() {
    ContentProviderClient client = contentResolver.acquireContentProviderClient(AUTHORITY);
    ShadowContentProviderClient shadow = shadowOf(client);
    assertThat(shadow.isReleased()).isFalse();
    client.release();
    assertThat(shadow.isReleased()).isTrue();
  }
}
