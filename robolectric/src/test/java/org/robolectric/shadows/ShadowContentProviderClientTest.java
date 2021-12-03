package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;

import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ContentProviderController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.testing.TestContentProvider1;

@RunWith(AndroidJUnit4.class)
public class ShadowContentProviderClientTest {

  private static final String AUTHORITY = "org.robolectric";

  private final ContentProviderController<TestContentProvider1> controller =
      Robolectric.buildContentProvider(TestContentProvider1.class);

  ContentProvider provider = controller.create().get();

  ContentResolver contentResolver =
      ApplicationProvider.getApplicationContext().getContentResolver();

  ContentProviderClient client;

  @Before
  public void setUp() {
    ShadowContentResolver.registerProviderInternal(AUTHORITY, provider);
  }

  @After
  public void tearDown() {
    if (client != null) {
      if (RuntimeEnvironment.getApiLevel() > M) {
        client.close();
      } else {
        client.release();
      }
    }
  }

  @Test
  public void acquireContentProviderClient_isStable() {
    client = contentResolver.acquireContentProviderClient(AUTHORITY);
    assertThat(shadowOf(client).isStable()).isTrue();
  }

  @Test
  public void acquireUnstableContentProviderClient_isUnstable() {
    client = contentResolver.acquireUnstableContentProviderClient(AUTHORITY);
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

  @Test
  @Config(maxSdk = M)
  public void release_shouldFailWhenCalledTwice() {
    ContentProviderClient client = contentResolver.acquireContentProviderClient(AUTHORITY);
    client.release();
    assertThrows(IllegalStateException.class, () -> client.release());
  }
}
