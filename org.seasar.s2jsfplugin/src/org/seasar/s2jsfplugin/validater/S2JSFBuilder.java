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
package org.seasar.s2jsfplugin.validater;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.seasar.s2jsfplugin.Util;
import org.seasar.s2jsfplugin.model.S2JSFProject;
import org.seasar.s2jsfplugin.pref.S2JSFProjectParams;

/**
 * S2JSFプロジェクトのビルダ。HTMLファイルのバリデーションを実行します。
 * <ul>
 *   <li>*.htmlファイルが更新された場合、そのファイルのみバリデーション</li>
 *   <li>*.java、*.class、*.diconファイルが更新された場合、プロジェクト内の全ての*.htmlファイルをバリデーション</li>
 * </ul>
 * 
 * @author Naoki Takezoe
 */
public class S2JSFBuilder extends IncrementalProjectBuilder {

	public S2JSFBuilder() {
		super();
	}

	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		
		S2JSFProject project = new S2JSFProject(JavaCore.create(getProject()));
		S2JSFProjectParams params = new S2JSFProjectParams(getProject());
		
		IResourceDelta delta = getDelta(getProject());
		BuildInfo info = new BuildInfo();
		processDelta(delta, info, params);
		if(info.fullBuild){
			validateFolder(project,getProject(), params);
		} else {
			for(int i=0;i<info.changeFiles.size();i++){
				IPath path = (IPath)info.changeFiles.get(i);
				validateFile(project,path, params);
			}
		}
		getProject().refreshLocal(IResource.DEPTH_INFINITE,monitor);
		return null;
	}
	
	private void processDelta(IResourceDelta delta,BuildInfo info,S2JSFProjectParams params){
		// HTMLファイル
		if(isHTMLFile(delta.getProjectRelativePath().toString(),params)){
			if(delta.getKind()==IResourceDelta.REMOVED){
			}
			else if(delta.getKind()==IResourceDelta.CHANGED){
				info.changeFiles.add(delta.getProjectRelativePath());
			}
			else if(delta.getKind()==IResourceDelta.ADDED){
				info.changeFiles.add(delta.getProjectRelativePath());
			}
		}
		// faces-config.xml
		else if(delta.getProjectRelativePath().toString().endsWith("faces-config.xml")){
			if(delta.getKind()==IResourceDelta.REMOVED){
			}
			else if(delta.getKind()==IResourceDelta.CHANGED){
				info.changeFiles.add(delta.getProjectRelativePath());
			}
			else if(delta.getKind()==IResourceDelta.ADDED){
				info.changeFiles.add(delta.getProjectRelativePath());
			}
		}
		// 以下のファイルが更新された場合はフルビルドを行う
		else if(delta.getProjectRelativePath().toString().endsWith(".java")){
			if(delta.getKind()!=IResourceDelta.NO_CHANGE){
				info.fullBuild = true;
			}
		}
		else if(delta.getProjectRelativePath().toString().endsWith(".class")){
			if(delta.getKind()!=IResourceDelta.NO_CHANGE){
				info.fullBuild = true;
			}
		}
		else if(delta.getProjectRelativePath().toString().endsWith(".dicon")){
			if(delta.getKind()!=IResourceDelta.NO_CHANGE){
				info.fullBuild = true;
			}
		}
		else if(delta.getProjectRelativePath().toString().equals(".project")){
			if(delta.getKind()!=IResourceDelta.NO_CHANGE){
				info.fullBuild = true;
			}
		}
		else if(delta.getProjectRelativePath().toString().equals(".classpath")){
			if(delta.getKind()!=IResourceDelta.NO_CHANGE){
				info.fullBuild = true;
			}
		}
		IResourceDelta[] children = delta.getAffectedChildren();
		for(int i=0;i<children.length;i++){
			processDelta(children[i],info,params);
		}
	}
	
	/** フォルダをバリデーションします。 */
	private void validateFolder(S2JSFProject project,IContainer folder,S2JSFProjectParams params){
		try {
			IResource[] resources = folder.members();
			for(int i=0;i<resources.length;i++){
				if(resources[i] instanceof IFile){
					// HTMLファイル
					if(isHTMLFile(resources[i].getName(), params) && !resources[i].getName().startsWith(".")){
						validateFile(project,resources[i].getProjectRelativePath(),params);
					}
					// faces-config.xml
					if(resources[i].getName().endsWith("faces-config.xml") && !resources[i].getName().startsWith(".")){
						validateFile(project,resources[i].getProjectRelativePath(),params);
					}
				} else if(resources[i] instanceof IContainer){
					validateFolder(project,(IContainer)resources[i],params);
				}
			}
		} catch(Exception ex){
			Util.logException(ex);
		}
	}
	
	/** ファイルをバリデーションします。 */
	private void validateFile(S2JSFProject project,IPath path,S2JSFProjectParams params){
		try {
			IFile file = getProject().getFile(path);
			// ドットファイルはバリデーションしない
			if(file.getName().startsWith(".")){
				return;
			}
			if(file.getName().equals("faces-config.xml")){
				S2JSFNavigationValidator validator = new S2JSFNavigationValidator(project,file);
				validator.doValidate();
			} else {
				S2JSFHTMLValidator validator = new S2JSFHTMLValidator(project,file);
				validator.doValidate();
			}
		} catch(Exception ex){
			Util.logException(ex);
		}
	}
	
	private boolean isHTMLFile(String filename,S2JSFProjectParams params){
		String[] exts = params.getExtensions().split(",");
		for(int i=0;i<exts.length;i++){
			if(filename.endsWith("."+exts[i].trim())){
				return true;
			}
		}
		return false;
	}
	
	/** ビルド情報を格納するための内部クラス */
	private class BuildInfo {
		private ArrayList changeFiles = new ArrayList();
		private boolean fullBuild = false;
	}

}
