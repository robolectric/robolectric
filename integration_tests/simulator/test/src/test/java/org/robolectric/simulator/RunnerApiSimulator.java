package org.robolectric.simulator;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JPanel;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.runner.common.RobolectricEnvironment;
import org.robolectric.runner.common.RobolectricSession;

/**
 * Runs an app in the simulator in an environment of the runner API, as {@link SimulatorMain} does,
 * and reaches Android while the simulator keeps the main thread, as a test runner that is based on
 * the simulator would. It exits with 0 once the simulator shows the app, which is blue.
 */
public final class RunnerApiSimulator {
  private static final int SDK = 35;
  private static final long TIMEOUT_MILLIS = 30_000;

  public static void main(String[] args) throws Exception {
    // The APK has the classes of the app too, which are not on the class path.
    Path app = Paths.get(args[0]);
    RobolectricSession session = RobolectricSession.builder().apk(app).classpath(app).build();
    Config config = new Config.Builder().setSdk(SDK).build();
    RobolectricEnvironment android =
        session.open(session.plan(config, GraphicsMode.Mode.NATIVE).get(0));
    Thread simulator =
        new Thread(
            () -> {
              // The simulator keeps the main thread of the environment: this only returns if it
              // fails.
              try {
                android.run(
                    () -> {
                      Class<?> start = android.loadClass(Start.class);
                      ((Runnable) start.getDeclaredConstructor().newInstance()).run();
                      return null;
                    });
              } catch (Exception e) {
                System.err.println("The simulator failed: " + e);
              }
              System.exit(1);
            });
    simulator.setDaemon(true);
    simulator.start();

    long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
    while (!showsBlue(android) && System.currentTimeMillis() < deadline) {
      Thread.sleep(200);
    }
    // The main thread is the simulator's now, so Android is reached through its looper.
    Object packageName =
        android.post(() -> android.loadClass(Start.class).getMethod("packageName").invoke(null));
    boolean shown = showsBlue(android);
    System.out.println("The simulator shows the app: " + shown + ", which is " + packageName);
    System.exit(shown && packageName.equals(args[1]) ? 0 : 1);
  }

  /** Returns whether the screen of the simulator, which the environment loads, is blue. */
  private static boolean showsBlue(RobolectricEnvironment android) throws Exception {
    Class<?> registry = android.loadClass(SimulatorPanelRegistry.class);
    JPanel panel = (JPanel) registry.getMethod("getActivePanel").invoke(null);
    if (panel == null) {
      return false;
    }
    Dimension size = panel.getPreferredSize();
    BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
    panel.setSize(size);
    Graphics2D graphics = image.createGraphics();
    panel.paint(graphics);
    graphics.dispose();
    return image.getRGB(image.getWidth() / 2, image.getHeight() / 2) == Color.BLUE.getRGB();
  }

  /**
   * Starts the simulator with the launcher activity of the app, as the simulator's own main class
   * does. It has to be loaded by the environment.
   */
  public static final class Start implements Runnable {
    @Override
    public void run() {
      Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
      PackageManager packages = RuntimeEnvironment.getApplication().getPackageManager();
      String activity = packages.queryIntentActivities(launcher, 0).get(0).activityInfo.name;
      try {
        new Simulator(Class.forName(activity).asSubclass(Activity.class)).start();
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException(e);
      }
    }

    public static String packageName() {
      return RuntimeEnvironment.getApplication().getPackageName();
    }
  }

  private RunnerApiSimulator() {}
}
