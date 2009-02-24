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
package org.seasar.s2jsfplugin.model.autoregister;

/**
 * shortClassNamesÇÇ†Ç∆Ç©ÇÁéÊìæÇ≈Ç´ÇÈÇÊÇ§Ç…ÇµÇΩClassPatternÇÃägí£ÉNÉâÉXÅB
 * 
 * @author Naoki Takezoe
 */
public class ClassPattern extends
		org.seasar.framework.container.autoregister.ClassPattern {

	private String shortClassName;
	
	public ClassPattern() {
		super();
	}

	public ClassPattern(String packageName, String shortClassNames) {
		super(packageName, shortClassNames);
	}

	public void setShortClassNames(String shortClassName) {
		this.shortClassName = shortClassName;
		super.setShortClassNames(shortClassName);
	}
	
	public String getShortClassNames(){
		return this.shortClassName;
	}
	
	
}
