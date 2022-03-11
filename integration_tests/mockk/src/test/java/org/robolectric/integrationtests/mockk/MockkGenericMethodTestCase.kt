package org.robolectric.integrationtests.mockk

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

class Entity

interface Repository {
  fun <T> get(key: String, type: Class<T>): T
}

@RunWith(RobolectricTestRunner::class)
class MockkGenericMethodTestCase {
  @Test
  fun `stubbing a generic method works`() {
    val entity = Entity()
    val repo: Repository = mockk { every { get(any(), Entity::class.java) } returns entity }

    assertThat(repo.get("a", Entity::class.java)).isEqualTo(entity)
  }
}
