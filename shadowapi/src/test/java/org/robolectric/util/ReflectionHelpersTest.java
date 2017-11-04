package org.robolectric.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import java.lang.reflect.Field;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@RunWith(JUnit4.class)
public class ReflectionHelpersTest {

  @Test
  public void getFieldReflectively_getsPrivateFields() {
    ExampleDescendant example = new ExampleDescendant();
    example.overridden = 5;
    assertThat((int) ReflectionHelpers.getField(example, "overridden")).isEqualTo(5);
  }

  @Test
  public void getFieldReflectively_getsInheritedFields() {
    ExampleDescendant example = new ExampleDescendant();
    example.setNotOverridden(6);
    assertThat((int) ReflectionHelpers.getField(example, "notOverridden")).isEqualTo(6);
  }

  @Test
  public void getFieldReflectively_givesHelpfulExceptions() {
    ExampleDescendant example = new ExampleDescendant();
    try {
      ReflectionHelpers.getField(example, "nonExistant");
      failBecauseExceptionWasNotThrown(RuntimeException.class);
    } catch (RuntimeException e) {
      if (!e.getMessage().contains("nonExistant")) {
        fail("poorly specified exception thrown", e);
      }
    }
  }

  @Test
  public void setFieldReflectively_setsPrivateFields() {
    ExampleDescendant example = new ExampleDescendant();
    example.overridden = 5;
    ReflectionHelpers.setField(example, "overridden", 10);
    assertThat(example.overridden).isEqualTo(10);
  }

  @Test
  public void setFieldReflectively_setsInheritedFields() {
    ExampleDescendant example = new ExampleDescendant();
    example.setNotOverridden(5);
    ReflectionHelpers.setField(example, "notOverridden", 10);
    assertThat(example.getNotOverridden()).isEqualTo(10);
  }

  @Test
  public void setFieldReflectively_givesHelpfulExceptions() {
    ExampleDescendant example = new ExampleDescendant();
    try {
      ReflectionHelpers.setField(example, "nonExistant", 6);
      failBecauseExceptionWasNotThrown(RuntimeException.class);
    } catch (RuntimeException e) {
      if (!e.getMessage().contains("nonExistant")) {
        fail("poorly specified exception thrown", e);
      }
    }
  }

  @Test
  public void getStaticFieldReflectively_withField_getsStaticField() throws Exception {
    Field field = ExampleDescendant.class.getDeclaredField("DESCENDANT");

    int result = ReflectionHelpers.getStaticField(field);
    assertThat(result).isEqualTo(6);
  }

  @Test
  public void getStaticFieldReflectively_withFieldName_getsStaticField() {
    assertThat((int) ReflectionHelpers.getStaticField(ExampleDescendant.class, "DESCENDANT"))
        .isEqualTo(6);
  }

  @Test
  public void setStaticFieldReflectively_withField_setsStaticFields() throws Exception {
    Field field = ExampleDescendant.class.getDeclaredField("DESCENDANT");
    int startingValue = ReflectionHelpers.getStaticField(field);

    ReflectionHelpers.setStaticField(field, 7);
    assertThat(startingValue).as("startingValue").isEqualTo(6);
    assertThat(ExampleDescendant.DESCENDANT).as("DESCENDENT").isEqualTo(7);

    /// Reset the value to avoid test pollution
    ReflectionHelpers.setStaticField(field, startingValue);
  }

  @Test
  public void setStaticFieldReflectively_withFieldName_setsStaticFields() {
    int startingValue = ReflectionHelpers.getStaticField(ExampleDescendant.class, "DESCENDANT");

    ReflectionHelpers.setStaticField(ExampleDescendant.class, "DESCENDANT", 7);
    assertThat(startingValue).as("startingValue").isEqualTo(6);
    assertThat(ExampleDescendant.DESCENDANT).as("DESCENDENT").isEqualTo(7);

    // Reset the value to avoid test pollution
    ReflectionHelpers.setStaticField(ExampleDescendant.class, "DESCENDANT", startingValue);
  }

