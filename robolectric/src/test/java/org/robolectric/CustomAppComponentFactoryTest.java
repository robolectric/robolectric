package org.robolectric;

import static com.google.common.truth.Truth.assertThat;

import android.app.Service;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.CustomConstructorServices.CustomConstructorIntentService;
import org.robolectric.CustomConstructorServices.CustomConstructorJobService;
import org.robolectric.CustomConstructorServices.CustomConstructorService;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(manifest = "TestAndroidManifestWithAppComponentFactory.xml", minSdk = Build.VERSION_CODES.P)
public class CustomAppComponentFactoryTest {

  @Test
  public void instantiateServiceWithCustomConstructor() {
    CustomConstructorService service = Robolectric.setupService(CustomConstructorService.class);
    assertThat(service.getIntValue()).isEqualTo(100);
  }

  @Test
  public void instantiateIntentServiceWithCustomConstructor() {
    CustomConstructorIntentService service =
        Robolectric.setupService(CustomConstructorIntentService.class);
    assertThat(service.getIntValue()).isEqualTo(100);
  }

  @Test
  public void instantiateJobServiceWithCustomConstructor() {
    CustomConstructorJobService service =
        Robolectric.setupService(CustomConstructorJobService.class);
    assertThat(service.getIntValue()).isEqualTo(100);
  }

  @Test
  public void instantiatePrivateServiceClass() {
    PrivateService service = Robolectric.setupService(PrivateService.class);
    assertThat(service.isCreated).isTrue();
  }

  @Test
  public void instantiateContentProviderWithCustomConstructor() {
    CustomConstructorContentProvider provider =
        Robolectric.setupContentProvider(CustomConstructorContentProvider.class);
    assertThat(provider.getIntValue()).isEqualTo(100);
  }

  @Test
  public void instantiateContentProviderWithCustomConstructorAndAuthority() {
    CustomConstructorContentProvider provider =
        Robolectric.setupContentProvider(
            CustomConstructorContentProvider.class, "org.robolectric.authority");
    assertThat(provider.getIntValue()).isEqualTo(100);
  }

  @Test
  public void instantiatePrivateContentProviderClass() {
    PrivateContentProvider provider =
        Robolectric.setupContentProvider(PrivateContentProvider.class);
    assertThat(provider.isCreated).isTrue();
  }

  private static class PrivateService extends Service {
    public boolean isCreated = false;

    @Override
    public void onCreate() {
      super.onCreate();
      isCreated = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
      return null;
    }
  }

  private static class PrivateContentProvider extends ContentProvider {
    public boolean isCreated = false;

    @Override
    public boolean onCreate() {
      isCreated = true;
      return false;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
      return null;
    }

    @Override
    public String getType(Uri uri) {
      return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
      return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
      return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
      return 0;
    }
  }
}
