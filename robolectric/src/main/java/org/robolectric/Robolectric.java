package org.robolectric;

import static org.robolectric.res.android.ResourceTypes.ANDROID_NS;
import static org.robolectric.res.android.ResourceTypes.AUTO_NS;
import static org.robolectric.res.android.ResourceTypes.RES_XML_END_ELEMENT_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_XML_RESOURCE_MAP_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_XML_START_ELEMENT_TYPE;

import android.app.Activity;
import android.app.Fragment;
import android.app.Service;
import android.app.IntentService;
import android.app.backup.BackupAgent;
import android.content.ContentProvider;
import android.content.Intent;

import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.android.controller.BackupAgentController;
import org.robolectric.android.controller.ContentProviderController;
import org.robolectric.android.controller.FragmentController;
import org.robolectric.android.controller.IntentServiceController;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.internal.ShadowProvider;
import org.robolectric.res.AttributeResource;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceTable;
import org.robolectric.res.android.DataType;
import org.robolectric.res.android.ResValue;
import org.robolectric.res.android.ResourceTypes.ResChunk_header;
import org.robolectric.res.android.ResourceTypes.ResStringPool_header;
import org.robolectric.res.android.ResourceTypes.ResStringPool_header.Writer;
import org.robolectric.res.android.ResourceTypes.ResXMLTree_attrExt;
import org.robolectric.res.android.ResourceTypes.ResXMLTree_endElementExt;
import org.robolectric.res.android.ResourceTypes.ResXMLTree_header;
import org.robolectric.res.android.ResourceTypes.ResXMLTree_node;
import org.robolectric.shadows.Converter;
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
    private static final int STYLE_RES_ID = Integer.MAX_VALUE - 2;
    private static final int CLASS_RES_ID = Integer.MAX_VALUE - 1;
    private static final int ID_RES_ID = Integer.MAX_VALUE;

    private ResourceTable appResourceTable;
    private Map<Integer, AttrInfo> attrToValue = new TreeMap<>();

    static class AttrInfo {
      private final String value;

      public AttrInfo(String value) {
        this.value = value;
      }

      public AttrInfo(int value) {
        this.value = "@" + value;
      }
    }

    AttributeSetBuilder(ResourceTable resourceTable) {
      this.appResourceTable = resourceTable;
    }

    public AttributeSetBuilder addAttribute(int resId, String value) {
      attrToValue.put(resId, new AttrInfo(value));
      return this;
    }

    public AttributeSetBuilder setStyleAttribute(String value) {
      attrToValue.put(STYLE_RES_ID, new AttrInfo(value));
      return this;
    }

    public AttributeSetBuilder setClassAttribute(String value) {
      attrToValue.put(CLASS_RES_ID, new AttrInfo(value));
      return this;
    }

    public AttributeSetBuilder setIdAttribute(String value) {
      attrToValue.put(ID_RES_ID, new AttrInfo(value));
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

      final SparseArray<Integer> resIds = new SparseArray<>();
      final int[] maxAttrNameIndex = new int[] { 0 };

      ResXMLTree_attrExt.Writer dummyStart = new ResXMLTree_attrExt.Writer(buf, resStringPoolWriter,
          null, "dummy") {
        {
          for (Entry<Integer, AttrInfo> entry : attrToValue.entrySet()) {
            Integer attrId = entry.getKey();
            String attrNs = "";
            String attrName;
            ResName attrResName = null;
            switch (attrId) {
              case STYLE_RES_ID:
                attrId = null;
                attrName = "style";
                break;

              case CLASS_RES_ID:
                attrId = null;
                attrName = "class";
                break;

              case ID_RES_ID:
                attrId = null;
                attrName = "id";
                break;

              default:
                attrResName = appResourceTable.getResName(attrId);
                attrNs = (attrResName.packageName.equals("android")) ? ANDROID_NS : AUTO_NS;
                attrName = attrResName.name;
            }

            String value = entry.getValue().value;
            DataType type;
            int valueInt = 0;

            if (attrResName != null) {
              ResourceTable resourceTable = RuntimeEnvironment.getAppResourceTable();
              TypedValue outValue = new TypedValue();
              AttributeResource attributeResource = new AttributeResource(attrResName, value,
                  RuntimeEnvironment.application.getPackageName());
              Converter.convert(resourceTable, attributeResource, outValue,
                  RuntimeEnvironment.getQualifiers(), true);

              type = DataType.fromCode(outValue.type);
              value = (String) outValue.string;
              valueInt = outValue.data;
            } else {
              if (value == null || AttributeResource.isNull(value)) {
                type = DataType.NULL;
              } else if (AttributeResource.isResourceReference(value)) {
                ResName resourceReference = AttributeResource
                    .getResourceReference(value, "unknown", "unknown");
                Integer valueResId = appResourceTable.getResourceId(resourceReference);
                type = DataType.REFERENCE;
                valueInt = valueResId;
              } else {
                type = DataType.STRING;
                valueInt = resStringPoolWriter.string(value);
              }
            }

            System.out.println(attrName + " type " + type + " value " + valueInt);
            ResValue resValue = new ResValue(type.code(), valueInt);

            int attrNameIndex = resStringPoolWriter.uniqueString(attrName);
            attr(resStringPoolWriter.string(attrNs), attrNameIndex,
                resStringPoolWriter.string(value), resValue, attrNs + ":" + attrName);
            if (attrId != null) {
              resIds.put(attrNameIndex, attrId);
            }
            maxAttrNameIndex[0] = Math.max(maxAttrNameIndex[0], attrNameIndex);
          }
        }
      };

      ResXMLTree_endElementExt.Writer dummyEnd =
          new ResXMLTree_endElementExt.Writer(buf, resStringPoolWriter, null, "dummy");

      int finalMaxAttrNameIndex = maxAttrNameIndex[0];
      ResXMLTree_header.write(buf, resStringPoolWriter, () -> {
        if (finalMaxAttrNameIndex > 0) {
          ResChunk_header.write(buf, (short) RES_XML_RESOURCE_MAP_TYPE, () -> {}, () -> {
            // not particularly compact, but no big deal for our purposes...
            for (int i = 0; i <= finalMaxAttrNameIndex; i++) {
              Integer value = resIds.get(i);
              buf.putInt(value == null ? 0 : value);
            }
          });
        }

        ResXMLTree_node.write(buf, RES_XML_START_ELEMENT_TYPE, dummyStart::write);
        ResXMLTree_node.write(buf, RES_XML_END_ELEMENT_TYPE, dummyEnd::write);
      });

      int size = buf.position();
      byte[] bytes = new byte[size];
      buf.position(0);
      buf.get(bytes, 0, size);

      Object xmlBlockInstance = ReflectionHelpers
          .callConstructor(xmlBlockClass, ClassParameter.from(byte[].class, bytes));

      AttributeSet parser = ReflectionHelpers.callInstanceMethod(xmlBlockClass, xmlBlockInstance,
          "newParser");
      ReflectionHelpers.callInstanceMethod(parser, "next");
      ReflectionHelpers.callInstanceMethod(parser, "next");

      return parser;
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
