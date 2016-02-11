package org.robolectric.fakes;

import static org.assertj.core.api.Assertions.assertThat;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.ide.common.rendering.api.RenderSession;
import com.android.ide.common.rendering.api.Result;
import com.android.ide.common.rendering.api.ViewInfo;
import com.android.ide.common.resources.ResourceItem;
import com.android.ide.common.resources.ResourceRepository;
import com.android.ide.common.resources.ResourceResolver;
import com.android.ide.common.resources.configuration.FolderConfiguration;
import com.android.io.FolderWrapper;
import com.android.resources.Density;
import com.android.resources.Keyboard;
import com.android.resources.KeyboardState;
import com.android.resources.Navigation;
import com.android.resources.NavigationState;
import com.android.resources.ScreenOrientation;
import com.android.resources.ScreenRatio;
import com.android.resources.ScreenSize;
import com.android.resources.TouchScreen;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;


@RunWith(TestRunners.MultiApiWithDefaults.class)
@Config(rendering=true, sdk = {21, 22})
public class RenderTest {
  // path to the SDK and the project to render
  private final static String PROJECT = "./src/test/resources/";
  private RenderSession session;

  @Test
  public void shouldInflateXML() throws Exception{
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
        factory.createResourceResolver(config, projectRes, "Theme.Robolectric", true/*isProjectTheme*/);
    // create the render service
    RenderService renderService = factory.createService(resources, config, new ProjectCallback());


    session =
        renderService.setLog(new StdOutLogger())
            .setAppInfo("foo", "icon") // optional
            .createRenderSession("text_views_hints", org.robolectric.R.layout.text_views_hints);
    // get the status of the render
    Result result = session.getResult();

    assertThat(result.isSuccess()).isEqualTo(true);
    BufferedImage image = session.getImage();
    // read the views
    assertThat(image.getWidth()).isNotEqualTo(0);
    File outputfile = new File("target/saved.png");
    ImageIO.write(image, "png", outputfile);
    assertThat(session.getRootViews().size()).isNotEqualTo(0);
    displayViewObjects(session.getRootViews());
    ReflectionHelpers.setField(session, "mLastResult", Result.Status.NOT_IMPLEMENTED.createResult());
    session.render();
    image = session.getImage();
    outputfile = new File("target/saved1.png");
    ImageIO.write(image, "png", outputfile);

    image.flush();

    image = null;
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
          if (textView.getHint().toString().startsWith("White")) {
            com.android.layoutlib.bridge.Bridge.prepareThread();
            textView.setText("New Text");
            assertThat(textView.getId()).isEqualTo(R.id.white_text_view_hint);
            com.android.layoutlib.bridge.Bridge.cleanupThread();
          }
          assertThat(textView.getWidth()).isNotEqualTo(0);
        } else if (realView instanceof ImageView) {
          ImageView imageView = (ImageView) realView;
          com.android.layoutlib.bridge.Bridge.prepareThread();
          imageView.setImageDrawable(
              (RuntimeEnvironment.application.getResources()
              .getDrawable(R.drawable.third_image)));
          com.android.layoutlib.bridge.Bridge.cleanupThread();
        }
        displayView(child, indent);
      }
    }
  }
}
