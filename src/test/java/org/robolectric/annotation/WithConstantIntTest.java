package org.robolectric.annotation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class WithConstantIntTest {

	private static final int NEW_VALUE = 9;

	@Test
	@WithConstantInt(classWithField=android.os.Build.VERSION.class, fieldName="SDK_INT", newValue=NEW_VALUE )
	public void testWithConstantInt() {
		assertThat(android.os.Build.VERSION.SDK_INT, equalTo(NEW_VALUE));
	}

	@Test
	public void testWithoutConstantInt() {
		assertThat(android.os.Build.VERSION.SDK_INT, equalTo(12)); // todo pull this from some other config spot? see shadow system properties
	}

}
