package org.robolectric.res.android

import com.google.common.truth.Truth
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ResTable_configTest {

    @Test
    @Throws(Exception::class)
    fun testLocale() {
        val resTable_config = ResTable_config()
        resTable_config.language[0] = 'e'.toByte()
        resTable_config.language[1] = 'n'.toByte()
        resTable_config.country[0] = 'u'.toByte()
        resTable_config.country[1] = 'k'.toByte()

        Truth.assertThat(resTable_config.locale())
                .isEqualTo('e'.toByte().toInt() shl 24 or ('n'.toByte().toInt() shl 16) or ('u'.toByte().toInt() shl 8) or 'k'.toByte().toInt())
    }

    /* canonicalize= */
    @Test
    fun bcp47Locale_shouldReturnCanonicalizedTag() {
        val resTable_config = ResTable_config()
        resTable_config.language[0] = 'j'.toByte()
        resTable_config.language[1] = 'a'.toByte()
        resTable_config.country[0] = 'j'.toByte()
        resTable_config.country[1] = 'p'.toByte()

        Truth.assertThat(resTable_config.getBcp47Locale( /* canonicalize= */true)).isEqualTo("ja-jp")
    }

    /* canonicalize= */
    @Test
    fun bcp47Locale_philippines_shouldReturnFil() {
        val resTable_config = ResTable_config()
        resTable_config.language[0] = 't'.toByte()
        resTable_config.language[1] = 'l'.toByte()
        resTable_config.country[0] = 'p'.toByte()
        resTable_config.country[1] = 'h'.toByte()

        Truth.assertThat(resTable_config.getBcp47Locale( /* canonicalize= */true)).isEqualTo("fil-ph")
    }

    @Test
    fun fromDtoH_preservesMnc() {
        val config = ResTable_config()
        config.mnc = 0xFFFF

        Truth.assertThat(ResTable_config.fromDtoH(config).mnc).isEqualTo(0xFFFF)
    }
}