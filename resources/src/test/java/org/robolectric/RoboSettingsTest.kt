package org.robolectric

import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RoboSettingsTest {

    private var originalUseGlobalScheduler = false

    @Before
    fun setUp() {
        originalUseGlobalScheduler = RoboSettings.isUseGlobalScheduler()
    }

    @After
    fun tearDown() {
        RoboSettings.setUseGlobalScheduler(originalUseGlobalScheduler)
    }

    @Test
    fun getIsUseGlobalScheduler_defaultFalse(){
        assertFalse(RoboSettings.isUseGlobalScheduler())
        }

    @Test
    fun setUseGlobalScheduler() {
        RoboSettings.setUseGlobalScheduler(true)
        assertTrue(RoboSettings.isUseGlobalScheduler())
    }
}