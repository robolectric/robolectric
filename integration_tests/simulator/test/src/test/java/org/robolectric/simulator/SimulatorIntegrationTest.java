package org.robolectric.simulator;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import javax.swing.JPanel;
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
        new Thread(
            () -> {
              String[] stringArgs = args.stream().map(File::getAbsolutePath).toArray(String[]::new);
              for (int i = 0; i < args.size(); i++) {
                stringArgs[i] = args.get(i).getAbsolutePath();
              }
              SimulatorMain.main(stringArgs);
            });
    simulatorThread.setDaemon(true);
    simulatorThread.start();

    BufferedImage capture = waitForBluePixel();
    assertThat(capture).isNotNull();
    int clr = capture.getRGB(capture.getWidth() / 2, capture.getHeight() / 2);
    Color c = new Color(clr);
    assertThat(c.getBlue()).isEqualTo(255);
    assertThat(c.getRed()).isEqualTo(0);
    assertThat(c.getGreen()).isEqualTo(0);
  }

  protected List<File> getSimulatorArgs() {
    URL resource = getClass().getResource("/simulator-app.apk");
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

  private BufferedImage waitForBluePixel() throws InterruptedException {
    long start = System.currentTimeMillis();
    while (System.currentTimeMillis() - start < 30_000) {
      JPanel panel = SimulatorPanelRegistry.getActivePanel();
      if (panel != null && panel.getPreferredSize().getWidth() != 10) {
        BufferedImage image = getPanelImage(panel);
        if (image != null && image.getWidth() > 0 && image.getHeight() > 0) {
          int clr = image.getRGB(image.getWidth() / 2, image.getHeight() / 2);
          Color c = new Color(clr);
          if (c.getBlue() == 255 && c.getRed() == 0 && c.getGreen() == 0) {
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
