package com.xtremelabs.robolectric.res;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.xtremelabs.robolectric.R;

public class DimenResourceLoaderTest {

	private DimenResourceLoader dimenResourceLoader;

	@Before
	public void setUp() throws Exception {
		ResourceExtractor resourceExtractor = new ResourceExtractor();
		resourceExtractor.addLocalRClass(R.class);
		dimenResourceLoader = new DimenResourceLoader(resourceExtractor);
		new DocumentLoader(dimenResourceLoader)
				.loadResourceXmlDir(resourceFile("res", "values"));
	}

	@Test
	public void testDimensionsAreResolved() throws Exception {
		assertThat(dimenResourceLoader.getValue(R.dimen.test_dp_dimen),
				equalTo(8.0f));
		assertThat(dimenResourceLoader.getValue(R.dimen.test_dip_dimen),
				equalTo(20.0f));
		assertThat(dimenResourceLoader.getValue(R.dimen.test_pt_dimen),
				equalTo(12.0f));
		assertThat(dimenResourceLoader.getValue(R.dimen.test_px_dimen),
				equalTo(15.0f));
		assertThat(dimenResourceLoader.getValue(R.dimen.test_sp_dimen),
				equalTo(5.0f));
	}

}
