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
package org.seasar.s2jsfplugin.model;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.seasar.s2jsfplugin.Util;

/**
 * @author Naoki Takezoe
 */
public class ManagedBeanProperty {
	
	private IType  parent;
	private String propertyName;
	private String propertyType;
	private IMethod method;
	
	public ManagedBeanProperty(IType parent,String propertyName,String propertyType,IMethod method){
		this.parent = parent;
		this.propertyName = propertyName;
		this.propertyType = propertyType;
		this.method = method;
	}
	
	public String getJavadoc(){
		try {
			return Util.extractJavadoc(method, null);
		} catch(Exception ex){
			return null;
		}
	}
	
	/**
	 * @return propertyName を戻します。
	 */
	public String getPropertyName() {
		return propertyName;
	}
	/**
	 * @return propertyType を戻します。
	 */
	public String getPropertyType() {
		return propertyType;
	}
	
	public ManagedBean toManagedBean(){
		String clazz = Util.getFullQName(parent,propertyType);
		return new ManagedBean(parent.getJavaProject(),clazz,propertyName,null);
	}
	
}
