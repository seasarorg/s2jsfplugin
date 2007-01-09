/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.s2jsfplugin.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * カスタムタグライブラリのTLDファイルをパースして<code>TLDInfo</code>オブジェクトを作成します。
 * 
 * @author Naoki Takezoe
 */
public class TLDParser {

	private String uri = null;
	private String prefix = null;
	private ArrayList result = new ArrayList();

	/**
	 * TLDファイルをパースして<code>TLDInfo</code>オブジェクトとして返却します。
	 */
	public TLDInfo parse(InputStream in) throws Exception {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		builder.setEntityResolver(new TLDResolver());
		Document doc = builder.parse(new InputSource(in));
		Element element = doc.getDocumentElement();

		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node instanceof Element) {
				Element childElement = (Element) nodeList.item(i);
				String elementName = childElement.getNodeName();
				if (elementName.equals("tag")) {
					result.add(parseTagElement(childElement));
				} else if (elementName.equals("uri")) {
					uri = getChildText(childElement);
				} else if (elementName.equals("shortname") || elementName.equals("short-name")) {
					if (prefix == null) {
						prefix = getChildText(childElement);
					}
				}
			}
		}

		return new TLDInfo(uri, result);
	}

	/**
	 * tag要素を処理。
	 */
	private TagInfo parseTagElement(Element tag) {
		NodeList children = tag.getChildNodes();

		List attributes = new ArrayList();
		String name = null;
		String description = "";
		boolean hasBody = true;

		for (int j = 0; j < children.getLength(); j++) {
			Node node = children.item(j);
			if (node instanceof Element) {
				Element element = (Element) node;
				String elementName = element.getNodeName();
				if (elementName.equals("name")) {
					name = prefix + ":" + getChildText(element);
				} else if (elementName.equals("bodycontent") || elementName.equals("body-content")) {
					hasBody = !getChildText(element).equals("empty");
				} else if (elementName.equals("description")) {
					description = wrap(getChildText(element));
				} else if (elementName.equals("attribute")) {
					AttributeInfo attrInfo = parseAttributeElement(element);
					attributes.add(attrInfo);
				}
			}
		}

		TagInfo info = new TagInfo(name, hasBody);
		info.setDescription(description);
		for (int i = 0; i < attributes.size(); i++) {
			info.addAttributeInfo((AttributeInfo) attributes.get(i));
		}

		return info;
	}
	
	/**
	 * attribute要素を処理。
	 */
	private AttributeInfo parseAttributeElement(Element attr) {
		NodeList children = attr.getChildNodes();

		String name = null;
		String description = "";
		boolean required = false;

		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node instanceof Element) {
				Element element = (Element) node;
				String elementName = element.getNodeName();
				if (elementName.equals("name")) {
					name = getChildText(element);
				} else if (elementName.equals("description")) {
					description = wrap(getChildText(element));
				} else if (elementName.equals("required")) {
					if (getChildText(element).equals("true")) {
						required = true;
					} else {
						required = false;
					}
				}
			}
		}

		AttributeInfo attrInfo = new AttributeInfo(name, true,
				AttributeInfo.NONE, required);
		attrInfo.setDescription(description);
		return attrInfo;
	}

	/**
	 * <code>Element</code>ノード配下のテキストを取得する。
	 */
	private static String getChildText(Element element) {
		StringBuffer sb = new StringBuffer();
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node instanceof Text) {
				sb.append(node.getNodeValue());
			}
		}
		return sb.toString().trim().replaceAll("\\s+", " ");
	}
	
	/**
	 * 文字列を<strong>約</strong>40文字で折り返す。
	 */
	private static String wrap(String text) {
		StringBuffer sb = new StringBuffer();
		int word = 0;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (word > 40) {
				if (c == ' ' || c == '\t') {
					sb.append('\n');
					word = 0;
					continue;
				}
			}
			sb.append(c);
			word++;
		}
		return sb.toString();
	}

	/**
	 * TLDファイルのDTDをローカルから参照するためのEntityResolver
	 */
	private static class TLDResolver implements EntityResolver {
		public InputSource resolveEntity(String publicId, String systemId)
				throws SAXException, IOException {
			if (systemId != null && systemId.equals("http://java.sun.com/j2ee/dtds/web-jsptaglibrary_1_1.dtd")) {
				InputStream in = getClass().getResourceAsStream("/DTD/web-jsptaglibrary_1_1.dtd");
				return new InputSource(in);
			}
			if (systemId != null && systemId.equals("http://java.sun.com/dtd/web-jsptaglibrary_1_2.dtd")) {
				InputStream in = getClass().getResourceAsStream("/DTD/web-jsptaglibrary_1_2.dtd");
				return new InputSource(in);
			}
			return null;
		}
	}

}
