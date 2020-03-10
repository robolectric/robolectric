package org.robolectric.spek

import org.spekframework.spek2.dsl.GroupBody

var runningSpec : RobolectricInstanceFactory.SpecState? = null

fun GroupBody.useRobolectric() {
    beforeEachTest {
        runningSpec!!.beforeTest()
    }

    afterEachTest {
        runningSpec!!.afterTest()
    }
}