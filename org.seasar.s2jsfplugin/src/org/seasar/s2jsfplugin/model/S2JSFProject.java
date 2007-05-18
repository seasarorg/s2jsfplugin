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

import java.io.InputStream;
import java.util.HashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.seasar.s2jsfplugin.Util;
import org.seasar.s2jsfplugin.pref.S2JSFProjectParams;

/**
 * S2JSFプロジェクトの各種情報にアクセスするためのクラス。
 * 
 * @author Naoki Takezoe
 */
public class S2JSFProject {
	
	private IJavaProject project;
	private DiconParser parser;
	private HashMap tldMap = new HashMap();
	private JsfConfig config;
	
	public S2JSFProject(IJavaProject project){
		this.project = project;
		this.parser = new DiconParser(project);
		
		// diconファイルのパース
		try {
			IFile file = null;
			IClasspathEntry[] entry = project.getRawClasspath();
			for(int i=0;i<entry.length;i++){
				if(entry[i].getEntryKind()==IClasspathEntry.CPE_SOURCE){
					file = project.getProject().getFile(entry[i].getPath().removeFirstSegments(1).append("app.dicon"));
					if(file!=null && file.exists()){
						break;
					}
				}
			}
			if(file!=null && file.exists()){
				parser.parse(file);
			}
			
		} catch(Exception ex){
			Util.logException(ex);
		}
		
		// TLDのスキャン
		try {
			scanTLD();
		} catch(Exception ex){
			Util.logException(ex);
		}
		
		// JsfConfigオブジェクトを準備
		config = new JsfConfig();
		Taglib[] taglibs = getTaglibs();
		for(int i=0;i<taglibs.length;i++){
			config.addTaglibUri(taglibs[i].getPrefix(),taglibs[i].getUri());
		}
	}
	
	private void scanTLD() throws Exception {
		/* プロジェクトのクラスパスに存在するJARファイルから読み込み
		IClasspathEntry[] entry = project.getRawClasspath();
		for(int i=0;i<entry.length;i++){
			if(entry[i].getEntryKind()==IClasspathEntry.CPE_LIBRARY){
				IPackageFragmentRoot root = project.getPackageFragmentRoot(entry[i].getPath().toString());
				IJavaElement[] children = root.getChildren();
				for(int j=0;j<children.length;j++){
					if(children[j].getElementName().equals("META-INF")){
						IPackageFragment metaInf = (IPackageFragment)children[j];
						Object[] tlds = metaInf.getNonJavaResources();
						for(int k=0;k<tlds.length;k++){
							IStorage file = (IStorage)tlds[k];
							if(file.getName().endsWith(".tld")){
								TLDInfo info = new TLDParser().parse(file.getContents());
								tldMap.put(info.getUri(),info);
							}
						}
						continue;
					}
				}
			}
		}
		*/
		String[] tld = new String[]{
			"myfaces_core.tld","myfaces_html.tld","tomahawk.tld","s2jsf.tld"
		};
		for(int i=0;i<tld.length;i++){
			InputStream in = getClass().getResourceAsStream("/TLD/" + tld[i]);
			TLDInfo info = new TLDParser().parse(in);
			tldMap.put(info.getUri(),info);
			in.close();
		}
		
		// WEB-INF/tldsをスキャン
		S2JSFProjectParams params = new S2JSFProjectParams(this.project.getProject());
		IResource resource = this.project.getProject().findMember(new Path(params.getRoot() + "/WEB-INF/tlds"));
		if(resource!=null && resource.exists() && resource instanceof IContainer){
			IContainer container = (IContainer) resource;
			IResource[] children = container.members();
			for(int i=0;i<children.length;i++){
				if(children[i] instanceof IFile && children[i].getName().endsWith(".tld")){
					InputStream in = ((IFile) children[i]).getContents();
					TLDInfo info = new TLDParser().parse(in);
					tldMap.put(info.getUri(),info);
					in.close();
				}
			}
		}
	}
	
	public JsfConfig getJsfConfig(){
		return config;
	}
	
//	/**
//	 * このプロジェクトに登録されているタグセレクタの一覧を取得します。
//	 */
//	public String[] getTagSelectors(){
//		return parser.getTagSelectors();
//	}
	
	public TLDInfo getTLDInfo(String uri){
		return (TLDInfo)tldMap.get(uri);
	}
	
	/**
	 * このプロジェクトに登録されているマネージド・ビーンの一覧を取得します。
	 * 
	 * @return
	 */
	public ManagedBean[] getManagedBeans(){
		return parser.getManagedBeans();
	}
	
	public ManagedBean getManagedBean(String beanName){
		ManagedBean[] beans = getManagedBeans();
		for(int i=0;i<beans.length;i++){
			if(beans[i].getBeanName().equals(beanName)){
				return beans[i];
			}
		}
		return null;
	}
	
	/**
	 * このプロジェクトに登録されているtaglibの一覧を取得します。
	 * 
	 * @return
	 */
	public Taglib[] getTaglibs(){
		return parser.getTaglibs();
	}
	
	/**
	 * プレフィックスを指定してtaglibの情報を取得します。
	 * 該当するものがなかった場合、nullを返却します。
	 * 
	 * @param prefix
	 * @return
	 */
	public Taglib getTaglib(String prefix){
		Taglib[] taglibs = getTaglibs();
		for(int i=0;i<taglibs.length;i++){
			if(taglibs[i].getPrefix().equals(prefix)){
				return taglibs[i];
			}
		}
		return null;
	}
	
	public IJavaProject getJavaProject(){
		return project;
	}
	
}
