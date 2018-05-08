package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.android.AndroidInterceptors;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.Function;

@SuppressWarnings("unchecked")
@RunWith(JUnit4.class)
public class ShadowWranglerUnitTest {
  private ShadowWrangler shadowWrangler;
  private Interceptors interceptors;

  @Before
  public void setup() throws Exception {
    interceptors = new Interceptors(AndroidInterceptors.all());
    shadowWrangler = new ShadowWrangler(ShadowMap.EMPTY, 23, interceptors);
  }

  @Test
  public void getInterceptionHandler_whenCallIsNotRecognized_shouldReturnDoNothingHandler() throws Exception {
    MethodSignature methodSignature = MethodSignature.parse("java/lang/Object/unknownMethod()V");
    Function<Object,Object> handler = interceptors.getInterceptionHandler(methodSignature);

    assertThat(handler.call(null, null, new Object[0])).isNull();
  }

  @Test
  public void getInterceptionHandler_whenInterceptingElderOnLinkedHashMap_shouldReturnNonDoNothingHandler() throws Exception {
    MethodSignature methodSignature = MethodSignature.parse("java/util/LinkedHashMap/eldest()Ljava/lang/Object;");
    Function<Object,Object> handler = interceptors.getInterceptionHandler(methodSignature);

    assertThat(handler).isNotSameAs(ShadowWrangler.DO_NOTHING_HANDLER);
  }

  @Test
  public void intercept_elderOnLinkedHashMapHandler_shouldReturnEldestMemberOfLinkedHashMap() throws Throwable {
    LinkedHashMap<Integer, String> map = new LinkedHashMap<>(2);
    map.put(1, "one");
    map.put(2, "two");

    Map.Entry<Integer, String> result = (Map.Entry<Integer, String>)
        shadowWrangler.intercept("java/util/LinkedHashMap/eldest()Ljava/lang/Object;", map, null, getClass());

    Map.Entry<Integer, String> eldestMember = map.entrySet().iterator().next();
    assertThat(result).isEqualTo(eldestMember);
    assertThat(result.getKey()).isEqualTo(1);
    assertThat(result.getValue()).isEqualTo("one");
  }

  @Test
  public void intercept_elderOnLinkedHashMapHandler_shouldReturnNullForEmptyMap() throws Throwable {
    LinkedHashMap<Integer, String> map = new LinkedHashMap<>();

    Map.Entry<Integer, String> result = (Map.Entry<Integer, String>)
        shadowWrangler.intercept("java/util/LinkedHashMap/eldest()Ljava/lang/Object;", map, null, getClass());

    assertThat(result).isNull();
  }

  @Test
  public void shadowClassWithSdkRange() throws Throwable {
    ShadowMap shadowMap = new ShadowMap.Builder().addShadowClasses(ShadowDummyClass.class).build();
    String methodName = internalName(DummyClass.class) + "/methodWithoutRange()V";
    assertThat(new ShadowWrangler(shadowMap, 18, interceptors).methodInvoked(methodName, false, DummyClass.class)).isNull();
    assertThat(new ShadowWrangler(shadowMap, 19, interceptors).methodInvoked(methodName, false, DummyClass.class).describe())
        .contains("ShadowDummyClass.methodWithoutRange()");
    assertThat(new ShadowWrangler(shadowMap, 23, interceptors).methodInvoked(methodName, false, DummyClass.class)).isNull();
  }

  @Test
  public void shadowMethodWithSdkRange() throws Throwable {
    ShadowMap shadowMap = new ShadowMap.Builder().addShadowClasses(ShadowDummyClass.class).build();
    String methodName = internalName(DummyClass.class) + "/methodFor20()V";
    assertThat(new ShadowWrangler(shadowMap, 19, interceptors).methodInvoked(methodName, false, DummyClass.class)).isNull();
    assertThat(new ShadowWrangler(shadowMap, 20, interceptors).methodInvoked(methodName, false, DummyClass.class).describe())
        .contains("ShadowDummyClass.methodFor20()");
    assertThat(new ShadowWrangler(shadowMap, 21, interceptors).methodInvoked(methodName, false, DummyClass.class)).isNull();
  }

  @Test
  public void shadowMethodWithMinSdk() throws Throwable {
    ShadowMap shadowMap = new ShadowMap.Builder().addShadowClasses(ShadowDummyClass.class).build();
    String methodName = internalName(DummyClass.class) + "/methodMin20()V";
    assertThat(new ShadowWrangler(shadowMap, 19, interceptors).methodInvoked(methodName, false, DummyClass.class)).isNull();
    assertThat(new ShadowWrangler(shadowMap, 20, interceptors).methodInvoked(methodName, false, DummyClass.class).describe())
        .contains("ShadowDummyClass.methodMin20()");
    assertThat(new ShadowWrangler(shadowMap, 21, interceptors).methodInvoked(methodName, false, DummyClass.class).describe())
        .contains("ShadowDummyClass.methodMin20()");
  }

