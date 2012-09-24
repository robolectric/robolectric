package com.xtremelabs.robolectric.bytecode;

import static junit.framework.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;

import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.v4.content.Loader;
import android.util.Log;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.bytecode.DirectCallPolicy.DirectCallException;
import com.xtremelabs.robolectric.bytecode.DirectCallPolicy.FullStackDirectCallPolicy;
import com.xtremelabs.robolectric.bytecode.DirectCallPolicy.OneShotDirectCallPolicy;
import com.xtremelabs.robolectric.internal.Instrument;

@RunWith(WithTestDefaultsRunner.class)
public class DirectCallPolicyTest {

	@Test
	public void nopShouldBeFollowedByNop() {
		assertSame(DirectCallPolicy.NOP, DirectCallPolicy.NOP.onMethodInvocationFinished(null));
		assertSame(DirectCallPolicy.NOP, DirectCallPolicy.NOP.onMethodInvocationFinished(new Object()));
	}
	
	@Test
	public void oneShotShouldBeFollowedByNop() {
		Object target = new Object();
		OneShotDirectCallPolicy oneShot = new OneShotDirectCallPolicy(target);
		assertSame(DirectCallPolicy.NOP, oneShot.onMethodInvocationFinished(target));
	}
	
	@Test(expected = DirectCallException.class)
	public void unexpectedTargetOneShotShouldThrowException() {
		new OneShotDirectCallPolicy(new Object()).shouldCallDirectly(new Object());
	}
	
	@Test(expected = DirectCallException.class)
	public void unexpectedStaticTargetOneShotShouldThrowException() {
		new OneShotDirectCallPolicy(new Object()).shouldCallDirectly(null);
	}

	@Test
	public void testGeneralOneShotBehavior() {
		Object target = new Object();
		OneShotDirectCallPolicy oneShot = new OneShotDirectCallPolicy(target);
		assertTrue(oneShot.shouldCallDirectly(target));
		assertFalse(oneShot.shouldCallDirectly(target));
		assertFalse(oneShot.shouldCallDirectly(new Object()));
		assertFalse(oneShot.shouldCallDirectly(null));
	}

	@Test(expected = DirectCallException.class)
	public void fullStackShouldExpectDirectCallBeforeMethodInvocationFinished() {
	    Object target = new Object();
	    FullStackDirectCallPolicy.withTarget(target).onMethodInvocationFinished(target);
	}
	
	@Test
	public void testGeneralFullStackBehavior() {
	    Object target = new Object();
	    FullStackDirectCallPolicy fullStack = FullStackDirectCallPolicy.withTarget(target);
        assertTrue(fullStack.shouldCallDirectly(target));
        
        // go deeper
        assertTrue(fullStack.shouldCallDirectly(target));
        Object another = new Object();
        assertFalse(fullStack.shouldCallDirectly(another));
        // go deeper, static call
        assertFalse(fullStack.shouldCallDirectly(null));
        
        // pop, static call
        assertSame(fullStack, fullStack.onMethodInvocationFinished(null));
        // pop
        assertSame(fullStack, fullStack.onMethodInvocationFinished(another));
        // pop, affect
        assertSame(fullStack, fullStack.onMethodInvocationFinished(target));
        
        assertSame(DirectCallPolicy.NOP, fullStack.onMethodInvocationFinished(target));
	}
	
    @Test
    public void ignoreOneShotWithInFullStack() {
        Object target = new Object(); 
        FullStackDirectCallPolicy fullStack = FullStackDirectCallPolicy.withTarget(target);
        assertTrue(fullStack.shouldCallDirectly(target));
        OneShotDirectCallPolicy oneShot = new OneShotDirectCallPolicy(new Object());
        assertFalse(oneShot.checkForChange(fullStack));
    }
    @Test
    public void ignoreFullStackWithInFullStack() {
        Object target = new Object(); 
        FullStackDirectCallPolicy fullStack = FullStackDirectCallPolicy.withTarget(target);
        assertTrue(fullStack.shouldCallDirectly(target));
        FullStackDirectCallPolicy fullStack2 = FullStackDirectCallPolicy.withTarget(new Object());
        assertFalse(fullStack2.checkForChange(fullStack));
    }

    @Test
    public void nopShouldAcceptChanges() {
        assertTrue(DirectCallPolicy.NOP.checkForChange(FullStackDirectCallPolicy.withTarget(new Object())));
        assertTrue(DirectCallPolicy.NOP.checkForChange(new OneShotDirectCallPolicy(new Object())));
    }
    
