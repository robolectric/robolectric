package com.xtremelabs.robolectric.res;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import com.xtremelabs.robolectric.tester.android.util.Attribute;
import com.xtremelabs.robolectric.tester.android.util.ResName;
import com.xtremelabs.robolectric.util.I18nException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoboLayoutInflater {
    public static final ResName ATTR_LAYOUT = new ResName(":attr/layout");

    private final ResourceExtractor resourceExtractor;
    private final Map<String, ViewNode> viewNodesByLayoutName;

    private List<String> layoutQualifierSearchPath = new ArrayList<String>();

    public RoboLayoutInflater(ResourceExtractor resourceExtractor, Map<String, ViewNode> viewNodesByLayoutName) {
        this.resourceExtractor = resourceExtractor;
        this.viewNodesByLayoutName = viewNodesByLayoutName;
    }

    public View inflate(Context context, ViewNode viewNode, ViewGroup parent) throws Exception {
        if (viewNode.isInclude()) {
            List<Attribute> viewNodeAttributes = viewNode.getAttributes();
            Attribute layoutAttribute = Attribute.find(viewNodeAttributes, ATTR_LAYOUT);
            String layoutName = layoutAttribute.qualifiedValue();
            return inflateView(context, layoutName, viewNodeAttributes, parent);
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
        String resourceName = resourceExtractor.getResourceName(resourceId);
        View viewNode = inflateView(context, resourceName, parent);
        if (viewNode != null) return viewNode;

        throw new RuntimeException("Could not find layout " + resourceName);
    }

    public View inflateView(Context context, String layoutName, List<Attribute> attributes, ViewGroup parent) {
        ViewNode viewNode = getViewNodeByLayoutName(layoutName);
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

    public View inflateView(Context context, String key, ViewGroup parent) {
        return inflateView(context, key, new ArrayList<Attribute>(), parent);
    }

    private ViewNode getViewNodeByLayoutName(String layoutName) {
        String[] parts = layoutName.split("/");
        if (parts[0].endsWith(":layout") && !layoutQualifierSearchPath.isEmpty()) {
            String rawLayoutName = parts[1];
            for (String location : layoutQualifierSearchPath) {
                ViewNode foundNode = viewNodesByLayoutName.get(parts[0] + "-" + location + "/" + rawLayoutName);
                if (foundNode != null) {
                    return foundNode;
                }
            }
        }
        return viewNodesByLayoutName.get(layoutName);
    }

    public void setLayoutQualifierSearchPath(List<String> layoutQualifierSearchPath) {
        this.layoutQualifierSearchPath = layoutQualifierSearchPath;
    }
}