  @Test
  public void callInstanceMethodReflectively_callsPrivateMethods() {
    ExampleDescendant example = new ExampleDescendant();
    assertThat((int) ReflectionHelpers.callInstanceMethod(example, "returnNumber")).isEqualTo(1337);
  }

  @Test
  public void callInstanceMethodReflectively_whenMultipleSignaturesExistForAMethodName_callsMethodWithCorrectSignature() {
    ExampleDescendant example = new ExampleDescendant();
    int returnNumber =
        ReflectionHelpers.callInstanceMethod(
            example, "returnNumber", ClassParameter.from(int.class, 5));
    assertThat(returnNumber).isEqualTo(5);
  }

  @Test
  public void callInstanceMethodReflectively_callsInheritedMethods() {
    ExampleDescendant example = new ExampleDescendant();
    assertThat((int) ReflectionHelpers.callInstanceMethod(example, "returnNegativeNumber"))
        .isEqualTo(-46);
  }

  @Test
  public void callInstanceMethodReflectively_givesHelpfulExceptions() {
    ExampleDescendant example = new ExampleDescendant();
    try {
      ReflectionHelpers.callInstanceMethod(example, "nonExistant");
      failBecauseExceptionWasNotThrown(RuntimeException.class);
    } catch (RuntimeException e) {
      if (!e.getMessage().contains("nonExistant")) {
        fail("poorly specified exception thrown", e);
      }
    }
  }

  @Test
  public void callInstanceMethodReflectively_rethrowsUncheckedException() {
    ExampleDescendant example = new ExampleDescendant();
    try {
      ReflectionHelpers.callInstanceMethod(example, "throwUncheckedException");
      failBecauseExceptionWasNotThrown(TestRuntimeException.class);
    } catch (TestRuntimeException e) {
    } catch (RuntimeException e) {
      fail("Unexpected exception thrown", e);
    }
  }

  @Test
  public void callInstanceMethodReflectively_rethrowsError() {
    ExampleDescendant example = new ExampleDescendant();
    try {
      ReflectionHelpers.callInstanceMethod(example, "throwError");
      failBecauseExceptionWasNotThrown(TestError.class);
    } catch (RuntimeException e) {
      fail("Unexpected exception thrown", e);
    } catch (TestError e) {
    }
  }

  @Test
  public void callInstanceMethodReflectively_wrapsCheckedException() {
    ExampleDescendant example = new ExampleDescendant();
    try {
      ReflectionHelpers.callInstanceMethod(example, "throwCheckedException");
      failBecauseExceptionWasNotThrown(RuntimeException.class);
    } catch (RuntimeException e) {
      assertThat(e.getCause()).isInstanceOf(TestException.class);
    }
  }

  @Test
  public void callStaticMethodReflectively_callsPrivateStaticMethodsReflectively() {
    int constantNumber =
        ReflectionHelpers.callStaticMethod(ExampleDescendant.class, "getConstantNumber");
    assertThat(constantNumber).isEqualTo(1);
  }

  @Test
  public void callStaticMethodReflectively_rethrowsUncheckedException() {
    try {
      ReflectionHelpers.callStaticMethod(ExampleDescendant.class, "staticThrowUncheckedException");
      failBecauseExceptionWasNotThrown(TestRuntimeException.class);
    } catch (TestRuntimeException e) {
    } catch (RuntimeException e) {
      fail("Unexpected exception thrown", e);
    }
  }

  @Test
  public void callStaticMethodReflectively_rethrowsError() {
    try {
      ReflectionHelpers.callStaticMethod(ExampleDescendant.class, "staticThrowError");
      failBecauseExceptionWasNotThrown(TestError.class);
    } catch (RuntimeException e) {
      fail("Unexpected exception thrown", e);
    } catch (TestError e) {
    }
  }

  @Test
  public void callStaticMethodReflectively_wrapsCheckedException() {
    try {
      ReflectionHelpers.callStaticMethod(ExampleDescendant.class, "staticThrowCheckedException");
      failBecauseExceptionWasNotThrown(RuntimeException.class);
    } catch (RuntimeException e) {
      assertThat(e.getCause()).isInstanceOf(TestException.class);
    }
  }

