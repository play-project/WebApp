package models.translator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import java.util.Collection;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.translators.wsn.notify.NotificationTranslator;

/**
 * Translates a WS-Notification notification payload to an {@link Event}. The
 * handler translates only the leaves of the XML tree to {@link Quadruple}s.
 * Then, when all the leavea are translated, it is possible to retrieve an
 * {@link Event} associated to the set of Quadruples that have been translated.
 * 
 * @author lpellegr
 */
public class WsNotifNotificationToEventHandler extends DefaultHandler {

    private List<Quadruple> quadruples;

    private LinkedList<Element> elements;

    private String lastTextNodeRead;

    private Node graphNode;

    private Node topicFullQName;

    private Map<String, XSDDatatype> elementDatatypes;

    public static final String URI_SEPARATOR = "$0$";
    
    public WsNotifNotificationToEventHandler(String graphValue,
            Map<String, XSDDatatype> elementDatatypes) {
        this.elements = new LinkedList<Element>();
        this.quadruples = new ArrayList<Quadruple>();
        this.graphNode = Node.createURI(graphValue);
        this.elementDatatypes = elementDatatypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {

        if (this.elements.size() > 1
                && this.elements.getLast().getFullQName().equals(
                        "http://docs.oasis-open.org/wsn/b-2/Message")) {
            this.topicFullQName = Node.createURI(uri + "/" + localName);
        }

        this.elements.add(new Element(uri, localName
        // , qName, attributes
        ));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (!this.lastTextNodeRead.trim().isEmpty()) {
            this.parseQuadruple(this.lastTextNodeRead);
        }
        this.elements.removeLast();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        this.lastTextNodeRead = new String(ch, start, length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endDocument() throws SAXException {
        // replaces null subjects by the topicFullQName
        for (int i = 0; i < this.quadruples.size(); i++) {
            Quadruple quad = this.quadruples.get(i);
            if (quad.getSubject() == null) {
                this.quadruples.set(i, new Quadruple(
                        quad.getGraph(), this.topicFullQName,
                        quad.getPredicate(), quad.getObject()));
            }
        }
    }

    private void parseQuadruple(String textNode) {
        StringBuilder predicate = new StringBuilder();
        for (int i = 0; i < this.elements.size(); i++) {
            predicate.append(this.elements.get(i).getFullQName());
            if (i < this.elements.size() - 1) {
                predicate.append(URI_SEPARATOR);
            }
        }

        Node object = null;
        // try to annotate the object value by leveraging the information from
        // the XSD file
        if (this.elementDatatypes != null) {
            XSDDatatype datatype =
                    this.elementDatatypes.get(this.elements.getLast().localName);
            if (datatype != null) {
                object = Node.createLiteral(textNode, null, datatype);
                // System.out.println("@@@@@@ Data TYPE = "+datatype.toString());
            } else {
                datatype = getDatatype(textNode);
                // System.out.println("@@@@@@ Data TYPE = "+datatype.toString());
                object = Node.createLiteral(textNode, null, datatype);
            }
        }

        if (object == null) {
            XSDDatatype datatype = getDatatype(textNode);
            if (datatype != null) {
                object = Node.createLiteral(textNode, null, datatype);
            } else {
                Node.createLiteral(textNode);
            }
        }

        this.quadruples.add(new Quadruple(
                this.graphNode, this.topicFullQName,
                Node.createURI(predicate.toString()), object, false, true));
    }

    private XSDDatatype getDatatype(String textNode) {
        // anticipate datatype
        XSDDatatype expectedType = new XSDDatatype("anySimpleType");
        DatatypeFactory dataFactory;
        try {
            dataFactory = DatatypeFactory.newInstance();

            // DateTimeDateFormat dt =new DateTimeDateFormat();

            try {
                Integer.parseInt(textNode);
                // System.out.println("the string parsed is of type INT = "+textNode);
                expectedType = XSDDatatype.XSDint;
                // System.out.println("XSD EXPECTED DATA TYPE IS INT = "+textNode+"====="+expectedType.toString());
            } catch (NumberFormatException e) {
                try {
                    Float.parseFloat(textNode);
                    // System.out.println("the string parsed is of type FLOAT = "+textNode);
                    expectedType = XSDDatatype.XSDfloat;
                    // System.out.println("XSD EXPECTED DATA TYPE IS FLOAT = "+textNode+"====="+expectedType.toString());
                } catch (NumberFormatException e1) {
                    try {
                        // Calendar cl= (Calendar) expectedType.parse(textNode);
                        // Date dd=dt.parse(textNode);
                        dataFactory.newXMLGregorianCalendar(textNode)
                                .toGregorianCalendar()
                                .getTime();
                        // System.out.println("the string parsed is of type XSD DATE TIME = "+textNode+dd.toString());
                        expectedType = XSDDatatype.XSDdateTime;
                        // System.out.println("XSD EXPECTED DATA TYPE IS DATETIME = "+textNode+"====="+expectedType.toString());
                    } catch (IllegalArgumentException e2) {
                        // System.out.println("the string parsed is of type STRING= "+textNode);
                        expectedType = XSDDatatype.XSDstring;
                        // System.out.println("XSD EXPECTED DATA TYPE IS STRING = "+textNode+"====="+expectedType.toString());

                        // e2.printStackTrace();
                    }

                }

            }
        } catch (DatatypeConfigurationException e3) {
            e3.printStackTrace();
        }

        return expectedType;
    }

    public List<Quadruple> getQuadruples() {
        return this.quadruples;
    }

    public CompoundEvent getEvent() {
        return new CompoundEvent(new ArrayList<Quadruple>(this.quadruples));
    }

    private static class Element {

        private final String uri;

        private final String localName;

        // private final String qName;

        // private final Attributes attributes;

        public Element(String uri, String localName
        // , String qName,
        // Attributes attributes
        ) {
            super();
            this.uri = uri;
            this.localName = localName;
            // this.qName = qName;
            // this.attributes = attributes;
        }

        public String getFullQName() {
            return this.uri + "/" + this.localName;
        }

    }

}