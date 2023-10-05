package org.robolectric;

import static com.google.common.truth.Truth.assertThat;

import android.app.Service;
import android.content.Intent;
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
}
