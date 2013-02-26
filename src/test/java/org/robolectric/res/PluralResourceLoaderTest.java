package org.robolectric.res;

import org.robolectric.util.TestUtil;
import org.junit.Before;
import org.junit.Test;

import static org.robolectric.util.TestUtil.testResources;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class PluralResourceLoaderTest {
    private ResBundle<PluralResourceLoader.PluralRules> pluralRulesResBundle;

    @Before
    public void setUp() throws Exception {
        ResourceIndex extractor = new ResourceExtractor(testResources());

        pluralRulesResBundle = new ResBundle<PluralResourceLoader.PluralRules>();
        PluralResourceLoader pluralResourceLoader = new PluralResourceLoader(extractor, pluralRulesResBundle);

        new DocumentLoader( pluralResourceLoader).loadResourceXmlDir(testResources(), "values");
    }

    @Test
    public void testPluralsAreResolved() throws Exception {
        ResName resName = new ResName(TestUtil.TEST_PACKAGE, "plurals", "beer");
        PluralResourceLoader.PluralRules pluralRules = pluralRulesResBundle.getValue(resName, "").value;
        assertThat(pluralRules.find(0).string, equalTo("@string/howdy"));
        assertThat(pluralRules.find(1).string, equalTo("One beer"));
        assertThat(pluralRules.find(2).string, equalTo("Two beers"));
        assertThat(pluralRules.find(3).string, equalTo("%d beers, yay!"));
    }
}
