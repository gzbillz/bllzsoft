package org.jboss.bpm.console.server.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class DOMUtils {
	private static Logger log = LoggerFactory.getLogger(DOMUtils.class);

	private static ThreadLocal<Document> documentThreadLocal = new ThreadLocal<Document>();

	private static ThreadLocal<DocumentBuilder> builderThreadLocal = new ThreadLocal<DocumentBuilder>() {
		protected DocumentBuilder initialValue() {
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				factory.setValidating(false);
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				setEntityResolver(builder);
				return builder;
			} catch (ParserConfigurationException e) {
				throw new RuntimeException("Failed to create DocumentBuilder",
						e);
			}
		}

		private void setEntityResolver(DocumentBuilder builder) {
			String[] resolvers = {
					"org.jboss.ws.core.utils.JBossWSEntityResolver",
					"org.jboss.util.xml.JBossEntityResolver" };

			EntityResolver entityResolver = null;
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			for (String resolver : resolvers) {
				try {
					@SuppressWarnings("rawtypes")
					Class resolverClass = loader.loadClass(resolver);
					entityResolver = (EntityResolver) resolverClass
							.newInstance();
				} catch (Exception ex) {
					DOMUtils.log.debug("Cannot load: " + resolver);
				}
			}

			if (entityResolver != null)
				builder.setEntityResolver(entityResolver);
		}
	};

	public static void clearThreadLocals() {
		documentThreadLocal.remove();
		builderThreadLocal.remove();
	}

	public static DocumentBuilder getDocumentBuilder() {
		return (DocumentBuilder) builderThreadLocal.get();
	}

	public static Element parse(String xmlString) throws IOException {
		try {
			return parse(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));
		} catch (IOException e) {
			log.error("Cannot parse: " + xmlString);
			throw e;
		}
	}

	public static Element parse(InputStream xmlStream) throws IOException {
		try {
			return getDocumentBuilder().parse(xmlStream).getDocumentElement();
		} catch (SAXException se) {
			throw new IOException(se.toString());
		} finally {
			xmlStream.close();
		}
	}

	public static Element parse(InputSource source) throws IOException {
		try {
			@SuppressWarnings("unused")
			InputStream is;
			@SuppressWarnings("unused")
			Reader r;
			return getDocumentBuilder().parse(source).getDocumentElement();
		} catch (SAXException se) {
			throw new IOException(se.toString());
		} finally {
			InputStream is = source.getByteStream();
			if (is != null) {
				is.close();
			}
			Reader r = source.getCharacterStream();
			if (r != null) {
				r.close();
			}
		}
	}

	public static Element createElement(String localPart) {
		Document doc = getOwnerDocument();
		log.trace("createElement {}" + localPart);
		return doc.createElement(localPart);
	}

	public static Element createElement(String localPart, String prefix) {
		Document doc = getOwnerDocument();
		log.trace("createElement {}" + prefix + ":" + localPart);
		return doc.createElement(prefix + ":" + localPart);
	}

	public static Element createElement(String localPart, String prefix,
			String uri) {
		Document doc = getOwnerDocument();
		if ((prefix == null) || (prefix.length() == 0)) {
			log.trace("createElement {" + uri + "}" + localPart);
			return doc.createElementNS(uri, localPart);
		}

		log.trace("createElement {" + uri + "}" + prefix + ":" + localPart);
		return doc.createElementNS(uri, prefix + ":" + localPart);
	}

	public static Element createElement(QName qname) {
		return createElement(qname.getLocalPart(), qname.getPrefix(),
				qname.getNamespaceURI());
	}

	public static Text createTextNode(String value) {
		Document doc = getOwnerDocument();
		return doc.createTextNode(value);
	}

	public static QName getElementQName(Element el) {
		String qualifiedName = el.getNodeName();
		return resolveQName(el, qualifiedName);
	}

	public static QName resolveQName(Element el, String qualifiedName) {
		String prefix = "";
		String namespaceURI = "";
		String localPart = qualifiedName;

		int colIndex = qualifiedName.indexOf(":");
		if (colIndex > 0) {
			prefix = qualifiedName.substring(0, colIndex);
			localPart = qualifiedName.substring(colIndex + 1);

			if ("xmlns".equals(prefix)) {
				namespaceURI = "URI:XML_PREDEFINED_NAMESPACE";
			} else {
				Element nsElement = el;
				while ((namespaceURI.equals("")) && (nsElement != null)) {
					namespaceURI = nsElement.getAttribute("xmlns:" + prefix);
					if (namespaceURI.equals("")) {
						nsElement = getParentElement(nsElement);
					}
				}
			}
			if (namespaceURI.equals(""))
				throw new IllegalArgumentException(
						"Cannot find namespace uri for: " + qualifiedName);
		} else {
			Element nsElement = el;
			while ((namespaceURI.equals("")) && (nsElement != null)) {
				namespaceURI = nsElement.getAttribute("xmlns");
				if (namespaceURI.equals("")) {
					nsElement = getParentElement(nsElement);
				}
			}
		}
		QName qname = new QName(namespaceURI, localPart, prefix);
		return qname;
	}

	public static String getAttributeValue(Element el, String attrName) {
		return getAttributeValue(el, new QName(attrName));
	}

	public static String getAttributeValue(Element el, QName attrName) {
		String attr = null;
		if ("".equals(attrName.getNamespaceURI()))
			attr = el.getAttribute(attrName.getLocalPart());
		else
			attr = el.getAttributeNS(attrName.getNamespaceURI(),
					attrName.getLocalPart());

		if ("".equals(attr)) {
			attr = null;
		}
		return attr;
	}

	public static QName getAttributeValueAsQName(Element el, String attrName) {
		return getAttributeValueAsQName(el, new QName(attrName));
	}

	public static QName getAttributeValueAsQName(Element el, QName attrName) {
		QName qname = null;

		String qualifiedName = getAttributeValue(el, attrName);
		if (qualifiedName != null) {
			qname = resolveQName(el, qualifiedName);
		}

		return qname;
	}

	public static boolean getAttributeValueAsBoolean(Element el, String attrName) {
		return getAttributeValueAsBoolean(el, new QName(attrName));
	}

	public static boolean getAttributeValueAsBoolean(Element el, QName attrName) {
		String attrVal = getAttributeValue(el, attrName);
		boolean ret = ("true".equalsIgnoreCase(attrVal))
				|| ("1".equalsIgnoreCase(attrVal));
		return ret;
	}

	public static Integer getAttributeValueAsInteger(Element el, String attrName) {
		return getAttributeValueAsInteger(el, new QName(attrName));
	}

	public static Integer getAttributeValueAsInteger(Element el, QName attrName) {
		String attrVal = getAttributeValue(el, attrName);
		return attrVal != null ? new Integer(attrVal) : null;
	}

	public static Map<QName, String> getAttributes(Element el) {
		Map<QName, String> attmap = new HashMap<QName, String>();
		NamedNodeMap attribs = el.getAttributes();
		for (int i = 0; i < attribs.getLength(); i++) {
			Attr attr = (Attr) attribs.item(i);
			String name = attr.getName();
			QName qname = resolveQName(el, name);
			String value = attr.getNodeValue();
			attmap.put(qname, value);
		}
		return attmap;
	}

	public static void copyAttributes(Element destElement, Element srcElement) {
		NamedNodeMap attribs = srcElement.getAttributes();
		for (int i = 0; i < attribs.getLength(); i++) {
			Attr attr = (Attr) attribs.item(i);
			String uri = attr.getNamespaceURI();
			String qname = attr.getName();
			String value = attr.getNodeValue();

			if ((uri == null) && (qname.startsWith("xmlns"))) {
				log.trace("Ignore attribute: [uri=" + uri + ",qname=" + qname
						+ ",value=" + value + "]");
			} else {
				destElement.setAttributeNS(uri, qname, value);
			}
		}
	}

	public static boolean hasTextChildNodesOnly(Node node) {
		NodeList nodeList = node.getChildNodes();
		if (nodeList.getLength() == 0) {
			return false;
		}
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node acksToChildNode = nodeList.item(i);
			if (acksToChildNode.getNodeType() != 3) {
				return false;
			}
		}
		return true;
	}

	public static boolean hasChildElements(Node node) {
		NodeList nlist = node.getChildNodes();
		for (int i = 0; i < nlist.getLength(); i++) {
			Node child = nlist.item(i);
			if (child.getNodeType() == 1)
				return true;
		}
		return false;
	}

	public static Iterator<Element> getChildElements(Node node) {
		List<Element> list = new LinkedList<Element>();
		NodeList nlist = node.getChildNodes();
		for (int i = 0; i < nlist.getLength(); i++) {
			Node child = nlist.item(i);
			if (child.getNodeType() == 1)
				list.add((Element) child);
		}
		return list.iterator();
	}

	public static String getTextContent(Node node) {
		boolean hasTextContent = false;
		StringBuffer buffer = new StringBuffer();
		NodeList nlist = node.getChildNodes();
		for (int i = 0; i < nlist.getLength(); i++) {
			Node child = nlist.item(i);
			if (child.getNodeType() == 3) {
				buffer.append(child.getNodeValue());
				hasTextContent = true;
			}
		}
		return hasTextContent ? buffer.toString() : null;
	}

	public static Element getFirstChildElement(Node node) {
		return getFirstChildElement(node, false);
	}

	public static Element getFirstChildElement(Node node, boolean recursive) {
		return getFirstChildElementIntern(node, null, recursive);
	}

	public static Element getFirstChildElement(Node node, String nodeName) {
		return getFirstChildElement(node, nodeName, false);
	}

	public static Element getFirstChildElement(Node node, String nodeName,
			boolean recursive) {
		return getFirstChildElementIntern(node, new QName(nodeName), recursive);
	}

	public static Element getFirstChildElement(Node node, QName nodeName) {
		return getFirstChildElement(node, nodeName, false);
	}

	public static Element getFirstChildElement(Node node, QName nodeName,
			boolean recursive) {
		return getFirstChildElementIntern(node, nodeName, recursive);
	}

	private static Element getFirstChildElementIntern(Node node,
			QName nodeName, boolean recursive) {
		Element childElement = null;
		Iterator<Element> it = getChildElementsIntern(node, nodeName, recursive);
		if (it.hasNext()) {
			childElement = it.next();
		}
		return childElement;
	}

	public static Iterator<Element> getChildElements(Node node, String nodeName) {
		return getChildElements(node, nodeName, false);
	}
 
	public static Iterator<Element> getChildElements(Node node, String nodeName,
			boolean recursive) {
		return getChildElementsIntern(node, new QName(nodeName), recursive);
	}

	public static Iterator<Element> getChildElements(Node node, QName nodeName) {
		return getChildElements(node, nodeName, false);
	}

	public static Iterator<Element> getChildElements(Node node, QName nodeName,
			boolean recursive) {
		return getChildElementsIntern(node, nodeName, recursive);
	}

	public static List<Element> getChildElementsAsList(Node node,
			String nodeName) {
		return getChildElementsAsList(node, nodeName, false);
	}

	public static List<Element> getChildElementsAsList(Node node,
			String nodeName, boolean recursive) {
		return getChildElementsAsListIntern(node, new QName(nodeName),
				recursive);
	}

	public static List<Element> getChildElementsAsList(Node node, QName nodeName) {
		return getChildElementsAsList(node, nodeName, false);
	}

	public static List<Element> getChildElementsAsList(Node node,
			QName nodeName, boolean recursive) {
		return getChildElementsAsListIntern(node, nodeName, recursive);
	}

	private static List<Element> getChildElementsAsListIntern(Node node,
			QName nodeName, boolean recursive) {
		List<Element> list = new LinkedList<Element>();
		NodeList nlist = node.getChildNodes();
		for (int i = 0; i < nlist.getLength(); i++) {
			Node child = nlist.item(i);
			if (child.getNodeType() == 1) {
				search(list, (Element) child, nodeName, recursive);
			}
		}
		return list;
	}

	private static void search(List<Element> list, Element baseElement,
			QName nodeName, boolean recursive) {
		if (nodeName == null) {
			list.add(baseElement);
		} else {
			QName qname;
			if (nodeName.getNamespaceURI().length() > 0) {
				qname = new QName(baseElement.getNamespaceURI(),
						baseElement.getLocalName());
			} else {
				qname = new QName(baseElement.getLocalName());
			}
			if (qname.equals(nodeName)) {
				list.add(baseElement);
			}
		}
		if (recursive) {
			NodeList nlist = baseElement.getChildNodes();
			for (int i = 0; i < nlist.getLength(); i++) {
				Node child = nlist.item(i);
				if (child.getNodeType() == 1) {
					search(list, (Element) child, nodeName, recursive);
				}
			}
		}
	}

	private static Iterator<Element> getChildElementsIntern(Node node, QName nodeName,
			boolean recursive) {
		return getChildElementsAsListIntern(node, nodeName, recursive)
				.iterator();
	}

	public static Element getParentElement(Node node) {
		Node parent = node.getParentNode();
		return (parent instanceof Element) ? (Element) parent : null;
	}

	public static Document getOwnerDocument() {
		Document doc = (Document) documentThreadLocal.get();
		if (doc == null) {
			doc = getDocumentBuilder().newDocument();
			documentThreadLocal.set(doc);
		}
		return doc;
	}

	public static Element sourceToElement(Source source) throws IOException {
		Element retElement = null;
		try {
			if ((source instanceof StreamSource)) {
				StreamSource streamSource = (StreamSource) source;

				InputStream ins = streamSource.getInputStream();
				if (ins != null) {
					retElement = parse(ins);
				} else {
					Reader reader = streamSource.getReader();
					retElement = parse(new InputSource(reader));
				}
			} else if ((source instanceof DOMSource)) {
				DOMSource domSource = (DOMSource) source;
				Node node = domSource.getNode();
				if ((node instanceof Element)) {
					retElement = (Element) node;
				} else if ((node instanceof Document)) {
					retElement = ((Document) node).getDocumentElement();
				} else {
					throw new RuntimeException("Unsupported Node type: "
							+ node.getClass().getName());
				}
			} else if ((source instanceof SAXSource)) {
				TransformerFactory tf = TransformerFactory.newInstance();
				ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
				Transformer transformer = tf.newTransformer();
				transformer.setOutputProperty("omit-xml-declaration", "yes");
				transformer.setOutputProperty("method", "xml");
				transformer.transform(source, new StreamResult(baos));
				retElement = parse(new ByteArrayInputStream(baos.toByteArray()));
			} else {
				throw new RuntimeException("Source type not implemented: "
						+ source.getClass().getName());
			}

		} catch (TransformerException ex) {
			IOException ioex = new IOException();
			ioex.initCause(ex);
			throw ioex;
		}

		return retElement;
	}
}