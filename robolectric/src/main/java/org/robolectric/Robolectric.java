package org.robolectric;

import static org.robolectric.res.android.ResourceTypes.RES_XML_END_ELEMENT_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_XML_START_ELEMENT_TYPE;

import android.app.Activity;
import android.app.Fragment;
import android.app.Service;
import android.app.IntentService;
import android.app.backup.BackupAgent;
import android.content.ContentProvider;
import android.content.Intent;

import android.util.AttributeSet;
import android.view.View;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.android.controller.BackupAgentController;
import org.robolectric.android.controller.ContentProviderController;
import org.robolectric.android.controller.FragmentController;
import org.robolectric.android.controller.IntentServiceController;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.internal.ShadowProvider;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceTable;
import org.robolectric.res.android.DataType;
import org.robolectric.res.android.ResStringPool;
import org.robolectric.res.android.ResValue;
import org.robolectric.res.android.ResourceTypes.ResStringPool_header;
import org.robolectric.res.android.ResourceTypes.ResStringPool_header.Writer;
import org.robolectric.res.android.ResourceTypes.ResXMLTree_attrExt;
import org.robolectric.res.android.ResourceTypes.ResXMLTree_endElementExt;
import org.robolectric.res.android.ResourceTypes.ResXMLTree_header;
import org.robolectric.res.android.ResourceTypes.ResXMLTree_node;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.*;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ServiceLoader;

public class Robolectric {
  private static ShadowsAdapter shadowsAdapter = null;
  private static Iterable<ShadowProvider> providers;

  public static void reset() {
    if (providers == null) {
      providers = ServiceLoader.load(ShadowProvider.class);
    }
    for (ShadowProvider provider : providers) {
      provider.reset();
    }
    RuntimeEnvironment.application = null;
    RuntimeEnvironment.setRobolectricPackageManager(null);
    RuntimeEnvironment.setActivityThread(null);
  }

  public static ShadowsAdapter getShadowsAdapter() {
    synchronized (ShadowsAdapter.class) {
      if (shadowsAdapter == null) {
        shadowsAdapter = instantiateShadowsAdapter();
      }
    }
    return shadowsAdapter;
  }

  public static <T extends Service> ServiceController<T> buildService(Class<T> serviceClass) {
    return buildService(serviceClass, null);
  }

  public static <T extends Service> ServiceController<T> buildService(Class<T> serviceClass, Intent intent) {
    return ServiceController.of(getShadowsAdapter(), ReflectionHelpers.callConstructor(serviceClass), intent);
  }

  public static <T extends Service> T setupService(Class<T> serviceClass) {
    return buildService(serviceClass).create().get();
  }

  public static <T extends IntentService> IntentServiceController<T> buildIntentService(Class<T> serviceClass) {
    return buildIntentService(serviceClass, null);
  }

  public static <T extends IntentService> IntentServiceController<T> buildIntentService(Class<T> serviceClass, Intent intent) {
    return IntentServiceController.of(getShadowsAdapter(), ReflectionHelpers.callConstructor(serviceClass, new ReflectionHelpers.ClassParameter<String>(String.class, "IntentService")), intent);
  }

  public static <T extends IntentService> T setupIntentService(Class<T> serviceClass) {
    return buildIntentService(serviceClass).create().get();
  }

  public static <T extends ContentProvider> ContentProviderController<T> buildContentProvider(Class<T> contentProviderClass) {
    return ContentProviderController.of(ReflectionHelpers.callConstructor(contentProviderClass));
  }

  public static <T extends ContentProvider> T setupContentProvider(Class<T> contentProviderClass) {
    return buildContentProvider(contentProviderClass).create().get();
  }

  public static <T extends ContentProvider> T setupContentProvider(Class<T> contentProviderClass, String authority) {
    return buildContentProvider(contentProviderClass).create(authority).get();
  }

  public static <T extends Activity> ActivityController<T> buildActivity(Class<T> activityClass) {
    return buildActivity(activityClass, null);
  }

  public static <T extends Activity> ActivityController<T> buildActivity(Class<T> activityClass, Intent intent) {
    return ActivityController.of(getShadowsAdapter(), ReflectionHelpers.callConstructor(activityClass), intent);
  }

  public static <T extends Activity> T setupActivity(Class<T> activityClass) {
    return buildActivity(activityClass).setup().get();
  }

  public static <T extends Fragment> FragmentController<T> buildFragment(Class<T> fragmentClass) {
    return FragmentController.of(ReflectionHelpers.callConstructor(fragmentClass));
  }

  public static <T extends Fragment> FragmentController<T> buildFragment(Class<T> fragmentClass, Class<? extends Activity> activityClass) {
    return FragmentController.of(ReflectionHelpers.callConstructor(fragmentClass), activityClass);
  }

  public static <T extends Fragment> FragmentController<T> buildFragment(Class<T> fragmentClass, Intent intent) {
    return FragmentController.of(ReflectionHelpers.callConstructor(fragmentClass), intent);
  }

  public static <T extends Fragment> FragmentController<T> buildFragment(Class<T> fragmentClass, Class<? extends Activity> activityClass, Intent intent) {
    return FragmentController.of(ReflectionHelpers.callConstructor(fragmentClass), activityClass, intent);
  }

  public static <T extends BackupAgent> BackupAgentController<T> buildBackupAgent(Class<T> backupAgentClass) {
    return BackupAgentController.of(ReflectionHelpers.callConstructor(backupAgentClass));
  }

  public static <T extends BackupAgent> T setupBackupAgent(Class<T> backupAgentClass) {
    return buildBackupAgent(backupAgentClass).setUp().get();
  }

