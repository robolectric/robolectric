package com.xtremelabs.robolectric.bytecode;

import static junit.framework.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.bytecode.DirectCallPolicy.DirectCallException;
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

}
