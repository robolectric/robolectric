package org.robolectric.simulator;

import static com.google.common.truth.Truth.assertWithMessage;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests that the simulator shows the app of {@link SimulatorIntegrationTest} in an environment of
 * the runner API too: see {@link RunnerApiSimulator}, which runs in a JVM of its own, because a
 * simulator never stops.
 */
@RunWith(JUnit4.class)
public class RunnerApiSimulatorTest {
  private static final String APP = "org.robolectric.integrationtests.simulator";

  @Test
  public void testSimulatorShowsAppInEnvironmentOfRunnerApi() throws Exception {
    File apk = new SimulatorIntegrationTest().getSimulatorArgs().get(0);
    ImmutableList.Builder<String> command = ImmutableList.builder();
    command.add(Paths.get(System.getProperty("java.home"), "bin", "java").toString());
    // What the build lets the JVM of the tests open to Robolectric.
    for (String argument : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
      if (argument.startsWith("--add-") || argument.startsWith("--enable-")) {
        command.add(argument);
      }
    }
    // The simulator needs no window here.
    command.add("-Djava.awt.headless=true", "-cp", System.getProperty("java.class.path"));
    command.add(RunnerApiSimulator.class.getName(), apk.getAbsolutePath(), APP);

    Process process = new ProcessBuilder(command.build()).redirectErrorStream(true).start();
    String output;
    try (InputStreamReader reader =
        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)) {
      output = CharStreams.toString(reader);
    }

    assertWithMessage(output).that(process.waitFor(1, TimeUnit.MINUTES)).isTrue();
    assertWithMessage(output).that(process.exitValue()).isEqualTo(0);
  }
}
