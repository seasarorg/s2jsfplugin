/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
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
package org.seasar.s2jsfplugin.editor;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * JSF用のDTDファイルを解決するためのEntityResolverです。
 * 
 * @author Naoki Takezoe
 */
public class FacesConfigDTDResolver implements EntityResolver {
	
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		InputStream in = getInputStream(systemId);
		if(in!=null){
			return new InputSource(in);
		}
		return null;
	}
	
	public InputStream getInputStream(String systemId){
		if(systemId!=null && systemId.equals("http://java.sun.com/dtd/web-facesconfig_1_0.dtd")){
			return getClass().getResourceAsStream("/DTD/web-facesconfig_1_0.dtd");
		}
		if(systemId!=null && systemId.equals("http://java.sun.com/dtd/web-facesconfig_1_1.dtd")){
			return getClass().getResourceAsStream("/DTD/web-facesconfig_1_1.dtd");
		}
		return null;
	}
}
