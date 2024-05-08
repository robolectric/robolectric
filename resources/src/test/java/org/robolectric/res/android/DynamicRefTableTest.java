package org.robolectric.res.android;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.res.android.Errors.BAD_TYPE;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.res.android.ResourceTypes.Res_value;

@RunWith(JUnit4.class)
public final class DynamicRefTableTest {

  private static final Ref<Res_value> RES_VALUE_OF_BAD_TYPE =
      new Ref<>(new Res_value(/* dataType= */ (byte) 99, /* data= */ 0));

  @Test
  public void lookupResourceValue_returnsBadTypeIfTypeOutOfEnumRange() {
    DynamicRefTable pseudoRefTable =
        new DynamicRefTable(/* packageId= */ (byte) 0, /* appAsLib= */ true);
    assertThat(pseudoRefTable.lookupResourceValue(RES_VALUE_OF_BAD_TYPE)).isEqualTo(BAD_TYPE);
  }
}