    @Test(expected = DirectCallException.class)
    public void oneShotShouldCheckTwiceSetting() {
        OneShotDirectCallPolicy oneShot = new OneShotDirectCallPolicy(new Object());
        OneShotDirectCallPolicy oneShot2 = new OneShotDirectCallPolicy(new Object());
        oneShot2.checkForChange(oneShot);
    }
    
    @Test(expected = DirectCallException.class)
    public void fullStackShouldCheckSettings() {
        FullStackDirectCallPolicy fullStack = FullStackDirectCallPolicy.withTarget(new Object());
        fullStack.shouldCallDirectly(new Object());
    }

    /*
     * XXX very interesting case 
     * Instrumentation injects default constructor to the Loader class.
     * As a result super invocations of Loader(Context) constructor are not performed.
     * Try to uncomment this test and debug .create() method. This test fails.
     * Currently the only way to provide context to a loader is to set private mContext field value with reflection API.  
     */
    
//    @Test
//    public void testDirectConstructorCall() throws Exception {
//        CustomLoader badLoader = new CustomLoader();
//        assertNull(Robolectric.directlyOnFullStack(badLoader).getContext());
//        
//        CustomLoaderCreator creator = new CustomLoaderCreator();
//        CustomLoader loader = Robolectric.directlyOnFullStack(FullStackDirectCallPolicy.build(creator).include(CustomLoader.class))
//                .create();
//        
//        // is constructor called directly?
//        assertTrue(loader.superConstructorCalled);
//        assertNotNull(Robolectric.directlyOnFullStack(loader).getContext());
//        
//        // everything still working
//        loader.startLoading();
//        assertFalse(loader.isForced());
//    }
    
    @Test
    public void customLoaderShouldBeForced() {
        CustomLoader loader = new CustomLoader();
        
        loader.startLoading();
        assertFalse(loader.isForced());
        
        Robolectric.directlyOnFullStack(loader).startLoading();
        assertTrue(loader.isForced());
        
        // test everything works after direct constructor call
        CustomLoaderCreator creator = new CustomLoaderCreator();
        CustomLoader loader2 = Robolectric.directlyOnFullStack(FullStackDirectCallPolicy.build(creator).include(CustomLoader.class))
                .create();
        
        loader2.startLoading();
        assertFalse(loader2.isForced());
        
        Robolectric.directlyOnFullStack(loader2).startLoading();
        assertTrue(loader2.isForced());
    }

    @Test
    public void testFullStack_WithStatics() {
        // direct constructor is not necessary in this case
        CustomLoaderWithLogger loader = new CustomLoaderWithLogger();

        // Use instrumented Log class
        Robolectric.directlyOnFullStack(loader).startLoading();
        assertTrue(loader.isForced());
        
        // Include static calls
        Exception e = null;
        try {
            Robolectric.directlyOnFullStack(
                    FullStackDirectCallPolicy.build(loader).withStatics(true)
            ).startLoading();
        } catch (Exception e1) {
            e = e1;
        }
        assertNotNull(e);
        assertEquals("Stub!", e.getMessage());
    }

    @Instrument
    public static class CustomLoaderParent<D> extends Loader<D> {

        boolean superConstructorCalled;
        
        public CustomLoaderParent() {
            this(Robolectric.application);
        }
        
        public CustomLoaderParent(Context context) {
            super(context);
            superConstructorCalled = true;
        }
        
    }
    
    /** Loader for testing. */
    public static class CustomLoader extends CustomLoaderParent<Object> {

        private boolean forced = false;
        
        @Override
        protected void onStartLoading() {
            forceLoad();
        }
        
        @Override
        protected void onForceLoad() {
            forced = true;
        }
        
        public boolean isForced() {
            return forced;
        }
        
    }
    
    /** Loader for testing. */
    public static class CustomLoaderWithLogger extends CustomLoader {

        @Override
        protected void onForceLoad() {
            super.onForceLoad();
            Log.i("CustomLoaderWithLogger", "Forced");
        }
        
    }

    /** 
     * This class must be instrumented to call {@link RobolectricInternals#shouldCallDirectly(Object)} 
     *  from {@link CustomLoaderCreator#create(Context)}.
     */
    @Instrument
    public static class CustomLoaderCreator {
        public CustomLoader create() {
            return new CustomLoader();
        }
    }
    
}
