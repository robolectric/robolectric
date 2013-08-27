package org.robolectric.shadows;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.res.MenuNode;
import org.robolectric.res.ResourceLoader;
import org.robolectric.util.I18nException;

import static org.fest.reflect.core.Reflection.type;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.res.ResourceLoader.ANDROID_NS;

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
    MenuNode menuNode = resourceLoader.getMenuNode(resourceLoader.getResourceIndex().getResName(resource), qualifiers);

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
      RoboAttributeSet attributes = shadowOf(context).createAttributeSet(child.getAttributes(), null);
      if (strictI18n) {
        attributes.validateStrictI18n();
      }
      if (name.equals("item")) {
        if (child.isSubMenuItem()) {
          SubMenu sub = root.addSubMenu(groupId,
              attributes.getAttributeResourceValue(ANDROID_NS, "id", 0),
              0, attributes.getAttributeValue(ANDROID_NS, "title"));
          MenuNode subMenuNode = child.getChildren().get(0);
          addChildrenInGroup(subMenuNode, groupId, sub);
        } else {
          MenuItem item = root.add(groupId,
              attributes.getAttributeResourceValue(ANDROID_NS, "id", 0),
              0, attributes.getAttributeValue(ANDROID_NS, "title"));

          addActionViewToItem(item, attributes);
        }
      } else if (name.equals("group")) {
        int newGroupId = attributes.getAttributeResourceValue(ANDROID_NS, "id", 0);
        addChildrenInGroup(child, newGroupId, root);
      }
    }
  }

  private void addActionViewToItem(MenuItem item, RoboAttributeSet attributes) {
    String actionViewClassName = attributes.getAttributeValue(ANDROID_NS, "actionViewClass");
    if (actionViewClassName != null) {
      try {
        View actionView = (View) type(actionViewClassName).load().getConstructor(Context.class).newInstance(context);
        item.setActionView(actionView);
      } catch (Exception e) {
        throw new RuntimeException("Action View class not found!", e);
      }
    }
  }
}
