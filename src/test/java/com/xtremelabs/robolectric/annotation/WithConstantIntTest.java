package com.xtremelabs.robolectric.annotation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class WithConstantIntTest {

	private static final int NEW_VALUE = 9;

	@Test
	@WithConstantInt(classWithField=android.os.Build.VERSION.class, fieldName="SDK_INT", newValue=NEW_VALUE )
	public void testWithConstantInt() {
		assertThat(android.os.Build.VERSION.SDK_INT, equalTo(NEW_VALUE));
	}

	@Test
	public void testWithoutConstantInt() {
		assertThat(android.os.Build.VERSION.SDK_INT, equalTo(0));
	}

}
