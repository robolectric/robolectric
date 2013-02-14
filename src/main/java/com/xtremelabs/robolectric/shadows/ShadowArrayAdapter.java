package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.res.ResourceLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

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
    private boolean notifyOnChange = true;

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
        if (notifyOnChange)
            notifyDataSetChanged();
    }

    @Implementation
    public void clear() {
        list.clear();
        if (notifyOnChange)
            notifyDataSetChanged();
    }

    @Implementation
    public void remove(T object) {
        list.remove(object);
        if (notifyOnChange)
            notifyDataSetChanged();
    }

    @Implementation
    public void insert(T object, int index) {
        list.add(index, object);
        if (notifyOnChange)
            notifyDataSetChanged();
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
            view = getResourceLoader().inflateView(context,resource, null);
        } else {
            view = convertView;
        }

        TextView text;
        if (textViewResourceId == 0) {
            text = (TextView) view;
        } else {
            text = (TextView) view.findViewById(textViewResourceId);
        }

        if (item instanceof CharSequence) {
            Robolectric.shadowOf(text).setText((CharSequence)item);
        } else {
        	Robolectric.shadowOf(text).setText(item.toString());
        }

        return view;
    }

    @Implementation
    public Filter getFilter() {
        return STUB_FILTER;
    }

    @Override
    @Implementation
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        notifyOnChange = true;
    }

    @Implementation
    public void setNotifyOnChange(boolean notifyOnChange) {
        this.notifyOnChange = notifyOnChange;
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