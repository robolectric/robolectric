package com.xtremelabs.robolectric;

import com.xtremelabs.robolectric.util.RealObject;
import com.xtremelabs.robolectric.util.ShadowWrangler;
import com.xtremelabs.robolectric.util.TestHelperInterface;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.Method;

public class AbstractRobolectricTestRunner extends BlockJUnit4ClassRunner {
    private Loader loader;
    private ClassHandler classHandler;
    private Class<? extends TestHelperInterface> testHelperClass;
    private TestHelperInterface testHelper;

    public AbstractRobolectricTestRunner(Class<?> testClass, Loader loader) throws InitializationError {
        super(loader.bootstrap(testClass));
        this.loader = loader;

        this.loader.delegateLoadingOf(TestHelperInterface.class.getName());
        this.loader.delegateLoadingOf(RealObject.class.getName());
        this.loader.delegateLoadingOf(ShadowWrangler.class.getName());
        this.loader.delegateLoadingOf(ProxyDelegatingHandler.class.getName());
    }

    public void setClassHandler(ClassHandler classHandler) {
        this.classHandler = classHandler;
        loader.delegateLoadingOf(getClass().getName());
    }

    public void setTestHelperClass(Class<? extends TestHelperInterface> testHelperClass) {
        this.testHelperClass = testHelperClass;
    }

    @Override protected Statement methodBlock(final FrameworkMethod method) {
        if (classHandler != null) classHandler.beforeTest();
        beforeTest(method.getMethod());

        final Statement statement = super.methodBlock(method);
        return new Statement() {
            @Override public void evaluate() throws Throwable {
                // todo: this try/finally probably isn't right -- should mimic RunAfters? [xw]
                try {
                    statement.evaluate();
                } finally {
                    afterTest(method.getMethod());
                    if (classHandler != null) classHandler.afterTest();
                }
            }
        };
    }

     protected void beforeTest(Method method) {
         if (testHelperClass != null) {
             testHelper = createTestHelper(method);
             testHelper.before(method);
         }
     }

     protected void afterTest(Method method) {
         if (testHelperClass != null) {
             testHelper.after(method);
         }
     }

     @Override
     protected Object createTest() throws Exception {
         Object test = super.createTest();
         if (testHelperClass != null) {
             testHelper.prepareTest(test);
         }
         return test;
     }

     private TestHelperInterface createTestHelper(Method method) {
         Class<?> testClass = method.getDeclaringClass();
         try {
             return (TestHelperInterface) testClass.getClassLoader().loadClass(testHelperClass.getName()).newInstance();
         } catch (ClassNotFoundException e) {
             throw new RuntimeException(e);
         } catch (InstantiationException e) {
             throw new RuntimeException(e);
         } catch (IllegalAccessException e) {
             throw new RuntimeException(e);
         }
     }

}
