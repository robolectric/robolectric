package com.xtremelabs.robolectric.res;

import android.graphics.Color;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

public class ColorResourceLoader extends XpathResourceXmlLoader implements ResourceValueConverter {
    private ResourceReferenceResolver<Integer> colorResolver = new ResourceReferenceResolver<Integer>("color");
    private static Map<String, Integer> androidColors = new HashMap<String, Integer>();

    static {
        androidColors.put("black", Color.BLACK);
        androidColors.put("darkgray", Color.DKGRAY);
        androidColors.put("gray", Color.GRAY);
        androidColors.put("lightgray", Color.LTGRAY);
        androidColors.put("white", Color.WHITE);
        androidColors.put("red", Color.RED);
        androidColors.put("green", Color.GREEN);
        androidColors.put("blue", Color.BLUE);
        androidColors.put("yellow", Color.YELLOW);
        androidColors.put("cyan", Color.CYAN);
        androidColors.put("magenta", Color.MAGENTA);
    }

    public ColorResourceLoader(ResourceExtractor resourceExtractor) {
        super(resourceExtractor, "/resources/color");
    }

    public int getValue(int colorId) {
        String resourceName = resourceExtractor.getResourceName(colorId);
        if (resourceName == null) {
            return -1;
        }

        Integer colorResolverValue = colorResolver.getValue(resourceName);
        return colorResolverValue == null ? -1 : colorResolverValue;
    }

    @Override
    protected void processNode(Node node, String name, boolean isSystem) {
        colorResolver.processResource(name, node.getTextContent(), this, isSystem);
    }

    @Override
    public Integer convertRawValue(String rawValue) {
        if (rawValue.startsWith("#")) {
            long color = Long.parseLong(rawValue.substring(1), 16);
            return (int) color;
        } else if (androidColors.containsKey(rawValue)) {
            return androidColors.get(rawValue);
        }
        return null;
    }
}