  /**
   * Allows for the programatic creation of an {@link AttributeSet} useful for testing {@link View} classes without
   * the need for creating XML snippets.
   */
  public static AttributeSetBuilder buildAttributeSet() {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setIgnoringComments(true);
    factory.setIgnoringElementContentWhitespace(true);
    Document document;
    try {
      DocumentBuilder documentBuilder = factory.newDocumentBuilder();
      document = documentBuilder.newDocument();
      Element dummy = document.createElementNS("http://schemas.android.com/apk/res/" + RuntimeEnvironment.application.getPackageName(), "dummy");
      document.appendChild(dummy);
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
    return new AttributeSetBuilder(RuntimeEnvironment.getCompileTimeResourceTable());
  }

  public static class AttributeSetBuilder {
    private static final ResName STYLE = ResName.qualifyResName("style", "", "");
    private ResourceTable appResourceTable;
    private Map<ResName, String> attributeNameToValue = new HashMap<>();

    AttributeSetBuilder(ResourceTable resourceTable) {
      this.appResourceTable = resourceTable;
    }

    public AttributeSetBuilder addAttribute(int resId, String value) {
      ResName resName = appResourceTable.getResName(resId);
      attributeNameToValue.put(resName, value);
      return this;
    }

    public AttributeSetBuilder setStyleAttribute(String value) {
      attributeNameToValue.put(STYLE, value);
      return this;
    }

    public AttributeSet build() {
//      XmlResourceParserImpl parser = new XmlResourceParserImpl(doc, null, RuntimeEnvironment.application.getPackageName(), RuntimeEnvironment.application.getPackageName(), appResourceTable);
//      try {
//        parser.next(); // Root document element
//        parser.next(); // "dummy" element
//      } catch (Exception e) {
//        throw new IllegalStateException("Expected single dummy element in the document to contain the attributes.", e);
//      }

      Class<?> xmlBlockClass = ReflectionHelpers
          .loadClass(this.getClass().getClassLoader(), "android.content.res.XmlBlock");

      ByteBuffer buf = ByteBuffer.allocate(16 * 1024).order(ByteOrder.LITTLE_ENDIAN);
      Writer resStringPoolWriter = new ResStringPool_header.Writer();

      ResXMLTree_header.write(buf, resStringPoolWriter, () -> {
        ResXMLTree_node.write(buf, RES_XML_START_ELEMENT_TYPE, () -> {
          new ResXMLTree_attrExt.Writer(buf, resStringPoolWriter, null, "dummy") {{
            for (Entry<ResName, String> entry : attributeNameToValue.entrySet()) {
              ResName resName = entry.getKey();
              String value = entry.getValue();
              DataType type;
              if (value == null) {
                type = DataType.NULL;
              } else if (value.startsWith("@")) {
                type = DataType.REFERENCE;
              } else {
                type = DataType.STRING;
              }

              ResValue resValue = new ResValue(type.code(), resStringPoolWriter.string(value));
              if (resName.equals(STYLE)) {
                attr(null, "style", value, resValue);
              } else {
                attr(resName.packageName, resName.name, value, resValue);
              }
            }
          }}.write();
        });
        ResXMLTree_node.write(buf, RES_XML_END_ELEMENT_TYPE, () -> {
          ResXMLTree_endElementExt.write(buf, resStringPoolWriter, null, "dummy");
        });
      });

      int size = buf.position();
      byte[] bytes = new byte[size];
      buf.position(0);
      buf.get(bytes, 0, size);

//      final int xmlChunkStart = xmlBytes.position();
//
//       begin RES_XML_FIRST_CHUNK_TYPE
//      xmlBytes.putShort((short) RES_XML_FIRST_CHUNK_TYPE) // type
//          .putShort((short) 8) // headerSize
//          .putInt(0); // size

      Object xmlBlockInstance = ReflectionHelpers
          .callConstructor(xmlBlockClass, ClassParameter.from(byte[].class, bytes));

      return ReflectionHelpers.callInstanceMethod(xmlBlockClass, xmlBlockInstance, "newParser");
    }
  }


  /**
   * Return the foreground scheduler (e.g. the UI thread scheduler).
   *
   * @return  Foreground scheduler.
   */
  public static Scheduler getForegroundThreadScheduler() {
    return ShadowApplication.getInstance().getForegroundThreadScheduler();
  }

  /**
   * Execute all runnables that have been enqueued on the foreground scheduler.
   */
  public static void flushForegroundThreadScheduler() {
    getForegroundThreadScheduler().advanceToLastPostedRunnable();
  }

  /**
   * Return the background scheduler.
   *
   * @return  Background scheduler.
   */
  public static Scheduler getBackgroundThreadScheduler() {
    return ShadowApplication.getInstance().getBackgroundThreadScheduler();
  }

  /**
   * Execute all runnables that have been enqueued on the background scheduler.
   */
  public static void flushBackgroundThreadScheduler() {
    getBackgroundThreadScheduler().advanceToLastPostedRunnable();
  }

  private static ShadowsAdapter instantiateShadowsAdapter() {
    ShadowsAdapter result = null;
    for (ShadowsAdapter adapter : ServiceLoader.load(ShadowsAdapter.class)) {
      if (result == null) {
        result = adapter;
      } else {
        throw new RuntimeException("Multiple " + ShadowsAdapter.class.getCanonicalName() + "s found.  Robolectric has loaded multiple core shadow modules for some reason.");
      }
    }
    if (result == null) {
      throw new RuntimeException("No shadows modules found containing a " + ShadowsAdapter.class.getCanonicalName());
    } else {
      return result;
    }
  }
}
