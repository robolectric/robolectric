package org.robolectric;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.internal.Instrument;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
@Config(sdk = Config.NEWEST_SDK)
public class InvokeDynamicTest {
  @Test
  @Config(shadows = {DoNothingShadow.class})
  public void doNothing() {
    DoNothing nothing = new DoNothing();
    assertThat(nothing.identity(5)).isEqualTo(0);
  }

  @Test
  @Config(shadows = {RealShadow.class})
  public void directlyOn() {
    Real real = new Real();
    RealShadow shadow = Shadow.extract(real);

    assertThat(real.x).isEqualTo(-1);
    assertThat(shadow.x).isEqualTo(-2);

    real.setX(5);
    assertThat(real.x).isEqualTo(-5);
    assertThat(shadow.x).isEqualTo(5);

    Shadow.directlyOn(real, Real.class).setX(42);
    assertThat(real.x).isEqualTo(42);
    assertThat(shadow.x).isEqualTo(5);
  }

  @Test
  @Config(shadows = {RealShadow1.class})
  public void rebindShadow1() {
    RealCopy real = new RealCopy();
    real.setX(42);
    assertThat(real.x).isEqualTo(1);
  }

  @Test
  @Config(shadows = {RealShadow2.class})
  public void rebindShadow2() {
    RealCopy real = new RealCopy();
    real.setX(42);
    assertThat(real.x).isEqualTo(2);
  }

  @Instrument
  public static class Real {
    public int x = -1;

    public void setX(int x) {
      this.x = x;
    }
  }

  @Instrument
  public static class RealCopy {
    public int x;

    public void setX(int x) {
    }
  }

  @Implements(Real.class)
  public static class RealShadow {
    @RealObject Real real;

    public int x = -2;

    @Implementation
    protected void setX(int x) {
      this.x = x;
      real.x = -x;
    }
  }

  @Implements(RealCopy.class)
  public static class RealShadow1 {
    @RealObject RealCopy real;

    @Implementation
    protected void setX(int x) {
      real.x = 1;
    }
  }

  @Implements(RealCopy.class)
  public static class RealShadow2 {
    @RealObject RealCopy real;

    @Implementation
    protected void setX(int x) {
      real.x = 2;
    }
  }

  @Instrument
  public static class DoNothing {
    public int identity(int x) {
      return x;
    }
  }

  @Implements(value = DoNothing.class, callThroughByDefault = false)
  public static class DoNothingShadow {

  }
}
