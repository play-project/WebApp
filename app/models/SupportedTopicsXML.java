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
import play.mvc.Router;

/**
 * Extracts topics from an XML file
 * 
 * @author Alexandre Bourdin
 * 
 */
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

	public static void parseXMLTree(ArrayList<EventTopic> result, Element node, String path) {
		if (node != null) {
			String id = node.getNamespacePrefix() + ":" + node.getName();
			path += " > " + id;
			List<Attribute> att = node.getAttributes();
			Boolean isTopic = false;
			String title = node.getName();
			String desc = "No description available.";
			String icon = "/images/noicon.png";
			if (att != null) {
				isTopic = false;
				for (Attribute a : att) {
					if (a.getName().equals("topic") && a.getNamespacePrefix().equals("wstop")
							&& a.getValue().equals("true")) {
						isTopic = true;
					}
					if (a.getName().equals("icon") && a.getNamespacePrefix().equals("xhtml")) {
						icon = a.getValue();
					}
					if (a.getName().equals("title") && a.getNamespacePrefix().equals("dcterms")) {
						title = a.getValue();
					}
					if (a.getName().equals("description") && a.getNamespacePrefix().equals("dcterms")) {
						desc = a.getValue();
					}
				}
				if (isTopic) {
					result.add(new EventTopic(node.getNamespacePrefix(), node.getName(), node.getNamespace()
							.getURI(), title, icon, desc, path));
				}
			}
			List<Element> el = node.getChildren();
			for (Element e : el) {
				parseXMLTree(result, e, path);
			}
		}
	}
}