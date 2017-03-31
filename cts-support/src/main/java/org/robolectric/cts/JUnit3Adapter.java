package org.robolectric.cts;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.PerformanceCollector;
import android.test.AndroidTestCase;
import android.test.InstrumentationTestCase;
import android.test.PerformanceCollectorTestCase;
import junit.framework.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

public class JUnit3Adapter {
  @SuppressWarnings("unused")
  public void fixup(Object target) {
    final Context context = RuntimeEnvironment.application;

    if (target instanceof Test) {
      setContextIfAndroidTestCase((Test) target, context, context);
      setInstrumentationIfInstrumentationTestCase((Test) target, new Instrumentation() {
        @Override
        public Context getContext() {
          return context;
        }

        @Override
        public Context getTargetContext() {
          return context;
        }

        @Override
        public Activity startActivitySync(Intent intent) {
          intent = new Intent(intent);
          ActivityInfo ai = intent.resolveActivityInfo(this.getTargetContext().getPackageManager(), 0);
          if(ai == null) {
            throw new RuntimeException("Unable to resolve activity for: " + intent);
          } else {
            intent.setComponent(new ComponentName(ai.applicationInfo.packageName, ai.name));
            try {
              Class<? extends Activity> activityClass = intent.getClass().getClassLoader()
                  .loadClass(intent.getComponent().getClassName())
                  .asSubclass(Activity.class);
              return Robolectric.setupActivity(activityClass);
            } catch (ClassNotFoundException e) {
              throw new RuntimeException(e);
            }
          }
        }

        @Override
        public void runOnMainSync(Runnable runner) {
          runner.run();
        }

        @Override
        public void waitForIdleSync() {
        }

        @Override
        public void setInTouchMode(boolean inTouch) {
        }
      });
      setPerformanceWriterIfPerformanceCollectorTestCase((Test) target, null);
    }
  }

  private void setContextIfAndroidTestCase(Test test, Context context, Context testContext) {
    if (AndroidTestCase.class.isAssignableFrom(test.getClass())) {
      ((AndroidTestCase) test).setContext(context);
      ((AndroidTestCase) test).setTestContext(testContext);
    }
  }

  private void setInstrumentationIfInstrumentationTestCase(
      Test test, Instrumentation instrumentation) {
    if (InstrumentationTestCase.class.isAssignableFrom(test.getClass())) {
      ((InstrumentationTestCase) test).injectInstrumentation(instrumentation);
    }
  }

  private void setPerformanceWriterIfPerformanceCollectorTestCase(
      Test test, PerformanceCollector.PerformanceResultsWriter writer) {
    if (PerformanceCollectorTestCase.class.isAssignableFrom(test.getClass())) {
      ((PerformanceCollectorTestCase) test).setPerformanceResultsWriter(writer);
    }
  }
}
