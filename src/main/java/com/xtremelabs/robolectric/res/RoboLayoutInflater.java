package com.xtremelabs.robolectric.res;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.xtremelabs.robolectric.tester.android.util.Attribute;
import com.xtremelabs.robolectric.tester.android.util.ResName;
import com.xtremelabs.robolectric.util.I18nException;

import java.util.List;

public class RoboLayoutInflater {
    public static final ResName ATTR_LAYOUT = new ResName(":attr/layout");

    private final ResourceLoader resourceLoader;

    public RoboLayoutInflater(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private View doInflate(Context context, ViewNode viewNode, ViewGroup parent, String qualifiers) {
        if (viewNode.isInclude()) {
            List<Attribute> viewNodeAttributes = viewNode.getAttributes();
            Attribute layoutAttribute = Attribute.find(viewNodeAttributes, ATTR_LAYOUT);
            ResName resName = new ResName(layoutAttribute.qualifiedValue());
            return inflateView(context, resName, viewNodeAttributes, parent, qualifiers);
        } else {
            View view = viewNode.create(context, parent);

            for (ViewNode child : viewNode.getChildren()) {
                doInflate(context, child, (ViewGroup) view, qualifiers);
            }

            if (view != null) {
                viewNode.invokeOnFinishInflate(view);
            }
            return view;
        }
    }

    public View inflateView(Context context, int resourceId, ViewGroup parent, String qualifiers) {
        ViewNode viewNode = resourceLoader.getLayoutViewNode(resourceId, qualifiers);
        if (viewNode == null) {
            String name = resourceLoader.getNameForId(resourceId);
            throw new RuntimeException("Could not find layout " + (name == null ? resourceId : name));
        }

        View view = doInflate(context, viewNode, parent, qualifiers);
        if (view != null) return view;

        throw new RuntimeException("Could not find layout " + resourceId);
    }

    public View inflateView(Context context, ResName resName, List<Attribute> attributes, ViewGroup parent, String qualifiers) {
        ViewNode viewNode = resourceLoader.getLayoutViewNode(resName, qualifiers);
        if (viewNode == null) {
            throw new RuntimeException("Could not find layout " + resName.name);
        }

        try {
            return doInflate(context, viewNode.plusAttributes(attributes), parent, qualifiers);
        } catch (I18nException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("error inflating " + resName.name, e);
        }
    }
}
