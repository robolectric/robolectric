package org.robolectric.util;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

public class ReflectionHelpersTest {

  @Test
  public void getFieldReflectively_getsPrivateFields() {
    ExampleDescendant example = new ExampleDescendant();
    example.overridden = 5;
    Assert.assertEquals(ReflectionHelpers.getFieldReflectively(example, "overridden"), 5);
  }

  @Test
  public void getFieldReflectively_getsInheritedFields() {
    ExampleDescendant example = new ExampleDescendant();
    example.setNotOverridden(6);
    Assert.assertEquals(ReflectionHelpers.getFieldReflectively(example, "notOverridden"), 6);
  }

  @Test
  public void getFieldReflectively_givesHelpfulExceptions() {
    ExampleDescendant example = new ExampleDescendant();
    try {
      ReflectionHelpers.getFieldReflectively(example, "nonExistant");
      Assert.fail("no Exception thrown");
    } catch (RuntimeException e) {
      if (!e.getMessage().contains("nonExistant")) {
        Assert.fail("poorly specified exception thrown: " + e.getMessage());
      }
    }
  }

  @Test
  public void setFieldReflectively_setsPrivateFields() {
    ExampleDescendant example = new ExampleDescendant();
    example.overridden = 5;
    ReflectionHelpers.setFieldReflectively(example, "overridden", 10);
    Assert.assertEquals(example.overridden, 10);
  }

  @Test
  public void setFieldReflectively_setsInheritedFields() {
    ExampleDescendant example = new ExampleDescendant();
    example.setNotOverridden(5);
    ReflectionHelpers.setFieldReflectively(example, "notOverridden", 10);
    Assert.assertEquals(example.getNotOverridden(), 10);
  }

  @Test
  public void setFieldReflectively_givesHelpfulExceptions() {
    ExampleDescendant example = new ExampleDescendant();
    try {
      ReflectionHelpers.setFieldReflectively(example, "nonExistant", 6);
      Assert.fail("no Exception thrown");
    } catch (RuntimeException e) {
      if (!e.getMessage().contains("nonExistant")) {
        Assert.fail("poorly specified exception thrown: " + e.getMessage());
      }
    }
  }

  @Test
  public void getStaticFieldReflectively_withField_getsStaticField() throws Exception {
    Field field = ExampleDescendant.class.getDeclaredField("DESCENDANT");

    int result = ReflectionHelpers.getStaticFieldReflectively(field);
    Assert.assertEquals(result, 6);
  }

  @Test
  public void getStaticFieldReflectively_withFieldName_getsStaticField() {
    Assert.assertEquals(ReflectionHelpers.getStaticFieldReflectively(ExampleDescendant.class, "DESCENDANT"), 6);
  }

  @Test
  public void setStaticFieldReflectively_withField_setsStaticFields() throws Exception {
    Field field = ExampleDescendant.class.getDeclaredField("DESCENDANT");
    int startingValue = ReflectionHelpers.getStaticFieldReflectively(field);

    ReflectionHelpers.setStaticFieldReflectively(field, 7);
    Assert.assertEquals(startingValue, 6);
    Assert.assertEquals(ExampleDescendant.DESCENDANT, 7);

    // Reset the value to avoid test pollution
    ReflectionHelpers.setStaticFieldReflectively(field, startingValue);
  }

  @Test
  public void setStaticFieldReflectively_withFieldName_setsStaticFields() {
    int startingValue = ReflectionHelpers.getStaticFieldReflectively(ExampleDescendant.class, "DESCENDANT");

    ReflectionHelpers.setStaticFieldReflectively(ExampleDescendant.class, "DESCENDANT", 7);
    Assert.assertEquals(startingValue, 6);
    Assert.assertEquals(ExampleDescendant.DESCENDANT, 7);

    // Reset the value to avoid test pollution
    ReflectionHelpers.setStaticFieldReflectively(ExampleDescendant.class, "DESCENDANT", startingValue);
  }

  @Test
  public void callInstanceMethodReflectively_callsPrivateMethods() {
    ExampleDescendant example = new ExampleDescendant();
    Assert.assertEquals(ReflectionHelpers.callInstanceMethodReflectively(example, "returnNumber"), 1337);
  }

  @Test
  public void callInstanceMethodReflectively_whenMultipleSignaturesExistForAMethodName_callsMethodWithCorrectSignature() {
    ExampleDescendant example = new ExampleDescendant();
    Assert.assertEquals(ReflectionHelpers.callInstanceMethodReflectively(example, "returnNumber", ClassParameter.from(int.class, 5)), 5);
  }

  @Test
  public void callInstanceMethodReflectively_callsInheritedMethods() {
    ExampleDescendant example = new ExampleDescendant();
    Assert.assertEquals(ReflectionHelpers.callInstanceMethodReflectively(example, "returnNegativeNumber"), -46);
  }

