package org.robolectric.android.interceptors;

import static java.lang.invoke.MethodType.methodType;

import com.google.auto.service.AutoService;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Locale;
import org.robolectric.internal.bytecode.Interceptor;
import org.robolectric.internal.bytecode.MethodRef;
import org.robolectric.internal.bytecode.MethodSignature;
import org.robolectric.util.Function;

@AutoService(Interceptor.class)
@SuppressWarnings("NewApi")
public class LocaleAdjustLanguageCodeInterceptor extends Interceptor {

  private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

  public LocaleAdjustLanguageCodeInterceptor() {
    super(new MethodRef(Locale.class, "adjustLanguageCode"));
  }

  static String adjustLanguageCode(String languageCode) {
    String adjusted = languageCode.toLowerCase(Locale.US);
    // Map new language codes to the obsolete language
    // codes so the correct resource bundles will be used.
    if (languageCode.equals("he")) {
      adjusted = "iw";
    } else if (languageCode.equals("id")) {
      adjusted = "in";
    } else if (languageCode.equals("yi")) {
      adjusted = "ji";
    }

    return adjusted;
  }

  @Override
  public Function<Object, Object> handle(MethodSignature methodSignature) {
    return (theClass, value, params) -> adjustLanguageCode((String) params[0]);
  }

  @Override
  public MethodHandle getMethodHandle(String methodName, MethodType type)
      throws NoSuchMethodException, IllegalAccessException {
    return lookup.findStatic(getClass(), "adjustLanguageCode",
        methodType(String.class, String.class));
  }
}
