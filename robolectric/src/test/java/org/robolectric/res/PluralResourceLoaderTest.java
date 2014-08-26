package org.robolectric.res;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.util.TestUtil;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.util.TestUtil.testResources;

public class PluralResourceLoaderTest {
  private ResBundle<PluralResourceLoader.PluralRules> pluralRulesResBundle;

  @Before
  public void setUp() throws Exception {
    pluralRulesResBundle = new ResBundle<PluralResourceLoader.PluralRules>();
    PluralResourceLoader pluralResourceLoader = new PluralResourceLoader(pluralRulesResBundle);

    new DocumentLoader(testResources()).load("values", pluralResourceLoader);
  }

  @Test
  public void testPluralsAreResolved() throws Exception {
    ResName resName = new ResName(TestUtil.TEST_PACKAGE, "plurals", "beer");
    PluralResourceLoader.PluralRules pluralRules = pluralRulesResBundle.getValue(resName, "").value;
    assertThat(pluralRules.find(0).string).isEqualTo("@string/howdy");
    assertThat(pluralRules.find(1).string).isEqualTo("One beer");
    assertThat(pluralRules.find(2).string).isEqualTo("Two beers");
    assertThat(pluralRules.find(3).string).isEqualTo("%d beers, yay!");
  }
}
