package org.robolectric.android.interceptors;

import static java.lang.invoke.MethodType.methodType;

import com.google.auto.service.AutoService;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.LinkedHashMap;
import javax.annotation.Nullable;
import org.robolectric.internal.bytecode.Interceptor;
import org.robolectric.internal.bytecode.MethodRef;
import org.robolectric.internal.bytecode.MethodSignature;
import org.robolectric.util.Function;

//  @Intercept(value = LinkedHashMap.class, method = "eldest")
@AutoService(Interceptor.class)
@SuppressWarnings("NewApi")
public class LinkedHashMapEldestInterceptor extends Interceptor {

  private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

  public LinkedHashMapEldestInterceptor() {
    super(new MethodRef(LinkedHashMap.class, "eldest"));
  }

  @Nullable
  static Object eldest(LinkedHashMap map) {
    return map.isEmpty() ? null : map.entrySet().iterator().next();
  }

  @Override
  public Function<Object, Object> handle(MethodSignature methodSignature) {
    return (theClass, value, params) -> eldest((LinkedHashMap) value);
  }

  @Override
  public MethodHandle getMethodHandle(String methodName, MethodType type)
      throws NoSuchMethodException, IllegalAccessException {
    return lookup.findStatic(getClass(), "eldest",
        methodType(Object.class, LinkedHashMap.class));
  }
}
