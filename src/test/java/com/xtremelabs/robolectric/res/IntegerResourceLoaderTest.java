package com.xtremelabs.robolectric.res;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.xtremelabs.robolectric.R;

public class IntegerResourceLoaderTest {

	protected IntegerResourceLoader resourceLoader;

	@Before
	public void setup() throws Exception {
        ResourceExtractor extractor = new ResourceExtractor();
        extractor.addLocalRClass( R.class );
        extractor.addSystemRClass( android.R.class );

        resourceLoader = new IntegerResourceLoader( extractor );
        DocumentLoader documentLoader = new DocumentLoader(resourceLoader);

        documentLoader.loadResourceXmlDir(resourceFile("res", "values"));
    }

	@Test
	public void testIntegersAreResolved() {
		assertThat( resourceLoader.getValue( R.integer.test_integer1 ), equalTo( 2000 ) );
		assertThat( resourceLoader.getValue( R.integer.test_integer2 ), equalTo( 9 ) );
        assertThat( resourceLoader.getValue( R.integer.test_large_hex), equalTo( 0xFFFF0000 ) );
    }

}
