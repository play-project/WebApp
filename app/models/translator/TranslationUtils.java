package models.translator;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

import fr.inria.eventcloud.api.CompoundEvent;

public class TranslationUtils {

	public static CompoundEvent translateWsNotifNotificationToEvent(InputStream xmlPayload,
			InputStream xsdPayload, URI eventId) {
		Map<String, XSDDatatype> elementDatatypes = null;
		if (xsdPayload != null) {
			XsdHandler xsdHandler = new XsdHandler();
			executeSaxParser(xsdPayload, null, xsdHandler);
			elementDatatypes = xsdHandler.getElementDatatypes();
		} else {

		}

		WsNotifNotificationToEventHandler handler = new WsNotifNotificationToEventHandler(eventId.toString(),
				elementDatatypes);
		executeSaxParser(xmlPayload, xsdPayload, handler);

		return handler.getEvent();
	}

	private static void executeSaxParser(InputStream in, InputStream xsd, DefaultHandler handler) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(in, handler);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
