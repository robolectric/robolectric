package com.xtremelabs.robolectric.annotation;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;

import com.xtremelabs.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(TestRunners.WithDefaults.class)
public class WithConstantStringTest {
	
	private static final String NEW_VALUE = "HTC";

	@Test
	@WithConstantString(classWithField=android.os.Build.class, fieldName="MANUFACTURER", newValue=NEW_VALUE)
	public void testWithConstantString() {
		assertThat(android.os.Build.MANUFACTURER, equalTo(NEW_VALUE));
	}

	@Test
	public void testWithoutConstantString() {
		assertThat(android.os.Build.MANUFACTURER, nullValue());
	}

}
