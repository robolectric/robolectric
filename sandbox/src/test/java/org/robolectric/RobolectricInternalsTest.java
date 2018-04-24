package org.robolectric;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.internal.Instrument;
import org.robolectric.internal.SandboxTestRunner;
import org.robolectric.internal.bytecode.SandboxConfig;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@SandboxConfig(shadows={ RobolectricInternalsTest.ShadowConstructors.class })
@RunWith(SandboxTestRunner.class)
public class RobolectricInternalsTest {

  private static final String PARAM1 = "param1";
  private static final Byte PARAM2 = (byte) 24;
  private static final Long PARAM3 = (long) 10122345;
  
  @Test
  public void getConstructor_withNoParams() {
    Constructors a = new Constructors();
    ShadowConstructors sa = shadowOf(a);

    assertThat(a.constructorCalled).isFalse();
    assertThat(sa.shadowConstructorCalled).isTrue();

    Shadow.invokeConstructor(Constructors.class, a);
    assertThat(a.constructorCalled).isTrue();
  }

  @Test
  public void getConstructor_withOneClassParam() {
    Constructors a = new Constructors(PARAM1);
    ShadowConstructors sa = shadowOf(a);

    assertThat(a.param11).isNull();
    assertThat(sa.shadowParam11).isEqualTo(PARAM1);

    Shadow.invokeConstructor(Constructors.class, a, ClassParameter.from(String.class, PARAM1));
    assertThat(a.param11).isEqualTo(PARAM1);
  }

  @Test
  public void getConstructor_withTwoClassParams() {
    Constructors a = new Constructors(PARAM1, PARAM2);
    ShadowConstructors sa = shadowOf(a);

    assertThat(a.param21).isNull();
    assertThat(a.param22).isNull();
    assertThat(sa.shadowParam21).isEqualTo(PARAM1);
    assertThat(sa.shadowParam22).isEqualTo(PARAM2);

    Shadow.invokeConstructor(Constructors.class, a, ClassParameter.from(String.class, PARAM1), ClassParameter.from(Byte.class, PARAM2));
    assertThat(a.param21).isEqualTo(PARAM1);
    assertThat(a.param22).isEqualTo(PARAM2);
  }

  @Test
  public void getConstructor_withThreeClassParams() {
    Constructors a = new Constructors(PARAM1, PARAM2, PARAM3);
    ShadowConstructors sa = shadowOf(a);

    assertThat(a.param31).isNull();
    assertThat(a.param32).isNull();
    assertThat(a.param33).isNull();
    assertThat(sa.shadowParam31).isEqualTo(PARAM1);
    assertThat(sa.shadowParam32).isEqualTo(PARAM2);
    assertThat(sa.shadowParam33).isEqualTo(PARAM3);
    
    Shadow.invokeConstructor(Constructors.class, a, ClassParameter.from(String.class, PARAM1), ClassParameter.from(Byte.class, PARAM2), ClassParameter.from(Long.class, PARAM3));
    assertThat(a.param31).isEqualTo(PARAM1);
    assertThat(a.param32).isEqualTo(PARAM2);
    assertThat(a.param33).isEqualTo(PARAM3);
  }

  private static ShadowConstructors shadowOf(Constructors realObject) {
    Object shadow = Shadow.extract(realObject);
    assertThat(shadow).isInstanceOf(ShadowConstructors.class);
    return (ShadowConstructors) shadow;
  }

  @Instrument
  public static class Constructors {
    public boolean constructorCalled = false;
    public String param11 = null;

    public String param21 = null;
    public Byte   param22 = null;
    
    public String param31 = null;
    public Byte   param32 = null;
    public Long   param33 = null;
    
    public Constructors() {
      constructorCalled = true;
    }
    
    public Constructors(String param) {
      param11 = param;
    }
    
    public Constructors(String param1, Byte param2) {
      param21 = param1;
      param22 = param2;
    }

    public Constructors(String param1, Byte param2, Long param3) {
      param31 = param1;
      param32 = param2;
      param33 = param3;
    }
  }
  
  @Implements(Constructors.class)
  public static class ShadowConstructors {
    public boolean shadowConstructorCalled = false;
    public String shadowParam11 = null;

    public String shadowParam21 = null;
    public Byte   shadowParam22 = null;
    
    public String shadowParam31 = null;
    public Byte   shadowParam32 = null;
    public Long   shadowParam33 = null;
    
    @Implementation
    protected void __constructor__() {
      shadowConstructorCalled = true;
    }

    @Implementation
    protected void __constructor__(String param) {
      shadowParam11 = param;
    }

    @Implementation
    protected void __constructor__(String param1, Byte param2) {
      shadowParam21 = param1;
      shadowParam22 = param2;
    }

    @Implementation
    protected void __constructor__(String param1, Byte param2, Long param3) {
      shadowParam31 = param1;
      shadowParam32 = param2;
      shadowParam33 = param3;
    }
  }
}
