package org.robolectric;

import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Correspondence.transforming;
import static com.google.common.truth.Truth.assertThat;

import android.os.Build.VERSION;
import com.google.common.truth.Correspondence;
import com.google.testing.junit.testparameterinjector.TestParameter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.Config;

@SuppressWarnings({"TestMethodWithIncorrectSignature", "UnconstructableJUnitTestCase"})
@RunWith(JUnit4.class)
public class RobolectricTestParameterInjectorTest {
  private static final Correspondence<Description, String> METHOD_NAME_TRANSFORM =
      transforming(Description::getMethodName, "has method name");

  private final RunNotifier runNotifier = new RunNotifier();

  @Before
  public void setup() {
    runNotifier.addListener(
        new RunListener() {
          @Override
          public void testFailure(Failure failure) throws Exception {
            throw new AssertionError("Unexpected test failure: " + failure, failure.getException());
          }
        });
  }

  public static class NoInjection {
    @Config(sdk = S)
    @Test
    public void test() {
      assertThat(VERSION.SDK_INT).isEqualTo(S);
    }
  }

  @Test
  public void noInjection() throws Exception {
    Runner runner = new RobolectricTestParameterInjector(NoInjection.class);

    runner.run(runNotifier);

    assertThat(runner.testCount()).isEqualTo(1);
  }

  @Config(sdk = Config.NEWEST_SDK)
  public static class InjectedMethod {
    @Test
    public void test(@TestParameter boolean param) {
      assertThat(param).isAnyOf(true, false);
    }
  }

  @Test
  public void injectedMethod() throws Exception {
    Runner runner = new RobolectricTestParameterInjector(InjectedMethod.class);

    runner.run(runNotifier);

    assertThat(runner.testCount()).isEqualTo(2);
    assertThat(runner.getDescription().getChildren())
        .comparingElementsUsing(METHOD_NAME_TRANSFORM)
        .containsExactly("test[param=true]", "test[param=false]");
  }

  @Config(sdk = Config.NEWEST_SDK)
  public static class InjectedField {
    @TestParameter({"hello", "world"})
    String param;

    @Test
    public void test() {
      assertThat(param).isAnyOf("hello", "world");
    }
  }

  @Test
  public void injectedField() throws Exception {
    Runner runner = new RobolectricTestParameterInjector(InjectedField.class);

    runner.run(runNotifier);

    assertThat(runner.testCount()).isEqualTo(2);
    assertThat(runner.getDescription().getChildren())
        .comparingElementsUsing(METHOD_NAME_TRANSFORM)
        .containsExactly("test[hello]", "test[world]");
  }

  @Config(sdk = Config.NEWEST_SDK)
  public static class InjectedConstructor {
    private final int param;

    public InjectedConstructor(@TestParameter({"1", "2"}) int param) {
      this.param = param;
    }

    @Test
    public void test() {
      assertThat(param).isAnyOf(1, 2);
    }
  }

  @Test
  public void injectedConstructor() throws Exception {
    Runner runner = new RobolectricTestParameterInjector(InjectedConstructor.class);

    runner.run(runNotifier);

    assertThat(runner.testCount()).isEqualTo(2);
    assertThat(runner.getDescription().getChildren())
        .comparingElementsUsing(METHOD_NAME_TRANSFORM)
        .containsExactly("test[param=1]", "test[param=2]");
  }

  @Config(sdk = Config.NEWEST_SDK)
  public static class InjectedEnum {
    enum Value {
      ONE,
      TWO
    }

    @Test
    public void test(@TestParameter Value param) {
      assertThat(param).isAnyOf(Value.ONE, Value.TWO);
    }
  }

  @Test
  public void injectedEnum() throws Exception {
    Runner runner = new RobolectricTestParameterInjector(InjectedEnum.class);

    runner.run(runNotifier);

    assertThat(runner.testCount()).isEqualTo(2);
    assertThat(runner.getDescription().getChildren())
        .comparingElementsUsing(METHOD_NAME_TRANSFORM)
        .containsExactly("test[ONE]", "test[TWO]");
  }

  public static class MultiSdk {
    @Test
    @Config(sdk = {28, 31})
    public void test(@TestParameter boolean param) {
      assertThat(param).isAnyOf(true, false);
      assertThat(VERSION.SDK_INT).isAnyOf(28, 31);
    }
  }

  @Test
  public void multiSdk() throws Exception {
    Runner runner = new RobolectricTestParameterInjector(MultiSdk.class);

    runner.run(runNotifier);

    assertThat(runner.testCount()).isEqualTo(4);
    assertThat(runner.getDescription().getChildren())
        .comparingElementsUsing(METHOD_NAME_TRANSFORM)
        .containsExactly(
            "test[param=true]",
            "test[param=false]",
            "test[param=true][28]",
            "test[param=false][28]");
  }

  // Simulate behavior of proto lite enum toString which includes the object hashcode (proto lite
  // toString tries to avoid dep on enum name so that the name can be stripped by appreduce).
  @Config(sdk = Config.NEWEST_SDK)
  public static class HashCodeToString {
    enum HashCodeToStringValue {
      ONE,
      TWO;

      @Override
      public String toString() {
        return "" + super.hashCode();
      }
    }

    @Test
    public void test(@TestParameter HashCodeToStringValue param) {
      assertThat(param).isAnyOf(HashCodeToStringValue.ONE, HashCodeToStringValue.TWO);
    }
  }

  @Test
  public void hashCodeToString() throws Exception {
    Runner runner = new RobolectricTestParameterInjector(HashCodeToString.class);

    runner.run(runNotifier);

    assertThat(runner.testCount()).isEqualTo(2);
  }

  public static class Base {
    @Test
    public void test() {}
  }

  @Config(sdk = Config.NEWEST_SDK)
  public static class Child extends Base {
    @Override
    public void test() {}
  }

  @Test
  public void overridden() throws Exception {
    Runner runner = new RobolectricTestParameterInjector(Child.class);

    runner.run(runNotifier);

    assertThat(runner.testCount()).isEqualTo(1);
  }
}
