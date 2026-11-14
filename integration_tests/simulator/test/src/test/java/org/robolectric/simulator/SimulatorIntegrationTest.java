package org.robolectric.simulator;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SimulatorIntegrationTest {

  @Test
  public void testSimulatorLaunchAndClick() throws Exception {
    List<File> args = getSimulatorArgs();
    assertThat(args).isNotEmpty();
    for (File f : args) {
      assertThat(f.exists()).isTrue();
    }
    // Run SimulatorMain in a separate thread
    Thread simulatorThread =
        new Thread(() -> {
          String[] stringArgs = args.stream().map(File::getAbsolutePath).toArray(String[]::new);
          for (int i = 0; i < args.size(); i++) {
            stringArgs[i] = args.get(i).getAbsolutePath();
          }
          SimulatorMain.main(stringArgs);
        });
    simulatorThread.setDaemon(true);
    simulatorThread.start();

    BufferedImage capture = waitForCenterColor(Color.BLUE);
    assertThat(capture).isNotNull();

    // Input reaches the app through the panel, even when the simulator is headless.
    click(capture.getWidth() / 2, capture.getHeight() / 2);
    assertThat(waitForCenterColor(Color.GREEN)).isNotNull();
    pressKey(KeyEvent.VK_SPACE);
    assertThat(waitForCenterColor(Color.YELLOW)).isNotNull();
  }

  /** Clicks the panel the way Swing reports a click to its mouse listeners. */
  private static void click(int x, int y) throws Exception {
    JPanel panel = SimulatorPanelRegistry.getActivePanel();
    SwingUtilities.invokeAndWait(() -> {
      for (int id : new int[] {MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_RELEASED}) {
        MouseEvent event =
            new MouseEvent(
                panel,
                id,
                System.currentTimeMillis(),
                id == MouseEvent.MOUSE_PRESSED ? InputEvent.BUTTON1_DOWN_MASK : 0,
                x,
                y,
                /* clickCount= */ 1,
                /* popupTrigger= */ false,
                MouseEvent.BUTTON1);
        for (MouseListener listener : panel.getMouseListeners()) {
          if (id == MouseEvent.MOUSE_PRESSED) {
            listener.mousePressed(event);
          } else {
            listener.mouseReleased(event);
          }
        }
      }
    });
  }

  /** Presses and releases a key the way Swing reports it to the panel's key listeners. */
  private static void pressKey(int keyCode) throws Exception {
    JPanel panel = SimulatorPanelRegistry.getActivePanel();
    SwingUtilities.invokeAndWait(() -> {
      for (int id : new int[] {KeyEvent.KEY_PRESSED, KeyEvent.KEY_RELEASED}) {
        KeyEvent event =
            new KeyEvent(
                panel,
                id,
                System.currentTimeMillis(),
                /* modifiers= */ 0,
                keyCode,
                KeyEvent.CHAR_UNDEFINED);
        for (KeyListener listener : panel.getKeyListeners()) {
          if (id == KeyEvent.KEY_PRESSED) {
            listener.keyPressed(event);
          } else {
            listener.keyReleased(event);
          }
        }
      }
    });
  }

  /** The test app to launch, as a test resource. */
  protected String getApkResource() {
    return "/simulator-app.apk";
  }

  protected List<File> getSimulatorArgs() {
    URL resource = getClass().getResource(getApkResource());
    assertThat(resource).isNotNull();
    try {
      if (Objects.equals(resource.getProtocol(), "file")) {
        return ImmutableList.of(new File(resource.toURI()));
      } else {
        // Extract to temp file
        File tempFile = File.createTempFile("simulator-app", ".apk");
        tempFile.deleteOnExit();
        try (InputStream in = resource.openStream();
            OutputStream out = new FileOutputStream(tempFile)) {
          ByteStreams.copy(in, out);
        }
        return ImmutableList.of(tempFile);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private BufferedImage waitForCenterColor(Color expected) throws InterruptedException {
    long start = System.currentTimeMillis();
    while (System.currentTimeMillis() - start < 30_000) {
      JPanel panel = SimulatorPanelRegistry.getActivePanel();
      if (panel != null && panel.getPreferredSize().getWidth() != 10) {
        BufferedImage image = getPanelImage(panel);
        if (image != null && image.getWidth() > 0 && image.getHeight() > 0) {
          int clr = image.getRGB(image.getWidth() / 2, image.getHeight() / 2);
          Color c = new Color(clr);
          if (c.equals(expected)) {
            return image;
          }
        }
      }
      Thread.sleep(500);
    }
    return null;
  }

  private BufferedImage getPanelImage(JPanel panel) {
    Dimension size = panel.getPreferredSize();
    BufferedImage image =
        new BufferedImage(
            (int) size.getWidth(), (int) size.getHeight(), BufferedImage.TYPE_INT_RGB);
    panel.setSize(size);
    Graphics2D g2d = image.createGraphics();
    panel.paint(g2d);
    g2d.dispose();
    return image;
  }
}
