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

    private String qualifiers = "";

    public RoboLayoutInflater(ResourceExtractor resourceExtractor, ResBundle<ViewNode> viewNodes) {
        this.resourceExtractor = resourceExtractor;
        this.viewNodes = viewNodes;
    }

    public View inflate(Context context, ViewNode viewNode, ViewGroup parent) throws Exception {
        if (viewNode.isInclude()) {
            List<Attribute> viewNodeAttributes = viewNode.getAttributes();
            Attribute layoutAttribute = Attribute.find(viewNodeAttributes, ATTR_LAYOUT);
            ResName resName = new ResName(layoutAttribute.qualifiedValue());
            return inflateView(context, resName.namespace, resName.name, viewNodeAttributes, parent);
        } else {
            View view = viewNode.create(context, parent);

            for (ViewNode child : viewNode.getChildren()) {
                inflate(context, child, (ViewGroup) view);
            }

            if (view != null) {
                viewNode.invokeOnFinishInflate(view);
            }
            return view;
        }
    }

    public View inflateView(Context context, int resourceId, ViewGroup parent) {
        ResName resName = resourceExtractor.getResName(resourceId);
        View viewNode = inflateView(context, resName.namespace, resName.name, parent);
        if (viewNode != null) return viewNode;

        throw new RuntimeException("Could not find layout " + resName);
    }

    public View inflateView(Context context, String packageName, String layoutName, List<Attribute> attributes, ViewGroup parent) {
        ViewNode viewNode = getViewNodeByLayoutName(packageName + ":layout/" + layoutName);
        if (viewNode == null) {
            throw new RuntimeException("Could not find layout " + layoutName);
        }

        try {
            return this.inflate(context, viewNode.plusAttributes(attributes), parent);
        } catch (I18nException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("error inflating " + layoutName, e);
        }
    }

    public View inflateView(Context context, String packageName, String key, ViewGroup parent) {
        return inflateView(context, packageName, key, new ArrayList<Attribute>(), parent);
    }

    private ViewNode getViewNodeByLayoutName(String layoutName) {
        return viewNodes.get(layoutName, qualifiers);
    }

    public void setQualifiers(String qualifiers) {
        this.qualifiers = qualifiers;
    }
}
