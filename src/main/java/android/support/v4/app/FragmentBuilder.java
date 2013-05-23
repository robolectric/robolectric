package android.support.v4.app;

import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.res.ResourceLoader.ANDROID_NS;

import java.util.List;

import org.robolectric.res.Attribute;
import org.robolectric.res.ViewNode;

import android.util.AttributeSet;
import android.view.View;

public class FragmentBuilder {
  
  public static Fragment create(ViewNode viewNode, FragmentActivity activity) {

    List<Attribute> attributes = viewNode.getAttributes();
    AttributeSet attributeSet = shadowOf(activity).createAttributeSet(attributes, View.class);
    
    String fname = Attribute.find(attributes, "android:attr/name").value;
    
    String tag = attributeSet.getAttributeValue(ANDROID_NS, "tag");
    int id = attributeSet.getAttributeResourceValue(ANDROID_NS, "id", 0);
  
    Fragment fragment = Fragment.instantiate(activity, fname);
    fragment.mFromLayout = true;
    fragment.mFragmentId = id;
    fragment.mTag = tag;
    fragment.mInLayout = true;
    fragment.mFragmentManager = activity.mFragments;
    fragment.onInflate(activity, attributeSet, fragment.mSavedFragmentState);
      
    return fragment;
  }

}
