package org.robolectric.android.controller;

import static com.google.common.truth.Truth.assertThat;

import android.app.Application;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.pm.PathPermission;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.testing.TestContentProvider1;

@RunWith(AndroidJUnit4.class)
public class ContentProviderControllerTest {
  private final ContentProviderController<TestContentProvider1> controller = Robolectric.buildContentProvider(TestContentProvider1.class);
  private ContentResolver contentResolver;

  @Before
  public void setUp() throws Exception {
    contentResolver = ApplicationProvider.getApplicationContext().getContentResolver();
  }

  @Test
  public void shouldSetBaseContext() throws Exception {
    TestContentProvider1 myContentProvider = controller.create().get();
    assertThat(myContentProvider.getContext())
        .isEqualTo(((Application) ApplicationProvider.getApplicationContext()).getBaseContext());
  }

  @Test
  public void shouldInitializeFromManifestProviderInfo() throws Exception {
    TestContentProvider1 myContentProvider = controller.create().get();
    assertThat(myContentProvider.getReadPermission()).isEqualTo("READ_PERMISSION");
    assertThat(myContentProvider.getWritePermission()).isEqualTo("WRITE_PERMISSION");

    assertThat(myContentProvider.getPathPermissions()).asList().hasSize(1);
    PathPermission pathPermission = myContentProvider.getPathPermissions()[0];
    assertThat(pathPermission.getPath()).isEqualTo("/path/*");
    assertThat(pathPermission.getType()).isEqualTo(PathPermission.PATTERN_SIMPLE_GLOB);
    assertThat(pathPermission.getReadPermission()).isEqualTo("PATH_READ_PERMISSION");
    assertThat(pathPermission.getWritePermission()).isEqualTo("PATH_WRITE_PERMISSION");
  }

  @Test
  public void shouldRegisterWithContentResolver() throws Exception {
    controller.create().get();

    ContentProviderClient client =
        contentResolver.acquireContentProviderClient(
            "org.robolectric.authority1");
    client.query(Uri.parse("something"), new String[]{"title"}, "*", new String[]{}, "created");
    assertThat(controller.get().transcript).containsExactly("onCreate", "query for something");
  }

  @Test
  public void whenNoProviderManifestEntryFound_shouldStillInitialize() throws Exception {
    TestContentProvider1 myContentProvider = Robolectric.buildContentProvider(NotInManifestContentProvider.class).create().get();
    assertThat(myContentProvider.getReadPermission()).isNull();
    assertThat(myContentProvider.getWritePermission()).isNull();
    assertThat(myContentProvider.getPathPermissions()).isNull();
  }

  @Test
  public void create_shouldCallOnCreate() throws Exception {
    TestContentProvider1 myContentProvider = controller.create().get();
    assertThat(myContentProvider.transcript).containsExactly("onCreate");
  }

  @Test
  public void shutdown_shouldCallShutdown() throws Exception {
    TestContentProvider1 myContentProvider = controller.shutdown().get();
    assertThat(myContentProvider.transcript).containsExactly("shutdown");
  }

  @Test
  public void withoutManifest_shouldRegisterWithContentResolver() throws Exception {
    ProviderInfo providerInfo = new ProviderInfo();
    providerInfo.authority = "some-authority";
    controller.create(providerInfo);

    ContentProviderClient client = contentResolver.acquireContentProviderClient(providerInfo.authority);
    client.query(Uri.parse("something"), new String[]{"title"}, "*", new String[]{}, "created");
    assertThat(controller.get().transcript).containsExactly("onCreate", "query for something");
  }

  @Test
  public void contentProviderShouldBeCreatedBeforeBeingRegistered() throws Exception {
    XContentProvider xContentProvider = Robolectric.setupContentProvider(XContentProvider.class, "x-authority");
    assertThat(xContentProvider.transcript).containsExactly("x-authority not registered yet");
    ContentProviderClient contentProviderClient = contentResolver.acquireContentProviderClient("x-authority");
    assertThat(contentProviderClient.getLocalContentProvider()).isSameAs(xContentProvider);
  }

  ////////////////////

  static class XContentProvider extends TestContentProvider1 {
    @Override
    public boolean onCreate() {
      ContentProviderClient contentProviderClient =
          ApplicationProvider.getApplicationContext()
              .getContentResolver()
              .acquireContentProviderClient("x-authority");
      transcript.add(contentProviderClient == null ? "x-authority not registered yet" : "x-authority is registered");
      return false;
    }
  }

  static class NotInManifestContentProvider extends TestContentProvider1 {}
}