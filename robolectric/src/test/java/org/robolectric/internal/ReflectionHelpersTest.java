package org.robolectric.internal;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class ReflectionHelpersTest {

  @Test
  public void getFieldReflectively_getsPrivateFields() {
    ExampleDescendant example = new ExampleDescendant();
    example.overridden = 5;
    assertEquals(ReflectionHelpers.getFieldReflectively(example, "overridden"), 5);
  }

  @Test
  public void getFieldReflectively_getsInheritedFields() {
    ExampleDescendant example = new ExampleDescendant();
    example.setNotOverridden(6);
    assertEquals(ReflectionHelpers.getFieldReflectively(example, "notOverridden"), 6);
  }

  @Test
  public void getFieldReflectively_givesHelpfulExceptions() {
    ExampleDescendant example = new ExampleDescendant();
    try {
      ReflectionHelpers.getFieldReflectively(example, "nonExistant");
      fail("no Exception thrown");
    } catch (RuntimeException e) {
      if (!e.getMessage().contains("nonExistant")) {
        fail("poorly specified exception thrown: " + e.getMessage());
      }
    }
  }

  @Test
  public void setFieldReflectively_setsPrivateFields() {
    ExampleDescendant example = new ExampleDescendant();
    example.overridden = 5;
    ReflectionHelpers.setFieldReflectively(example, "overridden", 10);
    assertEquals(example.overridden, 10);
  }

  @Test
  public void setFieldReflectively_setsInheritedFields() {
    ExampleDescendant example = new ExampleDescendant();
    example.setNotOverridden(5);
    ReflectionHelpers.setFieldReflectively(example, "notOverridden", 10);
    assertEquals(example.getNotOverridden(), 10);
  }

  @Test
  public void setFieldReflectively_givesHelpfulExceptions() {
    ExampleDescendant example = new ExampleDescendant();
    try {
      ReflectionHelpers.setFieldReflectively(example, "nonExistant", 6);
      fail("no Exception thrown");
    } catch (RuntimeException e) {
      if (!e.getMessage().contains("nonExistant")) {
        fail("poorly specified exception thrown: " + e.getMessage());
      }
    }
  }

  @Test
  public void getStaticFieldReflectively_withField_getsStaticField() throws Exception {
    Field field = ExampleDescendant.class.getDeclaredField("DESCENDANT");

    int result = ReflectionHelpers.getStaticFieldReflectively(field);
    assertEquals(result, 6);
  }

  @Test
  public void getStaticFieldReflectively_withFieldName_getsStaticField() {
    assertEquals(ReflectionHelpers.getStaticFieldReflectively(ExampleDescendant.class, "DESCENDANT"), 6);
  }

  @Test
  public void setStaticFieldReflectively_withField_setsStaticFields() throws Exception {
    Field field = ExampleDescendant.class.getDeclaredField("DESCENDANT");
    int startingValue = ReflectionHelpers.getStaticFieldReflectively(field);

    ReflectionHelpers.setStaticFieldReflectively(field, 7);
    assertEquals(startingValue, 6);
    assertEquals(ExampleDescendant.DESCENDANT, 7);

    // Reset the value to avoid test pollution
    ReflectionHelpers.setStaticFieldReflectively(field, startingValue);
  }

  @Test
  public void setStaticFieldReflectively_withFieldName_setsStaticFields() {
    int startingValue = ReflectionHelpers.getStaticFieldReflectively(ExampleDescendant.class, "DESCENDANT");

    ReflectionHelpers.setStaticFieldReflectively(ExampleDescendant.class, "DESCENDANT", 7);
    assertEquals(startingValue, 6);
    assertEquals(ExampleDescendant.DESCENDANT, 7);

    // Reset the value to avoid test pollution
    ReflectionHelpers.setStaticFieldReflectively(ExampleDescendant.class, "DESCENDANT", startingValue);
  }

  @Test
  public void callInstanceMethodReflectively_callsPrivateMethods() {
    ExampleDescendant example = new ExampleDescendant();
    assertEquals(ReflectionHelpers.callInstanceMethodReflectively(example, "returnNumber"), 1337);
  }

  @Test
  public void callInstanceMethodReflectively_whenMultipleSignaturesExistForAMethodName_callsMethodWithCorrectSignature() {
    ExampleDescendant example = new ExampleDescendant();
    assertEquals(ReflectionHelpers.callInstanceMethodReflectively(example, "returnNumber", new ReflectionHelpers.ClassParameter(int.class, 5)), 5);
  }

  @Test
  public void callInstanceMethodReflectively_callsInheritedMethods() {
    ExampleDescendant example = new ExampleDescendant();
    assertEquals(ReflectionHelpers.callInstanceMethodReflectively(example, "returnNegativeNumber"), -46);
  }

  @Test
  public void callInstanceMethodReflectively_givesHelpfulExceptions() {
    ExampleDescendant example = new ExampleDescendant();
    try {
      ReflectionHelpers.callInstanceMethodReflectively(example, "nonExistant");
      fail("no Exception thrown");
    } catch (RuntimeException e) {
      if (!e.getMessage().contains("nonExistant")) {
        fail("poorly specified exception thrown: " + e.getMessage());
      }
    }
  }

  @Test
  public void callStaticMethodReflectively_callsPrivateStaticMethodsReflectively() {
    assertEquals(ReflectionHelpers.callStaticMethodReflectively(ExampleDescendant.class, "getConstantNumber"), 1);
  }

  @Test
  public void callConstructorReflectively_callsPrivateConstructors() {
    Object o = ReflectionHelpers.callConstructorReflectively(ExampleClass.class);
    assertTrue(ExampleClass.class.isInstance(o));
  }

  @Test
  public void callConstructorReflectively_whenMultipleSignaturesExistForTheConstructor_callsConstructorWithCorrectSignature() {
    ExampleClass ec = ReflectionHelpers.callConstructorReflectively(ExampleClass.class, new ReflectionHelpers.ClassParameter(int.class, 16));
    assertEquals(ec.index, 16);
    assertNull(ec.name);
  }

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