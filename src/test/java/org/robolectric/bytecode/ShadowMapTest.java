package org.robolectric.bytecode;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ShadowMapTest {
    @Test public void equalsHashCode() throws Exception {
        ShadowMap a = new ShadowMap.Builder().addShadowClass("a", "b", true, false).build();
        ShadowMap b = new ShadowMap.Builder().addShadowClass("a", "b", true, false).build();
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());

        ShadowMap c = b.newBuilder().build();
        assertThat(c).isEqualTo(b);
        assertThat(c.hashCode()).isEqualTo(b.hashCode());

        ShadowMap d = new ShadowMap.Builder().addShadowClass("a", "x", true, false).build();
        assertThat(d).isNotEqualTo(a);
        assertThat(d.hashCode()).isNotEqualTo(b.hashCode());
    }
}
