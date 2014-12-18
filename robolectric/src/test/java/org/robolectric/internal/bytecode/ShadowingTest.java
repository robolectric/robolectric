package org.robolectric.internal.bytecode;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.bytecode.testing.Pony;
import org.robolectric.annotation.internal.Instrument;
import org.robolectric.internal.ShadowConstants;
import org.robolectric.internal.Shadow;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowingTest {

  @Test
  @Config(shadows = {ShadowAccountManagerForTests.class})
  public void testStaticMethodsAreDelegated() throws Exception {
    Context context = mock(Context.class);
    AccountManager.get(context);
    assertThat(ShadowAccountManagerForTests.wasCalled).isTrue();
    assertThat(ShadowAccountManagerForTests.context).isSameAs(context);
  }

  @Implements(AccountManager.class)
  public static class ShadowAccountManagerForTests {
    public static boolean wasCalled = false;
    public static Context context;

    public static AccountManager get(Context context) {
      wasCalled = true;
      ShadowAccountManagerForTests.context = context;
      return mock(AccountManager.class);
    }
  }

  @Test
  @Config(shadows = {ShadowClassWithProtectedMethod.class})
  public void testProtectedMethodsAreDelegated() throws Exception {
    ClassWithProtectedMethod overlay = new ClassWithProtectedMethod();
    assertEquals("shadow name", overlay.getName());
  }

  @Implements(ClassWithProtectedMethod.class)
  public static class ShadowClassWithProtectedMethod {
    @Implementation
    public String getName() {
      return "shadow name";
    }
  }

  @Instrument
  public static class ClassWithProtectedMethod {
    protected String getName() {
      return "protected name";
    }
  }

  @Test
  @Config(shadows = {ShadowPaintForTests.class})
  public void testNativeMethodsAreDelegated() throws Exception {
    Paint paint = new Paint();
    paint.setColor(1234);

    assertThat(paint.getColor()).isEqualTo(1234);
  }

  @Implements(Paint.class)
  public static class ShadowPaintForTests {
    private int color;

    @Implementation
    public void setColor(int color) {
      this.color = color;
    }

    @Implementation
    public int getColor() {
      return color;
    }
  }

  @Test
  public void testPrintlnWorks() throws Exception {
    Log.println(1, "tag", "msg");
  }

  @Implements(ClassWithNoDefaultConstructor.class)
  public static class ShadowForClassWithNoDefaultConstructor {
    public static boolean shadowDefaultConstructorCalled = false;
    public static boolean shadowDefaultConstructorImplementorCalled = false;

    public ShadowForClassWithNoDefaultConstructor() {
      this.shadowDefaultConstructorCalled = true;
    }

    public void __constructor__() {
      shadowDefaultConstructorImplementorCalled = true;
    }
  }

  @Instrument @SuppressWarnings({"UnusedDeclaration"})
  public static class ClassWithNoDefaultConstructor {
    ClassWithNoDefaultConstructor(String string) {
    }
  }

  @Test
  @Config(shadows = {Pony.ShadowPony.class})
  public void directlyOn_shouldCallThroughToOriginalMethodBody() throws Exception {
    Pony pony = new Pony();

    assertEquals("Fake whinny! You're on my neck!", pony.ride("neck"));
    assertEquals("Whinny! You're on my neck!", Shadow.directlyOn(pony, Pony.class).ride("neck"));

    assertEquals("Fake whinny! You're on my haunches!", pony.ride("haunches"));
  }

  @Test
  @Config(shadows = {Pony.ShadowPony.class})
  public void shouldCallRealForUnshadowedMethod() throws Exception {
    assertEquals("Off I saunter to the salon!", new Pony().saunter("the salon"));
  }

  @Implements(TextView.class)
  public static class TextViewWithDummyGetTextColorsMethod {
    public static ColorStateList getTextColors(Context context, TypedArray attrs) {
      return new ColorStateList(new int[0][0], new int[0]);
    }
  }

  @Test
  @Config(shadows = ShadowOfClassWithSomeConstructors.class)
  public void shouldGenerateSeparatedConstructorBodies() throws Exception {
    ClassWithSomeConstructors o = new ClassWithSomeConstructors("my name");
    assertNull(o.name);

    Method realConstructor = o.getClass().getDeclaredMethod(ShadowConstants.CONSTRUCTOR_METHOD_NAME, String.class);
    realConstructor.setAccessible(true);
    realConstructor.invoke(o, "my name");
    assertEquals("my name", o.name);
  }

  @Instrument
  public static class ClassWithSomeConstructors {
    public String name;

    public ClassWithSomeConstructors(String name) {
      this.name = name;
    }
  }

  @Implements(ClassWithSomeConstructors.class)
  public static class ShadowOfClassWithSomeConstructors {
    @SuppressWarnings("UnusedDeclaration")
    public void __constructor__(String s) {
    }
  }

  @Test
  public void shouldDelegateToObjectToStringIfShadowHasNone() throws Exception {
    assertThat(new Toast(RuntimeEnvironment.application).toString()).startsWith("android.widget.Toast@");
  }

  @Test
  public void shouldDelegateToObjectHashCodeIfShadowHasNone() throws Exception {
    assertFalse(new View(RuntimeEnvironment.application).hashCode() == 0);
  }

  @Test
  public void shouldDelegateToObjectEqualsIfShadowHasNone() throws Exception {
    View view = new View(RuntimeEnvironment.application);
    assertEquals(view, view);
  }

  @Test
  @Config(shadows = {ShadowApiImplementedClass.class})
  public void withNonApiSubclassesWhichExtendApi_shouldStillBeInvoked() throws Exception {
    assertEquals("did foo", new NonApiSubclass().doSomething("foo"));
  }

  public static class NonApiSubclass extends ApiImplementedClass {
    public String doSomething(String value) {
      return "did " + value;
    }
  }

  @Instrument
  public static class ApiImplementedClass {
  }

  @Implements(ApiImplementedClass.class)
  public static class ShadowApiImplementedClass {
  }
}
