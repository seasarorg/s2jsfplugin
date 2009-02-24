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

import java.util.ArrayList;
import java.util.List;

import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.XPath;

import org.eclipse.jdt.core.IJavaProject;
import org.seasar.framework.container.autoregister.AutoNaming;
import org.seasar.framework.container.autoregister.DefaultAutoNaming;
import org.seasar.s2jsfplugin.Util;
import org.seasar.s2jsfplugin.model.ManagedBean;

/**
 * IAutoRegisterの実装クラスのための基底クラス。
 * 
 * @author Naoki Takezoe
 */
public abstract class AbstractAutoRegister implements IAutoRegister {
	
	private IJavaProject project;
    private List classPatterns = new ArrayList();
    private List ignoreClassPatterns = new ArrayList();
	private List managedBeans = new ArrayList();
	private AutoNaming naming = new DefaultAutoNaming();
	private String scope = "singleton";
	
	public void init(FuzzyXMLElement element){
		project = null;
		classPatterns.clear();
		ignoreClassPatterns.clear();
		managedBeans.clear();
		naming = new DefaultAutoNaming();
		
		FuzzyXMLNode[] initMethod = XPath.selectNodes(element,"initMethod");
		for(int i=0;i<initMethod.length;i++){
			String name = ((FuzzyXMLElement)initMethod[i]).getAttributeValue("name"); 
			if("addClassPattern".equals(name)){
				String packageName = Util.getXPathValue((FuzzyXMLElement)initMethod[i],"arg[1]");
				String shortClassNames = Util.getXPathValue((FuzzyXMLElement)initMethod[i],"arg[2]");
				if(Util.isString(packageName) && Util.isString(shortClassNames)){
					addClassPattern(Util.decodeString(packageName),
							Util.decodeString(shortClassNames));
				}
			} else if("addIgnoreClassPattern".equals(name)){
				String packageName = Util.getXPathValue((FuzzyXMLElement)initMethod[i],"arg[1]");
				String shortClassNames = Util.getXPathValue((FuzzyXMLElement)initMethod[i],"arg[2]");
				if(Util.isString(packageName) && Util.isString(shortClassNames)){
					addIgnoreClassPattern(Util.decodeString(packageName),
							Util.decodeString(shortClassNames));
				}
			}
		}
		
		String instanceDef = Util.getXPathValue(element, "instanceDef");
		if(instanceDef!=null){
			setScope(ScopeUtil.getScope(instanceDef.trim()));
		}
	}
	
	public void setProject(IJavaProject project){
		this.project = project;
	}
	
	public IJavaProject getProject(){
		return this.project;
	}
	
	public void setAutoNaming(AutoNaming naming){
		if(naming!=null){
			this.naming = naming;
		}
	}
	
	public AutoNaming getAutoNaming(){
		return this.naming;
	}
	
	public void setScope(String scope){
		if(scope!=null){
			this.scope = scope;
		}
	}
	
	public String getScope(){
		return this.scope;
	}
	
	public ManagedBean[] getRegisteredBeans(){
		return (ManagedBean[])this.managedBeans.toArray(
				new ManagedBean[this.managedBeans.size()]);
	}
    
	public void addClassPattern(String packageName, String shortClassNames) {
		classPatterns.add(new ClassPattern(packageName, shortClassNames));
	}
	
	public List getClassPatterns(){
		return this.classPatterns;
	}
	
	public void addIgnoreClassPattern(String packageName, String shortClassNames) {
		ignoreClassPatterns.add(new ClassPattern(packageName, shortClassNames));
	}
	
	public List getIgnoreClassPatterns(){
		return this.ignoreClassPatterns;
	}
	
	/**
	 * 引数で指定したクラスを処理します。
	 * <p>
	 * 登録するクラスパターンにマッチしていればマネージド・ビーンとして登録し、
	 * そうでなければ何も行いません。
	 * 
	 * @param packageName パッケージ名
	 * @param shortClassName クラス名
	 */
	protected void processClass(String packageName, String shortClassName){
		if(isIgnore(packageName, shortClassName)){
			return;
		}
		if(isMatch(packageName, shortClassName)){
			ManagedBean bean = new ManagedBean(project, 
					packageName + "." + shortClassName, 
					naming.defineName(packageName, shortClassName),
					scope);
			managedBeans.add(bean);
		}
	}
	
	/**
	 * 引数で指定したクラスが登録するクラスパターンにマッチするかどうかを調べます。
	 */
	private boolean isMatch(String packageName, String shortClassName){
		if(classPatterns.isEmpty()){
			return false;
		}
		
        for (int i = 0; i < classPatterns.size(); ++i) {
            ClassPattern cp = (ClassPattern)classPatterns.get(i);
            if (cp.isAppliedPackageName(packageName) && cp.isAppliedShortClassName(shortClassName)) {
                return true;
            }
        }
        
        return false;
	}
	
	/**
	 * 引数で指定したクラスが無視するクラスパターンにマッチするかどうかを調べます。
	 */
    private boolean isIgnore(String packageName, String shortClassName) {
        if (ignoreClassPatterns.isEmpty()) {
            return false;
        }
        for (int i = 0; i < ignoreClassPatterns.size(); ++i) {
            ClassPattern cp = (ClassPattern) ignoreClassPatterns.get(i);
            if (!cp.isAppliedPackageName(packageName)) {
                continue;
            }
            if (cp.isAppliedShortClassName(shortClassName)) {
                return true;
            }
        }
        return false;
    }
}
