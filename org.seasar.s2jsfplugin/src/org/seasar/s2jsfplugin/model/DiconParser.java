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

import java.util.ArrayList;
import java.util.HashSet;

import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.FuzzyXMLParser;
import jp.aonir.fuzzyxml.XPath;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.seasar.s2jsfplugin.Util;
import org.seasar.s2jsfplugin.model.autoregister.AutoRegisterFactory;
import org.seasar.s2jsfplugin.model.autoregister.IAutoRegister;

/**
 * diconファイルをパースして必要な情報を取得します。
 * 
 * @author Naoki Takezoe
 */
public class DiconParser {
	
	private IJavaProject project;
	private HashSet   parsedFile   = new HashSet();
	private ArrayList managedBeans = new ArrayList();
	private ArrayList taglibs      = new ArrayList();
//	private ArrayList selectors    = new ArrayList();
	
	public DiconParser(IJavaProject project){
		this.project = project;
	}
	
	public void parse(IFile file) throws Exception {
		if(parsedFile.contains(file)){
			return;
		}
		parsedFile.add(file);
		
		FuzzyXMLDocument doc = new FuzzyXMLParser().parse(Util.getFile(file));
		FuzzyXMLElement root = doc.getDocumentElement();
		FuzzyXMLNode[] nodes = XPath.selectNodes(root,"components/*");
		String namespace = Util.getXPathValue(root,"components/@namespace");
		for(int i=0;i<nodes.length;i++){
			if(nodes[i] instanceof FuzzyXMLElement){
				FuzzyXMLElement element = (FuzzyXMLElement)nodes[i];
				// include要素
				if(element.getName().equals("include")){
					String path = Util.getXPathValue(element,"@path");
					IFile includeFile = null;
					IClasspathEntry[] entry = project.getRawClasspath();
					for(int j=0;j<entry.length;j++){
						if(entry[j].getEntryKind()==IClasspathEntry.CPE_SOURCE){
							includeFile = project.getProject().getFile(entry[j].getPath().removeFirstSegments(1).append(path));
							if(includeFile!=null && includeFile.exists()){
								break;
							}
						}
					}
					if(includeFile!=null && includeFile.exists()){
						parse(includeFile);
					}
				// component要素
				} else if(element.getName().equals("component")){
					String className = Util.getXPathValue(element,"@class");
					if(AutoRegisterFactory.isAutoRegister(className)){
						handleAutoRegister(className, element);
					} else if(namespace!=null && namespace.equals("jsf")){
						if(className.equals("org.seasar.jsf.runtime.JsfConfigImpl")){
							handleJsfConfigImpl(element);
						} else if(className.equals("org.seasar.jsf.runtime.TagProcessorTreeFactoryImpl")){
							handleTagProcessorTreeFactoryImpl(element);
						}
					} else {
						handleComponent(element);
					}
				}
			}
		}
	}
	
	private void handleAutoRegister(String className, FuzzyXMLElement element){
		IAutoRegister register = AutoRegisterFactory.getAutoRegister(className, element);
		register.setProject(project);
		register.registerAll();
		
		ManagedBean[] beans = register.getRegisteredBeans();
		for(int i=0;i<beans.length;i++){
			managedBeans.add(beans[i]);
		}
	}
	
	private void handleJsfConfigImpl(FuzzyXMLElement element){
		FuzzyXMLNode[] initMethod = XPath.selectNodes(element,"initMethod");
		for(int i=0;i<initMethod.length;i++){
			String prefix = Util.getXPathValue((FuzzyXMLElement)initMethod[i],"arg[1]");
			String uri    = Util.getXPathValue((FuzzyXMLElement)initMethod[i],"arg[2]");
			
			if(prefix!=null && !prefix.equals("") && uri!=null && !uri.equals("")){
				Taglib taglib = new Taglib(Util.decodeString(prefix),Util.decodeString(uri));
				taglibs.add(taglib);
			}
		}
	}
	
	private void handleTagProcessorTreeFactoryImpl(FuzzyXMLElement element){
//		FuzzyXMLNode[] initMethod = XPath.selectNodes(element,"initMethod");
//		for(int i=0;i<initMethod.length;i++){
//			String component = Util.getXPathValue((FuzzyXMLElement)initMethod[i],"arg[1]/component/@class");
//			if(component!=null && !component.equals("")){
//				selectors.add(component);
//			}
//		}
	}
	
	private void handleComponent(FuzzyXMLElement element){
		String clazz = Util.getXPathValue(element,"@class");
		String name  = Util.getXPathValue(element,"@name");
		String scope = Util.getXPathValue(element,"@instance");
		if(clazz != null && !clazz.equals("") && 
		   name  != null && !name.equals("")  //&& 
//		   scope != null && !scope.equals("")
		   ){
			ManagedBean bean = new ManagedBean(project,clazz,name,scope);
			managedBeans.add(bean);
		}
	}
	
//	public String[] getTagSelectors(){
//		return (String[])selectors.toArray(new String[selectors.size()]);
//	}
	
	/**
	 * このプロジェクトに登録されているマネージド・ビーンの一覧を取得します。
	 * 
	 * @return
	 */
	public ManagedBean[] getManagedBeans(){
		return (ManagedBean[])managedBeans.toArray(new ManagedBean[managedBeans.size()]);
	}
	
	/**
	 * このプロジェクトに登録されているtaglibの一覧を取得します。
	 * 
	 * @return
	 */
	public Taglib[] getTaglibs(){
		return (Taglib[])taglibs.toArray(new Taglib[taglibs.size()]);
	}
}
