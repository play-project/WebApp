package models;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

import javax.xml.parsers.DocumentBuilderFactory;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import play.Logger;
import play.Play;

public class SupportedTopicsXML {

	private static InputStream inputStreamFrom(String file) {
		InputStream is = null;
		if (file != null) {
			try {
				is = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return is;
	}

	public static ArrayList<EventTopic> getSupportedTopics() {
		ArrayList<EventTopic> topics = new ArrayList<EventTopic>();
		SAXBuilder sxb = new SAXBuilder();
		Document xml = new Document();
		Element root = null;
		try {
			xml = sxb.build(new File("/root/webapp/public/xml/SupportedTopicsSet.xml"));
			root = xml.getRootElement();
		} catch (Exception e) {
			Logger.error("Error while parsing XML document");
		}
		parseXMLTree(topics, root, "");
		return topics;
	}

	private static void parseXMLTree(ArrayList<EventTopic> result, Element node, String path) {
		String id = node.getNamespacePrefix() + ":" + node.getName();
		path += " > " + id;
		List<Attribute> att = node.getAttributes();
		if (att != null) {
			for (Attribute a : att) {
				if (a.getName().equals("topic") && a.getNamespacePrefix().equals("wstop")
						&& a.getValue().equals("true")) {
					result.add(new EventTopic(node.getNamespacePrefix(), node.getName(), node.getNamespace()
							.getURI(), node.getName(), "Path : " + path));
				}
			}
		}
		List<Element> el = node.getChildren();
		for (Element e : el) {
			parseXMLTree(result, e, path);
		}
	}

	public static String convertStreamToString(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}
}