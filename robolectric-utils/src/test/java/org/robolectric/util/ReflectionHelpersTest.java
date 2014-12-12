package org.robolectric.util;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import com.google.android.collect.Lists;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

public class ReflectionHelpersTest {

  @Test
  public void getFieldReflectively_getsPrivateFields() {
    ExampleDescendant example = new ExampleDescendant();
    example.overridden = 5;
    assertThat(ReflectionHelpers.getFieldReflectively(example, "overridden")).isEqualTo(5);
  }

  @Test
  public void getFieldReflectively_getsInheritedFields() {
    ExampleDescendant example = new ExampleDescendant();
    example.setNotOverridden(6);
    assertThat(ReflectionHelpers.getFieldReflectively(example, "notOverridden")).isEqualTo(6);
  }

  @Test
  public void getFieldReflectively_givesHelpfulExceptions() {
    ExampleDescendant example = new ExampleDescendant();
    try {
      ReflectionHelpers.getFieldReflectively(example, "nonExistant");
      Assertions.failBecauseExceptionWasNotThrown(RuntimeException.class);
    } catch (RuntimeException e) {
      if (!e.getMessage().contains("nonExistant")) {
        Assertions.fail("poorly specified exception thrown", e);
      }
    }
  }

  @Test
  public void setFieldReflectively_setsPrivateFields() {
    ExampleDescendant example = new ExampleDescendant();
    example.overridden = 5;
    ReflectionHelpers.setFieldReflectively(example, "overridden", 10);
    assertThat(example.overridden).isEqualTo(10);
  }

  @Test
  public void setFieldReflectively_setsInheritedFields() {
    ExampleDescendant example = new ExampleDescendant();
    example.setNotOverridden(5);
    ReflectionHelpers.setFieldReflectively(example, "notOverridden", 10);
    assertThat(example.getNotOverridden()).isEqualTo(10);
  }

  @Test
  public void setFieldReflectively_givesHelpfulExceptions() {
    ExampleDescendant example = new ExampleDescendant();
    try {
      ReflectionHelpers.setFieldReflectively(example, "nonExistant", 6);
      Assertions.failBecauseExceptionWasNotThrown(RuntimeException.class);
    } catch (RuntimeException e) {
      if (!e.getMessage().contains("nonExistant")) {
        Assertions.fail("poorly specified exception thrown", e);
      }
    }
  }

  @Test
  public void getStaticFieldReflectively_withField_getsStaticField() throws Exception {
    Field field = ExampleDescendant.class.getDeclaredField("DESCENDANT");

    int result = ReflectionHelpers.getStaticFieldReflectively(field);
    assertThat(result).isEqualTo(6);
  }

  @Test
  public void getStaticFieldReflectively_withFieldName_getsStaticField() {
    assertThat(ReflectionHelpers.getStaticFieldReflectively(ExampleDescendant.class, "DESCENDANT")).isEqualTo(6);
  }

  @Test
  public void setStaticFieldReflectively_withField_setsStaticFields() throws Exception {
    Field field = ExampleDescendant.class.getDeclaredField("DESCENDANT");
    int startingValue = ReflectionHelpers.getStaticFieldReflectively(field);

    ReflectionHelpers.setStaticFieldReflectively(field, 7);
    assertThat(startingValue).as("startingValue").isEqualTo(6);
    assertThat(ExampleDescendant.DESCENDANT).as("DESCENDENT").isEqualTo(7);

    /// Reset the value to avoid test pollution
    ReflectionHelpers.setStaticFieldReflectively(field, startingValue);
  }

