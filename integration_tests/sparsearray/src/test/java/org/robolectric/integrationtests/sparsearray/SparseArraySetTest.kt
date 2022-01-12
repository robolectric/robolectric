package org.robolectric.integrationtests.sparsearray

import android.util.SparseArray
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// Despite being a bug for pre-31, we don't want to break 31+ with our change, so we test all SDKs
@Config(sdk = [Config.ALL_SDKS])
class SparseArraySetTest {

  val sparseArray = SparseArray<String>()

  // See https://github.com/robolectric/robolectric/issues/6840
  @Test
  fun testSparseArrayBracketOperator_callsSetMethodPreApi31() {
    sparseArray[0] = "Blizzard"
    sparseArray[1] = "Blizzara"

    assertThat(sparseArray[0]).isEqualTo("Blizzard")
    assertThat(sparseArray[1]).isEqualTo("Blizzara")
  }

  @Test
  fun testSparseArraySetFunction_callsSetMethodPreApi31() {
    sparseArray.set(0, "Blizzaga")
    sparseArray.set(1, "Blizzaja")

    assertThat(sparseArray[0]).isEqualTo("Blizzaga")
    assertThat(sparseArray[1]).isEqualTo("Blizzaja")
  }
}
