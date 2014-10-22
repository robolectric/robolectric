package org.robolectric.shadows;

import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ContentProviderClientTest {

  static final String AUTHORITY = "org.robolectric";
  static final Uri URI = Uri.parse("content://" + AUTHORITY);
  static final ContentValues VALUES = new ContentValues();
  static final String[] PROJECTION = null;
  static final String SELECTION = "1=?";
  static final String[] SELECTION_ARGS = {"1"};
  static final String SORT_ORDER = "DESC";
  static final String MIME_TYPE = "application/octet-stream";

  @Mock ContentProvider provider;
  ContentResolver contentResolver = Robolectric.application.getContentResolver();

  @Before
  public void setUp() {
    initMocks(this);
    ShadowContentResolver.registerProvider(AUTHORITY, provider);
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

    final ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
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
