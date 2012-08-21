package com.xtremelabs.robolectric.bytecode;

import static junit.framework.Assert.*;

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

    @Test
    public void customLoaderShouldBeForced() {
        CustomLoader loader = new CustomLoader(Robolectric.application);
        
        loader.startLoading();
        assertFalse(loader.isForced());
        
        Robolectric.directlyOnFullStack(loader).startLoading();
        assertTrue(loader.isForced());
    }

    @Test
    public void testFullStack_WithStatics() {
        CustomLoaderWithLogger loader = new CustomLoaderWithLogger(Robolectric.application);

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
    
    /** Loader for testing. */
    public static class CustomLoader extends Loader<Object> {

        private boolean forced = false;
        
        public CustomLoader(Context context) {
            super(context);
        }
        
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

        public CustomLoaderWithLogger(Context context) {
            super(context);
        }
        
        @Override
        protected void onForceLoad() {
            super.onForceLoad();
            Log.i("CustomLoaderWithLogger", "Forced");
        }
        
    }

}
