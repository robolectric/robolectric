package com.xtremelabs.droidsugar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(DroidSugarAndroidTestRunner.class)
public class OuterTest {
    private int counter;

//    @Before
//    public void setUp() throws Exception {
//        counter = 1;
//    }

    @Test
    public void shouldOuter() throws Exception {
        assertThat(counter, equalTo(1));
    }

    @Test
    public void shouldOuterFail() throws Exception {
        assertThat(counter, equalTo(0));
    }

    public static class StaticInnerTest {
        private int counter;

        @Before
        public void setUp() throws Exception {
            counter++;
        }

        @Test
        public void shouldPass() throws Exception {
            assertThat(counter, equalTo(2));
        }

        @Test
        public void shouldFail() throws Exception {
            assertThat(counter, equalTo(1));
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public class NonStaticInnerTest {
        @Before
        public void setUp() throws Exception {
            counter++;
        }

        @Test
        public void shouldPass() throws Exception {
            assertThat(counter, equalTo(2));
        }

        @Test
        public void shouldFail() throws Exception {
            assertThat(counter, equalTo(1));
        }
    }
}
