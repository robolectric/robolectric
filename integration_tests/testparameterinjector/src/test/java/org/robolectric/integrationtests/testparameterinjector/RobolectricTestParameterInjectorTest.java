package org.robolectric.integrationtests.testparameterinjector;

import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Truth.assertThat;

import android.os.Build.VERSION;
import com.google.testing.junit.testparameterinjector.TestParameter;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.robolectric.RobolectricTestParameterInjector;
import org.robolectric.annotation.Config;

@SuppressWarnings({"TestMethodWithIncorrectSignature", "UnconstructableJUnitTestCase"})
@RunWith(JUnit4.class)
public class RobolectricTestParameterInjectorTest {
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

  @Ignore
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

  @Ignore
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
    ArrayList<Description> descriptions = runner.getDescription().getChildren();
    // In gradle it's test[false], in bazel it's test[param=false].
    assertThat(descriptions.get(0).getMethodName()).matches("test\\[(param=)?false\\]");
    assertThat(descriptions.get(1).getMethodName()).matches("test\\[(param=)?true\\]");
  }

  @Ignore
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

    assertThat(runner.testCount()).isEqualTo(2);
    ArrayList<Description> descriptions = runner.getDescription().getChildren();
    assertThat(descriptions.get(0).getMethodName()).isEqualTo("test[hello]");
    assertThat(descriptions.get(1).getMethodName()).isEqualTo("test[world]");
  }

  @Ignore
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
    ArrayList<Description> descriptions = runner.getDescription().getChildren();
    // In gradle it's test[1], in bazel it's test[param=1].
    assertThat(descriptions.get(0).getMethodName()).matches("test\\[(param=)?1\\]");
    assertThat(descriptions.get(1).getMethodName()).matches("test\\[(param=)?2\\]");
  }

  @Ignore
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

    ArrayList<Description> descriptions = runner.getDescription().getChildren();
    assertThat(descriptions.get(0).getMethodName()).isEqualTo("test[ONE]");
    assertThat(descriptions.get(1).getMethodName()).isEqualTo("test[TWO]");
  }

  @Ignore
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

    ArrayList<Description> descriptions = runner.getDescription().getChildren();
    // In gradle it's test[false][28], in bazel it's test[param=false][28].
    assertThat(descriptions.get(0).getMethodName()).matches("test\\[(param=)?false\\]\\[28\\]");
    assertThat(descriptions.get(1).getMethodName()).matches("test\\[(param=)?true\\]\\[28\\]");
    assertThat(descriptions.get(2).getMethodName()).matches("test\\[(param=)?false\\]");
    assertThat(descriptions.get(3).getMethodName()).matches("test\\[(param=)?true\\]");
  }

  // Simulate behavior of proto lite enum toString which includes the object hashcode (proto lite
  // toString tries to avoid dep on enum name so that the name can be stripped by appreduce).
  @Ignore
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

  @Ignore
  public static class Base {
    @Test
    public void test() {}
  }

  @Ignore
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
