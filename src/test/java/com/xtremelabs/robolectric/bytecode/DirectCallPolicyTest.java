package com.xtremelabs.robolectric.bytecode;

import static junit.framework.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

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
	    new FullStackDirectCallPolicy().onMethodInvocationFinished(null);
	}
	
	@Test
	public void testGeneralFullStackBehavior() {
	    FullStackDirectCallPolicy fullStack = new FullStackDirectCallPolicy();
        assertTrue(fullStack.shouldCallDirectly(null));
        
        assertTrue(fullStack.shouldCallDirectly(null));
        assertTrue(fullStack.shouldCallDirectly(null));
        
        assertSame(fullStack, fullStack.onMethodInvocationFinished(null));
        assertSame(fullStack, fullStack.onMethodInvocationFinished(null));
        
        assertSame(DirectCallPolicy.NOP, fullStack.onMethodInvocationFinished(null));
	}
	
    @Test
    public void ignoreOneShotWithInFullStack() {
        FullStackDirectCallPolicy fullStack = new FullStackDirectCallPolicy();
        OneShotDirectCallPolicy oneShot = new OneShotDirectCallPolicy(new Object());
        assertFalse(oneShot.checkForChange(fullStack));
    }
    @Test
    public void ignoreFullStackWithInFullStack() {
        FullStackDirectCallPolicy fullStack = new FullStackDirectCallPolicy();
        FullStackDirectCallPolicy fullStack2 = new FullStackDirectCallPolicy();
        assertFalse(fullStack2.checkForChange(fullStack));
    }

    @Test
    public void nopShouldAcceptChanges() {
        assertTrue(DirectCallPolicy.NOP.checkForChange(new FullStackDirectCallPolicy()));
        assertTrue(DirectCallPolicy.NOP.checkForChange(new OneShotDirectCallPolicy(new Object())));
    }
    
    @Test(expected = DirectCallException.class)
    public void oneShotShouldCheckTwiceSetting() {
        OneShotDirectCallPolicy oneShot = new OneShotDirectCallPolicy(new Object());
        OneShotDirectCallPolicy oneShot2 = new OneShotDirectCallPolicy(new Object());
        oneShot2.checkForChange(oneShot);
    }
    
}
