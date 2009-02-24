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

import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.FuzzyXMLParser;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.seasar.s2jsfplugin.Util;
import org.seasar.s2jsfplugin.pref.S2JSFProjectParams;

/**
 * faces-config.xml用のハイパーリンク機能を提供します。
 * 
 * @author Naoki Takezoe
 */
public class XMLHyperlinkProvider extends HTMLHyperlinkProvider {
	
	public XMLHyperlinkProvider(XMLEditor editor){
		super(editor);
	}
	
	protected IRegion selectWord(IDocument doc,int offset){
		FuzzyXMLDocument document = new FuzzyXMLParser().parse(doc.get());
		FuzzyXMLElement element = document.getElementByOffset(offset);
		if(element==null || !element.getName().equals("to-view-id")){
			return null;
		}
		if(element.getOffset() < offset && offset < element.getOffset() + element.getLength()){
			String value = element.getValue().trim();
			Object open = getOpenFileInfo(document,element,value);
			setOpenFile(open);
			if(open!=null){
				return new Region(getTextOffset(element),value.length());
			}
		}
		return null;
	}
	
	private int getTextOffset(FuzzyXMLElement element){
		String value = element.getValue();
		int offset = 0;
		for(int i=0;i<value.length();i++){
			char c = value.charAt(i);
			if(c!=' ' && c!='\t' && c!='\r' && c!='\n'){
				break;
			}
			offset++;
		}
		FuzzyXMLNode[] children = element.getChildren();
		if(children!=null && children.length>0){
			return children[0].getOffset() + offset;
		}
		return element.getOffset();
	}
	
	/**
	 * ハイパーリンクで開くファイルを取得。
	 */
	private Object getOpenFileInfo(FuzzyXMLDocument doc,FuzzyXMLElement element,String value){
		try {
			if(getProject()==null){
				return null;
			}

			IProject project = getProject().getJavaProject().getProject();
			S2JSFProjectParams params = new S2JSFProjectParams(project);
			IPath path = new Path(params.getRoot()).append(value);
			IResource resource = project.findMember(path);
			
			if(resource!=null && resource.exists() && resource instanceof IFile){
				return resource;
			}
		} catch(Exception ex){
			Util.logException(ex);
		}
		return null;
	}
}
