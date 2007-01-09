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
package org.seasar.s2jsfplugin.model.autoregister;

import java.util.ArrayList;
import java.util.List;

import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.XPath;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.seasar.s2jsfplugin.Util;
import org.seasar.s2jsfplugin.model.ManagedBean;

/**
 * <code>ComponentAutoRegister</code>の動作をエミュレートするIAutoRegisterの実装。
 * 
 * @author Naoki Takezoe
 */
public class ComponentAutoRegister extends AbstractAutoRegister {

	private List referenceClasses = new ArrayList();
	private IAutoRegister register;
	
	public void init(FuzzyXMLElement element){
		super.init(element);
		register = null;
		referenceClasses.clear();
		
		FuzzyXMLNode[] initMethod = XPath.selectNodes(element,"initMethod");
		for(int i=0;i<initMethod.length;i++){
			String name = ((FuzzyXMLElement)initMethod[i]).getAttributeValue("name"); 
			if("addReferenceClass".equals(name)){
				String referenceClass = Util.getXPathValue((FuzzyXMLElement)initMethod[i],"arg[0]");
				if(referenceClass.startsWith("@") && referenceClass.endsWith("@class")){
					String className = referenceClass.substring(1, referenceClass.length()-6);
					addReferenceClass(className);
				}
			}
		}
	}
	
	public void addReferenceClass(String className){
		referenceClasses.add(className);
	}
	
	public ManagedBean[] getRegisteredBeans() {
		if(register!=null){
			return register.getRegisteredBeans();
		} else {
			return new ManagedBean[0];
		}
	}

	public void registerAll() {
		try {
			for(int i=0;i<referenceClasses.size();i++){
				String referenceClass = (String)referenceClasses.get(i);
				IType type = getProject().findType(referenceClass);
				if(!type.exists()){
					return;
				}
				IJavaElement parent = type.getPackageFragment().getParent();
				if(parent.getElementName().endsWith(".jar")){
					// JARの場合
					register = new JarAutoRegister();
					((JarAutoRegister)register).setReferenceClass(referenceClass);
				} else {
					// JARじゃない場合
					register = new FileSystemAutoRegister();
				}
				
				register.setProject(getProject());
				register.setScope(getScope());
				
				List classPatterns = getClassPatterns();
				for(int j=0;j<classPatterns.size();j++){
					ClassPattern pattern = (ClassPattern)classPatterns.get(j);
					register.addClassPattern(
							pattern.getPackageName(), 
							pattern.getShortClassNames());
				}
				
				List ignorePatterns = getIgnoreClassPatterns();
				for(int j=0;j<ignorePatterns.size();j++){
					ClassPattern pattern = (ClassPattern)ignorePatterns.get(j);
					register.addIgnoreClassPattern(
							pattern.getPackageName(), 
							pattern.getShortClassNames());
				}
				
				register.registerAll();
			}
		} catch(Exception ex){
			Util.logException(ex);
		}
	}

}