  @Test
  public void callInstanceMethodReflectively_givesHelpfulExceptions() {
    ExampleDescendant example = new ExampleDescendant();
    try {
      ReflectionHelpers.callInstanceMethodReflectively(example, "nonExistant");
      Assert.fail("no Exception thrown");
    } catch (RuntimeException e) {
      if (!e.getMessage().contains("nonExistant")) {
        Assert.fail("poorly specified exception thrown: " + e.getMessage());
      }
    }
  }

  @Test
  public void callInstanceMethodReflectively_rethrowsUncheckedException() {
    ExampleDescendant example = new ExampleDescendant();
    try {
      ReflectionHelpers.callInstanceMethodReflectively(example, "throwUncheckedException");
      Assert.fail("no Exception thrown");
    } catch (TestRuntimeException e) {
    } catch (RuntimeException e) {
      Assert.fail("Unexpected exception thrown: " + e.getMessage());
    }
  }

  @Test
  public void callInstanceMethodReflectively_rethrowsError() {
    ExampleDescendant example = new ExampleDescendant();
    try {
      ReflectionHelpers.callInstanceMethodReflectively(example, "throwError");
      Assert.fail("no Exception thrown");
    } catch (RuntimeException e) {
      Assert.fail("Unexpected exception thrown: " + e.getMessage());
    } catch (TestError e) {
    }
  }

  @Test
  public void callInstanceMethodReflectively_wrapsCheckedException() {
    ExampleDescendant example = new ExampleDescendant();
    try {
      ReflectionHelpers.callInstanceMethodReflectively(example, "throwCheckedException");
      Assert.fail("no Exception thrown");
    } catch (RuntimeException e) {
      assertThat(e.getCause()).isInstanceOf(TestException.class);
    }
  }

  @Test
  public void callStaticMethodReflectively_callsPrivateStaticMethodsReflectively() {
    Assert.assertEquals(ReflectionHelpers.callStaticMethodReflectively(ExampleDescendant.class, "getConstantNumber"), 1);
  }

  @Test
  public void callStaticMethodReflectively_rethrowsUncheckedException() {
    try {
      ReflectionHelpers.callStaticMethodReflectively(ExampleDescendant.class, "staticThrowUncheckedException");
      Assert.fail("no Exception thrown");
    } catch (TestRuntimeException e) {
    } catch (RuntimeException e) {
      Assert.fail("Unexpected exception thrown: " + e.getMessage());
    }
  }

  @Test
  public void callStaticMethodReflectively_rethrowsError() {
    try {
      ReflectionHelpers.callStaticMethodReflectively(ExampleDescendant.class, "staticThrowError");
      Assert.fail("no Exception thrown");
    } catch (RuntimeException e) {
      Assert.fail("Unexpected exception thrown: " + e.getMessage());
    } catch (TestError e) {
    }
  }

  @Test
  public void callStaticMethodReflectively_wrapsCheckedException() {
    try {
      ReflectionHelpers.callStaticMethodReflectively(ExampleDescendant.class, "staticThrowCheckedException");
      Assert.fail("no Exception thrown");
    } catch (RuntimeException e) {
      assertThat(e.getCause()).isInstanceOf(TestException.class);
    }
  }

  @Test
  public void callConstructorReflectively_callsPrivateConstructors() {
    Object o = ReflectionHelpers.callConstructorReflectively(ExampleClass.class);
    // This test might be redundant. The class is guaranteed by the generic type constraints.
    // In fact, you can write ExampleClass e = ReflectionHelpers.callConstructorReflectively(ExampleClass.class)
    // and it will compile. I guess this is a test that we're not creating heap pollution?
    Assert.assertTrue(ExampleClass.class.isInstance(o));
  }

  @Test
  public void callConstructorReflectively_rethrowsUncheckedException() {
    try {
      ReflectionHelpers.callConstructorReflectively(ThrowsUncheckedException.class);
      Assert.fail("no Exception thrown");
    } catch (TestRuntimeException e) {
    } catch (RuntimeException e) {
      Assert.fail("Unexpected exception thrown: " + e.getMessage());
    }
  }

  @Test
  public void callConstructorReflectively_rethrowsError() {
    try {
      ReflectionHelpers.callConstructorReflectively(ThrowsError.class);
      Assert.fail("no Exception thrown");
    } catch (RuntimeException e) {
      Assert.fail("Unexpected exception thrown: " + e.getMessage());
    } catch (TestError e) {
    }
  }

  @Test
  public void callConstructorReflectively_wrapsCheckedException() {
    try {
      ReflectionHelpers.callConstructorReflectively(ThrowsCheckedException.class);
      Assert.fail("no Exception thrown");
    } catch (RuntimeException e) {
      assertThat(e.getCause()).isInstanceOf(TestException.class);
    }
  }

  @Test
  public void callConstructorReflectively_whenMultipleSignaturesExistForTheConstructor_callsConstructorWithCorrectSignature() {
    ExampleClass ec = ReflectionHelpers.callConstructorReflectively(ExampleClass.class, ClassParameter.from(int.class, 16));
    Assert.assertEquals(ec.index, 16);
    Assert.assertNull(ec.name);
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
    public ThrowsCheckedException() throws TestException {
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