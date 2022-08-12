package org.robolectric.res.android

import com.google.common.truth.Truth.assertThat
import org.robolectric.res.android.ResTable_config.DENSITY_ANY
import org.robolectric.res.android.ResTable_config.DENSITY_HIGH
import org.robolectric.res.android.ResTable_config.DENSITY_LOW
import org.robolectric.res.android.ResTable_config.DENSITY_MEDIUM
import org.robolectric.res.android.ResTable_config.DENSITY_NONE
import org.robolectric.res.android.ResTable_config.DENSITY_TV
import org.robolectric.res.android.ResTable_config.DENSITY_XHIGH
import org.robolectric.res.android.ResTable_config.DENSITY_XXHIGH
import org.robolectric.res.android.ResTable_config.DENSITY_XXXHIGH
import org.robolectric.res.android.ResTable_config.KEYBOARD_12KEY
import org.robolectric.res.android.ResTable_config.KEYBOARD_NOKEYS
import org.robolectric.res.android.ResTable_config.KEYBOARD_QWERTY
import org.robolectric.res.android.ResTable_config.KEYSHIDDEN_NO
import org.robolectric.res.android.ResTable_config.KEYSHIDDEN_SOFT
import org.robolectric.res.android.ResTable_config.KEYSHIDDEN_YES
import org.robolectric.res.android.ResTable_config.LAYOUTDIR_ANY
import org.robolectric.res.android.ResTable_config.LAYOUTDIR_LTR
import org.robolectric.res.android.ResTable_config.LAYOUTDIR_RTL
import org.robolectric.res.android.ResTable_config.NAVHIDDEN_NO
import org.robolectric.res.android.ResTable_config.NAVHIDDEN_YES
import org.robolectric.res.android.ResTable_config.NAVIGATION_DPAD
import org.robolectric.res.android.ResTable_config.NAVIGATION_NONAV
import org.robolectric.res.android.ResTable_config.NAVIGATION_TRACKBALL
import org.robolectric.res.android.ResTable_config.NAVIGATION_WHEEL
import org.robolectric.res.android.ResTable_config.ORIENTATION_LAND
import org.robolectric.res.android.ResTable_config.ORIENTATION_PORT
import org.robolectric.res.android.ResTable_config.ORIENTATION_SQUARE
import org.robolectric.res.android.ResTable_config.SCREENLONG_NO
import org.robolectric.res.android.ResTable_config.SCREENROUND_NO
import org.robolectric.res.android.ResTable_config.SCREENROUND_YES
import org.robolectric.res.android.ResTable_config.SCREENSIZE_LARGE
import org.robolectric.res.android.ResTable_config.SCREENSIZE_NORMAL
import org.robolectric.res.android.ResTable_config.SCREENSIZE_SMALL
import org.robolectric.res.android.ResTable_config.SCREENSIZE_XLARGE
import org.robolectric.res.android.ResTable_config.TOUCHSCREEN_FINGER
import org.robolectric.res.android.ResTable_config.TOUCHSCREEN_NOTOUCH
import org.robolectric.res.android.ResTable_config.TOUCHSCREEN_STYLUS
import org.robolectric.res.android.ResTable_config.UI_MODE_NIGHT_NO
import org.robolectric.res.android.ResTable_config.UI_MODE_NIGHT_YES
import org.robolectric.res.android.ResTable_config.UI_MODE_TYPE_APPLIANCE
import org.robolectric.res.android.ResTable_config.UI_MODE_TYPE_CAR
import org.robolectric.res.android.ResTable_config.UI_MODE_TYPE_TELEVISION
import org.robolectric.res.android.ResTable_config.UI_MODE_TYPE_WATCH

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class ConfigDescriptionTest {

    @Test
    fun parse_mcc() {
        val config = ResTable_config()
        ConfigDescription.parse("mcc310", config)
        assertThat(config.mcc).isEqualTo(310)
    }

    @Test
    fun parse_mcc_upperCase() {
        val config = ResTable_config()
        ConfigDescription.parse("MCC310", config)
        assertThat(config.mcc).isEqualTo(310)
    }

    @Test
    fun parse_mcc_mnc_upperCase() {
        val config = ResTable_config()
        ConfigDescription.parse("mcc310-mnc004", config)
        assertThat(config.mcc).isEqualTo(310)
        assertThat(config.mnc).isEqualTo(4)
    }

    @Test
    fun parse_layoutDirection_leftToRight() {
        val config = ResTable_config()
        ConfigDescription.parse("ldltr", config)
        assertThat(config.screenLayout).isEqualTo(LAYOUTDIR_LTR)
    }

    @Test
    fun parse_layoutDirection_rightToLeft() {
        val config = ResTable_config()
        ConfigDescription.parse("ldrtl", config)
        assertThat(config.screenLayout).isEqualTo(LAYOUTDIR_RTL)
    }

    @Test
    fun parse_layoutDirection_any() {
        val config = ResTable_config()
        ConfigDescription.parse("any", config)
        assertThat(config.screenLayout).isEqualTo(LAYOUTDIR_ANY)
    }

    @Test
    fun parse_screenSize_small() {
        val config = ResTable_config()
        ConfigDescription.parse("small", config)
        assertThat(config.screenLayout).isEqualTo(SCREENSIZE_SMALL)
    }

    @Test
    fun parse_screenSize_normal() {
        val config = ResTable_config()
        ConfigDescription.parse("normal", config)
        assertThat(config.screenLayout).isEqualTo(SCREENSIZE_NORMAL)
    }

    @Test
    fun parse_screenSize_large() {
        val config = ResTable_config()
        ConfigDescription.parse("large", config)
        assertThat(config.screenLayout).isEqualTo(SCREENSIZE_LARGE)
    }

    @Test
    fun parse_screenSize_xlarge() {
        val config = ResTable_config()
        ConfigDescription.parse("xlarge", config)
        assertThat(config.screenLayout).isEqualTo(SCREENSIZE_XLARGE)
    }

    @Test
    fun parse_smallestScreenWidth() {
        val config = ResTable_config()
        ConfigDescription.parse("sw320dp", config)
        assertThat(config.smallestScreenWidthDp).isEqualTo(320)
    }

    @Test
    fun getScreenWidth(){
        val config = ResTable_config()
        ConfigDescription.parse("w480dp", config)
        assertThat(config.screenWidthDp).isEqualTo(480)
    }

    @Test
    fun getScreenHeight(){
        val config = ResTable_config()
        ConfigDescription.parse("h1024dp", config)
        assertThat(config.screenHeightDp).isEqualTo(1024)
    }

    @Test
    fun parse_screenLayoutLong_notlong() {
        val config = ResTable_config()
        ConfigDescription.parse("notlong", config)
        assertThat(config.screenLayout).isEqualTo(SCREENLONG_NO)
    }

    @Test
    fun parse_screenRound_round() {
        val config = ResTable_config()
        ConfigDescription.parse("round", config)
        assertThat(config.screenLayout2).isEqualTo(SCREENROUND_YES.toByte())
    }

    @Test
    fun parse_screenRound_notround() {
        val config = ResTable_config()
        ConfigDescription.parse("notround", config)
        assertThat(config.screenLayout2).isEqualTo(SCREENROUND_NO.toByte())
    }

    @Test
    fun parse_orientation_port() {
        val config = ResTable_config()
        ConfigDescription.parse("port", config)
        assertThat(config.orientation).isEqualTo(ORIENTATION_PORT)
    }

    @Test
    fun parse_orientation_land() {
        val config = ResTable_config()
        ConfigDescription.parse("land", config)
        assertThat(config.orientation).isEqualTo(ORIENTATION_LAND)
    }

    @Test
    fun parse_orientation_square() {
        val config = ResTable_config()
        ConfigDescription.parse("square", config)
        assertThat(config.orientation).isEqualTo(ORIENTATION_SQUARE)
    }

    @Test
    fun parse_uiModeType_car() {
        val config = ResTable_config()
        ConfigDescription.parse("car", config)
        assertThat(config.uiMode).isEqualTo(UI_MODE_TYPE_CAR)
    }

    @Test
    fun parse_uiModeType_television() {
        val config = ResTable_config()
        ConfigDescription.parse("television", config)
        assertThat(config.uiMode).isEqualTo(UI_MODE_TYPE_TELEVISION)
    }

    @Test
    fun parse_uiModeType_appliance() {
        val config = ResTable_config()
        ConfigDescription.parse("appliance", config)
        assertThat(config.uiMode).isEqualTo(UI_MODE_TYPE_APPLIANCE)
    }

    @Test
    fun parse_uiModeType_watch() {
        val config = ResTable_config()
        ConfigDescription.parse("watch", config)
        assertThat(config.uiMode).isEqualTo(UI_MODE_TYPE_WATCH)
    }

    @Test
    fun parse_uiModeNight_night() {
        val config = ResTable_config()
        ConfigDescription.parse("night", config)
        assertThat(config.uiMode).isEqualTo(UI_MODE_NIGHT_YES)
    }

    @Test
    fun parse_uiModeNight_notnight() {
        val config = ResTable_config()
        ConfigDescription.parse("notnight", config)
        assertThat(config.uiMode).isEqualTo(UI_MODE_NIGHT_NO)
    }

    @Test
    fun parse_density_any() {
        val config = ResTable_config()
        ConfigDescription.parse("anydpi", config)
        assertThat(config.density).isEqualTo(DENSITY_ANY)
    }

    @Test
    fun parse_density_nodpi() {
        val config = ResTable_config()
        ConfigDescription.parse("nodpi", config)
        assertThat(config.density).isEqualTo(DENSITY_NONE)
    }

    @Test
    fun parse_density_ldpi() {
        val config = ResTable_config()
        ConfigDescription.parse("ldpi", config)
        assertThat(config.density).isEqualTo(DENSITY_LOW)
    }

    @Test
    fun parse_density_mdpi() {
        val config = ResTable_config()
        ConfigDescription.parse("mdpi", config)
        assertThat(config.density).isEqualTo(DENSITY_MEDIUM)
    }

    @Test
    fun parse_density_tvdpi() {
        val config = ResTable_config()
        ConfigDescription.parse("tvdpi", config)
        assertThat(config.density).isEqualTo(DENSITY_TV)
    }

    @Test
    fun parse_density_hdpi() {
        val config = ResTable_config()
        ConfigDescription.parse("hdpi", config)
        assertThat(config.density).isEqualTo(DENSITY_HIGH)
    }

    @Test
    fun parse_density_xhdpi() {
        val config = ResTable_config()
        ConfigDescription.parse("xhdpi", config)
        assertThat(config.density).isEqualTo(DENSITY_XHIGH)
    }

    @Test
    fun parse_density_xxhdpi() {
        val config = ResTable_config()
        ConfigDescription.parse("xxhdpi", config)
        assertThat(config.density).isEqualTo(DENSITY_XXHIGH)
    }

    @Test
    fun parse_density_xxxhdpi() {
        val config = ResTable_config()
        ConfigDescription.parse("xxxhdpi", config)
        assertThat(config.density).isEqualTo(DENSITY_XXXHIGH)
    }

    @Test
    fun parsedensity_specificDpt() {
        val config = ResTable_config()
        ConfigDescription.parse("720dpi", config)
        assertThat(config.density).isEqualTo(720)
    }

    @Test
    fun parse_touchscreen_notouch() {
        val config = ResTable_config()
        ConfigDescription.parse("notouch", config)
        assertThat(config.touchscreen).isEqualTo(TOUCHSCREEN_NOTOUCH)
    }

    @Test
    fun parse_touchscreen_stylus() {
        val config = ResTable_config()
        ConfigDescription.parse("stylus", config)
        assertThat(config.touchscreen).isEqualTo(TOUCHSCREEN_STYLUS)
    }

    @Test
    fun parse_touchscreen_finger() {
        val config = ResTable_config()
        ConfigDescription.parse("finger", config)
        assertThat(config.touchscreen).isEqualTo(TOUCHSCREEN_FINGER)
    }

    @Test
    fun parse_keysHidden_keysexposed() {
        val config = ResTable_config()
        ConfigDescription.parse("keysexposed", config)
        assertThat(config.inputFlags).isEqualTo(KEYSHIDDEN_NO)
    }

    @Test
    fun parse_keysHidden_keyshidden() {
        val config = ResTable_config()
        ConfigDescription.parse("keyshidden", config)
        assertThat(config.inputFlags).isEqualTo(KEYSHIDDEN_YES)
    }

    @Test
    fun parse_keysHidden_keyssoft() {
        val config = ResTable_config()
        ConfigDescription.parse("keyssoft", config)
        assertThat(config.inputFlags).isEqualTo(KEYSHIDDEN_SOFT)
    }

    @Test
    fun parse_keyboard_nokeys() {
        val config = ResTable_config()
        ConfigDescription.parse("nokeys", config)
        assertThat(config.keyboard).isEqualTo(KEYBOARD_NOKEYS)
    }

    @Test
    fun parse_keyboard_qwerty() {
        val config = ResTable_config()
        ConfigDescription.parse("qwerty", config)
        assertThat(config.keyboard).isEqualTo(KEYBOARD_QWERTY)
    }

    @Test
    fun parse_keyboard_12key() {
        val config = ResTable_config()
        ConfigDescription.parse("12key", config)
        assertThat(config.keyboard).isEqualTo(KEYBOARD_12KEY)
    }

    @Test
    fun parse_navHidden_navexposed() {
        val config = ResTable_config()
        ConfigDescription.parse("navexposed", config)
        assertThat(config.inputFlags).isEqualTo(NAVHIDDEN_NO)
    }

    @Test
    fun parse_navHidden_navhidden() {
        val config = ResTable_config()
        ConfigDescription.parse("navhidden", config)
        assertThat(config.inputFlags).isEqualTo(NAVHIDDEN_YES)
    }

    @Test
    fun parse_navigation_nonav() {
        val config = ResTable_config()
        ConfigDescription.parse("nonav", config)
        assertThat(config.navigation).isEqualTo(NAVIGATION_NONAV)
    }

    @Test
    fun parse_navigation_dpad() {
        val config = ResTable_config()
        ConfigDescription.parse("dpad", config)
        assertThat(config.navigation).isEqualTo(NAVIGATION_DPAD)
    }

    @Test
    fun parse_navigation_trackball() {
        val config = ResTable_config()
        ConfigDescription.parse("trackball", config)
        assertThat(config.navigation).isEqualTo(NAVIGATION_TRACKBALL)
    }

    @Test
    fun parse_navigation_wheel() {
        val config = ResTable_config()
        ConfigDescription.parse("wheel", config)
        assertThat(config.navigation).isEqualTo(NAVIGATION_WHEEL)
    }

    @Test
    fun parse_screenSize() {
        val config = ResTable_config()
        ConfigDescription.parse("480x320", config)
        assertThat(config.screenWidth).isEqualTo(480)
        assertThat(config.screenHeight).isEqualTo(320)
    }

    @Test
    fun parse_screenSize_ignoreWidthLessThanHeight() {
        val config = ResTable_config()
        ConfigDescription.parse("320x480", config)
        assertThat(config.screenWidth).isEqualTo(0)
        assertThat(config.screenHeight).isEqualTo(0)
    }

    @Test
    fun parse_version() {
        val config = ResTable_config()
        ConfigDescription.parse("v12", config)
        assertThat(config.sdkVersion).isEqualTo(12)
        assertThat(config.minorVersion).isEqualTo(0)
    }

    @Test
    fun parse_language() {
        val config = ResTable_config()
        ConfigDescription.parse("en", config)
        assertThat(config.languageString()).isEqualTo("en")
        assertThat(config.minorVersion).isEqualTo(0)
    }

    @Test
    fun parse_languageAndRegion() {
        val config = ResTable_config()
        ConfigDescription.parse("fr-rFR", config)
        assertThat(config.languageString()).isEqualTo("fr")
        assertThat(config.regionString()).isEqualTo("FR")
    }

    @Test
    fun parse_multipleQualifiers() {
        val config = ResTable_config()
        assertThat(ConfigDescription.parse("en-rUS-sw320dp-v7", config)).isTrue()
        assertThat(config.languageString()).isEqualTo("en")
        assertThat(config.regionString()).isEqualTo("US")
        assertThat(config.smallestScreenWidthDp).isEqualTo(320)
        assertThat(config.sdkVersion).isEqualTo(ConfigDescription.SDK_HONEYCOMB_MR2)
    }

    @Test
    fun parse_multipleQualifiers_outOfOrder() {
        val config = ResTable_config()
        assertThat(ConfigDescription.parse("v7-en-rUS-sw320dp", config)).isFalse()
    }
}