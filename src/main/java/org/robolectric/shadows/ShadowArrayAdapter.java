package org.robolectric.shadows;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.builder.LayoutBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.robolectric.Robolectric.shadowOf;

@SuppressWarnings( { "UnusedDeclaration" })
@Implements(ArrayAdapter.class)
public class ShadowArrayAdapter<T> extends ShadowBaseAdapter {

  private static final Filter STUB_FILTER = new Filter() {
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
      return null;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
    }
  };

  private Context context;
  private List<T> list;
  private int resource;
  private int textViewResourceId;
  private Filter filter;

  public int getTextViewResourceId() {
    return textViewResourceId;
  }

  public int getResourceId() {
    return resource;
  }

  public void __constructor__(Context context, int textViewResourceId) {
    init(context, textViewResourceId, 0, new ArrayList<T>());
  }

  public void __constructor__(Context context, int resource, int textViewResourceId) {
    init(context, resource, textViewResourceId, new ArrayList<T>());
  }

  public void __constructor__(Context context, int textViewResourceId, T[] objects) {
    init(context, textViewResourceId, 0, Arrays.asList(objects));
  }

  public void __constructor__(Context context, int resource, int textViewResourceId, T[] objects) {
    init(context, resource, textViewResourceId, Arrays.asList(objects));
  }

  public void __constructor__(Context context, int textViewResourceId, List<T> objects) {
    init(context, textViewResourceId, 0, objects);
  }

  public void __constructor__(Context context, int resource, int textViewResourceId, List<T> objects) {
    init(context, resource, textViewResourceId, objects);
  }

  private void init(Context context, int resource, int textViewResourceId, List<T> objects) {
    this.context = context;
    this.list = objects;
    this.resource = resource;
    this.textViewResourceId = textViewResourceId;
  }

  @Implementation
  public void add(T object) {
    list.add(object);
  }

  @Implementation
  public void clear() {
    list.clear();
  }

  @Implementation
  public void remove(T object) {
    list.remove(object);
  }

  @Implementation
  public void insert(T object, int index) {
    list.add(index, object);
  }

  @Implementation
  public Context getContext() {
    return context;
  }

  @Implementation
  public int getCount() {
    return list.size();
  }

  @Implementation
  public T getItem(int position) {
    return list.get(position);
  }

  @Implementation
  public int getPosition(T item) {
    return list.indexOf(item);
  }

  @Implementation
  public View getView(int position, View convertView, ViewGroup parent) {
    T item = list.get(position);
    View view;

    if (convertView == null) {
      String qualifiers = shadowOf(context.getResources().getConfiguration()).getQualifiers();
      view = new LayoutBuilder(getResourceLoader()).inflateView(context, resource, null, qualifiers);
    } else {
      view = convertView;
    }

    TextView text;
    if (textViewResourceId == 0) {
      text = (TextView) view;
    } else {
      text = (TextView) view.findViewById(textViewResourceId);
    }
    text.setText(item instanceof CharSequence ? (CharSequence) item : item.toString());
    return view;
  }

  @Implementation
  public Filter getFilter() {
    return STUB_FILTER;
  }

  private ResourceLoader getResourceLoader() {
    return shadowOf(Robolectric.application).getResourceLoader();
  }

  @Implementation
  public static ArrayAdapter<CharSequence> createFromResource(Context context, int textArrayResId, int textViewResId) {
    CharSequence[] strings = context.getResources().getTextArray(textArrayResId);
    return new ArrayAdapter<CharSequence>(context, textViewResId, strings);
  }
}