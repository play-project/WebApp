package models.translator;

import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.impl.dv.SchemaDVFactory;
import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.util.SymbolHash;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

/**
 * Extracts the XSD datatype associated to the leaf elements and creates a Map
 * that maintain the leaf element name with the Jena {@link XSDDatatype}
 * associated to the XSD datatype.
 * 
 * @author lpellegr
 */
public class XsdHandler extends DefaultHandler {

    public static final String XML_SCHEMA_NAMESPACE =
            "http://www.w3.org/2001/XMLSchema";

    private static final Map<String, XSDDatatype> datatypes;

    private Map<String, XSDDatatype> elementDatatypes;

    public static void main(String[] args) {
        // used to generate the code that initialize the datatypes map below
        SymbolHash types = SchemaDVFactory.getInstance().getBuiltInTypes();
        Object[] values = new Object[types.getLength()];
        types.getValues(values, 0);
        System.out.println("datatypes = new HashMap<String, XSDDatatype>("
                + types.getLength() + ", 1);");
        for (int i = 0; i < values.length; i++) {
            if (values[i] instanceof XSSimpleTypeDecl) {
                XSSimpleTypeDecl decl = (XSSimpleTypeDecl) values[i];
                System.out.println("datatypes.put(\"" + decl.getName()
                        + "\", XSDDatatype.XSD" + decl.getName() + ");");
            }
        }
    }

    static {
        datatypes = new HashMap<String, XSDDatatype>(41, 1);
        datatypes.put("byte", XSDDatatype.XSDbyte);
        datatypes.put("integer", XSDDatatype.XSDinteger);
        datatypes.put("ENTITY", XSDDatatype.XSDENTITY);
        datatypes.put("ID", XSDDatatype.XSDID);
        datatypes.put("NCName", XSDDatatype.XSDNCName);
        datatypes.put("boolean", XSDDatatype.XSDboolean);
        datatypes.put("base64Binary", XSDDatatype.XSDbase64Binary);
        datatypes.put("nonPositiveInteger", XSDDatatype.XSDnonPositiveInteger);
        datatypes.put("gYearMonth", XSDDatatype.XSDgYearMonth);
        datatypes.put("unsignedByte", XSDDatatype.XSDunsignedByte);
        datatypes.put("nonNegativeInteger", XSDDatatype.XSDnonNegativeInteger);
        datatypes.put("Name", XSDDatatype.XSDName);
        datatypes.put("NOTATION", XSDDatatype.XSDNOTATION);
        datatypes.put("positiveInteger", XSDDatatype.XSDpositiveInteger);
        datatypes.put("duration", XSDDatatype.XSDduration);
        datatypes.put("gMonthDay", XSDDatatype.XSDgMonthDay);
        datatypes.put("token", XSDDatatype.XSDtoken);
        datatypes.put("double", XSDDatatype.XSDdouble);
        datatypes.put("negativeInteger", XSDDatatype.XSDnegativeInteger);
        datatypes.put("float", XSDDatatype.XSDfloat);
        datatypes.put("date", XSDDatatype.XSDdate);
        datatypes.put("long", XSDDatatype.XSDlong);
        datatypes.put("normalizedString", XSDDatatype.XSDnormalizedString);
        datatypes.put("anyURI", XSDDatatype.XSDanyURI);
        datatypes.put("dateTime", XSDDatatype.XSDdateTime);
        datatypes.put("string", XSDDatatype.XSDstring);
        datatypes.put("unsignedInt", XSDDatatype.XSDunsignedInt);
        datatypes.put("IDREF", XSDDatatype.XSDIDREF);
        datatypes.put("unsignedShort", XSDDatatype.XSDunsignedShort);
        datatypes.put("unsignedLong", XSDDatatype.XSDunsignedLong);
        datatypes.put("QName", XSDDatatype.XSDQName);
        datatypes.put("gMonth", XSDDatatype.XSDgMonth);
        datatypes.put("gDay", XSDDatatype.XSDgDay);
        datatypes.put("hexBinary", XSDDatatype.XSDhexBinary);
        datatypes.put("gYear", XSDDatatype.XSDgYear);
        datatypes.put("time", XSDDatatype.XSDtime);
        datatypes.put("decimal", XSDDatatype.XSDdecimal);
        datatypes.put("language", XSDDatatype.XSDlanguage);
        datatypes.put("int", XSDDatatype.XSDint);
        datatypes.put("NMTOKEN", XSDDatatype.XSDNMTOKEN);
        datatypes.put("short", XSDDatatype.XSDshort);
    }

    public XsdHandler() {
        this.elementDatatypes = new HashMap<String, XSDDatatype>();
    }

    public Map<String, XSDDatatype> getElementDatatypes() {
        return this.elementDatatypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        // we are only interested by the declaration of the XSD elements
        if (uri.startsWith(XML_SCHEMA_NAMESPACE) && localName.equals("element")) {
            String lastName = null;
            String type = null;

            // from the attributes we look for the type associated to the
            // element name
            for (int i = 0; i < attributes.getLength(); i++) {
                if (attributes.getLocalName(i).startsWith("name")) {
                    lastName = attributes.getValue(i);
                }

                if (attributes.getLocalName(i).equals("type")
                        && attributes.getValue(i).startsWith(
                                qName.split(":")[0])) {
                    type = attributes.getValue(i);

                    XSDDatatype datatype = datatypes.get(type.split(":")[1]);
                    // if the type associated to the element name is a known XSD
                    // type we store the element name and its associated Jena
                    // Datatype
                    if (datatype != null) {
                        this.elementDatatypes.put(lastName, datatype);
                    }
                }
            }
        }
    }

}