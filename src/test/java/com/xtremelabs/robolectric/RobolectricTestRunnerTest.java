package com.xtremelabs.robolectric;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;

import android.app.Activity;
import android.app.Application;
import android.widget.TextView;

import com.xtremelabs.robolectric.annotation.DisableStrictI18n;
import com.xtremelabs.robolectric.annotation.EnableStrictI18n;
import com.xtremelabs.robolectric.annotation.Values;
import com.xtremelabs.robolectric.res.ResourceLoader;

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
    @Values( locale="fr")
    public void internalBeforeTest_setLocale(){
    	assertEquals( RunnerForTesting.instance.robolectricConfig.getValuesResQualifiers(), "fr" );
    }
    
    @Test
    @Values( qualifiers="fr")
    public void internalBeforeTest_testValuesResQualifiers() {
    	assertEquals( RunnerForTesting.instance.robolectricConfig.getValuesResQualifiers(), "fr" );
    }
    
    @Test
    public void internalBeforeTest_doesNotsetI18nStrictModeFromSystemIfPropertyAbsent() {
    	assertFalse(RunnerForTesting.instance.robolectricConfig.getStrictI18n());
    }
    
    @Test
    @EnableStrictI18n
    public void methodBlock_setsI18nStrictModeForClassHandler() {
    	TextView tv = new TextView(Robolectric.application);
    	try {
    		tv.setText("Foo");
    		fail("TextView#setText(String) should produce an i18nException");
    	} catch (Exception e) {
    		// Compare exception name because it was loaded in the instrumented classloader
    		assertEquals("com.xtremelabs.robolectric.util.I18nException", e.getClass().getName());
    	}
    }
    
    @Test
    @EnableStrictI18n
    public void createResourceLoader_setsI18nStrictModeForResourceLoader() {
    	ResourceLoader loader = Robolectric.shadowOf(Robolectric.application).getResourceLoader();
    	assertTrue(RunnerForTesting.instance.robolectricConfig.getStrictI18n());
		assertTrue(loader.getStrictI18n());
    	try {
    		loader.inflateView(Robolectric.application, R.layout.text_views, null);
    		fail("ResourceLoader#inflateView should produce an i18nException");
    	} catch (Exception e) {
    		assertEquals("com.xtremelabs.robolectric.util.I18nException", e.getClass().getName());
    	}
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

    public static class MyTestApplication extends Application {
        private boolean onCreateWasCalled;

        @Override public void onCreate() {
            this.onCreateWasCalled = true;
        }
    }
    
    public static class MyTestActivity extends Activity {
    	
    }

}
