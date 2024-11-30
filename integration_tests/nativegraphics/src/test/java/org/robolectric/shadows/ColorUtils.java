/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package org.robolectric.shadows;

import static org.junit.Assert.fail;

import android.graphics.Color;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ColorUtils {
  public static void verifyColor(int expected, int observed) {
    verifyColor(expected, observed, 0);
  }

  public static void verifyColor(int expected, int observed, int tolerance) {
    verifyColor("", expected, observed, tolerance);
  }

  /**
   * Verify that two colors match within a per-channel tolerance.
   *
   * @param s String with extra information about the test with an error.
   * @param expected Expected color.
   * @param observed Observed color.
   * @param tolerance Per-channel tolerance by which the color can mismatch.
   */
  public static void verifyColor(@Nonnull String s, int expected, int observed, int tolerance) {
    s +=
        " expected 0x"
            + Integer.toHexString(expected)
            + ", observed 0x"
            + Integer.toHexString(observed)
            + ", tolerated channel error 0x"
            + tolerance;
    String red = verifyChannel("red", expected, observed, tolerance, Color::red);
    String green = verifyChannel("green", expected, observed, tolerance, Color::green);
    String blue = verifyChannel("blue", expected, observed, tolerance, Color::blue);
    String alpha = verifyChannel("alpha", expected, observed, tolerance, Color::alpha);

    buildErrorString(s, red, green, blue, alpha);
  }

  /**
   * Verify that two colors match within a per-channel tolerance.
   *
   * @param msg String with extra information about the test with an error.
   * @param expected Expected color.
   * @param observed Observed color.
   * @param tolerance Per-channel tolerance by which the color can mismatch.
   */
  public static void verifyColor(
      @Nonnull String msg, Color expected, Color observed, float tolerance) {
    if (!expected.getColorSpace().equals(observed.getColorSpace())) {
      fail(
          "Cannot compare Colors with different color spaces! expected: "
              + expected
              + "\tobserved: "
              + observed);
    }
    msg +=
        " expected "
            + expected
            + ", observed "
            + observed
            + ", tolerated channel error "
            + tolerance;
    String red = verifyChannel("red", expected, observed, tolerance, Color::red);
    String green = verifyChannel("green", expected, observed, tolerance, Color::green);
    String blue = verifyChannel("blue", expected, observed, tolerance, Color::blue);
    String alpha = verifyChannel("alpha", expected, observed, tolerance, Color::alpha);

    buildErrorString(msg, red, green, blue, alpha);
  }

  private static void buildErrorString(
      @Nonnull String s,
      @Nullable String red,
      @Nullable String green,
      @Nullable String blue,
      @Nullable String alpha) {
    StringBuilder error = null;
    for (String channel : new String[] {red, green, blue, alpha}) {
      if (channel == null) {
        continue;
      }
      if (error == null) {
        error = new StringBuilder(s);
      }
      error.append("\n\t\t").append(channel);
    }
    if (error != null) {
      fail(error.toString());
    }
  }

  private static String verifyChannel(
      String channelName, int expected, int observed, int tolerance, IntUnaryOperator f) {
    int e = f.applyAsInt(expected);
    int o = f.applyAsInt(observed);
    if (Math.abs(e - o) <= tolerance) {
      return null;
    }
    return "Channel "
        + channelName
        + " mismatch: expected<0x"
        + Integer.toHexString(e)
        + ">, observed: <0x"
        + Integer.toHexString(o)
        + ">";
  }

  private static String verifyChannel(
      String channelName,
      Color expected,
      Color observed,
      float tolerance,
      Function<Color, Float> f) {
    float e = f.apply(expected);
    float o = f.apply(observed);
    float diff = Math.abs(e - o);
    if (diff <= tolerance) {
      return null;
    }
    return "Channel "
        + channelName
        + " mismatch: expected<"
        + e
        + ">, observed: <"
        + o
        + ">, difference: <"
        + diff
        + ">";
  }

  private ColorUtils() {}
}
