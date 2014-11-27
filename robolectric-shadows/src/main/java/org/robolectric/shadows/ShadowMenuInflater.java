package org.robolectric.shadows;

import android.content.Context;
import android.view.*;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.res.MenuNode;
import org.robolectric.res.ResourceLoader;

/**
 * Shadow of {@code MenuInflater} that actually inflates menus into {@code View}s that are
 * functional enough to support testing.
 */
@Implements(MenuInflater.class)
public class ShadowMenuInflater {
  private Context context;
  private ResourceLoader resourceLoader;

  public void __constructor__(Context context) {
    this.context = context;
    resourceLoader = Shadows.shadowOf(context).getResourceLoader();
  }

  @Implementation
  public void inflate(int resource, Menu root) {
    String qualifiers = Shadows.shadowOf(context.getResources().getConfiguration()).getQualifiers();
    MenuNode menuNode = resourceLoader.getMenuNode(resourceLoader.getResourceIndex().getResName(resource), qualifiers);

    try {
      addChildrenInGroup(menuNode, 0, root);
    } catch (Exception e) {
      throw new RuntimeException("error inflating " + Shadows.shadowOf(context).getResName(resource), e);
    }
  }

  private void addChildrenInGroup(MenuNode source, int groupId, Menu root) {
    for (MenuNode child : source.getChildren()) {
      String name = child.getName();
      RoboAttributeSet attributes = Shadows.shadowOf(context).createAttributeSet(child.getAttributes(), null);
      if (name.equals("item")) {
        if (child.isSubMenuItem()) {
          SubMenu sub = root.addSubMenu(groupId,
              attributes.getAttributeResourceValue(ResourceLoader.ANDROID_NS, "id", 0),
              0, attributes.getAttributeValue(ResourceLoader.ANDROID_NS, "title"));
          MenuNode subMenuNode = child.getChildren().get(0);
          addChildrenInGroup(subMenuNode, groupId, sub);
        } else {
          String menuItemTitle = attributes.getAttributeValue(ResourceLoader.ANDROID_NS, "title");
          if (isFullyQualifiedName(menuItemTitle)) {
            menuItemTitle = getStringResourceValue(attributes);
          }

          int orderInCategory = attributes.getAttributeIntValue(ResourceLoader.ANDROID_NS, "orderInCategory", 0);
          int menuItemId = attributes.getAttributeResourceValue(ResourceLoader.ANDROID_NS, "id", 0);
          MenuItem item = root.add(groupId, menuItemId, orderInCategory, menuItemTitle);

          addActionViewToItem(item, attributes);
        }
      } else if (name.equals("group")) {
        int newGroupId = attributes.getAttributeResourceValue(ResourceLoader.ANDROID_NS, "id", 0);
        addChildrenInGroup(child, newGroupId, root);
      }
    }
  }

  private String getStringResourceValue(RoboAttributeSet attributes) {
    int menuItemTitleId = attributes.getAttributeResourceValue(ResourceLoader.ANDROID_NS, "title", 0);
    return context.getString(menuItemTitleId);
  }

  private boolean isFullyQualifiedName(String menuItemTitle) {
    return menuItemTitle != null && menuItemTitle.startsWith("@");
  }

  private void addActionViewToItem(MenuItem item, RoboAttributeSet attributes) {
    String actionViewClassName = attributes.getAttributeValue(ResourceLoader.ANDROID_NS, "actionViewClass");
    if (actionViewClassName != null) {
      try {
        View actionView = (View) Class.forName(actionViewClassName).getConstructor(Context.class).newInstance(context);
        item.setActionView(actionView);
      } catch (Exception e) {
        throw new RuntimeException("Action View class not found!", e);
      }
    }
  }
}
