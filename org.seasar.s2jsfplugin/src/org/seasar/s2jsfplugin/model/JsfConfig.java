/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
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

import java.util.HashMap;
import java.util.Map;

import org.seasar.jsf.TagConfig;
import org.seasar.jsf.TaglibManager;

/**
 * S2Ç…àÀë∂ÇµÇ»Ç¢JsfConfigÇÃé¿ëïÅB
 * 
 * @author Naoki Takezoe
 */
public class JsfConfig implements org.seasar.jsf.JsfConfig {

	private Map taglibUris = new HashMap();
	
	private Map prefixes = new HashMap();
	
	private TaglibManager taglibManager;
	
	public JsfConfig() {
	}

	public void addTaglibUri(String prefix, String uri) {
		taglibUris.put(prefix, uri);
		prefixes.put(uri, prefix);

	}

	public boolean hasTaglibUri(String prefix) {
		return taglibUris.containsKey(prefix);
	}

	public String getTaglibUri(String prefix) {
		String uri = (String) taglibUris.get(prefix);
		if (uri != null) {
			return uri;
		}
		throw new RuntimeException(prefix);
	}
	
	public String getTaglibPrefix(String uri) {
		String prefix = (String) prefixes.get(uri);
		if (prefix != null) {
			return prefix;
		}
		throw new RuntimeException(uri);
	}

	public TagConfig getTagConfig(String prefix, String tagName) {
		return null;
//		String uri = getTaglibUri(prefix);
//		TaglibConfig taglibConfig = getTaglibManager().getTaglibConfig(uri);
//		return taglibConfig.getTagConfig(tagName);
	}

	public TagConfig getTagConfig(String inject) {
		int index = inject.indexOf(':');
		if (index < 0) {
			throw new IllegalArgumentException(inject);
		}
		String prefix = inject.substring(0, index);
		String tagName = inject.substring(index + 1);
		return getTagConfig(prefix, tagName);
	}

	public TaglibManager getTaglibManager() {
		if (taglibManager != null) {
			return taglibManager;
		}
		throw new RuntimeException("taglibManager");
	}

	public void setTaglibManager(TaglibManager taglibManager) {
		this.taglibManager = taglibManager;
	}

	public boolean isAllowJavascript() {
		return false;
	}

	public void setAllowJavascript(boolean arg0) {
	}
}
