package org.robolectric.simulator;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Priority;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.internal.bytecode.ClassHandler;

@RunWith(JUnit4.class)
public class SimulatorMainTest {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  /**
   * Building the sandbox fails after the sandbox's non-daemon main thread has started. The
   * simulator must still exit, rather than leave the JVM running with no window and no error.
   */
  @Test
  public void main_sandboxBuildFails_exitsWithStatus1() throws Exception {
    // An empty directory stands in for the APK; the sandbox build fails before it is read.
    File apkDir = tempFolder.newFolder("apk");
    // An extra classpath entry that makes the sandbox's injector pick FailingClassHandler.
    File pluginDir = tempFolder.newFolder("plugin");
    Path servicesFile =
        pluginDir.toPath().resolve("META-INF/services/" + ClassHandler.class.getName());
    Files.createDirectories(servicesFile.getParent());
    Files.write(servicesFile, FailingClassHandler.class.getName().getBytes(UTF_8));

    List<String> command = new ArrayList<>();
    command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
    command.add("-cp");
    command.add(System.getProperty("java.class.path"));
    for (String name : System.getProperties().stringPropertyNames()) {
      if (name.startsWith("robolectric.")) {
        command.add("-D" + name + "=" + System.getProperty(name));
      }
    }
    command.add(SimulatorMain.class.getName());
    command.add(apkDir.getPath());
    command.add(pluginDir.getPath());

    File output = tempFolder.newFile("output.txt");
    Process process =
        new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(output).start();
    try {
      boolean exited = process.waitFor(30, TimeUnit.SECONDS);
      String log = new String(Files.readAllBytes(output.toPath()), UTF_8);
      assertWithMessage("simulator did not exit; output:\n%s", log).that(exited).isTrue();
      assertWithMessage("output:\n%s", log).that(process.exitValue()).isEqualTo(1);
      assertThat(log).contains(FailingClassHandler.MESSAGE);
    } finally {
      process.destroyForcibly();
    }
  }

  /** A {@link ClassHandler} plugin that fails when the sandbox builder instantiates it. */
  @Priority(Integer.MAX_VALUE)
  public static final class FailingClassHandler implements ClassHandler {
    static final String MESSAGE = "FailingClassHandler: simulated sandbox build failure";

    public FailingClassHandler() {
      throw new IllegalStateException(MESSAGE);
    }

    @Override
    public void classInitializing(Class<?> clazz) {
      throw new UnsupportedOperationException();
    }

    @Override
    public MethodHandle getShadowCreator(Class<?> theClass) {
      throw new UnsupportedOperationException();
    }

    @Override
    public MethodHandle findShadowMethodHandle(
        Class<?> theClass, String name, MethodType methodType, boolean isStatic, boolean isNative) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object intercept(String signature, Object instance, Object[] params, Class<?> theClass) {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Throwable> T stripStackTrace(T throwable) {
      throw new UnsupportedOperationException();
    }
  }
}
