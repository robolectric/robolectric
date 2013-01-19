package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.SubMenu;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.res.MenuNode;
import com.xtremelabs.robolectric.res.ResourceLoader;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;
import com.xtremelabs.robolectric.util.I18nException;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

/**
 * Shadow of {@code MenuInflater} that actually inflates menus into {@code View}s that are functional enough to
 * support testing.
 */

@Implements(MenuInflater.class)
public class ShadowMenuInflater {
    private Context context;
    private ResourceLoader resourceLoader;
    private boolean strictI18n;

    public void __constructor__(Context context) {
        this.context = context;
        resourceLoader = shadowOf(context).getResourceLoader();
        strictI18n = shadowOf(context).isStrictI18n();
    }

    @Implementation
    public void inflate(int resource, Menu root) {
        String qualifiers = shadowOf(context.getResources().getConfiguration()).getQualifiers();
        MenuNode menuNode = resourceLoader.getMenuNode(resourceLoader.getResourceExtractor().getResName(resource), qualifiers);

        try {
            addChildrenInGroup(menuNode, 0, root);
        } catch (I18nException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("error inflating " + shadowOf(context).getResName(resource), e);
        }
    }

    private void addChildrenInGroup(MenuNode source, int groupId, Menu root) {
        for (MenuNode child : source.getChildren()) {
            String name = child.getName();
            TestAttributeSet attributes = shadowOf(context).createAttributeSet(child.getAttributes(), null);
            if (strictI18n) {
                attributes.validateStrictI18n();
            }
            if (name.equals("item")) {
                if (child.isSubMenuItem()) {
                    SubMenu sub = root.addSubMenu(groupId,
                            attributes.getAttributeResourceValue("android", "id", 0),
                            0, attributes.getAttributeValue("android", "title"));
                    MenuNode subMenuNode = child.getChildren().get(0);
                    addChildrenInGroup(subMenuNode, groupId, sub);
                } else {
                    root.add(groupId,
                            attributes.getAttributeResourceValue("android", "id", 0),
                            0, attributes.getAttributeValue("android", "title"));
                }
            } else if (name.equals("group")) {
                int newGroupId = attributes.getAttributeResourceValue("android", "id", 0);
                addChildrenInGroup(child, newGroupId, root);
            }
        }
    }
}
