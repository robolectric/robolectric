package org.robolectric.res.android

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ResTableConfigTest {

  companion object {
    const val MCC_US_CARRIER = 310
    const val MCC_US_VERIZON = 4
    val LANGUAGE_FRENCH = byteArrayOf('f'.toByte(), 'r'.toByte())
    private val LANGUAGE_SPANISH = byteArrayOf('e'.toByte(), 's'.toByte())

    fun newBuilder(): ResTableConfigBuilder {
      return ResTableConfigBuilder()
    }
  }

  @Test
  fun isBetterThan_emptyConfig() {
    // When a configuration is not specified the result is always false
    assertThat(newBuilder().build().isBetterThan(newBuilder().build(), newBuilder().build()))
      .isFalse()
  }

  /**
   * https://developer.android.com/guide/topics/resources/providing-resources.html#MccQualifier
   * @see [http://mcc-mnc.com/](http://mcc-mnc.com/)
   */
  @Test
  fun isBetterThan_mcc() {
    // When requested is less of a match
    assertThat(
        newBuilder()
          .setMcc(MCC_US_CARRIER)
          .build()
          .isBetterThan(newBuilder().setMcc(MCC_US_CARRIER).build(), newBuilder().build())
      )
      .isFalse()

    // When requested is a better match
    assertThat(
        newBuilder()
          .setMcc(MCC_US_CARRIER)
          .build()
          .isBetterThan(newBuilder().build(), newBuilder().setMcc(MCC_US_CARRIER).build())
      )
      .isTrue()
  }

  /**
   * https://developer.android.com/guide/topics/resources/providing-resources.html#MccQualifier
   * @see [http://mcc-mnc.com/](http://mcc-mnc.com/)
   */
  @Test
  fun isBetterThan_mnc() {
    // When a configuration is not specified the result is always false
    assertThat(newBuilder().build().isBetterThan(newBuilder().build(), newBuilder().build()))
      .isFalse()

    // When requested is less of a match
    assertThat(
        newBuilder()
          .setMcc(MCC_US_CARRIER)
          .setMnc(MCC_US_VERIZON)
          .build()
          .isBetterThan(newBuilder().setMcc(MCC_US_CARRIER).build(), newBuilder().build())
      )
      .isFalse()

    // When requested is a better match - any US Carrier is a better match to US + Verizon
    assertThat(
        newBuilder()
          .setMcc(MCC_US_CARRIER)
          .setMnc(MCC_US_VERIZON)
          .build()
          .isBetterThan(newBuilder().build(), newBuilder().setMcc(MCC_US_CARRIER).build())
      )
      .isTrue()

    // When requested is a better match - any US Carrier is a better match to US + Verizon
    assertThat(
        newBuilder()
          .setMcc(MCC_US_CARRIER)
          .setMnc(MCC_US_VERIZON)
          .build()
          .isBetterThan(
            newBuilder().setMcc(MCC_US_CARRIER).build(),
            newBuilder().setMcc(MCC_US_CARRIER).setMnc(MCC_US_VERIZON).build()
          )
      )
      .isTrue()

    // When requested is a better match - any US Carrier is not a better match to US + Verizon
    assertThat(
        newBuilder()
          .setMcc(MCC_US_CARRIER)
          .setMnc(MCC_US_VERIZON)
          .build()
          .isBetterThan(
            newBuilder().setMcc(MCC_US_CARRIER).setMnc(MCC_US_VERIZON).build(),
            newBuilder().setMcc(MCC_US_CARRIER).build()
          )
      )
      .isFalse()
  }

  @Test
  fun isBetterThan_language() {
    // When requested has no language, is not a better match
    assertThat(
        newBuilder()
          .setLanguage(LANGUAGE_FRENCH)
          .build()
          .isBetterThan(newBuilder().setLanguage(LANGUAGE_FRENCH).build(), newBuilder().build())
      )
      .isFalse()
  }

  @Test
  fun isBetterThan_language_comparedNotSame_requestedEnglish() {
    // When requested has no language, is not a better match
    assertThat(
        newBuilder()
          .setLanguage(LANGUAGE_FRENCH)
          .build()
          .isBetterThan(
            newBuilder().setLanguage(LANGUAGE_SPANISH).build(),
            newBuilder().setLanguage(ResTable_config.kEnglish).build()
          )
      )
      .isTrue()
  }

  @Test
  fun isBetterThan_language_comparedNotSame_requestedEnglishUS() {
    // When requested has no language, is not a better match
    Truth.assertThat(
        newBuilder()
          .setLanguage(LANGUAGE_FRENCH)
          .build()
          .isBetterThan(
            newBuilder().setLanguage(LANGUAGE_SPANISH).build(),
            newBuilder().setLanguage(ResTable_config.kEnglish).build()
          )
      )
      .isTrue()
  }

  @Test
  fun isBetterThan_layoutDirection_() {
    // Requested matches this configuration
    assertThat(
        newBuilder()
          .setLayoutDirection(ResTable_config.SCREENLAYOUT_LAYOUTDIR_RTL)
          .build()
          .isBetterThan(
            newBuilder().setLayoutDirection(ResTable_config.SCREENLAYOUT_LAYOUTDIR_LTR).build(),
            newBuilder().setLayoutDirection(ResTable_config.SCREENLAYOUT_LAYOUTDIR_RTL).build()
          )
      )
      .isTrue()

    // Requested matches this configuration
    assertThat(
        newBuilder()
          .setLayoutDirection(ResTable_config.SCREENLAYOUT_LAYOUTDIR_LTR)
          .build()
          .isBetterThan(
            newBuilder().setLayoutDirection(ResTable_config.SCREENLAYOUT_LAYOUTDIR_RTL).build(),
            newBuilder().setLayoutDirection(ResTable_config.SCREENLAYOUT_LAYOUTDIR_RTL).build()
          )
      )
      .isFalse()
  }

  public class ResTableConfigBuilder {
    var mcc = 0
    var mnc = 0
    var language = ByteArray(2)
    var region = ByteArray(2)
    var orientation = 0
    var touchscreen = 0
    var density = 0
    var keyboard = 0
    var navigation = 0
    var inputFlags = 0
    var screenWidth = 0
    var screenHeight = 0
    var sdkVersion = 0
    var minorVersion = 0
    var screenLayout = 0
    var uiMode = 0
    var smallestScreenWidthDp = 0
    var screenWidthDp = 0
    var screenHeightDp = 0
    var localeScript = ByteArray(4)
    var localeVariant = ByteArray(8)
    var screenLayout2: Byte = 0
    var screenConfigPad1: Byte = 0
    var screenConfigPad2: Short = 0

    fun build(): ResTable_config {
      return ResTable_config(
        0,
        mcc,
        mnc,
        language,
        region,
        orientation,
        touchscreen,
        density,
        keyboard,
        navigation,
        inputFlags,
        screenWidth,
        screenHeight,
        sdkVersion,
        minorVersion,
        screenLayout,
        uiMode,
        smallestScreenWidthDp,
        screenWidthDp,
        screenHeightDp,
        localeScript,
        localeVariant,
        screenLayout2,
        screenConfigPad1,
        screenConfigPad2,
        null
      )
    }

    fun setMcc(mcc: Int): ResTableConfigBuilder {
      this.mcc = mcc
      return this
    }

    fun setMnc(mnc: Int): ResTableConfigBuilder {
      this.mnc = mnc
      return this
    }

    fun setLanguage(language: ByteArray): ResTableConfigBuilder {
      this.language = language
      return this
    }

    fun setLayoutDirection(layoutDirection: Int): ResTableConfigBuilder {
      screenLayout = layoutDirection
      return this
    }
  }
}
