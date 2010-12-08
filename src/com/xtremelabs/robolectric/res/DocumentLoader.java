package com.xtremelabs.robolectric.res;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class DocumentLoader {
	private final XmlLoader[] xmlLoaders;
	private final DocumentBuilderFactory documentBuilderFactory;
	private FileFilter xmlFileFilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.getName().endsWith(".xml");
		}
	};

	public DocumentLoader(XmlLoader... xmlLoaders) {
		this.xmlLoaders = xmlLoaders;

		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		documentBuilderFactory.setIgnoringComments(true);
		documentBuilderFactory.setIgnoringElementContentWhitespace(true);
	}

	public void loadResourceXmlDirs(File... resourceXmlDirs) throws Exception {
		for (File resourceXmlDir : resourceXmlDirs) {
			loadResourceXmlDir(resourceXmlDir);
		}
	}

	public void loadResourceXmlDir(File resourceXmlDir) throws Exception {
		if (!resourceXmlDir.exists()) {
			throw new RuntimeException("no such directory " + resourceXmlDir);
		}

		File[] files = resourceXmlDir.listFiles(xmlFileFilter);
		Arrays.sort(files, new Comparator<File>() {
			public int compare(File f1, File f2) {

				if (f1.getName().contains("strings")) {
					return -1;
				}

				if (f2.getName().contains("strings")) {
					return 1;
				}

				return 0;
			}
		});

		for (File file : files) {
			loadResourceXmlFile(file);
		}
	}

	public void loadResourceXmlFile(File file) throws Exception {
		for (XmlLoader xmlLoader : xmlLoaders) {
			xmlLoader.processResourceXml(file, parse(file));
		}
	}

	private Document parse(File xmlFile) throws Exception {
		DocumentBuilder documentBuilder = documentBuilderFactory
				.newDocumentBuilder();
		return documentBuilder.parse(xmlFile);
	}

}
