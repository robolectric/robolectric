package org.robolectric;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.os.Build.VERSION_CODES.N;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;
import static org.robolectric.res.AttributeResource.ANDROID_NS;
import static org.robolectric.res.AttributeResource.RES_AUTO_NS_URI;

import android.app.Activity;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.objectweb.asm.ClassReader;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.LooperMode;
import org.robolectric.internal.bytecode.ShadowedObject;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowView;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
public class RobolectricTest {

  private final Application context = ApplicationProvider.getApplicationContext();

  @Test
  public void clickOn_shouldThrowIfViewIsDisabled() {
    View view = new View(context);
    view.setEnabled(false);
    assertThrows(RuntimeException.class, () -> ShadowView.clickOn(view));
  }

  @Test
  @LooperMode(LEGACY)
  @Config(maxSdk = BAKLAVA)
  public void shouldResetBackgroundSchedulerBeforeTests() {
    assertThat(Robolectric.getBackgroundThreadScheduler().isPaused()).isFalse();
    Robolectric.getBackgroundThreadScheduler().pause();
  }

  @Test
  @LooperMode(LEGACY)
  @Config(maxSdk = BAKLAVA)
  public void shouldResetBackgroundSchedulerAfterTests() {
    assertThat(Robolectric.getBackgroundThreadScheduler().isPaused()).isFalse();
    Robolectric.getBackgroundThreadScheduler().pause();
  }

  @Test
  public void idleMainLooper_executesScheduledTasks() {
    final boolean[] wasRun = new boolean[] {false};
    new Handler().postDelayed(() -> wasRun[0] = true, 2000);

    assertFalse(wasRun[0]);
    ShadowLooper.idleMainLooper(1999, TimeUnit.MILLISECONDS);
    assertFalse(wasRun[0]);
    ShadowLooper.idleMainLooper(1, TimeUnit.MILLISECONDS);
    assertTrue(wasRun[0]);
  }

  @Test
  public void clickOn_shouldCallClickListener() {
    View view = new View(context);
    shadowOf(view).setMyParent(ReflectionHelpers.createNullProxy(ViewParent.class));
    OnClickListener testOnClickListener = mock(OnClickListener.class);
    view.setOnClickListener(testOnClickListener);
    ShadowView.clickOn(view);

    verify(testOnClickListener).onClick(view);
  }

  @Test
  public void checkActivities_shouldSetValueOnShadowApplication() {
    shadowOf(RuntimeEnvironment.getApplication()).checkActivities(true);
    assertThrows(
        ActivityNotFoundException.class,
        () ->
            context.startActivity(
                new Intent("i.dont.exist.activity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
  }

  @Test
  @Config(sdk = Config.NEWEST_SDK)
  public void setupActivity_returnsAVisibleActivity() {
    LifeCycleActivity activity = Robolectric.setupActivity(LifeCycleActivity.class);

    assertThat(activity.isCreated()).isTrue();
    assertThat(activity.isStarted()).isTrue();
    assertThat(activity.isResumed()).isTrue();
    assertThat(activity.isVisible()).isTrue();
  }

  @Test
  public void getAttributeSetFromXml_androidNs() {
    AttributeSet roboAttributeSet = Robolectric.getAttributeSetFromXml(R.xml.attr_set);

    assertThat(roboAttributeSet.getAttributeCount()).isEqualTo(2);
    assertThat(roboAttributeSet.getAttributeResourceValue(ANDROID_NS, "text", 0))
        .isEqualTo(android.R.string.ok);
  }

  @Test
  public void getAttributeSetFromXml_appNs() {
    AttributeSet roboAttributeSet = Robolectric.getAttributeSetFromXml(R.xml.attr_set);

    assertThat(roboAttributeSet.getAttributeValue(RES_AUTO_NS_URI, "title")).isEqualTo("my title");
  }

  @Test
  public void getAttributeSetFromXml_invalidXmlResId() {
    assertThrows(Resources.NotFoundException.class, () -> Robolectric.getAttributeSetFromXml(1234));
  }

  @Test
  public void getAttributeSetFromXml_emptyXml() {
    AttributeSet emptySet = Robolectric.getAttributeSetFromXml(R.xml.empty);
    assertThat(emptySet).isNotNull();
    assertThat(emptySet.getAttributeCount()).isEqualTo(0);
  }

  /* This captures a bug in the Android Studio Coverage tool, but is also a test of a
   * Robolectric class loader feature. The Android Studio Coverage tool loads Android class files
   * as resources to instrument them. We should return the android-all classes, not stubs jar
   * classes.
   */
  @Test
  public void getResource_colorStateList_shouldBeInstrumented() throws Exception {
    String className = "android.content.res.ColorStateList";
    String resourceName = className.replace('.', '/') + ".class";

    ClassLoader loader = android.content.res.ColorStateList.class.getClassLoader();
    URL resource = loader.getResource(resourceName);
    assertThat(resource).isNotNull();

    byte[] bytes;
    try (InputStream is = resource.openStream()) {
      bytes = is.readAllBytes();
    }

    ClassReader classReader = new ClassReader(bytes);
    assertThat(asList(classReader.getInterfaces()))
        .contains(ShadowedObject.class.getName().replace('.', '/'));

    if (RuntimeEnvironment.getApiLevel() >= N) {
      assertThat(classReader.getSuperName()).isEqualTo("android/content/res/ComplexColor");
    }
  }

  @Implements(View.class)
  public static class TestShadowView {
    @Implementation
    protected Context getContext() {
      return null;
    }
  }

  private static class LifeCycleActivity extends Activity {
    private boolean created;
    private boolean started;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      created = true;
    }

    @Override
    protected void onStart() {
      super.onStart();
      started = true;
    }

    public boolean isStarted() {
      return started;
    }

    public boolean isCreated() {
      return created;
    }

    public boolean isVisible() {
      return getWindow().getDecorView().getWindowToken() != null;
    }
  }
}
