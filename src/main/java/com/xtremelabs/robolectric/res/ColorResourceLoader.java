package com.xtremelabs.robolectric.res;

import android.graphics.Color;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

public class ColorResourceLoader extends XpathResourceXmlLoader implements ResourceValueConverter {
    private ResourceReferenceResolver<Integer> colorResolver = new ResourceReferenceResolver<Integer>("color");
    private static Map<String, Integer> androidColors = new HashMap<String, Integer>();
    private static Map<Integer, Integer> androidColorsFromIds = new HashMap<Integer, Integer>();


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

        androidColors.put("@android:color/black", Color.BLACK);
        androidColors.put("@android:color/darkgray", Color.DKGRAY);
        androidColors.put("@android:color/gray", Color.GRAY);
        androidColors.put("@android:color/lightgray", Color.LTGRAY);
        androidColors.put("@android:color/white", Color.WHITE);
        androidColors.put("@android:color/red", Color.RED);
        androidColors.put("@android:color/green", Color.GREEN);
        androidColors.put("@android:color/blue", Color.BLUE);
        androidColors.put("@android:color/yellow", Color.YELLOW);
        androidColors.put("@android:color/cyan", Color.CYAN);
        androidColors.put("@android:color/magenta", Color.MAGENTA);

        androidColorsFromIds.put(android.R.color.black, Color.BLACK);
        androidColorsFromIds.put(android.R.color.white, Color.WHITE);
    }

    public ColorResourceLoader(ResourceExtractor resourceExtractor) {
        super(resourceExtractor, "/resources/color");
    }

    public int getValue(int colorId) {
        if (androidColorsFromIds.containsKey(colorId)) {
            return androidColorsFromIds.get(colorId);
        } else {
            String resourceName = resourceExtractor.getResourceName(colorId);
            return resourceName == null ? -1 : colorResolver.getValue(resourceName);
        }
    }

    @Override
    protected void processNode(Node node, String name, boolean ignored) {
        colorResolver.processResource(name, node.getTextContent(), this);
    }

    @Override
    public Integer convertRawValue(String rawValue) {
        if (rawValue.startsWith("#")) {
            return (int) (long) Long.valueOf(rawValue.replaceAll("#", ""), 16);
        } else if (androidColors.containsKey(rawValue)) {
            return androidColors.get(rawValue);
        }
        return null;
    }
}
