package org.robolectric.simulator;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SimulatorTest {

  @Test
  public void hasDisplay_linuxWithX11Display() {
    assertThat(Simulator.hasDisplay("Linux", ImmutableMap.of("DISPLAY", ":0"))).isTrue();
  }

  @Test
  public void hasDisplay_linuxWithoutDisplay() {
    assertThat(Simulator.hasDisplay("Linux", ImmutableMap.of())).isFalse();
    assertThat(Simulator.hasDisplay("Linux", ImmutableMap.of("DISPLAY", " "))).isFalse();
    assertThat(Simulator.hasDisplay("Linux", ImmutableMap.of("WAYLAND_DISPLAY", "wayland-0")))
        .isFalse();
  }

  @Test
  public void hasDisplay_macAndWindows() {
    assertThat(Simulator.hasDisplay("Mac OS X", ImmutableMap.of())).isTrue();
    assertThat(Simulator.hasDisplay("Windows 11", ImmutableMap.of())).isTrue();
  }
}
