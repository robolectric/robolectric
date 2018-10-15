package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowContentProviderClientTest {

  private static final String AUTHORITY = "org.robolectric";
  private final Uri URI = Uri.parse("content://" + AUTHORITY);
  private final ContentValues VALUES = new ContentValues();
  private static final String[] PROJECTION = null;
  private static final String SELECTION = "1=?";
  private static final String[] SELECTION_ARGS = {"1"};
  private static final String SORT_ORDER = "DESC";
  private static final String MIME_TYPE = "application/octet-stream";

  @Mock ContentProvider provider;
  ContentResolver contentResolver =
      ((Application) ApplicationProvider.getApplicationContext()).getContentResolver();

  @Before
  public void setUp() {
    initMocks(this);
    ShadowContentResolver.registerProviderInternal(AUTHORITY, provider);
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

  @Test(expected = IllegalStateException.class)
  public void release_shouldFailWhenCalledTwice() {
    ContentProviderClient client = contentResolver.acquireContentProviderClient(AUTHORITY);
    client.release();
    client.release();
    fail("client.release() was called twice and did not throw");
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void shouldDelegateToContentProvider() throws Exception {
    ContentProviderClient client = contentResolver.acquireContentProviderClient(AUTHORITY);

    client.query(URI, PROJECTION, SELECTION, SELECTION_ARGS, SORT_ORDER);
    verify(provider).query(URI, PROJECTION, SELECTION, SELECTION_ARGS, SORT_ORDER);

    CancellationSignal signal = new CancellationSignal();
    client.query(URI, PROJECTION, SELECTION, SELECTION_ARGS, SORT_ORDER, signal);
    verify(provider).query(URI, PROJECTION, SELECTION, SELECTION_ARGS, SORT_ORDER, signal);

    client.insert(URI, VALUES);
    verify(provider).insert(URI, VALUES);

    client.update(URI, VALUES, SELECTION, SELECTION_ARGS);
    verify(provider).update(URI, VALUES, SELECTION, SELECTION_ARGS);

    client.delete(URI, SELECTION, SELECTION_ARGS);
    verify(provider).delete(URI, SELECTION, SELECTION_ARGS);

    client.getType(URI);
    verify(provider).getType(URI);

    client.openFile(URI, "rw");
    verify(provider).openFile(URI, "rw");

    client.openAssetFile(URI, "r");
    verify(provider).openAssetFile(URI, "r");

    final Bundle opts = new Bundle();
    client.openTypedAssetFileDescriptor(URI, MIME_TYPE, opts);
    verify(provider).openTypedAssetFile(URI, MIME_TYPE, opts);

    client.getStreamTypes(URI, MIME_TYPE);
    verify(provider).getStreamTypes(URI, MIME_TYPE);

    final ArrayList<ContentProviderOperation> ops = new ArrayList<>();
    client.applyBatch(ops);
    verify(provider).applyBatch(ops);

    final ContentValues[] values = {VALUES};
    client.bulkInsert(URI, values);
    verify(provider).bulkInsert(URI, values);

    final String method = "method";
    final String arg = "arg";
    final Bundle extras = new Bundle();
    client.call(method, arg, extras);
    verify(provider).call(method, arg, extras);
  }
}
