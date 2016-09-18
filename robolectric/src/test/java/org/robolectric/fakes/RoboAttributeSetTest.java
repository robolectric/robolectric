package org.robolectric.fakes;

import android.content.Context;
import android.util.AttributeSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.res.Attribute;
import org.robolectric.res.ResName;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class RoboAttributeSetTest {

  @Test
  public void shouldCreateRoboAttributeSetFromVarargs() {
    Context context = RuntimeEnvironment.application;
    ResName resName = new ResName("android", "attr", "orientation");
    String attrValue = "vertical";
    String contextPackageName = context.getPackageName();

    ArrayList<Attribute> attributes = new ArrayList<>();
    attributes.add(new Attribute(resName, attrValue, contextPackageName));

    AttributeSet attributeSet = RoboAttributeSet.create(context, attributes);

    assertThat(attributeSet.getAttributeCount()).isEqualTo(1);
    assertThat(attributeSet.getAttributeName(0)).isEqualTo("android:orientation");
    assertThat(attributeSet.getAttributeValue(0)).isEqualTo(attrValue);
  }

  @Test
  public void shouldCreateRoboAttributeSetFromList() {
    Context context = RuntimeEnvironment.application;
    ResName resName = new ResName("android", "attr", "orientation");
    String attrValue = "vertical";
    String contextPackageName = context.getPackageName();

    Attribute attribute = new Attribute(resName, attrValue, contextPackageName);

    AttributeSet attributeSet = RoboAttributeSet.create(context, attribute);

    assertThat(attributeSet.getAttributeCount()).isEqualTo(1);
    assertThat(attributeSet.getAttributeName(0)).isEqualTo("android:orientation");
    assertThat(attributeSet.getAttributeValue(0)).isEqualTo(attrValue);
  }
}