  @Test
  public void shadowMethodWithMaxSdk() throws Throwable {
    ShadowMap shadowMap = new ShadowMap.Builder().addShadowClasses(ShadowDummyClass.class).build();
    String methodName = internalName(DummyClass.class) + "/methodMax20()V";
    assertThat(new ShadowWrangler(shadowMap, 19, interceptors).methodInvoked(methodName, false, DummyClass.class).describe())
        .contains("ShadowDummyClass.methodMax20()");
    assertThat(new ShadowWrangler(shadowMap, 20, interceptors).methodInvoked(methodName, false, DummyClass.class).describe())
        .contains("ShadowDummyClass.methodMax20()");
    assertThat(new ShadowWrangler(shadowMap, 21, interceptors).methodInvoked(methodName, false, DummyClass.class)).isNull();
  }

  @Test
  public void shadowConstructor() throws Throwable {
    ShadowMap shadowMap = new ShadowMap.Builder().addShadowClasses(ShadowDummyClass.class).build();
    String methodName = internalName(DummyClass.class) + "/__constructor__()V";
    assertThat(new ShadowWrangler(shadowMap, 19, interceptors).methodInvoked(methodName, false, DummyClass.class)).isNull();
    assertThat(new ShadowWrangler(shadowMap, 20, interceptors).methodInvoked(methodName, false, DummyClass.class).describe())
        .contains("ShadowDummyClass.__constructor__()");
    assertThat(new ShadowWrangler(shadowMap, 21, interceptors).methodInvoked(methodName, false, DummyClass.class)).isNull();
  }

  @Test
  public void whenChildShadowHasNarrowerSdk_createShadowFor_shouldReturnSuperShadowSometimes() throws Exception {
    ShadowMap shadowMap = new ShadowMap.Builder().addShadowClasses(ShadowDummyClass.class, ShadowChildOfDummyClass.class).build();
    assertThat(new ShadowWrangler(shadowMap, 18, interceptors).createShadowFor(new ChildOfDummyClass()))
        .isSameAs(ShadowWrangler.NO_SHADOW);
    assertThat(new ShadowWrangler(shadowMap, 19, interceptors).createShadowFor(new ChildOfDummyClass()))
        .isInstanceOf(ShadowDummyClass.class);
    assertThat(new ShadowWrangler(shadowMap, 20, interceptors).createShadowFor(new ChildOfDummyClass()))
        .isInstanceOf(ShadowChildOfDummyClass.class);
    assertThat(new ShadowWrangler(shadowMap, 21, interceptors).createShadowFor(new ChildOfDummyClass()))
        .isInstanceOf(ShadowChildOfDummyClass.class);
    assertThat(new ShadowWrangler(shadowMap, 22, interceptors).createShadowFor(new ChildOfDummyClass()))
        .isSameAs(ShadowWrangler.NO_SHADOW);
  }

  @Test
  public void whenChildShadowHasNarrowerSdk_shouldCallAppropriateShadowMethod() throws Exception {
    ShadowMap shadowMap = new ShadowMap.Builder().addShadowClasses(ShadowDummyClass.class, ShadowChildOfDummyClass.class).build();
    String methodName = internalName(ChildOfDummyClass.class) + "/methodWithoutRange()V";
    assertThat(new ShadowWrangler(shadowMap, 19, interceptors).methodInvoked(methodName, false, ChildOfDummyClass.class))
        .isNull();
    assertThat(new ShadowWrangler(shadowMap, 20, interceptors).methodInvoked(methodName, false, ChildOfDummyClass.class).describe())
        .contains("ShadowChildOfDummyClass.methodWithoutRange()");
    assertThat(new ShadowWrangler(shadowMap, 21, interceptors).methodInvoked(methodName, false, ChildOfDummyClass.class).describe())
        .contains("ShadowChildOfDummyClass.methodWithoutRange()");
  }

  public static class DummyClass {
  }

  @Implements(value = DummyClass.class, minSdk = 19, maxSdk = 21)
  public static class ShadowDummyClass {
    @Implementation(minSdk = 20, maxSdk = 20)
    protected void __constructor__() {
    }
    
    @Implementation
    protected void methodWithoutRange() {
    }

    @Implementation(minSdk = 20, maxSdk = 20)
    protected void methodFor20() {
    }

    @Implementation(minSdk = 20)
    protected void methodMin20() {
    }

    @Implementation(maxSdk = 20)
    protected void methodMax20() {
    }
  }

  public static class ChildOfDummyClass extends DummyClass {
  }

  @Implements(value = ChildOfDummyClass.class, minSdk = 20, maxSdk = 21)
  public static class ShadowChildOfDummyClass {
    @Implementation
    protected void methodWithoutRange() {
    }
  }

  ///////////////////////

  private String internalName(Class clazz) {
    return clazz.getName().replaceAll("\\.", "/");
  }
}
