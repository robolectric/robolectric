package com.xtremelabs.robolectric.res;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.xtremelabs.robolectric.R;

public class StringArrayResourceLoaderTest {
	private StringArrayResourceLoader stringArrayResourceLoader;

	@Before
	public void setUp() throws Exception {
		ResourceExtractor resourceExtractor = new ResourceExtractor();
		resourceExtractor.addRClass(R.class);
		StringResourceLoader stringResourceLoader = new StringResourceLoader(
				resourceExtractor);
		stringArrayResourceLoader = new StringArrayResourceLoader(
				resourceExtractor, stringResourceLoader);

		new DocumentLoader(stringArrayResourceLoader, stringResourceLoader)
				.loadResourceXmlDir(new File("test/res/values"));
	}

	@Test
	public void testStringsAreResolved() throws Exception {
		assertThat(Arrays.asList(stringArrayResourceLoader
				.getArrayValue(R.array.items)), contains("foo", "bar"));
	}

	@Test
	public void testStringsInsideTheArrayAreResolved() throws Exception {
		assertThat(Arrays.asList(stringArrayResourceLoader
				.getArrayValue(R.array.greetings)), contains("Howdy", "Hello"));
	}
}