  @Test
  public void setStaticFieldReflectively_withFieldName_setsStaticFields() {
    int startingValue = ReflectionHelpers.getStaticFieldReflectively(ExampleDescendant.class, "DESCENDANT");

    ReflectionHelpers.setStaticFieldReflectively(ExampleDescendant.class, "DESCENDANT", 7);
    assertThat(startingValue).as("startingValue").isEqualTo(6);
    assertThat(ExampleDescendant.DESCENDANT).as("DESCENDENT").isEqualTo(7);

    // Reset the value to avoid test pollution
    ReflectionHelpers.setStaticFieldReflectively(ExampleDescendant.class, "DESCENDANT", startingValue);
  }

  @Test
  public void callInstanceMethodReflectively_callsPrivateMethods() {
    ExampleDescendant example = new ExampleDescendant();
    assertThat(ReflectionHelpers.callInstanceMethodReflectively(example, "returnNumber")).isEqualTo(1337);
  }

  @Test
  public void callInstanceMethodReflectively_whenMultipleSignaturesExistForAMethodName_callsMethodWithCorrectSignature() {
    ExampleDescendant example = new ExampleDescendant();
    assertThat(ReflectionHelpers.callInstanceMethodReflectively(example, "returnNumber", ClassParameter.from(int.class, 5)))
      .isEqualTo(5);
  }

  @Test
  public void callInstanceMethodReflectively_callsInheritedMethods() {
    ExampleDescendant example = new ExampleDescendant();
    assertThat(ReflectionHelpers.callInstanceMethodReflectively(example, "returnNegativeNumber")).isEqualTo(-46);
  }

  @Test
  public void callInstanceMethodReflectively_givesHelpfulExceptions() {
    ExampleDescendant example = new ExampleDescendant();
    try {
      ReflectionHelpers.callInstanceMethodReflectively(example, "nonExistant");
      Assertions.failBecauseExceptionWasNotThrown(RuntimeException.class);
    } catch (RuntimeException e) {
      if (!e.getMessage().contains("nonExistant")) {
        Assertions.fail("poorly specified exception thrown", e);
      }
    }
  }

  @Test
  public void callInstanceMethodReflectively_rethrowsUncheckedException() {
    ExampleDescendant example = new ExampleDescendant();
    try {
      ReflectionHelpers.callInstanceMethodReflectively(example, "throwUncheckedException");
      Assertions.failBecauseExceptionWasNotThrown(TestRuntimeException.class);
    } catch (TestRuntimeException e) {
    } catch (RuntimeException e) {
      Assertions.fail("Unexpected exception thrown", e);
    }
  }

  @Test
  public void callInstanceMethodReflectively_rethrowsError() {
    ExampleDescendant example = new ExampleDescendant();
    try {
      ReflectionHelpers.callInstanceMethodReflectively(example, "throwError");
      Assertions.failBecauseExceptionWasNotThrown(TestError.class);
    } catch (RuntimeException e) {
      Assertions.fail("Unexpected exception thrown", e);
    } catch (TestError e) {
    }
  }

  @Test
  public void callInstanceMethodReflectively_wrapsCheckedException() {
    ExampleDescendant example = new ExampleDescendant();
    try {
      ReflectionHelpers.callInstanceMethodReflectively(example, "throwCheckedException");
      Assertions.failBecauseExceptionWasNotThrown(RuntimeException.class);
    } catch (RuntimeException e) {
      assertThat(e.getCause()).isInstanceOf(TestException.class);
    }
  }

  @Test
  public void callStaticMethodReflectively_callsPrivateStaticMethodsReflectively() {
    assertThat(ReflectionHelpers.callStaticMethodReflectively(ExampleDescendant.class, "getConstantNumber")).isEqualTo(1);
  }

  @Test
  public void callStaticMethodReflectively_rethrowsUncheckedException() {
    try {
      ReflectionHelpers.callStaticMethodReflectively(ExampleDescendant.class, "staticThrowUncheckedException");
      Assertions.failBecauseExceptionWasNotThrown(TestRuntimeException.class);
    } catch (TestRuntimeException e) {
    } catch (RuntimeException e) {
      Assertions.fail("Unexpected exception thrown", e);
    }
  }

