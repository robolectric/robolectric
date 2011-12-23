package com.xtremelabs.robolectric.shadows;

import android.location.Criteria;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(Criteria.class)
public class ShadowCriteria {
    private int accuracy = Criteria.NO_REQUIREMENT;
//    private int bearingAccuracy;
//    private int horizontalAccuracy;
//    private int verticalAccuracy;
//    private int speedAccuracy;
    private int powerRequirement = Criteria.NO_REQUIREMENT;

    public void __constructor__(Criteria criteria) {
        accuracy = criteria.getAccuracy();
        powerRequirement = criteria.getPowerRequirement();
    }

    @Implementation
    public int getAccuracy() {
        return accuracy;
    }

    @Implementation
    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    @Implementation
    public int getPowerRequirement() {
        return powerRequirement;
    }

    @Implementation
    public void setPowerRequirement(int powerRequirement) {
        this.powerRequirement = powerRequirement;
    }

    @Implementation
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Criteria)) {
            return false;
        }
        Criteria criteria = (Criteria) obj;
        if (criteria.getAccuracy() == accuracy && criteria.getPowerRequirement() == powerRequirement) {
            return true;
        }
        return false;
    }
}
