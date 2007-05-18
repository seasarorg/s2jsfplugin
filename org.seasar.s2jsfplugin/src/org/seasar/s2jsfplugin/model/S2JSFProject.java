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
 * S2JSF�v���W�F�N�g�̊e����ɃA�N�Z�X���邽�߂̃N���X�B
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
		
		// dicon�t�@�C���̃p�[�X
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
		
		// TLD�̃X�L����
		try {
			scanTLD();
		} catch(Exception ex){
			Util.logException(ex);
		}
		
		// JsfConfig�I�u�W�F�N�g������
		config = new JsfConfig();
		Taglib[] taglibs = getTaglibs();
		for(int i=0;i<taglibs.length;i++){
			config.addTaglibUri(taglibs[i].getPrefix(),taglibs[i].getUri());
		}
	}
	
	private void scanTLD() throws Exception {
		/* �v���W�F�N�g�̃N���X�p�X�ɑ��݂���JAR�t�@�C������ǂݍ���
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
		
		// WEB-INF/tlds���X�L����
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
//	 * ���̃v���W�F�N�g�ɓo�^����Ă���^�O�Z���N�^�̈ꗗ���擾���܂��B
//	 */
//	public String[] getTagSelectors(){
//		return parser.getTagSelectors();
//	}
	
	public TLDInfo getTLDInfo(String uri){
		return (TLDInfo)tldMap.get(uri);
	}
	
	/**
	 * ���̃v���W�F�N�g�ɓo�^����Ă���}�l�[�W�h�E�r�[���̈ꗗ���擾���܂��B
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
	 * ���̃v���W�F�N�g�ɓo�^����Ă���taglib�̈ꗗ���擾���܂��B
	 * 
	 * @return
	 */
	public Taglib[] getTaglibs(){
		return parser.getTaglibs();
	}
	
	/**
	 * �v���t�B�b�N�X���w�肵��taglib�̏����擾���܂��B
	 * �Y��������̂��Ȃ������ꍇ�Anull��ԋp���܂��B
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
