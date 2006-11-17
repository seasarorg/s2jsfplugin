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
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * カスタムタグライブラリのTLDファイルをパースしてTLDInfoオブジェクトを作成します。
 * 
 * @author Naoki Takezoe
 */
public class TLDParser {
	public TLDInfo parse(InputStream in) throws Exception {
		SAXParserFactory spfactory = SAXParserFactory.newInstance();
		spfactory.setValidating(false);
		SAXParser parser = spfactory.newSAXParser();
		XMLReader reader = parser.getXMLReader();
		TLDSAXHandler handler = new TLDSAXHandler();
		reader.setEntityResolver(new TLDResolver());
		reader.setContentHandler(handler);
		reader.parse(new InputSource(in));
		
		TLDInfo info = new TLDInfo(handler.getUri(),handler.getResult());
		return info;
	}
	
	/**
	 * TLDファイルをパースして補完情報を作成するSAXハンドラ
	 */
	private static class TLDSAXHandler extends DefaultHandler {
		
		private int mode = 0;
		private String prevTag = null;
		private boolean hasBody = true;
		private ArrayList attributes = new ArrayList();
		private HashMap required = new HashMap();
		private String uri = null;
		private StringBuffer tagName = new StringBuffer();
		private StringBuffer attrName = new StringBuffer();
		
		private StringBuffer tagDesc = new StringBuffer();
		private StringBuffer attrDesc = new StringBuffer();
		private HashMap attrDescMap = new HashMap();
		
		private ArrayList result = new ArrayList();
		
		public TLDSAXHandler(){
		}
		
		public String getUri(){
			return uri;
		}
		
		public List getResult(){
			return result;
		}
		
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if(qName.equals("tag")){
				tagDesc.setLength(0);
				mode = 1;
			} else if(qName.equals("attribute")){
				attrDesc.setLength(0);
				mode = 2;
			}
			prevTag = qName;
		}
		
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if(qName.equals("tag")){
				TagInfo info = new TagInfo(tagName.toString(),hasBody);
				for(int i=0;i<attributes.size();i++){
					String attrName = (String)attributes.get(i);
					boolean required = false;
					if(this.required.get(attrName)!=null){
						required = true;
					}
					AttributeInfo attrInfo = new AttributeInfo(attrName,true,AttributeInfo.NONE,required);
					if(attrDescMap.get(attrName)!=null){
						attrInfo.setDescription(wrap((String)attrDescMap.get(attrName)));
					}
					info.addAttributeInfo(attrInfo);
				}
				if(tagDesc.length() > 0){
					info.setDescription(wrap(tagDesc.toString()));
				}
				result.add(info);
				mode    = 0;
				prevTag = null;
				hasBody = true;
				tagName.setLength(0);
				tagDesc.setLength(0);
				attrDescMap.clear();
				required.clear();
				attributes.clear();
			} else if(qName.equals("name") && mode==2){
				attributes.add(attrName.toString());
				attrName.setLength(0);
			} else if(qName.equals("description") && mode==2){
				if(attrDesc.length() > 0){
					attrDescMap.put(attributes.get(attributes.size()-1), attrDesc.toString());
					attrDesc.setLength(0);
				}
			}
		}
		
		public void characters(char[] ch, int start, int length) throws SAXException {
			StringBuffer sb = new StringBuffer();
			for(int i=start;i<start+length;i++){
				sb.append(ch[i]);
			}
			String value = sb.toString().trim();
			if(!value.equals("")){
				if(prevTag.equals("name")){
					if(mode==1){
						tagName.append(value);
					} else {
						attrName.append(value);
					}
				} else if(prevTag.equals("bodycontent")){
					if(value.equals("empty")){
						hasBody = false;
					} else {
						hasBody = true;
					}
				} else if(prevTag.equals("required")){
					if(value.equals("true")){
						required.put(attributes.get(attributes.size()-1),"true");
					}
				} else if(prevTag.equals("uri")){
					uri = value;
				} else if(prevTag.equals("description")){
					if(mode==1){
						tagDesc.append(value);
					} else if(mode==2){
						attrDesc.append(value);
					}
				}
			}
		}
		
		/**
		 * 文字列を40文字で折り返します。
		 * 
		 * @param text 文字列
		 * @return 40文字で折り返された文字列
		 */
		private static String wrap(String text){
			StringBuffer sb = new StringBuffer();
			int word = 0;
			for(int i=0;i<text.length();i++){
				char c = text.charAt(i);
				if(word > 40){
					if(c==' ' || c== '\t'){
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
	}
	
	/**
	 * TLDファイルのDTDをローカルから参照するためのEntityResolver
	 */
	private static class TLDResolver implements EntityResolver {
		public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
			if(systemId!=null && systemId.equals("http://java.sun.com/j2ee/dtds/web-jsptaglibrary_1_1.dtd")){
				InputStream in = getClass().getResourceAsStream("/DTD/web-jsptaglibrary_1_1.dtd");
				return new InputSource(in);
			}
			if(systemId!=null && systemId.equals("http://java.sun.com/dtd/web-jsptaglibrary_1_2.dtd")){
				InputStream in = getClass().getResourceAsStream("/DTD/web-jsptaglibrary_1_2.dtd");
				return new InputSource(in);
			}
			return null;
		}
	}
	

}
