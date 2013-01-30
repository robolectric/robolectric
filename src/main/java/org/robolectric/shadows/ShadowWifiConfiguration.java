package com.xtremelabs.robolectric.shadows;

import android.net.wifi.WifiConfiguration;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.util.BitSet;

@Implements(WifiConfiguration.class)
public class ShadowWifiConfiguration {
    @RealObject WifiConfiguration realObject;

    public void __constructor__() {
        realObject.networkId = -1;
        realObject.SSID = null;
        realObject.BSSID = null;
        realObject.priority = 0;
        realObject.hiddenSSID = false;
        realObject.allowedKeyManagement = new BitSet();
        realObject.allowedProtocols = new BitSet();
        realObject.allowedAuthAlgorithms = new BitSet();
        realObject.allowedPairwiseCiphers = new BitSet();
        realObject.allowedGroupCiphers = new BitSet();
        realObject.wepKeys = new String[4];
        for (int i = 0; i < realObject.wepKeys.length; i++)
            realObject.wepKeys[i] = null;
//        for (EnterpriseField field : realObject.enterpriseFields) {
//            field.setValue(null);
//        }
    }

    public WifiConfiguration copy(){
        WifiConfiguration config = new WifiConfiguration();
        config.networkId = realObject.networkId;
        config.SSID = realObject.SSID;
        config.BSSID = realObject.BSSID;
        config.preSharedKey = realObject.preSharedKey;
        config.wepTxKeyIndex = realObject.wepTxKeyIndex;
        config.status = realObject.status;
        config.priority = realObject.priority;
        config.hiddenSSID = realObject.hiddenSSID;
        config.allowedKeyManagement = (BitSet) realObject.allowedKeyManagement.clone();
        config.allowedProtocols = (BitSet) realObject.allowedProtocols.clone();
        config.allowedAuthAlgorithms = (BitSet) realObject.allowedAuthAlgorithms.clone();
        config.allowedPairwiseCiphers = (BitSet) realObject.allowedPairwiseCiphers.clone();
        config.allowedGroupCiphers = (BitSet) realObject.allowedGroupCiphers.clone();
        config.wepKeys = new String[4];
        System.arraycopy(realObject.wepKeys, 0, config.wepKeys, 0, config.wepKeys.length);
        return config;
    }
}
