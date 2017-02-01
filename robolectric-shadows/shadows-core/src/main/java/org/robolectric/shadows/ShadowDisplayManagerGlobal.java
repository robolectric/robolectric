package org.robolectric.shadows;

import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.IDisplayManager;
import android.view.Display;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.RuntimeEnvironment.getApiLevel;

/**
 * Shadow for {@link android.hardware.display.DisplayManagerGlobal}.
 */
@Implements(value = DisplayManagerGlobal.class, isInAndroidSdk = false, minSdk = JELLY_BEAN_MR1)
public class ShadowDisplayManagerGlobal {
  private static final IDisplayManager displayManager = (IDisplayManager) Proxy.newProxyInstance(IDisplayManager.class.getClassLoader(),
      new Class[]{IDisplayManager.class}, new InvocationHandler() {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
          System.out.println("proxy = " + method.getName());
          switch (method.getName()) {
            case "getDisplayIds":
              return new int[]{1234};
          }
          return ReflectionHelpers.PRIMITIVE_RETURN_VALUES.get(method.getReturnType().getName());
        }
      });

  @Implementation
  public static Object getInstance() {
    return ReflectionHelpers.callConstructor(DisplayManagerGlobal.class,
        ClassParameter.from(IDisplayManager.class, displayManager));
  }

  @Implementation
  public Object getDisplayInfo(int displayId) {
    Object result = Shadow.newInstanceOf("android.view.DisplayInfo");
    if (getApiLevel() >= M) {
      ReflectionHelpers.setField(result, "supportedModes", new Display.Mode[]{new Display.Mode(0, 0, 0, 0.0f)});
    }
    return result;
  }
}