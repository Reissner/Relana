package eu.simuline.relana.sys;

import eu.simuline.relana.model.ClassLocator;
import eu.simuline.relana.model.InstanceLocator;

import eu.simuline.util.sgml.ParseExceptionHandler;
//import eu.simuline.util.sgml.AttributesImpl;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.Set;
import java.util.HashSet;

import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;


/**
 * Enables an xml parser to read a relana project file 
 * like <code>src/test/resources/eu/simuline/relana/proj.rml</code> 
 * serving both as {@link ContentHandler} and as {@link ParseExceptionHandler} 
 * as it implements {@link ProjectDesc} to hold the result of reading, 
 * i.e. the content. 
 *
 * Created: Thu Apr 28 22:03:26 2005
 *
 * @author <a href="mailto:ernst.reissner@simuline.eu">Ernst Reissner</a>
 * @version 1.0
 */
public final class Project 
    implements ProjectDesc, ContentHandler, ParseExceptionHandler {

    /* --------------------------------------------------------------------- *
     * fields                                                                *
     * --------------------------------------------------------------------- */

    private URL library;
    private ClassLocator baseClass;
    private final Set<InstanceLocator> outputEffects;

    /* --------------------------------------------------------------------- *
     * constructors                                                          *
     * --------------------------------------------------------------------- */

    public Project() {
	// InstanceLocator overwrites hashCode and is immutable 
	this.outputEffects = new HashSet<InstanceLocator>();
    } // Project constructor

    /* --------------------------------------------------------------------- *
     * getter methods implementing ProjectDesc                               *
     * --------------------------------------------------------------------- */
 
// Implementation of eu.simuline.relana.sys.ProjectDesc

    public URL getLibrary() {
	return this.library;
    }

    public ClassLocator getBaseClass() {
	return this.baseClass;
    }

    public Set<InstanceLocator> getOutputEffects() {
	return this.outputEffects;
    }

    /* --------------------------------------------------------------------- *
     * methods implementing ContentHandler                                   *
     * --------------------------------------------------------------------- */

    public void setDocumentLocator(Locator locator) {
      // is empty. 
    }

    public void startDocument() throws SAXException {
      // is empty. 
    }

    public void endDocument() throws SAXException {
      // is empty. 
    }

    public void startPrefixMapping(String prefix,
				   String uri)
	throws SAXException {
      // is empty. 
    }

    public void endPrefixMapping(String prefix)
	throws SAXException {
      // is empty. 
    }

    public void startElement(String namespaceURI,
			     String localName,
			     String qName,
			     Attributes atts)
	throws SAXException {
	if ("Rml".equals(qName)) {
	    this.baseClass = ClassLocator
		.getLocator(atts.getValue("baseClass"));
	    try {
		this.library = new URL(atts.getValue("library"));
	    } catch (MalformedURLException e) {
		throw new SAXException// NOPMD
		    ("Found malformed url \"" + atts.getValue("library") + 
		     "\". ");
	    }
	    return;
	}
	if ("Output".equals(qName)) {
	    InstanceLocator loc = InstanceLocator
		.getLocator(atts.getValue("effect"));
	    this.outputEffects.add(loc);
	}
    }

    public void endElement(String namespaceURI,
			   String localName,
			   String qName)
	throws SAXException {
	// is empty. 
	//this.events.add("TE</" + qName + ">");
    }

    public void characters(char[] chr,
			   int start,
			   int length)
	throws SAXException {
	// is empty. 
    }

    public void ignorableWhitespace(char[] chr,
				    int start,
				    int length)
	throws SAXException {
	// is empty. 
    }

    public void processingInstruction(String target,
				      String data)
	throws SAXException {
	//System.out.println("P<" + target + ">");
	// is empty. 
    }

    public void skippedEntity(String name)
	throws SAXException {
	// is empty. 
    }

    /* --------------------------------------------------------------------- *
     * methods implementing ParseExceptionHandler                            *
     * --------------------------------------------------------------------- */

    @edu.umd.cs.findbugs.annotations.SuppressWarnings
	(value = "UC_USELESS_OBJECT", 
	 justification = "use if activated ")
    public void foundMultipleAttribute(String attrName,
				       Object oldAttrValue) {
	// StringBuilder res = new StringBuilder();
	// res.append("Found second value for attribute \"");
	// res.append(attrName);
	// res.append("\"; overwritten ");
	// if (oldAttrValue == AttributesImpl.NO_VALUE) {
	//     res.append("no value. ");
	// } else {
	//     res.append("old value \"");
	//     res.append(oldAttrValue);
	//     res.append('\"');
	// }

	//this.events.add(res.toString());
    }


    public void foundIllegalCharInTag(char chr) {
	//this.events.add("exc: ill letter in tag: " + chr);
    }

    public void foundCharAfterEndOfEndTag(char chr) {
	//this.events.add("exc: etter after eo EndTag: " + chr);
    }

    public void foundUnexpectedEndOfDocument() {
      // is empty. 
    }

    /* --------------------------------------------------------------------- *
     * further methods                                                       *
     * --------------------------------------------------------------------- */

    public String toString() {
	StringBuilder res = new StringBuilder();
	res.append("\n<Rml library=\"");
	res.append(getLibrary().toString());
	res.append("\" baseClass=\"");
	res.append(getBaseClass().getPath().toString());
	res.append("\">\n</Rml>\n");
	return res.toString();
    }
} // Project