  @Test
  public void callStaticMethodReflectively_rethrowsError() {
    try {
      ReflectionHelpers.callStaticMethodReflectively(ExampleDescendant.class, "staticThrowError");
      Assertions.failBecauseExceptionWasNotThrown(TestError.class);
    } catch (RuntimeException e) {
      Assertions.fail("Unexpected exception thrown", e);
    } catch (TestError e) {
    }
  }

  @Test
  public void callStaticMethodReflectively_wrapsCheckedException() {
    try {
      ReflectionHelpers.callStaticMethodReflectively(ExampleDescendant.class, "staticThrowCheckedException");
      Assertions.failBecauseExceptionWasNotThrown(RuntimeException.class);
    } catch (RuntimeException e) {
      assertThat(e.getCause()).isInstanceOf(TestException.class);
    }
  }

  @Test
  public void callConstructorReflectively_callsPrivateConstructors() {
    ExampleClass e = ReflectionHelpers.callConstructorReflectively(ExampleClass.class);
    assertThat(e).isNotNull();
  }

  @Test
  public void callConstructorReflectively_rethrowsUncheckedException() {
    try {
      ReflectionHelpers.callConstructorReflectively(ThrowsUncheckedException.class);
      Assertions.failBecauseExceptionWasNotThrown(TestRuntimeException.class);
    } catch (TestRuntimeException e) {
    } catch (RuntimeException e) {
      Assertions.fail("Unexpected exception thrown", e);
    }
  }

  @Test
  public void callConstructorReflectively_rethrowsError() {
    try {
      ReflectionHelpers.callConstructorReflectively(ThrowsError.class);
      Assertions.failBecauseExceptionWasNotThrown(TestError.class);
    } catch (RuntimeException e) {
      Assertions.fail("Unexpected exception thrown", e);
    } catch (TestError e) {
    }
  }

  @Test
  public void callConstructorReflectively_wrapsCheckedException() {
    try {
      ReflectionHelpers.callConstructorReflectively(ThrowsCheckedException.class);
      Assertions.failBecauseExceptionWasNotThrown(RuntimeException.class);
    } catch (RuntimeException e) {
      assertThat(e.getCause()).isInstanceOf(TestException.class);
    }
  }

  @Test
  public void singleArgFrom_unboxes_primitives() {
    List<ClassParameter<?>> expected = Lists.newArrayList(new ClassParameter<?>[] {
        from(boolean.class, true),
        from(char.class, 'a'),
        from(byte.class, (byte)1),
        from(short.class, (short)2),
        from(int.class, 3),
        from(long.class, 4L),
        from(float.class, 5.0f),
        from(double.class, 6.0)
    });
    List<ClassParameter<?>> actual = Lists.newArrayList(new ClassParameter<?>[] {
       from(true),
       from('a'),
       from((byte)1),
       from((short)2),
       from(3),
       from(4L),
       from(5.0f),
       from(6.0)
    });
    for (int i = 0; i < actual.size(); i++) {
      ClassParameter<?> cpA = actual.get(i);
      ClassParameter<?> cpE = expected.get(i);
      assertThat(cpA.clazz).as("clazz").isEqualTo(cpE.clazz);
      assertThat(cpA.val).as("val").isEqualTo(cpE.val);
    }
  }
  
  @Test
  public void singleArgFrom_handlesNonPrimitives() {
    String param = "Hi there";
    ClassParameter<String> cpA = from(param);
    assertThat(cpA.clazz).as("clazz").isEqualTo(String.class);
    assertThat(cpA.val).as("val").isSameAs(param);
  }
  
  @Test
  public void callConstructorReflectively_whenMultipleSignaturesExistForTheConstructor_callsConstructorWithCorrectSignature() {
    ExampleClass ec = ReflectionHelpers.callConstructorReflectively(ExampleClass.class, ClassParameter.from(16));
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

    private static final int BASE = new Integer(8);

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