  @Test
  public void callConstructorReflectively_callsPrivateConstructors() {
    ExampleClass e = ReflectionHelpers.callConstructor(ExampleClass.class);
    assertThat(e).isNotNull();
  }

  @Test
  public void callConstructorReflectively_rethrowsUncheckedException() {
    try {
      ReflectionHelpers.callConstructor(ThrowsUncheckedException.class);
      failBecauseExceptionWasNotThrown(TestRuntimeException.class);
    } catch (TestRuntimeException e) {
    } catch (RuntimeException e) {
      fail("Unexpected exception thrown", e);
    }
  }

  @Test
  public void callConstructorReflectively_rethrowsError() {
    try {
      ReflectionHelpers.callConstructor(ThrowsError.class);
      failBecauseExceptionWasNotThrown(TestError.class);
    } catch (RuntimeException e) {
      fail("Unexpected exception thrown", e);
    } catch (TestError e) {
    }
  }

  @Test
  public void callConstructorReflectively_wrapsCheckedException() {
    try {
      ReflectionHelpers.callConstructor(ThrowsCheckedException.class);
      failBecauseExceptionWasNotThrown(RuntimeException.class);
    } catch (RuntimeException e) {
      assertThat(e.getCause()).isInstanceOf(TestException.class);
    }
  }

  @Test
  public void callConstructorReflectively_whenMultipleSignaturesExistForTheConstructor_callsConstructorWithCorrectSignature() {
    ExampleClass ec = ReflectionHelpers.callConstructor(ExampleClass.class, ClassParameter.from(int.class, 16));
    assertThat(ec.index).as("index").isEqualTo(16);
    assertThat(ec.name).as("name").isNull();
  }

  @SuppressWarnings("serial")
  private static class TestError extends Error {
  }

  @SuppressWarnings("serial")
  private static class TestException extends Exception {
  }

  @SuppressWarnings("serial")
  private static class TestRuntimeException extends RuntimeException {
  }

  @SuppressWarnings("unused")
  private static class ExampleBase {
    private int notOverridden;
    protected int overridden;

    private static final int BASE = 8;

    public int getNotOverridden() {
      return notOverridden;
    }

    public void setNotOverridden(int notOverridden) {
      this.notOverridden = notOverridden;
    }

    private int returnNegativeNumber() {
      return -46;
    }
  }

  @SuppressWarnings("unused")
  private static class ExampleDescendant extends ExampleBase {
    public static int DESCENDANT = 6;

    protected int overridden;

    private int returnNumber() {
      return 1337;
    }

    private int returnNumber(int n) {
      return n;
    }

    private static int getConstantNumber() {
      return 1;
    }

    private void throwUncheckedException() {
      throw new TestRuntimeException();
    }

    private void throwCheckedException() throws Exception {
      throw new TestException();
    }

    private void throwError() {
      throw new TestError();
    }

    private static void staticThrowUncheckedException() {
      throw new TestRuntimeException();
    }

    private static void staticThrowCheckedException() throws Exception {
      throw new TestException();
    }

    private static void staticThrowError() {
      throw new TestError();
    }
  }

  private static class ThrowsError {
    @SuppressWarnings("unused")
    public ThrowsError() {
      throw new TestError();
    }
  }

  private static class ThrowsCheckedException {
    @SuppressWarnings("unused")
    public ThrowsCheckedException() throws Exception {
      throw new TestException();
    }
  }

  private static class ThrowsUncheckedException {
    @SuppressWarnings("unused")
    public ThrowsUncheckedException() {
      throw new TestRuntimeException();
    }
  }

  private static class ExampleClass {
    public String name;
    public int index;

    private ExampleClass() {
    }

    private ExampleClass(String name) {
      this.name = name;
    }

    private ExampleClass(int index) {
      this.index = index;
    }
  }
}
