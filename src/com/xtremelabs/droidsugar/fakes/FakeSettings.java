package com.xtremelabs.droidsugar.fakes;

import android.content.ContentResolver;
import android.provider.Settings;
import com.xtremelabs.droidsugar.util.FakeHelper;
import com.xtremelabs.droidsugar.util.Implements;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Settings.class)
public class FakeSettings {
    private static class SettingsImpl {
        private static final WeakHashMap<ContentResolver, Map<String, Integer>> dataMap = new WeakHashMap<ContentResolver, Map<String, Integer>>();

        public static boolean putInt(ContentResolver cr, String name, int value) {
            get(cr).put(name, value);
            return true;
        }

        public static int getInt(ContentResolver cr, String name, int def) {
            Integer value = get(cr).get(name);
            return value == null ? def : value;
        }

        private static Map<String, Integer> get(ContentResolver cr) {
            Map<String, Integer> map = dataMap.get(cr);
            if (map == null) {
                map = new HashMap<String, Integer>();
                dataMap.put(cr, map);
            }
            return map;
        }
    }

    @Implements(Settings.System.class)
    public static class FakeSystem extends SettingsImpl {
    }

    @Implements(Settings.Secure.class)
    public static class FakeSecure extends SettingsImpl {
    }

    public static void setAirplaneMode(boolean isAirplaneMode) {
        Settings.System.putInt(FakeHelper.application.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, isAirplaneMode ? 1 : 0);
        setWifiOn(!isAirplaneMode);
    }

    public static void setWifiOn(boolean isOn) {
        Settings.Secure.putInt(FakeHelper.application.getContentResolver(), Settings.Secure.WIFI_ON, isOn ? 1 : 0);
    }
}
