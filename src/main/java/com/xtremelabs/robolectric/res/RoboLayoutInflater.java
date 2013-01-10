package com.xtremelabs.robolectric.res;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.xtremelabs.robolectric.tester.android.util.Attribute;
import com.xtremelabs.robolectric.tester.android.util.ResName;
import com.xtremelabs.robolectric.util.I18nException;

import java.util.ArrayList;
import java.util.List;

public class RoboLayoutInflater {
    public static final ResName ATTR_LAYOUT = new ResName(":attr/layout");

    private final ResourceExtractor resourceExtractor;
    private final ResBundle<ViewNode> viewNodes;

    public RoboLayoutInflater(ResourceExtractor resourceExtractor, ResBundle<ViewNode> viewNodes) {
        this.resourceExtractor = resourceExtractor;
        this.viewNodes = viewNodes;
    }

    public View inflate(Context context, ViewNode viewNode, ViewGroup parent, String qualifiers) throws Exception {
        if (viewNode.isInclude()) {
            List<Attribute> viewNodeAttributes = viewNode.getAttributes();
            Attribute layoutAttribute = Attribute.find(viewNodeAttributes, ATTR_LAYOUT);
            ResName resName = new ResName(layoutAttribute.qualifiedValue());
            return inflateView(context, resName.namespace, resName.name, viewNodeAttributes, parent, qualifiers);
        } else {
            View view = viewNode.create(context, parent);

            for (ViewNode child : viewNode.getChildren()) {
                inflate(context, child, (ViewGroup) view, qualifiers);
            }

            if (view != null) {
                viewNode.invokeOnFinishInflate(view);
            }
            return view;
        }
    }

    public View inflateView(Context context, int resourceId, ViewGroup parent, String qualifiers) {
        ResName resName = resourceExtractor.getResName(resourceId);
        View viewNode = inflateView(context, resName.namespace, resName.name, parent, qualifiers);
        if (viewNode != null) return viewNode;

        throw new RuntimeException("Could not find layout " + resName);
    }

    public View inflateView(Context context, String packageName, String layoutName, List<Attribute> attributes, ViewGroup parent, String qualifiers) {
        ViewNode viewNode = getViewNodeByLayoutName(packageName + ":layout/" + layoutName, qualifiers);
        if (viewNode == null) {
            throw new RuntimeException("Could not find layout " + layoutName);
        }

        try {
            return this.inflate(context, viewNode.plusAttributes(attributes), parent, qualifiers);
        } catch (I18nException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("error inflating " + layoutName, e);
        }
    }

    public View inflateView(Context context, String packageName, String key, ViewGroup parent, String qualifiers) {
        return inflateView(context, packageName, key, new ArrayList<Attribute>(), parent, qualifiers);
    }

    private ViewNode getViewNodeByLayoutName(String layoutName, String qualifiers) {
        return viewNodes.get(new ResName(layoutName), qualifiers);
    }
}
