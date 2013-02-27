package org.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.fest.assertions.api.Assertions.assertThat;

import org.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(TestRunners.WithDefaults.class)
public class DatabaseUtilsTest {

	@Test
	public void testQuote() {
        assertThat(ShadowDatabaseUtils.sqlEscapeString("foobar")).isEqualTo("'foobar'");
        assertThat(ShadowDatabaseUtils.sqlEscapeString("Rich's")).isEqualTo("'Rich''s'");
	}
}
