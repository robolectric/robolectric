package org.robolectric.android;

import android.annotation.IdRes;
import android.util.AttributeSet;
import org.robolectric.Robolectric;

/**
 * Builder of {@link AttributeSet}s.
 */
public interface AttributeSetBuilder extends Robolectric.AttributeSetBuilder {

  /**
   * Set an attribute to the given value.
   *
   * The value will be interpreted according to the attribute's format.
   *
   * @param resId The attribute resource id to set.
   * @param value The value to set.
   * @return This {@link AttributeSetBuilder}.
   */
  @Override
  AttributeSetBuilder addAttribute(@IdRes int resId, String value);

  /**
   * Set the style attribute to the given value.
   *
   * The value will be interpreted as a resource reference.
   *
   * @param value The value for the specified attribute in this {@link AttributeSet}.
   * @return This {@link AttributeSetBuilder}.
   */
  @Override
  AttributeSetBuilder setStyleAttribute(String value);

  /**
   * Set the class attribute to the given value.
   *
   * The value will be interpreted as a class name.
   *
   * @param value The value for this {@link AttributeSet}'s {@code class} attribute.
   * @return This {@link AttributeSetBuilder}.
   */
  AttributeSetBuilder setClassAttribute(String value);

  /**
   * Set the id attribute to the given value.
   *
   * The value will be interpreted as an element id name.
   *
   * @param value The value for this {@link AttributeSet}'s {@code id} attribute.
   * @return This {@link AttributeSetBuilder}.
   */
  AttributeSetBuilder setIdAttribute(String value);

  /**
   * Build an {@link AttributeSet} with the antecedent attributes.
   *
   * @return A new {@link AttributeSet}.
   */
  @Override
  AttributeSet build();

}
