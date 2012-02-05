package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.MatcherAssert.assertThat;
import junit.framework.Assert;

import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.location.Criteria;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class CriteriaTest {
    private Criteria criteria;

    @Before
    public void setUp() {
        criteria = new Criteria();
    }

    @Test
    public void shouldReturnAccuracy() {
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        assertThat(Criteria.ACCURACY_COARSE, IsEqual.equalTo(criteria.getAccuracy()));
    }

    @Test
    public void shouldReturnPowerRequirement() {
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        assertThat(Criteria.POWER_HIGH, IsEqual.equalTo(criteria.getPowerRequirement()));
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
