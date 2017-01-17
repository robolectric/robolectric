package org.robolectric.util;

import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PathPermission;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 23)
public class ContentProviderControllerTest {
  private final ContentProviderController<MyContentProvider> controller = Robolectric.buildContentProvider(MyContentProvider.class);

  @Test
  public void shouldSetBaseContext() throws Exception {
    MyContentProvider myContentProvider = controller.create().get();
    assertThat(myContentProvider.getContext()).isEqualTo(RuntimeEnvironment.application.getBaseContext());
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithContentProviders.xml")
  public void shouldInitializeFromManifestProviderInfo() throws Exception {
    MyContentProvider myContentProvider = controller.create().get();
    assertThat(myContentProvider.getReadPermission()).isEqualTo("READ_PERMISSION");
    assertThat(myContentProvider.getWritePermission()).isEqualTo("WRITE_PERMISSION");

    assertThat(myContentProvider.getPathPermissions()).hasSize(1);
    PathPermission pathPermission = myContentProvider.getPathPermissions()[0];
    assertThat(pathPermission.getPath()).isEqualTo("/path/*");
    assertThat(pathPermission.getType()).isEqualTo(PathPermission.PATTERN_SIMPLE_GLOB);
    assertThat(pathPermission.getReadPermission()).isEqualTo("PATH_READ_PERMISSION");
    assertThat(pathPermission.getWritePermission()).isEqualTo("PATH_WRITE_PERMISSION");
  }

  @Test
  @Config(manifest = "src/test/resources/TestAndroidManifestWithContentProviders.xml")
  public void shouldRegisterWithContentResolver() throws Exception {
    controller.create().get();

    ContentResolver contentResolver = RuntimeEnvironment.application.getContentResolver();
    ContentProviderClient client = contentResolver.acquireContentProviderClient("org.robolectric.authority2");
    client.query(Uri.parse("something"), new String[]{"title"}, "*", new String[]{}, "created");
    assertThat(controller.get().transcript).containsExactly("onCreate", "query for something");
  }

  @Test
  public void whenNoProviderManifestEntryFound_shouldStillInitialize() throws Exception {
    MyContentProvider myContentProvider = controller.create().get();
    assertThat(myContentProvider.getReadPermission()).isNull();
    assertThat(myContentProvider.getWritePermission()).isNull();
    assertThat(myContentProvider.getPathPermissions()).isNull();
  }

  @Test
  public void create_shouldCallOnCreate() throws Exception {
    MyContentProvider myContentProvider = controller.create().get();
    assertThat(myContentProvider.transcript).containsExactly("onCreate");
  }

  @Test
  public void shutdown_shouldCallShutdown() throws Exception {
    MyContentProvider myContentProvider = controller.shutdown().get();
    assertThat(myContentProvider.transcript).containsExactly("shutdown");
  }

  @Test
  public void withoutManifest_shouldRegisterWithContentResolver() throws Exception {
    ProviderInfo providerInfo = new ProviderInfo();
    providerInfo.authority = "some-authority";
    controller.create(providerInfo);

    ContentResolver contentResolver = RuntimeEnvironment.application.getContentResolver();
    ContentProviderClient client = contentResolver.acquireContentProviderClient(providerInfo.authority);
    client.query(Uri.parse("something"), new String[]{"title"}, "*", new String[]{}, "created");
    assertThat(controller.get().transcript).containsExactly("onCreate", "query for something");
  }

  public static class MyContentProvider extends ContentProvider {
    private final List<String> transcript = new ArrayList<>();

    @Override
    public boolean onCreate() {
      transcript.add("onCreate");
      return false;
    }

    @Override
    public void shutdown() {
      super.shutdown();
      transcript.add("shutdown");
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
      transcript.add("query for " + uri);
      return null;
    }

    @Override
    public String getType(Uri uri) {
      return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
      return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
      return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
      return 0;
    }
  }
}