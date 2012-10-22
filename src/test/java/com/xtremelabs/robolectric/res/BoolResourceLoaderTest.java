package com.xtremelabs.robolectric.res;

import com.xtremelabs.robolectric.R;
import org.junit.Before;
import org.junit.Test;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class BoolResourceLoaderTest {

	protected BoolResourceLoader resourceLoader;

	@Before
	public void setup() throws Exception {
        ResourceExtractor extractor = new ResourceExtractor();
        extractor.addLocalRClass(R.class);
        extractor.addSystemRClass(android.R.class);

        resourceLoader = new BoolResourceLoader(extractor);
        DocumentLoader documentLoader = new DocumentLoader(resourceLoader);

        documentLoader.loadResourceXmlDir(resourceFile("res", "values"));
    }

	@Test
	public void testBooleansAreResolved() {
		assertThat(resourceLoader.getValue(R.bool.false_bool_value ), equalTo(false));
		assertThat(resourceLoader.getValue(R.bool.true_bool_value ), equalTo(true));
    }
	
	@Test
	public void testIntegersAreResolvedAsBooleans() {
		assertThat(resourceLoader.getValue(R.bool.zero_is_false ), equalTo(false));
		assertThat(resourceLoader.getValue(R.bool.integers_are_true ), equalTo(true));
    }
}
