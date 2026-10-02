package org.robolectric.integrationtests.androidx;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Application;
import android.app.Presentation;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.hardware.display.VirtualDisplay;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDisplayManager;

/** Shows how to test an app across more than one display. */
@RunWith(AndroidJUnit4.class)
@Config(qualifiers = "w411dp-h891dp-port-mdpi")
public class MultiDisplayTest {
  // Display.TYPE_EXTERNAL, which is hidden.
  private static final int TYPE_EXTERNAL = 2;

  private final Application application = ApplicationProvider.getApplicationContext();
  private final DisplayManager displayManager = application.getSystemService(DisplayManager.class);

  @Test
  public void externalDisplay_connectChangeDisconnect_notifiesListeners() {
    List<String> events = recordDisplayEvents();

    int displayId = ShadowDisplayManager.addDisplay("w1920dp-h1080dp-land", "External display");
    ShadowDisplayManager.changeDisplay(displayId, "w2560dp-h1440dp");
    ShadowDisplayManager.removeDisplay(displayId);

    assertThat(events)
        .containsExactly("added " + displayId, "changed " + displayId, "removed " + displayId)
        .inOrder();
  }

  @Test
  public void externalDisplay_hasItsOwnSizeAndDensity() {
    Display display = addExternalDisplay("w960dp-h540dp-land-xhdpi");

    DisplayMetrics metrics = new DisplayMetrics();
    display.getRealMetrics(metrics);

    assertThat(displayIds()).contains(display.getDisplayId());
    assertThat(metrics.widthPixels).isEqualTo(1920);
    assertThat(metrics.heightPixels).isEqualTo(1080);
    assertThat(metrics.densityDpi).isEqualTo(DisplayMetrics.DENSITY_XHIGH);
  }

  @Test
  public void externalDisplay_isFoundAsAPresentationDisplay() {
    Display display = addExternalDisplay("w1920dp-h1080dp-land-mdpi");

    Display[] presentationDisplays =
        displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);

    assertThat(presentationDisplays).hasLength(1);
    assertThat(presentationDisplays[0].getDisplayId()).isEqualTo(display.getDisplayId());
  }

  @Test
  public void displayContext_resolvesResourcesForThatDisplay() {
    Display display = addExternalDisplay("w960dp-h540dp-land-xhdpi");

    Context displayContext = application.createDisplayContext(display);

    Configuration configuration = displayContext.getResources().getConfiguration();
    assertThat(configuration.screenWidthDp).isEqualTo(960);
    assertThat(configuration.orientation).isEqualTo(Configuration.ORIENTATION_LANDSCAPE);
    assertThat(configuration.densityDpi).isEqualTo(DisplayMetrics.DENSITY_XHIGH);
    assertThat(application.getResources().getConfiguration().screenWidthDp).isEqualTo(411);
  }

  @Test
  public void virtualDisplay_createResizeRelease() {
    List<String> events = recordDisplayEvents();

    VirtualDisplay virtualDisplay =
        displayManager.createVirtualDisplay(
            "Virtual display", 800, 600, DisplayMetrics.DENSITY_MEDIUM, null, 0);
    shadowOf(Looper.getMainLooper()).idle();
    int displayId = virtualDisplay.getDisplay().getDisplayId();

    virtualDisplay.resize(1024, 768, DisplayMetrics.DENSITY_MEDIUM);
    shadowOf(Looper.getMainLooper()).idle();
    DisplayMetrics metrics = new DisplayMetrics();
    virtualDisplay.getDisplay().getRealMetrics(metrics);
    assertThat(metrics.widthPixels).isEqualTo(1024);
    assertThat(metrics.heightPixels).isEqualTo(768);

    virtualDisplay.release();
    shadowOf(Looper.getMainLooper()).idle();
    assertThat(displayIds()).doesNotContain(displayId);
    assertThat(events)
        .containsExactly("added " + displayId, "changed " + displayId, "removed " + displayId)
        .inOrder();
  }

  @Test
  public void presentation_isShownOnTheExternalDisplayAndFillsIt() {
    Display display = addExternalDisplay("w1920dp-h1080dp-land-mdpi");
    Activity activity = Robolectric.setupActivity(Activity.class);

    Presentation presentation = new Presentation(activity, display);
    View content = new View(presentation.getContext());
    presentation.setContentView(content);
    presentation.show();
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(presentation.isShowing()).isTrue();
    assertThat(presentation.getDisplay().getDisplayId()).isEqualTo(display.getDisplayId());
    assertThat(content.getWidth()).isEqualTo(1920);
    assertThat(content.getHeight()).isEqualTo(1080);
  }

  @Test
  public void activity_launchedOnTheExternalDisplay_isLaidOutForIt() {
    Display display = addExternalDisplay("w1920dp-h1080dp-land-mdpi");
    Bundle options =
        ActivityOptions.makeBasic().setLaunchDisplayId(display.getDisplayId()).toBundle();

    try (ActivityController<ContentActivity> controller =
        Robolectric.buildActivity(ContentActivity.class, null, options).setup()) {
      ContentActivity activity = controller.get();

      assertThat(activity.getWindowManager().getDefaultDisplay().getDisplayId())
          .isEqualTo(display.getDisplayId());
      assertThat(activity.content.getWidth()).isEqualTo(1920);
    }
  }

  private Display addExternalDisplay(String qualifiers) {
    return displayManager.getDisplay(ShadowDisplayManager.addDisplay(qualifiers, TYPE_EXTERNAL));
  }

  private List<Integer> displayIds() {
    return Arrays.stream(displayManager.getDisplays())
        .map(Display::getDisplayId)
        .collect(Collectors.toList());
  }

  private List<String> recordDisplayEvents() {
    List<String> events = new ArrayList<>();
    displayManager.registerDisplayListener(
        new DisplayListener() {
          @Override
          public void onDisplayAdded(int displayId) {
            events.add("added " + displayId);
          }

          @Override
          public void onDisplayChanged(int displayId) {
            events.add("changed " + displayId);
          }

          @Override
          public void onDisplayRemoved(int displayId) {
            events.add("removed " + displayId);
          }
        },
        new Handler(Looper.getMainLooper()));
    return events;
  }

  /** An activity whose content fills its window. */
  public static class ContentActivity extends Activity {
    View content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      content = new View(this);
      setContentView(content);
    }
  }
}
