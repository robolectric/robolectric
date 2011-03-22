package com.xtremelabs.robolectric.res;

import java.io.File;

import org.w3c.dom.Document;

import com.xtremelabs.robolectric.Robolectric;

import android.content.Context;
import android.preference.PreferenceScreen;

public class PreferenceLoader extends XmlLoader {

	public PreferenceLoader(ResourceExtractor resourceExtractor) {
		super(resourceExtractor);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void processResourceXml(File xmlFile, Document document,
			boolean isSystem) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public PreferenceScreen inflatePrefs(Context context, String key) {
		PreferenceScreen screen = Robolectric.newInstanceOf(PreferenceScreen.class);
		
		return screen;
	}

}
