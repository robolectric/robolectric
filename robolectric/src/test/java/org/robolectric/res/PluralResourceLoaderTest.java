package org.robolectric.res;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.R;
import org.robolectric.util.TestUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.util.TestUtil.testResources;

public class PluralResourceLoaderTest {
  private ResBunch resBunch;

  @Before
  public void setUp() throws Exception {
    resBunch = new ResBunch();
    PluralResourceLoader pluralResourceLoader = new PluralResourceLoader(resBunch);

    new DocumentLoader(R.class.getPackage().getName(), testResources()).load("values", pluralResourceLoader);
  }

  @Test
  public void testPluralsAreResolved() throws Exception {
    ResName resName = new ResName(TestUtil.TEST_PACKAGE, "plurals", "beer");
    PluralResourceLoader.PluralRules pluralRules =
        (PluralResourceLoader.PluralRules) resBunch.get(resName, "");
    assertThat(pluralRules.find(0).string).isEqualTo("@string/howdy");
    assertThat(pluralRules.find(1).string).isEqualTo("One beer");
    assertThat(pluralRules.find(2).string).isEqualTo("Two beers");
    assertThat(pluralRules.find(3).string).isEqualTo("%d beers, yay!");
  }
}
