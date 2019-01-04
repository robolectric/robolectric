package org.robolectric;

import java.lang.reflect.Method;
import org.robolectric.annotation.Config;

public interface ConfigMerger {

  Config getConfig(Class<?> testClass, Method method, Config globalConfig);
}
