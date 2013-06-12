package org.robolectric.shadows;

import android.location.Criteria;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class CriteriaTest {
  private Criteria criteria;

  @Before
  public void setUp() {
    criteria = new Criteria();
  }

  @Test
  public void shouldReturnAccuracy() {
    criteria.setAccuracy(Criteria.ACCURACY_COARSE);
    assertThat(criteria.getAccuracy()).isEqualTo(Criteria.ACCURACY_COARSE);
  }

  @Test
  public void shouldReturnPowerRequirement() {
    criteria.setPowerRequirement(Criteria.POWER_HIGH);
    assertThat(criteria.getPowerRequirement()).isEqualTo(Criteria.POWER_HIGH);
  }

  @Test
  public void shouldBeEqual() {
    criteria.setPowerRequirement(Criteria.POWER_HIGH);
    criteria.setAccuracy(Criteria.ACCURACY_COARSE);

    Criteria criteria1 = new Criteria(criteria);
    Assert.assertTrue(criteria1.equals(criteria));

    Criteria criteria2 = new Criteria();
    criteria2.setPowerRequirement(Criteria.POWER_HIGH);
    criteria2.setAccuracy(Criteria.ACCURACY_COARSE);
    Assert.assertTrue(criteria2.equals(criteria));
  }

}
