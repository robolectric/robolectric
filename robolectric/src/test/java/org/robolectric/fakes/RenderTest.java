package org.robolectric.fakes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Robolectric.buildActivity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources_Theme_Delegate;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.BridgeInflater;
import android.view.Display;
import android.view.View;
import android.view.ViewRootImpl;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.ide.common.rendering.api.HardwareConfig;
import com.android.ide.common.rendering.api.RenderResources;
import com.android.ide.common.rendering.api.RenderSession;
import com.android.ide.common.rendering.api.Result;
import com.android.ide.common.rendering.api.SessionParams;
import com.android.ide.common.rendering.api.ViewInfo;
import com.android.ide.common.resources.ResourceItem;
import com.android.ide.common.resources.ResourceRepository;
import com.android.ide.common.resources.ResourceResolver;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.io.FolderWrapper;
import com.android.layoutlib.bridge.Bridge;
import com.android.layoutlib.bridge.android.BridgeContext;
import com.android.layoutlib.bridge.impl.RenderAction;
import com.android.layoutlib.bridge.impl.RenderSessionImpl;
import com.android.resources.Density;
import com.android.resources.Keyboard;
import com.android.resources.KeyboardState;
import com.android.resources.Navigation;
import com.android.resources.NavigationState;
import com.android.resources.ScreenOrientation;
import com.android.resources.ScreenRatio;
import com.android.resources.ScreenSize;
import com.android.resources.TouchScreen;

import org.apache.tools.ant.types.selectors.ExtendSelector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.internal.Shadow;
import org.robolectric.internal.runtime.RuntimeAdapter;
import org.robolectric.internal.runtime.RuntimeAdapterFactory;
import org.robolectric.shadows.ShadowDisplay;
import org.robolectric.shadows.ShadowViewTest.MyActivity;
import org.robolectric.util.ActivityController;
import org.robolectric.util.ReflectionHelpers;
import org.xmlpull.v1.XmlPullParserException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

@RunWith(TestRunners.WithDefaults.class)
public class RenderTest {
  // path to the SDK and the project to render
  private final static String PROJECT = "./src/test/resources/";

  @Test
  @Config(rendering="true")
  public void XMLLayoutsShouldbeProperlyInflated() throws Exception{
    final String SDK = System.getenv("ANDROID_HOME");
    File f = new File(SDK + "/platforms/android-21");
    assertThat(f != null).isEqualTo(true);
    RenderServiceFactory factory = RenderServiceFactory.create(f);
    assertThat(factory != null).isEqualTo(true);
    ResourceRepository projectRes =
        new ResourceRepository(new FolderWrapper(PROJECT + "/res"), false/*isFramework*/) {
          @Override
          protected ResourceItem createResourceItem(String name) {
            return new ResourceItem(name);
          }
        };
    projectRes.loadResources();
    // create the rendering config
    FolderConfiguration config =
        RenderServiceFactory.createConfig(1280, 800, ScreenSize.XLARGE, ScreenRatio.LONG,
            ScreenOrientation.PORTRAIT, Density.MEDIUM, TouchScreen.FINGER, KeyboardState.SOFT,
            Keyboard.QWERTY, NavigationState.EXPOSED, Navigation.NONAV, 21/*api level*/);
    // create the resource resolver once for the given config.
    ResourceResolver resources =
        factory.createResourceResolver(config, projectRes, "Theme", false/*isProjectTheme*/);
    // create the render service
    RenderService renderService = factory.createService(resources, config, new ProjectCallback());


    RenderSession session =
        renderService.setLog(new StdOutLogger())
            .setAppInfo("foo", "icon") // optional
            .createRenderSession("text_views_hints");
    // get the status of the render
    Result result = session.getResult();

    assertThat(result.isSuccess()).isEqualTo(true);
    BufferedImage image = session.getImage();
    ImageIO.write(image, "png", new File("./target/maven-archiver/test.png"));
    // read the views
    displayViewObjects(session.getRootViews());
    session.dispose();
  }

  private static void displayViewObjects(List<ViewInfo> rootViews) {
    for (ViewInfo info : rootViews) {
      displayView(info, "");
    }
  }

  private static void displayView(ViewInfo info, String indent) {
    // display info data
    System.out.println(indent + info.getClassName() + " [" + info.getLeft() + ", " + info.getTop()
        + ", " + info.getRight() + ", " + info.getBottom() + "]");
    // display the children
    List<ViewInfo> children = info.getChildren();
    if (children != null) {
      indent += "\t";
      for (ViewInfo child : children) {
        View realView = (View)child.getViewObject();
        if (realView instanceof TextView) {
          TextView textView = (TextView) realView;
          assertThat(textView.getWidth()).isNotEqualTo(0);
        }
        displayView(child, indent);
      }
    }
  }
}
