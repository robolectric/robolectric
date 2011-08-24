package com.xtremelabs.robolectric;

import android.app.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;

import com.xtremelabs.robolectric.annotation.DisableStrictI18n;
import com.xtremelabs.robolectric.annotation.EnableStrictI18n;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunnerTest.RunnerForTesting.class)
public class RobolectricTestRunnerTest {
	
    @Test
    public void shouldInitializeAndBindApplicationButNotCallOnCreate() throws Exception {
        assertNotNull(Robolectric.application);
        assertEquals(MyTestApplication.class, Robolectric.application.getClass());
        assertFalse(((MyTestApplication) Robolectric.application).onCreateWasCalled);
        assertNotNull(shadowOf(Robolectric.application).getResourceLoader());
    }

    @Test
    public void setStaticValue_shouldIgnoreFinalModifier() {
        RobolectricTestRunner.setStaticValue(android.os.Build.class, "MODEL", "expected value");

        assertEquals("expected value", android.os.Build.MODEL);
    }
    
    @Test
    @EnableStrictI18n
    public void internalBeforeTest_setsI18nStrictModeFromProperty() {
    	assertTrue(RunnerForTesting.instance.robolectricConfig.getStrictI18n());
    }

    @Test
    @DisableStrictI18n
    public void internalBeforeTest_clearsI18nStrictModeFromProperty() {
    	assertFalse(RunnerForTesting.instance.robolectricConfig.getStrictI18n());
    }
    
    @Test
    public void internalBeforeTest_doesNotsetI18nStrictModeFromSystemIfPropertyAbsent() {
    	assertFalse(RunnerForTesting.instance.robolectricConfig.getStrictI18n());
    }

    public static class RunnerForTesting extends WithTestDefaultsRunner {
    	public static RunnerForTesting instance;
 
        public RunnerForTesting(Class<?> testClass) throws InitializationError {
            super(testClass);
        	instance = this;
        }

        @Override protected Application createApplication() {
            return new MyTestApplication();
        }
    }

    private static class MyTestApplication extends Application {
        private boolean onCreateWasCalled;

        @Override public void onCreate() {
            this.onCreateWasCalled = true;
        }
    }
}
